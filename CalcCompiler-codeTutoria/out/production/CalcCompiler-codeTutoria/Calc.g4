grammar Calc;

prog   : stat+ EOF;

stat   : 'print' expr NEWLINE;

expr   : '-' expr                          # Uminus
       |<assoc=right>expr op='^' expr                  # Exp
       | expr op=('*'|'/') expr            # MulDiv
       | expr op=('+'|'-') expr            # AddSub
       | INT                               # Int
       | '(' expr ')'                      # Parens
       ;

INT      : DIGIT+ ;
NEWLINE  : '\r'? '\n' ;
WS       : [ \t\r\n]+ -> skip ;

fragment
DIGIT    : [0-9] ; 
