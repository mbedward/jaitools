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
  * Generates Java sources for the runtime class from the final AST.
  *
  * @author Michael Bedward
  */

tree grammar RuntimeSourceCreator;

options {
    superClass = AbstractRuntimeSourceCreator;
    tokenVocab = JiffleParser;
    ASTLabelType = CommonTree;
}


@header {
package jaitools.jiffle.parser;

import java.util.Map;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
}

@members {

private Jiffle.EvaluationModel model;
private Map<String, ExprSrcPair> imageScopeVars = CollectionFactory.orderedMap();

private void createVarSource() {
    if (imageScopeVars.isEmpty()) return;

    getterSB.append("public Double getVar(String varName) { \n");

    final int numVars = imageScopeVars.size();
    int k = 1;
    for (String name : imageScopeVars.keySet()) {
        ExprSrcPair pair = imageScopeVars.get(name);
        varSB.append("double ").append(name).append("; \n");

        if (pair != null) {
            if (pair.priorSrc != null) initSB.append(pair.priorSrc);
            initSB.append(name).append(" = ").append(pair.src).append("; \n");
        }

        getterSB.append("if (\"").append(name).append("\".equals(varName)) { \n");
        getterSB.append("return ").append(name).append("; \n");
        getterSB.append("}");
        if (k < numVars) {
            getterSB.append(" else ");
        }
        k++ ;
    }
    getterSB.append("\n").append("return null; \n");
    getterSB.append("} \n");
}

}

start[Jiffle.EvaluationModel model, String className]
@init {
    this.model = model;

    ctorSB.append("public ").append(className).append("() { \n");
    initSB.append("protected void initImageScopeVars() { \n");

    switch (model) {
        case DIRECT:
            evalSB.append("public void evaluate(int _x, int _y) { \n");
            break;

        case INDIRECT:
            evalSB.append("public double evaluate(int _x, int _y) { \n");
            break;

        default:
            throw new IllegalArgumentException("Invalid evaluation model parameter");
    }
}
@after {
    createVarSource();
    ctorSB.append("} \n");
    initSB.append("} \n");
    evalSB.append("} \n");
}
                : var_init* (statement { evalSB.append($statement.src).append(";\n"); } )+
                ;

var_init        : ^(VAR_INIT ID (e=expr)?)
                  {
                    ExprSrcPair pair = null;
                    if (e != null) {
                        pair = new ExprSrcPair(e.priorSrc, e.src);
                    }
                    imageScopeVars.put($ID.text, pair);
                  }
                ;

statement returns [String src]
                : image_write { $src = $image_write.src; }
                | var_assignment { $src = $var_assignment.src; }
                ;

image_write returns [String src]
                : ^(IMAGE_WRITE VAR_DEST expr)
                   {
                       StringBuilder sb = new StringBuilder();
                       if ($expr.priorSrc != null) {
                           sb.append($expr.priorSrc);
                       }

                       switch (model) {
                           case DIRECT:
                               sb.append("writeToImage( \"");
                               sb.append($VAR_DEST.text);
                               sb.append("\", _x, _y, 0, ");
                               sb.append($expr.src).append(" )");
                               break;

                           case INDIRECT:
                               sb.append("return ").append($expr.src);
                       }
                       $src = sb.toString();
                   }
                ;

var_assignment returns [String src]
                : ^(ASSIGN assign_op assignable_var expr)
                   {
                       StringBuilder sb = new StringBuilder();
                       if ($expr.priorSrc != null) {
                           sb.append($expr.priorSrc);
                       }

                       if (!$assignable_var.imageScope) {
                           sb.append("double ");
                       }
                       sb.append($assignable_var.src);
                       sb.append(" ").append($assign_op.text).append(" ");
                       sb.append($expr.src);
                       $src = sb.toString();
                   }
                ;

assignable_var returns [String src, boolean imageScope]
                : VAR_IMAGE_SCOPE { $src = $VAR_IMAGE_SCOPE.text; $imageScope = true; }
                | VAR_PIXEL_SCOPE { $src = $VAR_PIXEL_SCOPE.text; $imageScope = false; }
                ;

expr returns [String src, String priorSrc ]
                : ^(FUNC_CALL ID expr_list)
                  {
                      final int n = $expr_list.list.size();
                      StringBuilder sb = new StringBuilder();
                      try {
                          FunctionInfo info = FunctionLookup.getInfo($ID.text, n);
                          sb.append(info.getRuntimeExpr()).append("(");

                          // Work around Janino not handling vararg methods
                          // or generic collections
                          if (info.isVarArg()) {
                              sb.append("new Double[]{");
                          }

                          int k = 0;
                          for (String esrc : $expr_list.list) {
                              sb.append(esrc);
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
                    List<String> argList = $expr_list.list;

                    String signFn = getRuntimeExpr("sign", 1);
                    StringBuilder sb = new StringBuilder();

                    String condVar = makeLocalVar("Integer");
                    sb.append("Integer ").append(condVar).append(" = ");
                    sb.append(signFn).append("(").append(argList.get(0)).append("); \n");

                    String resultVar = makeLocalVar("double");
                    sb.append("double ").append(resultVar).append("; \n");

                    sb.append("if (").append(condVar).append(" == null) { \n");
                    sb.append(resultVar).append(" = Double.NaN; \n");
                    sb.append("} else { \n");

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
                            sb.append(resultVar).append(" = ").append(argList.get(1)).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = 0; \n");
                            sb.append("} \n");
                            break;

                        case 3:
                            sb.append("if (").append(condVar).append(" != 0 ) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(1)).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(2)).append("; \n");
                            sb.append("} \n");
                            break;

                        case 4:
                            sb.append("if (").append(condVar).append(" > 0 ) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(1)).append("; \n");
                            sb.append("} else if (").append(condVar).append(" == 0) { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(2)).append("; \n");
                            sb.append("} else { \n");
                            sb.append(resultVar).append(" = ").append(argList.get(3)).append("; \n");
                            sb.append("} \n");
                            break;

                        default:
                            throw new IllegalArgumentException("if function error");
                    }

                    sb.append("} \n");
                    $priorSrc = sb.toString();
                    $src = resultVar;

                  }

                | image_pos { $src = $image_pos.src; }

                | ^(POW e1=expr e2=expr) { $src = "Math.pow(" + e1.src + ", " + e2.src + ")"; }

                | ^(TIMES e1=expr e2=expr) { $src = e1.src + " * " + e2.src; }
                | ^(DIV e1=expr e2=expr) { $src = e1.src + " / " + e2.src; }
                | ^(MOD e1=expr e2=expr) { $src = e1.src + " \% " + e2.src; }
                | ^(PLUS e1=expr e2=expr) { $src = e1.src + " + " + e2.src; }
                | ^(MINUS e1=expr e2=expr) { $src = e1.src + " - " + e2.src; }

                | ^(SIGN PLUS e1=expr) { $src = "+" + e1.src; }
                | ^(SIGN MINUS e1=expr) { $src = "-" + e1.src; }

                | ^(PREFIX INCR e1=expr) { $src = "++" + e1.src; }
                | ^(PREFIX DECR e1=expr) { $src = "--" + e1.src; }

                | ^(POSTFIX INCR e1=expr) { $src = e1.src + "++"; }
                | ^(POSTFIX DECR e1=expr) { $src = e1.src + "--"; }

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

                | ^(SIGN NOT e1=expr)
                  {
                    String fn = getRuntimeExpr("NOT", 1);
                    $src = fn + "(" + e1.src + ")";
                  }

                | ^(BRACKETED_EXPR e1=expr)
                  { $src = "(" + e1.src + ")"; }

                | VAR_IMAGE_SCOPE
                  { $src = $VAR_IMAGE_SCOPE.text; }

                | VAR_PIXEL_SCOPE
                  { $src = $VAR_PIXEL_SCOPE.text; }

                | VAR_PROVIDED
                  { $src = $VAR_PROVIDED.text; }

                | VAR_SOURCE
                  { $src = "readFromImage(\"" + $VAR_SOURCE.text + "\", _x, _y, 0)"; }

                | CONSTANT
                  {
                    $src = String.valueOf(ConstantLookup.getValue($CONSTANT.text));
                    if ("NaN".equals($src)) {
                        $src = "Double.NaN";
                    }
                  }

                | literal
                  { $src = $literal.src; }
                ;

literal returns [String src]
                : INT_LITERAL { $src = $INT_LITERAL.text + ".0"; }
                | FLOAT_LITERAL { $src = $FLOAT_LITERAL.text; }
                ;

expr_list returns [ List<String> list ]
                :
                  { $list = CollectionFactory.list(); }
                  ^(EXPR_LIST ( e=expr {$list.add(e.src);} )*)
                ;

image_pos returns [String src]
                : ^(IMAGE_POS VAR_SOURCE b=band_specifier? p=pixel_specifier?)
                  {
                    StringBuilder sb = new StringBuilder();
                    sb.append("readFromImage(");
                    sb.append("\"").append($VAR_SOURCE.text).append("\", ");

                    if (p != null) {
                        sb.append(p).append(", ");
                    } else {
                        sb.append("_x, _y, ");
                    }

                    if (b != null) {
                        sb.append("(int)(").append(b).append("))");
                    } else {
                        sb.append("0)");  // band 0
                    }
                    $src = sb.toString();
                  }
                ;

band_specifier returns [String src]
                : ^(BAND_REF expr)
                  { $src = $expr.src; }
                ;

pixel_specifier returns [String src]
                : ^(PIXEL_REF x=pixel_pos y=pixel_pos)
                  {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(int)(");
                    if (x.isRelative) {
                        sb.append("_x + ");
                    }
                    sb.append(x.src).append("), ");

                    sb.append("(int)(");
                    if (y.isRelative) {
                        sb.append("_y + ");
                    }
                    sb.append(y.src).append(")");
                    $src = sb.toString();
                  }
                ;

pixel_pos returns [ String src, boolean isRelative ]
                : ^(ABS_POS expr)
                  {
                    $src = $expr.src;
                    $isRelative = false;
                  }

                | ^(REL_POS expr)
                  {
                    $src = $expr.src;
                    $isRelative = true;
                  }
                ;

assign_op       : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;

incdec_op       : INCR
                | DECR
                ;

type_name       : 'int'
                | 'float'
                | 'double'
                | 'boolean'
                ;
