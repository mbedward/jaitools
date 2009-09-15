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
  * Grammar for the image calculator that evaluates the runtime AST
  * for each output image pixel
  *
  * @author Michael Bedward
  */

tree grammar ImageCalculator;

options {
    tokenVocab = Morph5;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;
import jaitools.jiffle.runtime.JiffleRunner;
import jaitools.utils.CollectionFactory;

import static jaitools.numeric.DoubleComparison.*;
}

@members {
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private JiffleRunner runner = null;

public void setRunner(JiffleRunner runner) {
    this.runner = runner;
}

}

start
@init {
    if (runner == null) {
        throw new RuntimeException("you must set the runner before calling start()");
    }
}
                : (statement)+
                ;

statement       : image_write
                | var_assignment
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR expr)
                   {
                       runner.writeToImage($IMAGE_VAR.text, $expr.value);
                   }
                ;

var_assignment  : ^(ASSIGN assign_op id=VAR expr)
                   {
                       runner.setVar($id.text, $assign_op.text, $expr.value);
                   }
                ;
                
expr returns [double value]
                : ^(FUNC_CALL ID expr_list)
                  {$value = runner.invokeFunction($ID.text, $expr_list.values);}
                  
                | ^(QUESTION econd=expr e1=expr e2=expr)
                  {
                      if (!dzero(econd)) {
                          $value = e1;
                      } else {
                          $value = e2;
                      }
                  }
                  
                | ^(POW e1=expr e2=expr) {$value = Math.pow(e1, e2);}
                | ^(TIMES e1=expr e2=expr) {$value = e1 * e2;}
                | ^(DIV e1=expr e2=expr) {$value = e1 / e2;}
                | ^(MOD e1=expr e2=expr) {$value = e1 \% e2;}
                | ^(PLUS e1=expr e2=expr) {$value = e1 + e2;}
                | ^(MINUS e1=expr e2=expr) {$value = e1 - e2;}
                | ^(OR e1=expr e2=expr) {$value = (!dzero(e1) || !dzero(e2)) ? 1 : 0;}
                | ^(AND e1=expr e2=expr) {$value = (!dzero(e1) && !dzero(e2)) ? 1 : 0;}
                | ^(XOR e1=expr e2=expr) {$value = (!dzero(e1) ^ !dzero(e2)) ? 1 : 0;}
                | ^(GT e1=expr e2=expr) {$value = (dcomp(e1, e2) > 0) ? 1 : 0;}
                | ^(GE e1=expr e2=expr) {$value = (dcomp(e1, e2) >= 0) ? 1 : 0;}
                | ^(LT e1=expr e2=expr) {$value = (dcomp(e1, e2) < 0) ? 1 : 0;}
                | ^(LE e1=expr e2=expr) {$value = (dcomp(e1, e2) <= 0) ? 1 : 0;}
                | ^(LOGICALEQ e1=expr e2=expr) {$value = (dcomp(e1, e2) == 0) ? 1 : 0;}
                | ^(NE e1=expr e2=expr) {$value = (dcomp(e1, e2) != 0) ? 1 : 0;}
                | ^(PREFIX PLUS e1=expr) {$value = +e1;}
                | ^(PREFIX MINUS e1=expr) {$value = -e1;}
                
                /* @todo check that the expr is boolean */
                | ^(PREFIX NOT e1=expr) {$value = dzero(e1) ? 1 : 0;}
              
                | VAR 
                  {$value = runner.getVar($VAR.text);}
              
                | IMAGE_VAR 
                  {$value = runner.getImageValue($IMAGE_VAR.text);}
              
                | ^(NBR_REF IMAGE_VAR e1=expr e2=expr) 
                  {
                      // e1 is x offset, e2 is y offset
                      $value = runner.getImageValue($IMAGE_VAR.text, e1, e2);
                  }
                  
                | FIXED_VALUE 
                  {$value = ((FixedValueNode)$FIXED_VALUE).getValue();}
                ;
                
                
expr_list returns [ List<Double> values ] :
                 { $values = CollectionFactory.newList(); }
                  ^(EXPR_LIST ( e=expr {$values.add($e.value);} )*)
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

