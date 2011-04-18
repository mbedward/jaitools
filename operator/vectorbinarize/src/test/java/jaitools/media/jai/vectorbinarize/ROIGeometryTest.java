/*
 * Copyright 2011 Michael Bedward
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

package jaitools.media.jai.vectorbinarize;

import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.SubtractDescriptor;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.WKTReader;

import jaitools.imageutils.ROIGeometry;
import jaitools.swing.SimpleImagePane;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class ROIGeometryTest {
    
    // Set this to true to display test ROIs as images.
    private static final boolean INTERACTIVE = false;
    
    // Used to avoid problems with Hudson's headless build if INTERACTIVE
    // is true.
    private static boolean headless;
    
    // Width of frame when visualizing tests
    private static final int VIZ_WIDTH = 600;

    // Flag for running on OSX, used to skip some tests
    private static boolean isOSX;
    
    
    @BeforeClass
    public static void beforeClass() {
        GraphicsEnvironment grEnv = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
        headless = grEnv.isHeadless();

        String osname = System.getProperty("os.name").replaceAll("\\s", "");
        isOSX = "macosx".equalsIgnoreCase(osname);
    }

    @Test
    public void testCheckerBoard() throws Exception {
        String wkt = "MULTIPOLYGON (((4 4, 4 0, 8 0, 8 4, 4 4)), ((4 4, 4 8, 0 8, 0 4, 4 4)))";
        MultiPolygon poly = (MultiPolygon) new WKTReader().read(wkt);

        ROIGeometry g = new ROIGeometry(poly);
        ROIShape shape = getEquivalentROIShape(g);
        
        assertROIEquivalent(g, shape, "Checkerboard");
    }
    
    @Test
    public void testFractional() throws Exception {
        String wkt = "POLYGON ((0.4 0.4, 0.4 5.6, 4.4 5.6, 4.4 0.4, 0.4 0.4))";
        Polygon poly = (Polygon) new WKTReader().read(wkt);

        ROIGeometry g = new ROIGeometry(poly);
        ROIShape shape = getEquivalentROIShape(g);
        
        assertROIEquivalent(g, shape, "Fractional");
    }
    
    @Test 
    public void testCircles() throws Exception {
        if (isOSX) {
            System.out.println("skipping testCircles on OSX");
        } else {
            final int buffers[] = new int[]{3, 5, 7, 8, 10, 15, 20};
            for (int i = 0; i < buffers.length; i++) {
                Point p = new GeometryFactory().createPoint(new Coordinate(10, 10));
                Geometry buffer = p.buffer(buffers[i]);

                ROIGeometry g = new ROIGeometry(buffer);
                ROIShape shape = getEquivalentROIShape(g);

                assertROIEquivalent(g, shape, "Circle");
            }
        }
    }
    

    @Test
    @Ignore("not working on any platform ?")
    public void testUnion() throws Exception {
        Point p1 = new GeometryFactory().createPoint(new Coordinate(10, 10)); 
        Point p2 = new GeometryFactory().createPoint(new Coordinate(20, 10));
        Geometry buffer1 = p1.buffer(15);
        Geometry buffer2 = p2.buffer(15);
        
        ROIGeometry rg1 = new ROIGeometry(buffer1);
        ROIGeometry rg2 = new ROIGeometry(buffer2);
                
        ROIShape rs1 = getEquivalentROIShape(rg1);
        ROIShape rs2 = getEquivalentROIShape(rg2);
//        printRoiShape(rs1);
//        printRoiShape(rs2);
        
        ROIGeometry rgUnion = (ROIGeometry) rg1.add(rg2);
//        System.out.println("UNION\n " + rgUnion.getAsGeometry().toString());
        
        ROI rsUnion = rs1.add(rs2);
//        printRoiShape((ROIShape) rsUnion); 
          

        assertROIEquivalent(rg1, rs1, "circle 1 ROIG, circle 1 ROIS");
        assertROIEquivalent(rg2, rs2, "circle 2 ROIG, circle 2 ROIS");
        
//        assertROIEquivalent(rgUnion, rsUnion, "Union");
    }
    
    @Test
    @Ignore("not working on any platform ?")
    public void testIntersect() throws Exception {
        Point p1 = new GeometryFactory().createPoint(new Coordinate(10, 10)); 
        Point p2 = new GeometryFactory().createPoint(new Coordinate(20, 10));
        Geometry buffer1 = p1.buffer(15);
        Geometry buffer2 = p2.buffer(15);
        
        ROIGeometry rg1 = new ROIGeometry(buffer1);
        ROIGeometry rg2 = new ROIGeometry(buffer2);
        ROI rgIntersection = rg1.intersect(rg2);
        
        ROIShape rs1 = getEquivalentROIShape(rg1);
        ROIShape rs2 = getEquivalentROIShape(rg2);
        ROI rsIntersection = rs1.intersect(rs2);

        assertROIEquivalent(rgIntersection, rsIntersection, "Intersection");
    }
    
    @Test
    public void testSubtract() throws Exception {
        Point p1 = new GeometryFactory().createPoint(new Coordinate(10, 10)); 
        Point p2 = new GeometryFactory().createPoint(new Coordinate(20, 10));
        Geometry buffer1 = p1.buffer(15);
        Geometry buffer2 = p2.buffer(15);
        
        ROIGeometry rg1 = new ROIGeometry(buffer1);
        ROIGeometry rg2 = new ROIGeometry(buffer2);
        ROI rgSubtract = rg1.subtract(rg2);
        
        ROIShape rs1 = getEquivalentROIShape(rg1);
        ROIShape rs2 = getEquivalentROIShape(rg2);
        ROI rsSubtract = rs1.subtract(rs2);

        assertROIEquivalent(rgSubtract, rsSubtract, "Subtract");
    }
    
    @Test
    public void testXor() throws Exception {
        if (isOSX) {
            System.out.println("skipping testXor on OSX");
        } else {
            Point p1 = new GeometryFactory().createPoint(new Coordinate(10, 10));
            Point p2 = new GeometryFactory().createPoint(new Coordinate(20, 10));
            Geometry buffer1 = p1.buffer(15);
            Geometry buffer2 = p2.buffer(15);

            ROIGeometry rg1 = new ROIGeometry(buffer1);
            ROIGeometry rg2 = new ROIGeometry(buffer2);
            ROI rgXor = rg1.exclusiveOr(rg2);

            ROIShape rs1 = getEquivalentROIShape(rg1);
            ROIShape rs2 = getEquivalentROIShape(rg2);
            ROI rsXor = rs1.exclusiveOr(rs2);

            assertROIEquivalent(rgXor, rsXor, "Xor");
        }
    }
    
    @Test
    public void testRotatedRectangle() throws Exception {
        if (isOSX) {
            System.out.println("skipping testRotatedRectangle on OSX");
        } else {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON((20 0, 50 -30, 30 -50, 0 -20, 20 0))");

            ROIGeometry g = new ROIGeometry(polygon);
            ROIShape shape = getEquivalentROIShape(g);

            assertROIEquivalent(g, shape, "RotatedRectangle");
        }
    }
    
    @Test
    public void testRotatedRectangleUnion() throws Exception {
        if (isOSX) {
            System.out.println("skipping testRotatedRectangleUnion on OSX");
        } else {
            Polygon polygon1 = (Polygon) new WKTReader().read("POLYGON((20 0, 50 -30, 30 -50, 0 -20, 20 0))");
            Polygon polygon2 = (Polygon) new WKTReader().read("POLYGON((60 -40, 80 -20, 40 20, 20 0, 60 -40))");

            ROIGeometry geom1 = new ROIGeometry(polygon1);
            ROIShape shape1 = getEquivalentROIShape(geom1);

            ROIGeometry geom2 = new ROIGeometry(polygon2);
            ROIShape shape2 = getEquivalentROIShape(geom2);

            final ROI geomUnion = geom1.add(geom2);
            final ROI shapeUnion = shape1.add(shape2);

            assertROIEquivalent(geomUnion, shapeUnion, "RotatedUnion");
        }
    }
    
    @Test
    public void testRotatedRectangleIntersection() throws Exception {
        if (isOSX) {
            System.out.println("skipping testRotatedRectangleIntersection on OSX");
        } else {
            Polygon polygon1 = (Polygon) new WKTReader().read("POLYGON((20 0, 50 -30, 30 -50, 0 -20, 20 0))");
            Polygon polygon2 = (Polygon) new WKTReader().read("POLYGON((40 -40, 60 -20, 20 20, 0 0, 40 -40))");

            ROIGeometry geom1 = new ROIGeometry(polygon1);
            ROIShape shape1 = getEquivalentROIShape(geom1);

            ROIGeometry geom2 = new ROIGeometry(polygon2);
            ROIShape shape2 = getEquivalentROIShape(geom2);

            final ROI geomUnion = geom1.intersect(geom2);
            final ROI shapeUnion = shape1.intersect(shape2);

            assertROIEquivalent(geomUnion, shapeUnion, "RotatedIntersection");
        }
    }
    
    @Test
    public void testUnionFractional() throws Exception{
        final String geom1 = "POLYGON ((256.0156254550953 384.00000013906043, 384.00000082678343 384.00000013906043, 384.00000082678343 256.00000005685433, 256.0000004550675 256.00000005685433, 256.0000004550675 384.00000013906043, 256.0156254550953 384.00000013906043))"; 
        final String geom2 = "POLYGON ((384.0156256825708 128.00000008217478, 512.0000010543083 128.00000008217478, 512.0000010543083 -0.0000000000291038, 384.00000068254303 -0.0000000000291038, 384.00000068254303 128.00000008217478, 384.0156256825708 128.00000008217478))";
        
        final WKTReader wktReader = new WKTReader();
        final Geometry geometry1 = wktReader.read(geom1);
        final Geometry geometry2 = wktReader.read(geom2);

        final ROIGeometry roiGeom1 = new ROIGeometry(geometry1);
        final ROIGeometry roiGeom2 = new ROIGeometry(geometry2);
        final ROI roiGeometryUnion = roiGeom1.add(roiGeom2);
        
        final ROIShape roiShape1 = getEquivalentROIShape(roiGeom1);
        final ROIShape roiShape2 = getEquivalentROIShape(roiGeom2);
        final ROI roiShapeUnion = roiShape1.add(roiShape2);
        
        assertROIEquivalent(roiGeometryUnion, roiShapeUnion, "Union");
    }
    
    @Test
    public void testUnionTransformedFractional() throws Exception {
        if (isOSX) {
            System.out.println("skipping testUnionTransformedFractional on OSX");
        } else {
            final String geom1 = "POLYGON ((256.0156254550953 384.00000013906043, 384.00000082678343 384.00000013906043, 384.00000082678343 256.00000005685433, 256.0000004550675 256.00000005685433, 256.0000004550675 384.00000013906043, 256.0156254550953 384.00000013906043))";
            final String geom2 = "POLYGON ((384.0156256825708 128.00000008217478, 512.0000010543083 128.00000008217478, 512.0000010543083 -0.0000000000291038, 384.00000068254303 -0.0000000000291038, 384.00000068254303 128.00000008217478, 384.0156256825708 128.00000008217478))";

            final WKTReader wktReader = new WKTReader();
            final Geometry geometry1 = wktReader.read(geom1);
            final Geometry geometry2 = wktReader.read(geom2);
            geometry1.apply(new AffineTransformation(1.1, 1.1, 0, 0, 1.1, 0));
            geometry2.apply(new AffineTransformation(0, 1.1, 0, 1.1, 0, 0));

            final ROIGeometry roiGeom1 = new ROIGeometry(geometry1);
            final ROIGeometry roiGeom2 = new ROIGeometry(geometry2);
            final ROI roiGeometryUnion = roiGeom1.add(roiGeom2);

            final ROIShape roiShape1 = getEquivalentROIShape(roiGeom1);
            final ROIShape roiShape2 = getEquivalentROIShape(roiGeom2);
            final ROI roiShapeUnion = roiShape1.add(roiShape2);

            assertROIEquivalent(roiGeometryUnion, roiShapeUnion, "Union");
        }
    }

    /**
     * Turns the roi geometry in a ROIShape with the same geometry
     * @param g
     * @return
     */
    ROIShape getEquivalentROIShape(ROIGeometry g) {
        // get the roi geometry as shape, this generate a JTS wrapper
        final Shape shape = g.getAsShape();
        // the JTS wrapper sucks, build a general path out of it
        final GeneralPath gp = new GeneralPath(shape);
        // finally return the roi shape
        return new ROIShape(gp);
    }
    
    /**
     * Checks two ROIs are equivalent
     * @param first
     * @param second
     * @param title
     * @throws IOException
     */
    void assertROIEquivalent(ROI first, ROI second, String title) throws IOException {
        RenderedImage firstImage = first.getAsImage();
        RenderedImage secondImage = second.getAsImage();
        visualize(firstImage, secondImage, title);
        
        assertImagesEqual(firstImage, secondImage);
    }
    
    void assertImagesEqual(final RenderedImage image1, final RenderedImage image2) {
        
        // Preliminar checks on image properties
        assertEquals(image1.getWidth(), image2.getWidth());
        assertEquals(image1.getHeight(), image2.getHeight());
        
        // pixel by pixel difference check
        RenderedImage int1 = FormatDescriptor.create(image1, DataBuffer.TYPE_SHORT, null);
        RenderedImage int2 = FormatDescriptor.create(image2, DataBuffer.TYPE_SHORT, null);
        RenderedImage diff = SubtractDescriptor.create(int1, int2, null);
        RenderedImage extremaImg = ExtremaDescriptor.create(diff, null, 1, 1, false, Integer.MAX_VALUE, null);
        double[][] extrema = (double[][]) extremaImg.getProperty("extrema");
        for (int band = 0; band < extrema.length; band++) {
            assertEquals("Minimum should be 0", 0d, extrema[0][band], 1e-9);
            assertEquals("Maximum should be 0", 0d, extrema[1][band], 1e-9);
        }
    }
    
    /**
     * Shows the two images in the frame window and wait for the windows close before returning. 
     * @param ri1 the first image to be visualized
     * @param ri2 the second image to be visualized
     * @param title the title to be assigned to the window
     */
    void visualize(final RenderedImage ri1, final RenderedImage ri2, String title) throws IOException {
        
        if(INTERACTIVE && !headless) {
            final Thread mainThread = Thread.currentThread();
            
            final JFrame frame = new JFrame(title + " - Close the window to continue");

            final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            
            SimpleImagePane sip1 = new SimpleImagePane();
            sip1.setImage(ri1);
            splitPane.setLeftComponent(sip1);
            
            SimpleImagePane sip2 = new SimpleImagePane();
            sip2.setImage(ri2);
            splitPane.setRightComponent(sip2);
            
            
            frame.getContentPane().add(splitPane);
            
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                      mainThread.interrupt();
                }
            });
    
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame.pack();
                    frame.setSize(VIZ_WIDTH, VIZ_WIDTH / 2);
                    frame.setVisible(true);
                    splitPane.setDividerLocation(0.5);
                    frame.repaint();
                }
            });
            
            
            try {
                mainThread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // move on
            }
        }
    }
    
    private void printRoiShape(ROIShape rs1) {
        PathIterator pt1 = rs1.getAsShape().getPathIterator(null);
        float [] coords = new float[2];
        System.out.print("POLYGON ((");
        while (!pt1.isDone()){
            pt1.currentSegment(coords);
            System.out.print(coords[0] + " " + coords[1] + ",");
            pt1.next();
        }
        System.out.println("))/n");
    }
}
