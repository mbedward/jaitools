/*
 * Copyright 2009-11 Michael Bedward
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
package jaitools.jiffle;

import jaitools.jiffle.parser.ErrorCode;
import jaitools.CollectionFactory;
import jaitools.jiffle.parser.CompilerExitException;
import jaitools.jiffle.parser.DeferredErrorReporter;
import jaitools.jiffle.parser.FunctionValidator;
import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import jaitools.jiffle.parser.ParsingErrorReporter;
import jaitools.jiffle.parser.RuntimeSourceCreator;
import jaitools.jiffle.parser.VarClassifier;
import jaitools.jiffle.parser.VarTransformer;
import jaitools.jiffle.runtime.AbstractJiffleRuntime;
import jaitools.jiffle.runtime.JiffleRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.BufferedTreeNodeStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.codehaus.janino.SimpleCompiler;


/**
 * Compiles scripts into runtime objects.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Jiffle {
    
    private static int refCount = 0;
    
    private static final String PROPERTIES_FILE = "META-INF/jiffle.properties";

    private static final Properties properties = new Properties();
    
    private static final String NAME_KEY = "root.name";
    private static final String RUNTIME_PACKAGE_KEY = "runtime.package";
    private static final String RUNTIME_CLASS_KEY = "runtime.class";
    private static final String RUNTIME_BASE_CLASS_KEY = "runtime.base.class";
    
    private static final Class<? extends JiffleRuntime> DEFAULT_BASE_CLASS;
    
    static {
        InputStream in = null;
        try {
            in = Jiffle.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            properties.load(in);
            
            String baseClassName = properties.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                    properties.getProperty(RUNTIME_BASE_CLASS_KEY);
            
            DEFAULT_BASE_CLASS = (Class<? extends JiffleRuntime>) Class.forName(baseClassName);
            
        } catch (Exception ex) {
            throw new IllegalArgumentException("Internal compiler error", ex);
            
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ex) {
                // ignore
            }
        }

    }

    /**
     * Used to specify the roles of images referenced in
     * a Jiffle script. An image may be either read-only
     * ({@link Jiffle.ImageRole#SOURCE}) or write-only
     * ({@link Jiffle.ImageRole#DEST}) but not both.
     */
    public static enum ImageRole {
        /** Indicates an image is used for input (read-only) */
        SOURCE,
        
        /** Indicates an image is used for output (write-only) */
        DEST;
    }

    /** A name: either a default or one set by the client */
    private String name;

    private String theScript;
    private CommonTree primaryAST;
    private CommonTree finalAST;
    private CommonTokenStream tokens;
    private ParsingErrorReporter errorReporter;
    
    private Class<? extends JiffleRuntime> runtimeBaseClass;
    private JiffleRuntime runtimeInstance;
    private String runtimeSource;
    private boolean sourceIncludesScript;
    
    private Map<String, ImageRole> imageParams;
    private Map<String, ErrorCode> errors;
    
    
    /**
     * Creates a new instance.
     */
    public Jiffle() {
        init();
    }
    
    /**
     * Creates a new instance by compiling the provided script. Using this
     * constructor is equivalent to:
     * <pre><code>
     * Jiffle jiffle = new Jiffle();
     * jiffle.setScript(script);
     * jiffle.setImageParams(params);
     * jiffle.compile();
     * </code></pre>
     * 
     * @param script Jiffle source code to compile
     * 
     * @param params defines the names and roles of image variables
     *        referred to in the script.
     * 
     * @throws JiffleException if there are any errors compiling the script
     */
    public Jiffle(String script, Map<String, ImageRole> params)
            throws JiffleException {

        init();
        setScript(script);
        setImageParams(params);
        compile();
    }

    /**
     * Creates a new instance by compiling the script read from {@code scriptFile}. 
     * Using this constructor is equivalent to:
     * <pre><code>
     * Jiffle jiffle = new Jiffle();
     * jiffle.setScript(scriptFile);
     * jiffle.setImageParams(params);
     * jiffle.compile();
     * </code></pre>
     * 
     * @param scriptFile file containing the Jiffle script
     * 
     * @param params defines the names and roles of image variables
     *        referred to in the script.
     * 
     * @throws JiffleException if the file cannot be read or if there are 
     *         any errors compiling the script
     */
    public Jiffle(File scriptFile, Map<String, ImageRole> params)
            throws JiffleException, IOException {

        init();
        setScript(scriptFile);
        setImageParams(params);
        compile();
    }
    
    
    /**
     * Sets the script. Calling this method will clear any previous script
     * and runtime objects.
     * 
     * @param script a Jiffle script
     */
    public final void setScript(String script) throws JiffleException {
        if (script == null || script.trim().isEmpty()) {
            throw new JiffleException("script is empty !");
        }
        
        if (theScript != null) {
            clearCompiledObjects();
        }
        
        // add extra new line just in case last statement hits EOF
        theScript = script + "\n";
    }
    
    /**
     * Sets the script. Calling this method will clear any previous script
     * and runtime objects.
     * 
     * @param scriptFile a file containing a Jiffle script
     */
    public final void setScript(File scriptFile) throws JiffleException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(scriptFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    sb.append(line);
                    sb.append('\n');  // put the newline back on for the parser
                }
            }
            
            setScript(sb.toString());
            
        } catch (IOException ex) {
            throw new JiffleException("Could not read the script file", ex);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    
    /**
     * Gets the Jiffle script.
     * 
     * @return the script or an empty {@code String} if none
     *         has been set
     */
    public String getScript() {
        return theScript == null ? "" : theScript;
    }
    
    /**
     * Sets the image parameters. These define which variables in
     * the script refer to images and their types (source or destination).
     * <p>
     * This may be called before or after setting the script. No check is
     * made between script and parameters until the script is compiled.
     * 
     * @param params the image parameters
     */
    public final void setImageParams(Map<String, ImageRole> params) {
        imageParams.clear();
        imageParams.putAll(params);
    }
    
    /**
     * Gets a copy of the current image parameters.
     * 
     * @return image parameters or an empty {@code Map} if none
     *         are set
     */
    public Map<String, ImageRole> getImageParams() {
        Map<String, ImageRole> copy = CollectionFactory.map();
        if (imageParams != null) {
            copy.putAll(imageParams);
        }
        return copy;
    }

    /**
     * Replaces the default name set for this object with a user-supplied name.
     * The name is solely for use by client code. No checks are made for 
     * duplication between current instances.
     * 
     * @param name the name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name assigned to this object. This will either be the
     * default name or one assigned by the user via {@link #setName(String)}
     */
    public String getName() {
        return name;
    }
    
    /**
     * Compiles the script into a form from which a runtime object can
     * be created.
     * 
     * @throws JiffleException if no script has been set or if any errors
     *         occur during compilation
     */
    public final void compile() throws JiffleException {
        if (theScript == null) {
            throw new JiffleException("No script has been set");
        }
        
        if (imageParams.isEmpty()) {
            throw new JiffleException("No image parameters set");
        }
        
        clearCompiledObjects();
        buildAST();

        if (!checkFunctionCalls()) {
            throw new JiffleException(getErrorString());
        }

        if (!classifyVars()) {
            throw new JiffleException(getErrorString());
        }

        transformVars();
    }
    
    /**
     * Tests whether the script has been compiled successfully.
     */
    public boolean isCompiled() {
        return (finalAST != null);
    }
    
    /**
     * Gets the runtime object for this script. 
     * <p>
     * The runtime object is an instance of {@link JiffleRuntime}.
     * It is created on the first call to any one of the {@code getRuntimeInstance}
     * methods. Subsequent calls to this function will return
     * the same instance.
     * 
     * @return the runtime object
     * 
     * @see JiffleRuntime
     * @see jaitools.jiffle.runtime.AbstractJiffleRuntime
     */
    public JiffleRuntime getRuntimeInstance() {
        return getRuntimeInstance(false);
    }

    /**
     * Gets the runtime object for this script. 
     * <p>
     * The runtime object is an instance of {@link JiffleRuntime}.
     * It is created on the first call to any one of the {@code getRuntimeInstance}
     * methods. Subsequent calls to this function will return
     * the same instance unless {@code recreate} is {@code true}.
     * 
     * @param recreate when {@code true} a new runtime object will be
     *        created and returned; when {@code false} a previously
     *        created object will be returned if one exists
     * 
     * @return the runtime object
     * 
     * @see JiffleRuntime
     * @see jaitools.jiffle.runtime.AbstractJiffleRuntime
     */
    public JiffleRuntime getRuntimeInstance(boolean recreate) {
        if (runtimeInstance == null || recreate) {
            try {
                createRuntimeInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return runtimeInstance;
    }
    
    /**
     * Gets the runtime object for this script. 
     * <p>
     * The runtime object is an instance of {@link JiffleRuntime}. By default
     * it extends an abstract base class included with JAI-tools that implements
     * all methods other than {@link JiffleRuntime#evaluate(int, int)}. 
     * <p>
     * An alternative base class can be specified with this method. 
     * 
     * @param recreate when {@code true} a new runtime object will be
     *        created and returned; when {@code false} a previously
     *        created object will be returned if one exists
     * 
     * @param baseClass an abstract base class that implements all {@link JiffleRuntime}
     *        methods other than {@link JiffleRuntime#evaluate(int, int)}; 
     *        or {@code null} to use the default base class
     * 
     * @return the runtime object
     * 
     * @see JiffleRuntime
     * @see jaitools.jiffle.runtime.AbstractJiffleRuntime
     */
    public JiffleRuntime getRuntimeInstance(boolean recreate, Class<? extends JiffleRuntime> baseClass) {
        runtimeBaseClass = baseClass == null ? DEFAULT_BASE_CLASS : baseClass;
        
        // delete any existing runtime source to be safe
        runtimeSource = null;
        
        if (runtimeInstance == null || recreate) {
            try {
                createRuntimeInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return runtimeInstance;
    }
    
    /**
     * Gets a copy of the Java source for the runtime class.
     * 
     * @param scriptInDocs whether to include the original Jiffle script
     *        in the class javadocs
     * 
     * @return source for the runtime class
     */
    public String getRuntimeSource(boolean scriptInDocs) {
        if (runtimeSource == null || scriptInDocs != sourceIncludesScript) {
            try {
                createRuntimeSource(scriptInDocs);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return runtimeSource;
    }

    /**
     * Initializes this object's name and runtime base class.
     */
    private void init() {
        Jiffle.refCount++ ;
        this.name = properties.getProperty(NAME_KEY) + refCount;

        this.runtimeBaseClass = DEFAULT_BASE_CLASS;
        imageParams = CollectionFactory.map();
    }
    
    /**
     * Clears all compiler and runtime objects
     */
    private void clearCompiledObjects() {
        primaryAST = null;
        finalAST = null;
        tokens = null;
        clearRuntimeObjects();
    }
    
    /**
     * Clears all runtime objects
     */
    private void clearRuntimeObjects() {
        errorReporter = null;
        errors = null;
        runtimeBaseClass = DEFAULT_BASE_CLASS;
        runtimeSource = null;
        sourceIncludesScript = false;
        runtimeInstance = null;
    }
    
    /**
     * Write error messages to a string
     */
    private String getErrorString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, ErrorCode> e : errors.entrySet()) {
            sb.append(e.getValue().toString());
            sb.append(": ");
            sb.append(e.getKey());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Build a preliminary AST from the jiffle script. Basic syntax and grammar
     * checks are done at this stage.
     * 
     * @throws jaitools.jiffle.interpreter.JiffleException
     */
    private void buildAST() throws JiffleException {
        try {
            ANTLRStringStream input = new ANTLRStringStream(theScript);
            JiffleLexer lexer = new JiffleLexer(input);
            tokens = new CommonTokenStream(lexer);

            JiffleParser parser = new JiffleParser(tokens);
            JiffleParser.prog_return r = parser.prog();
            primaryAST = (CommonTree) r.getTree();

        } catch (RecognitionException re) {
            throw new JiffleException(
                    "error in script at or around line:" +
                    re.line + " col:" + re.charPositionInLine);
        }
    }

    /**
     * Examine function calls in the AST built by {@link #buildAST()} and
     * check that we recognize them all
     */
    private boolean checkFunctionCalls() throws JiffleException {
        FunctionValidator validator = null;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            validator = new FunctionValidator(nodes);
            validator.start();

            if (validator.hasError()) {
                errors = validator.getErrors();
                return false;
            }
            
        } catch (RecognitionException ex) {
            // anything at this stage is probably programmer error
            throw new RuntimeException(ex);
        }
        
        return true;
    }

    /**
     * Examine variables in the AST built by {@link #buildAST()} and
     * do the following:
     * <ul>
     * <li> Identify positional and non-positional vars
     * <li> Report on vars that are used before being assigned a value
     * (all being well, these will later be tagged as input image vars
     * by {@link #validateImageVars() })
     */
    private boolean classifyVars() throws JiffleException {
        VarClassifier classifier = null;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            classifier = new VarClassifier(nodes);
            classifier.setImageParams(imageParams);
            
            /*
             * TODO: actually do something with the ANTLR error messages in the reporter
             */
            classifier.setErrorReporter(errorReporter);
            
            classifier.start();
            
            
        } catch (CompilerExitException ex) {
            // no action required
            
        } catch (RecognitionException ex) {
            throw new JiffleException("Compilation failed: error not recognized");
            
        } finally {
            errors = classifier.getErrors();
        }

        for (ErrorCode error : errors.values()) {
            if (error.isError()) return false;
        }
        return true;
    }

    /**
     * Attempt to optimize the AST by identifying variables and expressions
     * that depend only on in-built and user-defined constants and
     * pre-calculating them.
     * 
     * @return the optimized AST
     */
    private void transformVars() {
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            
            VarTransformer vt = new VarTransformer(nodes);
            vt.setImageParams(imageParams);
            VarTransformer.start_return result = vt.start();
            finalAST = (CommonTree) result.getTree();

        } catch (RecognitionException ex) {
            throw new IllegalStateException(ex);
        }        
    }

    /**
     * Creates an instance of the runtime class. The Java source for the
     * class is created if not already cached and then compiled using
     * Janino's {@link SimpleCompiler}.
     * 
     * @throws Exception 
     */
    private void createRuntimeInstance() throws JiffleException {
        if (runtimeSource == null) {
            createRuntimeSource(false);
        }
        
        try {
        SimpleCompiler compiler = new SimpleCompiler();
        compiler.cook(runtimeSource);
        Class<?> clazz = compiler.getClassLoader().loadClass(
                properties.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                properties.getProperty(RUNTIME_CLASS_KEY));
        
        runtimeInstance = (JiffleRuntime) clazz.newInstance();
        } catch (Exception ex) {
            throw new JiffleException("Janino compiler failed", ex);
        }
    }
    
    /**
     * Creates the Java source code for the runtime class.
     * 
     * @param scriptInDocs whether to include the Jiffle script in the class
     *        javadocs
     * 
     * @throws JiffleException if an error occurs generating the source 
     */
    private void createRuntimeSource(boolean scriptInDocs) throws JiffleException {
        StringBuilder sb  = new StringBuilder();

        sb.append("package ").append(properties.getProperty(RUNTIME_PACKAGE_KEY)).append("; \n\n");
        
        if (scriptInDocs) {
            sb.append(formatAsJavadoc(theScript));
        }
        
        sb.append("public class ").append(properties.getProperty(RUNTIME_CLASS_KEY));
        sb.append(" extends ").append(runtimeBaseClass.getName()).append(" { \n");
        sb.append(formatSource(astToJava(), 4));
        sb.append("} \n");
        
        runtimeSource = sb.toString();
        sourceIncludesScript = scriptInDocs;
    }
    
    /**
     * Converts the AST to Java statements.
     * 
     * @return Java souce code
     * 
     * @throws JiffleException if an error occurs parsing the AST
     */
    private String astToJava() throws JiffleException {
        ParsingErrorReporter er = new DeferredErrorReporter();
        try {
            BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(finalAST);
            nodes.setTokenStream(tokens);
            RuntimeSourceCreator rsc = new RuntimeSourceCreator(nodes);
            rsc.setErrorReporter(er);

            rsc.compile();
            return rsc.getSource();
            
        } catch (RecognitionException ex) {
            throw new JiffleException(er.getErrors());
        }
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
     * A mind-numbingly dumb code formatter.
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
            if (line.endsWith("}")) indent -= 4;
            
            sb.append(spaces, 0, indent);
            sb.append(line.trim()).append("\n");
            
            if (line.endsWith("{")) indent += 4;
        }
        
        return sb.toString();
    }

}
