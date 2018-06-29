/* 
 *  Copyright (c) 2010-2011, Michael Bedward. All rights reserved. 
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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Tests for the JTS utility class.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */

public class UtilsTest {

    private final static String LINESTRING = "LINESTRING(0 0, 1 0, 2 0, 5 0, 5 1, 5 2, 5 5, 6 5, 6 8, 6 9, 7 0)";
    private final static String EXPECTED_LINESTRING = "LINESTRING(0 0, 5 0, 5 5, 6 5, 6 9, 7 0)";
    private final static String POLYGON = "POLYGON((1 1, 2 1, 3 1, 4 1, 5 1, 5 2, 5 3, 5 4, 5 5, 4 5, 3 5, 2 5, 1 5, 1 4, 1 3, 1 2, 1 1),(2 2,2 3,3 3,3 2,2 2))";
    private final static String EXPECTED_POLYGON = "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))";
    private final static String RING = "LINEARRING(1 1, 2 1, 3 1, 4 1, 5 1, 5 2, 5 3, 5 4, 5 5, 4 5, 3 5, 2 5, 1 5, 1 4, 1 3, 1 2, 1 1)";
    private final static String EXPECTED_RING = "LINEARRING(1 1,5 1,5 5,1 5,1 1)";
    private final static String DEGENERATE_LS = "LINESTRING(1 1, 2 2, 1 1)";

    @Test
    public void getGeometryFactory() {
        GeometryFactory gf = Utils.getGeometryFactory();
        assertNotNull(gf);
    }

    @Test
    public void setPrecision() {
        GeometryFactory gf1 = Utils.getGeometryFactory();

        Utils.setPrecision(100.0);
        GeometryFactory gf2 = Utils.getGeometryFactory();

        PrecisionModel pm = new PrecisionModel(100.0);
        assertFalse(pm.equals(gf1.getPrecisionModel()));
        assertTrue(pm.equals(gf2.getPrecisionModel()));
    }

    @Test
    public void testLineString() throws ParseException {
        final LineString ls = (LineString) new WKTReader().read(LINESTRING);

        // simplify
        final LineString simplifiedLS = Utils.removeCollinearVertices(ls);

        // check expected
        assertTrue(((LineString) new WKTReader().read(EXPECTED_LINESTRING)).equalsExact(simplifiedLS));
    }

    @Test
    public void testLinePolygon() throws ParseException {
        final Polygon poly = (Polygon) new WKTReader().read(POLYGON);

        // simplify
        final Polygon simplifiedPoly = Utils.removeCollinearVertices(poly);

        // check expected
        assertTrue(((Polygon) new WKTReader().read(EXPECTED_POLYGON)).equalsExact(simplifiedPoly));
    }

    @Test
    public void testLineRing() throws ParseException {
        final LinearRing ring = (LinearRing) new WKTReader().read(RING);

        // simplify
        final LinearRing simplifiedring = (LinearRing) Utils.removeCollinearVertices(ring);

        // check expected
        assertTrue(((LinearRing) new WKTReader().read(EXPECTED_RING)).equalsExact(simplifiedring));
    }

    @Test
    public void testDegenerate() throws ParseException {
        final LineString original = (LineString) new WKTReader().read(DEGENERATE_LS);

        // simplify
        final LineString simplified = (LineString) Utils.removeCollinearVertices(original);

        // check expected
        assertTrue(simplified.equalsExact(original));
    }

    @Test
    public void testGeometry() throws ParseException {
        final Geometry ring = new WKTReader().read(RING);

        // simplify
        final LinearRing simplifiedring = (LinearRing) Utils.removeCollinearVertices(ring);

        // check expected
        assertTrue(((LinearRing) new WKTReader().read(EXPECTED_RING)).equalsExact(simplifiedring));


        final Geometry poly = new WKTReader().read(POLYGON);

        // simplify
        final Polygon simplifiedPoly = (Polygon) Utils.removeCollinearVertices(poly);

        // check expected
        assertTrue(((Polygon) new WKTReader().read(EXPECTED_POLYGON)).equalsExact(simplifiedPoly));


        final Geometry ls = new WKTReader().read(LINESTRING);

        // simplify
        final LineString simplifiedLS = (LineString) Utils.removeCollinearVertices(ls);

        // check expected
        assertTrue(((LineString) new WKTReader().read(EXPECTED_LINESTRING)).equalsExact(simplifiedLS));

    }
}
