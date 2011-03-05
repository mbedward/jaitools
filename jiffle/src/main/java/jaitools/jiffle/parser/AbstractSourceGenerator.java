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
import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.stringtemplate.StringTemplateGroup;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import jaitools.jiffle.JiffleProperties;

/**
 * Base class for tree parsers that generate Jiffle runtime source.
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
    
    /** The runtime model to generate source for. */
    protected Jiffle.RuntimeModel model;

    /** The package name to use for the runtime class. */
    protected String pkgName;

    /** The imports to be included with the runtime class. */
    protected List<String> imports;

    /** The runtime class name. */
    protected String className;

    /** The name of the base class for the runtime class. */
    protected String baseClassName;
    
    /** A counter used in naming variables inserted into the runtime source. */
    protected int varIndex = 0;
    

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
        
        this.imports = CollectionFactory.list();
        String value = JiffleProperties.get(JiffleProperties.IMPORTS_KEY);
        if (value != null && !(value.trim().length() == 0)) {
            this.imports.addAll( Arrays.asList( 
                    value.split(JiffleProperties.RUNTIME_IMPORTS_DELIM) ) );
        }
    }
    
    /**
     * {@inheritDoc}
     */    
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

    /**
     * {@inheritDoc}
     */
    public void setBaseClassName(String baseClassName) {
        this.baseClassName = baseClassName;
    }

    /**
     * {@inheritDoc}
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
            return generate().getTemplate().toString();

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
    protected abstract RuleReturnScope generate() throws RecognitionException;

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
     * @param argTypes argument type names; null or empty for no-arg functions
     *
     * @return runtime source
     */
    protected String getRuntimeExpr(String name, List<String> argTypes) {
        try {
            return FunctionLookup.getRuntimeExpr(name, argTypes);
        } catch (UndefinedFunctionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    
    /**
     * Looks up the runtime source for a Jiffle function.
     *
     * @param name function name
     * @param argTypes argument type names; null or empty for no-arg functions
     *
     * @return runtime source
     */
    protected String getRuntimeExpr(String name, String ...argTypes) {
        return getRuntimeExpr(name, Arrays.asList(argTypes));
    }
    
    
    /**
     * Gets the runtime source for a script option name:value pair.
     * 
     * @param name option name
     * @param value option value
     * @return the runtime source
     */
    protected String getOptionExpr(String name, String value) {
        try {
            return OptionLookup.getActiveRuntimExpr(name, value);
        } catch (UndefinedOptionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    
    /**
     * Adds the given imports to those that will be included in the
     * runtime source.
     * 
     * @param importNames fully qualified class names
     */
    protected void addImport(String ...importNames) {
        for (String name : importNames) {
            boolean found = false;
            for (String imp : imports) {
                if (imp.equals(name)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                imports.add(name);
            }
        }
    }

}

