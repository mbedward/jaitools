/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package jaitools.jiffle;

import jaitools.CollectionFactory;
import jaitools.jiffle.parser.FunctionValidator;
import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import jaitools.jiffle.parser.MakeRuntime;
import jaitools.jiffle.parser.Morph1;
import jaitools.jiffle.parser.Morph4;
import jaitools.jiffle.parser.Morph5;
import jaitools.jiffle.parser.Morph6;
import jaitools.jiffle.parser.VarClassifier;
import jaitools.jiffle.runtime.JiffleInterpreter;
import jaitools.jiffle.runtime.JiffleRunner;
import jaitools.jiffle.runtime.VarTable;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.BufferedTreeNodeStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * This class is the starting point for compiling and running a Jiffle script.
 * It takes the script and a corresponding Map of image parameters which define
 * the relationship between image variables in the script and image objects.
 * The script is then compiled in a series of error checking and optimizing
 * steps.  If compilation is successful the Jiffle object will now contain
 * an executable form of the script which can be run directly by passing the object
 * to a new instance of {@link JiffleRunner} as in this example:
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
 * script to a {@link JiffleInterpreter} object which runs each submitted script 
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

    private String script;
    private CommonTree primaryAST;
    private CommonTokenStream tokens;
    private CommonTree runtimeAST;
    
    private Map<String, RenderedImage> imageParams;
    private Set<String> vars;
    private Set<String> unassignedVars;
    private Set<String> outputImageVars;
    
    private Metadata metadata;
    private Map<String, ErrorCode> errors;
    
    /**
     * Constructor: takes an input script as a String, together with
     * a Map relating image variable names to image objects, and compiles
     * the script.
     * 
     * @param script input jiffle statement(s)
     * @param params variable names and their corresponding images
     * @throws JiffleCompilationException on error compiling the script
     */
    public Jiffle(String script, Map<String, RenderedImage> params)
            throws JiffleCompilationException {

        init(script, params);
    }

    /**
     * Constructor: reads a script from a text file and compiles it.
     * 
     * @param scriptFile text file containing the Jiffle script
     * @param params variable names and their corresponding images
     *
     * @throws IOException on error reading the script file
     * @throws JiffleCompilationException on error compiling the script
     */
    public Jiffle(File scriptFile, Map<String, RenderedImage> params)
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
     * Helper function for constructors
     * @param script input jiffle statements
     * @param params variable names and their corresponding images
     */
    private void init(String script, Map<String, RenderedImage> params)
            throws JiffleCompilationException {

        this.imageParams = CollectionFactory.map();
        this.imageParams.putAll(params);

        // add extra new line just in case last statement hits EOF
        this.script = new String(script + "\n");
        compile();
    }

    /**
     * Get the image object associated with the given image variable
     * @param varName image variable name
     * @return image object
     */
    public RenderedImage getImage(String varName) {
        return imageParams.get(varName);
    }

    /**
     * Query if the input script has been compiled successfully
     * 
     * @todo is this necessary since at the moment compilation
     * errors result in exceptions ?
     */
    public boolean isCompiled() {
        return (runtimeAST != null);
    }

    /**
     * Set the image parameters to the given Map. Previous
     * parameters are replaced. This can be used when re-running
     * a compiled script to work with new input and/or output images.
     * 
     * @param params variable names and their corresponding images
     */
    public void setImageParams(Map<String, RenderedImage> params) {
        imageParams = CollectionFactory.map();
        imageParams.putAll(params);
    }

    /**
     * Returns the metadata for variables used in the Jiffle program
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the executable form of the Jiffle program as a stram of Abstract
     * Syntax Tree nodes
     */
    public BufferedTreeNodeStream getRuntimeAST() {
        BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(runtimeAST);
        nodes.setTokenStream(tokens);
        return nodes;
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
        primaryAST = null;

        if (script != null && script.length() > 0) {
            buildAST();
            
            if (!checkFunctionCalls()) {
                throw new JiffleCompilationException(getErrorString());
            }
            
            if (!classifyVars()) {
                throw new JiffleCompilationException(getErrorString());
            }
            
            runtimeAST = optimizeTree();
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
            classifier.setImageVars(imageParams.keySet());
            classifier.start();
            
            if (classifier.hasError()) {
                errors = classifier.getErrors();
                return false;
            }

        } catch (RecognitionException ex) {
            // anything at this stage is probably programmer error
            throw new RuntimeException(ex);
        }

        /*
         * Create a metadata object for later stages of compilation and execution
         */
        metadata = new Metadata(imageParams);
        metadata.setVarData(classifier);
        
        return true;
    }

    /**
     * Attempt to optimize the AST by identifying variables and expressions
     * that depend only on in-built and user-defined constants and
     * pre-calculating them.
     * 
     * @return the optimized AST
     */
    private CommonTree optimizeTree() {
        CommonTree tree;
        
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            
            Morph1 m1 = new Morph1(nodes);
            m1.setMetadata(metadata);
            Morph1.start_return m1Ret = m1.start();
            tree = (CommonTree) m1Ret.getTree();

            nodes = new CommonTreeNodeStream(tree);
            Morph4 m4 = new Morph4(nodes);
            Morph4.start_return m4Ret = m4.start();
            tree = (CommonTree) m4Ret.getTree();
            
            Morph5 m5;
            VarTable varTable = new VarTable();
            do {
                nodes = new CommonTreeNodeStream(tree);
                m5 = new Morph5(nodes);
                m5.setVarTable(varTable);
                Morph5.start_return m5Ret = m5.start();
                tree = (CommonTree) m5Ret.getTree();
            } while (m5.getCount() > 0);

            nodes = new CommonTreeNodeStream(tree);
            Morph6 m6 = new Morph6(nodes);
            m6.setVarTable(varTable);
            Morph6.start_return m6Ret = m6.start();
            tree = (CommonTree) m6Ret.getTree();
            
            nodes = new CommonTreeNodeStream(tree);
            MakeRuntime rt = new MakeRuntime(nodes);
            MakeRuntime.start_return rtRet = rt.start();
            tree = (CommonTree) rtRet.getTree();

        } catch (RecognitionException ex) {
            throw new RuntimeException(ex);
        }        
        
        return tree;
    }

}
