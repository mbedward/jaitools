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
  * Converts ternary expressions (a ? b : c) to IF_CALL nodes.
  *
  * @author Michael Bedward
  */

tree grammar ConvertTernaryExpr;

options {
    tokenVocab = VarTransformer;
    ASTLabelType = CommonTree;
    output = AST;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;
import java.util.Map;
import jaitools.CollectionFactory;

}

@members {
private ParsingErrorReporter errorReporter = null;

public void setErrorReporter( ParsingErrorReporter er ) {
    errorReporter = er;
}

public ParsingErrorReporter getErrorReporter() {
    return errorReporter;
}

@Override 
public void emitErrorMessage(String msg) {
    if (errorReporter != null) {
        errorReporter.addError(msg);
    } else {
        super.emitErrorMessage(msg);
    }
}

}  // end of @members

start           : statement+
                ;

statement       : image_write
                | var_assignment
                ;

image_write     : ^(IMAGE_WRITE IMAGE_VAR expr)
                ;

var_assignment  : ^(ASSIGN assign_op VAR expr)
                ;
                
expr            : ^(QUESTION expr expr expr) -> ^(IF_CALL ^(EXPR_LIST expr*))
                
                | ^(FUNC_CALL ID expr_list)
                | ^(IF_CALL expr_list)
                | ^(PREFIX unary_op expr)
                | ^(expr_op expr expr)
                | ^(BRACKETED_EXPR expr)
                | VAR 
                | IMAGE_VAR 
                | ^(NBR_REF IMAGE_VAR nbr_ref_expr nbr_ref_expr) 
                | FIXED_VALUE 
                ;

nbr_ref_expr    : ^(ABS_NBR_REF expr)
                | ^(REL_NBR_REF expr)
                ;
                
expr_list       : ^(EXPR_LIST (expr)*)
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
