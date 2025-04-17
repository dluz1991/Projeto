package CodeGenerator;

import TabelaSimbolos.TabelaSimbolos;
import Tuga.*;
import org.antlr.v4.runtime.tree.ParseTree;
import TabelaSimbolos.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por verificar os tipos das expressões na árvore de sintaxe.
 * Extende a classe TugaBaseVisitor para visitar os nós da árvore.
 */
public class TypeChecker extends TugaBaseVisitor<Void> {
    TabelaSimbolos tabelaSimbolos;
    /**
     * Contador de erros de tipo encontrados durante a verificação.
     * Inicializado em 0 e incrementado sempre que um erro de tipo é encontrado.
     */
    private int typeErrorCount = 0;
    /**
     * Mapa que associa cada nó da árvore de sintaxe a seu tipo correspondente.
     */
    private final Map<ParseTree, Tipo> types = new HashMap<>();

    public TypeChecker(TabelaSimbolos tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
    }

    /**
     * Método responsável por combinar dois tipos.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     *
     * @param t1 TIPO no nó da esquerda a ser combinado.
     * @param t2 TIPO no nó da direita a ser combinado.
     * @return O tipo resultante da combinação dos dois tipos.
     */
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
                if (t2 == Tipo.STRING || t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.BOOL) return Tipo.STRING;
            }
            case BOOL -> {
                if (t2 == Tipo.STRING) return Tipo.STRING;
                else if (t2 == Tipo.BOOL) return Tipo.BOOL;
            }
        }
        return Tipo.ERRO;
    }

    /**
     * Método responsável por visitar o nó raiz da árvore de sintaxe.
     * Chama o método visitChildren para visitar todos os filhos do nó.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     *
     * @param ctx nó raiz da árvore de sintaxe.
     * @return null
     */
    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {

        return visitChildren(ctx);
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        Tipo tipo = Tipo.INT; // default

        if (ctx.TYPE() != null) {
            String tipoTexto = ctx.TYPE().getText();
            tipo = switch (tipoTexto) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        for (var id : ctx.ID()) {
            String nome = id.getText();
            tabelaSimbolos.putSimbolo(nome, tipo);

        }

        return null;
    }

    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        if (getTypeErrorCount() > 0) return null;

        String nomeVar = ctx.ID().getText();
        ValorSimbolo entrada = tabelaSimbolos.getSimbolo(nomeVar);

        if (entrada == null) {
            System.err.printf("Erro: variável \"%s\" não foi declarada.%n", nomeVar);
            typeErrorCount++;
            return null;
        }

        visit(ctx.expr());
        Tipo tipoExpr = types.getOrDefault(ctx.expr(), Tipo.ERRO);

        if (entrada.getTipo() != tipoExpr && !(entrada.getTipo() == Tipo.REAL && tipoExpr == Tipo.INT)) {
            System.err.printf("Erro: atribuição inválida para a variável \"%s\". Esperado: %s, encontrado: %s%n",
                    nomeVar, entrada.getIndex(), tipoExpr);
            typeErrorCount++;
        }

        return null;
    }


    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        return visitChildren(ctx);
    }

    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        if (getTypeErrorCount() > 0) return null;

        visit(ctx.expr());
        Tipo tipoCond = types.getOrDefault(ctx.expr(), Tipo.ERRO);

        if (tipoCond != Tipo.BOOL) {
            System.err.println("Erro: a condição de \"enquanto\" deve ser do tipo booleano.");
            typeErrorCount++;
        }

        for (var stmt : ctx.stat()) {
            visit(stmt);
        }

        return null;
    }


    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        if (getTypeErrorCount() > 0) return null;

        visit(ctx.expr());
        Tipo tipoCond = types.getOrDefault(ctx.expr(), Tipo.ERRO);

        if (tipoCond != Tipo.BOOL) {
            System.err.println("Erro: a condição de \"se\" deve ser do tipo booleano.");
            typeErrorCount++;
        }

        visit(ctx.stat(0)); // ramo "então"
        if (ctx.stat().size() > 1) {
            visit(ctx.stat(1)); // ramo "senao"
        }

        return null;
    }


    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr());
        Tipo tipo = types.get(ctx.expr());
        if (tipo == Tipo.ERRO) {
            typeErrorCount++; // <--- increment here
        }
        return null;
    }

    @Override
    public Void visitVazia(TugaParser.VaziaContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        return null;
    }
    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        if (getTypeErrorCount() > 0) return null;

        String nome = ctx.ID().getText();
        ValorSimbolo entrada = tabelaSimbolos.getSimbolo(nome);

        if (entrada == null) {
            System.err.printf("Erro: variável \"%s\" não foi declarada.%n", nome);
            types.put(ctx, Tipo.ERRO);
            typeErrorCount++;
        } else {
            types.put(ctx, entrada.getTipo());
        }

        return null;
    }


    /**
     * Método responsável por visitar um nó de atribuição.
     * Chama o método visit para visitar a expressão contida na atribuição.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     *
     * @param ctx nó de atribuição.
     * @return null
     */
    @Override
    public Void visitOr(TugaParser.OrContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        types.put(ctx, (t1 == Tipo.BOOL && t2 == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    /**
     * Método responsável por visitar um nó de negação.
     * Chama o método visit para visitar a expressão contida na negação.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     *
     * @param ctx nó de negação.
     * @return null
     */
    @Override
    public Void visitAnd(TugaParser.AndContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        types.put(ctx, (t1 == Tipo.BOOL && t2 == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    /**
     * Método responsável por visitar um nó de adição ou subtração.
     * Chama o método visit para visitar as expressões contidas na adição ou subtração.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     * Para operações de adicão e subtração.
     *
     * @param ctx nó de adição ou subtração.
     * @return null
     */
    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));
        Tipo result = combinarTipos(t1, t2);
        types.put(ctx, result);
        return null;
    }

    /**
     * Método responsável por visitar um nó de multiplicação ou divisão.
     * Chama o método visit para visitar as expressões contidas na multiplicação ou divisão.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     * Para operações de multiplicação e divisão.
     *
     * @param ctx nó de multiplicação ou divisão.
     * @return null
     */
    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = types.get(ctx.expr(0));
        Tipo t2 = types.get(ctx.expr(1));

        if (t1 == Tipo.INT && t2 == Tipo.INT) {
            types.put(ctx, Tipo.INT);
        } else if ((t1 == Tipo.INT || t1 == Tipo.REAL) && (t2 == Tipo.INT || t2 == Tipo.REAL)) {
            types.put(ctx, Tipo.REAL);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    /**
     * Método responsável por visitar um nó de comparação.
     * Chama o método visit para visitar as expressões contidas na comparação.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     * Para operações de comparação. <, >, <=, >=, ==, !=.
     *
     * @param ctx nó de comparação.
     * @return null
     */
    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        if (getTypeErrorCount() > 0) return null;
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

    /**
     * Método responsável por visitar um nó de negação.
     * Chama o método visit para visitar a expressão contida na negação.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     * Para operações de negação.
     *
     * @param ctx nó de negação.
     * @return null
     */
    @Override
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr());
        Tipo tipoExpr = types.get(ctx.expr());
        if (ctx.op.getType() == TugaParser.MINUS) {
            if (tipoExpr == Tipo.INT || tipoExpr == Tipo.REAL) types.put(ctx, tipoExpr);
            else types.put(ctx, Tipo.ERRO);
        } else if (ctx.op.getType() == TugaParser.NOT) {
            if (tipoExpr == Tipo.BOOL) types.put(ctx, Tipo.BOOL);
            else types.put(ctx, Tipo.ERRO);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    /**
     * Método responsável por visitar um nó de parênteses.
     * Chama o método visit para visitar a expressão contida nos parênteses.
     * Aceita os tipos INT, REAL, STRING e BOOL.
     *
     * @param ctx nó de parênteses.
     * @return null
     */
    @Override
    public Void visitParens(TugaParser.ParensContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        visit(ctx.expr());
        types.put(ctx, types.get(ctx.expr()));
        return null;
    }

    /**
     * Método responsável por visitar um nó de variável.
     * Chama o método visit para visitar a expressão contida na variável.
     * Aceita os tipos INT.
     * Introduza o tipo da variável no mapa de tipos.
     *
     * @param ctx nó de variável e chave do hashmap.
     * @return null
     */
    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        types.put(ctx, Tipo.INT);
        return null;
    }

    /**
     * Método responsável por visitar um nó de variável.
     * Chama o método visit para visitar a expressão contida na variável.
     * Aceita os tipos REAL.
     * Introduza o tipo da variável no mapa de tipos.
     *
     * @param ctx nó de variável e chave do hashmap.
     * @return null
     */
    @Override
    public Void visitReal(TugaParser.RealContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        types.put(ctx, Tipo.REAL);
        return null;
    }

    /**
     * Método responsável por visitar um nó de variável.
     * Chama o método visit para visitar a expressão contida na variável.
     * Aceita os tipos STRING.
     * Introduza o tipo da variável no mapa de tipos.
     *
     * @param ctx nó de variável e chave do hashmap.
     * @return null
     */
    @Override
    public Void visitString(TugaParser.StringContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        types.put(ctx, Tipo.STRING);
        return null;
    }

    /**
     * Método responsável por visitar um nó de variável.
     * Chama o método visit para visitar a expressão contida na variável.
     * Aceita os tipos BOOL.
     * Introduza o tipo da variável no mapa de tipos.
     *
     * @param ctx nó de variável e chave do hashmap.
     * @return null
     */
    @Override
    public Void visitBool(TugaParser.BoolContext ctx) {
        if (getTypeErrorCount() > 0) return null;
        types.put(ctx, Tipo.BOOL);
        return null;
    }

    /**
     * Método responsável por visitar um nó de variável.
     * Chama o método visit para visitar a expressão contida na variável.
     * Aceita os tipos ID, REAL, STRING e BOOL.
     * Introduz o tipo da variável no mapa de tipos.
     *
     * @param node nó de variável e chave do hashmap.
     * @return Tipo como resultado.
     */
    public Tipo getTipo(ParseTree node) {
        return types.getOrDefault(node, Tipo.ERRO);
    }

    /**
     * Método responsável por retornar o contador de erros de tipo encontrados.
     *
     * @return O contador de erros de tipo encontrados.
     */

    public int getTypeErrorCount() {
        return typeErrorCount;
    }

    public Map<ParseTree, Tipo> getTypes() {
        return types;
    }
}
