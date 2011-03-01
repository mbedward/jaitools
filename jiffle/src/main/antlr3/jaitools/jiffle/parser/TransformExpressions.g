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

@members {

private boolean isProxy(String name) {
    try {
        return FunctionLookup.getInfo(name, null).isProxy(); 

    } catch (UndefinedFunctionException ex) {
        // If the function is not defined we let it pass here. It
        // will be picked up later by the function checking parser.
        return false;
    }
}

private String getRuntimeExpr(String name) {
    try {
        return FunctionLookup.getInfo(name, null).getRuntimeExpr();

    } catch (UndefinedFunctionException ex) {
        // getting here means a mistake in the grammar action code
        throw new RuntimeException(ex);
    }
}

}


start           : jiffleOption* varDeclaration* statement+
                ;


jiffleOption    : ^(JIFFLE_OPTION .+)
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL id=. expression)
                ;


block           : ^(BLOCK blockStatement*)
                ;


blockStatement  : statement
                | ^(BREAKIF expression)
                ;


statement       : block
                | assignmentExpression
                | ^(WHILE loopCondition statement)
                | ^(UNTIL loopCondition statement)
                | ^(FOREACH ID loopSet statement)
                | expression
                ;


loopCondition   : expression
                ;


loopSet         : ^(SEQUENCE expression expression)
                | listLiteral
                | VAR_LIST
                ;


expressionList returns [List<String> argTypes]
@init{ $argTypes = new ArrayList<String>(); }
                : ^(EXPR_LIST (e=expression
                    { 
                        $argTypes.add($e.start.getType() == VAR_LIST ? "List" : "D");
                    } )* )
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
                }
                  -> {isProxy($ID.text)}? VAR_PROVIDED[ getRuntimeExpr($ID.text) ]
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
                | listOperation
                | listLiteral
                | literal
                | identifier
                ;


listOperation   : ^(APPEND VAR_LIST expression)
                ;


listLiteral     : ^(DECLARED_LIST expressionList)
                ;


identifier      : VAR_SOURCE
                | VAR_DEST
                | VAR_IMAGE_SCOPE
                | VAR_PIXEL_SCOPE
                | VAR_LOOP
                | VAR_LIST
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
