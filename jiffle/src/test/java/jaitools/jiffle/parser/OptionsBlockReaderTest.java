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

import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the options block parser.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionsBlockReaderTest {

    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   simple block");
        String script =
                  "options { outside = 0; } \n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertTrue(options.containsKey("outside"));
    }
    
    @Test
    public void blockWithNewLines() throws Exception {
        System.out.println("   block with newlines");
        String script =
                  "options { \n"
                + "  outside = 0; \n"
                + "} \n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertTrue(options.containsKey("outside"));
    }
    
    @Test
    public void emptyBlock() throws Exception {
        System.out.println("   empty block");
        String script =
                  "options { }\n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(0, options.size());
    }
    
    private Map<String, String> parseOptions(String script) throws Exception {
        ANTLRStringStream stream = new ANTLRStringStream(script);
        JiffleLexer lexer = new JiffleLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        JiffleParser parser = new JiffleParser(tokens);
        CommonTree tree = (CommonTree) parser.prog().getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);

        MessageTable msgTable = new MessageTable();
        OptionsBlockReader reader = new OptionsBlockReader(nodes, msgTable);
        reader.downup(tree);

        return reader.getOptions();
    }
}
