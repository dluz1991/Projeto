package TabelaSimbolos;

import CodeGenerator.Tipo;

public class ValorSimbolo {
    private final Tipo tipo;
    private final int index;

    public ValorSimbolo(Tipo tipo, int endereco) {
        this.tipo = tipo;
        this.index = endereco;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getIndex() {
        return index;
    }
}
