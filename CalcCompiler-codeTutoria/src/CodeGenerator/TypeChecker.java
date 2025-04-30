package CodeGenerator;

import TabelaSimbolos.TabelaSimbolos;
import Tuga.*;
import org.antlr.v4.runtime.tree.ParseTree;
import TabelaSimbolos.*;

import java.util.*;

public class TypeChecker extends TugaBaseVisitor<Void> {
    TabelaSimbolos tabelaSimbolos;
    private int typeErrorCount = 0;
    private final Map<ParseTree, Tipo> types = new HashMap<>();
    private int addr = 0;
    private Tipo tipoRetornoAtual = Tipo.VOID;


    public TypeChecker(TabelaSimbolos tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
    }

    private Tipo combinarTipos(Tipo t1, Tipo t2) {
        if (t1 == Tipo.ERRO || t2 == Tipo.ERRO) return Tipo.ERRO;
        return switch (t1) {
            case INT ->
                    (t2 == Tipo.INT) ? Tipo.INT : (t2 == Tipo.REAL) ? Tipo.REAL : (t2 == Tipo.STRING) ? Tipo.STRING : Tipo.ERRO;
            case REAL ->
                    (t2 == Tipo.INT || t2 == Tipo.REAL) ? Tipo.REAL : (t2 == Tipo.STRING) ? Tipo.STRING : Tipo.ERRO;
            case STRING ->
                    (t2 == Tipo.STRING || t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.BOOL) ? Tipo.STRING : Tipo.ERRO;
            case BOOL -> (t2 == Tipo.STRING) ? Tipo.STRING : (t2 == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO;
            default -> Tipo.ERRO;
        };
    }

    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        for (var decl : ctx.varDeclaration()) visit(decl);
        for (var func : ctx.functionDecl()) visit(func);

        EntradaFuncao principal = (EntradaFuncao) tabelaSimbolos.getSimbolo("principal");
        if (principal == null) {
            System.out.println("erro na linha "+ctx.start.getLine()+": falta funcao principal()");
            typeErrorCount++;
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        Tipo tipo = Tipo.INT;
        if (ctx.TYPE() != null) {
            tipo = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        for (var id : ctx.ID()) {
            String nome = id.getText();
            if (!tabelaSimbolos.containsSimbolo(nome)) {
                tabelaSimbolos.putVariavel(nome, tipo, addr++);
            } else {
                System.out.printf("erro na linha %d: variavel '%s' ja foi declarada%n", ctx.start.getLine(), nome);
                typeErrorCount++;
            }
        }
        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();

        // Tipo de retorno
        Tipo tipoRetorno = Tipo.VOID;
        if (ctx.TYPE() != null) {
            tipoRetorno = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real"    -> Tipo.REAL;
                case "string"  -> Tipo.STRING;
                case "booleano"-> Tipo.BOOL;
                default        -> Tipo.ERRO;
            };
        }

        if (tabelaSimbolos.containsSimbolo(nome)) {
            System.err.printf("erro na linha %d: '%s' já foi declarado%n", ctx.start.getLine(), nome);
            typeErrorCount++;
            return null;
        }

        // Extrair parâmetros
        List<Tipo> tiposArgs = new ArrayList<>();
        Set<String> nomes = new HashSet<>();

        if (ctx.formalParameters() != null) {
            for (var param : ctx.formalParameters().formalParameter()) {
                String nomeParam = param.ID().getText();
                if (nomes.contains(nomeParam)) {
                    System.err.printf("erro na linha %d: '%s' duplicado%n", param.start.getLine(), nomeParam);
                    typeErrorCount++;
                    tiposArgs.add(Tipo.ERRO);
                    continue;
                }
                nomes.add(nomeParam);

                Tipo tipo = switch (param.TYPE().getText()) {
                    case "inteiro" -> Tipo.INT;
                    case "real"    -> Tipo.REAL;
                    case "string"  -> Tipo.STRING;
                    case "booleano"-> Tipo.BOOL;
                    default        -> Tipo.ERRO;
                };
                tiposArgs.add(tipo);
            }
        }

        // Regista a função
        tabelaSimbolos.putFuncao(nome, tipoRetorno, tiposArgs);

        // Novo escopo local
        TabelaSimbolos escopoLocal = new TabelaSimbolos(tabelaSimbolos);
        int localAddr = 0;

        if (ctx.formalParameters() != null) {
            var parametros = ctx.formalParameters().formalParameter();
            for (int i = 0; i < parametros.size(); i++) {
                String id = parametros.get(i).ID().getText();
                Tipo tipo = tiposArgs.get(i);
                escopoLocal.putVariavel(id, tipo, localAddr++);
            }
        }

        // Verificar corpo da função com novo TypeChecker
        TypeChecker checkerInterno = new TypeChecker(escopoLocal);
        checkerInterno.tipoRetornoAtual = tipoRetorno;
        checkerInterno.addr = localAddr;
        checkerInterno.visit(ctx.bloco());

        typeErrorCount += checkerInterno.getTypeErrorCount();
        types.putAll(checkerInterno.getTypes());

        return null;
    }


    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        String nome = ctx.ID().getText();
        Object simbolo = tabelaSimbolos.getSimbolo(nome);

//        if (!(simbolo instanceof EntradaFuncao func)) {
//            System.err.printf("erro na linha %d: '%s' nao é uma função%n", ctx.start.getLine(), nome);
//            typeErrorCount++;
//            types.put(ctx, Tipo.ERRO);
//            return null;
//        }

        List<Tipo> tiposEsperados = func.getTiposArgumentos();
        List<Tipo> tiposReais = new ArrayList<>();

        if (ctx.exprList() != null) {
            for (var expr : ctx.exprList().expr()) {
                visit(expr);
                tiposReais.add(getTipo(expr));
            }
        }

        if (tiposEsperados.size() != tiposReais.size()) {
            System.err.printf("erro na linha %d: número de argumentos inválido para função '%s'%n", ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
            return null;
        }

        for (int i = 0; i < tiposEsperados.size(); i++) {
            Tipo esperado = tiposEsperados.get(i);
            Tipo real = tiposReais.get(i);
            if (esperado != real && !(esperado == Tipo.REAL && real == Tipo.INT)) {
                System.err.printf("erro na linha %d: tipo do argumento %d inválido na função '%s'%n", ctx.start.getLine(), i+1, nome);
                typeErrorCount++;
            }
        }

        types.put(ctx, func.getTipoRetorno());
        return null;
    }
    @Override
    public Void visitChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx) {
        String nome = ctx.ID().getText();
        Object simbolo = tabelaSimbolos.getSimbolo(nome);

        if (!(simbolo instanceof EntradaFuncao func)) {
            System.err.printf("erro na linha %d: '%s' nao é uma função%n", ctx.start.getLine(), nome);
            typeErrorCount++;
            return null;
        }

        // Verifica se a função tem retorno (não pode)
        if (func.getTipoRetorno() != Tipo.VOID) {
            System.err.printf("erro na linha %d: chamada da função '%s' como instrução mas esta retorna valor%n", ctx.start.getLine(), nome);
            typeErrorCount++;
        }

        // Verificar número e tipo de argumentos
        List<Tipo> esperados = func.getTiposArgumentos();

        if (ctx.expr() == null) {
            if (!esperados.isEmpty()) {
                System.err.printf("erro na linha %d: função '%s' exige %d argumento(s)%n", ctx.start.getLine(), nome, esperados.size());
                typeErrorCount++;
            }
        } else {
            visit(ctx.expr());
            Tipo real = getTipo(ctx.expr());

            if (esperados.size() != 1) {
                System.err.printf("erro na linha %d: número de argumentos inválido na função '%s'%n", ctx.start.getLine(), nome);
                typeErrorCount++;
            } else {
                Tipo esperado = esperados.get(0);
                if (esperado != real && !(esperado == Tipo.REAL && real == Tipo.INT)) {
                    System.err.printf("erro na linha %d: tipo do argumento inválido na função '%s'%n", ctx.start.getLine(), nome);
                    typeErrorCount++;
                }
            }
        }

        return null;
    }




    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        if (ctx.expr() != null) {
            visit(ctx.expr());
            Tipo tipoExpr = getTipo(ctx.expr());
            if (tipoRetornoAtual != tipoExpr && !(tipoRetornoAtual == Tipo.REAL && tipoExpr == Tipo.INT)) {
                System.err.printf("erro na linha %d: tipo de retorno incompatível, esperado %s mas encontrado %s%n",
                        ctx.start.getLine(),
                        getTipoTexto(tipoRetornoAtual),
                        getTipoTexto(tipoExpr));
                typeErrorCount++;
            }
        } else {
            if (tipoRetornoAtual != Tipo.VOID) {
                System.err.printf("erro na linha %d: esperado valor de retorno do tipo %s%n",
                        ctx.start.getLine(),
                        getTipoTexto(tipoRetornoAtual));
                typeErrorCount++;
            }
        }
        return null;
    }



    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        visit(ctx.expr());
        String nomeVar = ctx.ID().getText();
        ValorSimbolo entrada =(ValorSimbolo) tabelaSimbolos.getSimbolo(nomeVar);

        Tipo tipoExpr = getTipo(ctx.expr());

        if (entrada == null) {
            System.out.printf("erro na linha %d: variavel '%s' nao foi declarada%n", ctx.start.getLine(), nomeVar);
            typeErrorCount++;

        }

        Tipo tipoEntrada = entrada.getTipo();
        if (tipoEntrada != tipoExpr && !(tipoEntrada == Tipo.REAL && tipoExpr == Tipo.INT)) {
            System.out.printf("erro na linha %d: operador '<-' eh invalido entre %s e %s%n", ctx.start.getLine(), getTipoTexto(tipoEntrada), getTipoTexto(tipoExpr));
            typeErrorCount++;
        }

        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        for (var stat : ctx.stat()) visit(stat);
        return null;
    }

    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        visit(ctx.expr());
        Tipo tipoCond = getTipo(ctx.expr());
        if (tipoCond != Tipo.BOOL) {
            System.out.printf("erro na linha %d: expressao de 'enquanto' nao eh do tipo booleano%n", ctx.start.getLine());
            typeErrorCount++;
        }
        visit(ctx.stat());
        return null;
    }

    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        visit(ctx.expr());
        Tipo tipoCond = getTipo(ctx.expr());
        if (tipoCond != Tipo.BOOL) {
            System.out.printf("erro na linha %d: expressao de 'se' nao eh do tipo booleano%n", ctx.start.getLine());
            typeErrorCount++;

        }
        visit(ctx.stat(0));
        if (ctx.stat().size() > 1) visit(ctx.stat(1));
        return null;
    }

    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        visit(ctx.expr());
        if (getTipo(ctx.expr()) == Tipo.ERRO) typeErrorCount++;
        return null;
    }

    @Override
    public Void visitVazia(TugaParser.VaziaContext ctx) {
        return null;
    }

    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        String nome = ctx.ID().getText();
        ValorSimbolo entrada = (ValorSimbolo) tabelaSimbolos.getSimbolo(nome);
        if (entrada == null) {
            System.err.printf("erro na linha %d: variavel '%s' nao foi declarada%n", ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        } else {
            types.put(ctx, entrada.getTipo());
        }
        return null;
    }

    @Override
    public Void visitOr(TugaParser.OrContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        types.put(ctx, (getTipo(ctx.expr(0)) == Tipo.BOOL && getTipo(ctx.expr(1)) == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    @Override
    public Void visitAnd(TugaParser.AndContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        types.put(ctx, (getTipo(ctx.expr(0)) == Tipo.BOOL && getTipo(ctx.expr(1)) == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        return null;
    }

    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        types.put(ctx, combinarTipos(getTipo(ctx.expr(0)), getTipo(ctx.expr(1))));
        return null;
    }

    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));
        types.put(ctx, (t1 == Tipo.INT && t2 == Tipo.INT) ? Tipo.INT : (t1 == Tipo.REAL || t2 == Tipo.REAL) ? Tipo.REAL : Tipo.ERRO);
        return null;
    }

    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));
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
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipoExpr = getTipo(ctx.expr());
        if (ctx.op.getType() == TugaParser.MINUS) {
            types.put(ctx, (tipoExpr == Tipo.INT || tipoExpr == Tipo.REAL) ? tipoExpr : Tipo.ERRO);
        } else if (ctx.op.getType() == TugaParser.NOT) {
            types.put(ctx, (tipoExpr == Tipo.BOOL) ? Tipo.BOOL : Tipo.ERRO);
        } else {
            types.put(ctx, Tipo.ERRO);
        }
        return null;
    }

    @Override
    public Void visitParens(TugaParser.ParensContext ctx) {
        visit(ctx.expr());
        types.put(ctx, getTipo(ctx.expr()));
        return null;
    }

    @Override
    public Void visitInt(TugaParser.IntContext ctx) {
        types.put(ctx, Tipo.INT);
        return null;
    }

    @Override
    public Void visitReal(TugaParser.RealContext ctx) {
        types.put(ctx, Tipo.REAL);
        return null;
    }

    @Override
    public Void visitString(TugaParser.StringContext ctx) {
        types.put(ctx, Tipo.STRING);
        return null;
    }

    @Override
    public Void visitBool(TugaParser.BoolContext ctx) {
        types.put(ctx, Tipo.BOOL);
        return null;
    }

    public Tipo getTipo(ParseTree node) {
        return types.getOrDefault(node, Tipo.ERRO);
    }

    public int getTypeErrorCount() {
        return typeErrorCount;
    }

    public Map<ParseTree, Tipo> getTypes() {
        return types;
    }

    private String getTipoTexto(Tipo tipo) {
        return switch (tipo) {
            case INT -> "inteiro";
            case REAL -> "real";
            case STRING -> "string";
            case BOOL -> "booleano";
            case VOID -> "vazio";
            case ERRO -> "erro";
        };
    }
    public int getAddr(){return addr;}
}
