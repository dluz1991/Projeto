package TabelaSimbolos;

import CodeGenerator.Tipo;
import java.util.List;

public class FuncaoSimbolo {
    private final String nome;
    private final Tipo tipoRetorno;
    private final List<VarSimbolo> argumentos;
    private final int scope;

    public FuncaoSimbolo(String nome, Tipo tipoRetorno, List<VarSimbolo> argumentos, int scope) {
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
        this.argumentos = argumentos;
        this.scope = scope;
    }

    public String getNome() {
        return nome;
    }

    public Tipo getTipoRetorno() {
        return tipoRetorno;
    }

    public List<VarSimbolo> getTiposArgumentos() {
        return argumentos;
    }

    public int getScope() {
        return scope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipoRetorno).append(" ").append(nome).append("(");
        if (argumentos != null && !argumentos.isEmpty()) {
            for (int i = 0; i < argumentos.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(argumentos.get(i).getTipo());
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
