import Tuga.*;
import TabelaSimbolos.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class DefPhase extends TugaBaseListener {
    private final Enquadramento enquandramento;
    private int scopeLevel = 0;
    private int currentScope = 0;
    private final Stack<Integer> scopeStack = new Stack<>();
    private final Map<Integer, Integer> parentMap = new HashMap<>();

    public DefPhase(Enquadramento scope) {
        this.enquandramento = scope;
        // Escopo global E0
        scopeStack.push(0);
    }

    @Override
    public void enterProg(TugaParser.ProgContext ctx) {
        currentScope = 0;
    }

    @Override
    public void enterFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        scopeLevel++;
        int novoScope = scopeLevel;
        parentMap.put(novoScope, currentScope);
        currentScope = novoScope;
        scopeStack.push(currentScope);
        enquandramento.put(nome, currentScope);
    }

    @Override
    public void exitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    @Override
    public void enterBloco(TugaParser.BlocoContext ctx) {
        scopeLevel++;
        int novoScope = scopeLevel;
        parentMap.put(novoScope, currentScope);
        currentScope = novoScope;
        scopeStack.push(currentScope);
    }

    @Override
    public void exitBloco(TugaParser.BlocoContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    @Override
    public void enterFormalParameter(TugaParser.FormalParameterContext ctx) {
        String nome = ctx.ID().getText();
        if (enquandramento.contains(nome) && enquandramento.get(nome) == currentScope) {
            System.out.println("Erro: parâmetro '" + nome + "' já declarado no mesmo scope");
        } else {
            enquandramento.put(nome, currentScope);
        }
    }

    @Override
    public void enterVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        for (var id : ctx.ID()) {
            String nome = id.getText();
            if (enquandramento.contains(nome) && enquandramento.get(nome) == currentScope) {
                System.out.println("Erro: variável '" + nome + "' já declarada no mesmo scope");
            } else {
                enquandramento.put(nome, currentScope);
            }
        }
    }

    public void printScopes() {
        System.out.println("--- Tabela de Identificadores ---");
        enquandramento.print();

        System.out.println("--- Hierarquia de Scopes ---");
        for (var entry : parentMap.entrySet()) {
            System.out.printf("Scope %d -> pai: %s%n", entry.getKey(),
                    entry.getValue() == null ? "nenhum" : entry.getValue());
        }
    }
}
