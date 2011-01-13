/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

grammar Jiffle;

options {
    output = AST;
    ASTLabelType = CommonTree;
}

tokens {
    ASSIGN;
    CAST;
    EXPR_LIST;
    BRACKETED_EXPR;
    FUNC_CALL;
    NBR_REF;
    POSTFIX;
    PREFIX;
}

@header {
package jaitools.jiffle.parser;
}

@lexer::header {
package jaitools.jiffle.parser;
}

@members {
    private boolean printParseTree = false;

    public void setPrint(boolean b) { printParseTree = b; }
}

@lexer::members {
    private boolean onCommentLine = false;
}


prog		: statement+
                ;
                
statement	: expr eos!
                { 
                    if (printParseTree) {
                        System.out.println($expr.tree == null ? "null" : $expr.tree.toStringTree());
                    }
                }
		;
		
expr		: assign_expr
                | cond_expr
		;
                
func_call       : ID LPAR expr_list RPAR -> ^(FUNC_CALL ID expr_list)
                /* special case for null() */
                | NULL LPAR expr_list RPAR -> ^(FUNC_CALL ID["null"] expr_list)
                ;
                
nbr_ref         : ID LSQUARE expr ',' expr RSQUARE -> ^(NBR_REF ID expr expr)
                ;

expr_list       : (expr (',' expr)* )? -> ^(EXPR_LIST expr*)
		;
		
assign_expr     : ID assign_op expr -> ^(ASSIGN assign_op ID expr)
                ;

cond_expr       : or_expr (QUESTION^ expr ':'! expr)? 
		;
		
or_expr		: xor_expr (OR^ xor_expr)*
		;

xor_expr	: and_expr (XOR^ and_expr)*
		;
		
and_expr	: eq_expr (AND^ eq_expr)*
		;

eq_expr		: comp_expr ((LOGICALEQ^ | NE^) comp_expr)?
		;
		
comp_expr	: add_expr ((GT^ | GE^ | LE^ | LT^) add_expr)?
		;

add_expr	: mult_expr ((PLUS^ | MINUS^) mult_expr)*
		;
		
mult_expr	: exp_expr ((TIMES^ | DIV^ | MOD^) exp_expr)*
		;

exp_expr        : cast_expr (POW^ cast_expr)*
                ;
		
cast_expr	: LPAR type_name RPAR cast_expr -> ^(CAST cast_expr)
		| unary_expr
		;	

unary_expr	: incdec_op postfix_expr -> ^(PREFIX incdec_op postfix_expr)
		| unary_op postfix_expr -> ^(PREFIX unary_op postfix_expr)
		| postfix_expr
		;
		
postfix_expr	: a=atom_expr (incdec_op^)?
		;
		
atom_expr	: ID
		| constant
		| bracketed_expr
                | func_call
                | nbr_ref
		;

bracketed_expr  : LPAR expr RPAR -> ^(BRACKETED_EXPR expr)
                ;
                
constant	: INT_LITERAL
		| FLOAT_LITERAL
                | TRUE
                | FALSE
                | NULL
		;

incdec_op       : INCR
                | DECR
                ;

unary_op	: PLUS
		| MINUS
		| NOT
		;
		
assign_op	: EQ
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;

type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;
                
eos             : (SEMICOLON|NEWLINE)+
                ;

BLOCK_COMMENT   : '/*' (~'*' | '*' ~'/')* '*/' 
                  { $channel = HIDDEN; onCommentLine = true; }
                ;

LINE_COMMENT    : '//' (~('\n' | '\r'))* 
                  { $channel = HIDDEN; onCommentLine = true; }
                ;
                

/* these named constants are defined using case-insensitive fragments
 * (see further down)
 */
TRUE            : T R U E
                ;
                
FALSE           : F A L S E
                ;
                
NULL            : N U L L
                ;

/* Operators sorted and grouped by precedence order */
INCR            : '++' ;  /* pre-fix and post-fix operations */
DECR            : '--' ;

NOT             : '!' ;

POW             : '^' ;      

TIMES           : '*' ;
DIV             : '/' ;
MOD             : '%' ;

PLUS            : '+' ;
MINUS           : '-' ;

GT		: '>';
GE		: '>=';
LE		: '<=';
LT		: '<';

LOGICALEQ	: '==';
NE		: '!=';

AND		: '&&';

OR		: '||';
XOR		: '^|';

QUESTION        : '?' ;  /* conditional operator ?: */

TIMESEQ         : '*=' ;
DIVEQ           : '/=' ;
MODEQ           : '%=' ;
PLUSEQ          : '+=' ;
MINUSEQ         : '-=' ;
EQ              : '='  ;


/* General symbols and token rules */
LPAR            : '(' ;
RPAR            : ')' ;
LSQUARE         : '[' ;
RSQUARE         : ']' ;

ID		: (Letter) (Letter | UNDERSCORE | Digit | Dot)*
		;

fragment
Letter		: 'a'..'z' | 'A'..'Z'
		;

UNDERSCORE      : '_' ;

INT_LITERAL	: '0' | NonZeroDigit Digit*
		;

FLOAT_LITERAL	: ('0' | NonZeroDigit Digit*)? Dot Digit* FloatExp?
		;
fragment
Digit           : '0'..'9'
                ;
                
fragment
Dot             : '.'
                ;

fragment
NonZeroDigit    : '1'..'9'
                ;

fragment
FloatExp        : ('e'|'E' (PLUS|MINUS)? '0'..'9'+)
                ;
				
SEMICOLON	: ';'
                ;

/* Mac: \r  PC: \r\n  Unix \n */
NEWLINE
@init {
    if (onCommentLine) {
        $channel = HIDDEN;
    }
}
@after {
    onCommentLine = false;
}
                : '\r' '\n'?
		| '\n'
		;

/* Fragment tokens for selective case-insensitive matching */
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

WS  		:  (' '|'\t'|'\u000C') {$channel=HIDDEN;} ;
