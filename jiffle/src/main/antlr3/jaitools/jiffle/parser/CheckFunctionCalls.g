/*
 * Copyright 2009-2011 Michael Bedward
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
  * Checks function calls in the primary AST.
  *
  * @author Michael Bedward
  */

tree grammar CheckFunctionCalls;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    filter = true;
}

@header {
package jaitools.jiffle.parser;
}

@members {

private MessageTable msgTable;

public CheckFunctionCalls(TreeNodeStream input, MessageTable msgTable) {
    this(input);
    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;
}

}

topdown : functionCall
        ;


functionCall    : ^(FUNC_CALL ID expressionList)
                {
                    if (!FunctionLookup.isDefined($ID.text, $expressionList.argTypes)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append($ID.text);
                        sb.append("(");
                        int k = 0;
                        for (String s : $expressionList.argTypes) {
                            sb.append(s);
                            if (++k < $expressionList.argTypes.size()) {
                                sb.append(", ");
                            }
                        }
                        sb.append(")");
                        msgTable.add(sb.toString(), Message.UNDEFINED_FUNCTION);
                    }
                }
                ;


expressionList returns [List<String> argTypes]
@init{ $argTypes = new ArrayList<String>(); }
                : ^(EXPR_LIST (e=. 
                    { 
                        int ttype = $e.getToken().getType();
                        $argTypes.add(ttype == VAR_LIST || ttype == DECLARED_LIST ? "List" : "D");
                    } )* )
                ;

