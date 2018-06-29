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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for LineSmoother.
 *
 * @author Michael Bedward
 * @since 1.1
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
     * Smooth a simple, horizontal zig-sag line string of five coordinates.
     * Test that all input vertices are present in the smoothed line and that
     * the smoothed line lies within the bounding rectangle of the input line.
     * @throws Exception 
     */
    @Test
    public void zigZag() throws Exception {
        LineString line = (LineString) reader.read("LINESTRING(0 0, 10 10, 20 0, 30 -10, 40 0)");
        LineString sline = smoother.smooth(line, 0.0);
        
        Coordinate[] coords = line.getCoordinates();
        int i = 0;
        for (Coordinate scoord : sline.getCoordinates()) {
            if (scoord.equals2D(coords[i])) {
                i++ ;
            }
        }
        assertEquals(coords.length, i);
        assertTrue(line.getEnvelopeInternal().contains(sline.getEnvelopeInternal()));
    }
    
    /**
     * Tests that user data attached to the input line string is preserved
     * in the smoothed output.
     * @throws Exception 
     */
    @Test
    public void userDataIsPreserved() throws Exception {
        LineString input = (LineString) reader.read("LINESTRING(0 0, 10 10, 20 0)");
        Object userData = "Some data";
        input.setUserData(userData);
        
        LineString output = smoother.smooth(input, 0.0);
        
        assertNotNull(output.getUserData());
        assertEquals(userData, output.getUserData());
    }
}
