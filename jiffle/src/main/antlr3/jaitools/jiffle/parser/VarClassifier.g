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
  * Grammar for VarClassifier. 
  * 
  * Takes the AST produced by the Jiffle parser and checks for errors
  * with variables (e.g. use before initial assignment).
  *
  * @author Michael Bedward
  */

tree grammar VarClassifier;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    superClass = ErrorHandlingTreeParser;
}

@header {
package jaitools.jiffle.parser;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
}

@members {

private Map<String, Jiffle.ImageRole> imageParams;

public void setImageParams( Map<String, Jiffle.ImageRole> imageParams ) {
    this.imageParams = imageParams;
}

private Set<String> varsAll = CollectionFactory.set();
private Set<String> varsAssignedTo = CollectionFactory.set();

private Map<String, ErrorCode> errors = CollectionFactory.orderedMap();

public Map<String, ErrorCode> getErrors() {
    return errors;
}

/*
 * This method is run after variables have been collected from the AST
 */
private void postCheck() {
    for (String varName : imageParams.keySet()) {
        if (!varsAll.contains(varName)) {
            errors.put(varName, ErrorCode.IMAGE_NOT_USED);
        } 
    }
}

/*
 * The following Exception class and overridden method are to
 * exit parsing early.
 */

private static class NbrRefException extends RecognitionException {
}

@Override
public void reportError(RecognitionException e) {
    if (e instanceof NbrRefException) {
        throw new CompilerExitException();
    }

    super.reportError(e);
}
    

}

start
@init {
    if (imageParams == null) {
        throw new IllegalStateException("Internal compiler error: image params not set");
    }
}
@after {
    postCheck();
}
                : (var_init_block)? statement+ 
                ;

var_init_block  : ^(VAR_INIT_BLOCK var_init_list)
                ;

var_init_list   : ^(VAR_INIT_LIST (var_init)*)
                ;

var_init        : ^(VAR_INIT ID expr)
                ;

statement       : assignment
                | expr
                ;

assignment      : ^(ASSIGN assign_op ID expr) 
                  { 
                      varsAll.add($ID.text);

                      Jiffle.ImageRole role = imageParams.get($ID.text);
                      if (role != null) {
                          if (role != Jiffle.ImageRole.DEST) {
                              errors.put($ID.text, ErrorCode.ASSIGNMENT_TO_SRC_IMAGE);
                          }
                      } else {
                          varsAssignedTo.add($ID.text);
                      }

                  }
                ;

expr            : ^(NBR_REF ID nbr_ref_expr nbr_ref_expr)
                  { 
                      Jiffle.ImageRole role = imageParams.get($ID.text);
                      if (role == null) {
                          errors.put($ID.text, ErrorCode.NBR_REF_ON_NON_IMAGE_VAR);
                      } else if (role == Jiffle.ImageRole.DEST) {
                          errors.put($ID.text, ErrorCode.NBR_REF_ON_DEST_IMAGE_VAR);

                          // give up at this point
                          throw new NbrRefException();
                      }
                  }

                | ID
                  { 
                      varsAll.add($ID.text);

                      if (!(varsAssignedTo.contains($ID.text)  ||
                            ConstantLookup.isDefined($ID.text))) {
                          if (imageParams.containsKey($ID.text)) {
                              if (imageParams.get($ID.text) == Jiffle.ImageRole.DEST) {
                                  errors.put($ID.text, ErrorCode.READING_FROM_DEST_IMAGE);
                              }
                          } else {
                              errors.put($ID.text, ErrorCode.UNINIT_VAR);
                          }
                      }
                  }

                | ^(FUNC_CALL ID expr_list)
                | ^(IF_CALL expr_list)
                | ^(expr_op expr expr)
                | ^(QUESTION expr expr expr)
                | ^(PREFIX unary_op expr)
                | ^(BRACKETED_EXPR expr)
                | INT_LITERAL 
                | FLOAT_LITERAL 
                | constant
                ;

expr_list       : ^(EXPR_LIST (expr)*)
                ;

nbr_ref_expr    : ^(ABS_NBR_REF expr)
                | ^(REL_NBR_REF expr)
                ;

constant        : TRUE
                | FALSE
                | NULL
                ;

expr_op         : POW
                | TIMES 
                | DIV 
                | MOD
                | PLUS  
                | MINUS
                | OR 
                | AND 
                | XOR 
                | GT 
                | GE 
                | LE 
                | LT 
                | LOGICALEQ 
                | NE 
                ;

assign_op	: EQ
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;
		
incdec_op       : INCR
                | DECR
                ;

unary_op	: PLUS
		| MINUS
		| NOT
		;
		
type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

