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

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.TreeNodeStream;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleProperties;
import java.util.Arrays;
import org.antlr.runtime.RecognizerSharedState;

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
    
    private class SourceElement {
        final StringBuilder sb = new StringBuilder();

        public StringBuilder append(String src) {
            sb.append(src);
            return sb;
        }
        
        public String getSource() {
            return sb.toString();
        }
    }
    
    private final SourceElement VARS;
    private final SourceElement CTOR;
    private final SourceElement INIT;
    private final SourceElement OPTION_INIT;
    private final SourceElement EVAL;
    private final SourceElement GETTER;
        
    
    /** Evaluation model of the runtime class being generated */
    Jiffle.EvaluationModel model;
    
    /** Runtime base class name */
    String baseClassName;

    protected int numLocalVars = 0;
    protected int numImageScopeVars = 0;
    
    protected Map<String, String> options;
    
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
    public AbstractRuntimeSourceCreator(TreeNodeStream input,
            Jiffle.EvaluationModel model, String baseClassName) {
        super(input);
        
        this.model = model;
        this.baseClassName = baseClassName;
        
        options = CollectionFactory.map();
        
        VARS = new SourceElement();
        CTOR = new SourceElement();
        INIT = new SourceElement();
        OPTION_INIT = new SourceElement();
        EVAL = new SourceElement();
        GETTER = new SourceElement();
    }
    
    /**
     * This constructor is only defined to appease the compiler and should not be used.
     */
    protected AbstractRuntimeSourceCreator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
        throw new IllegalStateException("Incorrect constructor used");
    }
    
    public synchronized String getSource(String jiffleScript) {
        StringBuilder sb  = new StringBuilder();

        sb.append("package ");
        sb.append(JiffleProperties.get(JiffleProperties.RUNTIME_PACKAGE_KEY)).append("; \n\n");
        
        String value = JiffleProperties.get(JiffleProperties.IMPORTS_KEY);
        if (value != null && !(value.trim().length() == 0)) {
            String[] importNames = value.split(JiffleProperties.RUNTIME_IMPORTS_DELIM);
            for (String importName : importNames) {
                sb.append("import ").append(importName).append("; \n");
            }
            sb.append("\n");
        }
        
        if (jiffleScript != null && jiffleScript.length() > 0) {
            sb.append(formatAsJavadoc(jiffleScript));
        }
        
        sb.append("public class ");
        String className = null;
        
        switch (model) {
            case DIRECT:
                className = JiffleProperties.get(JiffleProperties.DIRECT_CLASS_KEY);
                break;
                
            case INDIRECT:
                className = JiffleProperties.get(JiffleProperties.INDIRECT_CLASS_KEY);
                break;
                
            default:
                throw new IllegalArgumentException("Internal compiler error");
        }
        sb.append(className);

        sb.append(" extends ").append(baseClassName).append(" { \n");

        SourceElement[] elements = {VARS, CTOR, INIT, OPTION_INIT, EVAL, GETTER};
        for (SourceElement element : elements) {
            String src = element.getSource();
            sb.append(formatSource(src, 4));
            sb.append("\n");
        }

        sb.append("} \n");
        return sb.toString();
    }
    
    
    /**
     * Formats the input text as a javadoc block.
     * 
     * @param text the text to format
     * 
     * @return the javadoc block
     */
    private String formatAsJavadoc(String text) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("/** \n");
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.length() > 0) {
                sb.append(" * ").append(line).append("\n");
            }
        }
        sb.append(" */ \n");
        return sb.toString();
    }
    
    /**
     * A simple code formatter.
     * 
     * @param source source code to format
     * @param baseIndent initial indentation level (number of spaces)
     * 
     * @return formatted code
     */
    private String formatSource(String source, int baseIndent) {
        StringBuilder sb = new StringBuilder();
        int indent = baseIndent;
        
        char[] spaces = new char[100];
        Arrays.fill(spaces, ' ');
        
        String[] lines = source.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("}")) indent -= 4;
            
            sb.append(spaces, 0, indent);
            sb.append(line.trim()).append("\n");
            
            if (line.endsWith("{")) indent += 4;
        }
        
        return sb.toString();
    }

    /**
     * Call the start rule of the source creation parser.
     *
     * @throws RecognitionException on errors walking the input AST
     */
    public abstract void start() throws RecognitionException;

    
    protected void initializeSources() {
        String className = null;
        switch (model) {
            case DIRECT:
                className = JiffleProperties.get(JiffleProperties.DIRECT_CLASS_KEY);
                break;
                
            case INDIRECT:
                className = JiffleProperties.get(JiffleProperties.INDIRECT_CLASS_KEY);
                break;
        }
        
        CTOR.append("public ").append(className).append("() { \n");

        INIT.append("protected void initImageScopeVars() { \n");
        
        GETTER.append("public Double getVar(String varName) { \n");

        switch (model) {
            case DIRECT:
                EVAL.append("public void evaluate(int _x, int _y) { \n");
                break;

            case INDIRECT:
                EVAL.append("public double evaluate(int _x, int _y) { \n");
                break;

            default:
                throw new IllegalArgumentException("Invalid evaluation model parameter");
        }
    }
    
    protected void finalizeSources() {
        CTOR.append("} \n");
        EVAL.append("} \n");
        INIT.append("} \n");
        
        GETTER.append("return null; \n} \n");
        
        OPTION_INIT.append("protected void initOptionVars() { \n");
        String src;
        for (String name : OptionLookup.getNames()) {
            if (options.containsKey(name)) {
                src = OptionLookup.getActiveRuntimExpr(name, options.get(name));
            } else {
                src = OptionLookup.getDefaultRuntimeExpr(name);
            }
            
            OPTION_INIT.append(src).append("\n");
        }
        OPTION_INIT.append("} \n");
    }
    
    protected void setOption(String name, String value) {
        options.put(name, value);
    }
    
    protected void addStatement(String src, boolean isBlock) {
        EVAL.append(src);
        String eol = isBlock ? "\n" : ";\n";
        EVAL.append(eol);
    }
    
    protected void addImageScopeVar(String varName, ExprSrcPair pair) {
        VARS.append("double ").append(varName).append("; \n");

        if (pair != null) {
            if (pair.priorSrc != null) INIT.append(pair.priorSrc);
            INIT.append(varName).append(" = ").append(pair.src).append("; \n");
        }

        if (numImageScopeVars > 0) {
            GETTER.append(" else ");
        }
        GETTER.append("if (\"").append(varName).append("\".equals(varName)) { \n");
        GETTER.append("return ").append(varName).append("; \n");
        GETTER.append("} \n");
        
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
    
    protected String makeForEachListLoop(String loopVar, 
            List<String> exprList, String statement, boolean isBlockStatement) {
                
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
        
        String counterVar = makeLocalVar("index");
        sb.append("for (int ").append(counterVar).append("=0; ");
        sb.append(counterVar).append("<").append(n).append("; ");
        sb.append(counterVar).append("++ )");
        
        StringBuilder loopVarSB = new StringBuilder();
        loopVarSB.append("double ").append(loopVar).append("=").append(var);
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
    
    protected String makeForEachSequenceLoop(String loopVar, 
            String loPrior, String loSrc, String hiPrior, String hiSrc,
            String statement) {

        StringBuilder sb = new StringBuilder();

        String loVar = makeLocalVar("sequence");
        String hiVar = makeLocalVar("sequence");
        
        if (loPrior != null && loPrior.length() > 0) {
            sb.append(loPrior).append("\n");
        }
        sb.append("int ").append(loVar).append(" = (int)(").append(loSrc).append(");\n");
        
        if (hiPrior != null && hiPrior.length() > 0) {
            sb.append(hiPrior).append("\n");
        }
        sb.append("int ").append(hiVar).append(" = (int)(").append(hiSrc).append(");\n");
        
        sb.append("for (int ").append(loopVar).append(" = ").append(loVar).append("; ");
        sb.append(loopVar).append(" <= ").append(hiVar).append("; ");
        sb.append(loopVar).append("++) ");
        sb.append(statement);
        
        return sb.toString();
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
    
    protected String makeBreakIf(String prior, String src) {
        StringBuilder sb = new StringBuilder();
        
        if (prior != null && prior.length() > 0) {
            sb.append(prior);
        }
        
        String signFn = getRuntimeExpr("sign", 1);
        sb.append("if (").append(signFn).append("(").append(src);
        sb.append(") != 0) ").append("break");
        
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

