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
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
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


jiffle		: (statement 
                    { 
                        if (printParseTree) {
                            System.out.println($statement.tree == null ? "null" : $statement.tree.toStringTree());
                        }
                    })+ 
                ;

statement	: expr EOS!
		| EOS!
		;

expr		: func_call
                | assign_expr
                | or_expr
		;

func_call       : ID '(' expr_list ')' -> ^(FUNC_CALL ID expr_list)
                ;

expr_list       : (expr (',' expr)* )? -> ^(EXPR_LIST expr*)
		;
		
assign_expr     : ID assign_op expr -> ^(ASSIGN assign_op ID expr)
                ;

assign_op	: '='
		| '*='
		| '/='
		| '%='
		| '+='
		| '-='
		;
		
or_expr		: and_expr (OR^ and_expr)*
		;

and_expr	: xor_expr (AND^ xor_expr)*
		;

xor_expr	: eq_expr (XOR^ eq_expr)*
		;
		
eq_expr		: comp_expr ((EQ^ | NE^) comp_expr)?
		;
		
comp_expr	: add_expr ((GT^ | GE^ | LE^ | LT^) add_expr)?
		;

add_expr	: mult_expr (('+'^ | '-'^) mult_expr)*
		;
		
mult_expr	: cast_expr (('*'^ | '/'^ | '%'^) cast_expr)*
		;					
		
cast_expr	: '(' type_name ')' cast_expr -> ^(CAST cast_expr)
		| unary_expr
		;	

unary_expr	: '++'^ postfix_expr
		| '--'^ postfix_expr
		| unary_op^ postfix_expr
		| postfix_expr
		;
		
postfix_expr	: '++'
		| '--'
		| atom_expr
		;
		
unary_op	: '+'
		| '-'
		| '!'
		;
		
type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

atom_expr	: ID
		| constant
		| '('! expr ')'!
		;

constant	: INT_LITERAL
		| FLOAT_LITERAL
		;
		
OR		: '||' | 'OR' ;
AND		: '&&' | 'AND' ;
XOR		: '^|' | 'XOR' ;
EQ		: '==' | 'EQ' ;
NE		: '!=' | 'NE' ;
GT		: '>' | 'GT' ;
GE		: '>=' | 'GE' ;
LE		: '<=' | 'LE' ;
LT		: '<' | 'LT' ;

ID		: (LETTER) (LETTER | '_' | '0'..'9')*
		;

fragment
LETTER		: 'a'..'z' | 'A'..'Z'
		;
		
INT_LITERAL	: '0' | '1'..'9' '0'..'9'*
		;

FLOAT_LITERAL	: ('0' | '1'..'9' '0'..'9'*)? '.' '0'..'9'* ('e'|'E' ('+'|'-')? '0'..'9'+)?
		;
				
EOS		: ';' ;

WS  		:  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;} ;
