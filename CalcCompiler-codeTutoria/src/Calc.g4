grammar Calc;

prog   : stat+ EOF;

stat   : 'escreve' expr SCOMMA NEWLINE;


expr   : LPAREN expr RPAREN             # Parens
       | (UMINUS|NOT) expr              # Unary
       | expr (TIMES|DIV|REMAINDER) expr # MulDiv
       | expr (PLUS|MINUS) expr          # AddSub
       | expr (LESS|GREATER|LESSEQUAL|GREATEREQUAL|EQUAL|DIFFERENT) expr # Relational
       | expr AND expr             # And
       | expr OR expr              # Or
       | INT                        # Int
       |REAL                        # Real
       |STRING                      # String
       |BOOL                        # Bool
       ;

LPAREN  : '(' ;
RPAREN  : ')' ;
UMINUS  : '-' ;
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
SCOMMA   : ';' ;
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/'NEWLINE* -> skip ; // multi-line comment
NEWLINE  : '\r'? '\n' ;
WS       : [ \t\r\n]+ -> skip ;

fragment
DIGIT    : [0-9] ; 
