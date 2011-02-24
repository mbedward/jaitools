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

import java.io.InputStream;
import java.io.InputStreamReader;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.stringtemplate.StringTemplateGroup;

import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import jaitools.jiffle.JiffleProperties;

/**
 * Base class for runtime source generating tree parsers.
 * <p>
 * The runtime source generator is created from the ANTLR grammar
 * ({@code RuntimeSourceGenerator.g}. This class provides a small number
 * of common methods and fields.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AbstractSourceGenerator extends ErrorHandlingTreeParser implements SourceGenerator {
    
    protected Jiffle.RuntimeModel model;
    protected String pkgName;
    protected String[] imports;
    protected String className;
    protected String baseClassName;
    
    protected int varIndex = 0;
    
    public void setRuntimeModel(Jiffle.RuntimeModel model) {
        this.model = model;
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
    }
    
    public void setBaseClassName(String baseClassName) {
        this.baseClassName = baseClassName;
    }


    /**
     * Constructor called by ANTLR.
     *
     * @param input AST node stream
     */
    public AbstractSourceGenerator(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    
    /**
     * Constructor called by ANTLR.
     * 
     * @param input AST node stream
     * @param state parser state (not used by Jiffle directly)
     */
    protected AbstractSourceGenerator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);

        this.pkgName = JiffleProperties.get(JiffleProperties.RUNTIME_PACKAGE_KEY);
        
        String value = JiffleProperties.get(JiffleProperties.IMPORTS_KEY);
        if (value != null && !(value.trim().length() == 0)) {
            this.imports = value.split(JiffleProperties.RUNTIME_IMPORTS_DELIM);
        } else {
            this.imports = new String[0];
        }
    }
    
    
    /**
     * Returns the source for the runtime class. The runtime model and base class
     * name must be set before calling this method.
     * 
     * @return source of the runtime class as a single String.
     * @throws JiffleException on errors creating source
     * @throws RuntimeException if the runtime model or base class name are not set
     * 
     * @see #setRuntimeModel(jaitools.jiffle.Jiffle.RuntimeModel) 
     * @see #setBaseClassName(java.lang.String) 
     */
    public String getSource() throws JiffleException {
        if (model == null) {
            throw new RuntimeException("Runtime model has not been set");
        }
        if (baseClassName == null || baseClassName.trim().length() == 0) {
            throw new RuntimeException("Base class name has not been set");
        }
        
        String commonTemplateFile = JiffleProperties.get(JiffleProperties.COMMON_SOURCE_TEMPLATES_KEY);
        String modelTemplateFile = null;
        switch (model) {
            case DIRECT:
                modelTemplateFile = JiffleProperties.get(JiffleProperties.DIRECT_SOURCE_TEMPLATES_KEY);
                break;
                
            case INDIRECT:
                modelTemplateFile = JiffleProperties.get(JiffleProperties.INDIRECT_SOURCE_TEMPLATES_KEY);
                break;
        }
        
        try {
            InputStream strm = AbstractSourceGenerator.class.getResourceAsStream(commonTemplateFile);
            InputStreamReader reader = new InputStreamReader(strm);
            StringTemplateGroup commonSTG = new StringTemplateGroup(reader);
            reader.close();

            strm = AbstractSourceGenerator.class.getResourceAsStream(modelTemplateFile);
            reader = new InputStreamReader(strm);
            StringTemplateGroup modelSTG = new StringTemplateGroup(reader);
            setTemplateLib(modelSTG);
            reader.close();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            setErrorReporter(new DeferredErrorReporter());
            return start().getTemplate().toString();

        } catch (RecognitionException ex) {
            if (errorReporter != null && errorReporter.getNumErrors() > 0) {
                throw new JiffleException(errorReporter.getErrors());
            } else {
                throw new JiffleException(
                        "Error creating runtime source. No details available.");
            }
        }
    }
    
    /**
     * Starts generating source code based on the abstract syntax tree 
     * produced by the Jiffle compiler.
     * 
     * @return an ANTLR rule return object from which the results can be
     *         retrieved
     * 
     * @throws RecognitionException on errors processing the AST
     */
    protected abstract RuleReturnScope start() throws RecognitionException;

    /**
     * Used internally to set the string templates for source generation.
     * Declared public to accord with the underlying ANTLR tree parser.
     * 
     * @param templateLib source generation templates
     */
    public abstract void setTemplateLib(StringTemplateGroup templateLib);
    
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
    
    
    protected String getOptionExpr(String name, String value) {
        return OptionLookup.getActiveRuntimExpr(name, value);
    }

}

