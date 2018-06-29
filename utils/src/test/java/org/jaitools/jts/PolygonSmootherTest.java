/* 
 *  Copyright (c) 2009-2010, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.jts;

import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for PolygonSmoother.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class PolygonSmootherTest {
    
    private static final GeometryFactory gf = new GeometryFactory();
    private static final WKTReader reader = new WKTReader(gf);
    
    /**
     * Smooth a very simple polygon. Check that the output polygon covers the 
     * input polygon and contains all of the input's vertices.
     */
    @Test
    public void squarePoly() throws Exception {
        PolygonSmoother smoother = new PolygonSmoother(gf);
        Polygon p = (Polygon) reader.read("POLYGON((0 0, 0 100, 100 100, 100 0, 0 0))");
        Polygon ps = smoother.smooth(p, 0.0);
        
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
        PolygonSmoother smoother = new PolygonSmoother(gf);
        Polygon p = (Polygon) reader.read("POLYGON((0 0, 0 100, 100 100, 100 0, 0 0))");
        Polygon ps = smoother.smooth(p, 0.9);
        
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

    @Test
    public void lastSegmentShorterThanControlDistance() throws Exception {
        final double MIN_LEN = 1.0;
        
        // Create triangle where the base is shorter than the smoother control
        // min length
        final double base = MIN_LEN / 2.0;
        final double height = 10.0 * MIN_LEN;
        String wkt = String.format(Locale.US, "POLYGON((0 0, %f %f, %f 0, 0 0))",
                base / 2.0, height, base);
        Polygon p = (Polygon) reader.read(wkt);

        // Create a smoother using MIN_LEN as its minimum inter-vertex distance
        PolygonSmoother smoother = new PolygonSmoother(gf);
        smoother.setControl(new SmootherControl() {
            
            public double getMinLength() {
                return MIN_LEN;
            }

            public int getNumVertices(double length) {
                return 10;
            }
        });
        
        Polygon ps = null;
        try {
            ps = smoother.smooth(p, 0);
        } catch (Exception ex) {
            // If an exception was thrown the smoother did not return
            // a closed LinearRing
            fail("Smoother did not return a valid polygon");
        }

        // double check
        assertTrue(!ps.isEmpty() && ps.isValid());
    }
    
    @Test
    public void userDataIsPreservedFromInput() throws Exception {
        PolygonSmoother smoother = new PolygonSmoother(gf);
        Polygon input = (Polygon) reader.read("POLYGON((0 0, 0 100, 100 100, 100 0, 0 0))");
        
        Object userData = "Some data";
        input.setUserData(userData);
        
        Polygon output = smoother.smooth(input, 0.0);
        assertNotNull(output.getUserData());
        assertEquals(userData, output.getUserData());
    }
}
