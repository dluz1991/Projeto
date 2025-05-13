import Tuga.*;
import TabelaSimbolos.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DefPhase extends TugaBaseListener {
    private final Enquadramento enquadramento;
    private int scopeLevel = 0;
    private int currentScope = 0;
    private final Stack<Integer> scopeStack = new Stack<>();
    private final Map<Integer, Integer> parentMap = new HashMap<>();

    public DefPhase(Enquadramento scope) {
        this.enquadramento = scope;
        // Escopo global E0
        scopeStack.push(0);
    }

    @Override
    public void enterProg(TugaParser.ProgContext ctx) {
        currentScope = 0;  // O escopo global
    }

    @Override
    public void enterFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        scopeLevel++;  // Aumentar o nível do escopo
        int novoScope = scopeLevel;
        parentMap.put(novoScope, currentScope);
        currentScope = novoScope;
        scopeStack.push(currentScope);

        // Adicionar a função ao escopo da função
        enquadramento.put(nome, currentScope);

        // Adicionar os parâmetros ao escopo da função (não ao escopo do bloco)
        if (ctx.formalParameters() != null) {
            for (var param : ctx.formalParameters().formalParameter()) {
                String paramName = param.ID().getText();
                if (enquadramento.contains(paramName) && enquadramento.get(paramName) == currentScope) {
                    System.out.println("Erro: parâmetro '" + paramName + "' já declarado no mesmo scope");
                } else {
                    enquadramento.put(paramName, currentScope);  // Parâmetro associado ao escopo da função
                }
            }
        }
    }

    @Override
    public void exitFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();  // Voltar ao escopo anterior
    }

    @Override
    public void enterBloco(TugaParser.BlocoContext ctx) {
        scopeLevel++;  // Aumentar o nível do escopo para o bloco
        int novoScope = scopeLevel;
        parentMap.put(novoScope, currentScope);
        currentScope = novoScope;
        scopeStack.push(currentScope);

        // Declarar variáveis dentro do bloco da função (não os parâmetros)
        for (var varDecl : ctx.varDeclaration()) {
            for (var id : varDecl.ID()) {
                String nome = id.getText();
                // Não adicionamos novamente os parâmetros ao escopo do bloco
                if (enquadramento.contains(nome) && enquadramento.get(nome) == currentScope) {
                    System.out.println("Erro: variável '" + nome + "' já declarada no mesmo scope");
                } else {
                    enquadramento.put(nome, currentScope);  // A variável vai para o escopo do bloco
                }
            }
        }
    }

    @Override
    public void exitBloco(TugaParser.BlocoContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();  // Voltar ao escopo anterior
    }

    @Override
    public void enterFormalParameter(TugaParser.FormalParameterContext ctx) {
        String nome = ctx.ID().getText();
        // Parâmetros já são adicionados ao escopo da função em enterFunctionDecl
        if (enquadramento.contains(nome) && enquadramento.get(nome) == currentScope) {
            System.out.println("Erro: parâmetro '" + nome + "' já declarado no mesmo scope");
        } else {
            enquadramento.put(nome, currentScope);  // Parâmetro associado ao escopo da função
        }
    }

    @Override
    public void enterVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        for (var id : ctx.ID()) {
            String nome = id.getText();
            // Não precisamos verificar os parâmetros aqui, pois já foram verificados em enterFormalParameter
            if (enquadramento.contains(nome) && enquadramento.get(nome) == currentScope) {
                System.out.println("Erro: variável '" + nome + "' já declarada no mesmo scope");
            } else {
                enquadramento.put(nome, currentScope);  // A variável vai para o escopo do bloco
            }
        }
    }

    public void printScopes() {
        System.out.println("--- Tabela de Identificadores ---");
        enquadramento.print();

        System.out.println("--- Hierarquia de Scopes ---");
        for (var entry : parentMap.entrySet()) {
            System.out.printf("Scope %d -> pai: %s%n", entry.getKey(),
                    entry.getValue() == null ? "nenhum" : entry.getValue());
        }
    }
}
