/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 *
 * -- with a few modifications by Fernando Lobo (see readme.txt file for details)
***/
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import SymbolTable.*;
import Cymbol.*;

public class DefPhase extends CymbolBaseListener {
    ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
    Scope global;
    Scope currentScope;   // define symbols in this scope

    FunctionSymbol currentFunction = null;

    public void enterFile(CymbolParser.FileContext ctx) {
        global = new Scope(null, "global");
        currentScope = global;
        System.out.println(currentScope);
    }
	
    public void exitFile(CymbolParser.FileContext ctx) {
        System.out.println(currentScope);
    }

    public void enterFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        Token token = ctx.ID().getSymbol();
        int typeTokenType = ctx.type().start.getType();
        //System.out.println(">> token: " + token + ", typeTokenType: " + typeTokenType );
        Symbol.Type type = CheckSymbols.getType(typeTokenType);

        FunctionSymbol function = new FunctionSymbol(token, type);
        currentScope.define(function); // Define function in current scope
        System.out.println("enterFunction " + ctx.type().getText() + " "
                + function.lexeme() + "(...) >> " + currentScope);
        currentFunction = function;
    }

    void saveScope(ParserRuleContext ctx, Scope s) {
        scopes.put(ctx, s);
    }

    public void exitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        // now does nothing because end of block with remove the function scope.
    }

    public void enterBlock(CymbolParser.BlockContext ctx) {
        // push new scope
        currentScope = new Scope(currentScope);
        if (currentFunction != null) {
            currentScope.setName( currentFunction.lexeme() );
            // add function parameters to the current scope, as if they were local variables
            for (Symbol sym: currentFunction.get_arguments()) {
                if ( !currentScope.contains(sym.lexeme()) )
                    currentScope.define(sym);
                else
                    CheckSymbols.error(sym.getToken(), "formal parameter "
                            + sym.lexeme()
                            + " is defined more than once in function "
                            + currentFunction.lexeme() );
            }
        }
        else
            currentScope.setName("local");
        currentFunction = null;  // so that it does not interfere with other blocks.
        saveScope(ctx, currentScope);
        System.out.println("enterBlock >> " + currentScope);
    }

    public void exitBlock(CymbolParser.BlockContext ctx) {
        currentScope = currentScope.getEnclosingScope(); // pop scope
        System.out.println("exitBlock >> " + currentScope);
    }

    public void exitFormalParameter(CymbolParser.FormalParameterContext ctx) {
        CymbolParser.TypeContext typeCtx = ctx.type();
        int typeTokenType = typeCtx.start.getType();
        Symbol.Type type = CheckSymbols.getType(typeTokenType);
        VariableSymbol var = new VariableSymbol(ctx.ID().getSymbol(), type);
        //System.out.println("exitFormalP " + ctx.getText() + " >> " + var);
        currentFunction.add_argument(var);
        System.out.println("currentFunction " + currentFunction.lexeme() + " : " + currentFunction);
    }

    public void exitVarDecl(CymbolParser.VarDeclContext ctx) {
        defineVar(ctx.type(), ctx.ID().getSymbol());
        System.out.println("exitVarDecl " + ctx.type().getText() + " "
                + ctx.ID().getText() + " >> " + currentScope);
    }

    void defineVar(CymbolParser.TypeContext typeCtx, Token nameToken) {
        int typeTokenType = typeCtx.start.getType();
        Symbol.Type type = CheckSymbols.getType(typeTokenType);
        VariableSymbol var = new VariableSymbol(nameToken, type);
        if ( !currentScope.contains(var.lexeme()) )
            currentScope.define(var); // Define symbol in current scope
        else
            CheckSymbols.error(nameToken, "variable " + var.lexeme()
                    + " is already defined");
    }
}


