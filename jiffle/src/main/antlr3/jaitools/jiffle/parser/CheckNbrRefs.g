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
 * Checks for valid use neighbourhood references.
 *
 * @author Michael Bedward
 */

tree grammar CheckNbrRefs;

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

public CheckNbrRefs(TreeNodeStream input, 
        Map<String, Jiffle.ImageRole> imageParams, MessageTable msgTable) {
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

}

topdown         : nbr_ref
                ;

nbr_ref         : ^(NBR_REF ID .*)
                {
                    if (!isSourceImage($ID.text)) {
                        if (isDestImage($ID.text)) {
                            msgTable.add( $ID.text, Message.NBR_REF_ON_DEST_IMAGE_VAR );
                        } else {
                            msgTable.add( $ID.text, Message.NBR_REF_ON_NON_IMAGE_VAR );
                        }
                    }
                }
                ;
