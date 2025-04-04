package CodeGenerator;

import Calc.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;

public class TypeChecker extends CalcBaseVisitor<Void> {

    // Guarda o tipo de cada nó
    private final Map<ParseTree, Tipo> types = new HashMap<>();

    private Tipo combinarTipos(Tipo t1, Tipo t2) {
        if (t1 == Tipo.ERRO || t2 == Tipo.ERRO) return Tipo.ERRO;
        switch (t1) {
            case INT -> {
                if (t2 == Tipo.INT) return Tipo.INT;
                else if (t2 == Tipo.REAL) return Tipo.REAL;
                else if (t2 == Tipo.STRING) return Tipo.STRING;
            }
            case REAL -> {
                if (t2 == Tipo.INT || t2 == Tipo.REAL) return Tipo.REAL;
                else if (t2 == Tipo.STRING) return Tipo.STRING;
            }
            case STRING -> {
                if (t2 == Tipo.STRING || t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.BOOL)
                    return Tipo.STRING;
            }
            case BOOL -> {
                if (t2 == Tipo.STRING) return Tipo.STRING;
                else if (t2 == Tipo.BOOL) return Tipo.BOOL;
            }
        }
        return Tipo.ERRO;
    }

    @Override
    public Void visitProg(CalcParser.ProgContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitStat(CalcParser.StatContext ctx) {
        visit(ctx.expr());
        Tipo tipo = types.get(ctx.expr());
        if (tipo == Tipo.ERRO) {
            System.err.println("Erro de tipo na expressão.");
        }
        return null;
    }

    @Override
    public Void visitOr(CalcParser.OrContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        types.put(ctx, (t1 == Tipo.BOOL && t2 == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    @Override
    public Void visitAnd(CalcParser.AndContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        types.put(ctx, (t1 == Tipo.BOOL && t2 == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        types.put(ctx, combinarTipos(t1, t2));
        return null;
    }

    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        String op = ctx.op.getText();

        if (t1 == Tipo.INT && t2 == Tipo.INT) {
            types.put(ctx, Tipo.INT);
        } else if ((t1 == Tipo.INT || t1 == Tipo.REAL) && (t2 == Tipo.INT || t2 == Tipo.REAL)) {
            types.put(ctx, Tipo.REAL);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    @Override
    public Void visitRelational(CalcParser.RelationalContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        if ((t1 == Tipo.INT || t1 == Tipo.REAL) && (t2 == Tipo.INT || t2 == Tipo.REAL)) {
            types.put(ctx, Tipo.BOOL);
        } else if (t1 == t2 && t1 != Tipo.ERRO) {
            types.put(ctx, Tipo.BOOL);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    @Override
    public Void visitUnary(CalcParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipoExpr = types.get(ctx.expr());
        if (ctx.op.getType() == CalcParser.MINUS) {
            if (tipoExpr == Tipo.INT || tipoExpr == Tipo.REAL)
                types.put(ctx, tipoExpr);
            else types.put(ctx, Tipo.ERRO);
        } else if (ctx.op.getType() == CalcParser.NOT) {
            if (tipoExpr == Tipo.BOOL)
                types.put(ctx, Tipo.BOOL);
            else types.put(ctx, Tipo.ERRO);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    @Override
    public Void visitParens(CalcParser.ParensContext ctx) {
        visit(ctx.expr());
        types.put(ctx, types.get(ctx.expr()));
        return null;
    }

    @Override
    public Void visitInt(CalcParser.IntContext ctx) {
        types.put(ctx, Tipo.INT);
        return null;
    }

    @Override
    public Void visitReal(CalcParser.RealContext ctx) {
        types.put(ctx, Tipo.REAL);
        return null;
    }

    @Override
    public Void visitString(CalcParser.StringContext ctx) {
        types.put(ctx, Tipo.STRING);
        return null;
    }

    @Override
    public Void visitBool(CalcParser.BoolContext ctx) {
        types.put(ctx, Tipo.BOOL);
        return null;
    }

    // Getter para consultar o tipo de qualquer nó
    public Tipo getTipo(ParseTree node) {
        return types.getOrDefault(node, Tipo.ERRO);
    }

    public Map<ParseTree, Tipo> getTypes() {
        return types;
    }
}
