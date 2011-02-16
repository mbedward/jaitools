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
    
    /** Evaluation model of the runtime class being generated */
    Jiffle.EvaluationModel model;
    
    /** Runtime class name */
    String className;

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
    
    protected int numLocalVars = 0;
    protected int numImageScopeVars = 0;

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
    
    protected void initializeSources(Jiffle.EvaluationModel model, String className) {
        this.model = model;
        this.className = className;
        
        ctorSB = new StringBuilder();
        ctorSB.append("public ").append(className).append("() { \n");

        initSB = new StringBuilder();
        initSB.append("protected void initImageScopeVars() { \n");

        evalSB = new StringBuilder();
        switch (model) {
            case DIRECT:
                evalSB.append("public void evaluate(int _x, int _y) { \n");
                break;

            case INDIRECT:
                evalSB.append("public double evaluate(int _x, int _y) { \n");
                break;

            default:
                throw new IllegalArgumentException("Invalid evaluation model parameter");
        }

        varSB = new StringBuilder();
        getterSB = new StringBuilder();
    }
    
    protected void finalizeSources() {
        ctorSB.append("} \n");
        evalSB.append("} \n");
        initSB.append("} \n");
        
        if (numImageScopeVars > 0) {
            getterSB.append("return null; \n");
            getterSB.append("} \n");
        }
    }
    
    protected void addImageScopeVar(String varName, ExprSrcPair pair) {
        if (numImageScopeVars == 0) {
            getterSB.append("public Double getVar(String varName) { \n");
        }

        varSB.append("double ").append(varName).append("; \n");

        if (pair != null) {
            if (pair.priorSrc != null) initSB.append(pair.priorSrc);
            initSB.append(varName).append(" = ").append(pair.src).append("; \n");
        }

        if (numImageScopeVars > 0) {
            getterSB.append(" else ");
        }
        getterSB.append("if (\"").append(varName).append("\".equals(varName)) { \n");
        getterSB.append("return ").append(varName).append("; \n");
        getterSB.append("} \n");
        
        numImageScopeVars++ ;
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
    protected String makeLocalVar(String prefix) {
        StringBuilder sb = new StringBuilder("_");
        if (prefix != null && prefix.length() > 0) {
            sb.append(prefix);
        } else {
            sb.append("local");
        }
        sb.append(numLocalVars++);
        return sb.toString();
    }
    
    protected String makeAssignment(String varName, String op, String prior, String expr) {
        StringBuilder sb = new StringBuilder();
        if (prior != null) {
            sb.append(prior);
        }

        sb.append(varName).append(" ").append(op).append(" ").append(expr);
        return sb.toString();
    }
    
    protected String makeFunctionCall(String fnName, List<String> args) {
        final int n = args.size();
        StringBuilder sb = new StringBuilder();
        
        try {
            FunctionInfo info = FunctionLookup.getInfo(fnName, n);
            sb.append(info.getRuntimeExpr()).append("(");

            // Work around Janino not handling vararg methods
            // or generic collections
            if (info.isVarArg()) {
                sb.append("new Double[]{");
            }

            int k = 0;
            for (String arg : args) {
                sb.append(arg);
                if (++k < n) {
                    sb.append(", ");
                }
            }
            if (info.isVarArg()) {
                sb.append("}");
            }
            sb.append(")");
            return sb.toString();

        } catch (UndefinedFunctionException ex) {
            // An undefined function should hae been picked up 
            // by the compiler prior to being here
            throw new RuntimeException(ex);
        }
    }
    
    protected ExprSrcPair makeIfCall(List<String> args) {
        StringBuilder sb = new StringBuilder();

        String signFn = getRuntimeExpr("sign", 1);

        String condVar = makeLocalVar("test");
        sb.append("Integer ").append(condVar).append(" = ");
        sb.append(signFn).append("(").append(args.get(0)).append("); \n");

        String resultVar = makeLocalVar("result");
        sb.append("double ").append(resultVar).append("; \n");

        sb.append("if (").append(condVar).append(" == null) { \n");
        sb.append(resultVar).append(" = Double.NaN; \n");
        sb.append("} else { \n");

        switch (args.size()) {
            case 1:
                sb.append("if (").append(condVar).append(" != 0 ) { \n");
                sb.append(resultVar).append(" = 1; \n");
                sb.append("} else { \n");
                sb.append(resultVar).append(" = 0; \n");
                sb.append("} \n");
                break;

            case 2:
                sb.append("if (").append(condVar).append(" != 0 ) { \n");
                sb.append(resultVar).append(" = ").append(args.get(1)).append("; \n");
                sb.append("} else { \n");
                sb.append(resultVar).append(" = 0; \n");
                sb.append("} \n");
                break;

            case 3:
                sb.append("if (").append(condVar).append(" != 0 ) { \n");
                sb.append(resultVar).append(" = ").append(args.get(1)).append("; \n");
                sb.append("} else { \n");
                sb.append(resultVar).append(" = ").append(args.get(2)).append("; \n");
                sb.append("} \n");
                break;

            case 4:
                sb.append("if (").append(condVar).append(" > 0 ) { \n");
                sb.append(resultVar).append(" = ").append(args.get(1)).append("; \n");
                sb.append("} else if (").append(condVar).append(" == 0) { \n");
                sb.append(resultVar).append(" = ").append(args.get(2)).append("; \n");
                sb.append("} else { \n");
                sb.append(resultVar).append(" = ").append(args.get(3)).append("; \n");
                sb.append("} \n");
                break;

            default:
                throw new IllegalArgumentException("if function error");
        }

        sb.append("} \n");
        
        return new ExprSrcPair(sb.toString(), resultVar);
    }
    
    protected String makeWriteToImage(String imgVarName, String prior, String expr) {
                
        StringBuilder sb = new StringBuilder();
        if (prior != null) {
            sb.append(prior);
        }

        switch (model) {
            case DIRECT:
                sb.append("writeToImage(");
                sb.append("\"").append(imgVarName).append("\", ");
                sb.append("_x, _y, 0, ");
                sb.append(expr).append(" )");
                break;

            case INDIRECT:
                sb.append("return ").append(expr);
        }
        
        return sb.toString();
    }
    
    
    protected String makeWhileLoop(String prior, String expr,
            String statement, boolean isBlockStatement) {
    
        return makeConditionalLoop(JiffleParser.WHILE, 
                prior, expr, statement, isBlockStatement);
    }
    
    
    protected String makeUntilLoop(String prior, String expr,
            String statement, boolean isBlockStatement) {
    
        return makeConditionalLoop(JiffleParser.UNTIL, 
                prior, expr, statement, isBlockStatement);
    }
    
    
    private String makeConditionalLoop(int loopType,
            String prior, String expr,
            String statement, boolean isBlockStatement) {

        StringBuilder sb = new StringBuilder();
        sb.append("while (");
        sb.append(getRuntimeExpr("sign", 1));
        sb.append("(").append(expr).append(")");
        switch (loopType) {
            case JiffleParser.WHILE:
                sb.append(" != 0) ");
                break;

            case JiffleParser.UNTIL:
                sb.append(" == 0) ");
                break;

            default:
                throw new RuntimeException("Unknown loop type");
        }
        
        sb.append(statement);
        String eol = isBlockStatement ? "\n" : ";\n";
        sb.append(eol);
        
        return sb.toString();
    }
    
    protected String makeForEachLoop(String loopVar, 
            ExprSrcPair loopSet, int loopSetSize,
            String statement, boolean isBlockStatement) {
    
        StringBuilder sb = new StringBuilder();
        sb.append(loopSet.priorSrc);
        
        String counterVar = makeLocalVar("index");
        sb.append("for (int ").append(counterVar).append("=0; ");
        sb.append(counterVar).append("<").append(loopSetSize).append("; ");
        sb.append(counterVar).append("++ )");
        
        StringBuilder loopVarSB = new StringBuilder();
        loopVarSB.append("double ").append(loopVar).append("=").append(loopSet.src);
        loopVarSB.append("[").append(counterVar).append("];\n");
        
        if (isBlockStatement) {
            String insert = "{\n" + loopVarSB.toString();
            sb.append(statement.replaceFirst("\\{\\s*", insert));
            
        } else {
            sb.append("{ \n");
            sb.append(loopVarSB.toString());
            sb.append(statement).append(";\n");
            sb.append("}");
        }
        
        return sb.toString();
    }
    
    protected ExprSrcPair makeLoopSet(List<String> exprList) {
        StringBuilder sb = new StringBuilder();
        
        String var = makeLocalVar("loopset");
        sb.append("Double[] ").append(var).append(" = { \n");
        
        final int n = exprList.size();
        int k = 0;
        for (String expr : exprList) {
            sb.append(expr);
            if (++k < n) sb.append(", \n");
        }
        sb.append("\n}; \n");
        
        return new ExprSrcPair(sb.toString(), var);
    }
    
    protected String makeBinaryExpression(int type,
            String prior1, String expr1, String prior2, String expr2) {
    
        StringBuilder sb = new StringBuilder();
        String fn;
        
        switch (type) {
            case JiffleParser.POW:
                sb.append("Math.pow").append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.TIMES:
                sb.append(expr1).append(" * ").append(expr2);
                break;
                
            case JiffleParser.DIV:
                sb.append(expr1).append(" / ").append(expr2);
                break;
                
            case JiffleParser.MOD:
                sb.append(expr1).append(" % ").append(expr2);
                break;
                
            case JiffleParser.PLUS:
                sb.append(expr1).append(" + ").append(expr2);
                break;
                
            case JiffleParser.MINUS:
                sb.append(expr1).append(" - ").append(expr2);
                break;
                
            case JiffleParser.OR:
                fn = getRuntimeExpr("OR", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.AND:
                fn = getRuntimeExpr("AND", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.XOR:
                fn = getRuntimeExpr("XOR", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;

            case JiffleParser.GT:
                fn = getRuntimeExpr("GT", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.GE:
                fn = getRuntimeExpr("GE", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.LT:
                fn = getRuntimeExpr("LT", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.LE:
                fn = getRuntimeExpr("LE", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.LOGICALEQ:
                fn = getRuntimeExpr("EQ", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            case JiffleParser.NE:
                fn = getRuntimeExpr("NE", 2);
                sb.append(fn).append(makeArgList(expr1, expr2));
                break;
                
            default:
                throw new IllegalArgumentException("Unknown operator type: " + type);
        }

        return sb.toString();
    }
    
    private String makeArgList(String ...args) {
        StringBuilder sb = new StringBuilder("(");
        final int n = args.length;
        for (int i = 0; i < n; i++) {
            sb.append(args[i]);
            if (i < n-1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}

