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
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
}


@header {
package jaitools.jiffle.parser;

import java.util.Map;
import java.util.Set;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
}

@members {

private Jiffle.EvaluationModel model;
private Map<String, ExprSrcPair> imageScopeVars = CollectionFactory.orderedMap();
private Set<String> declaredVars = CollectionFactory.set();

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
                : jiffleOption* varDeclaration* (s=statement 
                        {
                            evalSB.append(s.src); 
                            String eol = s.isBlock ? "\n" : ";\n" ;
                            evalSB.append(eol);
                        })+
                ;


jiffleOption    : ^(JIFFLE_OPTION ID optionValue)
                ;


optionValue     : ID
                | INT_LITERAL
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL VAR_IMAGE_SCOPE e=expression)
                {
                    ExprSrcPair pair = null;
                    if (e != null) {
                        pair = new ExprSrcPair(e.priorSrc, e.src);
                    }
                    imageScopeVars.put($VAR_IMAGE_SCOPE.text, pair);
                }
                ;


block returns [String src]
@init {
    StringBuilder sb = new StringBuilder("{ \n");
}
@after {
    sb.append("} \n");
    $src = sb.toString();
}
                : ^(BLOCK (s=statement 
                        {
                            sb.append(s.src);
                            String eol = s.isBlock ? "\n" : ";\n" ;
                            sb.append(eol);
                        } )* )
                ;


statement returns [String src, boolean isBlock]
                : simpleStatement {$src = $simpleStatement.src; $isBlock = false;}
                | block {$src = $block.src; $isBlock = true;}
                ;


simpleStatement returns [String src]
@init {
    $src = "";
}
                : imageWrite
                { $src = $imageWrite.src; }

                | assignmentExpression
                { $src = $assignmentExpression.src; }


                | loop
                { $src = $loop.src; }

                | expression
                {
                    if ($expression.priorSrc != null) {
                        $src = $expression.priorSrc;
                    }
                    $src = $src + $expression.src;
                }
                ;


imageWrite returns [String src]
                : ^(IMAGE_WRITE VAR_DEST expression)
                { 
                    StringBuilder sb = new StringBuilder();
                    if ($expression.priorSrc != null) {
                        sb.append($expression.priorSrc);
                    }

                    switch (model) {
                        case DIRECT:
                            sb.append("writeToImage(");
                            sb.append("\"").append($VAR_DEST.text).append("\", ");
                            sb.append("_x, _y, 0, ");
                            sb.append($expression.src).append(" )");
                            break;

                        case INDIRECT:
                            sb.append("return ").append($expression.src);
                    }
                    $src = sb.toString();
                }
                ;


assignmentExpression returns [String src]
                : ^(assignmentOp assignableVar expression)
                {
                    StringBuilder sb = new StringBuilder();
                    if ($expression.priorSrc != null) {
                        sb.append($expression.priorSrc);
                    }

                    sb.append($assignableVar.src);
                    sb.append(" ").append($assignmentOp.start.getText()).append(" ");
                    sb.append($expression.src);
                    $src = sb.toString();
                }
                ;


assignmentOp    : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


assignableVar returns [String src]
@init {
    $src = "";
}
                : VAR_IMAGE_SCOPE 
                { 
                    $src = $VAR_IMAGE_SCOPE.text; 
                }

                | VAR_PIXEL_SCOPE 
                { 
                    if (!declaredVars.contains($VAR_PIXEL_SCOPE.text)) {
                        declaredVars.add($VAR_PIXEL_SCOPE.text);
                        $src = "double ";
                    }
                    $src = $src + $VAR_PIXEL_SCOPE.text;
                }
                ;


loop returns [String src]
@init {
    StringBuilder sb = new StringBuilder();
}
@after {
    $src = sb.toString();
}
                : ^(loopType e=expression s=statement)
                {
                    sb.append("while (");
                    sb.append(getRuntimeExpr("sign", 1));
                    sb.append("(").append(e.src).append(")");
                    switch( $loopType.start.getType() ) {
                        case WHILE:
                            sb.append(" != 0) ");
                            break;

                        case UNTIL:
                            sb.append(" == 0) ");
                            break;

                        default:
                            throw new RuntimeException("Unknown loop type");
                    }
                    sb.append($s.src);
                    String eol = s.isBlock ? "\n" : ";\n" ;
                    sb.append(eol);
                }
                ;


loopType        : WHILE
                | UNTIL
                ;


expression returns [String src, String priorSrc ]
                : ^(FUNC_CALL ID expressionList)
                  {
                      final int n = $expressionList.list.size();
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
                          for (String esrc : $expressionList.list) {
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


                | ^(IF_CALL expressionList)
                  {
                    List<String> argList = $expressionList.list;

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

                | imagePos { $src = $imagePos.src; }

                | ^(POW e1=expression e2=expression) { $src = "Math.pow(" + e1.src + ", " + e2.src + ")"; }

                | ^(TIMES e1=expression e2=expression) { $src = e1.src + " * " + e2.src; }

                | ^(DIV e1=expression e2=expression) { $src = e1.src + " / " + e2.src; }

                | ^(MOD e1=expression e2=expression) { $src = e1.src + " \% " + e2.src; }

                | ^(PLUS e1=expression e2=expression) { $src = e1.src + " + " + e2.src; }

                | ^(MINUS e1=expression e2=expression) { $src = e1.src + " - " + e2.src; }

                | ^(PREFIX NOT e1=expression) {$src = getRuntimeExpr("NOT", 1) + "(" + e1.src + ")";}

                | ^(PREFIX op=prefixOp e1=expression) { $src = $op.src + e1.src; }

                | ^(POSTFIX op=postfixOp e1=expression) { $src = e1.src + $op.src; }

                | ^(OR e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("OR", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(AND e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("AND", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(XOR e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("XOR", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(GT e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("GT", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(GE e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("GE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(LT e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("LT", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(LE e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("LE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(LOGICALEQ e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("EQ", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(NE e1=expression e2=expression)
                  {
                    String fn = getRuntimeExpr("NE", 2);
                    $src = fn + "(" + e1.src + ", " + e2.src + ")";
                  }

                | ^(PAR e1=expression)
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


expressionList returns [ List<String> list ]
                :
                  { $list = CollectionFactory.list(); }
                  ^(EXPR_LIST ( e=expression {$list.add(e.src);} )*)
                ;


imagePos returns [String src]
                : ^(IMAGE_POS VAR_SOURCE b=bandSpecifier? p=pixelSpecifier?)
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


bandSpecifier returns [String src]
                : ^(BAND_REF expression)
                  { $src = $expression.src; }
                ;


pixelSpecifier returns [String src]
                : ^(PIXEL_REF x=pixelPos y=pixelPos)
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


pixelPos returns [ String src, boolean isRelative ]
                : ^(ABS_POS expression)
                  {
                    $src = $expression.src;
                    $isRelative = false;
                  }

                | ^(REL_POS expression)
                  {
                    $src = $expression.src;
                    $isRelative = true;
                  }
                ;


prefixOp returns [String src]
                : PLUS { $src = "+"; }
                | MINUS { $src = "-"; }
                | incdecOp { $src = $incdecOp.src; }
                ;


postfixOp returns [String src]
                : incdecOp { $src = $incdecOp.src; }
                ;


incdecOp returns [String src]
                : INCR { $src = "++"; }
                | DECR { $src = "--"; }
                ;
