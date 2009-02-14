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
import jaitools.jiffle.parser.Morph1;
import jaitools.jiffle.parser.Morph2;
import jaitools.jiffle.parser.VarClassifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.media.jai.TiledImage;
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
    private CommonTree primaryAST;
    private CommonTokenStream tokens;
    private CommonTree runtimeAST;
    
    private Map<String, TiledImage> imageParams;
    private Set<String> vars;
    private Set<String> unassignedVars;
    private Set<String> outputImageVars;
    
    private Metadata metadata;
    
    /**
     * Constructor
     * @param script input jiffle statement(s)
     */
    public Jiffle(String script, Map<String, TiledImage> imgParams)
            throws JiffleCompilationException {

        this.imageParams = CollectionFactory.newMap();
        this.imageParams.putAll(imgParams);

        this.script = new String(script);
        compile();
    }

    public TiledImage getImage(String varName) {
        return imageParams.get(varName);
    }

    /**
     * Query if the input script has been compiled successfully
     */
    public boolean isCompiled() {
        return (runtimeAST != null);
    }

    /**
     * Associate a variable name with a rendered image
     * @param varName
     * @param image
     */
    public void setImageParam(String varName, TiledImage image) {
        imageParams.put(varName, image);
    }

    /**
     * Associate a group of variable names with rendered
     * images
     * @param map variable names and their corresponding images
     */
    public void setImageParams(Map<String, TiledImage> map) {
        for (Entry<String, TiledImage> e : map.entrySet()) {
            setImageParam(e.getKey(), e.getValue());
        }
    }

    /**
     * Package private method for {@link JiffleRunner} to get
     * the metadata describing variables
     */
    Metadata getMetadata() {
        return metadata;
    }

    /**
     * Package private method for {@link JiffleRunner} to get the
     * runtime AST
     */
    CommonTreeNodeStream getRuntimeAST() {
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(runtimeAST);
        nodes.setTokenStream(tokens);
        return nodes;
    }

    /**
     * Attempt to compile the script into an AST
     */
    private void compile() throws JiffleCompilationException {
        primaryAST = null;

        if (script != null && script.length() > 0) {
            buildAST();
            classifyVars();
            runtimeAST = optimizeTree();
        }
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
            parser.setPrint(true);

            JiffleParser.prog_return r = parser.prog();
            primaryAST = (CommonTree) r.getTree();

        } catch (RecognitionException re) {
            throw new JiffleCompilationException(
                    "error in script at or around line:" +
                    re.line + " col:" + re.charPositionInLine);
        }
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
    private void classifyVars() throws JiffleCompilationException {
        VarClassifier classifier = null;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            classifier = new VarClassifier(nodes);
            classifier.setImageVars(imageParams.keySet());
            classifier.setPrint(true);
            classifier.start();

        } catch (RecognitionException ex) {
            // anything at this stage is probably programmer error
            throw new RuntimeException(ex);
        }

        /*
         * Create a metadata object for later stages of compilation and execution
         */
        metadata = new Metadata(imageParams);
        metadata.setVarData(classifier);
    }

    private CommonTree optimizeTree() {
        CommonTree t;
        
        t = tree_morph1();
        t = tree_morph2(t);
        
        return t;
    }
    
    /**
     * 
     * @return the modified AST
     */
    private CommonTree tree_morph1() {
        
        Morph1 morph;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(primaryAST);
            nodes.setTokenStream(tokens);
            morph = new Morph1(nodes);
            morph.setMetadata(metadata);
            morph.setPrint(true);
            
            Morph1.start_return retVal = morph.start();
            
            CommonTree tree = (CommonTree) retVal.getTree();
            System.out.println(tree.toStringTree());
            return tree;
            
        } catch (RecognitionException ex) {
            throw new RuntimeException(ex);
        }        
    }
    
    /**
     * 
     * @param tree the AST prepared by tree_morph1()
     * @return the optimized AST
     */
    private CommonTree tree_morph2(CommonTree tree) {
        
        Morph2 morph;
        try {
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
            nodes.setTokenStream(tokens);
            morph = new Morph2(nodes);
            morph.setPrint(true);
            Morph2.start_return retVal = morph.start();
            
            CommonTree simpleTree = (CommonTree) retVal.getTree();
            System.out.println(simpleTree.toStringTree());
            return simpleTree;
            
        } catch (RecognitionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
