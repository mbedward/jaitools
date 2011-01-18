/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

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

/*
 * Recording of user variables and checking that they are
 * assigned a value before being used in an expression.
 * Use of an unsassigned variable is not necessarily an error
 * as it might (should) be an input image variable.
 */
private Set<String> userVars = CollectionFactory.set();

public Set<String> getUserVars() {
    return userVars;
}

private Set<String> unassignedVars = CollectionFactory.set();

public Set<String> getUnassignedVars() {
    return unassignedVars;
}

public boolean hasUnassignedVar() {
    return !unassignedVars.isEmpty();
}

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
                : statement+ 
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

expr            : ^(NBR_REF ID expr expr)
                  { 
                      Jiffle.ImageRole role = imageParams.get($ID.text);
                      if (role == null) {
                          errors.put($ID.text, ErrorCode.NBR_REF_ON_NON_IMAGE_VAR);
                      } else if (role == Jiffle.ImageRole.DEST) {
                          errors.put($ID.text, ErrorCode.NBR_REF_ON_DEST_IMAGE_VAR);
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

