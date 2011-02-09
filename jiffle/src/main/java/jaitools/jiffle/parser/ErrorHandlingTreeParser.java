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

package jaitools.jiffle.parser;

import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

/**
 * Provides error handling methods for Jiffle tree parsers.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class ErrorHandlingTreeParser extends TreeParser {

    protected ParsingErrorReporter errorReporter;
    
    protected Map<String, List<Message>> errors;
    
    public ErrorHandlingTreeParser(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    @Override
    public void emitErrorMessage(String msg) {
        if (errorReporter != null) {
            errorReporter.addError(msg);
        } else {
            super.emitErrorMessage(msg);
        }
    }

    public ParsingErrorReporter getErrorReporter() {
        return errorReporter;
    }

    public void setErrorReporter(ParsingErrorReporter er) {
        errorReporter = er;
    }

}