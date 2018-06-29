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

package org.jaitools.imageutils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;

import org.jaitools.jts.CoordinateSequence2D;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ROIGeometry.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ROIGeometryTest {
    
    private static GeometryFactory gf = new GeometryFactory();

    public ROIGeometryTest() {
    }
    
    @Ignore
    @Test
    public void testAdd() {
        System.out.println("add");
    }

    @Test
    public void testContains_Point() {
        System.out.println("contains Point");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Point p0 = new Point(-1, 2);
        Point p1 = new Point(-2, 1);
        
        assertTrue(roi.contains(p0));
        assertFalse(roi.contains(p1));
    }

    @Test
    public void testContains_Point2D() {
        System.out.println("contains Point2D");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Point2D p0 = new Point2D.Double(-1.0, 2.5);
        Point2D p1 = new Point2D.Double(-2.5, 1.0);
        
        assertTrue(roi.contains(p0));
        assertFalse(roi.contains(p1));
    }

    @Test
    public void testContains_int_int() {
        System.out.println("contains (int int)");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.contains(-1, 2));
        assertFalse(roi.contains(-2, 1));
    }

    @Test
    public void testContains_double_double() {
        System.out.println("contains (double double)");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.contains(-1.0, 2.5));
        assertFalse(roi.contains(-2.5, 1.0));
    }

    @Test
    public void testContains_Rectangle() {
        System.out.println("contains Rectangle");
        
        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle r = new Rectangle(-1, -2, 4, 6);
        assertTrue(roi.contains(r));
        
        r.width += 1;
        assertFalse(roi.contains(r));
    }

    @Test
    public void testContains_Rectangle2D() {
        System.out.println("contains Rectangle2D");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle2D r = new Rectangle2D.Double(-1.0, -2.0, 4.0, 6.0);
        assertTrue(roi.contains(r));
        
        r = new Rectangle2D.Double(-1.2, -2.0, 4.0, 6.0);
        assertFalse(roi.contains(r));
    }

    @Test
    public void testContains_intRectArgs() {
        System.out.println("contains int rect args");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.contains(-1, -2, 4, 6));
        assertFalse(roi.contains(-1, -2, 5, 6));
    }

    @Test
    public void testContains_doubleRectArgs() {
        System.out.println("contains double rect args");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.contains(-1.0, -2.0, 4.0, 6.0));
        assertFalse(roi.contains(-1.0, -2.0, 5.0, 6.0));
    }

    @Ignore
    @Test
    public void testExclusiveOr() {
        System.out.println("exclusiveOr");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testGetAsBitmask() {
        System.out.println("getAsBitmask");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testGetAsImage() {
        System.out.println("getAsImage");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testGetAsRectangleList_4args() {
        System.out.println("getAsRectangleList");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testGetAsRectangleList_5args() {
        System.out.println("getAsRectangleList");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testGetAsShape() {
        System.out.println("getAsShape");
        fail("not implemented");
    }

    @Test
    public void testGetBounds() {
        System.out.println("getBounds");
        
        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle expected = new Rectangle(-1, -2, 4, 6);
        assertTrue( expected.equals(roi.getBounds()) );
    }

    @Test
    public void testGetBounds2D() {
        System.out.println("getBounds2D");
        
        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle2D expected = new Rectangle2D.Double(-1.1, -2.2, 4.4, 6.6);
        Rectangle2D result = roi.getBounds2D();

        assertEquals(expected.getMinX(), result.getMinX(), 0.0001);
        assertEquals(expected.getMinY(), result.getMinY(), 0.0001);
        assertEquals(expected.getWidth(), result.getWidth(), 0.0001);
        assertEquals(expected.getHeight(), result.getHeight(), 0.0001);
    }

    @Ignore
    @Test
    public void testGetThreshold() {
        System.out.println("getThreshold");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testIntersect() {
        System.out.println("intersect");
        fail("not implemented");
    }

    @Test
    public void testIntersects_Rectangle() {
        System.out.println("intersects Rectangle");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle r = new Rectangle(0, 0, 10, 10);
        assertTrue(roi.intersects(r));
        
        r.x = r.y = 5;
        assertFalse(roi.intersects(r));
    }

    @Test
    public void testIntersects_Rectangle2D() {
        System.out.println("intersects Rectangle2D");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        Rectangle2D r = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        assertTrue(roi.intersects(r));
        
        r = new Rectangle2D.Double(-10.0, -10.0, 5.0, 5.0);
        assertFalse(roi.intersects(r));
    }

    @Test
    public void testIntersects_intRectArgs() {
        System.out.println("intersects int rect args");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.intersects(0, 0, 10, 10));
        assertFalse(roi.intersects(-10, -10, 5, 5));
    }

    @Test
    public void testIntersects_doubleRectArgs() {
        System.out.println("intersects double rect args");

        ROIGeometry roi = createRectROI(-1.1, -2.2, 3.3, 4.4);
        assertTrue(roi.intersects(-5.0, -5.0, 5.0, 5.0));
        assertFalse(roi.intersects(-10.0, -10.0, 5.0, 5.0));
    }
    
    @Test
    public void canCreateFromEmptyGeometry() {
        System.out.println("can create empty ROI");
        createEmptyROI();
    }
    
    @Test
    public void emptyROIContainsPoint() {
        System.out.println("empty ROI should not contain point");
        ROIGeometry roi = createEmptyROI();
        assertFalse(roi.contains(0, 0));
    }
    
    @Test
    public void emptyROIContainsRect() {
        System.out.println("empty ROI should not contain rectangle");
        ROIGeometry empty = createEmptyROI();
        assertFalse(empty.contains(0, 0, 1, 1));
    }
    
    @Test 
    public void addNonEmptyROIToEmpty() {
        System.out.println("add ROI to empty ROI");
        ROIGeometry nonEmpty = createRectROI(0, 0, 10, 10);
        ROIGeometry empty = createEmptyROI();
        ROI result = empty.add(nonEmpty);
        
        assertTrue( result.getBounds().equals(nonEmpty.getBounds()) );
    }
    
    @Test
    public void addEmptyROIToNonEmptyROI() {
        System.out.println("add empty ROI to non-empty ROI");
        ROIGeometry nonEmpty = createRectROI(0, 0, 10, 10);
        ROIGeometry empty = createEmptyROI();
        ROI result = nonEmpty.add(empty);
        
        assertTrue( result.getBounds().equals(nonEmpty.getBounds()) );
    }

    @Ignore
    @Test
    public void testPerformImageOp_4args_1() {
        System.out.println("performImageOp");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testPerformImageOp_4args_2() {
        System.out.println("performImageOp");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testSetThreshold() {
        System.out.println("setThreshold");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testSubtract() {
        System.out.println("subtract");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testTransform_AffineTransform_Interpolation() {
        System.out.println("transform");
        fail("not implemented");
    }

    @Ignore
    @Test
    public void testTransform_AffineTransform() {
        System.out.println("transform");
        fail("not implemented");
    }

    private ROIGeometry createRectROI(double x0, double y0, double x1, double y1) {
        CoordinateSequence2D cs = new CoordinateSequence2D(
                x0, y0, x0, y1, x1, y1, x1, y0, x0, y0);
        
        Polygon poly = gf.createPolygon(gf.createLinearRing(cs), null);
        return new ROIGeometry(poly, false);
    }

    private ROIGeometry createEmptyROI() {
        Polygon poly = gf.createPolygon(null, null);
        return new ROIGeometry(poly, false);
    }
}
