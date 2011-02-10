/*
 * Copyright 2010 Michael Bedward
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

package jaitools.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CoordinateSequence2D.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class CoordinateSequence2DTest {
    
    private static final double TOL = 0.0001;
    
    @Test
    public void emptySequenceInt() {
        System.out.println("empty sequence - int ctor");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(0);
        assertEquals(0, cs.size());
    }
    
    @Test
    public void emptySequenceXY() {
        System.out.println("empty sequence - xy pairs ctor");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(null);
        assertEquals(0, cs.size());
    }
    
    @Test
    public void testGetDimension() {
        System.out.println("getDimension");

        CoordinateSequence2D cs = new CoordinateSequence2D(1);
        assertEquals(2, cs.getDimension());
    }

    @Test
    public void testGetCoordinate() {
        System.out.println("getCoordinate");

        CoordinateSequence2D cs = new CoordinateSequence2D(1.1, 1.2, 2.1, 2.2);
        
        Coordinate c0 = cs.getCoordinate(0);
        assertEquals(1.1, c0.x, TOL);
        assertEquals(1.2, c0.y, TOL);

        Coordinate c1 = cs.getCoordinate(1);
        assertEquals(2.1, c1.x, TOL);
        assertEquals(2.2, c1.y, TOL);
    }

    @Test
    public void testGetCoordinateCopy() {
        System.out.println("getCoordinateCopy");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(1.1, 1.2, 2.1, 2.2);
        Coordinate c0 = cs.getCoordinateCopy(0);
        assertEquals(1.1, c0.x, TOL);
        assertEquals(1.2, c0.y, TOL);

        Coordinate c1 = cs.getCoordinateCopy(1);
        assertEquals(2.1, c1.x, TOL);
        assertEquals(2.2, c1.y, TOL);
    }

    @Test
    public void testGetCoordinate_int_Coordinate() {
        System.out.println("getCoordinate (int, Coordinate)");

        CoordinateSequence2D cs = new CoordinateSequence2D(1.1, 1.2, 2.1, 2.2);
        Coordinate c = new Coordinate();
        cs.getCoordinate(0, c);
        assertEquals(1.1, c.x, TOL);
        assertEquals(1.2, c.y, TOL);

        cs.getCoordinate(1, c);
        assertEquals(2.1, c.x, TOL);
        assertEquals(2.2, c.y, TOL);
    }

    @Test
    public void testGetX() {
        System.out.println("getX");

        CoordinateSequence2D cs = new CoordinateSequence2D(1.2, 3.4);
        assertEquals(1.2, cs.getX(0), TOL);
    }

    @Test
    public void testGetY() {
        System.out.println("getY");

        CoordinateSequence2D cs = new CoordinateSequence2D(1.2, 3.4);
        assertEquals(3.4, cs.getY(0), TOL);
    }

    @Test
    public void testGetOrdinate() {
        System.out.println("getOrdinate");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(1.2, 3.4);
        assertEquals(1.2, cs.getOrdinate(0, 0), TOL);
        assertEquals(3.4, cs.getOrdinate(0, 1), TOL);
    }

    @Test
    public void testSize() {
        System.out.println("size");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(42);
        assertEquals(42, cs.size());
        
        cs = new CoordinateSequence2D(0, 1, 2, 3, 4, 5);
        assertEquals(3, cs.size());
    }

    @Test
    public void testSetOrdinate() {
        System.out.println("setOrdinate");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(2);
        cs.setOrdinate(0, 0, 1.1);
        cs.setOrdinate(0, 1, 1.2);
        cs.setOrdinate(1, 0, 2.1);
        cs.setOrdinate(1, 1, 2.2);
        
        assertEquals(1.1, cs.getX(0), TOL);
        assertEquals(1.2, cs.getY(0), TOL);
        assertEquals(2.1, cs.getX(1), TOL);
        assertEquals(2.2, cs.getY(1), TOL);
    }

    @Test
    public void testSetX() {
        System.out.println("setX");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(2);
        cs.setX(1, 42);
        
        assertEquals(42, cs.getX(1), TOL);
    }

    @Test
    public void testSetY() {
        System.out.println("setY");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(2);
        cs.setY(1, 42);
        
        assertEquals(42, cs.getY(1), TOL);
    }

    @Test
    public void testSetXY() {
        System.out.println("setXY");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(2);
        cs.setXY(1, 42, -1);
        
        assertEquals(42, cs.getX(1), TOL);
        assertEquals(-1, cs.getY(1), TOL);
    }

    @Test
    public void testToCoordinateArray() {
        System.out.println("toCoordinateArray");

        CoordinateSequence2D cs = new CoordinateSequence2D(1.1, 1.2, 2.1, 2.2, 3.1, 3.2);
        Coordinate[] coords = cs.toCoordinateArray();
        
        assertEquals(3, coords.length);
        for (int i = 0; i < coords.length; i++) {
            double x = i + 1.1;
            double y = i + 1.2;
            assertEquals(x, coords[i].x, TOL);
            assertEquals(y, coords[i].y, TOL);
        }
    }

    @Test
    public void testExpandEnvelope() {
        System.out.println("expandEnvelope");
        
        Envelope env = new Envelope();
        CoordinateSequence2D cs = new CoordinateSequence2D(-5.0, 10.0, 5.0, -10.0);
        env = cs.expandEnvelope(env);
        
        assertEquals(-5.0, env.getMinX(), TOL);
        assertEquals(-10.0, env.getMinY(), TOL);
        assertEquals(5.0, env.getMaxX(), TOL);
        assertEquals(10.0, env.getMaxY(), TOL);
    }

    @Test
    public void testClone() {
        System.out.println("clone");
        
        CoordinateSequence2D cs = new CoordinateSequence2D(1,2,3,4,5,6);
        CoordinateSequence2D copy = (CoordinateSequence2D) cs.clone();
        
        assertTrue(cs != copy);
        assertEquals(cs.size(), copy.size());
        
        for (int i = 0; i < cs.size(); i++) {
            assertEquals(cs.getX(i), copy.getX(i), TOL);
            assertEquals(cs.getY(i), copy.getY(i), TOL);
        }
    }

}
