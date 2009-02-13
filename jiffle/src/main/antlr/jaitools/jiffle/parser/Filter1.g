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
 
 /** 
  * Introduces FIXED_VALUE token with FixedValueNode as the tree node.
  * Replaces INT_LITERAL AND FLOAT_LITERAL with FIXED_VALUE
  *
  * @author Michael Bedward
  */

tree grammar Filter1;

options {
    tokenVocab = ExpressionSimplifier;
    ASTLabelType = CommonTree;
    output = AST;
}

tokens {
    FIXED_VALUE;
}

@header {
package jaitools.jiffle.parser;
}

@members {
    
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

}

start           : statement+ 
                ;

statement       : image_write
                | var_assignment
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR typed_expr)
                ;

var_assignment  : ^(ASSIGN assign_op (POS_VAR|SIMPLE_VAR) typed_expr)
                ;
                

typed_expr      : ^(POS_EXPR expr)
                | ^(FIXED_EXPR expr)
                ;
                
expr            : ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
                
                | POS_VAR
                | IMAGE_VAR
                | SIMPLE_VAR
                
                | INT_LITERAL -> FIXED_VALUE<FixedValueNode>[$INT_LITERAL.text]
                | FLOAT_LITERAL -> FIXED_VALUE<FixedValueNode>[$FLOAT_LITERAL.text]
                ;
                
expr_list       : ^(EXPR_LIST expr*)
                ;
                
expr_op         : POW
                | TIMES 
                | DIV 
                | MOD
                | PLUS  
                | MINUS
                | OR 
                | AND 
                | XOR 
                | GT 
                | GE 
                | LE 
                | LT 
                | LOGICALEQ 
                | NE 
                ;

assign_op	: EQ
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;
		
incdec_op       : INCR
                | DECR
                ;

unary_op	: PLUS
		| MINUS
		| NOT
		;
		
type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

