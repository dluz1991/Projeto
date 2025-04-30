package TabelaSimbolos;

import CodeGenerator.Tipo;
import java.util.List;

public class EntradaFuncao {
    private final String nome;
    private final Tipo tipoRetorno;
    private final List<Tipo> tiposArgumentos;

    public EntradaFuncao(String nome, Tipo tipoRetorno, List<Tipo> tiposArgumentos) {
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
        this.tiposArgumentos = tiposArgumentos;
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
}
