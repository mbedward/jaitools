/*
 * Copyright 2009-2010 Michael Bedward
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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for LineSmoother.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class LineSmootherTest {
    
    private static final GeometryFactory gf = new GeometryFactory();
    private static final WKTReader reader = new WKTReader(gf);
    private LineSmoother smoother;
    
    @Before
    public void setup() {
        smoother = new LineSmoother(gf);
    }

    /**
     * Smooth a very simple polygon. Check that the output polygon covers the 
     * input polygon and contains all of the input's vertices.
     */
    @Test
    public void squarePoly() throws Exception {
        Polygon p = (Polygon) reader.read("POLYGON((0 0, 0 100, 100 100, 100 0, 0 0))");
        Polygon ps = smoother.smooth(p, 0.0, 10);
        
        assertTrue(ps.covers(p));
        
        Coordinate[] pscoords = ps.getCoordinates();
        for (Coordinate c : p.getCoordinates()) {
            // Hopelessly inefficient but ok for this small test
            boolean found = false;
            for (int j = 0; j < pscoords.length; j++) {
                if (c.equals2D(pscoords[j])) {
                    found = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }

    /**
     * Smooth a very simple polygon but this time with a tight fit. 
     * Check that the output polygon covers the input polygon and contains 
     * all of the input's vertices.
     */
    @Test
    public void squarePolyTight() throws Exception {
        Polygon p = (Polygon) reader.read("POLYGON((0 0, 0 100, 100 100, 100 0, 0 0))");
        Polygon ps = smoother.smooth(p, 0.9, 10);
        
        assertTrue(ps.covers(p));
        
        Coordinate[] pscoords = ps.getCoordinates();
        for (Coordinate c : p.getCoordinates()) {
            // Hopelessly inefficient but ok for this small test
            boolean found = false;
            for (int j = 0; j < pscoords.length; j++) {
                if (c.equals2D(pscoords[j])) {
                    found = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }
}
