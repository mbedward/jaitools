/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.jiffle.parser;

import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import jaitools.CollectionFactory;

/**
 *
 * @author michael
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
