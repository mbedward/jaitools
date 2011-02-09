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
 * Reads the options block in a Jiffle script.
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
private Map<String, String> optionsTable = CollectionFactory.map();

public Map<String, String> getOptions() {
    return optionsTable;
}

private boolean readOptionsBlock = false;

}


start
    :    options_block general_statement+ EOF
    ;

general_statement
    :    ~(OPTIONS) ( options { greedy = false; } : . )* (SEMI | RCURLY)
    ;

options_block
    :    OPTIONS LCURLY option* RCURLY
    ;

option
    :    ID EQ value SEMI { optionsTable.put($ID.text, $value.text); }
    ;

value
    :    ID | INT
    ;

OPTIONS :   'options' ;
    
LCURLY     :   '{' ;
RCURLY  :   '}' ;
SEMI    :   ';' ;
EQ      :   '=' ;

ID  :    ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :    '0'..'9'+
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;
