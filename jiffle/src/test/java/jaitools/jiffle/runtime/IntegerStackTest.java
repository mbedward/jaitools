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

package jaitools.jiffle.runtime;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the simple integer stack used by {@link AbstractJiffleRuntime}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class IntegerStackTest {
    
    private IntegerStack stk;
    
    @Before
    public void setup() {
        stk = new IntegerStack();
    }

    @Test
    public void push() {
        stk.push(1);
        stk.push(2);
        stk.push(Integer.MAX_VALUE);
        assertEquals(3, stk.size());
    }
    
    @Test
    public void pushNull() {
        stk.push(null);
        stk.push(1);
        stk.push(null);
        stk.push(2);
        assertEquals(4, stk.size());
    }

    @Test
    public void pop() {
        for (int i = 0; i < 100; i++) {
            stk.push(i);
        }
        
        for (int i = 99; i >= 0; i--) {
            assertEquals(i, stk.pop().intValue());
        }
    }

    @Test
    public void peek() {
        stk.push(1);
        assertEquals(1, stk.peek().intValue());
        
        stk.push(null);
        assertNull(stk.peek());
        
        stk.pop();
        assertEquals(1, stk.peek().intValue());
    }

    @Test
    public void clear() {
        stk.push(1);
        stk.push(2);
        stk.push(3);
        stk.clear();
        assertEquals(0, stk.size());
    }
    
    @Test
    public void grow() {
        for (int i = 0; i < 5 * IntegerStack.CHUNK_SIZE; i++) {
            stk.push(i);
        }
        
        for (int i = 5 * IntegerStack.CHUNK_SIZE - 1; i >= 0; i--) {
            assertEquals(i, stk.pop().intValue());
        }
    }

}