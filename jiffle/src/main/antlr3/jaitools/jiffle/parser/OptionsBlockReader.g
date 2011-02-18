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
  *
  * @author Michael Bedward
  */

tree grammar OptionsBlockReader;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    filter = true;
}


@header {
package jaitools.jiffle.parser;

import java.util.Map;
import jaitools.CollectionFactory;
}


@members {

private MessageTable msgTable;
private Map<String, String> options = CollectionFactory.map();

public OptionsBlockReader( TreeNodeStream nodes, MessageTable msgTable ) {
    this(nodes);
    if (msgTable == null) {
        throw new IllegalArgumentException( "msgTable should not be null" );
    }
    this.msgTable = msgTable;
}

public Map<String, String> getOptions() {
    return options;
}

}


topdown         : jiffleOption
                ;


jiffleOption    : ^(JIFFLE_OPTION ID optionValue)
                { options.put($ID.text, $optionValue.str); }
                ;


optionValue returns [String str]
                : ID { $str = $ID.text; }
                | INT_LITERAL { $str = $INT_LITERAL.text; }
                ;
