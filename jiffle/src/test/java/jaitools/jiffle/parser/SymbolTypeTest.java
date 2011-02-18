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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SymbolScopeStack.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SymbolTypeTest {

    @Test
    public void isMyType() {
        System.out.println("   isType");
        
        for (SymbolType t : SymbolType.values()) {
            assertTrue(t.isType(t));
        }
    }
    
    @Test
    public void isMyParentsType() {
        System.out.println("   isType of parent");
        
        SymbolType t = SymbolType.LOOP_VAR;
        assertTrue(t.isType(SymbolType.PIXEL_SCOPE));
        assertFalse(t.isType(SymbolType.IMAGE_SCOPE));
    }
    

}