grammar Tuga;

prog   : stat+ EOF;

stat   : 'escreve' expr COMMA NEWLINE;

expr   :op=(UMINUS|NOT) expr              # Unary
       | expr op=(TIMES|DIV|REMAINDER) expr # MulDiv
       | expr op=(PLUS|MINUS) expr          # AddSub
       | expr op=(LESS|GREATER|LESOEQ|GREOEQ|EQUAL|DIF) expr # Relational
       | expr op=AND expr             # And
       | expr op=OR expr              # Or
       |LPAREN expr RPAREN             # Parens
       | INT                        # Int
       |REAL                        # Real
       |STRING                      # String
       |BOOL                        # Bool
       ;

LPAREN  : '(' ;
RPAREN  : ')' ;
UMINUS  : MINUS ;
PLUS    : '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV     : '/' ;
REMAINDER : '%' ;
LESS    : '<' ;
GREATER : '>' ;
LESOEQ : '<=' ;
GREOEQ : '>=' ;
EQUAL   : 'igual' ;
DIF : 'diferente' ;
AND     : 'e' ;
OR : 'ou' ;
NOT : 'nao' ;
INT      : DIGIT+ ;
REAL     : DIGIT+ '.' DIGIT+ ;
STRING   : '"' .*? '"' ;
BOOL     : 'verdadeiro' | 'falso' ;
COMMA   : ';' ;
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/'NEWLINE* -> skip ; // multi-line comment
NEWLINE  : '\r'? '\n' ;
WS       : [ \t\r\n]+ -> skip ;

fragment
DIGIT    : [0-9] ;
