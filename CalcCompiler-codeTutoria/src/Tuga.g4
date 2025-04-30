grammar Tuga;

prog    : varDeclaration* functionDecl+ EOF;

functionDecl : 'funcao' ID LPAREN formalParameters? RPAREN(':' TYPE)? bloco;

formalParameters : ID ':'TYPE (',' ID ':' TYPE)*;
bloco : 'inicio' varDeclaration* stat* 'fim' ;

varDeclaration: ID(','ID)* ':' TYPE? SCOMMA;

stat   : ID '<-' expr SCOMMA                           # Afetacao
       | bloco                                       # BlocoStat
       | 'enquanto' LPAREN expr RPAREN stat           # Equanto
       | 'se' LPAREN expr RPAREN stat ('senao' stat)?  # Se
       | 'escreve' expr SCOMMA                         # Escreve
       | 'retorna' expr SCOMMA                          # Retorna
       | ID LPAREN expr? RPAREN SCOMMA       # ChamadaFuncao
       | SCOMMA                                        # Vazia
       ;


expr   :op=(MINUS|NOT) expr                                                 # Unary
       | expr op=(TIMES|DIV|REMAINDER) expr                                 # MulDiv
       | expr op=(PLUS|MINUS) expr                                          # AddSub
       | expr op=(LESS|GREATER|LESSEQUAL|GREATEREQUAL|EQUAL|DIFFERENT) expr # Relational
       | expr op=AND expr                                                   # And
       | expr op=OR expr                                                    # Or
       |LPAREN expr RPAREN                                                  # Parens
       |ID LPAREN exprList? RPAREN                                          # ChamadaFuncaoExpr
       |ID                                                                  # Var
       |INT                                                                 # Int
       |REAL                                                                # Real
       |STRING                                                              # String
       |BOOL                                                                # Bool
       ;


exprList: expr (',' expr)*;

TYPE    :'inteiro' | 'real' | 'string' | 'booleano' ;
LPAREN  : '(' ;
RPAREN  : ')' ;
PLUS    : '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV     : '/' ;
REMAINDER : '%' ;
LESS    : '<' ;
GREATER : '>' ;
LESSEQUAL : '<=' ;
GREATEREQUAL : '>=' ;
EQUAL   : 'igual' ;
DIFFERENT : 'diferente' ;
AND     : 'e' ;
OR : 'ou' ;
NOT : 'nao' ;
INT      : DIGIT+ ;
REAL     : DIGIT+ '.' DIGIT+ ;
STRING   : '"' .*? '"' ;
BOOL     : 'verdadeiro' | 'falso' ;
ID: [a-zA-Z_][a-zA-Z_0-9]*;
SCOMMA   : ';' ;
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/' -> skip ; // multi-line comment

WS         : [ \t\r\n]+ -> skip ;



fragment
DIGIT    : [0-9] ; 
