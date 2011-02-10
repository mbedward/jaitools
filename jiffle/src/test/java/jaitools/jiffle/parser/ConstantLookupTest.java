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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for ConstantLookup.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ConstantLookupTest {
    
    private final double TOL = 1.0e-8;
    
    public void getPI() {
        assertEquals(Math.PI, ConstantLookup.getValue("M_PI"), TOL);
    }
    
    public void getPIOn2() {
        assertEquals(Math.PI / 2.0, ConstantLookup.getValue("M_PI_2"), TOL);
    }

    public void getPIOn4() {
        assertEquals(Math.PI / 4.0, ConstantLookup.getValue("M_PI_4"), TOL);
    }
    
    public void getSqrt2() {
        assertEquals(Math.sqrt(2.0), ConstantLookup.getValue("M_SQRT2"), TOL);
    }
    
    public void getE() {
        assertEquals(Math.E, ConstantLookup.getValue("M_E"), TOL);
    }
    
    public void getNanPrefix() {
        assertTrue(Double.isNaN( ConstantLookup.getValue("M_NaN")));
        assertTrue(Double.isNaN( ConstantLookup.getValue("M_NAN")));
    }
    
    public void getNanNoPrefix() {
        assertTrue(Double.isNaN( ConstantLookup.getValue("NaN")));
        assertTrue(Double.isNaN( ConstantLookup.getValue("NAN")));
    }

    @Test(expected=IllegalArgumentException.class)
    public void unknownConstant() {
        ConstantLookup.getValue("NotAConstant");
    }
    
    
}
