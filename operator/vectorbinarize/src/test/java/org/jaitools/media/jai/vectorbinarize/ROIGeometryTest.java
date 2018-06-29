/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.vectorbinarize;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileScheduler;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.SubtractDescriptor;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.jaitools.imageutils.ROIGeometry;
import org.jaitools.imageutils.shape.LiteShape;
import org.jaitools.swing.SimpleImagePane;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.io.WKTReader;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;


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
        JAI.setDefaultTileSize(new Dimension(512, 512));
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
    public void testLargeConcurrentCheckerBoard() throws Exception {
        String wkt = "MULTIPOLYGON (((400 400, 400 0, 800 0, 800 400, 400 400)), ((400 400, 400 800, 0 800, 0 400, 400 400)))";
        MultiPolygon poly = (MultiPolygon) new WKTReader().read(wkt);

        ImageLayout layout = new ImageLayout(0, 0, 800, 800, 0, 0, 10, 10, null, null);
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        ROIGeometry g = new ROIGeometry(poly, hints);
        RenderedOp image = (RenderedOp) g.getAsImage();

        // start parallel prefetching
        TileScheduler ts = (TileScheduler) image.getRenderingHint(JAI.KEY_TILE_SCHEDULER);
        ts.setParallelism(32);
        ts.setPrefetchParallelism(32);
        List<java.awt.Point> tiles = new ArrayList<java.awt.Point>();
        for (int i = 0; i < image.getNumXTiles(); i++) {
            for (int j = 0; j < image.getNumYTiles(); j++) {
                tiles.add(new java.awt.Point(image.getMinTileX() + i, image.getMinTileY() + j));
            }
        }
        java.awt.Point[] tileArray = (java.awt.Point[]) tiles.toArray(new java.awt.Point[tiles.size()]);
        ts.prefetchTiles(image, tileArray);
        
        // check tile by tile
        assertEquals(0, image.getMinX());
        assertEquals(0, image.getMinY());
        assertEquals(10, image.getTileWidth());
        assertEquals(10, image.getTileHeight());
        for (int x = 0; x < image.getNumXTiles(); x++) {
            for (int y = 0; y < image.getNumYTiles(); y++) {
                Raster tile = image.getTile(x, y);
                int[] pixel = new int[1];
                tile.getPixel(tile.getMinX(), tile.getMinY(), pixel);
                if(x < 40 && y < 40 || x >= 40 && y >= 40) {
                    assertEquals("Expected 0 at x = " + x + ", y = " + y, 0, pixel[0]);
                } else {
                    assertEquals("Expected 1 at x = " + x + ", y = " + y, 1, pixel[0]);
                }
            }
        }
    }

    
    @Test
    public void testTopologyException() throws Exception {
        String wkt = "POLYGON ((4 4, 4 0, 8 0, 8 4, 4 4, 4 0, 8 0, 8 2, 4 4))";
        Polygon poly = (Polygon) new WKTReader().read(wkt);

        ROIGeometry g = new ROIGeometry(poly);
        assertROIEquivalent(g, g, "Deal with Topology exception");
        
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
    public void testFractionalUnion() throws Exception {
        // this odd beast is a real world case that was turned into a test
        // due to the amount of blood and tears it took to get it to work
        String[] coords = new String[] {
                "1750 1312.5, 508 1312.5, 508 1609, 1750 1609, 1750 1312.5",
                "508 875, 508 1312, 1750 1312, 1750 875, 508 875",
                "508 875, 1750 875, 1750 437.5, 508 437.5, 508 875",
                "508 377, 508 437, 1750 437, 1750 377, 508 377" };
        String[] wkts = new String[] { "POLYGON ((" + coords[0] + "))",
                "POLYGON ((" + coords[1] + "))", "POLYGON ((" + coords[2] + "))",
                "POLYGON ((" + coords[3] + "))" };
        final int size = wkts.length;
        final Polygon[] polygons = new Polygon[size];
        final ROIGeometry[] roiGeometries = new ROIGeometry[size];
        final ROIShape[] roiShapes = new ROIShape[size];
        ROI unionGeometry = null;
        ROI unionShape = null;
        for (int i = 0; i < size; i++) {
            polygons[i] = (Polygon) new WKTReader().read(wkts[i]);
            roiGeometries[i] = new ROIGeometry(polygons[i]);
            final GeneralPath gp = new GeneralPath();
            final String[] wkt = coords[i].split(",");
            final int segments = wkt.length;
            for (int k = 0; k < segments; k++) {
                String[] x_y = wkt[k].trim().split(" ");
                if (k == 0) {
                    gp.moveTo(Float.valueOf(x_y[0]), Float.valueOf(x_y[1]));
                } else {
                    gp.lineTo(Float.valueOf(x_y[0]), Float.valueOf(x_y[1]));
                }
            }
            roiShapes[i] = new ROIShape(gp);
            if (i == 0) {
                unionGeometry = new ROIGeometry(((ROIGeometry) roiGeometries[i]).getAsGeometry());
                unionShape = roiShapes[0];
            } else {
                unionGeometry = unionGeometry.add(roiGeometries[i]);
                unionShape = unionShape.add(roiShapes[i]);
            }
        }
        Shape shape = new LiteShape(((ROIGeometry) unionGeometry).getAsGeometry());

        if (INTERACTIVE) {
            printRoiShape((ROIShape) unionShape, "unionShape");
            System.out.println(((ROIGeometry) unionGeometry).getAsGeometry());
            printShape(shape, "unionGeometry");
        }

        assertROIEquivalent(unionGeometry, unionShape, "Fractional union");
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

        assertROIEquivalent(rg1, rs1, "circle 1 ROIG, circle 1 ROIS");
        assertROIEquivalent(rg2, rs2, "circle 2 ROIG, circle 2 ROIS");
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
        return new ROIShape(shape);
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
    
    private boolean assertReportErrorImagesEqual(ROI first, ROI second) {
        RenderedImage image1 = first.getAsImage();
        RenderedImage image2 = second.getAsImage();
        boolean isOk = true;
        isOk &= (image1.getWidth() == image2.getWidth());
        isOk &= (image1.getHeight() == image2.getHeight());
        double[][] extrema = computeExtrema(image1, image2);
        for (int band = 0; band < extrema.length; band++) {
            isOk &= (Math.abs(0d - extrema[0][band]) < 1e-9);
            isOk &= (Math.abs(0d - extrema[1][band]) < 1e-9);
        }
        
        return isOk;
    }

    private void printError(ROI first, ROI second) {
        if (first == null || second == null){
            System.out.println("A ROI is missing");
        }
        if (first instanceof ROIGeometry){
            printGeometry(((ROIGeometry)first), "ROIGeometry");
            printRoiShape(((ROIShape)second), "ROIShape");
        } else {
            printGeometry(((ROIGeometry)second), "ROIGeometry");
            printRoiShape(((ROIShape)first), "ROIShape");
        }
        
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
    
    private double[][] computeExtrema(RenderedImage image1, RenderedImage image2) {
        RenderedImage int1 = FormatDescriptor.create(image1, DataBuffer.TYPE_SHORT, null);
        RenderedImage int2 = FormatDescriptor.create(image2, DataBuffer.TYPE_SHORT, null);
        RenderedImage diff = SubtractDescriptor.create(int1, int2, null);
        RenderedImage extremaImg = ExtremaDescriptor.create(diff, null, 1, 1, false, Integer.MAX_VALUE, null);
        double[][] extrema = (double[][]) extremaImg.getProperty("extrema");
        return extrema;
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
    
    private static void printShape(final Shape shape, final String title) {
        PathIterator pt1 = shape.getPathIterator(null);
        double [] coords = new double[2];
        StringBuilder sb = new StringBuilder();
        sb.append(title + " POLYGON ((");
        while (!pt1.isDone()){
            int type = pt1.currentSegment(coords);
            
            sb.append(getPathType(type) + "(" + coords[0] + " " + coords[1] + "),");
            pt1.next();
        }
        final String string = sb.toString();
        sb = new StringBuilder(string.substring(0, string.length()-1));
        sb.append("))\n");
        System.out.println(sb.toString());
    }
    
    private static String getPathType (int pathType){
        switch (pathType) {
        case PathIterator.SEG_CLOSE:
            return "]";
        case PathIterator.SEG_LINETO:
            return " ";
        case PathIterator.SEG_MOVETO:
            return "[";
        case PathIterator.SEG_QUADTO:
            return "QUADTO";
        case PathIterator.SEG_CUBICTO:
            return "CUBICTO";
        }
        return "UNKNOWN";
    }
    
    private static void printRoiShape(final ROIShape rs1, final String title) {
        printShape(rs1.getAsShape(), title);
    }
    
    private static void printGeometry(ROIGeometry g, String title) {
        // System.out.println(title + " " + g.getAsGeometry());
        printShape(new LiteShape(g.getAsGeometry()), title);
    }

}
