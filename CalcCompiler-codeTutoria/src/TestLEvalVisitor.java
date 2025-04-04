/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/

import Calc.CalcBaseVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import Calc.*;

public class TestLEvalVisitor {
    // a4 -visitor Expr.g4

    /**
     * Visitor "calculator"
     */
    public static class EvalVisitor extends CalcBaseVisitor<Integer> {
        public Integer visitMulDiv(CalcParser.MulDivContext ctx) {
            return visit(ctx.expr(0)) * visit(ctx.expr(1));
        }

        public Integer visitAddSub(CalcParser.AddSubContext ctx) {
            return visit(ctx.expr(0)) + visit(ctx.expr(1));
        }

        public Integer visitInt(CalcParser.IntContext ctx) {
            return Integer.valueOf(ctx.INT().getText());
        }

        public Integer visitOr(CalcParser.OrContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitBool(CalcParser.BoolContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitParens(CalcParser.ParensContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitAnd(CalcParser.AndContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitReal(CalcParser.RealContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitRelational(CalcParser.RelationalContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitString(CalcParser.StringContext ctx) {
            return visitChildren(ctx);
        }

        public Integer visitUnary(CalcParser.UnaryContext ctx) {
            return visitChildren(ctx);
        }

    }

    public static void main(String[] args) throws Exception {
        boolean showLexerErrors = true;
        boolean showParserErrors = true;

        String inputFile = null;
        if (args.length > 0) inputFile = args[0];
        InputStream is = System.in;
        try {
            if (inputFile != null) is = new FileInputStream(inputFile);
            CharStream input = CharStreams.fromStream(is);

            CalcLexer lexer = new CalcLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CalcParser parser = new CalcParser(tokens);
            ParseTree tree = parser.prog();


            //
            // add my own error listener
            //

            MyErrorListener errorListener = new MyErrorListener(showLexerErrors, showParserErrors);
            CalcLexer lexer1 = new CalcLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            CommonTokenStream tokens1 = new CommonTokenStream(lexer);
            CalcParser parser1 = new CalcParser(tokens);


            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            ParseTree tree1 = parser.prog();

            if (errorListener.getNumLexerErrors() > 0) {
                System.out.println("Input has lexical errors");
                return;
            }
            if (errorListener.getNumParsingErrors() > 0) {
                System.out.println("Input has parsing errors");
                return;
            }

            EvalVisitor evalVisitor = new EvalVisitor();
            int result = evalVisitor.visit(tree);
            System.out.println("visitor result = " + result);
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
