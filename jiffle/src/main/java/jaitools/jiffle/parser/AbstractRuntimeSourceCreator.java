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

    /** StringBuilder for constructor source */
    protected StringBuilder ctorSB;
    /** StringBuilder for init method source */
    protected StringBuilder initSB;
    /** StringBuilder for evaluate method source */
    protected StringBuilder evalSB;
    /** StringBuilder for runtime class field delcarations */
    protected StringBuilder varSB;
    /** StringBuilder for class field (image scope variable) getter method source */
    protected StringBuilder getterSB;

    /**
     * Holds details of a local variable created
     * during source generation.
     */
    protected class LocalVar {
        String type;
        String name;
    }

    /**
     * Local variables (e.g. used to implement conditional statements).
     */
    protected List<LocalVar> localVars = CollectionFactory.list();
    
    /**
     * String pair used to pass source elements between rules in
     * the source creation parser.
     */
    protected class ExprSrcPair {

        String src;
        String priorSrc;

        ExprSrcPair(String priorSrc, String src) {
            this.priorSrc = priorSrc;
            this.src = src;
        }
    }

    /**
     * Constructor called in the ANTLR grammar.
     *
     * @param input AST node stream
     * @param state parser state (not used by Jiffle directly)
     */
    public AbstractRuntimeSourceCreator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);

        ctorSB = new StringBuilder();
        initSB = new StringBuilder();
        evalSB = new StringBuilder();
        varSB = new StringBuilder();
        getterSB = new StringBuilder();
    }
    
    /**
     * Call the start rule of the source creation parser.
     *
     * @param model evaluation model for the runtime class being created
     * @param runtimeClassName runtime class name
     * @throws RecognitionException on errors walking the input AST
     */
    public abstract void start(Jiffle.EvaluationModel model, String runtimeClassName) throws RecognitionException;

    /**
     * Gets the source for the runtime class constructor.
     *
     * @return constructor source
     */
    public String getCtorSource() {
        return ctorSB.toString();
    }

    /**
     * Gets the source for the method that initializes runtime class fields
     * (Jiffle image-scope variables).
     *
     * @return init method source
     */
    public String getInitSource() {
        return initSB.toString();
    }

    /**
     * Gets the source for the evaluate method.
     *
     * @return evaluate method source
     */
    public String getEvalSource() {
        return evalSB.toString();
    }
    
    /**
     * Gets the source for the runtime class field declarations
     * (Jiffle image scope variables).
     *
     * @return runtime class field source
     */
    public String getVarSource() {
        return varSB.toString();
    }

    /**
     * Gets the source for the field variable getter method.
     *
     * @return getter method source
     */
    public String getGetterSource() {
        return getterSB.toString();
    }

    /**
     * Looks up the runtime source for a Jiffle function.
     *
     * @param name function name
     * @param numArgs number of arguments
     *
     * @return runtime source
     */
    protected String getRuntimeExpr(String name, int numArgs) {
        try {
            return FunctionLookup.getRuntimeExpr(name, numArgs);
        } catch (UndefinedFunctionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Creates a uniquely named, local variable for use in a runtime class method.
     *
     * @param type variable type name
     *
     * @return variable name
     */
    protected String makeLocalVar(String type) {
        LocalVar var = new LocalVar();
        var.type = type;
        var.name = "_local" + localVars.size();
        localVars.add(var);
        return var.name;
    }

}
