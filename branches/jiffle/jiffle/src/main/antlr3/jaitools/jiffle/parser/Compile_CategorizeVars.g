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
  * Tree rewriting grammar for Jiffle compiler.
  * Converts ASSIGN with image var on rhs to IMAGE_WRITE.
  *
  * @author Michael Bedward
  */

tree grammar Compile_CategorizeVars;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    output = AST;
    filter = true;
    backtrack = true;
}

tokens {
    POS_VAR;
    IMAGE_VAR;
    IMAGE_WRITE;
    LOCAL_VAR;
    NON_LOCAL_VAR;
    CONSTANT;
}

@header {
package jaitools.jiffle.parser;

import java.util.List;

import jaitools.CollectionFactory;
import jaitools.jiffle.CompilationProblem;
import jaitools.jiffle.ErrorCode;
import jaitools.jiffle.Metadata;
import jaitools.jiffle.VarError;
import jaitools.jiffle.runtime.FunctionTable;
import jaitools.jiffle.runtime.VarTable;
}

@members {
private Metadata metadata = null;
private FunctionTable funcTable = new FunctionTable();
private List<CompilationProblem> errors = CollectionFactory.newList();

public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
}

private boolean isInfoFunc(String funcName) {
    return funcTable.isInfoFunction(funcName);
}

private boolean isPosFunc(String funcName) {
    return funcTable.isPositionalFunction(funcName);
}

private boolean isPosVar(String varName) {
    return metadata.getPositionalVars().contains(varName);
}

private boolean isImageVar(String varName) {
    return metadata.getImageVars().contains(varName);
}

private boolean isLocalVar(String varName) {
    return metadata.getLocalVars().contains(varName);
}

private boolean isJiffleConstant(String varName) {
    return VarTable.isConstant(varName);
}

private void addError(String varName) {
    errors.add(new VarError(ErrorCode.INVALID_NBR_REF, varName));
}

public boolean success() {
    return errors.isEmpty();
}

public List<CompilationProblem> getErrors() {
    return errors;
}

}

topdown     : nbrRef
            | var
            ;

bottomup    : imageWrite
            ;

imageWrite  : ^(ASSIGN assign_op IMAGE_VAR e=.)
                  -> ^(IMAGE_WRITE IMAGE_VAR $e)
            ;
                
// Check that neighbourhood references are being applied only to image vars
nbrRef          : ^(NBR_REF ID . .)
                {
                  if (!isImageVar($ID.text)) {
                      addError($ID.text);
                  }
                }
                ;

// Categorize variables
var             :ID
                -> {isPosVar($ID.text)}? POS_VAR[$ID.text]
                -> {isImageVar($ID.text)}? IMAGE_VAR[$ID.text]
                -> {isLocalVar($ID.text)}? LOCAL_VAR[$ID.text]
                -> {isJiffleConstant($ID.text)}? CONSTANT[$ID.text]
                -> NON_LOCAL_VAR[$ID.text]
                ;

assign_op	: EQ
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;
