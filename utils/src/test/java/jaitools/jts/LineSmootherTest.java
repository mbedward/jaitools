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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

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
}
