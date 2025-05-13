package TabelaSimbolos;
import TabelaSimbolos.*;
import CodeGenerator.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaSimbolos {

    private final Map<String, VarSimbolo>  vars = new HashMap<>();
    private final Map<String,FuncaoSimbolo> funs = new HashMap<>();
    private final TabelaSimbolos pai;

    public TabelaSimbolos()            { this.pai = null; }
    public TabelaSimbolos(TabelaSimbolos p){ this.pai = p; }

    /* --------- variáveis --------- */
    public void putVariavel(String nome, Tipo tipo, int addr, int scope) {
        if (vars.containsKey(nome))
            throw new RuntimeException("Variável já existe: "+nome);
        if (funs.containsKey(nome)){
            throw new RuntimeException("Nome de função já existe: "+nome);
        }
        vars.put(nome, new VarSimbolo(tipo, addr, scope));
    }

    public VarSimbolo getVar(String nome){
        if (vars.containsKey(nome))          return vars.get(nome);
        else if (pai != null)                return pai.getVar(nome);
        else                                 return null;
    }

    /* --------- funções --------- */
    public void putFuncao(String nome, Tipo ret, List<Tipo> args, int scope){
        if (funs.containsKey(nome))
            throw new RuntimeException("Função já existe: "+nome);
        if (vars.containsKey(nome)){
            throw new RuntimeException("Nome de variável já existe: "+nome);
        }
        funs.put(nome,new FuncaoSimbolo(nome,ret,args,scope));
    }

    public FuncaoSimbolo getFuncao(String nome){
        if (funs.containsKey(nome))          return funs.get(nome);
        else if (pai != null)                return pai.getFuncao(nome);
        else                                 return null;
    }

    /* ---------- utilidades genéricas ---------- */
    public int sizeVars (){ return vars.size(); }
    public int sizeFuncs(){ return funs.size(); }
    public TabelaSimbolos getPai(){ return pai; }

    public boolean existeVar (String nome){ return getVar(nome)  != null; }
    public boolean existeFunc(String nome){ return getFuncao(nome)!= null; }

    public int scopeVar (String nome){
        VarSimbolo v = getVar(nome);
        return v==null ? -1 : v.getScope();
    }

    public int scopeFunc(String nome){
        FuncaoSimbolo f = getFuncao(nome);
        return f==null ? -1 : f.getScope();
    }
}
