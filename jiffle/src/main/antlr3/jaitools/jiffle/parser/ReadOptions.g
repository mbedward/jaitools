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

}


start
    :    (options_block | general_statement)+ EOF
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

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
