/*
 * Copyright 2011 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.vectorize;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RenderedOp;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.Polygon;

import jaitools.numeric.NumberOperations;

import static org.junit.Assert.*;

/**
 * Based class for unit tests.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class TestBase {
    
    protected static final GeometryFactory gf = new GeometryFactory();
    protected static final WKTReader reader = new WKTReader(gf);
    
    /**
     * Helper function. Builds parameter block and runs the operation.
     * 
     * @param sourceImg source image
     * @param args optional {@code Map} of arguments
     * 
     * @return the destination image
     */
    protected RenderedOp doOp(RenderedImage sourceImg, Map<String, Object> args) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", sourceImg);
        
        if (args != null) {
            ParameterListDescriptor pld = pb.getParameterListDescriptor();
            for (String paramName : pld.getParamNames()) {
                Object obj = args.get(paramName);
                if (obj != null) {
                    pb.setParameter(paramName, obj);
                }
            }
        }

        return JAI.create("Vectorize", pb);
    }

    /**
     * Helper function. Gets the vectors property from a destination
     * image and checks the following:
     * <ol type="1">
     * <li> The property is a {@code Collection} </li>
     * <li> Its size equals {@code expectedN} </li>
     * <li> It contains {@code Polygons}
     * </ol>
     * If these checks are satisfied, the {@code Polygons} are returned.
     * 
     * @param dest property from the destination image
     * @param expectedN expected number of polygons
     * 
     * @return the polygons
     */
    protected List<Polygon> getPolygons(RenderedOp dest, int expectedN) {
        Object prop = dest.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
        assertTrue(prop != null && prop instanceof Collection);

        Collection coll = (Collection) prop;
        assertEquals(expectedN, coll.size());

        List<Polygon> polys = new ArrayList<Polygon>();
        if (expectedN > 0) {
            Object obj = coll.iterator().next();
            assertTrue(obj instanceof Polygon);

            polys.addAll(coll);
        }


        return polys;
    }

    protected void assertPolygons(ExpectedPoly expected, List<Polygon> observed) throws Exception {
        assertPolygons(new ExpectedPoly[]{expected}, observed);
    }

    /**
     * Assert equality of expected and observed polygons.
     * 
     * @param expectedWKT WKT string for expected polygon
     * @param observed observed polygon
     */
    protected void assertPolygons(ExpectedPoly[] expected, List<Polygon> observed) throws Exception {
        PolyList pl = new PolyList(observed);

        for (ExpectedPoly ep : expected) {
            Polygon poly = (Polygon) reader.read(ep.wkt);
            int index = pl.indexOf(poly);
            assertTrue("Polygon not found", index >= 0);
            
            Polygon matchPoly = pl.get(index);
            Number value = (Number) matchPoly.getUserData();
            assertEquals("User data does not match",
                    0, NumberOperations.compare(value, ep.value));
        }
    }

    /**
     * Class to hold WKT String and a numeric value for
     * an expected polygon.
     */
    protected static final class ExpectedPoly {

        String wkt;
        Number value;

        ExpectedPoly(String wkt, Number value) {
            this.wkt = wkt;
            this.value = value;
        }
    }

    /**
     * A {@code List} class that normalizes {@code Polygons} added to it
     * and overrides the {@code indexOf} method to use {@code Polygon.equalsExact}.
     */
    protected static final class PolyList extends ArrayList<Polygon> {

        private static final double TOL = 0.5d;

        PolyList(List<Polygon> polys) {
            for (Polygon p : polys) {
                add(p);
            }
        }

        @Override
        public boolean add(Polygon p) {
            p.normalize();
            return super.add(p);
        }

        @Override
        public int indexOf(Object o) {
            Polygon op = (Polygon) o;
            op.normalize();
            for (int i = 0; i < size(); i++) {
                if (get(i).equalsExact(op, TOL)) {
                    return i;
                }
            }
            return -1;
        }
    }
    
}
