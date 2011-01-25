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
  * Checks function calls.
  *
  * @author Michael Bedward
  */

tree grammar FunctionValidator;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    superClass = ErrorHandlingTreeParser;
}

@header {
package jaitools.jiffle.parser;

import java.util.Map;
import jaitools.CollectionFactory;
import jaitools.jiffle.parser.ErrorCode;
}

@members {
private FunctionLookup functionLookup = new FunctionLookup();

/* Table of function name : error code */
private Map<String, ErrorCode> errors = CollectionFactory.orderedMap();

public Map<String, ErrorCode> getErrors() {
    return errors;
}

public boolean hasError() {
    return !errors.isEmpty();
}

}

start           : (var_init_block)? statement+ 
                ;

var_init_block  : ^(VAR_INIT_BLOCK var_init_list)
                ;

var_init_list   : ^(VAR_INIT_LIST (var_init)*)
                ;

var_init        : ^(VAR_INIT ID expr)
                ;

statement       : expr
                ;

expr_list returns [int size]
@init{
    $size = 0;
}
                : ^(EXPR_LIST (expr { size++; } )*)
                ;

expr            : ^(ASSIGN assign_op ID expr)

                | ^(FUNC_CALL ID expr_list)
                  { 
                      if (!functionLookup.isDefined($ID.text, $expr_list.size)) {
                          errors.put($ID.text, ErrorCode.UNDEFINED_FUNCTION);
                      }
                  }

                | ^(IF_CALL expr_list)
                | ^(BRACKETED_EXPR expr)                  
                | ^(expr_op expr expr)
                | ^(QUESTION expr expr expr)
                | ^(PREFIX unary_op expr)
                | ID
                | ^(NBR_REF ID nbr_ref_expr nbr_ref_expr)
                | INT_LITERAL 
                | FLOAT_LITERAL 
                | constant
                ;

nbr_ref_expr    : ^(ABS_NBR_REF expr)
                | ^(REL_NBR_REF expr)
                ;

constant        : TRUE
                | FALSE
                | NULL
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

