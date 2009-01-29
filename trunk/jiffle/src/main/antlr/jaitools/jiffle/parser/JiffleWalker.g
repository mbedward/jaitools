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

    private static class GeneralValue {
        enum Type {DOUBLE, BOOLEAN};

        Object value;
        Type type;

        GeneralValue(double d) { value = Double.valueOf(d); type = Type.DOUBLE; }
        GeneralValue(boolean b) { value = Boolean.valueOf(b); type = Type.BOOLEAN; }

        boolean isNumber() { return type == Type.DOUBLE; }
        boolean isBoolean() { return type == Type.BOOLEAN; }

        double getAsDouble() { return ((Double)value).doubleValue(); }
        boolean getAsBoolean() { return (Boolean)value; }
        String getAsString() { return value.toString(); }

        boolean equals(GeneralValue other) {
            if (other != null) {
                if (type == other.type) {
                    if (isNumber()) return getAsDouble() == other.getAsDouble();
                    if (isBoolean()) return getAsBoolean() == other.getAsBoolean();
                }
            }
            return false;
        }
    }
}

jiffle          : statement+ ;

statement       : general_expr { 
                    System.out.println("" + $general_expr.value.getAsString()); }
                ;

expr_list returns [ List<Double> values ] :
                 { $values = new ArrayList<Double>(); }
                  ^(EXPR_LIST ( e=expr {$values.add($e.value);} )*)
                ;
                        
general_expr returns [GeneralValue value] :
                  expr { $value = new GeneralValue($expr.value); }
                | bool_expr { $value = new GeneralValue($bool_expr.value); }
                ;

bool_expr returns [boolean value] :
                  ^(OR a=bool_expr b=bool_expr) {$value = a || b;}
                  ^(AND a=bool_expr b=bool_expr) {$value = a && b;}
                  ^(XOR a=bool_expr b=bool_expr) {$value = a^b;}
                  ^(GT x=expr y=expr) {$value = x > y;}
                  ^(GE x=expr y=expr) {$value = x >= y;}
                  ^(LE x=expr y=expr) {$value = x <= y;}
                  ^(LT x=expr y=expr) {$value = x < y;}
                  ^(EQ g=general_expr h=general_expr) {$value = g.equals(h);}
                  ^(NE g=general_expr h=general_expr) {$value = !g.equals(h);}
                ;

expr returns [double value] : 
                  ^(FUNC_CALL ID expr_list) { $value = fnTable.invoke($ID.text, $expr_list.values); }

                | ^(ASSIGN assign_op ID a=expr)
                  { 
                    varTable.assign($ID.text, $assign_op.text, a); 
                    $value = a; 
                  }

                | ^('+' a=expr b=expr) {$value = a+b;} 
                | ^('-' a=expr b=expr) {$value = a-b;} 
                | ^('*' a=expr b=expr) {$value = a*b;}
                | ^('/' a=expr b=expr) {$value = a/b;}
                | ^('%' a=expr b=expr) {$value = a%b;}
                | ^('^' a=expr b=expr) {$value = Math.pow(a, b);}
                | ID {$value = varTable.get($ID.text);}
                | INT_LITERAL {$value = Double.valueOf($INT_LITERAL.text);}
                | FLOAT_LITERAL {$value = Double.valueOf($FLOAT_LITERAL.text);}
                ;

assign_op	: '='
		| '*='
		| '/='
		| '%='
		| '+='
		| '-='
		;
		
