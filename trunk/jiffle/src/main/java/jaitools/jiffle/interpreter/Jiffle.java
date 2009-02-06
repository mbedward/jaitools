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
import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.Map.Entry;
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
    public Jiffle(String script) {
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
     * @return true if the mapping was valid; false otherwise (e.g. if
     * the variable name does not appear in the jiffle script)
     */
    public boolean setImageMapping(String varName, RenderedImage image) {
        // @todo check varName against a symbol table
        
        imageMappings.put(varName, image);
        return true;
    }
    
    /**
     * Associate a group of variable names with rendered
     * images
     * @param map variable names and their corresponding images
     * @return true if all mappings were successful; false otherwise (e.g.
     * if one of the variable names does not appear in the jiffle script)
     */
    public boolean setImageMapping(Map<String, RenderedImage> map) {
        for (Entry<String, RenderedImage> e : map.entrySet()) {
            if (!setImageMapping(e.getKey(), e.getValue())) {
                return false;
            }
        }
        
        return true;
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
    private void compile() {
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
                reportError(re);
            } 
        }
    }

    /**
     * Report an ANTLR-generated error
     * @param re
     */
    private void reportError(RecognitionException re) {
        // @todo WRITE ME !
    }
}
