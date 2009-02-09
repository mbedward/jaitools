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
  * Grammar for JiffleVarClassifier. Reads the AST produced by the token
  * parser; checks for any user-defined variables used before being assigned 
  * values; and identifies positional variables (those that depend directly
  * or indirectly on image position)
  *
  * @author Michael Bedward
  */

tree grammar JiffleVarClassifier;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
}

@members {
    private boolean printDebug = false;
    public void setPrint(boolean b) { printDebug = b; }

    private boolean isPositional;

    private boolean isPositionalFunc(String funcName) {
        return FunctionTable.getFunctionType(funcName) == FunctionTable.Type.POSITIONAL;
    }

    /* properly defined vars */
    private Set<String> defVars = new HashSet<String>();

    /* map: key is var name; value is line where var first referenced */
    private Map<String, Integer> unassignedVars = new HashMap<String, Integer>();

    public Map<String, Integer> getUnassignedVars() {
        return unassignedVars;
    }
    
    private Set<String> positionalVars = new HashSet<String>();

    public Set<String> getPositionalVars() {
        return positionalVars;
    }
}

start           : statement+ 
                ;

statement       : assignment
                | expr
                ;

expr_list       : ^(EXPR_LIST expr*)
                ;

assignment
@init {
    isPositional = false;
}
                : ^(ASSIGN assign_op ID expr)
                  {
                      defVars.add($ID.text);
                      
                      if (isPositional) {
                          positionalVars.add($ID.text);
                          if (printDebug) {
                              System.out.println($ID.text + " is positional");
                          }
                      } else {
                          if (printDebug) {
                              System.out.println($ID.text + " is not positional");
                          }
                      }
                  }
                ;

expr            : ^(FUNC_CALL ID expr_list)
                  { 
                      if (isPositionalFunc($ID.text)) {
                          isPositional = true;
                      }
                  }

                | ID
                  {
                      if (!defVars.contains($ID.text)) {
                          unassignedVars.put($ID.text, $ID.line);
                      }
                      
                      if (positionalVars.contains($ID.text)) {
                          isPositional = true;
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

