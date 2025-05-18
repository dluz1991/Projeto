package CodeGenerator;

import TabelaSimbolos.FuncaoSimbolo;
import TabelaSimbolos.*;
import Tuga.*;
import org.antlr.v4.runtime.tree.ParseTree;
import TabelaSimbolos.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class TypeChecker extends TugaBaseVisitor<Void> {
    TabelaSimbolos tabelaSimbolos;
    private final Map<ParseTree, Tipo> types = new HashMap<>();
    private int typeErrorCount = 0;
    private int addr = 0;
    private Tipo tipoRetornoAtual = Tipo.VOID;
    ParseTreeProperty<Enquadramento> scopes = new ParseTreeProperty<Enquadramento>();

    public TypeChecker(TabelaSimbolos tabelaSimbolos, ParseTreeProperty<Enquadramento> scopes) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.scopes = scopes;


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
            default -> Tipo.VOID;
        };
    }


    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        //1.º PASSO - variáveis globais

        for (TugaParser.VarDeclarationContext decl : ctx.varDeclaration()) {

            visit(decl);
        }
        // 2.º PASSO - visitar corpo de cada função
        for (var funcDecl : ctx.functionDecl()) {

            visit(funcDecl);
        }
        //System.out.printf(tabelaSimbolos.toString()); //print para despiste
        // 3.º PASSO - garantir existência de principal()
        if (!tabelaSimbolos.existeFunc("principal")) {
            System.out.printf("erro na linha %d: falta funcao principal()%n",ctx.getStop().getLine()+1);
            typeErrorCount++;
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        // Recupera o escopo atual através do ctx
        Enquadramento currentScope = scopes.get(ctx);
        //System.out.println("Escopo atual: " + currentScope.getCurrentScope());

        Tipo tipo = Tipo.INT;  // Tipo padrão
        if (ctx.TYPE() != null) {
            tipo = getTipo(ctx.TYPE().getText());
        }

        // Verificar se a variável já foi declarada no escopo atual
        for (TerminalNode id : ctx.ID()) {
            String nome = id.getText();
            if (!tabelaSimbolos.existeVar(nome)) {
                tabelaSimbolos.putVariavel(nome, tipo, addr++, currentScope.getCurrentScope());
            }

            if (currentScope.contains(nome)) {
                // Lidar com erro se a variável já existir no escopo
                System.out.printf("erro na linha %d: variável '%s' já declarada no escopo atual.%n", ctx.start.getLine(), nome);
                typeErrorCount++;
            } else {
                currentScope.put(nome, tipo);

            }
        }
        return null;
    }


    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        // Recupera o escopo atual através do ctx
        Enquadramento currentScope = scopes.get(ctx);

        String nome = ctx.ID().getText();
        VarSimbolo simbolo = tabelaSimbolos.getVar(nome);

        // Verificar se a variável está definida
        if (simbolo == null) {
            System.out.printf("erro na linha %d: variável '%s' não foi declarada.%n", ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        } else {
            types.put(ctx, simbolo.getTipo());
        }
        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();

        // Tipo de retorno
        Tipo tipoRet = Tipo.VOID;
        if (ctx.TYPE() != null) {
            tipoRet = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        // Obter escopo atual da função
        Enquadramento currentScope = scopes.get(ctx);
        int escopoFunc = currentScope.getCurrentScope();

        // Construir lista de argumentos com índices negativos
        List<VarSimbolo> tiposArgs = new ArrayList<>();
        if (ctx.formalParameters() != null) {
            List<TugaParser.FormalParameterContext> params = ctx.formalParameters().formalParameter();
            for (int i = 0; i < params.size(); i++) {
                var p = params.get(i);
                String nomeArg = p.ID().getText();
                Tipo tipoArg = getTipo(p.TYPE().getText());
                int idx = -1 - i;
                tiposArgs.add(new VarSimbolo(nomeArg, tipoArg, idx, escopoFunc));
                currentScope.put(nomeArg, tipoArg); // registar tipo no escopo
            }
        }

        if (tabelaSimbolos.existeFunc(nome)) {
            System.out.printf("erro na linha %d: função '%s' já declarada no escopo atual.%n", ctx.start.getLine(), nome);
            typeErrorCount++;
        } else {
            tabelaSimbolos.putFuncao(nome, tipoRet, tiposArgs, escopoFunc);
        }

        return null;
    }


    @Override
    public Void visitChamadaFuncaoStat(TugaParser.ChamadaFuncaoStatContext ctx) {
        String nomeFunc = ctx.chamadaFuncao().ID().getText();
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(nomeFunc);

        if (funcao == null) {
            System.out.printf("erro na linha %d: função '%s' não foi declarada%n", ctx.start.getLine(), nomeFunc);
            typeErrorCount++;
            return null;
        }

        if (funcao.getTipoRetorno() != Tipo.VOID) {
            System.out.printf("erro na linha %d: chamada à função '%s' não é válida como instrução, pois retorna um valor%n",
                    ctx.start.getLine(), nomeFunc);
            typeErrorCount++;
        }

        return null;
    }

    /*------------------------------------------------------------------
     *  visitChamadaFuncaoExpr
     *------------------------------------------------------------------*/
    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        // Recupera o escopo atual através do ctx
        Enquadramento currentScope = scopes.get(ctx);

        String nomeFunc = ctx.chamadaFuncao().ID().getText();
        FuncaoSimbolo funcao = tabelaSimbolos.getFuncao(nomeFunc);
        // Verificar se a função está declarada
        if (funcao == null) {
            System.out.printf("erro na linha %d: função '%s' não declarada.%n", ctx.start.getLine(), nomeFunc);
            types.put(ctx,Tipo.ERRO);;
            typeErrorCount++;
        }

        // Verificar se os tipos dos parâmetros correspondem à declaração da função
        int numParametros = ctx.chamadaFuncao().exprList().expr().size();
        //System.out.println("Numero de parametros: " + numParametros); //debugging
        if (numParametros != funcao.getArgumentos().size()) {
            System.out.printf("erro na linha %d: '%s' requer %d argumentos", ctx.start.getLine(), nomeFunc, numParametros);
            typeErrorCount++;
        }

        for (int i = 0; i < numParametros; i++) {
            Tipo tipoEsperado = funcao.getArgumentos().get(i).getTipo();
            Tipo tipoReal = getTipo(ctx.chamadaFuncao().exprList().expr(i));
            String nomeArg = ctx.chamadaFuncao().exprList().expr(i).getText();

            // Verificar se o tipo do argumento é compatível com o esperado
            if (tipoEsperado != tipoReal) {
                System.out.printf("erro na linha %d: '%s'devia ser do tipo %s .%n", ctx.start.getLine(), nomeArg, tipoEsperado);
                typeErrorCount++;
            }
        }
        types.put(ctx, funcao.getTipoRetorno());
        return null;
    }


    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        // Recupera o escopo atual através do ctx
        Enquadramento currentScope = scopes.get(ctx);

        // Verifica o nome da função atual (ou busca na tabela de símbolos)
        String nomeFuncao = obterNomeFuncao(ctx);// Ou outro método para pegar o nome da função no escopo
        FuncaoSimbolo funcaoAtual = tabelaSimbolos.getFuncao(nomeFuncao);

        if (funcaoAtual == null) {
            System.out.println("erro na linha "+ ctx.start.getLine()+": Não foi possível localizar a função para o retorno.");
            return null;
        }

        Tipo tipoRetornoEsperado = funcaoAtual.getTipoRetorno();

        // Verificação para funções do tipo void
        if (tipoRetornoEsperado == Tipo.VOID) {
            // Se a função for void, não deve ter uma expressão no retorno
            return null;
        } else {
            // Se a função não for void, deve ter uma expressão e o tipo da expressão deve ser compatível
            if (ctx.expr() != null) {
                Tipo tipoExprRetornada = getTipo(ctx.expr());

                // Verifica se o tipo da expressão retornada é compatível com o tipo de retorno da função
                if (tipoRetornoEsperado != tipoExprRetornada) {
                    // Verificação de compatibilidade (sem conversão de tipos nesta fase)
                    System.out.printf("erro na linha %d: tipo de retorno incompatível. Esperado %s, encontrado %s.%n", ctx.start.getLine(), getTipoTexto(tipoRetornoEsperado), getTipoTexto(tipoExprRetornada));
                    typeErrorCount++;
                }
            } else {
                // Caso a função espere um retorno, mas a expressão não foi fornecida
                System.out.printf("erro na linha %d: função '%s' espera um valor de retorno do tipo %s, mas nenhum valor foi retornado.%n", ctx.start.getLine(), funcaoAtual.getNome(), getTipoTexto(tipoRetornoEsperado));
                typeErrorCount++;
            }
        }

        return null;
    }

    private String obterNomeFuncao(ParseTree ctx) {
        while (ctx != null && !(ctx instanceof TugaParser.FunctionDeclContext)) {
            ctx = ctx.getParent();
        }
        if (ctx instanceof TugaParser.FunctionDeclContext funcCtx) {
            return funcCtx.ID().getText();
        }
        return null;
    }



    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        // Recupera o escopo atual através do ctx
        Enquadramento currentScope = scopes.get(ctx);

        visit(ctx.expr());
        String nomeVar = ctx.ID().getText();
        VarSimbolo simbolo = tabelaSimbolos.getVar(nomeVar);

        Tipo tipoExpr = getTipo(ctx.expr());

        if (simbolo == null) {
            System.out.printf("erro na linha %d: variavel '%s' nao foi declarada%n", ctx.start.getLine(), nomeVar);
            typeErrorCount++;
        }

        Tipo tipoEntrada = simbolo.getTipo();
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
        Tipo tipo1 = getTipo(ctx.expr(0));
        Tipo tipo2 = getTipo(ctx.expr(1));
        if (ctx.op.getText().equals("+")) {
            if (tipo1 == Tipo.STRING || tipo2 == Tipo.STRING) {
                types.put(ctx, Tipo.STRING);
            } else if (tipo1 == Tipo.REAL || tipo2 == Tipo.REAL) {
                types.put(ctx, Tipo.REAL);
            } else if (tipo1 == Tipo.INT && tipo2 == Tipo.INT) {
                types.put(ctx, Tipo.INT);
            } else {
                types.put(ctx, Tipo.ERRO);
            }
        } else { // '-'
            if (tipo1 == Tipo.REAL || tipo2 == Tipo.REAL) {
                types.put(ctx, Tipo.REAL);
            } else if (tipo1 == Tipo.INT && tipo2 == Tipo.INT) {
                types.put(ctx, Tipo.INT);
            } else {
                types.put(ctx, Tipo.ERRO);
            }
        }
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
        if (types.containsKey(node)) {
            return types.get(node);
        }
        String text = node.getText();
        if (text.equals("verdadeiro") || text.equals("falso")) {
            return Tipo.BOOL;
        }
        if (text.matches("-?\\d+")) {
            return Tipo.INT;
        }
        if (text.matches("-?\\d+\\.\\d+")) {
            return Tipo.REAL;
        }
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return Tipo.STRING;
        }
        return Tipo.VOID;
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

    public static Tipo getTipo(String tipo) {
        return switch (tipo) {
            case "inteiro" -> Tipo.INT;
            case "real" -> Tipo.REAL;
            case "string" -> Tipo.STRING;
            case "booleano" -> Tipo.BOOL;
            case "vazio" -> Tipo.VOID;
            default -> Tipo.VOID;
        };
    }

    public int getAddr() {
        return addr;
    }
}
