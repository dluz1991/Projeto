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


    public CodeGen(TypeChecker checker, ConstantPool constantPool, TabelaSimbolos tabelaSimbolos) {
        this.typeChecker = checker;
        this.constantPool = constantPool;
        this.tabelaSimbolos = tabelaSimbolos;
        this.addr = typeChecker.getAddr();
    }

    private void visitAndConvert(ParseTree expr, Tipo target) {
        Tipo origem = typeChecker.getTipo(expr);
        visit(expr); // visita primeiro
        if (origem != target) emitConversion(origem, target);
    }

    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        for (TugaParser.VarDeclarationContext var : ctx.varDeclaration()) {
            visit(var); // regista variáveis e tipos
        }


        for (TugaParser.StatContext stat : ctx.stat()) {
            visit(stat);
        }

        emit(OpCode.halt);
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

        int count = 0;
        for (TerminalNode id : ctx.ID()) {
            String nome = id.getText();
            count++;
            if (!tabelaSimbolos.containsVar(nome)) {
                tabelaSimbolos.putSimbolo(nome, tipo, addr++);
                
            }
        }

// Só faz galloc se houver novas variáveis nesta linha
        if (count > 0) {
            emit(OpCode.galloc, count);
        }


        return null;
    }


    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        String nome = ctx.ID().getText();
        ValorSimbolo simbolo = tabelaSimbolos.getSimbolo(nome);
        if (simbolo == null) return null;
        Tipo tipoAlvo = simbolo.getTipo();
        visitAndConvert(ctx.expr(), tipoAlvo);
        emit(OpCode.gstore, simbolo.getIndex());


        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        for (TugaParser.StatContext s : ctx.stat()) {
            visit(s);
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
    public Void visitVazia(TugaParser.VaziaContext ctx) {
        return null;
    }

    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        ValorSimbolo simbolo = tabelaSimbolos.getSimbolo(ctx.ID().getText());

        if (simbolo != null) emit(OpCode.gload, simbolo.getIndex());
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
            System.err.println("Erro de tipo na expressão.");
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
            System.err.println("Erro de tipo na expressão.");
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
                    case "%" -> emit(OpCode.dmod);
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
            default -> System.err.println("Conversão não suportada: " + de + " -> " + para);
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

    public List<Instruction> getCode() {
        return code;
    }
}
