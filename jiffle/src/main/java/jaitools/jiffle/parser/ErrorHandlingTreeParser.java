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

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

/**
 * A base class for Jiffle tree parsers that want to intercept
 * ANTLR error and warning messages.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class ErrorHandlingTreeParser extends TreeParser {

    /** Stores error and warning messages. */
    protected ParsingErrorReporter errorReporter;
    
    /**
     * Constructor.
     *
     * @param input AST node stream
     */
    public ErrorHandlingTreeParser(TreeNodeStream input) {
        super(input);
    }
    
    /**
     * Constructor.
     * 
     * @param input input AST node stream
     * @param state recognizer state
     */
    public ErrorHandlingTreeParser(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    /**
     * Overrides the ANTLR parser method to capture an error message that
     * would otherwise be sent to std err.
     *
     * @param msg the message
     */
    @Override
    public void emitErrorMessage(String msg) {
        if (errorReporter != null) {
            errorReporter.addError(msg);
        } else {
            super.emitErrorMessage(msg);
        }
    }

    /**
     * Gets the error reporter object.
     *
     * @return the error reporter
     */
    public ParsingErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Sets the error reporter.
     *
     * @param er the error reporter (may be {@code null} if message
     *        interception is not required).
     */
    public void setErrorReporter(ParsingErrorReporter er) {
        errorReporter = er;
    }

}
