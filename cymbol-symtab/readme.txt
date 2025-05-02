This is the code taken from chapter 8.4 from Terrence Parr's The Definitive ANTLR 4
Reference, with the following modification:

(1) Change Symbol to include a Token rather than just a name. This allows for better error
    messages.

(2) FunctionSymbol does not implement Scope. It is simply a subclass of Symbol.
    I think it's much simpler this way.

(3) Got rid of interface Scope, classes BaseScope, GlobalScope, and LocalScope
    There's only one class called Scope now. I think it's simpler this way, at least for now.

(4) The function parameters do not constitute a separate scope on its own. Instead,
    they become part of the scope of the main block of the function. That is, they act
    as if they were declared as local variables of the function.
    To implement this we do not create a scope upon entering the function declaration,
    we store the function parameters, and upon entering the block of the function body,
    we then create a new scope and define the parameters there.

(5) Check redeclaration of variables and function parameters,
    and emit error messages accordingly.

-- Fernando Lobo, Apr 2022