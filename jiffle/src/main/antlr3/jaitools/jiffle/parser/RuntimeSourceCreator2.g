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

tree grammar RuntimeSourceCreator2;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    output = template;
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

private String className;
private String baseClassName;

public RuntimeSourceCreator2(TreeNodeStream input, String className, String baseClassName) {
    this(input);
    this.className = className;
    this.baseClassName = baseClassName;
}

private String getRuntimeExpr(String name, int numArgs) {
    try {
        return FunctionLookup.getRuntimeExpr(name, numArgs);
    } catch (UndefinedFunctionException ex) {
        throw new IllegalArgumentException(ex);
    }
}


}


start           : (jiffleOption)* (el+=varDeclaration)* (el+=statement)+
                -> class(name={className}, base={baseClassName}, members={$el})
                ;


jiffleOption    : ^(JIFFLE_OPTION ID optionValue)
                ;


optionValue     : ID -> {%{$ID.text}}
                | literal -> {$literal.st}
                ;


varDeclaration  : ^(IMAGE_SCOPE_VAR_DECL VAR_IMAGE_SCOPE e=expression)
                -> field(name={$VAR_IMAGE_SCOPE.text}, type={%{"double"}}, mods={%{"private"}}, init={$e.st})
                ;


block           : ^(BLOCK s+=blockStatement*)
                -> block(stmts={$s})
                ;


blockStatement  : statement -> statement(stmt={$statement.st})
                | ^(BREAKIF expression) -> breakif(cond={$expression.st})
                ;


statement       : simpleStatement -> delimstmt(stmt={$simpleStatement.st})
                | block -> {$block.st}
                ;


simpleStatement : imageWrite -> {$imageWrite.st}
                | assignmentExpression -> {$assignmentExpression.st}
                | loop -> {$loop.st}
                | expression -> {$expression.st}
                ;


imageWrite      : ^(IMAGE_WRITE VAR_DEST expression)
                -> imagewrite(var={$VAR_DEST.text}, expr={$expression.st})
                ;


assignmentExpression
                : ^(op=assignmentOp id=assignableVar e=expression)
                -> binaryexpr(lhs={$id.st}, op={$op.st}, rhs={$e.st})
                ;


assignmentOp
@after { $st = %{$text}; }
                : EQ
                | TIMESEQ
                | DIVEQ
                | MODEQ
                | PLUSEQ
                | MINUSEQ
                ;


assignableVar
scope { boolean addType; }
@init { 
    $assignableVar::addType = false; 
}
@after { 
    if ($assignableVar::addType) {
        $st = %{"double " + $text};
    } else {
        $st = %{$text};
    }
}
                : VAR_IMAGE_SCOPE 
                | VAR_PIXEL_SCOPE 
                { 
                    if (!declaredVars.contains($VAR_PIXEL_SCOPE.text)) {
                        declaredVars.add($VAR_PIXEL_SCOPE.text);
                        $assignableVar::addType = true;
                    }
                }
                ;


loop            : conditionalLoop -> {$conditionalLoop.st}
                | foreachLoop -> {$foreachLoop.st}
                ;


conditionalLoop
                : ^(WHILE e=expression s=statement) -> while(cond={$e.st}, stmt={$s.st})
                | ^(UNTIL e=expression s=statement) -> until(cond={$e.st}, stmt={$s.st})
                ;

foreachLoop     : ^(FOREACH ID ^(DECLARED_LIST e=expressionList) s=statement)
                -> foreachlist(var={$ID.text}, list={$e.st}, stmt={$s.st})

                | ^(FOREACH ID ^(SEQUENCE lo=expression hi=expression) s=statement)
                -> foreachseq(var={$ID.text}, lo={$lo.st}, hi={$hi.st}, stmt={$s.st})
                ;


expression returns [String src]
                : ^(FUNC_CALL ID el=expressionList)
                { $src = getRuntimeExpr($ID.text, $el.list.size()); }
                -> call(name={$src}, args={$el.list})

                | ^(IF_CALL el=expressionList) -> ifcall(args={$el.list})

                | imagePos -> {$imagePos.st}

                | binaryExpression -> {$binaryExpression.st}

                | ^(PREFIX NOT e=expression) 
                {$src = getRuntimeExpr("NOT", 1);}
                -> call(name={$src}, args={$e.st})

                | ^(PREFIX prefixOp e=expression) -> preop(op={$prefixOp.st}, expr={$e.st})

                | ^(POSTFIX postfixOp e=expression) -> postop(op={$postfixOp.st}, expr={$e.st})

                | ^(PAR e=expression) -> par(expr={$e.st})

                | var -> {$var.st}

                | VAR_SOURCE -> readimage(var={$VAR_SOURCE.text})

                | CONSTANT
                  {
                    $src = String.valueOf(ConstantLookup.getValue($CONSTANT.text));
                    if ("NaN".equals($src)) {
                        $src = "Double.NaN";
                    }
                  }
                  -> {%{$src}}

                | literal -> {$literal.st}
                ;

var
@after { $st = %{$text}; }
                : VAR_IMAGE_SCOPE
                | VAR_PIXEL_SCOPE
                | VAR_PROVIDED
                | VAR_LOOP
                ;


binaryExpression returns [String src]
                : ^(POW x=expression y=expression) -> pow(x={x.st}, y={y.st})

                | ^(OR e+=expression e+=expression) 
                { $src = getRuntimeExpr("OR", 2); } -> call(name={$src}, args={$e})

                | ^(XOR e+=expression e+=expression) 
                { $src = getRuntimeExpr("XOR", 2); } -> call(name={$src}, args={$e})

                | ^(AND e+=expression e+=expression) 
                { $src = getRuntimeExpr("AND", 2); } -> call(name={$src}, args={$e})

                | ^(LOGICALEQ e+=expression e+=expression) 
                { $src = getRuntimeExpr("EQ", 2); } -> call(name={$src}, args={$e})

                | ^(NE e+=expression e+=expression) 
                { $src = getRuntimeExpr("NE", 2); } -> call(name={$src}, args={$e})

                | ^(GT e+=expression e+=expression) 
                { $src = getRuntimeExpr("GT", 2); } -> call(name={$src}, args={$e})

                | ^(GE e+=expression e+=expression) 
                { $src = getRuntimeExpr("GE", 2); } -> call(name={$src}, args={$e})

                | ^(LT e+=expression e+=expression) 
                { $src = getRuntimeExpr("LT", 2); } -> call(name={$src}, args={$e})

                | ^(LE e+=expression e+=expression) 
                { $src = getRuntimeExpr("LE", 2); } -> call(name={$src}, args={$e})

                | ^(arithmeticOp x=expression y=expression) 
                -> binaryexpr(lhs={x.st}, op={$arithmeticOp.st}, rhs={y.st})
                ;


arithmeticOp
@after { $st = %{$text}; }
                : TIMES
                | DIVIDE
                | MOD
                | PLUS
                | MINUS
                ;


literal         : INT_LITERAL -> {%{$INT_LITERAL.text + ".0"}}
                | FLOAT_LITERAL -> {%{$FLOAT_LITERAL.text}}
                ;


expressionList returns [List list]
@init { $list = new ArrayList(); }
                : ^(EXPR_LIST (e=expression {$list.add($e.st);})* )
                ;


imagePos        : ^(IMAGE_POS VAR_SOURCE b=bandSpecifier? p=pixelSpecifier?)
                -> imageread(var={$VAR_SOURCE.text}, pixel={$p.st}, band={$b.st})
                ;


bandSpecifier   : ^(BAND_REF expression) -> {$expression.st}
                ;


pixelSpecifier  : ^(PIXEL_REF xpos=pixelPos["x"] ypos=pixelPos["y"]) -> pixel(x={xpos.st}, y={ypos.st})
                ;


pixelPos[String var]
                : ^(ABS_POS expression) -> {$expression.st}
                | ^(REL_POS expression) -> binaryexpr(lhs={$var}, op={%{"+"}}, rhs={$expression.st})
                ;


prefixOp        : PLUS -> {%{"+"}}
                | MINUS -> {%{"-"}}
                | incdecOp  -> {$incdecOp.st}
                ;


postfixOp       : incdecOp -> {$incdecOp.st}
                ;


incdecOp        : INCR -> {%{"++"}}
                | DECR -> {%{"--"}}
                ;
