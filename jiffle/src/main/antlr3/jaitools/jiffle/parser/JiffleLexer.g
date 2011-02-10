/*
 * Copyright 2009-2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * Jiffle language lexer grammar.
 *
 * @author Michael Bedward
 */

lexer grammar JiffleLexer;

@header {
package jaitools.jiffle.parser;
}

@members {
public static final int NEWLINE_CHANNEL = Token.DEFAULT_CHANNEL + 1;
}


COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

/* Logical constants */
TRUE    : 'TRUE' | 'true' ;
FALSE   : 'FALSE' | 'false' ;
NULL    : 'NULL' | 'null' ;

/* Keywords */
INT_TYPE        : 'int' ;
FLOAT_TYPE      : 'float' ;
DOUBLE_TYPE     : 'double' ;
BOOLEAN_TYPE    : 'boolean' ;
OPTIONS : 'options' ;
INIT    : 'init' ;
IF      : 'if' ;

/* Operators sorted and grouped by precedence order */

ABS_POS_PREFIX
        : '$'  ;

INCR    : '++' ;
DECR    : '--' ;

NOT     : '!' ;
POW     : '^' ;
TIMES   : '*' ;
DIV     : '/' ;
MOD     : '%' ;
PLUS    : '+' ;
MINUS   : '-' ;
GT      : '>';
GE      : '>=';
LE      : '<=';
LT      : '<';
LOGICALEQ : '==';
NE      : '!=';
AND     : '&&';
OR      : '||';
XOR     : '^|';
QUESTION: '?' ;  /* ternary conditional operator ?: */
TIMESEQ : '*=' ;
DIVEQ   : '/=' ;
MODEQ   : '%=' ;
PLUSEQ  : '+=' ;
MINUSEQ : '-=' ;
EQ      : '='  ;

/* General tokens */
COMMA   : ',' ;
SEMI    : ';' ;
COLON   : ':' ;
LPAR    : '(' ;
RPAR    : ')' ;
LSQUARE : '[' ;
RSQUARE : ']' ;
LCURLY  : '{' ;
RCURLY  : '}' ;

ID      : (Letter) (Letter | UNDERSCORE | Digit | Dot)*
        ;

fragment
Letter  : 'a'..'z' | 'A'..'Z'
        ;

UNDERSCORE
        : '_' ;

INT_LITERAL
        : '0' | NonZeroDigit Digit*
        ;

FLOAT_LITERAL
        : ('0' | NonZeroDigit Digit*)? Dot Digit* FloatExp?
        ;

fragment
Digit   : '0'..'9' ;

fragment
Dot     : '.' ;

fragment
NonZeroDigit
        : '1'..'9'
        ;

fragment
FloatExp
        : ('e'|'E' (PLUS|MINUS)? '0'..'9'+)
        ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        | '\u000C'
        ) {$channel=HIDDEN;}
    ;


/* 
 * The following are for future use 
 */

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
