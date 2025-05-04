package CodeGenerator;

import java.io.*;
import java.util.*;

import TabelaSimbolos.*;
import Tuga.*;
import ConstantPool.ConstantPool;
import VM.OpCode;
import VM.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CodeGen extends TugaBaseVisitor<Void> {
    private final ArrayList<Instruction> code = new ArrayList<>();
    private final TypeChecker typeChecker;
    private ConstantPool constantPool;
    private TabelaSimbolos tabelaSimbolos;
    private final Map<String, Integer> labelsFuncoes = new HashMap<>();
    private String currentFunction = "";
    private Map<String, Integer> localVars = new HashMap<>();
    private int nextLocalVarIndex = 1; // Start at 1 since parameters have negative indices
    private int functionParamCount = 0;

    public CodeGen(TypeChecker checker, ConstantPool constantPool, TabelaSimbolos tabelaSimbolos) {
        this.typeChecker = checker;
        this.constantPool = constantPool;
        this.tabelaSimbolos = tabelaSimbolos;
    }

    private void visitAndConvert(ParseTree expr, Tipo target) {
        Tipo origem = typeChecker.getTipo(expr);
        visit(expr); // visit first
        if (origem != target && origem != Tipo.ERRO && target != Tipo.ERRO) {
            emitConversion(origem, target);
        }
    }

    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        // Generate initial jump to 'principal'
        int jumpToMain = code.size();
        emit(OpCode.call, 0); // Placeholder, will be fixed later
        emit(OpCode.halt);

        // Visit all functions (this defines labels)
        for (var func : ctx.functionDecl()) {
            visit(func);
        }

        // Fix the jump to 'principal'
        Integer labelPrincipal = labelsFuncoes.get("principal");
        if (labelPrincipal == null) {
            System.err.println("Error: principal() function not found.");
        } else {
            ((Instruction1Arg) code.get(jumpToMain)).setArg(labelPrincipal);
        }

        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        currentFunction = nome;
        localVars.clear();
        nextLocalVarIndex = 1;

        // Count parameters for this function
        functionParamCount = (ctx.formalParameters() != null) ?
                ctx.formalParameters().formalParameter().size() : 0;

        // Save the label for the function (current code position)
        int label = code.size();
        labelsFuncoes.put(nome, label);

        // Count local variable declarations in function body
        int localVarCount = countLocalVars(ctx.bloco());
        if (localVarCount > 0) {
            emit(OpCode.lalloc, localVarCount);
        }

        // Visit function body
        visit(ctx.bloco());

        // Ensure function has a return statement if it's not a void function
        boolean hasExplicitReturn = hasReturnStatement(ctx.bloco());
        if (!hasExplicitReturn) {
            // For void functions, add default return
            if (ctx.TYPE() == null) {
                emit(OpCode.ret, functionParamCount);
            }
        }

        currentFunction = "";
        return null;
    }

    private int countLocalVars(TugaParser.BlocoContext ctx) {
        int count = 0;
        for (var varDecl : ctx.varDeclaration()) {
            count += varDecl.ID().size();
        }
        return count;
    }

    private boolean hasReturnStatement(TugaParser.BlocoContext ctx) {
        for (var stat : ctx.stat()) {
            if (stat instanceof TugaParser.RetornaContext) {
                return true;
            } else if (stat instanceof TugaParser.BlocoStatContext) {
                if (hasReturnStatement(((TugaParser.BlocoStatContext) stat).bloco())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        if (currentFunction.isEmpty()) {
            // Global variables
            int count = ctx.ID().size();
            if (count > 0) {
                emit(OpCode.galloc, count);
            }
        } else {
            // Local variables - map names to stack indices
            for (var id : ctx.ID()) {
                String varName = id.getText();
                localVars.put(varName, nextLocalVarIndex++);
            }
        }
        return null;
    }

    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        String nome = ctx.ID().getText();
        VarSimbolo simbolo = tabelaSimbolos.getVar(nome);
        if (simbolo == null) return null;

        Tipo tipoAlvo = simbolo.getTipo();
        visitAndConvert(ctx.expr(), tipoAlvo);

        // Determine if this is a local, parameter, or global variable
        if (!currentFunction.isEmpty() && localVars.containsKey(nome)) {
            // Local variable
            emit(OpCode.lstore, localVars.get(nome));
        } else if (simbolo.getIndex() < 0) {
            // Parameter (negative index)
            emit(OpCode.lstore, simbolo.getIndex());
        } else {
            // Global variable
            emit(OpCode.gstore, simbolo.getIndex());
        }

        return null;
    }

    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        String nome = ctx.ID().getText();
        VarSimbolo simbolo = tabelaSimbolos.getVar(nome);
        if (simbolo == null) return null;

        // Determine if this is a local, parameter, or global variable
        if (!currentFunction.isEmpty() && localVars.containsKey(nome)) {
            // Local variable
            emit(OpCode.lload, localVars.get(nome));
        } else if (simbolo.getIndex() < 0) {
            // Parameter (negative index)
            emit(OpCode.lload, simbolo.getIndex());
        } else {
            // Global variable
            emit(OpCode.gload, simbolo.getIndex());
        }

        return null;
    }

    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        String nome = ctx.ID().getText();
        FuncaoSimbolo func = tabelaSimbolos.getFuncao(nome);
        if (func == null) return null;

        // Evaluate and push all arguments in order
        if (ctx.exprList() != null) {
            for (var expr : ctx.exprList().expr()) {
                visit(expr);
            }
        }

        // Call the function
        Integer label = labelsFuncoes.get(nome);
        if (label != null) {
            emit(OpCode.call, label);
        } else {
            System.err.println("Error: Function label not found for " + nome);
        }

        return null;
    }

    @Override
    public Void visitChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx) {
        String nome = ctx.ID().getText();
        FuncaoSimbolo func = tabelaSimbolos.getFuncao(nome);
        if (func == null) return null;

        // Push single argument if present
        if (ctx.expr() != null) {
            visit(ctx.expr());
        }

        // Call the function
        Integer label = labelsFuncoes.get(nome);
        if (label != null) {
            emit(OpCode.call, label);
        } else {
            System.err.println("Error: Function label not found for " + nome);
        }

        return null;
    }

    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        if (ctx.expr() != null) {
            visit(ctx.expr());
            emit(OpCode.retval, functionParamCount);
        } else {
            emit(OpCode.ret, functionParamCount);
        }
        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        // Visit variable declarations first
        for (var varDecl : ctx.varDeclaration()) {
            visit(varDecl);
        }

        // Then visit statements
        for (var stat : ctx.stat()) {
            visit(stat);
        }
        return null;
    }

    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx.expr());

        switch (tipo) {
            case INT -> emit(OpCode.iprint);
            case REAL -> emit(OpCode.dprint);
            case BOOL -> emit(OpCode.bprint);
            case STRING -> emit(OpCode.sprint);
        }
        return null;
    }

    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        int beginWhile = code.size();

        // Evaluate condition (must be boolean)
        visitAndConvert(ctx.expr(), Tipo.BOOL);

        // Jump to end if condition is false
        emit(OpCode.jumpf, 0);
        int jumpFIndex = code.size() - 1;

        // Execute the statement
        visit(ctx.stat());

        // Jump back to condition
        emit(OpCode.jump, beginWhile);

        // Fix the jumpf instruction to point past the loop
        ((Instruction1Arg) code.get(jumpFIndex)).setArg(code.size());

        return null;
    }

    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        // Evaluate condition (must be boolean)
        visitAndConvert(ctx.expr(), Tipo.BOOL);

        // Jump to else/end if condition is false
        emit(OpCode.jumpf, 0);
        int jumpFIndex = code.size() - 1;

        // Execute 'if' branch
        visit(ctx.stat(0));

        // If there's an 'else' branch
        if (ctx.stat().size() > 1) {
            // Jump past else branch when 'if' branch completes
            emit(OpCode.jump, 0);
            int jumpIndex = code.size() - 1;

            // Fix jumpf to point to else branch
            ((Instruction1Arg) code.get(jumpFIndex)).setArg(code.size());

            // Execute 'else' branch
            visit(ctx.stat(1));

            // Fix jump to point to end of if-else
            ((Instruction1Arg) code.get(jumpIndex)).setArg(code.size());
        } else {
            // No else branch, fix jumpf to point to end of if
            ((Instruction1Arg) code.get(jumpFIndex)).setArg(code.size());
        }

        return null;
    }

    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.parseInt(ctx.getText()));
        return null;
    }

    @Override
    public Void visitReal(TugaParser.RealContext ctx) {
        double val = Double.parseDouble(ctx.getText());
        int index = constantPool.addDouble(val);
        emit(OpCode.dconst, index);
        return null;
    }

    @Override
    public Void visitBool(TugaParser.BoolContext ctx) {
        if (ctx.getText().equals("verdadeiro")) {
            emit(OpCode.tconst);
        } else {
            emit(OpCode.fconst);
        }
        return null;
    }

    @Override
    public Void visitString(TugaParser.StringContext ctx) {
        String text = ctx.getText();
        // Remove quotes
        text = text.substring(1, text.length() - 1);
        int index = constantPool.addString(text);
        emit(OpCode.sconst, index);
        return null;
    }

    @Override
    public Void visitParens(TugaParser.ParensContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx.expr());

        if (ctx.op.getType() == TugaParser.MINUS) {
            if (tipo == Tipo.INT) {
                emit(OpCode.iuminus);
            } else if (tipo == Tipo.REAL) {
                emit(OpCode.duminus);
            }
        } else if (ctx.op.getType() == TugaParser.NOT) {
            if (tipo == Tipo.BOOL) {
                emit(OpCode.not);
            }
        }
        return null;
    }

    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        Tipo tipo = typeChecker.getTipo(ctx);
        String op = ctx.op.getText();

        if (tipo == Tipo.STRING && op.equals("+")) {
            // String concatenation
            visitAndConvert(ctx.expr(0), Tipo.STRING);
            visitAndConvert(ctx.expr(1), Tipo.STRING);
            emit(OpCode.sconcat);
        } else {
            // Numeric operations
            visitAndConvert(ctx.expr(0), tipo);
            visitAndConvert(ctx.expr(1), tipo);

            if (tipo == Tipo.INT) {
                if (op.equals("+")) {
                    emit(OpCode.iadd);
                } else {
                    emit(OpCode.isub);
                }
            } else if (tipo == Tipo.REAL) {
                if (op.equals("+")) {
                    emit(OpCode.dadd);
                } else {
                    emit(OpCode.dsub);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        Tipo tipo = typeChecker.getTipo(ctx);
        String op = ctx.op.getText();

        visitAndConvert(ctx.expr(0), tipo);
        visitAndConvert(ctx.expr(1), tipo);

        if (tipo == Tipo.INT) {
            switch (op) {
                case "*" -> emit(OpCode.imult);
                case "/" -> emit(OpCode.idiv);
                case "%" -> emit(OpCode.imod);
            }
        } else if (tipo == Tipo.REAL) {
            switch (op) {
                case "*" -> emit(OpCode.dmult);
                case "/" -> emit(OpCode.ddiv);
            }
        }
        return null;
    }

    @Override
    public Void visitAnd(TugaParser.AndContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.and);
        return null;
    }

    @Override
    public Void visitOr(TugaParser.OrContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.or);
        return null;
    }

    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));
        String op = ctx.op.getText();

        // Determine common type for comparison
        Tipo tipoComp;
        if (t1 == Tipo.REAL || t2 == Tipo.REAL) {
            tipoComp = Tipo.REAL;
        } else if (t1 == Tipo.INT && t2 == Tipo.INT) {
            tipoComp = Tipo.INT;
        } else if (t1 == Tipo.STRING && t2 == Tipo.STRING) {
            tipoComp = Tipo.STRING;
        } else if (t1 == Tipo.BOOL && t2 == Tipo.BOOL) {
            tipoComp = Tipo.BOOL;
        } else {
            tipoComp = Tipo.ERRO;
        }

        // Visit and convert both expressions
        visitAndConvert(ctx.expr(0), tipoComp);
        visitAndConvert(ctx.expr(1), tipoComp);

        // Emit appropriate comparison operation
        switch (tipoComp) {
            case INT -> {
                switch (op) {
                    case "<" -> emit(OpCode.ilt);
                    case ">" -> emit(OpCode.ilt); // Swapped operands
                    case "<=" -> emit(OpCode.ileq);
                    case ">=" -> emit(OpCode.ileq); // Swapped operands
                    case "igual" -> emit(OpCode.ieq);
                    case "diferente" -> emit(OpCode.ineq);
                }
            }
            case REAL -> {
                switch (op) {
                    case "<" -> emit(OpCode.dlt);
                    case ">" -> emit(OpCode.dlt); // Swapped operands
                    case "<=" -> emit(OpCode.dleq);
                    case ">=" -> emit(OpCode.dleq); // Swapped operands
                    case "igual" -> emit(OpCode.deq);
                    case "diferente" -> emit(OpCode.dneq);
                }
            }
            case STRING -> {
                switch (op) {
                    case "igual" -> emit(OpCode.seq);
                    case "diferente" -> emit(OpCode.sneq);
                }
            }
            case BOOL -> {
                switch (op) {
                    case "igual" -> emit(OpCode.beq);
                    case "diferente" -> emit(OpCode.bneq);
                }
            }
        }
        return null;
    }

    private void emitConversion(Tipo de, Tipo para) {
        if (de == para) return;

        switch (de) {
            case INT -> {
                if (para == Tipo.REAL) emit(OpCode.itod);
                else if (para == Tipo.STRING) emit(OpCode.itos);
            }
            case REAL -> {
                if (para == Tipo.STRING) emit(OpCode.dtos);
            }
            case BOOL -> {
                if (para == Tipo.STRING) emit(OpCode.btos);
            }
        }
    }

    public void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    public void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    public void dumpCode() {
        System.out.println("*** Instructions ***");
        for (int i = 0; i < code.size(); i++)
            System.out.println(i + ": " + code.get(i));
    }

    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            for (Instruction inst : code)
                inst.writeTo(dout);
        }
    }

    public List<Instruction> getCode() {
        return code;
    }
}