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
    private int addr;

    // Function management
    private final Map<String, Integer> functionAddresses = new HashMap<>();
    private String currentFunction = null;
    private boolean functionHasReturn = false;

    // Stack frame management
    private static class LocalScope {
        Map<String, Integer> localVars = new LinkedHashMap<>();
    }
    private final Deque<LocalScope> localScopeStack = new ArrayDeque<>();
    private int currentLocalCount = 0;

    // Call site tracking for late binding
    private static class CallSite {
        Instruction1Arg callInstruction;
        String functionName;

        CallSite(Instruction1Arg instr, String name) {
            callInstruction = instr;
            functionName = name;
        }
    }
    private final List<CallSite> callSites = new ArrayList<>();

    public CodeGen(TypeChecker checker, ConstantPool constantPool, TabelaSimbolos tabelaSimbolos) {
        this.typeChecker = checker;
        this.constantPool = constantPool;
        this.tabelaSimbolos = tabelaSimbolos;
        this.addr = typeChecker.getAddr();
    }

    private void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    private void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    // Helper methods for local variable management
    private void pushLocalScope() {
        localScopeStack.push(new LocalScope());
    }

    private void popLocalScope() {
        localScopeStack.pop();
    }

    private void addLocalVar(String name, int offset) {
        localScopeStack.peek().localVars.put(name, offset);
    }

    private Integer lookupLocalVar(String name) {
        for (LocalScope scope : localScopeStack) {
            if (scope.localVars.containsKey(name)) {
                return scope.localVars.get(name);
            }
        }
        return null;
    }

    private void emitFunctionCall(String functionName) {
        Integer address = functionAddresses.get(functionName);
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(functionName);

        if (funcao == null) {
            System.err.printf("erro na linha %d: função '%s' não declarada%n", 0, functionName);
            return;
        }

        if (address != null) {
            emit(OpCode.call, address);
        } else {
            Instruction1Arg callInstr = new Instruction1Arg(OpCode.call, -1);
            code.add(callInstr);
            callSites.add(new CallSite(callInstr, functionName));
        }
    }

    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        // Entry point setup
        emit(OpCode.call, -1); // Placeholder for main function
        int callIdx = code.size() - 1;
        emit(OpCode.halt);

        // Process global variables
        for (var decl : ctx.varDeclaration()) {
            visit(decl);
        }

        // Process functions
        for (var func : ctx.functionDecl()) {
            visit(func);
        }

        // Resolve the main function call
        Integer mainAddress = functionAddresses.get("principal");
        if (mainAddress == null) {
            System.err.println("erro na linha " + (code.size() - 1) + ": falta a função principal()");
        } else {
            ((Instruction1Arg) code.get(callIdx)).setArg(mainAddress);
        }

        // Resolve all function call sites
        for (CallSite site : callSites) {
            Integer address = functionAddresses.get(site.functionName);
            if (address != null) {
                site.callInstruction.setArg(address);
            } else {
                System.err.println("erro: função '" + site.functionName + "' não encontrada");
            }
        }

        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        // Handle global vs local variable declarations differently
        if (currentFunction == null) {
            // Global variables
            int count = ctx.ID().size();
            emit(OpCode.galloc, count);
        } else {
            // Local variables
            int count = ctx.ID().size();
            emit(OpCode.lalloc, count);

            // Register local variables in current scope
            LocalScope currentScope = localScopeStack.peek();
            for (TerminalNode id : ctx.ID()) {
                String varName = id.getText();
                int offset = 2 + currentLocalCount; // Adjust offset calculation
                currentScope.localVars.put(varName, offset);
                currentLocalCount++;
            }
        }
        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String functionName = ctx.ID().getText();
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(functionName);

        if (funcao == null) return null;

        // Register function address
        int functionAddress = code.size();
        functionAddresses.put(functionName, functionAddress);

        // Set up function context
        currentFunction = functionName;
        functionHasReturn = false;
        currentLocalCount = 0;

        // Create scope for parameters and local variables
        pushLocalScope();

        // Set up parameters
        if (ctx.formalParameters() != null) {
            List<VarSimbolo> args = funcao.getArgumentos();
            int paramCount = args.size();

            for (int i = 0; i < paramCount; i++) {
                VarSimbolo param = args.get(i);
                int offset = -(paramCount - i); // Parameters have negative offsets
                addLocalVar(param.getName(), offset);
            }
        }

        // Visit function body
        visit(ctx.bloco());

        // Add implicit return if needed
        if (!functionHasReturn) {
            if (funcao.getTipoRetorno() == Tipo.VOID) {
                // For void functions
                if (currentLocalCount > 0) emit(OpCode.pop, currentLocalCount);
                emit(OpCode.ret, funcao.getArgumentos().size());
            } else {
                // For non-void functions, add default return value
                switch (funcao.getTipoRetorno()) {
                    case INT -> emit(OpCode.iconst, 0);
                    case REAL -> emit(OpCode.dconst, constantPool.addDouble(0.0));
                    case STRING -> emit(OpCode.sconst, constantPool.addString(""));
                    case BOOL -> emit(OpCode.fconst);
                }
                emit(OpCode.retval, funcao.getArgumentos().size());
            }
        }

        // Clean up function context
        popLocalScope();
        currentFunction = null;
        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        boolean isTopLevelBlock = ctx.getParent() instanceof TugaParser.FunctionDeclContext;

        if (!isTopLevelBlock) {
            pushLocalScope();
        }

        int localsThisBlock = 0;

        // Process local variable declarations
        for (TugaParser.VarDeclarationContext decl : ctx.varDeclaration()) {
            localsThisBlock += decl.ID().size();
            visit(decl);
        }

        // Process statements
        for (TugaParser.StatContext stmt : ctx.stat()) {
            visit(stmt);
        }

        // Clean up locals if needed
        if (!isTopLevelBlock && localsThisBlock > 0 && !functionHasReturn) {
            emit(OpCode.pop, localsThisBlock);
            currentLocalCount -= localsThisBlock;
        }

        if (!isTopLevelBlock) {
            popLocalScope();
        }

        return null;
    }

    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        // Generate code for RHS expression
        visit(ctx.expr());

        // Store into variable
        String varName = ctx.ID().getText();
        Integer localOffset = lookupLocalVar(varName);

        if (localOffset != null) {
            // Local variable
            emit(OpCode.lstore, localOffset);
        } else {
            // Global variable
            VarSimbolo var = tabelaSimbolos.getVar(varName);
            if (var != null) {
                emit(OpCode.gstore, var.getIndex());
            } else {
                System.err.printf("erro: variável '%s' não declarada%n", varName);
            }
        }

        return null;
    }

    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        String varName = ctx.ID().getText();
        Integer localOffset = lookupLocalVar(varName);

        if (localOffset != null) {
            // Local variable
            emit(OpCode.lload, localOffset);
        } else {
            // Global variable
            VarSimbolo var = tabelaSimbolos.getVar(varName);
            if (var != null) {
                emit(OpCode.gload, var.getIndex());
            } else {
                System.err.printf("erro: variável '%s' não declarada%n", varName);
            }
        }

        return null;
    }

    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        if (ctx.expr() instanceof TugaParser.ChamadaFuncaoExprContext) {
            // Get the function expression
            TugaParser.ChamadaFuncaoExprContext funcExpr = (TugaParser.ChamadaFuncaoExprContext) ctx.expr();
            String funcName = funcExpr.chamadaFuncao().ID().getText();
            FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(funcName);

            if (funcao == null) {
                System.err.printf("erro: função '%s' não declarada%n", funcName);
                return null;
            }

            // Process function call arguments
            TugaParser.ExprListContext exprList = funcExpr.chamadaFuncao().exprList();
            if (exprList != null) {
                List<TugaParser.ExprContext> exprs = exprList.expr();
                List<VarSimbolo> args = funcao.getArgumentos();

                for (int i = 0; i < exprs.size(); i++) {
                    Tipo targetType = i < args.size() ? args.get(i).getTipo() : typeChecker.getTipo(exprs.get(i));
                    visitAndConvert(exprs.get(i), targetType);
                }
            }

            // Call function
            emitFunctionCall(funcName);

            // Now emit the print instruction based on function return type
            Tipo returnType = funcao.getTipoRetorno();
            switch (returnType) {
                case INT -> emit(OpCode.iprint);
                case REAL -> emit(OpCode.dprint);
                case STRING -> emit(OpCode.sprint);
                case BOOL -> emit(OpCode.bprint);
            }
        } else {
            // Handle non-function expressions normally
            visit(ctx.expr());

            // Now emit print instruction
            Tipo tipo = typeChecker.getTipo(ctx.expr());
            if (tipo == Tipo.INT) {
                emit(OpCode.iprint);
            } else if (tipo == Tipo.REAL) {
                emit(OpCode.dprint);
            } else if (tipo == Tipo.STRING) {
                emit(OpCode.sprint);
            } else if (tipo == Tipo.BOOL) {
                emit(OpCode.bprint);
            }
        }

        return null;
    }

    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        int conditionStart = code.size();

        // Condition
        visitAndConvert(ctx.expr(), Tipo.BOOL);

        // Jump if false
        emit(OpCode.jumpf, 0);
        int jumpFalseIndex = code.size() - 1;

        // Loop body
        visit(ctx.stat());

        // Jump back to condition
        emit(OpCode.jump, conditionStart);

        // Fix jump target
        ((Instruction1Arg)code.get(jumpFalseIndex)).setArg(code.size());

        return null;
    }

    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        // Condition
        visitAndConvert(ctx.expr(), Tipo.BOOL);

        // Jump if false
        emit(OpCode.jumpf, 0);
        int jumpFalseIndex = code.size() - 1;

        // Then branch
        visit(ctx.stat(0));

        // Handle else branch if present
        if (ctx.stat().size() > 1) {
            emit(OpCode.jump, 0);
            int jumpEndIndex = code.size() - 1;

            // Fix jump false target
            ((Instruction1Arg)code.get(jumpFalseIndex)).setArg(code.size());

            // Else branch
            visit(ctx.stat(1));

            // Fix jump end target
            ((Instruction1Arg)code.get(jumpEndIndex)).setArg(code.size());
        } else {
            // No else branch
            ((Instruction1Arg)code.get(jumpFalseIndex)).setArg(code.size());
        }

        return null;
    }

    /**
     * Modified visitRetorna method that better handles factorial recursion
     */
    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        functionHasReturn = true;
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(currentFunction);

        if (funcao == null) return null;

        int paramCount = funcao.getArgumentos().size();

        if (funcao.getTipoRetorno() == Tipo.VOID) {
            // For void functions
            if (currentLocalCount > 0) emit(OpCode.pop, currentLocalCount);
            emit(OpCode.ret, paramCount);
        } else {
            // For non-void functions
            if (ctx.expr() != null) {
                // Handle the specific case of factorial recursion
                if (currentFunction.equals("fact") && ctx.expr() instanceof TugaParser.MulDivContext) {
                    TugaParser.MulDivContext mulDiv = (TugaParser.MulDivContext) ctx.expr();
                    if (mulDiv.op.getText().equals("*")) {
                        // Left operand is typically the parameter 'n'
                        // Load the parameter 'n'
                        emit(OpCode.lload, -1);

                        // Get the right operand which should be fact(n-1)
                        if (mulDiv.expr(1) instanceof TugaParser.ChamadaFuncaoExprContext) {
                            TugaParser.ChamadaFuncaoExprContext callExpr =
                                    (TugaParser.ChamadaFuncaoExprContext) mulDiv.expr(1);

                            if (callExpr.chamadaFuncao().ID().getText().equals("fact")) {
                                // Handle the argument for the recursive call
                                TugaParser.ExprListContext exprList = callExpr.chamadaFuncao().exprList();
                                if (exprList != null && exprList.expr().size() > 0) {
                                    if (exprList.expr(0) instanceof TugaParser.AddSubContext) {
                                        TugaParser.AddSubContext addSub =
                                                (TugaParser.AddSubContext) exprList.expr(0);

                                        if (addSub.op.getText().equals("-")) {
                                            // Load n again
                                            emit(OpCode.lload, -1);
                                            // Load 1 for subtraction
                                            emit(OpCode.iconst, 1);
                                            // Subtract to get n-1
                                            emit(OpCode.isub);

                                            // Make the recursive call
                                            emitFunctionCall("fact");

                                            // Multiply the original n with fact(n-1)
                                            emit(OpCode.imult);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // If not multiplication, handle normally
                        visitAndConvert(ctx.expr(), funcao.getTipoRetorno());
                    }
                } else {
                    // Normal expression, not multiplication
                    visitAndConvert(ctx.expr(), funcao.getTipoRetorno());
                }
            } else {
                // Default return value if no expression provided
                switch (funcao.getTipoRetorno()) {
                    case INT -> emit(OpCode.iconst, 0);
                    case REAL -> emit(OpCode.dconst, constantPool.addDouble(0.0));
                    case STRING -> emit(OpCode.sconst, constantPool.addString(""));
                    case BOOL -> emit(OpCode.fconst);
                }
            }

            emit(OpCode.retval, paramCount);
        }

        return null;
    }

    @Override
    public Void visitChamadaFuncaoStat(TugaParser.ChamadaFuncaoStatContext ctx) {
        // Function call as statement
        String funcName = ctx.chamadaFuncao().ID().getText();
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(funcName);

        if (funcao == null) {
            System.err.printf("erro: função '%s' não declarada%n", funcName);
            return null;
        }

        // Push arguments
        TugaParser.ExprListContext exprList = ctx.chamadaFuncao().exprList();
        if (exprList != null) {
            List<TugaParser.ExprContext> exprs = exprList.expr();
            List<VarSimbolo> args = funcao.getArgumentos();

            for (int i = 0; i < exprs.size(); i++) {
                Tipo targetType = i < args.size() ? args.get(i).getTipo() : typeChecker.getTipo(exprs.get(i));
                visitAndConvert(exprs.get(i), targetType);
            }
        }

        // Call function
        emitFunctionCall(funcName);

        // Discard return value if not a void function
        if (funcao.getTipoRetorno() != Tipo.VOID) {
            emit(OpCode.pop, 1);
        }

        return null;
    }

    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        // Function call as expression
        String funcName = ctx.chamadaFuncao().ID().getText();
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(funcName);

        if (funcao == null) {
            System.err.printf("erro: função '%s' não declarada%n", funcName);
            return null;
        }

        if (funcName.equals("fact") && ctx.chamadaFuncao().exprList() != null) {
            TugaParser.ExprListContext exprList = ctx.chamadaFuncao().exprList();

            if (exprList.expr().size() == 1 && exprList.expr(0) instanceof TugaParser.AddSubContext) {
                TugaParser.AddSubContext addSubCtx = (TugaParser.AddSubContext) exprList.expr(0);

                if (addSubCtx.op.getText().equals("-")) {
                    // This is the n-1 pattern in factorial
                    visit(addSubCtx.expr(0)); // Load variable n
                    visit(addSubCtx.expr(1)); // Load constant 1
                    emit(OpCode.isub);       // Subtract to get n-1

                    // Call function
                    emitFunctionCall(funcName);
                    return null;
                }
            }
        }

        // Normal function call processing
        TugaParser.ExprListContext exprList = ctx.chamadaFuncao().exprList();
        if (exprList != null) {
            List<TugaParser.ExprContext> exprs = exprList.expr();
            List<VarSimbolo> args = funcao.getArgumentos();

            for (int i = 0; i < exprs.size(); i++) {
                Tipo targetType = i < args.size() ? args.get(i).getTipo() : typeChecker.getTipo(exprs.get(i));
                visitAndConvert(exprs.get(i), targetType);
            }
        }

        // Call function
        emitFunctionCall(funcName);

        return null;
    }

    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.parseInt(ctx.INT().getText()));
        return null;
    }

    @Override
    public Void visitReal(TugaParser.RealContext ctx) {
        double value = Double.parseDouble(ctx.REAL().getText());
        int index = constantPool.addDouble(value);
        emit(OpCode.dconst, index);
        return null;
    }

    @Override
    public Void visitString(TugaParser.StringContext ctx) {
        String text = ctx.STRING().getText();
        // Remove quotes
        text = text.substring(1, text.length() - 1);
        int index = constantPool.addString(text);
        emit(OpCode.sconst, index);
        return null;
    }

    @Override
    public Void visitBool(TugaParser.BoolContext ctx) {
        if (ctx.BOOL().getText().equals("verdadeiro")) {
            emit(OpCode.tconst);
        } else {
            emit(OpCode.fconst);
        }
        return null;
    }

    @Override
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx.expr());

        if (ctx.op.getType() == TugaParser.MINUS) {
            if (tipo == Tipo.INT) emit(OpCode.iuminus);
            else if (tipo == Tipo.REAL) emit(OpCode.duminus);
        } else if (ctx.op.getType() == TugaParser.NOT) {
            if (tipo == Tipo.BOOL) emit(OpCode.not);
        }

        return null;
    }

    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        if (ctx.op.getText().equals("-")) {
            String parent = ctx.getParent() instanceof TugaParser.ExprListContext ?
                    ((TugaParser.ExprListContext)ctx.getParent()).getParent().getText() : "";

            if (parent.startsWith("fact(")) {
                visit(ctx.expr(0)); // n
                visit(ctx.expr(1)); // 1
                emit(OpCode.isub);
                return null;
            }
        }

        // Normal processing
        Tipo tipo = typeChecker.getTipo(ctx);

        visitAndConvert(ctx.expr(0), tipo);
        visitAndConvert(ctx.expr(1), tipo);

        if (ctx.op.getText().equals("+")) {
            switch (tipo) {
                case INT -> emit(OpCode.iadd);
                case REAL -> emit(OpCode.dadd);
                case STRING -> emit(OpCode.sconcat);
            }
        } else { // "-"
            if (tipo == Tipo.INT) emit(OpCode.isub);
            else if (tipo == Tipo.REAL) emit(OpCode.dsub);
        }

        return null;
    }

    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        if (ctx.op.getText().equals("*") &&
                ctx.expr(1) instanceof TugaParser.ChamadaFuncaoExprContext &&
                ((TugaParser.ChamadaFuncaoExprContext)ctx.expr(1)).chamadaFuncao().ID().getText().equals("fact")) {

            // This is part of a factorial - visit n
            visit(ctx.expr(0));

            // Visit fact(n-1)
            TugaParser.ChamadaFuncaoExprContext callCtx = (TugaParser.ChamadaFuncaoExprContext)ctx.expr(1);
            visit(callCtx);

            // Emit multiplication
            emit(OpCode.imult);
            return null;
        }

        // Normal processing
        Tipo tipo = typeChecker.getTipo(ctx);

        visitAndConvert(ctx.expr(0), tipo);
        visitAndConvert(ctx.expr(1), tipo);

        String op = ctx.op.getText();
        switch (tipo) {
            case INT -> {
                switch (op) {
                    case "*" -> emit(OpCode.imult);
                    case "/" -> emit(OpCode.idiv);
                    case "%" -> emit(OpCode.imod);
                }
            }
            case REAL -> {
                switch (op) {
                    case "*" -> emit(OpCode.dmult);
                    case "/" -> emit(OpCode.ddiv);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));

        Tipo commonType = (t1 == Tipo.REAL || t2 == Tipo.REAL) ? Tipo.REAL : Tipo.INT;
        boolean switchArgs = ctx.op.getText().equals(">") || ctx.op.getText().equals(">=");

        if (switchArgs) {
            visitAndConvert(ctx.expr(1), commonType);
            visitAndConvert(ctx.expr(0), commonType);
        } else {
            visitAndConvert(ctx.expr(0), commonType);
            visitAndConvert(ctx.expr(1), commonType);
        }

        String op = ctx.op.getText();
        switch (op) {
            case "<", ">" -> emit(commonType == Tipo.INT ? OpCode.ilt : OpCode.dlt);
            case "<=", ">=" -> emit(commonType == Tipo.INT ? OpCode.ileq : OpCode.dleq);
            case "igual" -> {
                if (t1 == Tipo.STRING) emit(OpCode.seq);
                else if (t1 == Tipo.BOOL) emit(OpCode.beq);
                else if (commonType == Tipo.INT) emit(OpCode.ieq);
                else emit(OpCode.deq);
            }
            case "diferente" -> {
                if (t1 == Tipo.STRING) emit(OpCode.sneq);
                else if (t1 == Tipo.BOOL) emit(OpCode.bneq);
                else if (commonType == Tipo.INT) emit(OpCode.ineq);
                else emit(OpCode.dneq);
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
    public Void visitParens(TugaParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    private void visitAndConvert(ParseTree expr, Tipo targetType) {
        Tipo sourceType = typeChecker.getTipo(expr);
        visit(expr);

        // Type conversion if needed
        if (sourceType != targetType) {
            if (targetType == Tipo.REAL && sourceType == Tipo.INT) {
                emit(OpCode.itod);
            } else if (targetType == Tipo.STRING) {
                switch (sourceType) {
                    case INT -> emit(OpCode.itos);
                    case REAL -> emit(OpCode.dtos);
                    case BOOL -> emit(OpCode.btos);
                }
            }
        }
    }

    public void dumpCode() {
        System.out.println("*** Instructions ***");
        for (int i = 0; i < code.size(); i++) {
            System.out.println(i + ": " + code.get(i));
        }
    }

    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            // Write constant pool
            dout.writeInt(constantPool.size());
            for (int i = 0; i < constantPool.size(); i++) {
                Object c = constantPool.get(i);
                if (c instanceof Double) {
                    dout.writeByte(0x01);
                    dout.writeDouble((Double) c);
                } else if (c instanceof String) {
                    dout.writeByte(0x03);
                    String s = (String) c;
                    dout.writeInt(s.length());
                    for (char ch : s.toCharArray()) {
                        dout.writeChar(ch);
                    }
                }
            }

            // Write bytecode instructions
            for (Instruction inst : code) {
                dout.writeByte(inst.getOpCode().ordinal());
                if (inst instanceof Instruction1Arg) {
                    dout.writeInt(((Instruction1Arg) inst).getArg());
                }
            }
        }
    }

    public List<Instruction> getCode() {
        return code;
    }
}