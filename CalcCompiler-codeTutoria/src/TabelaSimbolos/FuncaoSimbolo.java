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

    public List<VarSimbolo> getArgumentos() {
        return argumentos;
    }

    public int getScope() {
        return scope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Função: ").append(nome).append("\n");
        sb.append("  Tipo de retorno: ").append(tipoRetorno).append("\n");
        sb.append("  Scope: ").append(scope).append("\n");
        sb.append("  Argumentos:");
        if (argumentos == null || argumentos.isEmpty()) {
            sb.append(" (nenhum)\n");
        } else {
            sb.append("\n");
            for (VarSimbolo arg : argumentos) {
                sb.append("    - ").append(arg.getName())
                        .append(" : ").append(arg.getTipo()).append("\n");
            }
        }
        return sb.toString();
    }

}
