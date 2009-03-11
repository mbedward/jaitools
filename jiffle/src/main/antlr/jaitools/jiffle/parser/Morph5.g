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
  * This tree walker is run repeatedly so that disjoint chains of local
  * vars are processed fully (e.g. a = 42; b = a;). After each run the
  * number of local vars removed from the AST and stored in the VarTable
  * can be checked with the getCount() method.
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

import jaitools.jiffle.runtime.VarTable;

import static jaitools.utils.DoubleComparison.*;
}

@members {
    
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private VarTable varTable;
public void setVarTable(VarTable varTable) { this.varTable = varTable; }

private double getFixedValue(CommonTree t) {
    return ((FixedValueNode)t).getValue();
}

private int count = 0;
public int getCount() { return count; }
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
                          count++ ;
                          $value = $e.value;
                      }
                  }
                  -> {$e.value != null}?   // node discarded
                     
                  -> ^(ASSIGN assign_op assignable_var expr)
                  
                | ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                
                /* we evalute prefixed expressions here to catch
                 * prefixed fixed values
                 */
                | ^(PREFIX unary_op e=expr)
                   {
                       if ($e.value != null) {
                           switch ($unary_op.type) {
                               case PLUS:
                                   $value = +$e.value;
                                   break;
                               
                               case MINUS:
                                   $value = -$e.value;
                                   break;
                               
                               case NOT:
                                   // @todo check that we are dealing with a
                                   // pseudo logical value here
                                   $value = (dzero($e.value) ? 1.0d : 0.0d);
                                   break;
                               
                               default:
                                   throw new RuntimeException("unknown unary_op type");
                           }
                       }
                   }
                   
                | ^(expr_op expr expr)
              
                | assignable_var
                  {if ($assignable_var.value != null) $value = $assignable_var.value;}
              
                | non_assignable_var
                
                | FIXED_VALUE 
                  {$value = getFixedValue($FIXED_VALUE);}
              
                | CONSTANT
                  {$value = varTable.get($CONSTANT.text);}
                
                ;
                
                
assignable_var returns [String varName, Double value]
                : POS_VAR {$varName = $POS_VAR.text;}
                | NON_LOCAL_VAR {$varName = $NON_LOCAL_VAR.text;}
                | LOCAL_VAR 
                  {
                      $varName = $LOCAL_VAR.text;
                      // if this is a return run we might have this local var
                      // in variable table
                      if (varTable.contains($LOCAL_VAR.text)) {
                          $value = varTable.get($LOCAL_VAR.text);
                      }
                  }
                ;
                
non_assignable_var : image_input
                | IMAGE_POS_LOOKUP
                | IMAGE_INFO_LOOKUP
                ;

image_input     : IMAGE_VAR
                | ^(NBR_REF IMAGE_VAR expr expr)
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

unary_op returns [int type]
                : PLUS {$type = $PLUS.type;}
		| MINUS {$type = $MINUS.type;}
		| NOT {$type = $NOT.type;}
		;
		
type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

