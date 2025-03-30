// Generated from C:/Users/luz33/Desktop/UALG/2024_2025/2º Semestre/Compiladores/Projeto/CalcCompiler-codeTutoria/out/production/CalcCompiler-codeTutoria/Calc.g4 by ANTLR 4.13.2
package Calc;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CalcParser}.
 */
public interface CalcListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CalcParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(CalcParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link CalcParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(CalcParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by {@link CalcParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(CalcParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link CalcParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(CalcParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualNotEqual}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqualNotEqual(CalcParser.EqualNotEqualContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualNotEqual}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqualNotEqual(CalcParser.EqualNotEqualContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDiv(CalcParser.MulDivContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDiv(CalcParser.MulDivContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(CalcParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(CalcParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Real}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterReal(CalcParser.RealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitReal(CalcParser.RealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelational(CalcParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelational(CalcParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Logical}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLogical(CalcParser.LogicalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Logical}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLogical(CalcParser.LogicalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code String}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterString(CalcParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code String}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitString(CalcParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(CalcParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(CalcParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parentheses}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParentheses(CalcParser.ParenthesesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parentheses}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParentheses(CalcParser.ParenthesesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnary(CalcParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnary(CalcParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Int}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInt(CalcParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link CalcParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInt(CalcParser.IntContext ctx);
}