package CodeGenerator;

public class TypeChecker extends CalcBaseVisitor<Void> {

    // the target code
    private final StringBuilder errors = new StringBuilder();

    // stat: expr NEWLINE;
    @Override
    public Void visitStat(CalcParser.StatContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // expr: '-' expr                          # Uminus
    @Override
    public Void visitUminus(CalcParser.UminusContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // expr: expr op=('*'|'/') expr            # MulDiv
    @Override
    public Void visitMulDiv(CalcParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        return null;
    }

    // expr: expr op=('+'|'-') expr            # AddSub
    @Override
    public Void visitAddSub(CalcParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        return null;
    }

    // expr: INT                           # Int
    @Override
    public Void visitInt(CalcParser.IntContext ctx) {
        return null;
    }


}
