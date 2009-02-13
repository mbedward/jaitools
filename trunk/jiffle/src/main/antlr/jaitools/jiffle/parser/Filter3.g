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
  * Calcultates fixed expressions and stores them in the var table
  *
  * @author Michael Bedward
  */

tree grammar Filter3;

options {
    tokenVocab = Filter2;
    ASTLabelType = CommonTree;
    output = AST;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;
import jaitools.jiffle.collection.CollectionFactory;
import jaitools.jiffle.interpreter.FunctionTable;
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

private FunctionTable funcTable = new FunctionTable();

/*
 * Double value comparison within tolerance methods
 */
private static final double TOL = 1.0e-8;

private boolean dzero(double x) {
    return Math.abs(x) < TOL;
}

private int dcomp(double x1, double x2) {
    if (dzero(x1 - x2)) {
        return 0;
    } else {
        return Double.compare(x1, x2);
    }
}

}

start           : statement+ 
                ;

statement       : image_write
                | var_assignment
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR ^(FIXED_EXPR e=expr))
                  -> ^(IMAGE_WRITE IMAGE_VAR FIXED_VALUE<FixedValueNode>[$e.value])
                  
                | ^(IMAGE_WRITE IMAGE_VAR FIXED_VALUE)
                ;

var_assignment  : ^(ASSIGN op=assign_op SIMPLE_VAR ^(FIXED_EXPR e=expr))
                  { varTable.assign($SIMPLE_VAR.text, $op.tree.getText(), $e.value); }
                  ->  // discard node
                  
                | ^(ASSIGN assign_op POS_VAR ^(POS_EXPR expr))
                ;
                
expr returns [boolean isFixedValue, double value]
                : ^(FUNC_CALL ID args=expr_list) {$value = funcTable.invoke($ID.text, $expr_list.values);}
            
                | ^(QUESTION expr expr expr)
                
                | ^(POW e1=expr e2=expr) {$value = Math.pow(e1.value, e2.value);}
                | ^(TIMES e1=expr e2=expr) {$value = e1.value * e2.value;}
                | ^(DIV e1=expr e2=expr) {$value = e1.value / e2.value;}
                | ^(MOD e1=expr e2=expr) {$value = e1.value \% e2.value;}
                | ^(PLUS e1=expr e2=expr) {$value = e1.value + e2.value;}
                | ^(MINUS e1=expr e2=expr) {$value = e1.value - e2.value;}
                | ^(OR e1=expr e2=expr) {$value = (!dzero(e1.value) || !dzero(e2.value)) ? 1 : 0;}
                | ^(AND e1=expr e2=expr) {$value = (!dzero(e1.value) && !dzero(e2.value)) ? 1 : 0;}
                | ^(XOR e1=expr e2=expr) {$value = (!dzero(e1.value) ^ !dzero(e2.value)) ? 1 : 0;}
                | ^(GT e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) > 0) ? 1 : 0;}
                | ^(GE e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) >= 0) ? 1 : 0;}
                | ^(LT e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) < 0) ? 1 : 0;}
                | ^(LE e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) <= 0) ? 1 : 0;}
                | ^(LOGICALEQ e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) == 0) ? 1 : 0;}
                | ^(NE e1=expr e2=expr) {$value = (dcomp(e1.value, e2.value) != 0) ? 1 : 0;}
                | POS_VAR
                | IMAGE_VAR
                | SIMPLE_VAR {$value = varTable.get($SIMPLE_VAR.text);}
                
                | FIXED_VALUE {$isFixedValue = true; $value = getFixedValue($FIXED_VALUE);}
                ;
                
expr_list returns [List<Double> values]
@init{
    $values = CollectionFactory.newList();
}
                : ^(EXPR_LIST (e=expr {$values.add($e.value);})*)
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

