grammar Calc;

prog   : stat+ EOF;

stat   : 'escreve' expr COMMA NEWLINE;

expr   : LPAREN expr RPAREN
       | (UMINUS|NOT) expr
       | expr (MULT|DIV|REMAINDER) expr
       | expr (PLUS|SUB) expr
       | expr (LESS|GREATER|LESSEQUAL|GREATEREQUAL) expr
       | expr (EQUAL|NOTEQUAL) expr
       | expr (AND|OR) expr
       | INT
       | REAL
       | STRING
       | BOOLEAN
       ;

LPAREN : '(' ;
RPAREN : ')' ;
UMINUS : '-' ;
MULT   : '*' ;
DIV    : '/' ;
PLUS   : '+' ;
SUB  : '-' ;
REMAINDER : '%' ;
LESS   : '<' ;
GREATER : '>' ;
LESSEQUAL : '<=' ;
GREATEREQUAL : '>=' ;
EQUAL  : '==' ;
NOTEQUAL : '!=' ;
NOT    : 'nao' ;
AND    : 'e' ;
OR     : 'ou' ;
COMMA  : ';' ;
INT      : DIGIT+ ;
REAL     : DIGIT+ '.' DIGIT+ ;
STRING   : '"' (~["\r\n])* '"' ;
BOOLEAN  : 'verdadeiro' | 'falso' ;
NEWLINE  : '\r'? '\n' ;
WS       : [ \t\r\n]+ -> skip ;

fragment
DIGIT    : [0-9] ;

