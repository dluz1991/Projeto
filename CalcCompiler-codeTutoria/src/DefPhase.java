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
    public void exitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        currentScope = scopeStack.pop();
        funcaoAtual = null;
    }
    @Override
    public void enterFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        // Cria novo escopo da função
        Enquadramento funcScope = new Enquadramento(currentScope, ++lastScope);
        scopeStack.push(currentScope); // guarda escopo anterior
        currentScope = funcScope;
        saveScope(ctx, currentScope); // associa função ao novo escopo

        // Extrai nome e tipo
        String nome = ctx.ID().getText();
        String tipoString = ctx.TYPE() != null ? ctx.TYPE().getText() : "void";
        Tipo tipo = TypeChecker.getTipo(tipoString);

        // Processa parâmetros
        List<VarSimbolo> args = new ArrayList<>();
        if (ctx.formalParameters() != null) {
            for (TugaParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                String paramNome = param.ID().getText();
                Tipo paramTipo = TypeChecker.getTipo(param.TYPE().getText());
                currentScope.put(paramNome, paramTipo); // INSERE no escopo da função
                args.add(new VarSimbolo(paramNome, paramTipo));
            }
        }

        funcaoAtual = new FuncaoSimbolo(nome, tipo, args, currentScope.getCurrentScope());

        if (print) {
            System.out.println("Função " + nome + " no escopo " + currentScope.getCurrentScope());
            currentScope.print();
        }
    }



    void saveScope(ParserRuleContext ctx, Enquadramento s) {
        //System.out.println("Salvando escopo: " + s.getCurrentScope() + " para " + ctx.getText());
        scopes.put(ctx, s);
    }

    @Override
    public void enterBloco(TugaParser.BlocoContext ctx) {
        if (ctx.getParent() instanceof TugaParser.FunctionDeclContext) {
            // Se o bloco é parte de uma função, não cria um novo escopo
            return;
        }
        currentScope = new Enquadramento(currentScope, ++lastScope);
        scopeStack.push(currentScope);
        saveScope(ctx, currentScope);

        if (print) {
            System.out.println("Entrando no escopo: " + currentScope.getCurrentScope());
        }
    }


    @Override
    public void exitBloco(TugaParser.BlocoContext ctx) {
        if (ctx.getParent() instanceof TugaParser.FunctionDeclContext) {
            // Se o bloco é parte de uma função, não cria um novo escopo
            return;
        }
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
        saveScope(ctx, currentScope);
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
