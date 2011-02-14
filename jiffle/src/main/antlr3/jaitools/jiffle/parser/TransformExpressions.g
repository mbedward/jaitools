/*
 * Copyright 2011 Michael Bedward
 * 
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
  
 /**
  * Converts calls to proxy functions into the associated variables and
  * converts ternary conditional expressions to if calls.
  *
  * @author Michael Bedward
  */

tree grammar TransformExpressions;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    output = AST;
    superClass = ErrorHandlingTreeParser;
}


@header {
package jaitools.jiffle.parser;
}


start           : jiffleOption* varDeclaration* statement+
                ;


jiffleOption    : ^(JIFFLE_OPTION .+)
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL id=. expression)
                ;


block           : ^(BLOCK statement*)
                ;


statement       : block
                | assignmentExpression
                | ^(WHILE loopCondition statement)
                | ^(UNTIL loopCondition statement)
                | expression
                ;


loopCondition   : expression
                ;


expressionList returns [int size]
@init { $size = 0; }
                : ^(EXPR_LIST (expression {$size++;} )* )
                ;


assignmentExpression
                : ^(assignmentOp identifier expression)
                ;


assignmentOp    : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


expression
                : ^(FUNC_CALL ID expressionList)
                { 
                    FunctionInfo info = null;
                    try {
                        info = FunctionLookup.getInfo($ID.text, $expressionList.size); 
                    } catch (UndefinedFunctionException ex) {
                        throw new RuntimeException("Internal parser error: undefined function in TagProxyFunctions");
                    }
                }
                  -> {info.isProxy()}? VAR_PROVIDED[ info.getRuntimeExpr() ]
                  -> ^(FUNC_CALL ID expressionList)
                    

                | ^(QUESTION e1=expression e2=expression e3=expression) 
                  -> ^(IF_CALL ^(EXPR_LIST $e1 $e2 $e3))


                | ^(IF_CALL expressionList)
                | ^(IMAGE_WRITE identifier expression)
                | ^(IMAGE_POS identifier bandSpecifier? pixelSpecifier?)
                | ^(logicalOp expression expression)
                | ^(arithmeticOp expression expression)
                | ^(POW expression expression)
                | ^(PREFIX prefixOp expression)
                | ^(POSTFIX incdecOp expression)
                | ^(PAR expression)
                | literal
                | identifier
                ;


identifier      : VAR_SOURCE
                | VAR_DEST
                | VAR_IMAGE_SCOPE
                | VAR_PIXEL_SCOPE
                | CONSTANT
                ;


logicalOp       : OR
                | XOR
                | AND
                | LOGICALEQ
                | NE
                | GT
                | GE
                | LT
                | LE
                ;


arithmeticOp    : PLUS
                | MINUS
                | TIMES
                | DIV
                | MOD
                ;


prefixOp        : PLUS
                | MINUS
                | NOT
                | incdecOp
                ;


incdecOp        : INCR
                | DECR
                ;


pixelSpecifier  : ^(PIXEL_REF pixelPos pixelPos)
                ;


bandSpecifier   : ^(BAND_REF expression)
                ;


pixelPos        : ^(ABS_POS expression)
                | ^(REL_POS expression)
                ;


literal         : INT_LITERAL
                | FLOAT_LITERAL
                | TRUE
                | FALSE
                | NULL
                ;
