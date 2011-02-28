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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SymbolScopeStack.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SymbolScopeStackTest {
    
    private SymbolScopeStack stack;
    
    @Before
    public void setup() {
        stack = new SymbolScopeStack();
    }

    @Test
    public void testAddLevel_0args() {
        stack.addLevel();
        assertEquals(1, stack.size());
    }

    @Test
    public void testAddLevel_String() {
        stack.addLevel("foo");
        assertEquals(1, stack.size());
        
        SymbolScope level = stack.dropLevel();
        assertTrue("foo".equals(level.getName()));
    }

    @Test
    public void testAddSymbol() {
        stack.addLevel();
        stack.addSymbol("foo", SymbolType.SCALAR, ScopeType.PIXEL);
        
        assertTrue(stack.isDefined("foo"));
    }

    @Test(expected=IllegalStateException.class)
    public void testAddSymbolToEmptyStack() {
        stack.addSymbol("foo", SymbolType.SCALAR, ScopeType.PIXEL);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(stack.isEmpty());
        
        stack.addLevel();
        assertFalse(stack.isEmpty());
        
        stack.dropLevel();
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testIsDefinedAtTopLevel() {
        stack.addLevel();
        stack.addSymbol("foo", SymbolType.SCALAR, ScopeType.PIXEL);
        
        assertTrue(stack.isDefined("foo"));
        assertFalse(stack.isDefined("bar"));
    }

    @Test
    public void testIsDefinedAtEnclosingLevel() {
        stack.addLevel();
        stack.addLevel();
        stack.addSymbol("foo", SymbolType.SCALAR, ScopeType.PIXEL);
        stack.addLevel();
        stack.addLevel();
        
        
        assertTrue(stack.isDefined("foo"));
        assertFalse(stack.isDefined("bar"));
    }

}