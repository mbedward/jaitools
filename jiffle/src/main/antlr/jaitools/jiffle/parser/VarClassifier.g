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
  * Grammar for VarClassifier. Reads the AST produced by the token
  * parser and does the following; 
  * <ol type="1">
  * <li> performs an check for any user-defined variables that are 
  *      used before being assigned a value (either an error or
  *      an image var)
  * <li> identifies positional variables (those that depend directly
  *      or indirectly on image position)
  * </ol>
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jaitools.jiffle.interpreter.ErrorCode;
import jaitools.jiffle.interpreter.JiffleRunner;
import jaitools.jiffle.interpreter.VarTable;
}

@members {
private boolean printDebug = false;
public void setPrint(boolean b) { printDebug = b; }

private boolean isInfoFunc(String funcName) {
    return JiffleRunner.isInfoFunction(funcName);
}

private boolean isPositionalFunc(String funcName) {
    return JiffleRunner.isPositionalFunction(funcName);
}

/*
 * Positional variables - those that depend directly or indirectly
 * on image position when the jiffle is being run
 */
private Set<String> positionalVars = new HashSet<String>();

public Set<String> getPositionalVars() {
    return positionalVars;
}


/*
 * Purely local variables - those that depend only on
 * local numeric values and/or named constants
 */
private Set<String> localVars = new HashSet<String>();
private Set<String> nonLocalVars = new HashSet<String>();

public Set<String> getLocalVars() {
    return localVars;
}


/*
 * Recording of user variables and checking that they are
 * assigned a value before being used in an expression.
 * Use of an unsassigned variable is not necessarily an error
 * as it might (should) be an input image variable.
 */
private Set<String> userVars = new HashSet<String>();

public Set<String> getUserVars() {
    return userVars;
}

private Set<String> unassignedVars = new HashSet<String>();

public Set<String> getUnassignedVars() {
    return unassignedVars;
}

public boolean hasUnassignedVar() {
    return !unassignedVars.isEmpty();
}

/**
 * Image var validation - there should be at least one output image
 * and no image should be used for both input and output
 */
private Set<String> imageVars;

public void setImageVars(Collection<String> varNames) {
    imageVars = new HashSet<String>();
    imageVars.addAll(varNames);
}

private Set<String> inImageVars = new HashSet<String>();
private Set<String> outImageVars = new HashSet<String>();

public Set<String> getOutputImageVars() {
    return outImageVars;
}


/* Table of var name : error code */
private Map<String, ErrorCode> errorTable = new HashMap<String, ErrorCode>();

public Map<String, ErrorCode> getErrors() {
    return errorTable;
}

public boolean hasError() {
    return !errorTable.isEmpty();
}

/*
 * This method is run after the tree has been processed to 
 * check that the image var params and the AST are in sync
 */
private void postValidation() {
    for (String varName : unassignedVars) {
        errorTable.put(varName, ErrorCode.VAR_UNDEFINED);
    }

    if (outImageVars.isEmpty()) {
        errorTable.put("n/a", ErrorCode.IMAGE_NO_OUT);
    }

    // check all image vars are accounted for
    for (String varName : imageVars) {
        if (!inImageVars.contains(varName) && !outImageVars.contains(varName)) {
            errorTable.put(varName, ErrorCode.IMAGE_UNUSED);
        }
    }
}

    
}

start
@init {
    if (imageVars == null || imageVars.isEmpty()) {
        throw new RuntimeException("failed to set image vars before using VarClassifier");
    }
}
@after {
    postValidation();
}
                : statement+ 
                ;

statement       : expr
                ;

expr_list returns [boolean isLocal]
@init{
    $isLocal = true;
}
                : ^(EXPR_LIST (expr {if (!$expr.isLocal) $isLocal = false;} )*)
                ;

expr returns [boolean isLocal, boolean isPositional]
@init {
    $isLocal = true; 
    $isPositional = false;
}
                : ^(ASSIGN assign_op ID e1=expr)
                  {
                      if (imageVars.contains($ID.text)) {
                          outImageVars.add($ID.text);
                      
                          if (printDebug) {
                              System.out.println("Output image var: " + $ID.text);
                          }
                          
                      } else {
                          userVars.add($ID.text);
                      
                          if ($e1.isPositional) {
                              positionalVars.add($ID.text);
                              if (printDebug) {
                                  System.out.println($ID.text + " is positional");
                              }
                          } else if ($e1.isLocal) {
                              localVars.add($ID.text);
                              if (printDebug) {
                                  System.out.println($ID.text + " is local");
                              }
                          } else {
                              nonLocalVars.add($ID.text);
                          }
                      }
                  }


                | ^(FUNC_CALL ID expr_list)
                  { 
                      if (isPositionalFunc($ID.text)) {
                          $isPositional = true;
                          $isLocal = false;

                      } else if (isInfoFunc($ID.text)) {
                          $isLocal = false;
                      } else {
                          $isLocal = $expr_list.isLocal;
                      }
                  }

                | ID
                  {
                      if (imageVars.contains($ID.text)) {
                          if (outImageVars.contains($ID.text)) {
                              // error - using image for input and output
                              errorTable.put($ID.text, ErrorCode.IMAGE_IO);
                          
                              if (printDebug) {
                                  System.out.println("Image var error: " +
                                    $ID.text + " used for both input and output");
                              }
                          } else {
                              inImageVars.add($ID.text);
                          
                              // input image var so this is a non-local assignment
                              $isLocal = false;
                          }
                          
                      } else if (!userVars.contains($ID.text) &&     // not assigned yet
                                 !VarTable.isConstant($ID.text) &&  // not a named constant
                                 !unassignedVars.contains($ID.text))  // not already reported
                      {
                          unassignedVars.add($ID.text);
                      }
                      
                      // var dependency tracking
                      if (positionalVars.contains($ID.text)) {
                          $isPositional = true;
                          $isLocal = false;
                      } else if (nonLocalVars.contains($ID.text)) {
                          $isLocal = false;
                      }
                  }
                  
                | ^(expr_op e1=expr e2=expr)
                  {
                      $isLocal = ($e1.isLocal && $e2.isLocal);
                      $isPositional = ($e1.isPositional || $e2.isPositional);
                  }
     
                | ^(QUESTION expr expr expr)
                | INT_LITERAL 
                | FLOAT_LITERAL 
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

