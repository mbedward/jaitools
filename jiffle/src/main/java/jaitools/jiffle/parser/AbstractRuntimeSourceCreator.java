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

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;

/**
 * Base class for runtime source creation tree parsers.
 * <p>
 * The runtime source creation classes are generated from the ANTLR grammars
 * ({@code DirectRuntimeSourceCreator.g} and {@code IndirectRuntimeSourceCreator.g}).
 * This class provides some common methods and fields.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AbstractRuntimeSourceCreator extends ErrorHandlingTreeParser {

    protected StringBuilder ctorSB;
    protected StringBuilder initSB;
    protected StringBuilder evalSB;
    protected StringBuilder varSB;
    protected StringBuilder getterSB;
    protected FunctionLookup functionLookup;

    protected class LocalVar {
        String type;
        String name;
    }

    protected List<LocalVar> localVars = CollectionFactory.list();
    
    protected class ExprSrcPair {

        String src;
        String priorSrc;

        ExprSrcPair(String priorSrc, String src) {
            this.priorSrc = priorSrc;
            this.src = src;
        }
    }

    public AbstractRuntimeSourceCreator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);

        functionLookup = new FunctionLookup();

        ctorSB = new StringBuilder();
        initSB = new StringBuilder();
        evalSB = new StringBuilder();
        varSB = new StringBuilder();
        getterSB = new StringBuilder();
    }
    
    public abstract void start(Jiffle.EvaluationModel model, String runtimeClassName) throws RecognitionException;

    public String getCtorSource() {
        return ctorSB.toString();
    }

    public String getInitSource() {
        return initSB.toString();
    }

    public String getEvalSource() {
        return evalSB.toString();
    }
    
    public String getVarSource() {
        return varSB.toString();
    }

    public String getGetterSource() {
        return getterSB.toString();
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
