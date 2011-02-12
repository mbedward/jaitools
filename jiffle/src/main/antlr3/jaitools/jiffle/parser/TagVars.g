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
  * <p>
  * Image variable ID tokens, other than those in neighbourhood reference nodes, are
  * transformed to either VAR_DEST or VAR_SOURCE tokens.
  * <p>
  * Non-image variables are transformed to VAR_IMAGE_SCOPE or VAR_PIXEL_SCOPE
  * tokens, with the former being variables declared in an init block.
  *
  * @author Michael Bedward
  */

tree grammar TagVars;

options {
    tokenVocab = JiffleParser;
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

block           : ^(BLOCK statement*)
                ;

expr_list       : ^(EXPR_LIST expr*)
                ;

expr            : ^(ASSIGN assign_op ID expr)
                  -> {isDestImage($ID.text)}? ^(IMAGE_WRITE VAR_DEST[$ID.text] expr)
                  -> {imageScopeVars.contains($ID.text)}? ^(ASSIGN assign_op VAR_IMAGE_SCOPE[$ID.text] expr)
                  -> ^(ASSIGN assign_op VAR_PIXEL_SCOPE[$ID.text] expr)
                  
                | ^(FUNC_CALL ID expr_list)
                  
                | ^(IMAGE_POS ID band_specifier? pixel_specifier?)
                  -> ^(IMAGE_POS VAR_SOURCE[$ID.text] band_specifier? pixel_specifier?)
                  
                | ^(IF_CALL expr_list)
                | ^(WHILE expr block?)
                | ^(QUESTION expr expr expr)

                | ^(SIGN sign_op expr)
                | ^(PREFIX incdec_op expr)
                | ^(POSTFIX incdec_op expr)

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

band_specifier  : ^(BAND_REF expr)
                ;

pixel_specifier : ^(PIXEL_REF x=pixel_pos y=pixel_pos)
                ;

pixel_pos       : ^(ABS_POS expr)
                | ^(REL_POS expr)
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

assign_op       : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;

sign_op         : PLUS
                | MINUS
                | NOT
                ;

incdec_op       : INCR
                | DECR
                ;

type_name       : 'int'
                | 'float'
                | 'double'
                | 'boolean'
                ;

