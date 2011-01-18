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
import jaitools.jiffle.parser.FunctionValidator;
import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
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
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.BufferedTreeNodeStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.codehaus.janino.SimpleCompiler;


/**
 * This class is the starting point for compiling and running a Jiffle script.
 * It takes the script and a corresponding Map of image parameters which define
 * the relationship between image variables in the script and image objects.
 * The script is then compiled in a series of error checking and optimizing
 * steps.  If compilation is successful the Jiffle object will now contain
 * an executable form of the script which can be run directly by passing the object
 * to a new instance of {@link jaitools.jiffle.runtime.JiffleRunner} as in this example:
 * <pre><code>
 * RenderedImage inImg = ...  // get an input image
 * 
 *  // create an image to write output values to
 *  TiledImage outImg = JiffleUtilities.createDoubleImage(100, 100);
 *       
 *  // relate variable names in script to image objects
 *  Map<String, RenderedImage> imgParams = CollectionFactory.newMap();
 *  imgParams.put("result", outImg);
 *  imgParams.put("img1", inImg);
 *
 *  // get the script as a string and create a Jiffle object
 *  String script = ... 
 *  boolean success = false;
 *  try {
 *      Jiffle jif = new Jiffle(script, imgParams);
 *      if (jif.isCompiled()) {
 *         JiffleRunner runner = new JiffleRunner(jif);
 *         success = runner.run();
 *      }
 *  } catch (JiffleCompilationException cex) {
 *      cex.printStackTrace();
 *  } catch (JiffleInterpeterException iex) {
 *      iex.printStackTrace();
 *  }
 * 
 *  if (success) {
 *     // display result ...
 *  }
 * </code></pre>
 * 
 * Alternatively, the compiled Jiffle can be run indirectly by submitting the
 * script to a {@link jaitools.jiffle.runtime.JiffleInterpreter} object which runs each submitted script 
 * in a separate thread.
 * <pre><code>
 *  public class Foo
 *      private JiffleInterpreter interp;
 * 
 *      public Foo() {
 *          interp = new JiffleInterpreter();
 * 
 *          // set up methods to listen for interpreter events
 *          interp.addEventListener(new JiffleEventListener() {
 *              public void onCompletionEvent(JiffleCompletionEvent ev) {
 *                  onCompletion(ev);
 *              }
 *
 *              public void onFailureEvent(JiffleFailureEvent ev) {
 *                  onFailure(ev);
 *              }
 *          });
 *      }
 *
 *      public void runScript(String script, int imgWidth, int imgHeight) {
 *          // create an image to write output data to
 *          TiledImage tImg = JiffleUtilities.createDoubleImage(imgWidth, imgHeight);
 *
 *          Map<String, RenderedImage> imgParams = CollectionFactory.newMap();
 *          imgParams.put("result", tImg);
 *          // ...plus other entries if there are input images
 *
 *          // compile the script and submit it to the interpreter
 *          try {
 *              Jiffle j = new Jiffle(prog, imgParams);
 *              if (j.isCompiled()) {
 *                  interp.submit(j);
 *              }
 *          } catch (JiffleCompilationException cex) {
 *              cex.printStackTrace();
 *          } catch (JiffleInterpreterException iex) {
 *              iex.printStackTrace();
 *          }
 *      }
 *
 *      // Respond to completion events
 *      private void onCompletion(JiffleCompletionEvent ev) {
 *          RenderedImage img = ev.getJiffle().getImage("result");
 * 
 *          // display or write image ...
 *      }
 * 
 *      // Respond to failure events
 *      private void onFailure(JiffleFailureEvent ev) {
 *          System.out.println("Bummer...");
 *      }
 *  }
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Jiffle {
    
    private static final String PROPERTIES_FILE = "META-INF/compiler.properties";

    private static final Properties defaults = new Properties();
    
    private static final String RUNTIME_PACKAGE_KEY = "runtime.package";
    private static final String RUNTIME_CLASS_KEY = "runtime.class";
    private static final String RUNTIME_BASE_CLASS_KEY = "runtime.base.class";
    
    private static final Class<? extends JiffleRuntime> DEFAULT_BASE_CLASS;
    
    static {
        InputStream in = null;
        try {
            in = Jiffle.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            defaults.load(in);
            
            String baseClassName = defaults.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                    defaults.getProperty(RUNTIME_BASE_CLASS_KEY);
            
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

    private String script;
    
    private CommonTree primaryAST;
    private CommonTree finalAST;
    private CommonTokenStream tokens;
    
    private Class<? extends JiffleRuntime> runtimeBaseClass;
    private JiffleRuntime runtimeInstance;
    private String runtimeSource;
    
    private Map<String, ImageRole> imageParams;
    private Set<String> vars;
    private Set<String> unassignedVars;
    private Set<String> outputImageVars;
    
//    private Metadata metadata;
    private Map<String, ErrorCode> errors;
    

    /**
     * Creates a new instance by compiling the provided script. The image
     * names forming keys in the {@code params} argument are used by the
     * compiler to distinguish between image variables and other user-defined
     * variables in the script.
     * 
     * @param script Jiffle source code to compile
     * 
     * @param params defines the names and roles of image variables
     *        referred to in the script.
     * 
     * @throws JiffleCompilationException on error compiling the script
     */
    public Jiffle(String script, Map<String, ImageRole> params)
            throws JiffleCompilationException {

        init(script, params);
    }

    /**
     * Creates a new instance by compiling a script read from file. The image
     * names forming keys in the {@code params} argument are used by the
     * compiler to distinguish between image variables and other user-defined
     * variables in the script.
     * 
     * @param scriptFile file containing Jiffle source code to compile
     * 
     * @param params defines the names and roles of image variables
     *        referred to in the script.
     * 
     * @throws IOException on error reading the script file
     * @throws JiffleCompilationException on error compiling the script
     */
    public Jiffle(File scriptFile, Map<String, ImageRole> params)
            throws JiffleCompilationException, IOException {

        BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0) {
                sb.append(line);
                sb.append('\n');  // put the newline back on for the parser
            }
        }
        String prog = sb.toString();
        
        init(prog, params);
    }
    
    /**
     * Tests whether the script was compiled successfully.
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
    
    public String getRuntimeSource() {
        if (runtimeSource == null) {
            try {
                createRuntimeInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return runtimeSource;
    }

    /**
     * Helper function for constructors
     * @param script input jiffle statements
     * @param params variable names and their corresponding images
     */
    private void init(String script, Map<String, ImageRole> params)
            throws JiffleCompilationException {

        this.runtimeBaseClass = DEFAULT_BASE_CLASS;
        
        this.imageParams = CollectionFactory.map();
        this.imageParams.putAll(params);

        // add extra new line just in case last statement hits EOF
        this.script = script + "\n";
        compile();
    }

    /**
     * Called on object construction to compile the script into an optimized 
     * Abstract Syntax Tree (AST) suitable for execution. 
     * The compilation process includes:
     * <ul>
     * <li> Check for recognized function names in function calls.
     * <li> Check for errors with variable use (e.g. use of a local 
     *      variable in an expression before it has been assigned
     *      a value.
     * <li> Categorizing variables and expressions to identify elements
     *      of the script that can be pre-calculated prior to
     *      execution.
     * <li> Pre-calculation and optimizing expressions.
     * </ul>
     * @throws JiffleCompilationExceptions if errors occur while compiling
     * 
     * @todo better system for error and warning reporting
     */
    private void compile() throws JiffleCompilationException {
        if (script != null && script.length() > 0) {
            buildAST();
            
            if (!checkFunctionCalls()) {
                throw new JiffleCompilationException(getErrorString());
            }
            
            if (!classifyVars()) {
                throw new JiffleCompilationException(getErrorString());
            }
            
            transformVars();
        }
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
     * @throws jaitools.jiffle.interpreter.JiffleCompilationException
     */
    private void buildAST() throws JiffleCompilationException {
        try {
            ANTLRStringStream input = new ANTLRStringStream(script);
            JiffleLexer lexer = new JiffleLexer(input);
            tokens = new CommonTokenStream(lexer);

            JiffleParser parser = new JiffleParser(tokens);
            JiffleParser.prog_return r = parser.prog();
            primaryAST = (CommonTree) r.getTree();

        } catch (RecognitionException re) {
            throw new JiffleCompilationException(
                    "error in script at or around line:" +
                    re.line + " col:" + re.charPositionInLine);
        }
    }

    /**
     * Examine function calls in the AST built by {@link #buildAST()} and
     * check that we recognize them all
     */
    private boolean checkFunctionCalls() throws JiffleCompilationException {
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
    private boolean classifyVars() throws JiffleCompilationException {
        VarClassifier classifier = null;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            classifier = new VarClassifier(nodes);
            classifier.setImageParams(imageParams);
            classifier.start();
            
            errors = classifier.getErrors();
            for (ErrorCode error : errors.values()) {
                if (error.isError()) return false;
            }

        } catch (RecognitionException ex) {
            // anything at this stage is probably programmer error
            throw new RuntimeException(ex);
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

    private void createRuntimeInstance() throws Exception {
        BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(finalAST);
        nodes.setTokenStream(tokens);
        RuntimeSourceCreator me = new RuntimeSourceCreator(nodes);
        me.compile();
        
        createRuntimeSource(me.getSource());
        
        SimpleCompiler compiler = new SimpleCompiler();
        compiler.cook(runtimeSource);
        Class<?> clazz = compiler.getClassLoader().loadClass(
                defaults.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                defaults.getProperty(RUNTIME_CLASS_KEY));
        
        runtimeInstance = (JiffleRuntime) clazz.newInstance();
    }
    
    private void createRuntimeSource(String evalSource) throws Exception {
        StringBuilder sb  = new StringBuilder();

        sb.append("package ").append(defaults.getProperty(RUNTIME_PACKAGE_KEY)).append("; \n\n");
        sb.append("public class ").append(defaults.getProperty(RUNTIME_CLASS_KEY));
        sb.append(" extends ").append(runtimeBaseClass.getName()).append(" { \n");
        sb.append(formatSource(evalSource, 4));
        sb.append("} \n");
        
        runtimeSource = sb.toString();
    }
    
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
