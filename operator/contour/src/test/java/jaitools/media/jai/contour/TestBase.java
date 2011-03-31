/*
 * Copyright 2010-2011 Michael Bedward
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;

import static org.junit.Assert.*;

/**
 * Base class for unit test of the Contour operator.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class TestBase {
    
    protected static final double TOL = 1.0e-6d;

    protected enum Gradient { VERTICAL, HORIZONTAL, RADIAL };
    
    protected static final int IMAGE_WIDTH = 100;
    
    
    /**
     * Runs the operation and retrieves the contours.
     * 
     * @param src the source image
     * 
     * @return the contours
     */
    protected Collection<LineString> doOp(PlanarImage src, Map<String, Object> args) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", src);
        
        for (String paramName : pb.getParameterListDescriptor().getParamNames()) {
            Object obj = args.get(paramName);
            if (obj != null) {
                pb.setParameter(paramName, obj);
            }
        }

        RenderedOp dest = JAI.create("Contour", pb);
        Object prop = dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        
        return (Collection<LineString>) prop;
    }
    
    /**
     * Check a single contour's end-points and number of coordinates.
     */
    protected void assertContour(LineString contour, double x0, double y0, double x1, double y1) {
        contour.normalize(); 
        Coordinate[] coords = contour.getCoordinates();
        final int N = coords.length;
        
        assertEquals(coords[0].x, x0, TOL);
        assertEquals(coords[0].y, y0, TOL);
        assertEquals(coords[N-1].x, x1, TOL);
        assertEquals(coords[N-1].y, y1, TOL);
    }

    /**
     * Asserts that a collection of contours matches those expected.
     *
     * @param contours collection returned by the operator
     * @param expected expected contours
     */
    protected void assertContoursMatch(Collection<LineString> contours, LineString ...expected) {
        assertEquals(expected.length, contours.size());

        // copy into a new collection just in case the caller needs the
        //  input collection afterwards
        List<LineString> list = CollectionFactory.list();
        list.addAll(contours);

        for (LineString contour : list) {
            contour.normalize();
        }

        for (LineString exp : expected) {
            boolean found = false;
            exp.normalize();
            // might be JTS pre 1.12 so do equality test explicitly
            for (LineString contour : list) {
                if (exp.equalsExact(contour, TOL)) {
                    list.remove(contour);
                    found = true;
                    break;
                }
            }

            assertTrue("Expected contour not found", found);
        }

        assertEquals("result collection had additional contours", 0, list.size());
    }

    
    /**
     * Creates an image with a linear gradient of values.
     * 
     * @param gradient direction of the gradient
     * 
     * @return the image
     */
    protected TiledImage createGradientImage(Gradient gradient) {
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
