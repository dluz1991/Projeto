import CodeGenerator.Tipo;
import CodeGenerator.TypeChecker;
import Tuga.*;
import TabelaSimbolos.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.*;

public class DefPhase extends TugaBaseListener {
    ParseTreeProperty<Enquadramento> scopes = new ParseTreeProperty<Enquadramento>();
    Enquadramento global;
    Enquadramento currentScope;
    int lastScope = 0;
    Stack<Enquadramento> scopeStack = new Stack<>();

    FuncaoSimbolo funcaoAtual = null;

    boolean print = false; // Flag para imprimir informações de depuração

    public DefPhase(Enquadramento scope) {
        if (scope != null) {
            this.global = scope;
            this.currentScope = scope;
            this.lastScope = scope.getCurrentScope();
        }
    }

    @Override
    public void enterProg(TugaParser.ProgContext ctx) {
        if (global == null) {
            global = new Enquadramento(null, 0);
            currentScope = global;
            lastScope = 0;
        }
        scopeStack.push(currentScope);
    }

    @Override
    public void exitProg(TugaParser.ProgContext ctx) {
        // Verifica se a função principal "main" foi declarada
        scopeStack.pop();
    }

    @Override
    public void enterFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        String tipoString = ctx.TYPE() != null ? ctx.TYPE().getText() : "void";  // Verifica se o tipo é nulo
        Tipo tipo = TypeChecker.getTipo(tipoString);

        List<VarSimbolo> args = new ArrayList<>();
        if (ctx.formalParameters() != null) {  // Verifica se formalParameters() é não nulo
            for (TugaParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                String paramNome = param.ID().getText();
                Tipo paramTipo = TypeChecker.getTipo(param.TYPE().getText());
                currentScope.put(paramNome, paramTipo);
                args.add(new VarSimbolo(paramNome, paramTipo));
                if (print) {
                    System.out.println("Parâmetro: " + paramTipo + " " + paramNome + " no escopo " + currentScope.getCurrentScope());
                }
            }
        }

        FuncaoSimbolo funcao = new FuncaoSimbolo(nome, tipo, args, currentScope.getCurrentScope());
        funcaoAtual = funcao;

        // Se necessário, print para debug
        if (print) {
            String type=ctx.TYPE() != null ? ctx.TYPE().getText() : "void";
            System.out.println("enterFunction " + type + " " + funcao.getNome() + "(...) >> " + currentScope);
            currentScope.print();
        }
    }

    void saveScope(ParserRuleContext ctx, Enquadramento s) {
        scopes.put(ctx, s);
    }

    @Override
    public void enterBloco(TugaParser.BlocoContext ctx) {
        currentScope = new Enquadramento(currentScope, ++lastScope);
        saveScope(ctx, currentScope);
        scopeStack.push(currentScope);

        // Adiciona as variáveis da função no escopo
        if (funcaoAtual != null) {
            currentScope.put(funcaoAtual.getNome(), funcaoAtual.getTipoRetorno());
            for (var arg : funcaoAtual.getTiposArgumentos()) {
                currentScope.put(arg.getName(), arg.getTipo());
            }
        }

        if (print) {
            System.out.println("Entrando no escopo: " + currentScope.getCurrentScope());
        }
    }

    @Override
    public void exitBloco(TugaParser.BlocoContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();
        if (print) {
            System.out.println("Saindo do escopo: " + currentScope.getCurrentScope());
        }
    }

    @Override
    public void enterVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        String tipoString = ctx.TYPE().getText();
        Tipo tipo = TypeChecker.getTipo(tipoString);

        for (var id : ctx.ID()) {
            String nome = id.getText();
            // Verifica se já existe uma variável com o mesmo nome no escopo
            if (currentScope.contains(nome)) {
                // Caso seja necessário, tratar como erro (sem implementação extra de erro)
            } else {
                currentScope.put(nome, tipo);
            }
        }
        if (print) {
            System.out.println("Declaração de variável: " + tipo + " " + ctx.getText() + " no escopo " + currentScope.getCurrentScope());
        }
    }
    public ParseTreeProperty<Enquadramento> getScopes() {
        return scopes;
    }

}
