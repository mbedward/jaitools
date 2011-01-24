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

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import jaitools.CollectionFactory;

/**
 * Base class for runtime source creation tree parsers.
 * <p>
 * The runtime source creation classes are generated from the ANTLR grammars
 * ({@code DirectRuntimeSourceCreator.g} and {@code IndirectRuntimeSourceCreator.g}).
 * This class provides some common methods and fields.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class RuntimeSourceCreator extends TreeParser {

    protected StringBuilder srcSB;
    protected FunctionLookup functionLookup;
    protected ParsingErrorReporter errorReporter;

    protected class LocalVar {
        String type;
        String name;
    }

    protected List<LocalVar> localVars = CollectionFactory.list();
    
    protected class ExprSrcPair {

        String src;
        String priorSrc;

        ExprSrcPair(String src, String priorSrc) {
            this.src = src;
            this.priorSrc = priorSrc;
        }
    }

    public RuntimeSourceCreator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);

        functionLookup = new FunctionLookup();
        srcSB = new StringBuilder();
    }
    
    public abstract void start() throws RecognitionException;

    public void setErrorReporter(ParsingErrorReporter er) {
        errorReporter = er;
    }

    public ParsingErrorReporter getErrorReporter() {
        return errorReporter;
    }

    @Override
    public void emitErrorMessage(String msg) {
        if (errorReporter != null) {
            errorReporter.addError(msg);
        } else {
            super.emitErrorMessage(msg);
        }
    }

    public String getSource() {
        return srcSB.toString();
    }

    protected String getRuntimeExpr(String name, int numArgs) {
        try {
            return functionLookup.getRuntimeExpr(name, numArgs);
        } catch (UndefinedFunctionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected String makeLocalVar(String type) {
        LocalVar var = new LocalVar();
        var.type = type;
        var.name = "_local" + localVars.size();
        localVars.add(var);
        return var.name;
    }

}
