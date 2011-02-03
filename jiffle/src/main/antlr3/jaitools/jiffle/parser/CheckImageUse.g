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
 * Checks for valid use of image variables in the primary AST.
 *
 * @author Michael Bedward
 */

tree grammar CheckImageUse;

options {
    ASTLabelType = CommonTree;
    tokenVocab = Jiffle;
    filter = true;
}

@header {
package jaitools.jiffle.parser;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;

}

@members {

private Map<String, Jiffle.ImageRole> imageParams;

private MessageTable msgTable;

public CheckImageUse(TreeNodeStream input, 
        Map<String, Jiffle.ImageRole> imageParams, 
        MessageTable msgTable) {
    this(input);
    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;

    if (imageParams == null) {
        this.imageParams = CollectionFactory.map();
    } else { 
        this.imageParams = imageParams;
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

private boolean isImage(String varName) {
    return imageParams.containsKey(varName);
}

}

topdown         : varInit
                | assignment
                ;

varInit         : ^(VAR_INIT ID expr?)
                { 
                    if (isImage($ID.text)) {
                        msgTable.add( $ID.text, Message.IMAGE_VAR_INIT_LHS );
                    }
                }
                ;

assignment      : ^(ASSIGN assign_op ID expr)
                { 
                    if (isSourceImage($ID.text)) {
                        msgTable.add($ID.text, Message.ASSIGNMENT_TO_SRC_IMAGE); 
                    } else if (isDestImage($ID.text) && !$assign_op.isEq) {
                        msgTable.add($ID.text, Message.INVALID_ASSIGNMENT_OP_WITH_DEST_IMAGE);
                    }
                }
                ;

nbr_ref         : ^(NBR_REF ID e=.+)
                ;

expr            : ^(FUNC_CALL ID expr_list)
                | ^(IF_CALL expr_list)
                | ^(expr_op expr expr)
                | ^(QUESTION expr expr expr)
                | ^(PREFIX . expr)
                | ^(BRACKETED_EXPR expr)
                | ID
                {
                    if (isDestImage($ID.text)) {
                        msgTable.add($ID.text, Message.READING_FROM_DEST_IMAGE);
                    }
                }
                ;

expr_list       : ^(EXPR_LIST (expr)*)
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

assign_op returns [ boolean isEq ]
@init { isEq = false; }
                : EQ { isEq = true; }
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;
		


