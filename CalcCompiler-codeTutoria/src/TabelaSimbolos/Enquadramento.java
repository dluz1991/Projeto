package TabelaSimbolos;

import java.util.HashMap;

public class Enquadramento {
    private final HashMap<String, Integer> scope;
    private final HashMap<Integer, Integer> parentMap = new HashMap<>();
    private int currentScope = 0;

    public Enquadramento() {
        this.scope = new HashMap<>();
    }

    public void put(String nome, Integer valor) {
        scope.put(nome, valor);
    }

    public Integer get(String nome) {
        return scope.get(nome);
    }

    public boolean contains(String nome) {
        return scope.containsKey(nome);
    }

    public void remove(String nome) {
        scope.remove(nome);
    }

    public HashMap<String, Integer> getScope() {
        return scope;
    }

    public void print() {
        for (String key : scope.keySet()) {
            System.out.println("Nome: " + key + ", Enquadramento: " + scope.get(key));
        }
    }

    // Adicionado para herança entre escopos
    public void setPai(int filho, int pai) {
        parentMap.put(filho, pai);
    }

    public Integer getPai(int scopeId) {
        return parentMap.get(scopeId);
    }

    public void setCurrentScope(int scope) {
        currentScope = scope;
    }

    public int getCurrentScope() {
        return currentScope;
    }
}
