/*
 * Copyright 2009-2011 Michael Bedward
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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * Lexes and parses Jiffle scripts for unit tests.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public abstract class ParserTestBase {
    
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
