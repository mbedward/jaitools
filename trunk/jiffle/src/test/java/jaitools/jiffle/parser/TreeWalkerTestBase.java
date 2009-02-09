/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 *
 * @author Michael Bedward and Murray Ellis
 */
public abstract class TreeWalkerTestBase {
    
    /**
     * Helper function to scan and parse an input script and
     * prepare a tree node stream for a tree walker
     * 
     * @param input input jiffle script
     * @return the AST as a tree node stream with attached tokens
     * @throws java.lang.Exception
     */
    protected CommonTreeNodeStream getAST(String input) throws Exception {

        ANTLRStringStream strm = new ANTLRStringStream(input);
        JiffleLexer lexer = new JiffleLexer(strm);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JiffleParser parser = new JiffleParser(tokens);
        JiffleParser.prog_return r = parser.prog();
        CommonTree tree = (CommonTree) r.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        
        return nodes;
    }
    
}
