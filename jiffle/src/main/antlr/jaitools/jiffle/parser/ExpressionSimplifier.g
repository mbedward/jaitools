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
  *
  * @author Michael Bedward
  */

tree grammar ExpressionSimplifier;

options {
    tokenVocab = TreeRebuilder;
    ASTLabelType = CommonTree;
    output = AST;
}

tokens {
    SIMPLE_EXPR;
}

@header {
package jaitools.jiffle.parser;
}

@members {
public String getErrorMessage(RecognitionException e, String[] tokenNames) 
{ 
    List stack = getRuleInvocationStack(e, this.getClass().getName()); 
    String msg = null; 
    if ( e instanceof NoViableAltException ) { 
        NoViableAltException nvae = (NoViableAltException)e; 
        msg = " no viable alt; token="+e.token+ 
        " (decision="+nvae.decisionNumber+ 
        " state "+nvae.stateNumber+")"+ 
        " decision=<<"+nvae.grammarDecisionDescription+">>"; 
    } 
    else { 
        msg = super.getErrorMessage(e, tokenNames); 
    } 
    return stack+" "+msg; 
} 

public String getTokenErrorDisplay(Token t) { 
    return t.toString(); 
} 
    
    
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private int exprId = 0;
}

start           : statement+ 
                ;

statement
@init {
    {System.out.println("statement");}
}
                : image_write
                | var_assignment
                ;

image_write     : {System.out.println("image_write");} ^(IMAGE_WRITE term term)
                ;

var_assignment  : {System.out.println("var assignment");} ^(ASSIGN assign_op term term)
                ;
                
term
scope {
    boolean positional;
    boolean calculated;
}
@init {
    $term::positional = false;
    $term::calculated = false;
}
                : expr
                  -> {$term::calculated && !$term::positional}? ^(SIMPLE_EXPR[String.valueOf(++exprId)] expr)
                  -> expr
                ;

expr            : calc_expr {$term::calculated = true;}
                | var
                | INT_LITERAL 
                | FLOAT_LITERAL 
                ;
                
calc_expr       : ^(FUNC_CALL ID expr_list)
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
                ;
                
expr_list       : ^(EXPR_LIST expr*)
                ;
                
var             : POS_VAR {$term::positional = true;}
                | SIMPLE_VAR
                | IMAGE_VAR
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

