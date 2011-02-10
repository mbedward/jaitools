lexer grammar OptionsLexer;

options {
    filter = true;
}

@header {
package jaitools.jiffle.parser;

import java.util.Map;
import jaitools.CollectionFactory;
}

@members {
private Map<String, String> optionsTable = CollectionFactory.map();

public Map<String, String> getOptions() { return optionsTable; }
}

OptionBlock
    : 'options' WS? '{' WS? (ID WS? '=' WS? VALUE WS? ';' {optionsTable.put($ID.text, $VALUE.text);} WS?)* WS? '}' 
    ;

fragment
ID      : ('a'..'z' | 'A'..'Z')+
        ;

fragment
VALUE   : ('a'..'z' | 'A'..'Z' | '0'..'9')+
        ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n'
    |   '/*' ( options {greedy=false;} : . )* '*/'
    ;

WS  : (' ' | '\t' | '\r' | '\n' | '\u000C')+
    ;
