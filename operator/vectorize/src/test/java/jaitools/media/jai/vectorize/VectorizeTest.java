/*
 * Copyright 2010 Michael Bedward
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

import java.awt.Rectangle;
import jaitools.numeric.NumberOperations;
import java.awt.image.RenderedImage;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import jaitools.imageutils.ImageUtils;

import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROIShape;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.registry.RenderedRegistryMode;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Vectorize operator.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class VectorizeTest {

    private static final GeometryFactory gf = new GeometryFactory();
    private static final WKTReader reader = new WKTReader(gf);
    private Map<String, Object> args;

    @BeforeClass
    public static void setupClass() {
        ensureRegistered();
    }

    @Before
    public void setup() {
        args = new HashMap<String, Object>();
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

        ExpectedPoly expected = new ExpectedPoly("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", 0);
        assertPolygons(expected, polys);
    }

    /**
     * Tests for graceful handling of an image with no inside values.
     */
    @Test
    public void noInsideValues() throws Exception {
        final int IMAGE_WIDTH = 10;

        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));

        Random rr = new Random();
        final int MAX = 5;

        for (int y = 0; y < IMAGE_WIDTH; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                src.setSample(x, y, 0, rr.nextInt(MAX));
            }
        }

        Set<Integer> outsideValues = new HashSet<Integer>();
        for (int i = 0; i < MAX; i++) {
            outsideValues.add(i);
        }

        args.put("outsideValues", outsideValues);
        RenderedOp dest = doOp(src, args);
        getPolygons(dest, 0);
    }

    /**
     * Vectorize a test image that has a single block of inside pixels
     * surrounded by a margin of outside pixels.
     * 
     * Expected result is a single polygon surrounding the inside pixels.
     */
    @Test
    public void outsideMargin() throws Exception {
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

        args.put("outsideValues", Collections.singleton(OUTSIDE));
        RenderedOp dest = doOp(src, args);
        List<Polygon> polys = getPolygons(dest, 1);

        ExpectedPoly expected = new ExpectedPoly("POLYGON((2 2, 2 8, 8 8, 8 2, 2 2))", 1);
        assertPolygons(expected, polys);
    }

    /**
     * Vectorize a test image that has a single block of data pixels
     * surrounded by a margin of pixels with a different value
     * 
     * Expected result is two polygons, one within the other. The
     * inner polygon should be simple. The outer polygon should
     * have a hole corresponding to the inner one.
     */
    @Test
    public void dataMargin() throws Exception {
        final int IMAGE_WIDTH = 10;
        final int MARGIN_WIDTH = 2;

        final int VAL1 = -1;
        final int VAL2 = 1;

        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(VAL1));

        for (int y = MARGIN_WIDTH; y < IMAGE_WIDTH - MARGIN_WIDTH; y++) {
            for (int x = MARGIN_WIDTH; x < IMAGE_WIDTH - MARGIN_WIDTH; x++) {
                src.setSample(x, y, 0, VAL2);
            }
        }

        RenderedOp dest = doOp(src, args);
        List<Polygon> polys = getPolygons(dest, 2);

        ExpectedPoly[] expected = {
            new ExpectedPoly("POLYGON((2 2, 2 8, 8 8, 8 2, 2 2))", VAL2),
            new ExpectedPoly("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0), (2 2, 2 8, 8 8, 8 2, 2 2))", VAL1)
        };

        assertPolygons(expected, polys);
    }

    /**
     * Vectorize a test image that has horizontal stripes of uniform data.
     */
    @Test
    public void horizontalStripes() throws Exception {
        final int IMAGE_WIDTH = 100;
        final int BLOCK_HEIGHT = 25;
        final int NUM_BLOCKS = 4;

        int val = -1;
        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(val));

        for (int y = 0; y < IMAGE_WIDTH; y++) {
            val = y / BLOCK_HEIGHT;
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                src.setSample(x, y, 0, val);
            }
        }

        RenderedOp dest = doOp(src, null);
        List<Polygon> polys = getPolygons(dest, NUM_BLOCKS);

        ExpectedPoly[] expected = new ExpectedPoly[NUM_BLOCKS];
        for (int i = 0; i < NUM_BLOCKS; i++) {
            int ymin = i * BLOCK_HEIGHT;
            int ymax = ymin + BLOCK_HEIGHT;
            
            String wkt = String.format("POLYGON((%d %d, %d %d, %d %d, %d %d, %d %d))",
                    0, ymin,
                    0, ymax,
                    IMAGE_WIDTH, ymax,
                    IMAGE_WIDTH, ymin,
                    0, ymin);
            expected[i] = new ExpectedPoly(wkt, i);
        }

        assertPolygons(expected, polys);
    }

    /**
     * Vectorize a test image that has vertical stripes of uniform data.
     */
    @Test
    public void verticalStripes() throws Exception {
        final int IMAGE_WIDTH = 100;
        final int BLOCK_WIDTH = 25;
        final int NUM_BLOCKS = 4;

        int val = -1;
        TiledImage src = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(val));

        for (int y = 0; y < IMAGE_WIDTH; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                src.setSample(x, y, 0, x / BLOCK_WIDTH);
            }
        }

        RenderedOp dest = doOp(src, null);
        List<Polygon> polys = getPolygons(dest, NUM_BLOCKS);

        ExpectedPoly[] expected = new ExpectedPoly[NUM_BLOCKS];
        for (int i = 0; i < NUM_BLOCKS; i++) {
            int xmin = i * BLOCK_WIDTH;
            int xmax = xmin + BLOCK_WIDTH;
            
            String wkt = String.format("POLYGON((%d %d, %d %d, %d %d, %d %d, %d %d))",
                    xmin, 0,
                    xmin, IMAGE_WIDTH,
                    xmax, IMAGE_WIDTH,
                    xmax, 0,
                    xmin, 0);
            
            expected[i] = new ExpectedPoly(wkt, i);
        }

        assertPolygons(expected, polys);
    }
    
    /**
     * Test image has alternating square blocks of pixels
     * with value of 0 or 1 like a chessboard pattern.
     */
    @Test
    public void chessboard() throws Exception {
        final int IMAGE_WIDTH = 100;
        final int SQUARE_WIDTH = 25;
        final int NUM_SQUARES_ACROSS = 4;
        final int NUM_SQUARES = 16;
        
        Valuer valuer = new Valuer() {
            public int getValue(int areaX, int areaY) {
                return (areaX % 2 == areaY % 2 ? 1 : 0);
            }
        };
        
        RenderedImage src = createChessboardImage(IMAGE_WIDTH, SQUARE_WIDTH, valuer);
        RenderedOp dest = doOp(src, null);
        List<Polygon> polys = getPolygons(dest, NUM_SQUARES);

        ExpectedPoly[] expected = new ExpectedPoly[NUM_SQUARES];
        int n = 0;
        for (int i = 0; i < NUM_SQUARES_ACROSS; i++) {
            int ymin = i * SQUARE_WIDTH;
            int ymax = ymin + SQUARE_WIDTH;
            for (int j = 0; j < NUM_SQUARES_ACROSS; j++) {
                int xmin = j * SQUARE_WIDTH;
                int xmax = xmin + SQUARE_WIDTH;

                String wkt = String.format("POLYGON((%d %d, %d %d, %d %d, %d %d, %d %d))",
                        xmin, ymin,
                        xmin, ymax,
                        xmax, ymax,
                        xmax, ymin,
                        xmin, ymin);
                
                expected[n++] = new ExpectedPoly(wkt, valuer.getValue(j, i));
            }
        }

        assertPolygons(expected, polys);
    }
    
    /**
     * Test image has alternating square blocks of pixels
     * with value of 0 or 1 like a chessboard pattern.
     * Here we set the insideEdges arg to false. Expected
     * result is a single polygon for the image boundary.
     */
    @Test
    public void chessboardNoInsideEdges() throws Exception {
        final int IMAGE_WIDTH = 100;
        final int SQUARE_WIDTH = 25;
        
        Valuer valuer = new Valuer() {
            public int getValue(int areaX, int areaY) {
                return (areaX % 2 == areaY % 2 ? 1 : 0);
            }
        };
        
        RenderedImage src = createChessboardImage(IMAGE_WIDTH, SQUARE_WIDTH, valuer);
        args.put("insideEdges", Boolean.FALSE);
        RenderedOp dest = doOp(src, args);
        List<Polygon> polys = getPolygons(dest, 1);
    }

    /**
     * Test image has alternating square blocks of pixels
     * with value of 0 or 1 like a chessboard pattern. When
     * vectorizing, 0 is set to be an outside value.
     */
    @Test
    public void chessboardInsideOutside() throws Exception {
        final int IMAGE_WIDTH = 100;
        final int SQUARE_WIDTH = 25;
        final int NUM_SQUARES_ACROSS = 4;
        final int NUM_SQUARES = 16;
        
        Valuer valuer = new Valuer() {
            public int getValue(int areaX, int areaY) {
                return (areaX % 2 == areaY % 2 ? 1 : 0);
            }
        };
        
        RenderedImage src = createChessboardImage(IMAGE_WIDTH, SQUARE_WIDTH, valuer);
        
        args.put("outsideValues", Collections.singleton(0));
        RenderedOp dest = doOp(src, args);
        List<Polygon> polys = getPolygons(dest, NUM_SQUARES / 2);

        ExpectedPoly[] expected = new ExpectedPoly[NUM_SQUARES / 2];
        int n = 0;
        for (int i = 0; i < NUM_SQUARES_ACROSS; i++) {
            int ymin = i * SQUARE_WIDTH;
            int ymax = ymin + SQUARE_WIDTH;
            for (int j = 0; j < NUM_SQUARES_ACROSS; j++) {
                int value = valuer.getValue(j, i);
                if (value != 0) {
                    int xmin = j * SQUARE_WIDTH;
                    int xmax = xmin + SQUARE_WIDTH;

                    String wkt = String.format("POLYGON((%d %d, %d %d, %d %d, %d %d, %d %d))",
                            xmin, ymin,
                            xmin, ymax,
                            xmax, ymax,
                            xmax, ymin,
                            xmin, ymin);
                
                    expected[n++] = new ExpectedPoly(wkt, value);
                }
            }
        }

        assertPolygons(expected, polys);
    }
    
    /**
     * Test image with uniform pixel values. ROI covering part of the image.
     */
    @Test
    public void imageBoundaryWithROI() throws Exception {
        final int IMAGE_WIDTH = 100;
        
        TiledImage src = ImageUtils.createConstantImage(IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));
        
        final int ROIW = IMAGE_WIDTH / 2;
        ROI roi = new ROIShape(new Rectangle(0, 0, ROIW, ROIW));
        
        args.put("roi", roi);
        RenderedOp dest = doOp(src, args);
        
        String wkt = String.format("POLYGON((0 0, 0 %d, %d %d, %d 0, 0 0))", 
                ROIW, ROIW, ROIW, ROIW);
        ExpectedPoly ep = new ExpectedPoly(wkt, 0);
        assertPolygons(ep, getPolygons(dest, 1));
    }
    
    /**
     * An ROI created from an image such that pixels alternate
     * between included and excluded. A very small source image
     * is used to avoid a slow test.
     */
    @Test
    public void smallSquaresROI() throws Exception {
        final int IMAGE_WIDTH = 10;
        final int SQUARE_WIDTH = 1;
        
        RenderedImage src = ImageUtils.createConstantImage(IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));
        
        RenderedImage roiImg =
                createChessboardImage(IMAGE_WIDTH, SQUARE_WIDTH,
                new Valuer() {

                    public int getValue(int areaX, int areaY) {
                        return (areaX % 2 == areaY % 2 ? 1 : 0);
                    }
                });
        
        ROI roi = new ROI(roiImg, 1);
        
        args.put("roi", roi);
        RenderedOp dest = doOp(src, args);
        getPolygons(dest, 50);
    }
    

    /**
     * Interface used with the {@link #createChessboardImage} method. 
     * Provides a single function that assigns values to squares.
     */
    private interface Valuer {
        int getValue(int areaX, int areaY);
    }

    /**
     * Helper method to create with square blocks in a chessboard-like pattern.
     * 
     * @param imgW image width
     * @param squareW square width
     * @param valuer instance of {@link VectorizeTest.Valuer} to assign values
     *        to squares
     * 
     * @return the created image
     */
    private RenderedImage createChessboardImage(final int imgW, final int squareW, Valuer valuer) {
        TiledImage src = ImageUtils.createConstantImage(
                imgW, imgW, Integer.valueOf(0));

        int areaX, areaY;
        for (int y = 0; y < imgW; y++) {
            areaY = y / squareW;

            for (int x = 0; x < imgW; x++) {
                areaX = x / squareW;
                src.setSample(x, y, 0, valuer.getValue(areaX, areaY));
            }
        }
        return src;
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
            if (roi != null) {
                pb.setParameter("roi", roi);
            }

            Integer band = (Integer) args.get("band");
            if (band != null) {
                pb.setParameter("band", band);
            }

            Collection<? extends Number> outsideValues = (Collection<? extends Number>) args.get("outsideValues");
            if (outsideValues != null) {
                pb.setParameter("outsideValues", outsideValues);
            }

            Boolean insideEdges = (Boolean) args.get("insideEdges");
            if (insideEdges != null) {
                pb.setParameter("insideEdges", insideEdges);
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

    private void assertPolygons(ExpectedPoly expected, List<Polygon> observed) throws Exception {
        assertPolygons(new ExpectedPoly[]{expected}, observed);
    }

    /**
     * Assert equality of expected and observed polygons.
     * 
     * @param expectedWKT WKT string for expected polygon
     * @param observed observed polygon
     */
    private void assertPolygons(ExpectedPoly[] expected, List<Polygon> observed) throws Exception {
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

    /**
     * Class to hold WKT String and a numeric value for
     * an expected polygon.
     */
    private static final class ExpectedPoly {

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
    private static final class PolyList extends ArrayList<Polygon> {

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
