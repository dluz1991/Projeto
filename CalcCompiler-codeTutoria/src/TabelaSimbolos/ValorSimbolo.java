package TabelaSimbolos;

import CodeGenerator.Tipo;

public class ValorSimbolo {
    private final Tipo tipo; // tipo do simbolo
    private final int index; // endereco na memoria

    public ValorSimbolo(Tipo tipo, int endereco) {
        if (tipo == null) {
            this.tipo = Tipo.NULL;
        } else {
            this.tipo = tipo;
        }
        this.index = endereco;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getIndex() {
        return index;
    }
}
