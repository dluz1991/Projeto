package TabelaSimbolos;

import CodeGenerator.Tipo;

import java.util.*;

public class TabelaSimbolos {
    private final Map<String, Object> tabela = new HashMap<>();
    private final TabelaSimbolos pai;

    public TabelaSimbolos() {
        this.pai = null;
    }

    public TabelaSimbolos(TabelaSimbolos pai) {
        this.pai = pai;
    }
    public int getSizeTable() {
        return tabela.size();
    }
    // Inserir variável
    public void putVariavel(String nome, Tipo tipo, int endereco) {
        if (tabela.containsKey(nome)) throw new RuntimeException("Símbolo já existe: " + nome);
        tabela.put(nome, new ValorSimbolo(tipo, endereco));
    }

    // Inserir função
    public void putFuncao(String nome, Tipo tipoRetorno, List<Tipo> tiposArgumentos) {
        if (tabela.containsKey(nome)) throw new RuntimeException("Símbolo já existe: " + nome);
        tabela.put(nome, new EntradaFuncao(nome, tipoRetorno, tiposArgumentos));
    }


    // Obter símbolo (pode ser variável ou função)
    public Object getSimbolo(String nome) {
        if (tabela.containsKey(nome)) return tabela.get(nome);
        else if (pai != null) return pai.getSimbolo(nome);
        else return null;
    }

    public boolean containsSimbolo(String nome) {
        return getSimbolo(nome) != null;
    }

    public Tipo getTipo(String nome) {
        Object simbolo = getSimbolo(nome);
        if (simbolo instanceof ValorSimbolo var) {
            return var.getTipo();
        } else if (simbolo instanceof EntradaFuncao func) {
            return func.getTipoRetorno();
        } else {
            return null;
        }
    }
    public TabelaSimbolos getPai() {
        return pai;
    }


    public void printTabela() {
        System.out.println("Tabela de simbolos:");
        for (Map.Entry<String, Object> entry : tabela.entrySet()) {
            String nome = entry.getKey();
            Object valor = entry.getValue();

            if (valor instanceof ValorSimbolo var) {
                System.out.printf("VAR  - Nome: %s, Tipo: %s, Endereco: %d%n", nome, var.getTipo(), var.getIndex());
            } else if (valor instanceof EntradaFuncao func) {
                System.out.printf("FUNC - Nome: %s, Tipo retorno: %s, Argumentos: %s%n", nome, func.getTipoRetorno(), func.getTiposArgumentos());
            } else {
                System.out.printf("???  - Nome: %s, Tipo desconhecido%n", nome);
            }
        }
    }


}
