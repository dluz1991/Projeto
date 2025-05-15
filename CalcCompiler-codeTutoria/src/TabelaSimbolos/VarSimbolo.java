package TabelaSimbolos;

import CodeGenerator.Tipo;

public class VarSimbolo {
    private final Tipo tipo;
    private final int index;
    private final int scope;
    private final String name;

    public VarSimbolo(String nome, Tipo tipo, int endereco, int scope) {
        this.tipo = tipo;
        this.index = endereco;
        this.scope = scope;
        this.name = nome;
    }

    public VarSimbolo(String nome, Tipo tipo) {
        this.tipo = tipo;
        this.index = -1;
        this.scope = -1;
        this.name = nome;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getIndex() {
        return index;
    }

    public int getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }
    public String toString() {
        return "VarSimbolo{" +
                "tipo=" + tipo +
                ", index=" + index +
                ", scope=" + scope +
                ", name='" + name + '\'' +
                '}';
    }
}
