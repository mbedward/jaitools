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
package jaitools.jiffle.interpreter;

import jaitools.jiffle.collection.CollectionFactory;
import jaitools.jiffle.parser.ImageVarValidator;
import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import jaitools.jiffle.parser.VarClassifier;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 *
 * @author Michael Bedward
 */
public class Jiffle {

    private String script;
    private CommonTree tree;
    private CommonTokenStream tokens;
    
    private Map<String, RenderedImage> imageMappings;

    /**
     * Constructor
     * @param script input jiffle statement(s)
     */
    public Jiffle(String script) throws JiffleCompilationException {
        this.imageMappings = CollectionFactory.newTreeMap();
        this.script = new String(script);
        compile();
    }

    public RenderedImage getImage(String varName) {
        return imageMappings.get(varName);
    }

    /**
     * Query if the input script has been compiled successfully
     */
    public boolean isCompiled() {
        return (tree != null);
    }

    /**
     * Associate a variable name with a rendered image
     * @param varName
     * @param image
     */
    public void setImageMapping(String varName, RenderedImage image) {
        imageMappings.put(varName, image);
    }
    
    /**
     * Associate a group of variable names with rendered
     * images
     * @param map variable names and their corresponding images
     */
    public void setImageMapping(Map<String, RenderedImage> map) {
        for (Entry<String, RenderedImage> e : map.entrySet()) {
            setImageMapping(e.getKey(), e.getValue());
        }
    }

    /**
     * Package private method called by JiffleInterpreter to provide
     * the nodes of the AST.
     */
    CommonTreeNodeStream getTree() {
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        return nodes;
    }

    /**
     * Attempt to compile the script into an AST
     */
    private void compile() throws JiffleCompilationException {
        tree = null;

        if (script != null && script.length() > 0) {
            try {
                ANTLRStringStream input = new ANTLRStringStream(script);
                JiffleLexer lexer = new JiffleLexer(input);
                tokens = new CommonTokenStream(lexer);

                JiffleParser parser = new JiffleParser(tokens);

                JiffleParser.prog_return r = parser.prog();
                tree = (CommonTree) r.getTree();

            } catch (RecognitionException re) {
                reportCompilationError(re);
            }

            VarClassifier classifier = null;
            try {
                CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
                nodes.setTokenStream(tokens);
                classifier = new VarClassifier(nodes);
                classifier.start();
                
            } catch (RecognitionException re) {
                // anything at this stage is a programmer error
                throw new RuntimeException("VarClassifier failed to process AST");
            }

            
            Set<String> vars = classifier.getUserVars();
            Set<String> posVars = classifier.getPositionalVars();
            Set<String> unassignedVars = classifier.getUnassignedVars();
            
            /* 
             * Check image var mappings - they should correspond with 
             * var names
             */
            for (String name : imageMappings.keySet()) {
                if (!vars.contains(name)) {
                    throw new JiffleCompilationException(
                            "Unknown variable " + name + " used in image mapping");
                }
            }
            
            ImageVarValidator validator;
            try {
                CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
                nodes.setTokenStream(tokens);
                validator = new ImageVarValidator(nodes);
                validator.setImageVars(imageMappings.keySet());
                validator.start();
                
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            
            if (validator.hasImageVarError()) {
                String msg = "Same image var(s) being used for both input and output";
                throw new JiffleCompilationException(msg);
            }

            Set<String> outputImageVars = validator.getOutputImageVars();
        }
    }

    /**
     * Report an ANTLR-generated error
     * @param re
     */
    private void reportCompilationError(RecognitionException re) {
        // @todo WRITE ME !
    }
    
}
