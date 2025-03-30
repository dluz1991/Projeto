grammar Calc;

prog   : stat+ EOF;

stat   : 'escreve' expr COMMA NEWLINE;

expr   : LPAREN expr RPAREN                             # Parentheses
       | (UMINUS|NOT) expr                              # Unary
       | expr (MULT|DIV|REMAINDER) expr                 # MulDiv
       | expr (PLUS|SUB) expr                           # AddSub
       | expr (LESS|GREATER|LESSEQUAL|GREATEREQUAL) expr # Relational
       | expr (EQUAL|NOTEQUAL) expr                     # EqualNotEqual
       | expr (AND|OR) expr                             # Logical
       | INT                                            # Int
       | REAL                                           # Real
       | STRING                                         # String
       | BOOLEAN                                        # Boolean
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
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/' -> skip ; // multi-line comment

fragment
DIGIT    : [0-9] ;

