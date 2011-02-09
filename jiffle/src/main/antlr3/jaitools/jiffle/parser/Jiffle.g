/*
 * Copyright 2009-2011 Michael Bedward
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
 * Combined parser and lexer grammar to generate the primary AST
 * from an input Jiffle script.
 *
 * @author Michael Bedward
 */

grammar Jiffle;

options {
    output = AST;
    ASTLabelType = CommonTree;
}

tokens {
    VAR_INIT;
    JIFFLE_OPTION;

    ASSIGN;
    CAST;
    EXPR_LIST;
    BRACKETED_EXPR;
    FUNC_CALL;
    IF_CALL;

    IMAGE_POS;
    BAND_REF;
    PIXEL_REF;
    ABS_POS;
    REL_POS;

    PREFIX;
    POSTFIX;
    SIGN;

    // Used by later tree parsers
    CONSTANT;
    IMAGE_WRITE;
    VAR_DEST;
    VAR_SOURCE;
    VAR_IMAGE_SCOPE;
    VAR_PIXEL_SCOPE;
    VAR_PROVIDED;
}

@header {
package jaitools.jiffle.parser;
}

@lexer::header {
package jaitools.jiffle.parser;
}

@members {

private class UnexpectedInputException extends RuntimeException {
    UnexpectedInputException(String msg) {
        super(msg);
    }
}

@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    if (ttype == Token.EOF) {
        throw new UnexpectedInputException("Invalid statement before end of file");
    }
    return super.recoverFromMismatchedToken(input, ttype, follow);
}

}

@lexer::members {
    private boolean onCommentLine = false;
}


prog            : header_blocks? statement+ EOF!
                ;
                catch [UnexpectedInputException ex] {
                    throw new JiffleParserException(ex);
                }

header_blocks   : options_block var_init_block?
                | var_init_block options_block?
                ;

options_block   : OPTIONS LCURLY NEWLINE* option* RCURLY NEWLINE* -> option*
                ;

option          : key=ID EQ value=ID eos -> ^(JIFFLE_OPTION $key $value)
                ;

var_init_block  : INIT LCURLY NEWLINE* var_init* RCURLY NEWLINE* -> var_init*
                ;

var_init        : ID EQ expr eos -> ^(VAR_INIT ID expr)
                ;

statement       : expr eos!
                ;

expr            : assign_expr
                | incdec_expr
                | cond_expr
                ;

if_call         : 'if' LPAR expr_list RPAR -> ^(IF_CALL expr_list)
                ;

func_call       : ID LPAR expr_list RPAR -> ^(FUNC_CALL ID expr_list)

                /* special case for null() */
                | NULL LPAR expr_list RPAR -> ^(FUNC_CALL ID["null"] expr_list)
                ;

image_location
options {
    backtrack = true;
    memoize = true;
}
                : ID band_specifier pixel_specifier -> ^(IMAGE_POS ID band_specifier pixel_specifier)
                | ID pixel_specifier -> ^(IMAGE_POS ID pixel_specifier)
                | ID band_specifier -> ^(IMAGE_POS ID band_specifier)
                ;
                
pixel_specifier : LSQUARE pixel_pos ',' pixel_pos RSQUARE -> ^(PIXEL_REF pixel_pos pixel_pos)
                ;

band_specifier  : LSQUARE expr RSQUARE -> ^(BAND_REF expr)
                ;

pixel_pos       : ABS_POS_PREFIX expr -> ^(ABS_POS expr)
                | expr -> ^(REL_POS expr)
                ;

expr_list       : (expr (',' expr)* )? -> ^(EXPR_LIST expr*)
                ;

assign_expr     : ID assign_op expr -> ^(ASSIGN assign_op ID expr)
                ;

incdec_expr     : prefix_expr
                | postfix_expr
                ;

prefix_expr     : incdec_op ID -> ^(PREFIX incdec_op ID)
                ;

postfix_expr    : ID incdec_op -> ^(POSTFIX incdec_op ID)
                ;

cond_expr       : or_expr (QUESTION^ expr ':'! expr)?
                ;

or_expr         : xor_expr (OR^ xor_expr)*
                ;

xor_expr        : and_expr (XOR^ and_expr)*
                ;

and_expr        : eq_expr (AND^ eq_expr)*
                ;

eq_expr         : comp_expr ((LOGICALEQ^ | NE^) comp_expr)?
                ;

comp_expr       : add_expr ((GT^ | GE^ | LE^ | LT^) add_expr)?
                ;

add_expr        : mult_expr ((PLUS^ | MINUS^) mult_expr)*
                ;

mult_expr       : exp_expr ((TIMES^ | DIV^ | MOD^) exp_expr)*
                ;

exp_expr        : cast_expr (POW^ cast_expr)*
                ;

cast_expr       : LPAR type_name RPAR cast_expr -> ^(CAST cast_expr)
                | sign_expr
                ;

sign_expr       : sign_op atom_expr -> ^(SIGN sign_op atom_expr)
                | atom_expr
                ;

atom_expr       : ID
                | constant
                | bracketed_expr
                | func_call
                | if_call
                | image_location
                ;

bracketed_expr  : LPAR expr RPAR -> ^(BRACKETED_EXPR expr)
                ;
                
constant        : INT_LITERAL
                | FLOAT_LITERAL
                | TRUE
                | FALSE
                | NULL
                ;

incdec_op       : INCR
                | DECR
                ;

sign_op         : PLUS
                | MINUS
                | NOT
                ;

assign_op       : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;

type_name       : 'int'
                | 'float'
                | 'double'
                | 'boolean'
                ;
                
eos             : (SEMICOLON|NEWLINE)+
                ;

OPTIONS         : 'options'
                ;

INIT            : 'init'
                ;

BLOCK_COMMENT   : '/*' (~'*' | '*' ~'/')* '*/' 
                  { $channel = HIDDEN; onCommentLine = true; }
                ;

LINE_COMMENT    : '//' (~('\n' | '\r'))* 
                  { $channel = HIDDEN; onCommentLine = true; }
                ;
                

/* these named constants are defined using case-insensitive fragments
 * (see further down)
 */
TRUE            : T R U E
                ;
                
FALSE           : F A L S E
                ;
                
NULL            : N U L L
                ;

/* Operators sorted and grouped by precedence order */

ABS_POS_PREFIX  : '$'  ;

INCR            : '++' ;  /* pre-fix and post-fix operations */
DECR            : '--' ;

NOT             : '!' ;

POW             : '^' ;      

TIMES           : '*' ;
DIV             : '/' ;
MOD             : '%' ;

PLUS            : '+' ;
MINUS           : '-' ;

GT              : '>';
GE              : '>=';
LE              : '<=';
LT                  : '<';

LOGICALEQ       : '==';
NE              : '!=';

AND             : '&&';

OR              : '||';
XOR             : '^|';

QUESTION        : '?' ;  /* conditional operator ?: */

TIMESEQ         : '*=' ;
DIVEQ           : '/=' ;
MODEQ           : '%=' ;
PLUSEQ          : '+=' ;
MINUSEQ         : '-=' ;
EQ              : '='  ;


/* General symbols and token rules */
LPAR            : '(' ;
RPAR            : ')' ;
LSQUARE         : '[' ;
RSQUARE         : ']' ;
LCURLY          : '{' ;
RCURLY          : '}' ;

ID              : (Letter) (Letter | UNDERSCORE | Digit | Dot)*
                ;

fragment
Letter          : 'a'..'z' | 'A'..'Z'
                ;

UNDERSCORE      : '_' ;

INT_LITERAL     : '0' | NonZeroDigit Digit*
                ;

FLOAT_LITERAL   : ('0' | NonZeroDigit Digit*)? Dot Digit* FloatExp?
                ;

fragment
Digit           : '0'..'9'
                ;
                
fragment
Dot             : '.'
                ;

fragment
NonZeroDigit    : '1'..'9'
                ;

fragment
FloatExp        : ('e'|'E' (PLUS|MINUS)? '0'..'9'+)
                ;

SEMICOLON       : ';'
                ;

/* Mac: \r  PC: \r\n  Unix \n */
NEWLINE
@init {
    if (onCommentLine) {
        $channel = HIDDEN;
    }
}
@after {
    onCommentLine = false;
}
                : '\r' '\n'?
                | '\n'
                ;

/* Fragment tokens for selective case-insensitive matching */
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

WS   :  (' '|'\t'|'\u000C') {$channel=HIDDEN;} ;
