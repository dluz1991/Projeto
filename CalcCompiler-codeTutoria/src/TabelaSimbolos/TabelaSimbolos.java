package TabelaSimbolos;

import CodeGenerator.Tipo;

import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    private final Map<String, ValorSimbolo> tabela= new HashMap<>();
    private int proximoIndex=0;

    public void putSimbolo(String nome, Tipo tipo) {
        if (tabela.containsKey(nome)) {
            throw new RuntimeException("Variável " + nome + " já existe na tabela de simbolos");
        }
        ValorSimbolo simbolo = new ValorSimbolo(tipo, proximoIndex);
        tabela.put(nome, simbolo);
        proximoIndex++;
    }
    public ValorSimbolo getSimbolo(String nome) {
        return tabela.get(nome);
    }
    public Map<String, ValorSimbolo> getTabela() {
        return tabela;
    }
    public int getSizeTable() {
        return tabela.size();
    }
    public boolean containsVar(String nome) {
        return tabela.containsKey(nome);

    }
    public String getName(int index) {
        for (Map.Entry<String, ValorSimbolo> entry : tabela.entrySet()) {
            if (entry.getValue().getIndex() == index) {
                return entry.getKey();
            }
        }
        return null;
    }
    public Tipo getTipo(String nome) {
        if (tabela.containsKey(nome)) {
            return tabela.get(nome).getTipo();
        }
        return null;
    }
    public int getIndex(String nome) {
        if (tabela.containsKey(nome)) {
            return tabela.get(nome).getIndex();
        }
        return -1;
    }

}
