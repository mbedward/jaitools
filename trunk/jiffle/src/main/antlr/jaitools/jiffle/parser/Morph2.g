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
  * Classifies variables with VAR tokens into either NON_LOCAL_VAR, if they
  * depend on image info lookups, or LOCAL_VAR, if they depend only on
  * local numeric expressions or named constants.
  * <p>
  * IMAGE_VAR and POS_VAR variables have already been classified prior to
  * this step.
  *
  * @author Michael Bedward
  */

tree grammar Morph2;

options {
    tokenVocab = Morph2;
    ASTLabelType = CommonTree;
    output = AST;
}

tokens {
    LOCAL_VAR;
    NON_LOCAL_VAR;
}

@header {
package jaitools.jiffle.parser;

import jaitools.jiffle.interpreter.JiffleRunner;
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

image_write     : ^(IMAGE_WRITE IMAGE_VAR expr)
                ;

var_assignment  : ^(ASSIGN assign_op VAR expr)
                  -> {$expr.local}? ^(ASSIGN assign_op LOCAL_VAR expr)
                  -> ^(ASSIGN assign_op NON_LOCAL_VAR expr)
                  
                | ^(ASSIGN assign_op POS_VAR expr)
                ;
                
expr returns [boolean local]
@init {
    $local = true;
}
                : calc_expr
                | POS_VAR {$local = false;}
                | IMAGE_VAR {$local = false;}
                | IMAGE_POS_LOOKUP {$local = false;}
                | IMAGE_INFO_LOOKUP {$local = false;}
                | VAR
                | INT_LITERAL 
                | FLOAT_LITERAL 
                ;
                
calc_expr       : ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
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

