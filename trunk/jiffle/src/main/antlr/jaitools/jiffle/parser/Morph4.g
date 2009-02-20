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
  * Replaces INT_LITERAL AND FLOAT_LITERAL with FIXED_VALUE.
  * Replaces TRUE and FALSE tokens with 1.0 and 0.0 FIXED_VALUE nodes
  * respectively.
  *
  * @author Michael Bedward
  */

tree grammar Morph4;

options {
    tokenVocab = Morph1;
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
                | expr
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR expr)
                ;

expr            : ^(ASSIGN assign_op assignable_var expr)
                | ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(PREFIX unary_op expr)
                | ^(expr_op expr expr)
                | assignable_var
                | non_assignable_var
                | INT_LITERAL -> FIXED_VALUE<FixedValueNode>[$INT_LITERAL.text]
                | FLOAT_LITERAL -> FIXED_VALUE<FixedValueNode>[$FLOAT_LITERAL.text]
                | TRUE -> FIXED_VALUE<FixedValueNode>[1.0d]
                | FALSE -> FIXED_VALUE<FixedValueNode>[0.0d]
                | NULL -> FIXED_VALUE<FixedValueNode>[Double.NaN]
                ;
                
assignable_var  : POS_VAR
                | LOCAL_VAR
                | NON_LOCAL_VAR
                ;
                
non_assignable_var :
                  IMAGE_VAR
                | IMAGE_POS_LOOKUP
                | IMAGE_INFO_LOOKUP
                | CONSTANT
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

