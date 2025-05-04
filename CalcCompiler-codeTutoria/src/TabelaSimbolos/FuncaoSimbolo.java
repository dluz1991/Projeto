package TabelaSimbolos;

import CodeGenerator.Tipo;
import java.util.List;

public class FuncaoSimbolo {
    private final String nome;
    private final Tipo tipoRetorno;
    private final List<Tipo> tiposArgumentos;
    private final int scope;

    public FuncaoSimbolo(String nome, Tipo tipoRetorno, List<Tipo> tiposArgumentos, int scope) {
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
        this.tiposArgumentos = tiposArgumentos;
        this.scope = scope;
    }

    public String getNome() {
        return nome;
    }

    public Tipo getTipoRetorno() {
        return tipoRetorno;
    }

    public List<Tipo> getTiposArgumentos() {
        return tiposArgumentos;
    }

    public int getScope() {
        return scope;
    }
}
