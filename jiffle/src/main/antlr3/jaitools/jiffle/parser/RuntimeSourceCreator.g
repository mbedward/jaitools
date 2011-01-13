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
  * Takes the optimized Jiffle AST and writes out the corresponding source
  * for the evaluate method in a JiffleRuntime implementation.
  *
  * @author Michael Bedward
  */

tree grammar RuntimeSourceCreator;

options {
    tokenVocab = Morph5;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;
import java.util.Map;
import jaitools.CollectionFactory;

}

@members {
private StringBuilder srcSB;

private FunctionLookup functionLookup = new FunctionLookup();

public String getSource() { return srcSB.toString(); }
}

compile
@init {
    srcSB = new StringBuilder();
}
                : (statement { srcSB.append($statement.src).append(";\n"); } )+
                ;

statement returns [String src]
                : image_write { $src = $image_write.src; }
                | var_assignment { $src = $var_assignment.src; }
                ;

image_write returns [String src]
                : ^(IMAGE_WRITE IMAGE_VAR expr)
                   {
                       $src = "writeToImage(\"" + $IMAGE_VAR.text + "\", _x, _y, _band, " + $expr.src + ")";
                   }
                ;

var_assignment returns [String src]
                : ^(ASSIGN assign_op id=VAR expr)
                   { $src = "double " + $id.text + $assign_op.text + $expr.src; }
                ;
                
expr returns [String src]
                : ^(FUNC_CALL ID expr_list)
                  {
                      final int n = $expr_list.list.size();
                      StringBuilder sb = new StringBuilder();
                      String fn = functionLookup.getRuntimeName($ID.text, n);
                      sb.append(fn).append("(");
                      int k = 0;
                      for (String e : $expr_list.list) {
                          sb.append(e);
                          if (++k < n) sb.append(", ");
                      }
                      sb.append(")");
                      $src = sb.toString();
                  }
                  
                | ^(QUESTION cond=expr e1=expr e2=expr)
                  {
                      $src = "if (dzero(" + $cond.src + ") {\n" 
                          + e1 + "; \n"
                          + "} else { \n"
                          + e2 + "; \n }";
                  }
                  
                | ^(POW e1=expr e2=expr) { $src = "Math.pow(" + e1 + ", " + e2 + ")"; }

                | ^(TIMES e1=expr e2=expr) { $src = e1 + " * " + e2; }
                | ^(DIV e1=expr e2=expr) { $src = e1 + " / " + e2; }
                | ^(MOD e1=expr e2=expr) { $src = e1 + " & " + e2; }
                | ^(PLUS e1=expr e2=expr) { $src = e1 + " + " + e2; }
                | ^(MINUS e1=expr e2=expr) { $src = e1 + " - " + e2; }
                | ^(PREFIX PLUS e1=expr) { $src = "+" + e1; }
                | ^(PREFIX MINUS e1=expr) { $src = "-" + e1; }

                | ^(OR e1=expr e2=expr) 
                  {                       
                    String fn = functionLookup.getRuntimeName("OR", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(AND e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("AND", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(XOR e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("XOR", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(GT e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("GT", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(GE e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("GE", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(LT e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("LT", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(LE e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("LE", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(LOGICALEQ e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("EQ", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(NE e1=expr e2=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("NE", 2);
                    $src = fn + "(" + e1 + ", " + e2 + ")"; 
                  }

                | ^(PREFIX NOT e1=expr)
                  {                       
                    String fn = functionLookup.getRuntimeName("NOT", 1);
                    $src = fn + "(" + e1 + ")"; 
                  }

                | ^(BRACKETED_EXPR e1=expr)
                  { $src = "(" + e1 + ")"; }
  
                | VAR 
                  { $src = $VAR.text; }
              
                | IMAGE_VAR 
                  { $src = "getImageValue(" + $IMAGE_VAR.text + ")"; }
              
                | ^(NBR_REF IMAGE_VAR e1=expr e2=expr) 
                  {
                      // e1 is x offset, e2 is y offset
                      $src = "getImageValue(" + $IMAGE_VAR.text + ", " + e1 + ", " + e2 + ")";
                  }
                  
                | FIXED_VALUE 
                  { $src = String.valueOf(((FixedValueNode)$FIXED_VALUE).getValue()); }
                ;
                
                
expr_list returns [ List<String> list ] 
                :
                  { $list = CollectionFactory.list(); }
                  ^(EXPR_LIST ( e=expr {$list.add($e.src);} )*)
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

type_name	: 'int'
		| 'float'
		| 'double'
		| 'boolean'
		;

