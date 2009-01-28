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
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

tree grammar JiffleWalker;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;
import java.util.HashMap;
import java.util.Random;
}

@members {
    VarTable varTable = new VarTable();
    FunctionTable fnTable = new FunctionTable();
}

jiffle          : statement+ ;

statement       : expr { System.out.println("" + $expr.value); }
                ;

expr_list returns [List<Double> values] : ^(EXPR_LIST expr+) 
                  { List<Double> vlist = new ArrayList<Double>();
                    int n = $EXPR_LIST.getChildCount();
                    for (int i = 0; i < n; i++) {
                        vlist.add($expr.value);
                    }
                    $values = vlist;
                  }
                ;
                        
general_expr returns [Object value] :
                  expr { $value = Double.valueOf($expr.value); }
                | bool_expr { $value = Boolean.valueOf($bool_expr.value); }
                ;

bool_expr returns [boolean value] :
                  ^(OR a=bool_expr b=bool_expr) {$value = a || b;}
                  ^(AND a=bool_expr b=bool_expr) {$value = a && b;}
                  ^(XOR a=bool_expr b=bool_expr) {$value = a^b;}
                  ^(GT x=expr y=expr) {$value = x > y;}
                  ^(GE x=expr y=expr) {$value = x >= y;}
                  ^(LE x=expr y=expr) {$value = x <= y;}
                  ^(LT x=expr y=expr) {$value = x < y;}
                  ^(EQ g=general_expr h=general_expr) {$value = g == h;}
                  ^(NE g=general_expr h=general_expr) {$value = g != h;}
                ;

expr returns [double value] : 
                  ^(FUNC ID expr_list) { fnTable.invoke($ID.text, $expr_list.values); }

                | ^(assign_op ID a=expr) 
                  { varTable.assign($ID.text, $assign_op.text, a); $value = a; }

                | ^(COND yes=general_expr no=general_expr) test=bool_expr { }

                | ^('+' a=expr b=expr) {$value = a+b;} 
                | ^('-' a=expr b=expr) {$value = a-b;} 
                | ^('*' a=expr b=expr) {$value = a*b;}
                | ^('/' a=expr b=expr) {$value = a/b;}
                | ^('%' a=expr b=expr) {$value = a%b;}
                | ^('^' a=expr b=expr) {$value = Math.pow(a, b);}
                
                ;

assign_op	: '='
		| '*='
		| '/='
		| '%='
		| '+='
		| '-='
		;
		
