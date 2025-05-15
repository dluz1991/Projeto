// Generated from C:/Users/luz33/Desktop/UALG/2024_2025/2º Semestre/Compiladores/Projeto/CalcCompiler-codeTutoria/src/Tuga.g4 by ANTLR 4.13.2
package Tuga;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TugaParser}.
 */
public interface TugaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(TugaParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(TugaParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(TugaParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(TugaParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(TugaParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(TugaParser.FormalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(TugaParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(TugaParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#bloco}.
	 * @param ctx the parse tree
	 */
	void enterBloco(TugaParser.BlocoContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#bloco}.
	 * @param ctx the parse tree
	 */
	void exitBloco(TugaParser.BlocoContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclaration(TugaParser.VarDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclaration(TugaParser.VarDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Afetacao}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterAfetacao(TugaParser.AfetacaoContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Afetacao}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitAfetacao(TugaParser.AfetacaoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BlocoStat}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterBlocoStat(TugaParser.BlocoStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BlocoStat}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitBlocoStat(TugaParser.BlocoStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Equanto}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterEquanto(TugaParser.EquantoContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Equanto}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitEquanto(TugaParser.EquantoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Se}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterSe(TugaParser.SeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Se}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitSe(TugaParser.SeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Escreve}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterEscreve(TugaParser.EscreveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Escreve}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitEscreve(TugaParser.EscreveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Retorna}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterRetorna(TugaParser.RetornaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Retorna}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitRetorna(TugaParser.RetornaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ChamadaFuncaoStat}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterChamadaFuncaoStat(TugaParser.ChamadaFuncaoStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ChamadaFuncaoStat}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitChamadaFuncaoStat(TugaParser.ChamadaFuncaoStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Vazia}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterVazia(TugaParser.VaziaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Vazia}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitVazia(TugaParser.VaziaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOr(TugaParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOr(TugaParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDiv(TugaParser.MulDivContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDiv(TugaParser.MulDivContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParens(TugaParser.ParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParens(TugaParser.ParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Var}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVar(TugaParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Var}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVar(TugaParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelational(TugaParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Relational}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelational(TugaParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterString(TugaParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitString(TugaParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnary(TugaParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnary(TugaParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInt(TugaParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInt(TugaParser.IntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ChamadaFuncaoExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ChamadaFuncaoExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBool(TugaParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBool(TugaParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd(TugaParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd(TugaParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterReal(TugaParser.RealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitReal(TugaParser.RealContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#chamadaFuncao}.
	 * @param ctx the parse tree
	 */
	void enterChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#chamadaFuncao}.
	 * @param ctx the parse tree
	 */
	void exitChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(TugaParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(TugaParser.ExprListContext ctx);
}