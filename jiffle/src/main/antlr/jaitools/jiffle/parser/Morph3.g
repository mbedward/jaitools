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
  * Classifies expressions as either NON_LOCAL_EXPR, if they involve
  * POS_VAR, XXX_LOOKUP or NON_LOCAL_VAR, or LOCAL_EXPR if they only
  * involve LOCAL_VAR, local constants and named constants.
  *
  * @author Michael Bedward
  */

tree grammar Morph3;

options {
    tokenVocab = Morph1;
    ASTLabelType = CommonTree;
    output = AST;
}

tokens {
    LOCAL_EXPR;
    NON_LOCAL_EXPR;
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

image_write     : ^(IMAGE_WRITE var[false] term)
                ;

var_assignment  : ^(ASSIGN assign_op var[false] term)
                ;
                
term
scope {
    boolean local;
}
@init {
    $term::local = true;
}
                : expr
                  -> {$term::local}? ^(LOCAL_EXPR expr)
                  -> ^(NON_LOCAL_EXPR expr)
                ;

expr            : calc_expr
                | var[true]
                | INT_LITERAL 
                | FLOAT_LITERAL 
                ;
                
calc_expr       : ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
                ;
                
expr_list       : ^(EXPR_LIST expr*)
                ;
                
var[boolean inExpr]             
                : (POS_VAR | IMAGE_VAR | IMAGE_POS_LOOKUP | IMAGE_INFO_LOOKUP | NON_LOCAL_VAR)
                  {if ($inExpr) $term::local = false;}
              
                | LOCAL_VAR
                | CONSTANT
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

