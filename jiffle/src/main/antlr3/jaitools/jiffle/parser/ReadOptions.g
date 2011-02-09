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
 * Reads and checks script options that affect JiffleLexer and JiffleParser.
 *
 * @author Michael Bedward
 */

grammar ReadOptions;

@header {
package jaitools.jiffle.parser;

import java.util.Map;
import jaitools.CollectionFactory;
}

@lexer::header {
package jaitools.jiffle.parser;
}

@members {
private Map<String, String> options = CollectionFactory.map();

public Map<String, String> getOptions() { return options; }
}

start           : options_block ;

options_block   : OPTIONS LCURLY option* RCURLY
                ;

option          : WORD '=' value ';'
                  { options.put($WORD.text, $value.text); }
                ;

value           : WORD | INT
                ;

OPTIONS         : 'options'
                ;

WORD            : Letter+
                ;

INT             : Digit+
                ;

LCURLY          : '{' ;
RCURLY          : '}' ;
SEMI            : ';' ;

fragment
Letter          : 'a'..'z' | 'A'..'Z'
                ;

fragment
Digit           : '0'..'9'
                ;

WS   :  (' '|'\t'|'\r'|'\n'|'\u000C') {$channel=HIDDEN;} ;
