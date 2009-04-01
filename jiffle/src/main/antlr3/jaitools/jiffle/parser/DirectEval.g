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
  * Grammar for a tree walker that performs direct evaluation of an 
  * AST from the token parser
  */

tree grammar DirectEval;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;
import java.util.HashMap;
import java.util.Random;
import jaitools.jiffle.runtime.FunctionTable;
import jaitools.jiffle.runtime.VarTable;
}

@members {
    private boolean printDebug = false;
    public void setPrint(boolean b) { printDebug = b; }

    VarTable varTable = new VarTable();
    FunctionTable fnTable = new FunctionTable();

    // comparison of doubles within set tolerance
    private final static double TOL=1.0e-8;  
    int deq(double d1, double d2) { return ((Math.abs(d1-d2) < TOL) ? 1 : 0); }

    private double result = 0.0d;
    public double getResult() { return result; }
}

prog            : statement+ ;

statement       : general_expr 
                  { 
                    result = $general_expr.value;
                    if (printDebug) System.out.println("" + result); 
                  }
                ;

expr_list returns [ List<Double> values ] :
                 { $values = new ArrayList<Double>(); }
                  ^(EXPR_LIST ( e=expr {$values.add($e.value);} )*)
                ;

general_expr returns [double value] :
                  expr { $value = $expr.value; }
                | bool_expr { $value = (double)$bool_expr.value; }
                ;

bool_expr returns [int value] :
                  ^(OR a=bool_expr b=bool_expr) {$value = (a!=0 || b!=0) ? 1 : 0;}
                | ^(AND a=bool_expr b=bool_expr) {$value = (a!=0 && b!=0) ? 1 : 0;}
                | ^(XOR a=bool_expr b=bool_expr) {$value = (a!=0 ^ b!=0) ? 1 : 0;}
                | ^(GT x=expr y=expr) {$value = x > y ? 1 : 0;}
                | ^(GE x=expr y=expr) {$value = x >= y ? 1 : 0;}
                | ^(LE x=expr y=expr) {$value = x <= y ? 1 : 0;}
                | ^(LT x=expr y=expr) {$value = x < y ? 1 : 0;}
                | ^(LOGICALEQ g=general_expr h=general_expr) {$value = deq(g, h);}
                | ^(NE g=general_expr h=general_expr) {$value = (deq(g, h) == 1 ? 0 : 1);}
                ;

expr returns [double value] : 
                  ^(FUNC_CALL ID expr_list) { $value = fnTable.invoke($ID.text, $expr_list.values); }

                | ^(ASSIGN assign_op ID a=expr)
                  { 
                    varTable.assign($ID.text, $assign_op.text, a); 
                    $value = a; 
                  }

                | ^(QUESTION t=bool_expr a=expr b=expr)
                  { $value = (($t.value != 0) ? $a.value : $b.value); }

                | ^(POW a=expr b=expr) {$value = Math.pow(a, b);}
                | ^(TIMES a=expr b=expr) {$value = a*b;}
                | ^(DIV a=expr b=expr) {$value = a/b;}
                | ^(MOD a=expr b=expr) {$value = a \% b;}  /* percent sign escaped for antlr */
                | ^(PLUS a=expr b=expr) {$value = a+b;} 
                | ^(MINUS a=expr b=expr) {$value = a-b;} 
                | ID {$value = varTable.get($ID.text);}
                | INT_LITERAL {$value = Double.valueOf($INT_LITERAL.text);}
                | FLOAT_LITERAL {$value = Double.valueOf($FLOAT_LITERAL.text);}
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
