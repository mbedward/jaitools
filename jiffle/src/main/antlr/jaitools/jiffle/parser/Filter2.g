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
  * Replaces any instances of SIMPLE_VAR = FIXED_VALUE with FIXED_VALUE
  * to avoid var table lookups.
  *
  * @author Michael Bedward
  */

tree grammar Filter2;

options {
    tokenVocab = Filter1;
    ASTLabelType = CommonTree;
    output = AST;
}

@header {
package jaitools.jiffle.parser;

import jaitools.jiffle.interpreter.VarTable;
}

@members {
    
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private VarTable varTable;
public void setVarTable(VarTable varTable) { this.varTable = varTable; }

private double getFixedValue(CommonTree t) {
    return ((FixedValueNode)t).getValue();
}

}

start           : statement+ 
                ;

statement       : image_write
                | var_assignment
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR ^(FIXED_EXPR e=expr))
                  -> {$e.isFixedValue}? 
                     ^(IMAGE_WRITE IMAGE_VAR FIXED_VALUE<FixedValueNode>[getFixedValue($e.tree)])

                  -> ^(IMAGE_WRITE IMAGE_VAR ^(FIXED_EXPR expr))
                     

                | ^(IMAGE_WRITE IMAGE_VAR ^(POS_EXPR expr))
                ;

var_assignment  : ^(ASSIGN op=assign_op SIMPLE_VAR ^(FIXED_EXPR e=expr))
                  {
                      if ($e.isFixedValue) {
                          varTable.assign($SIMPLE_VAR.text, $op.tree.getText(), $e.value);
                      }
                  }
                  -> {$e.isFixedValue}?   // node discarded
                     
                  -> ^(ASSIGN assign_op SIMPLE_VAR ^(FIXED_EXPR expr))
                  
                | ^(ASSIGN assign_op POS_VAR ^(POS_EXPR expr))
                ;
                
expr returns [boolean isFixedValue, double value]
@init {
    $isFixedValue = false;
}
                : ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
                
                | POS_VAR
                | IMAGE_VAR
                | SIMPLE_VAR
                
                | FIXED_VALUE {$isFixedValue = true; $value = getFixedValue($FIXED_VALUE);}
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

