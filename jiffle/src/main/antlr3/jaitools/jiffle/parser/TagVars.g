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
import java.util.Set;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
}

@members {

private Map<String, Jiffle.ImageRole> imageParams;
private Set<String> imageScopeVars = CollectionFactory.set();

public TagVars( TreeNodeStream nodes, Map<String, Jiffle.ImageRole> params ) {
    this(nodes);
    if (params == null) {
        this.imageParams = CollectionFactory.map();
    } else {
        this.imageParams = params;
    }
}

private boolean isSourceImage( String varName ) {
    Jiffle.ImageRole role = imageParams.get( varName );
    return role == Jiffle.ImageRole.SOURCE;
}

private boolean isDestImage( String varName ) {
    Jiffle.ImageRole role = imageParams.get( varName );
    return role == Jiffle.ImageRole.DEST;
}

}

start           : var_init* statement+ 
                ;

var_init        : ^(VAR_INIT ID {imageScopeVars.add($ID.text);} expr?)
                ;

statement       : expr
                ;

expr_list       : ^(EXPR_LIST expr*)
                ;

expr            : ^(ASSIGN assign_op ID expr)
                  -> {isDestImage($ID.text)}? ^(IMAGE_WRITE VAR_DEST[$ID.text] expr)
                  -> {imageScopeVars.contains($ID.text)}? ^(ASSIGN assign_op VAR_IMAGE_SCOPE[$ID.text] expr)
                  -> ^(ASSIGN assign_op VAR_PIXEL_SCOPE[$ID.text] expr)
                  
                | ^(FUNC_CALL ID expr_list)
                  
                | ^(NBR_REF ID nbr_ref_expr nbr_ref_expr) 
                  -> ^(NBR_REF VAR_SOURCE[$ID.text] nbr_ref_expr nbr_ref_expr)
                  
                | ^(IF_CALL expr_list)
                | ^(QUESTION expr expr expr)
                | ^(PREFIX unary_op expr)
                | ^(expr_op expr expr)
                | ^(BRACKETED_EXPR expr)
                | CONSTANT
                | VAR_PROVIDED

                | ID
                  -> {isSourceImage($ID.text)}? VAR_SOURCE[$ID.text]
                  -> {imageScopeVars.contains($ID.text)}? VAR_IMAGE_SCOPE[$ID.text]
                  -> VAR_PIXEL_SCOPE[$ID.text]

                | INT_LITERAL
                | FLOAT_LITERAL
                ;

nbr_ref_expr    : ^(ABS_NBR_REF expr)
                | ^(REL_NBR_REF expr)
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

