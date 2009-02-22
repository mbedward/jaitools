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
  * Takes a VarTable provided by client code that contains var names and values
  * for fixed values. Replaces all instances such vars in the AST by their value.
  *
  * @author Michael Bedward
  */

tree grammar Morph6;

options {
    tokenVocab = Morph5;
    ASTLabelType = CommonTree;
    output = AST;
}

@header {
package jaitools.jiffle.parser;

import jaitools.jiffle.interpreter.FunctionTable;
import jaitools.jiffle.interpreter.VarTable;
import jaitools.jiffle.util.CollectionFactory;

import static jaitools.jiffle.util.DoubleComparison.*;
}

@members {
    
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private VarTable varTable;
public void setVarTable(VarTable varTable) { this.varTable = varTable; }

private FunctionTable funcTable = new FunctionTable();

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

expr returns [boolean hasValue, Double value]
                : sub_expr
                  {$hasValue = $sub_expr.hasValue; $value = $sub_expr.value;}
              
                  -> {$sub_expr.hasValue}? FIXED_VALUE<FixedValueNode>[$sub_expr.value]
                  
                  -> sub_expr
                ;
                
sub_expr returns [boolean hasValue, Double value]
@init {
    $hasValue = false;
    $value = null;
}
                : ^(ASSIGN op=assign_op assignable_var expr)
                
                | ^(FUNC_CALL ID expr_list)
                  {
                      if ($expr_list.values != null) {
                          $value = funcTable.invoke($ID.text, $expr_list.values);
                          $hasValue = true;
                      }
                  }
                  
                | ^(QUESTION econd=expr e1=expr e2=expr)
                  {
                      if (econd.hasValue && e1.hasValue && e2.hasValue) {
                          if (!dzero(econd.value)) {
                              $value = e1.value;
                          } else {
                              $value = e2.value;
                          }
                      }
                  }
                  
                | ^(POW e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = Math.pow(e1.value, e2.value);
                          $hasValue = true;
                      }
                  }
              
                | ^(TIMES e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = e1.value * e2.value;
                          $hasValue = true;
                      }
                  }
              
                | ^(DIV e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = e1.value / e2.value;
                          $hasValue = true;
                      }
                  }
              
                | ^(MOD e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = e1.value \% e2.value;
                          $hasValue = true;
                      }
                  }
                  
                | ^(PLUS e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = e1.value + e2.value;
                          $hasValue = true;
                      }
                  }
              
                | ^(MINUS e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = e1.value - e2.value;
                          $hasValue = true;
                      }
                  }
              
                | ^(OR e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (!dzero(e1.value) || !dzero(e2.value)) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(AND e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (!dzero(e1.value) && !dzero(e2.value)) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(XOR e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (!dzero(e1.value) ^ !dzero(e2.value)) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(GT e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) > 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(GE e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) >= 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(LT e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) < 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(LE e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) <= 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(LOGICALEQ e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) == 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | ^(NE e1=expr e2=expr) 
                  {
                      if (e1.hasValue && e2.hasValue) {
                          $value = (dcomp(e1.value, e2.value) != 0) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
                  
                | ^(PREFIX PLUS e1=expr)
                  {
                      if (e1.hasValue) {
                          $value = +e1.value;
                          $hasValue = true;
                      }
                  }

                | ^(PREFIX MINUS e1=expr)
                  {
                      if (e1.hasValue) {
                          $value = -e1.value;
                          $hasValue = true;
                      }
                  }
                               
                | ^(PREFIX NOT e1=expr)
                  {
                      // @todo check that we are dealing with a
                      // pseudo logical value here
                      if (e1.hasValue) {
                          $value = dzero(e1.value) ? 1d : 0d;
                          $hasValue = true;
                      }
                  }
              
                | POS_VAR
                | IMAGE_VAR
                | ^(NBR_REF IMAGE_VAR expr expr)
                | NON_LOCAL_VAR
                | IMAGE_POS_LOOKUP
                | IMAGE_INFO_LOOKUP
                
                | LOCAL_VAR
                  {
                      $value = varTable.get($LOCAL_VAR.text);
                      $hasValue = true;
                  }
                  
                | FIXED_VALUE 
                  {
                      $value = ((FixedValueNode)$FIXED_VALUE).getValue();
                      $hasValue = true;
                  }
                  
                | CONSTANT
                  {
                      $value = varTable.get($CONSTANT.text);
                      $hasValue = true;
                  }
                ;
                
                
expr_list returns [ List<Double> values ]
@init { 
    $values = CollectionFactory.newList(); 
}
                : ^(EXPR_LIST ( sub_expr 
                                {
                                    if ($values != null && $sub_expr.value != null) {
                                        $values.add($sub_expr.value);
                                    } else {
                                        $values = null;
                                    }
                                } 
                              )* )
                ;                
                
assignable_var  : POS_VAR
                | LOCAL_VAR
                | NON_LOCAL_VAR
                ;
                
non_assignable_var : IMAGE_VAR
                | IMAGE_POS_LOOKUP
                | IMAGE_INFO_LOOKUP
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

type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

