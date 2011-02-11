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
  * Converts calls to functions that are proxies for reference image
  * parameters (e.g. width(), height()) to VAR_PROVIDED tokens.
  *
  * @author Michael Bedward
  */

tree grammar TagProxyFunctions;

options {
    tokenVocab = JiffleParser;
    ASTLabelType = CommonTree;
    output = AST;
    filter = true;
}

@header {
package jaitools.jiffle.parser;
}


topdown         : function_call
                ;

function_call   
@init {
    FunctionInfo info = null;
}
                : ^(FUNC_CALL ID expr_list)
                  { 
                      try {
                          info = FunctionLookup.getInfo($ID.text, $expr_list.size);
                      } catch (UndefinedFunctionException ex) {
                          throw new IllegalStateException("Internal compiler error", ex);
                      }
                  }

                  -> {info.isProxy()}? VAR_PROVIDED[info.getRuntimeExpr()]
                  -> ^(FUNC_CALL ID expr_list)
                ;

expr_list returns [int size]
@init { $size = 0; }
                : ^(EXPR_LIST (e=. {$size++;})*)
                ;
