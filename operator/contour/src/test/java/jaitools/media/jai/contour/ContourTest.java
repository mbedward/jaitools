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

package jaitools.media.jai.contour;

import javax.media.jai.ROI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import jaitools.imageutils.ImageUtils;
import static jaitools.numeric.DoubleComparison.*;

import java.util.Map;
import javax.media.jai.PlanarImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the "Contour" operation.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourTest {
    
    enum Gradient { VERTICAL, HORIZONTAL, RADIAL };
    
    private static final int IMAGE_WIDTH = 100;
    
    private Map<String, Object> args;
    
    @Before
    public void setup() {
        args = new HashMap<String, Object>();
    }

    /**
     * Trace a single contour in a source image with a vertical
     * gradient of values. Contour simplification is on (default).
     */
    @Test
    public void singleContourVerticalGradient() {
        PlanarImage src = createGradientImage(Gradient.VERTICAL);
        
        args.put("levels", Collections.singleton(IMAGE_WIDTH / 2));
        Collection<LineString> contours = doOp(src);
        assertEquals(1, contours.size());
        
        LineString contour = contours.iterator().next();
        assertContour(contour, 0, IMAGE_WIDTH/2, IMAGE_WIDTH-1, IMAGE_WIDTH/2, 2);
    }
    
    /**
     * Same as test singleContourVerticalGradient but contour simplification 
     * is turned off so we should get one coordinate per pixel.
     */
    @Test
    public void doNotSimplify() {
        PlanarImage src = createGradientImage(Gradient.VERTICAL);
        
        args.put("levels", Collections.singleton(IMAGE_WIDTH / 2));
        args.put("simplify", false);
        
        Collection<LineString> contours = doOp(src);
        assertEquals(1, contours.size());
        
        LineString contour = contours.iterator().next();
        assertContour(contour, 0, IMAGE_WIDTH/2, IMAGE_WIDTH-1, IMAGE_WIDTH/2, IMAGE_WIDTH);
    }
    
    /**
     * Trace a single contour in a source image with a horizontal
     * gradient of values. Contour simplification is on (default).
     */
    @Test
    public void singleContourHorizontalGradient() {
        PlanarImage src = createGradientImage(Gradient.HORIZONTAL);
        
        args.put("levels", Collections.singleton(IMAGE_WIDTH / 2));
        Collection<LineString> contours = doOp(src);
        assertEquals(1, contours.size());
        
        LineString contour = contours.iterator().next();
        assertContour(contour, IMAGE_WIDTH/2, 0, IMAGE_WIDTH/2, IMAGE_WIDTH-1, 2);
    }
    
    /**
     * Trace a single ring contour from a source image with a radial value
     * gradient and check that each vertex of the contour is within an 
     * acceptable distance from the image centre.
     */
    @Test
    public void singleContourRadialGradient() {
        PlanarImage src = createGradientImage(Gradient.RADIAL);
        
        final double value = IMAGE_WIDTH / 3.0d;
        args.put("levels", Collections.singleton(value));
        Collection<LineString> contours = doOp(src);

        assertEquals(1, contours.size());
        
        LineString contour = contours.iterator().next();
        Coordinate mid = new Coordinate(IMAGE_WIDTH/2, IMAGE_WIDTH/2);
        final double tol = value / 100.0;
        for (Coordinate c : contour.getCoordinates()) {
            assertTrue(dequal(value, c.distance(mid), tol));
        }
    }
    
    /**
     * Helper method: runs the operation and retrieves the contours.
     * 
     * @param src the source image
     * 
     * @return the contours
     */
    private Collection<LineString> doOp(PlanarImage src) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", src);

        if (args.containsKey("roi")) pb.setParameter("roi", (ROI)args.get("roi"));
        if (args.containsKey("band")) pb.setParameter("band", (Integer)args.get("band"));
        if (args.containsKey("levels")) pb.setParameter("levels", (Collection)args.get("levels"));
        if (args.containsKey("simplify")) pb.setParameter("simplify", (Boolean)args.get("simplify"));
        if (args.containsKey("mergeTiles")) pb.setParameter("mergeTiles", (Boolean)args.get("mergeTiles"));
        if (args.containsKey("smooth")) pb.setParameter("smooth", (Boolean)args.get("smooth"));
        
        RenderedOp dest = JAI.create("Contour", pb);
        Object prop = dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        
        return (Collection<LineString>) prop;
    }
    
    /**
     * Helper method to check a single contour's end-points and number of coordinates.
     * 
     * @param contour the contour
     * @param x0 expected first x
     * @param y0 expected first y
     * @param x1 expected last x
     * @param y1 expected last y
     * @param N  expected number of coordinates
     */
    private void assertContour(LineString contour, double x0, double y0, double x1, double y1, int N) {
        contour.normalize(); 
        Coordinate[] coords = contour.getCoordinates();
        assertEquals(coords.length, N);
        
        assertTrue( dequal(coords[0].x, x0) );
        assertTrue( dequal(coords[0].y, y0) );
        assertTrue( dequal(coords[N-1].x, x1) );
        assertTrue( dequal(coords[N-1].y, y1) );
    }
    
    /**
     * Creates an image with a linear gradient of values.
     * 
     * @param gradient direction of the gradient
     * 
     * @return the image
     */
    private PlanarImage createGradientImage(Gradient gradient) {
        TiledImage src = ImageUtils.createConstantImage(IMAGE_WIDTH, IMAGE_WIDTH, Double.valueOf(0));
        
        switch (gradient) {
            case HORIZONTAL:
                for (int y = 0; y < IMAGE_WIDTH; y++) {
                    for (int x = 0; x < IMAGE_WIDTH; x++) {
                        src.setSample(x, y, 0, x);
                    }
                }
                break;
                
            case RADIAL:
                double mid = IMAGE_WIDTH / 2;
                for (int y = 0; y < IMAGE_WIDTH; y++) {
                    double yd2 = (y - mid)*(y - mid);
                    for (int x = 0; x < IMAGE_WIDTH; x++) {
                        double xd2 = (x - mid)*(x - mid);
                        src.setSample(x, y, 0, Math.sqrt(yd2 + xd2));
                    }
                }
                break;
                
            case VERTICAL:
                for (int y = 0; y < IMAGE_WIDTH; y++) {
                    for (int x = 0; x < IMAGE_WIDTH; x++) {
                        src.setSample(x, y, 0, y);
                    }
                }
                break;
        }

        return src;
    }
}
