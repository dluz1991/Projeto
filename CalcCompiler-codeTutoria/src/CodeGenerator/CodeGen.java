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
    private ConstantPool constantPool = new ConstantPool();
    private TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();

    private int addr;
    private final Map<String, Integer> labelsFuncoes = new HashMap<>();
    private final Map<String, List<Integer>> pendingCalls = new HashMap<>();

    private String currentFunction = null;


    public CodeGen(TypeChecker checker, ConstantPool constantPool, TabelaSimbolos tabelaSimbolos) {
        this.typeChecker = checker;
        this.constantPool = constantPool;
        this.tabelaSimbolos = tabelaSimbolos;
        this.addr = typeChecker.getAddr();

    }


    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        // Depois gera o código principal
        emitCall("principal");
        emit(OpCode.halt);
        // Primeiro processa as funções
        for (var fn : ctx.functionDecl()) visit(fn);
        // Resolve chamadas pendentes
        for (var entry : pendingCalls.entrySet()) {
            String fn = entry.getKey();
            Integer loc = labelsFuncoes.get(fn);
            if (loc == null) {
                System.err.printf("erro: falta a função '%s'\n", fn);
                continue;
            }
            for (int callIdx : entry.getValue()) {
                ((Instruction1Arg) code.get(callIdx)).setArg(loc);
            }
        }
        debugInfo();
        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        currentFunction = ctx.ID().getText();
        labelsFuncoes.put(currentFunction, code.size());

        // reindex and register arguments
        FuncaoSimbolo fs = tabelaSimbolos.getFuncao(currentFunction);
        List<VarSimbolo> args = fs.getArgumentos();
        for (int i = 0; i < args.size(); i++) {
            VarSimbolo old = args.get(i);
            int idx = -1 - i;
            VarSimbolo nu = new VarSimbolo(old.getName(), old.getTipo(), idx, fs.getScope());
            args.set(i, nu);
            tabelaSimbolos.putVariavel(nu.getName(), nu.getTipo(), nu.getIndex(), fs.getScope());
        }

        // body
        visit(ctx.bloco());
        // void functions need a ret
        if (fs.getTipoRetorno() == Tipo.VOID) {
            emit(OpCode.ret, args.size());
        }
        currentFunction = null;
        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        FuncaoSimbolo fs = tabelaSimbolos.getFuncao(currentFunction);
        int paramCount = fs.getArgumentos().size();

        // Calcula total de locais
        int totalLoc = 0;
        for (var vd : ctx.varDeclaration()) totalLoc += vd.ID().size();

        // Aloca espaço para variáveis locais
        if (totalLoc > 0) emit(OpCode.lalloc, totalLoc);

        // Registra variáveis locais
        int localIdx = 0;
        for (var vd : ctx.varDeclaration()) {
            Tipo t = switch (vd.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "booleano" -> Tipo.BOOL;
                case "string" -> Tipo.STRING;
                default -> Tipo.ERRO;
            };
            for (TerminalNode id : vd.ID()) {
                tabelaSimbolos.putVariavel(id.getText(), t, paramCount + localIdx++, fs.getScope());
            }
        }

        // Processa statements
        for (var st : ctx.stat()) visit(st);

        return null;
    }

    @Override
    public Void visitBlocoStat(TugaParser.BlocoStatContext ctx) {
        return visit(ctx.bloco());
    }

    @Override
    public Void visitChamadaFuncaoStat(TugaParser.ChamadaFuncaoStatContext ctx) {
        String fn = ctx.chamadaFuncao().ID().getText();
        FuncaoSimbolo fs = tabelaSimbolos.getFuncao(fn);
        // gera argumentos
        if (ctx.chamadaFuncao().exprList() != null) {
            var args = ctx.chamadaFuncao().exprList().expr();
            for (int i = 0; i < args.size(); i++) {
                Tipo want = i < fs.getArgumentos().size() ? fs.getArgumentos().get(i).getTipo() : typeChecker.getTipo(args.get(i));
                visitAndConvert(args.get(i), want);
            }
        }
        emitCall(fn);
        if (fs.getTipoRetorno() != Tipo.VOID) {
            emit(OpCode.pop, 1);
        }
        return null;
    }

    @Override
    public Void visitChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx) {
        String fn = ctx.ID().getText();
        FuncaoSimbolo fs = tabelaSimbolos.getFuncao(fn);

        if (fs == null) {
            throw new RuntimeException("Função '" + fn + "' não declarada");
        }

        // Gera os argumentos com conversão de tipos se necessário
        if (ctx.exprList() != null) {
            var args = ctx.exprList().expr();
            for (int i = 0; i < args.size(); i++) {
                Tipo expected = i < fs.getArgumentos().size()
                        ? fs.getArgumentos().get(i).getTipo()
                        : typeChecker.getTipo(args.get(i));
                visitAndConvert(args.get(i), expected);
            }
        }

        emitCall(fn);
        return null;
    }

    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        visitChamadaFuncao(ctx.chamadaFuncao());
        return null;
    }
    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        FuncaoSimbolo fs = tabelaSimbolos.getFuncao(currentFunction);
        if (fs == null) {
            throw new RuntimeException("Retorno fora de função");
        }

        int nArgs = fs.getArgumentos().size();

        if (ctx.expr() != null) {
            Tipo returnType = fs.getTipoRetorno();
            if (returnType == Tipo.VOID) {
                throw new RuntimeException("Função void não pode retornar valor");
            }

            visitAndConvert(ctx.expr(), returnType);
            emit(OpCode.retval, nArgs);
        } else {
            if (fs.getTipoRetorno() != Tipo.VOID) {
                throw new RuntimeException("Função não-void deve retornar valor");
            }
            emit(OpCode.ret, nArgs);
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        Tipo tipo = Tipo.STRING; // tipo default
        if (ctx.TYPE() != null) {
            tipo = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        int count = ctx.ID().size();

        return null;
    }


    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        String varName = ctx.ID().getText();
        VarSimbolo vs = tabelaSimbolos.getVar(varName);

        if (vs == null) {
            throw new RuntimeException("Variável '" + varName + "' não declarada");
        }

        visitAndConvert(ctx.expr(), vs.getTipo());

        if (vs.getScope() == 0) {
            emit(OpCode.gstore, vs.getIndex());
        } else {
            emit(OpCode.lstore, vs.getIndex());
        }
        return null;
    }


    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        int beginWhile = code.size();

        // VISITA diretamente a expressão — para garantir que o visitRelational é chamado
        visitAndConvert(ctx.expr(), Tipo.BOOL);

        emit(OpCode.jumpf, 0);
        int jumpFIndex = code.size() - 1;

        visit(ctx.stat());

        emit(OpCode.jump, beginWhile);
        ((Instruction1Arg) code.get(jumpFIndex)).setArg(code.size());

        return null;
    }


    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        visitAndConvert(ctx.expr(), Tipo.BOOL);
        emit(OpCode.jumpf, 0);
        int jfIdx = code.size() - 1;
        visit(ctx.stat(0));
        if (ctx.stat().size() > 1) {
            emit(OpCode.jump, 0);
            int jIdx = code.size() - 1;
            ((Instruction1Arg) code.get(jfIdx)).setArg(code.size());
            visit(ctx.stat(1));
            ((Instruction1Arg) code.get(jIdx)).setArg(code.size());
        } else {
            ((Instruction1Arg) code.get(jfIdx)).setArg(code.size());
        }
        return null;
    }

    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        // Primeiro visita a expressão para gerar o código necessário
        visit(ctx.expr());

        // Obtém o tipo da expressão após a visita
        Tipo tipo = typeChecker.getTipo(ctx.expr());

        // Verifica se é uma chamada de função
        if (ctx.expr() instanceof TugaParser.ChamadaFuncaoExprContext) {
            FuncaoSimbolo fs = tabelaSimbolos.getFuncao(((TugaParser.ChamadaFuncaoExprContext)ctx.expr()).chamadaFuncao().ID().getText());
            if (fs != null) {
                tipo = fs.getTipoRetorno();
            }
        }

        // Emite a instrução de impressão correta
        switch (tipo) {
            case INT -> emit(OpCode.iprint);
            case REAL -> emit(OpCode.dprint);
            case BOOL -> emit(OpCode.bprint);
            case STRING -> emit(OpCode.sprint);
            default -> System.err.println("Erro: tipo não suportado para impressão");
        }

        return null;
    }

    @Override
    public Void visitVazia(TugaParser.VaziaContext ctx) {
        return null;
    }


    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        String varName = ctx.ID().getText();
        VarSimbolo vs = tabelaSimbolos.getVar(varName);

        if (vs.getScope() == 0) {
            emit(OpCode.gload, vs.getIndex());
        } else {
            emit(OpCode.lload, vs.getIndex());
        }
        return null;
    }


    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.parseInt(ctx.getText()));
        return null;
    }

    /**
     * Visita o nó de Reais e gera o bytecode correspondente.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitReal(TugaParser.RealContext ctx) {
        double val = Double.parseDouble(ctx.getText());
        int index = constantPool.addDouble(val);
        emit(OpCode.dconst, index);
        return null;
    }

    /**
     * Visita o nó de Booleanos e gera o bytecode correspondente.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitBool(TugaParser.BoolContext ctx) {
        if (ctx.getText().equals("verdadeiro")) emit(OpCode.tconst);
        else if (ctx.getText().equals("falso")) emit(OpCode.fconst);
        return null;
    }

    /**
     * Visita o nó de String e gera o bytecode correspondente.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */

    @Override
    public Void visitString(TugaParser.StringContext ctx) {
        String text = ctx.getText().substring(1, ctx.getText().length() - 1); // remove aspas
        int index = constantPool.addString(text);
        emit(OpCode.sconst, index);
        return null;
    }

    /**
     * Visita o nó de Parens e gera o bytecode correspondente.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitParens(TugaParser.ParensContext ctx) {
        visit(ctx.expr());
        return null;
    }

    /**
     * Visita o nó de Unary e gera o bytecode correspondente.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx);
        if (ctx.op.getType() == TugaParser.MINUS) {
            if (tipo == Tipo.INT) emit(OpCode.iuminus);
            else if (tipo == Tipo.REAL) emit(OpCode.duminus);
            else System.out.println("Erro: operação - inválida para tipo " + tipo);
        } else if (ctx.op.getType() == TugaParser.NOT) {
            if (tipo == Tipo.BOOL) emit(OpCode.not);
            else System.out.println("Erro: operação NOT inválida para tipo " + tipo);
        }
        return null;
    }

    /**
     * Visita o nó de AddSub e gera o bytecode para adiçao e subtraçao.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        Tipo tipo = typeChecker.getTipo(ctx);

        if (tipo == Tipo.ERRO) {
            //System.err.println("Erro de tipo na expressão.");
            return null;
        }
        String op = ctx.op.getText();
        switch (tipo) {
            case INT -> {
                visitAndConvert(ctx.expr(0), tipo);
                visitAndConvert(ctx.expr(1), tipo);
                if (op.equals("+")) emit(OpCode.iadd);
                else if (op.equals("-")) emit(OpCode.isub);
            }
            case REAL -> {
                visitAndConvert(ctx.expr(0), tipo);
                visitAndConvert(ctx.expr(1), tipo);
                if (op.equals("+")) emit(OpCode.dadd);
                else if (op.equals("-")) emit(OpCode.dsub);
            }
            case STRING -> {
                if (op.equals("+")) {
                    visitAndConvert(ctx.expr(0), Tipo.STRING);
                    visitAndConvert(ctx.expr(1), Tipo.STRING);
                    emit(OpCode.sconcat);
                }
            }
            default -> System.err.println("Erro: tipo não suportado em AddSub");
        }
        return null;
    }

    /**
     * Visita o nó de MulDiv e gera o bytecode para multiplicação, divisão e módulo.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        Tipo tipo = typeChecker.getTipo(ctx);
        visitAndConvert(ctx.expr(0), tipo);
        visitAndConvert(ctx.expr(1), tipo);
        if (tipo == Tipo.ERRO) {
            //System.err.println("Erro de tipo na expressão.");
            return null;
        }
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
            default -> System.err.println("Erro: tipo não suportado em MulDiv");
        }
        return null;
    }

    /**
     * Visita o nó de And e gera o bytecode correspondente para And.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitAnd(TugaParser.AndContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.and);
        return null;
    }

    /**
     * Visita o nó de Or e gera o bytecode correspondente para Or.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */

    @Override
    public Void visitOr(TugaParser.OrContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.or);
        return null;
    }

    /**
     * Visita o nó de Relational e gera o bytecode correspondente para operações relacionais.
     *
     * @param ctx O contexto do nó de atribuição.
     * @return null
     */
    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));

        Tipo tipoFinal;
        if (t1 == Tipo.REAL || t2 == Tipo.REAL) tipoFinal = Tipo.REAL;
        else if (t1 == Tipo.INT && t2 == Tipo.INT) tipoFinal = Tipo.INT;
        else if (t1 == Tipo.STRING && t2 == Tipo.STRING) tipoFinal = Tipo.STRING;
        else if (t1 == Tipo.BOOL && t2 == Tipo.BOOL) tipoFinal = Tipo.BOOL;
        else {
            System.err.println("Tipos incompatíveis em Relational");
            return null;
        }

        String op = ctx.op.getText();

        switch (tipoFinal) {
            case INT: {
                switch (op) {
                    // Trocar a ordem das visitas para evitar conversões desnecessárias
                    // (0)'<'(1) == (1)'>='(0) , (0)'<='(1) == (1)'>'(0)
                    case "<":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ilt);
                        break;
                    case ">":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.ilt);
                        break;
                    case "<=":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ileq);
                        break;
                    case ">=":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.ileq);
                        break;
                    case "igual":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ieq);
                        break;
                    case "diferente":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ineq);
                        break;
                    default:
                        erroOpRel(op);
                }
                break;
            }
            case REAL: {
                switch (op) {
                    // Trocar a ordem das visitas para evitar conversões desnecessárias
                    // (0)'<'(1) == (1)'>='(0) , (0)'<='(1) == (1)'>'(0)
                    case "<":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.dlt);
                        break;
                    case ">":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.dlt);
                        break;
                    case "<=":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.dleq);
                        break;
                    case ">=":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.dleq);
                        break;
                    case "igual":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.deq);
                        break;
                    case "diferente":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.dneq);
                        break;
                    default:
                        erroOpRel(op);
                }
                break;
            }
            case STRING: {
                visitAndConvert(ctx.expr(0), tipoFinal);
                visitAndConvert(ctx.expr(1), tipoFinal);
                switch (op) {
                    case "igual" -> emit(OpCode.seq);
                    case "diferente" -> emit(OpCode.sneq);
                    default -> erroOpRel(op);
                }
                break;
            }
            case BOOL: {

                visitAndConvert(ctx.expr(0), tipoFinal);
                visitAndConvert(ctx.expr(1), tipoFinal);
                switch (op) {
                    case "igual" -> emit(OpCode.beq);
                    case "diferente" -> emit(OpCode.bneq);
                    default -> erroOpRel(op);
                }
                break;
            }

        }

        return null;
    }

    //____________________ METODOS AUXILIARES______________________
    private void erroOpRel(String op) {
        System.err.println("Operação relacional inválida: " + op);
    }

    /**
     * Fução auxiliar para emitir a conversão de tipos.
     *
     * @param de, para. O tipo de origem e o tipo de destino.
     * @return null
     */
    private void emitConversion(Tipo de, Tipo para) {
        if (de == Tipo.VOID) {
            return;
        } // não faz nada mas garante que void nao da erro
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

    /**
     * Emite uma instrução de bytecode.
     *
     * @param opc O código da operação a emitir.
     */
    public void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    /**
     * Emite uma instrução de bytecode com um argumento.
     *
     * @param opc O código da operação a emitir.
     * @param val O valor do argumento.
     */
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void emitCall(String fn) {
        Integer loc = labelsFuncoes.get(fn);
        if (loc != null) {
            emit(OpCode.call, loc);
        } else {
            // Se função ainda não definida, adiciona ao pendingCalls
            emit(OpCode.call, -1);
            int idx = code.size() - 1;
            pendingCalls.computeIfAbsent(fn, k -> new ArrayList<>()).add(idx);
        }
    }

    private void visitAndConvert(ParseTree expr, Tipo target) {
        Tipo origem = typeChecker.getTipo(expr);
        visit(expr); // visita primeiro
        if (origem != target) emitConversion(origem, target);
    }

    public List<Instruction> getCode() {
        return code;
    }

    public void debugInfo() {
        System.out.println("\n=== DEBUG INFO ===");

        // 1. Imprime funções e seus endereços
        System.out.println("\nFunções declaradas:");
        labelsFuncoes.forEach((name, addr) -> {
            FuncaoSimbolo fs = tabelaSimbolos.getFuncao(name);
            String returnType = (fs != null && fs.getTipoRetorno() != null) ?
                    fs.getTipoRetorno().toString() : "?";
            System.out.printf("%-15s @ %-4d (retorna: %s)\n", name, addr, returnType);
        });

        // 2. Imprime chamadas pendentes
        System.out.println("\nChamadas pendentes:");
        pendingCalls.forEach((fn, callIndices) -> {
            System.out.printf("%s -> ", fn);
            callIndices.forEach(idx -> {
                Instruction1Arg call = (Instruction1Arg) code.get(idx);
                System.out.printf("[%d: call %d] ", idx, call.getArg());
            });
            System.out.println();
        });

        // 3. Imprime constant pool
        System.out.println("\nConstant pool:");
        System.out.println("Tamanho: " + constantPool.size());

        // 4. Imprime código gerado
        System.out.println("\nCódigo gerado:");
        for (int i = 0; i < code.size(); i++) {
            System.out.printf("%4d: %s\n", i, code.get(i));
        }
        System.out.println("\n====================\n");
    }
}
