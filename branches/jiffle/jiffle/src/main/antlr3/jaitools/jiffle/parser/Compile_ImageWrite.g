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
  * Converts ASSIGN with image var on rhs to IMAGE_WRITE
  *
  * @author Michael Bedward
  */

tree grammar Compile_ImageWrite;

options {
    tokenVocab = Jiffle;
    ASTLabelType = CommonTree;
    output = AST;
    filter = true;
    backtrack = true;
}

tokens {
    IMAGE_WRITE;
}

@header {
package jaitools.jiffle.parser;

import java.util.Collections;
import jaitools.jiffle.CompilationProblem;
import jaitools.jiffle.Metadata;
}

@members {
private Metadata metadata = null;

public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
}

private boolean isImageVar(String varName) {
    if (metadata == null) {
        throw new RuntimeException("forget to set metadata ?");
    }

    return metadata.getImageVars().contains(varName);
}

public boolean success() {
    return true;
}

public List<CompilationProblem> getErrors() {
    return Collections.emptyList();
}

}

topdown : writeToImage ;

writeToImage : ^(ASSIGN assign_op ID e=.)
                  -> {isImageVar($ID.text)}? ^(IMAGE_WRITE ID $e)
                  -> ^(ASSIGN assign_op ID $e)
                  
                ;

assign_op	: EQ
		| TIMESEQ
		| DIVEQ
		| MODEQ
		| PLUSEQ
		| MINUSEQ
		;
