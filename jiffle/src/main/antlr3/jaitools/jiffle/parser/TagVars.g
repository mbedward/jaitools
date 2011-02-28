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
  * Transforms tokens representing variables into specific token types.
  *
  * @author Michael Bedward
  */

tree grammar TagVars;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    output = AST;
    superClass = ErrorHandlingTreeParser;
}


@header {
package jaitools.jiffle.parser;

import java.util.Map;
import java.util.Stack;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
}


@members {

private Map<String, Jiffle.ImageRole> imageParams;
private MessageTable msgTable;

public TagVars( TreeNodeStream nodes, Map<String, Jiffle.ImageRole> params, MessageTable msgTable ) {
    this(nodes);

    if (params == null) {
        this.imageParams = CollectionFactory.map();
    } else {
        this.imageParams = params;
    }

    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;
}

private boolean isSourceImage( String varName ) {
    Jiffle.ImageRole role = imageParams.get( varName );
    return role == Jiffle.ImageRole.SOURCE;
}

private boolean isDestImage( String varName ) {
    Jiffle.ImageRole role = imageParams.get( varName );
    return role == Jiffle.ImageRole.DEST;
}

private SymbolScopeStack varScope = new SymbolScopeStack();


private String getReturnType( String funcName ) {
    try {
        return FunctionLookup.getReturnType(funcName);

    } catch (UndefinedFunctionException ex) {
        // We can't really let undefined function exceptions pass here
        throw new JiffleParserException(ex);
    }
}

}

start 
@init {
    varScope.addLevel("top");
}
                : jiffleOption* varDeclaration* statement+
                ;


jiffleOption    : ^(JIFFLE_OPTION ID optionValue)
                ;


optionValue     : identifier
                | literal
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL ID expression)
                {
                    String varName = $ID.text;

                    if (isSourceImage(varName) || isDestImage(varName)) {
                        msgTable.add( varName, Message.IMAGE_VAR_INIT_LHS );

                    } else {
                        varScope.addSymbol(varName, SymbolType.SCALAR, ScopeType.IMAGE);
                    }
                }
                  -> ^(IMAGE_SCOPE_VAR_DECL VAR_IMAGE_SCOPE[varName] expression)
                ;


block
@init {
    varScope.addLevel("block");
}
@after {
    varScope.dropLevel();
}
                : ^(BLOCK blockStatement*)
                ;


blockStatement  : statement
                | ^(BREAKIF expression)
                ;


statement       : block
                | assignmentExpression
                | listDeclaration
                | ^(WHILE loopCondition statement)
                | ^(UNTIL loopCondition statement)
                | foreachLoop
                | expression
                ;


foreachLoop     
@init {
    varScope.addLevel("foreach");
}
@after {
    varScope.dropLevel();
}
                : ^(FOREACH ID {varScope.addSymbol($ID.text, SymbolType.LOOP_VAR, ScopeType.PIXEL);} loopTarget statement)
                ;


loopCondition   : expression
                ;


loopTarget      : ^(SEQUENCE expression expression)
                | ^(DECLARED_LIST expressionList)
                ;


expressionList  : ^(EXPR_LIST expression*)
                ;


declaredList    : ^(DECLARED_LIST expressionList)
                ;


assignmentExpression
                : ^(assignmentOp identifier expression)
                { 
                    String varName = $identifier.start.getText();

                    SymbolType symbolType = null;
                    boolean ok = true;
                    if (!varScope.isDefined(varName)) {
                        if ($expression.rtnType.equals("List")) {
                            symbolType = SymbolType.LIST;
                        } else {
                            symbolType = SymbolType.SCALAR;
                        }
                        varScope.addSymbol(varName, symbolType, ScopeType.PIXEL);

                    } else {
                        symbolType = varScope.findSymbol(varName).getType();
                        if (symbolType == SymbolType.LOOP_VAR) {
                            msgTable.add(varName, Message.ASSIGNMENT_TO_LOOP_VAR);
                            ok = false;

                        } else if ($expression.rtnType.equals("List")) {
                            if (symbolType != SymbolType.LIST) {
                                msgTable.add(varName, Message.ASSIGNMENT_LIST_TO_SCALAR);
                                ok = false;
                            } else if ($assignmentOp.start.getType() != EQ) {
                                msgTable.add(varName + " " + $assignmentOp.start.getText(), 
                                        Message.INVALID_OPERATION_FOR_LIST);
                                ok = false;
                            }

                        } else {
                            if (symbolType != SymbolType.SCALAR) {
                                msgTable.add(varName, Message.ASSIGNMENT_SCALAR_TO_LIST);
                                ok = false;
                            }
                        }
                    }

                    if (!ok) throw new JiffleParserException("Cancelling script compilation");
                }

                  -> {isDestImage($identifier.text)}? ^(IMAGE_WRITE identifier expression)

                  -> {$expression.rtnType.equals("List")}? 
                     ^(EQ VAR_LIST[$identifier.start.getText()] expression)

                  -> ^(assignmentOp identifier expression)
                ;


listDeclaration : ^(EQ ID {varScope.addSymbol($ID.text, SymbolType.LIST, ScopeType.PIXEL);} declaredList)
                -> ^(LIST_NEW VAR_LIST[$ID.text] declaredList)
                ;

assignmentOp    : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


expression returns [String rtnType]
                : ^(FUNC_CALL ID args) { $rtnType = getReturnType($ID.text); }
                | listOperation { $rtnType = "List"; }
                | scalarExpression { $rtnType = "D"; }
                | identifier { $rtnType = $identifier.isList ? "List" : "D"; }
                ;


scalarExpression
                : ^(IF_CALL expressionList)
                | ^(QUESTION expression expression expression)
                | ^(IMAGE_POS identifier bandSpecifier? pixelSpecifier?)
                | ^(logicalOp expression expression)
                | ^(arithmeticOp expression expression)
                | ^(POW expression expression)
                | ^(PREFIX prefixOp expression)
                | ^(POSTFIX incdecOp expression)
                | ^(PAR expression)
                | literal
                ;


args            : expressionList
                | declaredList
                ;


listOperation   : ^(APPEND identifier expression)
                ;


identifier returns [boolean isList]
                : ID 
                { $isList = varScope.isDefined($ID.text, SymbolType.LIST); }
                  -> {isSourceImage($ID.text)}? VAR_SOURCE[$ID.text]
                  -> {isDestImage($ID.text)}? VAR_DEST[$ID.text]
                  -> {ConstantLookup.isDefined($ID.text)}? CONSTANT[$ID.text]
                  -> {varScope.isDefined($ID.text, SymbolType.LOOP_VAR)}? VAR_LOOP[$ID.text]
                  -> {$isList}? VAR_LIST[$ID.text]
                  -> {varScope.isDefined($ID.text, ScopeType.IMAGE)}? VAR_IMAGE_SCOPE[$ID.text]
                  -> VAR_PIXEL_SCOPE[$ID.text]
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
                | TRUE -> FLOAT_LITERAL["1.0"]
                | FALSE -> FLOAT_LITERAL["0.0"]
                | NULL -> CONSTANT["NaN"]
                ;
