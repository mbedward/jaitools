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

parser grammar JiffleParser;

options {
    tokenVocab = JiffleLexer;
    output=AST;
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
    BLOCK;

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

@members {

@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    if (ttype == Token.EOF) {
        throw new UnexpectedInputException("Invalid statement before end of file");
    }
    return super.recoverFromMismatchedToken(input, ttype, follow);
}

}

prog            : options_block? var_init_block? statement+ EOF!
                ;
                catch [UnexpectedInputException ex] {
                    throw new JiffleParserException(ex);
                }
                catch [EarlyExitException ex] {
                    throw new JiffleParserException("Unexpected input at line " + ex.line);
                }

options_block   : OPTIONS LCURLY option* RCURLY -> option*
                ;

option          : key=ID EQ value=ID SEMI -> ^(JIFFLE_OPTION $key $value)
                ;

var_init_block  : INIT LCURLY var_init* RCURLY -> var_init*
                ;

var_init        : ID EQ expr SEMI -> ^(VAR_INIT ID expr)
                ;

statement       : expr SEMI!
                | loop
                | SEMI!
                ;

loop            : while_loop statement
                ;

while_loop      : WHILE^ bracketed_expr block
                ;

block           : LCURLY statement* RCURLY -> ^(BLOCK statement*)
                ;

expr            : assign_expr
                | incdec_expr
                | cond_expr
                ;

bracketed_expr  : LPAR expr RPAR -> ^(BRACKETED_EXPR expr)
                ;

if_call         : IF LPAR expr_list RPAR -> ^(IF_CALL expr_list)
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

pixel_specifier : LSQUARE pixel_pos COMMA pixel_pos RSQUARE -> ^(PIXEL_REF pixel_pos pixel_pos)
                ;

band_specifier  : LSQUARE expr RSQUARE -> ^(BAND_REF expr)
                ;

pixel_pos       : ABS_POS_PREFIX expr -> ^(ABS_POS expr)
                | expr -> ^(REL_POS expr)
                ;

expr_list       : (expr (COMMA expr)* )? -> ^(EXPR_LIST expr*)
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

cond_expr       : or_expr (QUESTION^ expr COLON! expr)?
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

type_name       : INT_TYPE
                | FLOAT_TYPE
                | DOUBLE_TYPE
                | BOOLEAN_TYPE
                ;
