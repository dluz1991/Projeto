package CodeGenerator;

import java.io.*;
import java.util.*;

import Calc.*;
import ConstantPool.ConstantPool;
import VM.OpCode;
import VM.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class CodeGen extends CalcBaseVisitor<Void> {

    private final ArrayList<Instruction> code = new ArrayList<>();
    private final TypeChecker typeChecker;
    private  ConstantPool constantPool = new ConstantPool();

    //___________BUILDERS
    public CodeGen(TypeChecker checker, ConstantPool constantPool) {
        this.typeChecker = checker;
        this.constantPool= constantPool;
    }
    public CodeGen() {
        this.typeChecker = new TypeChecker();
    }

    //____________VISITORS__________________
    private void visitAndConvert(ParseTree expr, Tipo target) {
        visit(expr);
        Tipo origem = typeChecker.getTipo(expr);
        if (origem != target) emitConversion(origem, target);
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
        if (tipo == Tipo.ERRO) {
            System.err.println("Erro de tipo na expressão.");
            return null;
        }
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
        int index = constantPool.addDouble(val);
        emit(OpCode.dconst, index);
        return null;
    }

    @Override
    public Void visitBool(CalcParser.BoolContext ctx) {
        if (ctx.getText().equals("verdadeiro")) emit(OpCode.tconst);
        else if (ctx.getText().equals("falso")) emit(OpCode.fconst);
        return null;
    }

    @Override
    public Void visitString(CalcParser.StringContext ctx) {
        String text = ctx.getText().substring(1, ctx.getText().length() - 1); // remove aspas
        int index = constantPool.addString(text);
        emit(OpCode.sconst, index);
        return null;
    }

    @Override
    public Void visitParens(CalcParser.ParensContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitUnary(CalcParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipo = typeChecker.getTipo(ctx);
        if (ctx.op.getType() == CalcParser.MINUS) {
            if (tipo == Tipo.INT) emit(OpCode.iuminus);
            else if (tipo == Tipo.REAL) emit(OpCode.duminus);
            else System.out.println("Erro: operação - inválida para tipo " + tipo);
        } else if (ctx.op.getType() == CalcParser.NOT) {
            if (tipo == Tipo.BOOL) emit(OpCode.not);
            else System.out.println("Erro: operação NOT inválida para tipo " + tipo);
        }
        return null;
    }

    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        Tipo tipo = typeChecker.getTipo(ctx);

        if (tipo == Tipo.ERRO) {
            System.err.println("Erro de tipo na expressão.");
            return null;
        }
        String op = ctx.op.getText();
        switch (tipo) {
            case INT ->{
                visitAndConvert(ctx.expr(0), tipo);
                visitAndConvert(ctx.expr(1), tipo);
               if(op.equals("+")) emit(OpCode.iadd);
               else if (op.equals("-")) emit(OpCode.isub);
            }
            case REAL -> {
                visitAndConvert(ctx.expr(0), tipo);
                visitAndConvert(ctx.expr(1), tipo);
                if(op.equals("+")) emit(OpCode.dadd);
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

    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
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

    @Override
    public Void visitAnd(CalcParser.AndContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.and);
        return null;
    }

    @Override
    public Void visitOr(CalcParser.OrContext ctx) {
        visitAndConvert(ctx.expr(0), Tipo.BOOL);
        visitAndConvert(ctx.expr(1), Tipo.BOOL);
        emit(OpCode.or);
        return null;
    }

    @Override
    public Void visitRelational(CalcParser.RelationalContext ctx) {
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
                    //Trocar a ordem das visitas para evitar conversões desnecessárias
                    // (0)'<'(1) == (1)'>='(0) , (0)'<='(1) == (1)'>'(0)
                    case "<":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ilt);
                        break;
                    case ">":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.ileq);
                        break;
                    case "<=":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.ileq);
                        break;
                    case ">=":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.ilt);
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
                    //Trocar a ordem das visitas para evitar conversões desnecessárias
                    // (0)'<'(1) == (1)'>='(0) , (0)'<='(1) == (1)'>'(0)
                    case "<":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.dlt);
                        break;
                    case ">":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.dleq);
                        break;
                    case "<=":
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        emit(OpCode.dleq);
                        break;
                    case ">=":
                        visitAndConvert(ctx.expr(1), tipoFinal);
                        visitAndConvert(ctx.expr(0), tipoFinal);
                        emit(OpCode.dlt);
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


    private void erroOpRel(String op) {
        System.err.println("Operação relacional inválida: " + op);
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
            default -> System.err.println("Conversão não suportada: " + de + " -> " + para);
        }
    }

    //METODOS AUXILIARES
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
            System.out.println("Saving the bytecodes to " + filename);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Instruction> getCode() {
        return code;
    }

    /*private int addToConstantPool(Object value) {
        int index = constantPool.indexOf(value);
        if (index != -1) {
            return index;
        }
        constantPool.add(value);
        return constantPool.size() - 1;
    }

    public Object getConstant(int index) {
        if (index >= 0 && index < constantPool.size()) {
            return constantPool.get(index);
        }
        return null;
    }
*/

}