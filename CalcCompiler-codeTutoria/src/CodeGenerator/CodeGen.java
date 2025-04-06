package CodeGenerator;

import java.io.*;
import java.util.*;

import Tuga.*;
import VM.OpCode;
import VM.Instruction.*;

public class CodeGen extends TugaBaseVisitor<Void> {

    // the target code
    private final ArrayList<Instruction> code = new ArrayList<>();

    // stat: expr NEWLINE;
    @Override
    public Void visitStat(CalcParser.StatContext ctx) {
        visit(ctx.expr());
        emit(OpCode.iprint);
        return null;
    }

    // expr: '-' expr                          # Uminus
    @Override
    public Void visitUminus(CalcParser.UminusContext ctx) {
        visit(ctx.expr());
        emit(OpCode.iuminus);
        return null;
    }

    // expr: expr op=('*'|'/') expr            # MulDiv
    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        if ("*".equals(ctx.op.getText())) emit(OpCode.imult);
        else // must be /
            emit(OpCode.idiv);
        return null;
    }

    // expr: expr op=('+'|'-') expr            # AddSub
    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        if ("+".equals(ctx.op.getText())) emit(OpCode.iadd);
        else // must be -
            emit(OpCode.isub);
        return null;
    }

    // expr: INT                           # Int
    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.valueOf(ctx.INT().getText()));
        return null;
    }

    // expr:  '(' expr ')'                      # Parens
    @Override
    public Void visitParens(CalcParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    //expr: expr '^' expr
    @Override
    public Void visitExp(CalcParser.ExpContext ctx) {
        if (ctx.expr(1).getText().equals("0")) {
            emit(OpCode.iconst, 1);
            return null;
        } else if (Integer.valueOf(ctx.expr(1).getText()) > 0) {
            visit(ctx.expr(0));
            int exp = Integer.valueOf(ctx.expr(1).getText());
            if ("^".equals(ctx.op.getText())) {
                for (int i = 0; i < exp - 1; i++) {
                    visit(ctx.expr(0));
                    emit(OpCode.imult);
                }
            }
        }
//        }else {
//            visit(ctx.expr(0));
//
//        }
        return null;
        }
    
   /*
        Utility functions
    */

    public void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    public void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    // dump the code to the screen in "assembly" format
    public void dumpCode() {
        System.out.println("Generated code in assembly format");
        for (int i = 0; i < code.size(); i++)
            System.out.println(i + ": " + code.get(i));
    }

    // save the generated bytecodes to file filename
    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            for (Instruction inst : code)   // the instructions
                inst.writeTo(dout);
            System.out.println("Saving the bytecodes to " + filename);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
