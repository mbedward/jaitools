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

private Set<String> declaredVars = CollectionFactory.set();

}


start[Jiffle.EvaluationModel model, String className]
@init {
    initializeSources(model, className);
}
@after {
    finalizeSources();
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
                    addImageScopeVar($VAR_IMAGE_SCOPE.text, pair);
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
                    $src = makeWriteToImage($VAR_DEST.text, 
                            $expression.priorSrc, $expression.src); 
                }
                ;


assignmentExpression returns [String src]
                : ^(assignmentOp assignableVar expression)
                { 
                    $src = makeAssignment($assignableVar.src, 
                            $assignmentOp.start.getText(),
                            $expression.priorSrc,
                            $expression.src);
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
                : conditionalLoop { $src = $conditionalLoop.src; }
                | foreachLoop { $src = $foreachLoop.src; }
                ;


conditionalLoop returns [String src]
                : ^(WHILE expression statement)
                { 
                    $src = makeWhileLoop($expression.priorSrc, $expression.src, 
                            $statement.src, $statement.isBlock);
                }

                | ^(UNTIL expression statement)
                { 
                    $src = makeUntilLoop($expression.priorSrc, $expression.src, 
                            $statement.src, $statement.isBlock);
                }
                ;

foreachLoop returns [String src]
                : ^(FOREACH ID ^(DECLARED_LIST expressionList) statement)
                {
                    $src = makeForEachListLoop($ID.text, $expressionList.list,
                            $statement.src, $statement.isBlock);
                }

                | ^(FOREACH ID ^(SEQUENCE lo=expression hi=expression) statement)
                {
                    $src = makeForEachSequenceLoop($ID.text, $lo.priorSrc, $lo.src,
                            $hi.priorSrc, $hi.src, $statement.src);
                }
                ;


expression returns [String src, String priorSrc ]
                : ^(FUNC_CALL ID expressionList)
                { 
                    $src = makeFunctionCall($ID.text, $expressionList.list); 
                }

                | ^(IF_CALL expressionList)
                { 
                    ExprSrcPair pair = makeIfCall($expressionList.list); 
                    $src = pair.src;
                    $priorSrc = pair.priorSrc;
                }

                | imagePos { $src = $imagePos.src; }

                | ^(binaryOp e1=expression e2=expression)
                {  
                    $src = makeBinaryExpression($binaryOp.start.getType(), 
                            e1.priorSrc, e1.src, e2.priorSrc, e2.src);
                }

                | ^(PREFIX NOT e1=expression) {$src = getRuntimeExpr("NOT", 1) + "(" + e1.src + ")";}

                | ^(PREFIX op=prefixOp e1=expression) { $src = $op.src + e1.src; }

                | ^(POSTFIX op=postfixOp e1=expression) { $src = e1.src + $op.src; }

                | ^(PAR e1=expression)
                  { $src = "(" + e1.src + ")"; }

                | VAR_IMAGE_SCOPE
                  { $src = $VAR_IMAGE_SCOPE.text; }

                | VAR_PIXEL_SCOPE
                  { $src = $VAR_PIXEL_SCOPE.text; }

                | VAR_PROVIDED
                  { $src = $VAR_PROVIDED.text; }

                | VAR_LOOP
                  { $src = $VAR_LOOP.text; }

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


binaryOp        : POW
                | TIMES
                | DIV
                | MOD
                | PLUS
                | MINUS
                | OR
                | XOR
                | AND
                | LOGICALEQ
                | NE
                | GT
                | GE
                | LT
                | LE
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
