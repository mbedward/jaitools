package jaitools.media.jai.vectorbinarize;

import static junit.framework.Assert.*;
import jaitools.imageutils.ROIGeometry;

import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.SubtractDescriptor;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class ROIGeometryTest {
    
    // REMEMBER TO SET THIS ONE TO FALSE BEFORE COMMITTING!
    static final boolean INTERACTIVE = false;

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
    @Ignore
    public void testCircle() throws Exception {
        Point p = new GeometryFactory().createPoint(new Coordinate(10, 10)); 
        Geometry buffer = p.buffer(15);
        
        ROIGeometry g = new ROIGeometry(buffer);
        ROIShape shape = getEquivalentROIShape(g);
        
        assertROIEquivalent(g, shape, "Circle");

    }

    @Test
    @Ignore
    public void testUnion() throws Exception {
        Point p1 = new GeometryFactory().createPoint(new Coordinate(10, 10)); 
        Point p2 = new GeometryFactory().createPoint(new Coordinate(20, 10));
        Geometry buffer1 = p1.buffer(15);
        Geometry buffer2 = p2.buffer(15);
        
        ROIGeometry rg1 = new ROIGeometry(buffer1);
        ROIGeometry rg2 = new ROIGeometry(buffer2);
        ROI rgUnion = rg1.add(rg2);
        
        ROIShape rs1 = getEquivalentROIShape(rg1);
        ROIShape rs2 = getEquivalentROIShape(rg2);
        ROI rsUnion = rs1.add(rs2);

        assertROIEquivalent(rgUnion, rsUnion, "Union");
    }
    
    @Test
    @Ignore
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
    @Ignore
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
    @Ignore
    public void testXor() throws Exception {
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
        RenderedImage firstImage = first.getAsImage().getAsBufferedImage();
        BufferedImage secondImage = second.getAsImage().getAsBufferedImage();
        visualize(firstImage, secondImage, title);
        
        assertImagesEqual(firstImage, secondImage);
    }
    
    void assertImagesEqual(final RenderedImage image1, final RenderedImage image2) {
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
     * Shows the two images in the 
     * @param ri1
     * @param ri2
     * @param title
     */
    void visualize(final RenderedImage ri1, final RenderedImage ri2, String title) throws IOException {
        
        if(INTERACTIVE) {
            final Thread mainThread = Thread.currentThread();
            
            final JFrame frame = new JFrame(title + " - Close the window to continue");
            frame.getContentPane().setLayout(new GridLayout(1, 2));
            frame.getContentPane().add(new ScrollingImagePanel(ri1, 200, 200));
            frame.getContentPane().add(new ScrollingImagePanel(ri2, 200, 200));
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
                    frame.setSize(400, 200);
                    frame.setVisible(true);
                }
            });
            
            
            try {
                mainThread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // move on
            }
        }
    }
    
    
}
