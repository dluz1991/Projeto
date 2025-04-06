grammar Tuga;

prog   : stat+ EOF;

stat   : 'escreve' expr SCOMMA NEWLINE;


expr   :op=(MINUS|NOT) expr              # Unary
       | expr op=(TIMES|DIV|REMAINDER) expr # MulDiv
       | expr op=(PLUS|MINUS) expr          # AddSub
       | expr op=(LESS|GREATER|LESSEQUAL|GREATEREQUAL|EQUAL|DIFFERENT) expr # Relational
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
