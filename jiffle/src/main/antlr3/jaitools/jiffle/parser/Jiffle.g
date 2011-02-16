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
 * Jiffle language parser grammar. Generates the primary AST
 * from an input Jiffle script.
 *
 * @author Michael Bedward
 */

grammar Jiffle;

options {
    output=AST;
    ASTLabelType = CommonTree;
}

tokens {
    JIFFLE_OPTION;
    IMAGE_SCOPE_VAR_DECL;
    EXPR_LIST;
    DECLARED_LIST;
    PAR;
    FUNC_CALL;
    IF_CALL;
    BLOCK;
    IMAGE_POS;
    BAND_REF;
    PIXEL_REF;
    ABS_POS;
    REL_POS;
    PREFIX;
    POSTFIX;
    SEQUENCE;

    // Used by later tree parsers
    CONSTANT;
    IMAGE_WRITE;
    VAR_DEST;
    VAR_SOURCE;
    VAR_IMAGE_SCOPE;
    VAR_PIXEL_SCOPE;
    VAR_PROVIDED;
    VAR_LOOP;

}

@header {
package jaitools.jiffle.parser;
}

@lexer::header {
package jaitools.jiffle.parser;
}

@members {

@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    if (ttype == Token.EOF) {
        throw new UnexpectedInputException("Invalid statement before end of file");
    }
    return super.recoverFromMismatchedToken(input, ttype, follow);
}

}

prog            : optionsBlock? initBlock? statement+ EOF!
                ;
                catch [UnexpectedInputException ex] {
                    throw new JiffleParserException(ex);
                }
                catch [EarlyExitException ex] {
                    throw new JiffleParserException("Unexpected input at line " + ex.line);
                }


optionsBlock    : OPTIONS LCURLY option* RCURLY -> option*
                ;

option          : ID EQ optionValue SEMI -> ^(JIFFLE_OPTION ID optionValue)
                ;


optionValue     : ID
                | INT_LITERAL
                ;


initBlock       : INIT LCURLY varDeclaration* RCURLY -> varDeclaration*
                ;


varDeclaration  : ID EQ expression SEMI -> ^(IMAGE_SCOPE_VAR_DECL ID expression)
                ;


block           : LCURLY statement* RCURLY -> ^(BLOCK statement*)
                ;


statement       : block
                | delimitedStatement SEMI!
                | assignmentExpression SEMI!
                | WHILE LPAR loopCondition RPAR statement -> ^(WHILE loopCondition statement)
                | UNTIL LPAR loopCondition RPAR statement -> ^(UNTIL loopCondition statement)
                | FOREACH LPAR ID IN loopSet RPAR statement -> ^(FOREACH ID loopSet statement)
                | SEMI!
                ;


delimitedStatement
                : expression
                ;


loopCondition   : orExpression
                ;


/* 
 * A foreach block can have the following forms:
 * 
 * Integer sequence: e.g.  foreach (i in -1..1) { ...
 *
 * Expression list: e.g.   foreach (i in {x(), y(), 10.0, sin(z)}) { ...
 *
 */
loopSet         : sequence
                | declaredList
                ;


expressionList  : (expression (COMMA expression)* )? -> ^(EXPR_LIST expression*)
                ;


sequence        : lo=expression COLON hi=expression -> ^(SEQUENCE $lo $hi)
                ;


declaredList    : LCURLY expressionList RCURLY -> ^(DECLARED_LIST expressionList)
                ;


/*
 * If statements are function calls in Jiffle. This form is inherited
 * from the r.mapcalc language. They are treated separately from general
 * functions during compilation because we want to ensure lazy evaluation
 * of the alternatives.
 */
ifCall          : IF LPAR expressionList RPAR -> ^(IF_CALL expressionList)
                ;


/*
 * Assignment expressions are treated as a special case, outside of the general
 * expression rule hierarchy. This allows ANTLR to cope with the grammar without
 * requiring backtracking.
 */
assignmentExpression
                : ID assignmentOp^ expression 
                ;


expression      : conditionalExpression
                ;


assignmentOp    : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


conditionalExpression
                : orExpression (QUESTION^ expression COLON! expression)?
                ;


orExpression    : xorExpression (OR^ xorExpression)*
                ;


xorExpression   : andExpression (XOR^ andExpression)*
                ;


andExpression   : eqExpression (AND^ eqExpression)*
                ;


eqExpression    : compExpression ((LOGICALEQ^ | NE^) compExpression)?
                ;


compExpression  : addExpression ((GT^ | GE^ | LE^ | LT^) addExpression)?
                ;


addExpression   : multExpression ((PLUS^ | MINUS^) multExpression)*
                ;


multExpression  : unaryExpression ((TIMES^ | DIV^ | MOD^) unaryExpression)*
                ;


unaryExpression : prefixOp unaryExpression -> ^(PREFIX prefixOp unaryExpression)
                | powerExpression
                ;


prefixOp        : PLUS
                | MINUS
                | NOT
                | incdecOp
                ;


incdecOp        : INCR
                | DECR
                ;


powerExpression : primaryExpression (POW^ primaryExpression)*
                ;


primaryExpression
@init { boolean postfix = false; }
                : atom (incdecOp { postfix = true; } )?
                  -> {postfix}? ^(POSTFIX incdecOp atom)
                  -> atom
                ;


atom            : LPAR expression RPAR -> ^(PAR expression)
                | literal
                | ifCall
                | identifiedAtom
                ;


identifiedAtom  : ID arguments -> ^(FUNC_CALL ID arguments)
                | ID imagePos -> ^(IMAGE_POS ID imagePos)
                | ID
                ;


arguments       : LPAR! expressionList RPAR!
                ;


imagePos
options { backtrack=true; memoize=true; }
                : bandSpecifier pixelSpecifier
                | pixelSpecifier
                | bandSpecifier
                ;


pixelSpecifier  : LSQUARE pixelPos COMMA pixelPos RSQUARE -> ^(PIXEL_REF pixelPos pixelPos)
                ;


bandSpecifier   : LSQUARE expression RSQUARE -> ^(BAND_REF expression)
                ;


pixelPos        : ABS_POS_PREFIX expression -> ^(ABS_POS expression)
                | expression -> ^(REL_POS expression)
                ;


literal         : INT_LITERAL
                | FLOAT_LITERAL
                | TRUE
                | FALSE
                | NULL
                ;


/////////////////////////////////////////////////
// Lexer rules
/////////////////////////////////////////////////

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

/* Logical constants */
TRUE    : 'TRUE' | 'true' ;
FALSE   : 'FALSE' | 'false' ;
NULL    : 'NULL' | 'null' ;

/* Keywords */
INT_TYPE        : 'int' ;
FLOAT_TYPE      : 'float' ;
DOUBLE_TYPE     : 'double' ;
BOOLEAN_TYPE    : 'boolean' ;

OPTIONS : 'options' ;
INIT    : 'init' ;

IF      : 'if' ;
WHILE   : 'while' ;
UNTIL   : 'until' ;
FOREACH : 'foreach' ;
IN      : 'in' ;

/* Operators sorted and grouped by precedence order */

ABS_POS_PREFIX
        : '$'  ;

INCR    : '++' ;
DECR    : '--' ;

NOT     : '!' ;
POW     : '^' ;
TIMES   : '*' ;
DIV     : '/' ;
MOD     : '%' ;
PLUS    : '+' ;
MINUS   : '-' ;
GT      : '>';
GE      : '>=';
LE      : '<=';
LT      : '<';
LOGICALEQ : '==';
NE      : '!=';
AND     : '&&';
OR      : '||';
XOR     : '^|';
QUESTION: '?' ;  /* ternary conditional operator ?: */
TIMESEQ : '*=' ;
DIVEQ   : '/=' ;
MODEQ   : '%=' ;
PLUSEQ  : '+=' ;
MINUSEQ : '-=' ;
EQ      : '='  ;

/* General tokens */
COMMA   : ',' ;
SEMI    : ';' ;
COLON   : ':' ;
LPAR    : '(' ;
RPAR    : ')' ;
LSQUARE : '[' ;
RSQUARE : ']' ;
LCURLY  : '{' ;
RCURLY  : '}' ;

ID      : (Letter) (Letter | UNDERSCORE | Digit | Dot)*
        ;


fragment
Letter  : 'a'..'z' | 'A'..'Z'
        ;

UNDERSCORE
        : '_' ;

INT_LITERAL
        : '0' | NonZeroDigit Digit*
        ;

FLOAT_LITERAL
        : ('0' | NonZeroDigit Digit*)? Dot Digit* FloatExp?
        ;

fragment
Digit   : '0'..'9' ;

fragment
Dot     : '.' ;

fragment
NonZeroDigit
        : '1'..'9'
        ;

fragment
FloatExp
        : ('e'|'E' (PLUS|MINUS)? '0'..'9'+)
        ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        | '\u000C'
        ) {$channel=HIDDEN;}
    ;


/* 
 * The following are for future use 
 */

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
