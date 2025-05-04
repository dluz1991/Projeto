package CodeGenerator;

import TabelaSimbolos.TabelaSimbolos;
import Tuga.*;
import org.antlr.v4.runtime.tree.ParseTree;
import TabelaSimbolos.*;

import java.util.*;

public class TypeChecker extends TugaBaseVisitor<Void> {
    private TabelaSimbolos tabelaSimbolos;
    private final Enquadramento scope;
    private final Map<ParseTree, Tipo> types = new HashMap<>();
    private final Map<String, List<String>> functionParams = new HashMap<>();
    private int typeErrorCount = 0;
    private int addr = 0;
    private Tipo currentFunctionType = Tipo.VOID;
    private String currentFunction = "";
    private boolean hasReturn = false;

    public TypeChecker(TabelaSimbolos tabelaSimbolos, Enquadramento scope) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.scope = scope;
    }

    @Override
    public Void visitProg(TugaParser.ProgContext ctx) {
        // Pass 1: Register all function signatures first
        for (var funcDecl : ctx.functionDecl()) {
            String nome = funcDecl.ID().getText();

            // Determine return type
            Tipo tipoRet = Tipo.VOID;
            if (funcDecl.TYPE() != null) {
                tipoRet = switch (funcDecl.TYPE().getText()) {
                    case "inteiro" -> Tipo.INT;
                    case "real" -> Tipo.REAL;
                    case "string" -> Tipo.STRING;
                    case "booleano" -> Tipo.BOOL;
                    default -> Tipo.ERRO;
                };
            }

            // Collect parameter types and names
            List<Tipo> tiposArgs = new ArrayList<>();
            List<String> paramNames = new ArrayList<>();
            if (funcDecl.formalParameters() != null) {
                for (var param : funcDecl.formalParameters().formalParameter()) {
                    Tipo tipo = switch (param.TYPE().getText()) {
                        case "inteiro" -> Tipo.INT;
                        case "real" -> Tipo.REAL;
                        case "string" -> Tipo.STRING;
                        case "booleano" -> Tipo.BOOL;
                        default -> Tipo.ERRO;
                    };
                    tiposArgs.add(tipo);
                    paramNames.add(param.ID().getText());
                }
            }

            int escopoFunc = scope.get(nome);
            functionParams.put(nome, paramNames);

            if (tabelaSimbolos.existeFunc(nome)) {
                System.err.printf("erro na linha %d: '%s' ja foi declarado%n",
                        funcDecl.start.getLine(), nome);
                typeErrorCount++;
            } else {
                tabelaSimbolos.putFuncao(nome, tipoRet, tiposArgs, escopoFunc);
            }
        }

        // Pass 2: Process global variables
        for (var decl : ctx.varDeclaration()) {
            visit(decl);
        }

        // Pass 3: Check function bodies
        for (var funcDecl : ctx.functionDecl()) {
            visit(funcDecl);
        }

        // Pass 4: Verify principal() function exists
        if (!tabelaSimbolos.existeFunc("principal")) {
            System.err.printf("erro na linha %d: falta funcao principal()%n",
                    ctx.stop.getLine());
            typeErrorCount++;
        }

        return null;
    }

    @Override
    public Void visitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        currentFunction = nome;

        // Set return type
        if (ctx.TYPE() != null) {
            currentFunctionType = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        } else {
            currentFunctionType = Tipo.VOID;
        }

        hasReturn = false;

        // Create local scope and add parameters
        TabelaSimbolos localScope = new TabelaSimbolos(tabelaSimbolos);
        int paramIndex = -1; // Parameters have negative indices, starting from -1

        // Register parameters in local scope
        if (ctx.formalParameters() != null) {
            List<String> paramNames = functionParams.get(nome);
            List<Tipo> paramTypes = tabelaSimbolos.getFuncao(nome).getTiposArgumentos();

            for (int i = 0; i < ctx.formalParameters().formalParameter().size(); i++) {
                var param = ctx.formalParameters().formalParameter(i);
                String paramName = param.ID().getText();
                Tipo paramType = switch (param.TYPE().getText()) {
                    case "inteiro" -> Tipo.INT;
                    case "real" -> Tipo.REAL;
                    case "string" -> Tipo.STRING;
                    case "booleano" -> Tipo.BOOL;
                    default -> Tipo.ERRO;
                };

                // Check for duplicate parameters
                if (i > 0 && paramNames.subList(0, i).contains(paramName)) {
                    System.err.printf("erro na linha %d: parametro '%s' ja foi declarado%n",
                            param.start.getLine(), paramName);
                    typeErrorCount++;
                } else {
                    // Add to local scope with negative index
                    localScope.putVariavel(paramName, paramType, paramIndex--, scope.get(paramName));
                }
            }
        }

        // Reset local variable address counter
        int savedAddr = addr;
        addr = 0;

        // Check function body
        TypeChecker localChecker = new TypeChecker(localScope, scope);
        localChecker.currentFunction = nome;
        localChecker.currentFunctionType = currentFunctionType;
        localChecker.visit(ctx.bloco());

        // Check if non-void function has a return statement
        if (currentFunctionType != Tipo.VOID && !localChecker.hasReturn) {
            System.err.printf("erro na linha %d: funcao '%s' do tipo %s deve ter retorno%n",
                    ctx.start.getLine(), nome, getTipoTexto(currentFunctionType));
            typeErrorCount++;
        }

        // Add errors from function body checking
        typeErrorCount += localChecker.typeErrorCount;
        types.putAll(localChecker.types);

        // Restore address counter
        addr = savedAddr;
        currentFunction = "";
        currentFunctionType = Tipo.VOID;

        return null;
    }

    @Override
    public Void visitVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        Tipo tipo = Tipo.INT; // Default type is INT
        if (ctx.TYPE() != null) {
            tipo = switch (ctx.TYPE().getText()) {
                case "inteiro" -> Tipo.INT;
                case "real" -> Tipo.REAL;
                case "string" -> Tipo.STRING;
                case "booleano" -> Tipo.BOOL;
                default -> Tipo.ERRO;
            };
        }

        for (var idToken : ctx.ID()) {
            String nome = idToken.getText();
            int escopoVar = scope.get(nome);

            // Check if variable already exists in this scope
            boolean existe = (tabelaSimbolos.existeVar(nome) && tabelaSimbolos.scopeVar(nome) == escopoVar) ||
                    (tabelaSimbolos.existeFunc(nome) && tabelaSimbolos.scopeFunc(nome) == escopoVar);

            if (!existe) {
                tabelaSimbolos.putVariavel(nome, tipo, addr++, escopoVar);
            } else {
                System.err.printf("erro na linha %d: '%s' ja foi declarado%n",
                        idToken.getSymbol().getLine(), nome);
                typeErrorCount++;
            }
        }

        return null;
    }

    @Override
    public Void visitVar(TugaParser.VarContext ctx) {
        String nome = ctx.ID().getText();

        VarSimbolo var = tabelaSimbolos.getVar(nome);
        if (var == null) {
            System.err.printf("erro na linha %d: '%s' nao foi declarado%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        } else {
            types.put(ctx, var.getTipo());
        }
        return null;
    }

    @Override
    public Void visitAfetacao(TugaParser.AfetacaoContext ctx) {
        visit(ctx.expr());

        String nome = ctx.ID().getText();
        VarSimbolo var = tabelaSimbolos.getVar(nome);

        if (var == null) {
            System.err.printf("erro na linha %d: '%s' nao foi declarado%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            return null;
        }

        Tipo tipoExpr = getTipo(ctx.expr());
        Tipo tipoVar = var.getTipo();

        if (tipoExpr != Tipo.ERRO && tipoVar != tipoExpr &&
                !(tipoVar == Tipo.REAL && tipoExpr == Tipo.INT)) {
            System.err.printf("erro na linha %d: operador '<-' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), getTipoTexto(tipoVar), getTipoTexto(tipoExpr));
            typeErrorCount++;
        }
        return null;
    }

    @Override
    public Void visitChamadaFuncaoExpr(TugaParser.ChamadaFuncaoExprContext ctx) {
        String nome = ctx.ID().getText();
        FuncaoSimbolo func = tabelaSimbolos.getFuncao(nome);

        if (func == null) {
            System.err.printf("erro na linha %d: '%s' nao foi declarado%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
            return null;
        }

        // Function used as expression must return a value
        if (func.getTipoRetorno() == Tipo.VOID) {
            System.err.printf("erro na linha %d: operador '<-' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), "variável", "vazio");
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
            return null;
        }

        // Check arguments
        List<Tipo> esperados = func.getTiposArgumentos();
        List<Tipo> reais = new ArrayList<>();

        if (ctx.exprList() != null) {
            for (var expr : ctx.exprList().expr()) {
                visit(expr);
                reais.add(getTipo(expr));
            }
        }

        // Check argument count
        if (esperados.size() != reais.size()) {
            System.err.printf("erro na linha %d: '%s' requer %d argumentos%n",
                    ctx.start.getLine(), nome, esperados.size());
            typeErrorCount++;
        } else {
            // Check argument types
            for (int i = 0; i < esperados.size(); i++) {
                Tipo esperado = esperados.get(i);
                Tipo real = reais.get(i);

                if (real != Tipo.ERRO && esperado != real &&
                        !(esperado == Tipo.REAL && real == Tipo.INT)) {
                    System.err.printf("erro na linha %d: argumento %d de '%s' devia ser do tipo %s%n",
                            ctx.start.getLine(), i+1, nome, getTipoTexto(esperado));
                    typeErrorCount++;
                }
            }
        }

        types.put(ctx, func.getTipoRetorno());
        return null;
    }

    @Override
    public Void visitChamadaFuncao(TugaParser.ChamadaFuncaoContext ctx) {
        String nome = ctx.ID().getText();
        FuncaoSimbolo func = tabelaSimbolos.getFuncao(nome);

        if (func == null) {
            System.err.printf("erro na linha %d: '%s' nao foi declarado%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            return null;
        }

        // Function used as statement must be void
        if (func.getTipoRetorno() != Tipo.VOID) {
            System.err.printf("erro na linha %d: valor de '%s' tem de ser atribuido a uma variavel%n",
                    ctx.start.getLine(), nome);
            typeErrorCount++;
            return null;
        }

        // Check arguments
        int expectArgs = func.getTiposArgumentos().size();
        int hasArgs = ctx.expr() != null ? 1 : 0;

        if (expectArgs != hasArgs) {
            System.err.printf("erro na linha %d: '%s' requer %d argumentos%n",
                    ctx.start.getLine(), nome, expectArgs);
            typeErrorCount++;
        } else if (hasArgs == 1) {
            // Check argument type
            visit(ctx.expr());
            Tipo argTipo = getTipo(ctx.expr());
            Tipo expectedTipo = func.getTiposArgumentos().get(0);

            if (argTipo != Tipo.ERRO && expectedTipo != argTipo &&
                    !(expectedTipo == Tipo.REAL && argTipo == Tipo.INT)) {
                System.err.printf("erro na linha %d: '%s' devia ser do tipo %s%n",
                        ctx.start.getLine(), "argumento", getTipoTexto(expectedTipo));
                typeErrorCount++;
            }
        }

        return null;
    }

    @Override
    public Void visitRetorna(TugaParser.RetornaContext ctx) {
        hasReturn = true;

        if (ctx.expr() != null) {
            visit(ctx.expr());
            Tipo tipoExpr = getTipo(ctx.expr());

            if (currentFunctionType == Tipo.VOID) {
                System.err.printf("erro na linha %d: funcao do tipo void nao pode retornar valor%n",
                        ctx.start.getLine());
                typeErrorCount++;
            }
            else if (tipoExpr != Tipo.ERRO && currentFunctionType != tipoExpr &&
                    !(currentFunctionType == Tipo.REAL && tipoExpr == Tipo.INT)) {
                System.err.printf("erro na linha %d: retorna %s, esperado %s%n",
                        ctx.start.getLine(), getTipoTexto(tipoExpr), getTipoTexto(currentFunctionType));
                typeErrorCount++;
            }
        }
        else if (currentFunctionType != Tipo.VOID) {
            System.err.printf("erro na linha %d: funcao deve retornar valor do tipo %s%n",
                    ctx.start.getLine(), getTipoTexto(currentFunctionType));
            typeErrorCount++;
        }

        return null;
    }

    @Override
    public Void visitBloco(TugaParser.BlocoContext ctx) {
        // Visit variable declarations
        for (var varDecl : ctx.varDeclaration()) {
            visit(varDecl);
        }

        // Visit statements
        for (var stmt : ctx.stat()) {
            visit(stmt);

            // Check if this statement introduces a return
            if (stmt instanceof TugaParser.RetornaContext) {
                hasReturn = true;
            }
        }

        return null;
    }

    @Override
    public Void visitEquanto(TugaParser.EquantoContext ctx) {
        visit(ctx.expr());
        Tipo tipoCond = getTipo(ctx.expr());

        if (tipoCond != Tipo.BOOL && tipoCond != Tipo.ERRO) {
            System.err.printf("erro na linha %d: expressao de 'enquanto' deve ser do tipo booleano%n",
                    ctx.start.getLine());
            typeErrorCount++;
        }

        visit(ctx.stat());
        return null;
    }

    @Override
    public Void visitSe(TugaParser.SeContext ctx) {
        visit(ctx.expr());
        Tipo tipoCond = getTipo(ctx.expr());

        if (tipoCond != Tipo.BOOL && tipoCond != Tipo.ERRO) {
            System.err.printf("erro na linha %d: expressao de 'se' deve ser do tipo booleano%n",
                    ctx.start.getLine());
            typeErrorCount++;
        }

        visit(ctx.stat(0));
        if (ctx.stat().size() > 1) {
            visit(ctx.stat(1));
        }

        return null;
    }

    @Override
    public Void visitEscreve(TugaParser.EscreveContext ctx) {
        visit(ctx.expr());
        if (getTipo(ctx.expr()) == Tipo.ERRO) {
            typeErrorCount++;
        }
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
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));

        if ((t1 == Tipo.BOOL || t1 == Tipo.ERRO) && (t2 == Tipo.BOOL || t2 == Tipo.ERRO)) {
            types.put(ctx, Tipo.BOOL);
        } else {
            System.err.printf("erro na linha %d: operador 'ou' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), getTipoTexto(t1), getTipoTexto(t2));
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        }

        return null;
    }

    @Override
    public Void visitAnd(TugaParser.AndContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));

        if ((t1 == Tipo.BOOL || t1 == Tipo.ERRO) && (t2 == Tipo.BOOL || t2 == Tipo.ERRO)) {
            types.put(ctx, Tipo.BOOL);
        } else {
            System.err.printf("erro na linha %d: operador 'e' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), getTipoTexto(t1), getTipoTexto(t2));
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        }

        return null;
    }

    @Override
    public Void visitAddSub(TugaParser.AddSubContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));
        String op = ctx.op.getText();

        // String concatenation with +
        if (op.equals("+") && (t1 == Tipo.STRING || t2 == Tipo.STRING)) {
            types.put(ctx, Tipo.STRING);
        }
        // Numeric operations
        else if ((t1 == Tipo.INT || t1 == Tipo.REAL || t1 == Tipo.ERRO) &&
                (t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.ERRO)) {
            if (t1 == Tipo.REAL || t2 == Tipo.REAL) {
                types.put(ctx, Tipo.REAL);
            } else {
                types.put(ctx, Tipo.INT);
            }
        } else {
            System.err.printf("erro na linha %d: operador '%s' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), op, getTipoTexto(t1), getTipoTexto(t2));
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        }

        return null;
    }

    @Override
    public Void visitMulDiv(TugaParser.MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));
        String op = ctx.op.getText();

        if ((t1 == Tipo.INT || t1 == Tipo.REAL || t1 == Tipo.ERRO) &&
                (t2 == Tipo.INT || t2 == Tipo.REAL || t2 == Tipo.ERRO)) {

            // % only works with integers
            if (op.equals("%") && (t1 == Tipo.REAL || t2 == Tipo.REAL)) {
                System.err.printf("erro na linha %d: operador '%%' requer operandos inteiros%n",
                        ctx.start.getLine());
                typeErrorCount++;
                types.put(ctx, Tipo.ERRO);
            } else if (t1 == Tipo.REAL || t2 == Tipo.REAL) {
                types.put(ctx, Tipo.REAL);
            } else {
                types.put(ctx, Tipo.INT);
            }
        } else {
            System.err.printf("erro na linha %d: operador '%s' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), op, getTipoTexto(t1), getTipoTexto(t2));
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        }

        return null;
    }

    @Override
    public Void visitRelational(TugaParser.RelationalContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        Tipo t1 = getTipo(ctx.expr(0));
        Tipo t2 = getTipo(ctx.expr(1));
        String op = ctx.op.getText();

        // Equal/not equal work for all same types
        if ((op.equals("igual") || op.equals("diferente")) &&
                (t1 == t2 || (t1 == Tipo.INT && t2 == Tipo.REAL) || (t1 == Tipo.REAL && t2 == Tipo.INT))) {
            types.put(ctx, Tipo.BOOL);
        }
        // Comparison operators only work for numeric types
        else if ((op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")) &&
                (t1 == Tipo.INT || t1 == Tipo.REAL) && (t2 == Tipo.INT || t2 == Tipo.REAL)) {
            types.put(ctx, Tipo.BOOL);
        } else {
            System.err.printf("erro na linha %d: operador '%s' eh invalido entre %s e %s%n",
                    ctx.start.getLine(), op, getTipoTexto(t1), getTipoTexto(t2));
            typeErrorCount++;
            types.put(ctx, Tipo.ERRO);
        }

        return null;
    }

    @Override
    public Void visitUnary(TugaParser.UnaryContext ctx) {
        visit(ctx.expr());
        Tipo tipoExpr = getTipo(ctx.expr());
        String op = ctx.op.getText();

        if (op.equals("-") && (tipoExpr == Tipo.INT || tipoExpr == Tipo.REAL || tipoExpr == Tipo.ERRO)) {
            types.put(ctx, tipoExpr);
        } else if (op.equals("nao") && (tipoExpr == Tipo.BOOL || tipoExpr == Tipo.ERRO)) {
            types.put(ctx, Tipo.BOOL);
        } else {
            System.err.printf("erro na linha %d: operador '%s' eh invalido para %s%n",
                    ctx.start.getLine(), op, getTipoTexto(tipoExpr));
            typeErrorCount++;
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

    public int getAddr() {
        return addr;
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
}