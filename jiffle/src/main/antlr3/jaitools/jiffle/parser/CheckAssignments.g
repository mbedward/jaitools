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
 * Checks for valid use of image variables.
 *
 * @author Michael Bedward
 */

tree grammar CheckAssignments;

options {
    ASTLabelType = CommonTree;
    tokenVocab = Jiffle;
}

@header {
package jaitools.jiffle.parser;

import java.util.Set;
import jaitools.CollectionFactory;
}

@members {

private MessageTable msgTable;
private Set<String> initializedVars = CollectionFactory.set();

public CheckAssignments(TreeNodeStream input, MessageTable msgTable) {
    this(input);
    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;
}

}


start           : jiffleOption* varDeclaration* statement+
                ;


jiffleOption    : ^(JIFFLE_OPTION ID .)
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL VAR_IMAGE_SCOPE .)
                { 
                    initializedVars.add($VAR_IMAGE_SCOPE.text);
                }
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
                | ^(FOREACH ID loopTarget statement)
                | expression
                ;


loopCondition   : expression
                ;


loopTarget      : ^(SEQUENCE expression expression)
                | ^(DECLARED_LIST expressionList)
                ;


expressionList  : ^(EXPR_LIST expression*)
                ;


assignmentExpression
                : ^(assignmentOp identifier expression)
                { 
                    String varName = $identifier.start.getText();
                    int idtype = $identifier.start.getType();
                    switch (idtype) {
                        case CONSTANT:
                            msgTable.add(varName, Message.CONSTANT_LHS);
                            break;

                        case VAR_SOURCE:
                            msgTable.add(varName, Message.ASSIGNMENT_TO_SRC_IMAGE); 
                            break;

                        default:
                            if ($assignmentOp.start.getType() == EQ) {
                                initializedVars.add(varName);

                            } else {
                                switch (idtype) {
                                    case VAR_DEST:
                                        msgTable.add(varName, Message.INVALID_ASSIGNMENT_OP_WITH_DEST_IMAGE);
                                        break;

                                    default:
                                        if (!initializedVars.contains(varName)) {
                                            msgTable.add(varName, Message.UNINIT_VAR);
                                        }
                                }
                            }
                    }
                }
                ;


assignmentOp    : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


identifier      : imageVar
                | userVar
                | CONSTANT
                ;


imageVar        : VAR_SOURCE
                | VAR_DEST
                | VAR_LOOP
                ;

userVar         : VAR_IMAGE_SCOPE
                | VAR_PIXEL_SCOPE
                ;


expression      : ^(FUNC_CALL ID expressionList)
                | ^(IF_CALL expressionList)
                | ^(QUESTION expression expression expression)
                | ^(IMAGE_WRITE . expression)
                | ^(IMAGE_POS . bandSpecifier? pixelSpecifier?)
                | ^(logicalOp expression expression)
                | ^(arithmeticOp expression expression)
                | ^(POW expression expression)
                | ^(PREFIX prefixOp expression)
                | ^(POSTFIX incdecOp expression)
                | ^(PAR expression)
                | literal
                | imageVar
                | CONSTANT

                | userVar
                {
                    String varName = $userVar.start.getText();
                    int varType = $userVar.start.getType();
                    if (varType != VAR_LOOP && !initializedVars.contains(varName)) {
                        msgTable.add(varName, Message.UNINIT_VAR);
                    }
                }
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
