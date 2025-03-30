package CodeGenerator;

import Calc.*;

import java.util.Stack;

public class TypeChecker extends CalcBaseVisitor<Void> {

    private Stack<Tipo> types = new Stack<>();


    private void typeToUse(Tipo t1, Tipo t2) {
        switch (t1) {
            case INT -> {
                if (t2 == Tipo.INT) types.push(Tipo.INT);
                else if (t2 == Tipo.REAL) types.push(Tipo.REAL);
                else if (t2 == Tipo.STRING) types.push(Tipo.STRING);
            }
            case REAL -> {
                if (t2 == Tipo.INT || t2 == Tipo.REAL) types.push(Tipo.REAL);
                else if (t2 == Tipo.STRING) types.push(Tipo.STRING);
            }
            case STRING -> {
                if (t2 == Tipo.STRING || t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.BOOL) types.push(Tipo.STRING);
            }
            case BOOL -> {
                if (t2 == Tipo.STRING) types.push(Tipo.STRING);
                else if (t2 == Tipo.BOOL) types.push(Tipo.BOOL);
            }
            default -> types.push(Tipo.ERRO);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */


    @Override
    public Void visitProg(CalcParser.ProgContext ctx) {

        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitStat(CalcParser.StatContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitOr(CalcParser.OrContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        Tipo t2 = types.pop();
        Tipo t1 = types.pop();

        typeToUse(t1, t2);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitBool(CalcParser.BoolContext ctx) {
        types.push(Tipo.BOOL);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        Tipo t2 = types.pop();
        Tipo t1 = types.pop();

        typeToUse(t1, t2);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        Tipo t2 = types.pop();
        Tipo t1 = types.pop();

        typeToUse(t1, t2);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitParens(CalcParser.ParensContext ctx) {
        visit(ctx.expr());
        //visita o nó até ao no terminal e faz push a esse tipo
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitAnd(CalcParser.AndContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        Tipo t2 = types.pop();
        Tipo t1 = types.pop();

        typeToUse(t1, t2);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitReal(CalcParser.RealContext ctx) {
        types.push(Tipo.REAL);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitRelational(CalcParser.RelationalContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        Tipo t2 = types.pop();
        Tipo t1 = types.pop();

        typeToUse(t1, t2);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitString(CalcParser.StringContext ctx) {
        types.push(Tipo.STRING);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitUnary(CalcParser.UnaryContext ctx) {
        visit(ctx.expr());
        // visita o no até ao terminal e faz push do tipo

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Void visitInt(CalcParser.IntContext ctx) {
        types.push(Tipo.INT);
        return null;
    }

    public Stack<Tipo> getTypes() {return types;}

}
