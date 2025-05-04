package TabelaSimbolos;

import CodeGenerator.Tipo;

public class VarSimbolo {
    private final Tipo tipo;
    private final int index;
    private final int scope;

    public VarSimbolo(Tipo tipo, int endereco, int scope) {
        this.tipo = tipo;
        this.index = endereco;
        this.scope = scope;
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
}
