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
    FUNC_CALL;
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


prog		: (statement 
                    { 
                        if (printParseTree) {
                            System.out.println($statement.tree == null ? "null" : $statement.tree.toStringTree());
                        }
                    })+ 
                ;

statement	: expr eos!
		;
		
expr		: func_call
                | assign_expr
                | cond_expr
		;

func_call       : ID LPAR expr_list RPAR -> ^(FUNC_CALL ID expr_list)
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
		| LPAR! expr RPAR!
		;

constant	: INT_LITERAL
		| FLOAT_LITERAL
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

/* end of statement */
eos		: SEMICOLON ( NEWLINE )*   /* semicolon, options followed by newline(s) */
		| NEWLINE ( SEMICOLON ( NEWLINE )* )*  /* newline optionally followed by semicolon and any number of newlines */
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

ID		: (Letter) (Letter | UNDERSCORE | Digit)*
		;

fragment
Letter		: 'a'..'z' | 'A'..'Z'
		;

UNDERSCORE      : '_' ;

INT_LITERAL	: '0' | NonZeroDigit Digit*
		;

FLOAT_LITERAL	: ('0' | NonZeroDigit Digit*)? '.' Digit* FloatExp?
		;
fragment
Digit           : '0'..'9'
                ;

fragment
NonZeroDigit    : '1'..'9'
                ;

fragment
FloatExp        : ('e'|'E' (PLUS|MINUS)? '0'..'9'+)
                ;
				
SEMICOLON	: ';' ;

/* Mac: \r  PC: \r\n  Unix \n */
NEWLINE		: '\r' '\n'?
		| '\n'
		;

WS  		:  (' '|'\t'|'\u000C') {$channel=HIDDEN;} ;
