/*
 * Copyright 2009 Michael Bedward
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

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import javax.media.jai.ROI;
import java.util.Map;
import java.awt.image.RenderedImage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import jaitools.imageutils.ImageUtils;

import java.util.Collection;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.registry.RenderedRegistryMode;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorizeTest {
    
    public static final GeometryFactory gf = new GeometryFactory();
    
    @BeforeClass
    public static void setup() {
        ensureRegistered();
    }

    /**
     * Test vectorizing a small image where all pixels have 
     * equal value. We leave the outsideValues arg at its null default
     * so all image pixels are treated as inside.
     * 
     * Expected result is a single polygon with coordinates equal to those
     * of the source image bounding rectangle.
     */
    @Test
    public void imageBoundary() throws Exception {
        final int IMAGE_WIDTH = 10;
        
        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));

        RenderedOp dest = doOp(src, null);
        List<Polygon> polys = getPolygons(dest, 1);
        
        Coordinate[] expected = {
            new Coordinate(0, 0),
            new Coordinate(0, 10),
            new Coordinate(10, 10),
            new Coordinate(10, 0),
            new Coordinate(0, 0)
        };
        assertPolygons(expected, polys.get(0));
    }
    
    /**
     * Vectorize a test image that has a single block of inside pixels
     * surrounded by a margin of outside pixels.
     * 
     * Expected result is a single polygon surrounding the inside pixels.
     */
    @Test
    public void imageWithOutsideMargin() throws Exception {
        final int IMAGE_WIDTH = 10;
        final int MARGIN_WIDTH = 2;
        
        final int OUTSIDE = 0;
        final int INSIDE = 1;
        
        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));

        for (int y = MARGIN_WIDTH; y < IMAGE_WIDTH - MARGIN_WIDTH; y++) {
            for (int x = MARGIN_WIDTH; x < IMAGE_WIDTH - MARGIN_WIDTH; x++) {
                src.setSample(x, y, 0, 1);
            }
        }
        
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("outsideValues", Collections.singleton(OUTSIDE));
        
        RenderedOp dest = doOp(src, args);
        List<Polygon> polys = getPolygons(dest, 1);

        Coordinate[] expected = {
            new Coordinate(2, 2),
            new Coordinate(2, 8),
            new Coordinate(8, 8),
            new Coordinate(8, 2),
            new Coordinate(2, 2)
        };
        assertPolygons(expected, polys.get(0));
    }

    /**
     * Helper function. Builds parameter block and runs the operation.
     * 
     * @param sourceImg source image
     * @param args optional {@code Map} of arguments
     * 
     * @return the destination image
     */
    private RenderedOp doOp(RenderedImage sourceImg, Map<String, Object> args) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", sourceImg);
        
        if (args != null) {
            ROI roi = (ROI) args.get("roi");
            if (roi != null) pb.setParameter("roi", roi);
            
            Integer band = (Integer) args.get("band");
            if (band != null) pb.setParameter("band", band);
            
            Collection<? extends Number> outsideValues = (Collection<? extends Number>) args.get("outsideValues");
            if (outsideValues != null) pb.setParameter("outsideValues", outsideValues);
            
            Boolean insideEdges = (Boolean) args.get("insideEdges");
            if (insideEdges != null) pb.setParameter("insideEdges", insideEdges);
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
    private List<Polygon> getPolygons(RenderedOp dest, int expectedN) {
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
    
    /**
     * Assert equality of expected and observed polygons.
     * 
     * @param expected coordinates defining the expected polygon
     * @param observed observed polygon
     */
    private void assertPolygons(Coordinate[] expected, Polygon observed) {
        assertPolygons(gf.createPolygon(gf.createLinearRing(expected), null), observed);
    }
    
    /**
     * Assert equality of expected and observed polygons.
     * 
     * @param expected expected polygon
     * @param observed observed polygon
     */
    private void assertPolygons(Polygon expected, Polygon observed) {
        expected.normalize();
        observed.normalize();
        assertTrue(expected.equalsExact(observed, 0.5d));
    }
    
    /**
     * Register the operator with JAI if it is not already registered
     */
    private static void ensureRegistered() {
        OperationRegistry reg = JAI.getDefaultInstance().getOperationRegistry();
        String[] names = reg.getDescriptorNames(RenderedRegistryMode.MODE_NAME);
        VectorizeDescriptor desc = new VectorizeDescriptor();
        String descName = desc.getName();
        for (String name : names) {
            if (descName.equalsIgnoreCase(name)) {
                return;
            }
        }

        VectorizeSpi spi = new VectorizeSpi();
        spi.updateRegistry(reg);
    }
    
}
