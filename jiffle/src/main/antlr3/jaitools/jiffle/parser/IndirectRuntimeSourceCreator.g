/*
 * Copyright 2011 Michael Bedward
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
  * Takes the final Jiffle AST and generates Java source
  * for the evaluate method in a JiffleRuntime implementation.
  *
  * @author Michael Bedward
  */

tree grammar IndirectRuntimeSourceCreator;

options {
    superClass = RuntimeSourceCreator;
    tokenVocab = ConvertTernaryExpr;
    ASTLabelType = CommonTree;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;
import jaitools.CollectionFactory;
}

start
@init {
    srcSB = new StringBuilder();
    srcSB.append("public double evaluate(int _x, int _y) { \n");
}
@after {
    srcSB.append("} \n");
}
                : (var_init_block)? (statement { srcSB.append($statement.src).append(";\n"); } )+
                ;

var_init_block  : ^(VAR_INIT_BLOCK var_init_list)
                ;

var_init_list   : ^(VAR_INIT_LIST (var_init)*)
                ;

var_init        : ^(VAR_INIT ID expr)
                ;

statement returns [String src]
                : image_write { $src = $image_write.src; }
                | var_assignment { $src = $var_assignment.src; }
                ;

image_write returns [String src]
                : ^(IMAGE_WRITE IMAGE_VAR expr)
                   {
                       if ($expr.priorSrc != null) {
                           $src = $expr.priorSrc;
                       } else {
                           $src = "";
                       }
                       $src = $src + "return " + $expr.src;
                   }
                ;

var_assignment returns [String src]
                : ^(ASSIGN assign_op VAR expr)
                   { 
                       if ($expr.priorSrc != null) {
                           $src = $expr.priorSrc;
                       } else {
                           $src = "";
                       }
                       $src = $src + "double " + $VAR.text + $assign_op.text + $expr.src; 
                   }
                ;
                
expr returns [String src, String priorSrc ]
                : ^(FUNC_CALL ID expr_list)
                  {
                      final int n = $expr_list.list.size();
                      StringBuilder sb = new StringBuilder();
                      try {
                          FunctionInfo info = functionLookup.getInfo($ID.text, n);
                          sb.append(info.getRuntimeExpr()).append("(");

                          // Work around Janino not handling vararg methods
                          // or generic collections
                          if (info.isVarArg()) {
                              sb.append("new Double[]{");
                          }

                          int k = 0;
                          for (ExprSrcPair esp : $expr_list.list) {
                              sb.append(esp.src);
                              if (++k < n) sb.append(", ");
                          }
                          if (info.isVarArg()) {
                              sb.append("}");
                          }
                          sb.append(")");
                          $src = sb.toString();

                      } catch (UndefinedFunctionException ex) {
                          throw new IllegalStateException(ex);
                      }
                  }

                | ^(IF_CALL expr_list)
                  {
                    List<ExprSrcPair> argList = $expr_list.list;

                    String signFn = getRuntimeExpr("sign", 1);
                    StringBuilder sb = new StringBuilder();

                    String condVar = makeLocalVar("int");
                    sb.append("int ").append(condVar).append(" = ").append(signFn);
                    sb.append("(").append(argList.get(0).src).append("); \n");

                    String resultVar = makeLocalVar("double");
                    sb.append("double ").append(resultVar).append(" = 0; \n");

                    switch (argList.size()) {
                        case 1:
                            sb.append("if (").append(condVar).append(" != 0 ) { \n");
                            sb.append(resultVar).append(" = 1; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = 0; \n");
                            sb.append("} \n");
                            break;

                        case 2:
                            sb.append("if (").append(condVar).append(" != 0 ) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(1).src).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = 0; \n");
                            sb.append("} \n");
                            break;

                        case 3:
                            sb.append("if (").append(condVar).append(" != 0 ) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(1).src).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(2).src).append("; \n");
                            sb.append("} \n");
                            break;
                    
                        case 4:
                            sb.append("if (").append(condVar).append(" > 0 ) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(1).src).append("; \n");
                            sb.append("} else if (").append(condVar).append(" == 0) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(2).src).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(3).src).append("; \n");
                            sb.append("} \n");
                            break;
                    
                        default:
                            throw new IllegalArgumentException("if function error");
                    }
                    $priorSrc = sb.toString();
                    $src = resultVar;

                  }
                  
                | ^(POW e1=expr e2=expr) { $src = "Math.pow(" + e1.src + ", " + e2.src + ")"; }

                | ^(TIMES e1=expr e2=expr) { $src = e1.src + " * " + e2.src; }
                | ^(DIV e1=expr e2=expr) { $src = e1.src + " / " + e2.src; }
                | ^(MOD e1=expr e2=expr) { $src = e1.src + " \% " + e2.src; }
                | ^(PLUS e1=expr e2=expr) { $src = e1.src + " + " + e2.src; }
                | ^(MINUS e1=expr e2=expr) { $src = e1.src + " - " + e2.src; }
                | ^(PREFIX PLUS e1=expr) { $src = "+" + e1.src; }
                | ^(PREFIX MINUS e1=expr) { $src = "-" + e1.src; }

                  
                | ^(OR e1=expr e2=expr) 
                  {                       
                    String fn = getRuntimeExpr("OR", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(AND e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("AND", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(XOR e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("XOR", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(GT e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("GT", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(GE e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("GE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(LT e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("LT", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(LE e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("LE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(LOGICALEQ e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("EQ", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(NE e1=expr e2=expr)
                  {                       
                    String fn = getRuntimeExpr("NE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")"; 
                  }

                | ^(PREFIX NOT e1=expr)
                  {                       
                    String fn = getRuntimeExpr("NOT", 1);
                    $src = fn + "(" + e1.src + ")"; 
                  }

                | ^(BRACKETED_EXPR e1=expr)
                  { $src = "(" + e1.src + ")"; }
  
                | VAR 
                  { $src = $VAR.text; }
              
                | IMAGE_VAR 
                  { $src = "readFromImage(\"" + $IMAGE_VAR.text + "\", _x, _y, _band)"; }
              
                | ^(NBR_REF IMAGE_VAR xref=nbr_ref_expr yref=nbr_ref_expr) 
                  {
                    StringBuilder sb = new StringBuilder();
                    sb.append("readFromImage(");
                    sb.append("\"").append($IMAGE_VAR.text).append("\", ");

                    if (xref.isRelative) {
                        sb.append("(int)(_x + ");
                    } else {
                        sb.append("(int)(");
                    }
                    sb.append(xref.src).append("), ");

                    if (yref.isRelative) {
                        sb.append("(int)(_y + ");
                    } else {
                        sb.append("(int)(");
                    }
                    sb.append(yref.src).append("), ");

                    sb.append("_band)");
                    $src = sb.toString();
                  }
                  
                | FIXED_VALUE 
                  { 
                    double value = ((FixedValueNode)$FIXED_VALUE).getValue();
                    if (Double.isNaN(value)) {
                        $src = "Double.NaN";
                    } else {
                        $src = String.valueOf(value);
                    }
                  }
                ;

nbr_ref_expr returns [ String src, boolean isRelative ]
                : ^(ABS_NBR_REF expr)
                  { 
                    $src = $expr.src;
                    $isRelative = false;
                  }

                | ^(REL_NBR_REF expr)
                  { 
                    $src = $expr.src;
                    $isRelative = true;
                  }
                ;
                
expr_list returns [ List<ExprSrcPair> list ] 
                :
                  { $list = CollectionFactory.list(); }
                  ^(EXPR_LIST ( e=expr {$list.add(new ExprSrcPair(e.src, e.priorSrc));} )*)
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

