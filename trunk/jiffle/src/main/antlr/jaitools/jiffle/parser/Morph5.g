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
  * For each subtree which of the form LOCAL_VAR = FIXED_VALUE
  * <ul>
  * <li> stores the var's name and value in a VarTable object passed in
  *      by the client code;<br> 
  * <li> deletes the subtree from the AST
  * </ul>
  *
  * @author Michael Bedward
  */

tree grammar Morph5;

options {
    tokenVocab = Morph4;
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

start
@init {
    if (varTable == null) {
        throw new RuntimeException("Must initialize varTable first");
    }
}
                : statement+ 
                ;

statement       : image_write
                | expr
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR expr)
                ;
                
expr returns [Double value]
@init {
    $value = null;
}
                : ^(ASSIGN op=assign_op assignable_var e=expr)
                  {
                      if ($e.value != null) {
                          varTable.assign($assignable_var.varName, $op.tree.getText(), $e.value);
                          $value = $e.value;
                      }
                  }
                  -> {$e.value != null}?   // node discarded
                     
                  -> ^(ASSIGN assign_op assignable_var expr)
                  
                | ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
                | assignable_var
                | non_assignable_var

                | FIXED_VALUE 
                  {$value = getFixedValue($FIXED_VALUE);}
              
                | CONSTANT
                  {$value = varTable.get($CONSTANT.text);}
                
                ;
                
                
assignable_var returns [String varName]
                : POS_VAR {$varName = $POS_VAR.text;}
                | LOCAL_VAR {$varName = $LOCAL_VAR.text;}
                | NON_LOCAL_VAR {$varName = $NON_LOCAL_VAR.text;}
                ;
                
non_assignable_var : IMAGE_VAR
                | IMAGE_POS_LOOKUP
                | IMAGE_INFO_LOOKUP
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

