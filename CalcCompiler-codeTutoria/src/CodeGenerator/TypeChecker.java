package CodeGenerator;

import TabelaSimbolos.TabelaSimbolos;
import Tuga.*;
import org.antlr.v4.runtime.tree.ParseTree;
import TabelaSimbolos.*;

import java.util.*;

public class TypeChecker extends TugaBaseVisitor<Void> {
    TabelaSimbolos tabelaSimbolos;
    private final Enquadramento scope;
    private final Map<ParseTree, Tipo> types = new HashMap<>();
    private int typeErrorCount = 0;
    private int addr = 0;
    private Tipo tipoRetornoAtual = Tipo.VOID;

    public TypeChecker(TabelaSimbolos tabelaSimbolos, Enquadramento scope) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.scope = scope;
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
         //1.º PASSO - inserir *assinaturas* de todas as funções
        for (var funcDecl : ctx.functionDecl()) {

            String nome = funcDecl.ID().getText();

            /*-- tipo de retorno --*/
            Tipo tipoRet = Tipo.VOID;
            if (funcDecl.TYPE() != null) {
                tipoRet = switch (funcDecl.TYPE().getText()) {
                    case "inteiro"   -> Tipo.INT;
                    case "real"      -> Tipo.REAL;
                    case "string"    -> Tipo.STRING;
                    case "booleano"  -> Tipo.BOOL;
                    default          -> Tipo.ERRO;
                };
            }

            /*-- lista de tipos dos argumentos --*/
            List<Tipo> tiposArgs = new ArrayList<>();
            if (funcDecl.formalParameters() != null) {
                for (var p : funcDecl.formalParameters().formalParameter()) {
                    Tipo t = switch (p.TYPE().getText()) {
                        case "inteiro"   -> Tipo.INT;
                        case "real"      -> Tipo.REAL;
                        case "string"    -> Tipo.STRING;
                        case "booleano"  -> Tipo.BOOL;
                        default          -> Tipo.ERRO;
                    };
                    tiposArgs.add(t);
                }
            }

            int escopoFunc = scope.get(nome);

            if (tabelaSimbolos.existeFunc(nome)) {
                System.err.printf("erro: funcao '%s' duplicada%n", nome);
                typeErrorCount++;
            } else {
                tabelaSimbolos.putFuncao(nome, tipoRet, tiposArgs, escopoFunc);
            }
        }


         //2.º PASSO - variáveis globais

        for (var decl : ctx.varDeclaration())
            visit(decl);

        // 3.º PASSO - visitar corpo de cada função
        for (var funcDecl : ctx.functionDecl())
            visit(funcDecl);
         // 4.º PASSO - garantir existência de principal()
        if (!tabelaSimbolos.existeFunc("principal")) {
            System.err.printf("erro na linha %d: falta funcao principal()%n",
                    ctx.start.getLine());
            typeErrorCount++;
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {

        /*  tipo explícito ou INT por omissão  */
        Tipo tipo = Tipo.INT;
        if (ctx.TYPE() != null) {
            tipo = switch (ctx.TYPE().getText()) {
                case "inteiro"   -> Tipo.INT;
                case "real"      -> Tipo.REAL;
                case "string"    -> Tipo.STRING;
                case "booleano"  -> Tipo.BOOL;
                default          -> Tipo.ERRO;
            };
        }

        /*  cada identificador da lista  */
        for (var idTok : ctx.ID()) {
            String nome      = idTok.getText();
            int    escopoVar = scope.get(nome);

            /*  já existe símbolo com o mesmo nome neste escopo?  */
            boolean existe =
                    (tabelaSimbolos.existeVar(nome)  && tabelaSimbolos.scopeVar (nome)==escopoVar) ||
                            (tabelaSimbolos.existeFunc(nome) && tabelaSimbolos.scopeFunc(nome)==escopoVar);

            if (!existe) {
                tabelaSimbolos.putVariavel(nome, tipo, addr++, escopoVar);
            } else {
                System.err.printf("erro na linha %d: simbolo '%s' ja existe no mesmo escopo%n",
                        idTok.getSymbol().getLine(), nome);
                typeErrorCount++;
            }
        }
        return null;
    }


    @Override public Void visitVar(TugaParser.VarContext ctx){
        String nome = ctx.ID().getText();

        VarSimbolo var = tabelaSimbolos.getVar(nome);   // <-- agora assim
        if (var == null){
            System.err.printf("erro na linha %d: variavel '%s' nao foi declarada%n",
                    ctx.start.getLine(),nome);
            typeErrorCount++;
            types.put(ctx,Tipo.ERRO);
        }else{
            types.put(ctx,var.getTipo());
        }
        return null;
    }
//---  FIM ALTERAÇÃO 2  -------------------------------

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();

        // Tipo de retorno
        Tipo tipoRetorno = Tipo.VOID;
        if (ctx.TYPE() != null) {
            tipoRetorno = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        tipoRetornoAtual = tipoRetorno;

        // Novo escopo local
        TabelaSimbolos escopoLocal = new TabelaSimbolos(tabelaSimbolos);
        int localAddr = 0;
        Set<String> nomes = new HashSet<>();
        List<Tipo> tiposArgs = new ArrayList<>();

        if (ctx.formalParameters() != null) {
            var parametros = ctx.formalParameters().formalParameter();
            for (int i = 0; i < parametros.size(); i++) {
                String id = parametros.get(i).ID().getText();
                Tipo tipo = switch (parametros.get(i).TYPE().getText()) {
                    case "inteiro" -> Tipo.INT;
                    case "real" -> Tipo.REAL;
                    case "string" -> Tipo.STRING;
                    case "booleano" -> Tipo.BOOL;
                    default -> Tipo.ERRO;
                };
                tiposArgs.add(tipo);

                if (nomes.contains(id)) {
                    System.err.printf("erro na linha %d: parâmetro '%s' duplicado%n", parametros.get(i).start.getLine(), id);
                    typeErrorCount++;
                    continue;
                }

                nomes.add(id);
                escopoLocal.putVariavel(id, tipo, localAddr++, scope.get(id));
            }
        }

        // Verificar corpo da função com um novo TypeChecker
        TypeChecker checkerInterno = new TypeChecker(escopoLocal, scope);
        checkerInterno.tipoRetornoAtual = tipoRetorno;
        checkerInterno.addr = localAddr;
        checkerInterno.visit(ctx.bloco());

        typeErrorCount += checkerInterno.getTypeErrorCount();
        types.putAll(checkerInterno.getTypes());

        return null;
    }




    /*------------------------------------------------------------------
     *  visitChamadaFuncaoExpr
     *------------------------------------------------------------------*/
    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {

        String nome = ctx.ID().getText();
        FuncaoSimbolo func = tabelaSimbolos.getFuncao(nome);

        /*  símbolo não é função?  */
        if (func == null) {
            System.err.printf("erro na linha %d: '%s' nao e uma função%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
            return null;
        }

        /*  tipos esperados vs. reais dos argumentos  */
        List<Tipo> esperados = func.getTiposArgumentos();
        List<Tipo> reais     = new ArrayList<>();

        if (ctx.exprList() != null) {
            for (var e : ctx.exprList().expr()) {
                visit(e);
                reais.add(getTipo(e));
            }
        }

        /*  nº de argumentos  */
        if (esperados.size() != reais.size()) {
            System.err.printf("erro na linha %d: número de argumentos inválido para '%s'%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
        } else {
            /*  verificação tipo-a-tipo  */
            for (int i = 0; i < esperados.size(); i++) {
                Tipo exp = esperados.get(i);
                Tipo rel = reais.get(i);
                boolean ok = (exp == rel) || (exp == Tipo.REAL && rel == Tipo.INT);
                if (!ok) {
                    System.err.printf("erro na linha %d: argumento %d tem tipo inválido para '%s'%n",
                            ctx.start.getLine(), i + 1, nome);
                    typeErrorCount++;
                }
            }
        }

        /*  tipo da expressão é o tipo de retorno da função  */
        types.put(ctx, func.getTipoRetorno());
        return null;
    }


    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        if (ctx.expr() != null) {
            visit(ctx.expr());
            Tipo tipoExpr = getTipo(ctx.expr());
            if (tipoRetornoAtual != tipoExpr && !(tipoRetornoAtual == Tipo.REAL && tipoExpr == Tipo.INT)) {
                System.err.printf("erro na linha %d: tipo de retorno incompatível, esperado %s mas encontrado %s%n", ctx.start.getLine(), getTipoTexto(tipoRetornoAtual), getTipoTexto(tipoExpr));
                typeErrorCount++;
            }
        } else {
            if (tipoRetornoAtual != Tipo.VOID) {
                System.err.printf("erro na linha %d: esperado valor de retorno do tipo %s%n", ctx.start.getLine(), getTipoTexto(tipoRetornoAtual));
                typeErrorCount++;
            }
        }
        return null;
    }

    @Override public Void visitAfetacao(TugaParser.AfetacaoContext ctx){
        visit(ctx.expr());

        String nome = ctx.ID().getText();
        VarSimbolo var = tabelaSimbolos.getVar(nome);      // <--

        if (var == null){
            System.err.printf("erro na linha %d: variavel '%s' nao foi declarada%n",
                    ctx.start.getLine(),nome);
            typeErrorCount++;
            return null;
        }

        Tipo tipoExpr = getTipo(ctx.expr());
        Tipo tipoVar  = var.getTipo();

        if (tipoVar!=tipoExpr && !(tipoVar==Tipo.REAL && tipoExpr==Tipo.INT)){
            System.err.printf("erro na linha %d: operador '<-' incompatível entre %s e %s%n",
                    ctx.start.getLine(),tipoVar,tipoExpr);
            typeErrorCount++;
        }
        return null;
    }
//---  FIM ALTERAÇÃO 3  -------------------------------


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

    public int getAddr() {
        return addr;
    }
}
