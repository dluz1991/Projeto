package TabelaSimbolos;

import CodeGenerator.*;

import java.util.HashMap;

public class Enquadramento {
    private HashMap<String, Tipo> scope;
    private Enquadramento parent;
    private int currentScope;

    public Enquadramento(Enquadramento parent, int scopeNumber) {
        this.parent = parent;
        this.currentScope = scopeNumber;
        this.scope = new HashMap<>();
    }

    public void put(String nome, Tipo tipo) {
        scope.put(nome, tipo);
    }

    public Tipo get(String nome) {
        Tipo tipo = scope.get(nome);
        if (tipo == null && parent != null) {
            return parent.get(nome);
        }
        return tipo;
    }

    public boolean contains(String nome) {
        return scope.containsKey(nome) || (parent != null && parent.contains(nome));
    }

    public int getCurrentScope() { 
        return currentScope; 
    }

    public void remove(String nome) {
        scope.remove(nome);
    }

    public HashMap<String, Tipo> getScope() {
        return scope;
    }

    public Enquadramento getParent() {
        return parent;
    }

    public void print() {
        System.out.println("Scope " + currentScope + ":");
        for (String key : scope.keySet()) {
            System.out.println("  " + key + " : " + scope.get(key));
        }
        if (parent != null) {
            parent.print();
        }
    }
}
