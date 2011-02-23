/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class RuntimeSourceTest {
    private static StringTemplateGroup templates;
    
    private static final String CLASS = "JiffleDirectRuntimeImpl";
    private static final String BASECLASS = "AbstractDirectRuntime";
    
    private String script;
    private CommonTree primaryAST;
    private CommonTree finalAST;
    private CommonTokenStream tokens;
    
    private Map<String, Jiffle.ImageRole> imageParams;
    private MessageTable msgTable;
    
    
    @BeforeClass
    public static void setupClass() throws Exception {
        URL url = RuntimeSourceTest.class.getResource("DirectRuntimeSource.stg");
        File file = new File(url.toURI());
        FileReader reader = new FileReader(file);
        templates = new StringTemplateGroup(reader);
        reader.close();
    }
    
    @Before
    public void setup() {
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);
        
        msgTable = new MessageTable();
    }
    

    @Test
    public void spike() throws Exception {
        script = "dest = 42;" ;
        assertTrue( compile() );
        
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(finalAST);
        nodes.setTokenStream(tokens);
        
        
        RuntimeSourceCreator2 creator = new RuntimeSourceCreator2(nodes, CLASS, BASECLASS);
        creator.setTemplateLib(templates);
        StringTemplate st = (StringTemplate) creator.start().getTemplate();
        
        System.out.println(st);
    }
    
    private boolean compile() throws Exception {
        ANTLRStringStream input = new ANTLRStringStream(script);
        JiffleLexer lexer = new JiffleLexer(input);
        tokens = new CommonTokenStream(lexer);

        JiffleParser parser = new JiffleParser(tokens);
        primaryAST = (CommonTree) parser.prog().getTree();

        CommonTree tree = primaryAST;

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        TagVars tag = new TagVars(nodes, imageParams, msgTable);
        tree = (CommonTree) tag.start().getTree();
        if (msgTable.hasErrors()) {
            return false;
        }

        nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);

        CheckAssignments check = new CheckAssignments(nodes, msgTable);
        check.start();
        if (msgTable.hasErrors()) {
            return false;
        }

        nodes = new CommonTreeNodeStream(tree);
        TransformExpressions trexpr = new TransformExpressions(nodes);
        tree = (CommonTree) trexpr.start().getTree();

        finalAST = tree;
        return true;
    }
}
