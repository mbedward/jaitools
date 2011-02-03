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

tree grammar CheckUninitVars;

options {
    ASTLabelType = CommonTree;
    tokenVocab = Jiffle;
    filter = true;
}

@header {
package jaitools.jiffle.parser;

import java.util.Set;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;

}

@members {

private MessageTable msgTable;
private Set<String> varsAssigned = CollectionFactory.set();

public CheckUninitVars(TreeNodeStream input, MessageTable msgTable) {
    this(input);
    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;
}

}

topdown         : var_assignment
                | var_use
                ;

var_assignment  : ^(ASSIGN assign_op=. var expr=.)
                  { varsAssigned.add($var.name); }

                | ^(VAR_INIT ID (expr=.)?)
                { if ($expr != null) varsAssigned.add($ID.text); }
                ;

var_use         : var
                  {
                    if (!varsAssigned.contains($var.name)) {
                        msgTable.add($var.name, Message.UNINIT_VAR);
                    }
                  }
                ;

var returns [String name]
                : VAR_IMAGE_SCOPE { $name = $VAR_IMAGE_SCOPE.text; }
                | VAR_PIXEL_SCOPE { $name = $VAR_PIXEL_SCOPE.text; }
                ;

