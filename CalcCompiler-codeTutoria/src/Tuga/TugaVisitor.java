// Generated from C:/Users/maxis/Desktop/Compilators/Projeto/CalcCompiler-codeTutoria/src/Tuga.g4 by ANTLR 4.13.2
package Tuga;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TugaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TugaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(TugaParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(TugaParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(TugaParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(TugaParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDiv(TugaParser.MulDivContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParens(TugaParser.ParensContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(TugaParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReal(TugaParser.RealContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(TugaParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(TugaParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(TugaParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInt(TugaParser.IntContext ctx);
}