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
  * Replaces all var qualifiers, other than IMAGE_VAR, that were used in the 
  * optimizing steps with VAR
  *
  * @author Michael Bedward
  */

tree grammar Morph5;

options {
    tokenVocab = Morph4;
    ASTLabelType = CommonTree;
    output = AST;
}

tokens {
    VAR;
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

assignment      : 
                ;
                
expr            : ^(ASSIGN op=assign_op assignable_var expr) 
                | ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(POW expr expr) 
                | ^(TIMES expr expr) 
                | ^(DIV expr expr) 
                | ^(MOD expr expr) 
                | ^(PLUS expr expr) 
                | ^(MINUS expr expr) 
                | ^(OR expr expr) 
                | ^(AND expr expr) 
                | ^(XOR expr expr) 
                | ^(GT expr expr) 
                | ^(GE expr expr) 
                | ^(LT expr expr) 
                | ^(LE expr expr) 
                | ^(LOGICALEQ expr expr) 
                | ^(NE expr expr) 
                | ^(PREFIX unary_op expr)
                | ^(BRACKETED_EXPR expr)
                | FIXED_VALUE 
                | assignable_var
                | non_assignable_var
                ;
                
expr_list       : ^(EXPR_LIST expr*) 
                ;                
                
assignable_var  : POS_VAR -> VAR[$POS_VAR.getToken()]
                | NON_LOCAL_VAR -> VAR[$NON_LOCAL_VAR.getToken()]
                ;
                
non_assignable_var 
                : IMAGE_VAR
                | ^(NBR_REF IMAGE_VAR expr expr)
                | IMAGE_POS_LOOKUP -> VAR[$IMAGE_POS_LOOKUP.getToken()]
                | IMAGE_INFO_LOOKUP -> VAR[$IMAGE_INFO_LOOKUP.getToken()]
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

