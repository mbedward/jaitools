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

package jaitools.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import org.junit.Before;
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
    private PolygonSmoother smoother;
    
    @Before
    public void setup() {
        smoother = new PolygonSmoother(gf);
    }

    /**
     * Smooth a very simple polygon. Check that the output polygon covers the 
     * input polygon and contains all of the input's vertices.
     */
    @Test
    public void squarePoly() throws Exception {
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
}
