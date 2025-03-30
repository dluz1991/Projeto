package CodeGenerator;

import java.io.*;
import java.util.*;

import Calc.*;
import VM.OpCode;
import VM.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class CodeGen extends CalcBaseVisitor<Void> {

    private final ArrayList<Instruction> code = new ArrayList<>();
    private final TypeChecker typeChecker;

    public CodeGen(TypeChecker checker) {
        this.typeChecker = checker;
    }

    @Override
    public Void visitProg(CalcParser.ProgContext ctx) {
        visitChildren(ctx);
        emit(OpCode.halt);
        return null;

    }

    @Override
    public Void visitStat(CalcParser.StatContext ctx) {
        visit(ctx.expr());

        Tipo tipo = typeChecker.getTipo(ctx.expr());
        switch (tipo) {
            case INT -> emit(OpCode.iprint);
            case REAL -> emit(OpCode.dprint);
            case BOOL -> emit(OpCode.bprint);
            case STRING -> emit(OpCode.sprint);
            default -> System.err.println("Tipo invalido para impressao.");
        }

        return null;
    }

    @Override
    public Void visitInt(CalcParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.parseInt(ctx.getText()));
        return null;
    }

    @Override
    public Void visitReal(CalcParser.RealContext ctx) {
        double val = Double.parseDouble(ctx.getText());
        emit(OpCode.dconst, (int) val); // assume-se que dconst aceita inteiro representando double
        return null;
    }

    @Override
    public Void visitBool(CalcParser.BoolContext ctx) {
        boolean val = ctx.getText().equals("verdadeiro");
        emit(OpCode.bconst, val ? 1 : 0);
        return null;
    }

    @Override
    public Void visitString(CalcParser.StringContext ctx) {
        String text = ctx.getText().substring(1, ctx.getText().length() - 1);
        emit(OpCode.sconst, text.hashCode()); // substituição simples sem tabelas
        return null;
    }

    @Override
    public Void visitParens(CalcParser.ParensContext ctx) {
        visit(ctx.expr());
        return null;
    }

    /**
     * Visit a parse tree produced .
     *
     * @param ctx the parse tree
     * @return the visitor result
     * <p>
     * Padrões permitidos :
     * -INT
     * -REAL
     * NOT BOOL
     */
    @Override
    public Void visitUnary(CalcParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx);

        if (ctx.op.getType() == CalcParser.UMINUS) {
            if (tipo == Tipo.INT) emit(OpCode.iuminus);
            else if (tipo == Tipo.REAL) emit(OpCode.duminus);
            else System.out.println("Erro: operação NOT inválida para tipo " + tipo);
        } else if (ctx.op.getType() == CalcParser.NOT) {
            if (tipo == Tipo.BOOL) emit(OpCode.bnot);
            else System.out.println("Erro: operação NOT inválida para tipo " + tipo);

        }

        return null;
    }

    /**
     * Visit a parse tree produced by
     *
     * @param ctx the parse tree
     * @return the visitor result
     * <p>
     * Padrões permitidos :
     * -INT
     * -REAL
     * -STRING
     */
    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));

        visit(ctx.expr(1));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));

        Tipo resultado = typeChecker.getTipo(ctx);
        //resultado da operacao
        if (t1 != resultado) emitConversion(t1, resultado);
        if (t2 != resultado) emitConversion(t2, resultado);


        switch (resultado) {
            case INT -> emit(ctx.op.getText().equals("+") ? OpCode.iadd : OpCode.isub);
            case REAL -> emit(ctx.op.getText().equals("+") ? OpCode.dadd : OpCode.dsub);
            case STRING -> {
                if (ctx.op.getText().equals("+")) {
                    emit(OpCode.sconcat);
                } else {
                    System.err.println("Input has type checking errors");
                }
            }
            default -> System.err.println("Input has type checking errors");
        }

        return null;
    }

    /**
     * Visit a parse tree produced by
     *
     * @param ctx the parse tree
     * @return the visitor result
     * <p>
     * Padrões permitidos :
     * -INT
     * -REAL
     */
    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));

        visit(ctx.expr(1));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));

        Tipo resultado = typeChecker.getTipo(ctx);

        if (t1 != resultado) emitConversion(t1, resultado);
        if (t2 != resultado) emitConversion(t2, resultado);

        switch (resultado) {
            case INT -> {
                if (ctx.op.getType() == CalcParser.TIMES) {
                    emit(OpCode.imult);
                } else if (ctx.op.getType() == CalcParser.DIV) {
                    emit(OpCode.idiv);
                }
            }
            case REAL -> {
                if (ctx.op.getType() == CalcParser.TIMES) {
                    emit(OpCode.dmult);
                } else if (ctx.op.getType() == CalcParser.DIV) {
                    emit(OpCode.ddiv);
                }
            }
            default -> System.err.println("Input has type checking errors");
        }

        return null;
    }

    /**
     * Visit a parse tree produced by
     *
     * @param ctx the parse tree
     * @return the visitor result
     * <p>
     * Padrões permitidos :
     * BOOLEAN
     */
    @Override
    public Void visitAnd(CalcParser.AndContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));
        if (t1 == t2) emit(OpCode.band);
        else System.out.println("Input has type checking errors");
        return null;
    }

    @Override
    public Void visitOr(CalcParser.OrContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));
        if (t1 == t2) emit(OpCode.bor);
        else System.out.println("Input has type checking errors");
        return null;
    }

    /**
     * Visit a parse tree produced by
     *
     * @param ctx the parse tree
     * @return the visitor result
     * <p>
     * Padrões permitidos :
     * -INT
     * -REAL
     */
    @Override
    public Void visitRelational(CalcParser.RelationalContext ctx) {
        visit(ctx.expr(0));
        Tipo t1 = typeChecker.getTipo(ctx.expr(0));

        visit(ctx.expr(1));
        Tipo t2 = typeChecker.getTipo(ctx.expr(1));

        Tipo tipoFinal = Tipo.BOOL; //tipo intermedio para decidir o opcode a usar. O tipo deste no ha de ser sempre booleano
        if ((t1 == Tipo.REAL || t2 == Tipo.REAL)) {
            tipoFinal = Tipo.REAL;
        } else if (t1 == Tipo.INT && t2 == Tipo.INT) {
            tipoFinal = Tipo.INT;
        } else if (t1 == Tipo.STRING && t2 == Tipo.STRING) {
            tipoFinal = Tipo.STRING;
        }

        if (t1 != tipoFinal) emitConversion(t1, tipoFinal);
        if (t2 != tipoFinal) emitConversion(t2, tipoFinal);

        String op = ctx.op.getText();

        switch (tipoFinal) {
            case INT -> {
                switch (op) {
                    case "<" -> emit(OpCode.iless);
                    case ">" -> emit(OpCode.igreater);
                    case "<=" -> emit(OpCode.ilessequal);
                    case ">=" -> emit(OpCode.igreaterequal);
                    case "igual" -> emit(OpCode.iequal);
                    case "diferente" -> emit(OpCode.idifferent);
                    default -> erroOpRel(op);
                }
            }
            case REAL -> {
                switch (op) {
                    case "<" -> emit(OpCode.dless);
                    case ">" -> emit(OpCode.dgreater);
                    case "<=" -> emit(OpCode.dlessequal);
                    case ">=" -> emit(OpCode.dgreaterequal);
                    case "igual" -> emit(OpCode.dequal);
                    case "diferente" -> emit(OpCode.ddifferent);
                    default -> erroOpRel(op);
                }
            }
            case STRING -> {
                switch (op) {
                    case "igual" -> emit(OpCode.sequal);
                    case "diferente" -> emit(OpCode.sdifferent);
                    default -> erroOpRel(op);
                }
            }
            case BOOL -> {
                switch (op) {
                    case "igual" -> emit(OpCode.bequal);
                    case "diferente" -> emit(OpCode.bdifferent);
                    default -> erroOpRel(op);
                }
            }
            default -> System.err.println("Input has type checking errors");
        }

        return null;
    }

    private void erroOpRel(String op) {
        System.err.println("Input has parsing errors");
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
            default -> System.err.println("Input has parsing errors");
        }
    }

    public void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    public void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    public void dumpCode() {
        System.out.println("Generated code in assembly format");
        for (int i = 0; i < code.size(); i++)
            System.out.println(i + ": " + code.get(i));
    }

    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            for (Instruction inst : code)
                inst.writeTo(dout);
            System.out.println("Saving the bytecodes to " + filename);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Instruction> getCode() {
        return code;
    }
}