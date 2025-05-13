import CodeGenerator.Tipo;
import CodeGenerator.TypeChecker;
import Tuga.*;
import TabelaSimbolos.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.*;

public class DefPhase extends TugaBaseListener {
    ParseTreeProperty<Enquadramento> scopes = new ParseTreeProperty<Enquadramento>();
    Enquadramento global;
    Enquadramento currentScope;
    int lastScope = 0;
    Stack<Enquadramento> scopeStack = new Stack<>();

    FuncaoSimbolo funcaoAtual = null;
    boolean print = true;

    public DefPhase(Enquadramento scope) {
        if (scope != null) {
            this.global = scope;
            this.currentScope = scope;
            this.lastScope = scope.getCurrentScope();
        }
    }

    @Override
    public void enterProg(TugaParser.ProgContext ctx) {
        if (global == null) {
            global = new Enquadramento(null, 0);
            currentScope = global;
            lastScope = 0;
        }
        scopeStack.push(currentScope);
        if (print) {
            System.out.println(currentScope);
        }
    }

    @Override
    public void exitProg(TugaParser.ProgContext ctx) {
        // Verifica se a função principal "main" foi declarada
        if (!currentScope.contains("principal")) {
            System.err.println("Erro: função principal não encontrada.");
        }
        if (print) {
            System.out.println(currentScope);
        }
        scopeStack.pop();
    }

    @Override
    public void enterFunctionDecl(TugaParser.FunctionDeclContext ctx) {
        String nome = ctx.ID().getText();
        String tipoString = ctx.TYPE() != null ? ctx.TYPE().getText() : "void";  // Verifica se o tipo é nulo
        Tipo tipo = TypeChecker.getTipo(tipoString);

        List<VarSimbolo> args = new ArrayList<>();
        if (ctx.formalParameters() != null) {  // Verifica se formalParameters() é não nulo
            for (TugaParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                String paramNome = param.ID().getText();
                Tipo paramTipo = TypeChecker.getTipo(param.TYPE().getText());
                currentScope.put(paramNome, paramTipo);
                args.add(new VarSimbolo(paramNome, paramTipo));
            }
        }

        FuncaoSimbolo funcao = new FuncaoSimbolo(nome, tipo, args, currentScope.getCurrentScope());
        funcaoAtual = funcao;

        // Se necessário, print para debug
        if (print) {
            System.out.println("enterFunction " + ctx.TYPE() + " " + funcao.getNome() + "(...) >> " + currentScope);
        }
    }


    void saveScope(ParserRuleContext ctx, Enquadramento s) {
        scopes.put(ctx, s);
    }

    @Override
    public void enterBloco(TugaParser.BlocoContext ctx) {
        currentScope = new Enquadramento(currentScope, ++lastScope);
        scopeStack.push(currentScope);

        // Adiciona as variáveis da função no escopo
        if (funcaoAtual != null) {
            currentScope.put(funcaoAtual.getNome(), funcaoAtual.getTipoRetorno());
            for (var arg : funcaoAtual.getTiposArgumentos()) {
                currentScope.put(arg.getName(), arg.getTipo());
            }
        }

        if (print) {
            System.out.println("Entrando no escopo: " + currentScope.getCurrentScope());
        }
    }

    @Override
    public void exitBloco(TugaParser.BlocoContext ctx) {
        scopeStack.pop();
        currentScope = scopeStack.peek();
        if (print) {
            System.out.println("Saindo do escopo: " + currentScope.getCurrentScope());
        }
    }

    @Override
    public void enterVarDeclaration(TugaParser.VarDeclarationContext ctx) {
        String tipoString = ctx.TYPE().getText();
        Tipo tipo = TypeChecker.getTipo(tipoString);

        for (var id : ctx.ID()) {
            String nome = id.getText();
            // Verifica se já existe uma variável com o mesmo nome no escopo
            if (currentScope.contains(nome)) {
                // Caso seja necessário, tratar como erro (sem implementação extra de erro)
            } else {
                currentScope.put(nome, tipo);
            }
        }
    }

    public void printScopes() {
        System.out.println("--- Tabela de Identificadores ---");
        currentScope.print();
    }
}
