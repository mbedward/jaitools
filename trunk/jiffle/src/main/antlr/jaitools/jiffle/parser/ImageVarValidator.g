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
  * Grammar for ImageVarValidator. Takes a jiffle AST, and a set of
  * image var names and checks that:
  * <ol type="1">
  * <li> there is at least on output image var in the script
  * <li> no image is used for both input and output
  * </ol>
  *
  * @author Michael Bedward
  */

tree grammar ImageVarValidator;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
}

@members {
    private boolean printDebug = false;
    public void setPrint(boolean b) { printDebug = b; }

    private Set<String> imageVars;

    public void setImageVars(String[] varNames) {
        imageVars = new HashSet<String>();
        imageVars.addAll(Arrays.asList(varNames));
    }
    
    public void setImageVars(Collection<String> varNames) {
        imageVars = new HashSet<String>();
        imageVars.addAll(varNames);
    }
    
    private Set<String> outImageVars = new HashSet<String>();

    public Set<String> getOutputImageVars() {
        return outImageVars;
    }
    
    private Set<String> errorImageVars = new HashSet<String>();

    public Set<String> getErrorImageVars() {
        return errorImageVars;
    }
    
    public boolean hasImageVarError() {
        return !errorImageVars.isEmpty();
    }
}

start
@init {
    if (imageVars == null || imageVars.isEmpty()) {
        throw new RuntimeException("failed to set image vars before using VarClassifier");
    }
}
                : statement+ 
                ;

statement       : assignment
                | expr
                ;

expr_list       : ^(EXPR_LIST expr*)
                ;

assignment      : ^(ASSIGN assign_op ID expr)
                  {
                      if (imageVars.contains($ID.text)) {
                          outImageVars.add($ID.text);
                      
                          if (printDebug) {
                              System.out.println("Output image var: " + $ID.text);
                          }
                      }
                  }
                ;

expr            : ^(FUNC_CALL ID expr_list)
                | ID
                  {
                      if (imageVars.contains($ID.text)) {
                          if (outImageVars.contains($ID.text)) {
                              // error - using image for input and output
                              errorImageVars.add($ID.text);
                          
                              if (printDebug) {
                                  System.out.println("Image var error: " +
                                    $ID.text + " used for both input and output");
                              }
                          }
                      }
                  }
                  
                | ^(QUESTION expr expr expr)
                | ^(expr_op expr expr)
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

