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
    tokenVocab = JiffleParser;
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

topdown : function_call
        ;


function_call   : ^(FUNC_CALL ID expr_list)
                  { 
                      if (!FunctionLookup.isDefined($ID.text, $expr_list.size)) {
                          msgTable.add($ID.text, Message.UNDEFINED_FUNCTION);
                      }
                  }
                ;

expr_list returns [int size]
@init{ $size = 0; }
                : ^(EXPR_LIST (. {$size++;})*)
                ;
