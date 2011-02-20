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

import jaitools.CollectionFactory;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the options block parser.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionsBlockReaderTest {
    private String script;
    private MessageTable msgTable;
    private Map<String, String> options;
    private Map<String, String> expectedOptions;
    
    @Before
    public void setup() {
        expectedOptions = CollectionFactory.map();
    }
    

    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   simple block");
        script = "options { outside = 0; } dest = 42;" ;

        parseOptions(script);
        assertMessages();
        expectedOptions.put("outside", "0");
        assertOptions();
    }
    
    @Test
    public void blockWithNewLines() throws Exception {
        System.out.println("   block with newlines");
        script =
                  "options { \n"
                + "  outside = 0; \n"
                + "} \n"
                + "dest = 42;" ;

        parseOptions(script);
        assertMessages();
        expectedOptions.put("outside", "0");
        assertOptions();
    }
    
    @Test
    public void emptyBlock() throws Exception {
        System.out.println("   empty block");
        script = "options { } dest = 42;" ;

        parseOptions(script);
        assertMessages();
        assertOptions();
    }
    
    @Test
    public void outsideNull() throws Exception {
        System.out.println("   outside option with null");
        script = "options { outside = null; } dest = 42;" ;
        
        parseOptions(script);
        assertMessages();
        expectedOptions.put("outside", "null");
        assertOptions();
    }

    @Test
    public void invalidOptionName() throws Exception {
        System.out.println("   invalid option");
        script = "options { foo = 0; } dest = 42;" ;
        
        parseOptions(script);
        assertMessages(Message.INVALID_OPTION);
    }
    
    @Test
    public void invalidOutsideOptionValue() throws Exception {
        System.out.println("   invalid outside option value");
        script = "options { outside = foo; } dest = 42;" ;
        
        parseOptions(script);
        assertMessages(Message.INVALID_OPTION_VALUE);
    }
    
    
    private void assertOptions() {
        assertEquals(expectedOptions.size(), options.size());
        for (String key : expectedOptions.keySet()) {
            assertTrue(expectedOptions.get(key).equals(options.get(key)));
        }
    }
    
    private void assertMessages(Message ...expectedMessages) {
        Map<String, List<Message>> messages = msgTable.getMessages();
        assertEquals(expectedMessages.length, messages.size());
    }
    
    private void parseOptions(String script) throws Exception {
        ANTLRStringStream stream = new ANTLRStringStream(script);
        JiffleLexer lexer = new JiffleLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        JiffleParser parser = new JiffleParser(tokens);
        CommonTree tree = (CommonTree) parser.prog().getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);

        msgTable = new MessageTable();
        OptionsBlockReader reader = new OptionsBlockReader(nodes, msgTable);
        reader.downup(tree);

        options = reader.getOptions();
    }

}
