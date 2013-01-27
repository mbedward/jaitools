/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.tiledimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.media.jai.JAI;

import org.jaitools.testutils.WaitingImageFrame;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A test to verify that clipping works correctly.
 *
 * @author Akos Maroy, akos@maroy.hu
 * @author Michel Bedward
 */
public class GraphicsClipTest extends TiledImageTestBase {
    
    private final int tileWidth = 600;
    private final int tileHeight = 600;
    private final int xTiles = 1;
    private final int yTiles = 1;
    
    // Set this to true to display the test image on screen
    private static final boolean SHOW_IMAGE = false;
    
    // Check that we are not on the build server or some other headless environment
    private static final boolean HEADLESS =
            GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
    
    private DiskMemImage image;
    private Graphics2D graphics;
    private Rectangle imageBounds;
    private Rectangle midRect;
    
    @Before
    public void setup() {
        image = makeImage(tileWidth, xTiles, yTiles);
        graphics = image.createGraphics();
        
        imageBounds = image.getBounds();

        // Set up a rectangle in the middle of the image to
        // be used by tests
        midRect = new Rectangle(
                imageBounds.x + imageBounds.width / 4,
                imageBounds.y + imageBounds.height / 4,
                imageBounds.width / 2,
                imageBounds.height / 2);
    }
    
    @Test
    public void graphicsHasNoClipInitially() throws Exception {
        assertNull(graphics.getClip());
    }
    
    @Test
    public void setAndGetClip() {
        graphics.clip(midRect);
        assertEquals(midRect, graphics.getClipBounds());
    }
    
    @Test
    public void clipWithNullArgClearsClipRegion() throws Exception {
        graphics.clip(midRect);
        graphics.clip(null);
        
        assertNull( graphics.getClip() );
        
        // check that it is safe to call getClipBounds
        assertNull( graphics.getClipBounds() );
    }
    
    
    @Test
    public void getClipBoundsWithArg() throws Exception {
        Rectangle r = new Rectangle();
        graphics.clip(midRect);
        graphics.getClipBounds(r);
        assertEquals(midRect, r);
        
        // clear clip region
        graphics.clip(null);
        graphics.getClipBounds(r);
        
        // r should be unchanged
        assertEquals(midRect, r);
    }
    
    @Test
    public void clipWithExistingTransform() throws Exception {
        AffineTransform tr = AffineTransform.getRotateInstance(Math.PI / 4);
        graphics.transform(tr);
        graphics.clip(midRect);
        
        /*
         * getClip should return the untransformed region - however we 
         * allow for some slope because DiskMemImageGraphics is actually
         * returning the inverse-transformed version of its internal user
         * clip (transformed) shape.
         */
        assertBounds(midRect, graphics.getClipBounds(), 2);
    }
    
    @Test
    public void drawWithClip() throws Exception {
        graphics.setPaint(Color.BLACK);
        graphics.fill(imageBounds);
        
        graphics.clip(midRect);
        graphics.setPaint(Color.RED);
        graphics.fill(imageBounds);
        
        showImage("drawWithClip");
        
        // Outside clip region
        Rectangle outer = new Rectangle(midRect);
        outer.grow(5, 5);
        assertColor(Color.BLACK, getCorners(outer));
        
        // Inside clip region
        Rectangle inner = new Rectangle(midRect);
        inner.grow(-5, -5);
        assertColor(Color.RED, getCorners(inner));
    }
    
    @Test
    public void drawWithClipAndTransform() throws Exception {
        graphics.setPaint(Color.BLACK);
        graphics.fill(imageBounds);
        
        AffineTransform tr = AffineTransform.getRotateInstance(
                Math.PI / 4, 
                image.getMinX() + image.getWidth() / 2,
                image.getMinY() + image.getHeight() / 2);
        
        graphics.transform(tr);
        graphics.clip(midRect);
        
        graphics.setPaint(Color.RED);
        graphics.fill(imageBounds);
        
        showImage("drawWithClipAndTransform");
        
        // Outside transformed clip region
        Rectangle outer = new Rectangle(midRect);
        outer.grow(5, 5);
        Point2D[] corners = getCorners(outer);
        Point2D[] trPoints = new Point2D[corners.length];
        tr.transform(corners, 0, trPoints, 0, corners.length);
        assertColor(Color.BLACK, trPoints);
        
        // Inside transformed clip region
        Rectangle inner = new Rectangle(midRect);
        inner.grow(-5, -5);
        corners = getCorners(inner);
        tr.transform(corners, 0, trPoints, 0, corners.length);
        assertColor(Color.RED, trPoints);
    }
    
    private void assertBounds(Rectangle expected, Rectangle observed, int tolerance) {
        assertTrue(Math.abs(expected.x - observed.x) <= tolerance);
        assertTrue(Math.abs(expected.y - observed.y) <= tolerance);
        assertTrue(Math.abs(expected.width - observed.width) <= tolerance);
        assertTrue(Math.abs(expected.height - observed.height) <= tolerance);
    }
    
    private void assertColor(Color c, Point2D ...points) {
        BufferedImage bufImg = image.getAsBufferedImage();
        int expectedRGB = c.getRGB();
        for (Point2D p : points) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            
            assertEquals(
                    String.format("x=%d y=%d", x, y),
                    expectedRGB, bufImg.getRGB(x, y));
        }
    }
    
    private Point2D[] getCorners(Rectangle r) {
        return new Point[] {
            new Point(r.x, r.y),
            new Point(r.x, r.y + r.height),
            new Point(r.x + r.width, r.y),
            new Point(r.x + r.width, r.y + r.height)
        };
    }
    
    private void showImage(String title) throws Exception {
        if (!HEADLESS && SHOW_IMAGE) {
            CountDownLatch latch = new CountDownLatch(1);
            WaitingImageFrame.showImage(image, title, latch);
            latch.await();
        }
    }
    
    
    // Set this to true to save the test images as tiff files to the tmp dir
    private static final boolean SAVE_IMAGE = true;

    @Ignore("original test - refactor if we keep it and allow for platform differences")
    @Test
    public void test() throws Exception {
        DiskMemImage dmi = makeImage(tileWidth, xTiles, yTiles);
        BufferedImage bi = new BufferedImage(tileWidth, tileHeight,
                                             BufferedImage.TYPE_INT_ARGB);

        draw(dmi.createGraphics());
        draw(bi.createGraphics());
        

        if (!HEADLESS && SAVE_IMAGE) {
            String dmiPath = File.createTempFile("diskmem", "tif").getPath();
            JAI.create("filestore", dmi, dmiPath, "TIFF", null);
            
            String bmPath = File.createTempFile("buffered", "tif").getPath();
            JAI.create("filestore", bi, bmPath, "TIFF", null);
            
            System.out.println("Saved test image files to:");
            System.out.println(dmiPath);
            System.out.println(bmPath);
        }

        if (!HEADLESS && SHOW_IMAGE) {
            CountDownLatch latch = new CountDownLatch(1);
            WaitingImageFrame.showImage(dmi, "Close window to continue test", latch);
            latch.await();
        }

        /*
        // Test for matching pixels between image tiles and reference tile
        Raster refTile = bi.getData();
        Raster tile    = dmi.getTile(0, 0);
        assertEquals(tile.getWidth(), refTile.getWidth());
        assertEquals(tile.getHeight(), refTile.getHeight());
        assertEquals(tile.getNumBands(), refTile.getNumBands());
        for (int y = 0; y < tileHeight; y++) {
            for (int x = 0; x < tileWidth; x++) {
                for (int band = 0; band < dmi.getNumBands(); band++) {
                    assertEquals(refTile.getSample(x, y, band),
                            tile.getSample(x + tile.getMinX(), y + tile.getMinY(), band));
                }
            }
        }
        */
        
    }

    /**
     * Draw some graphics into an Graphics2D object.
     *
     * @param image the image to draw into
     * @throws NoninvertibleTransformException in transform errors.
     */
    private void draw(Graphics2D gr) throws NoninvertibleTransformException {
        gr.setPaint(Color.WHITE);
        gr.fill(new Rectangle(0, 0, tileWidth, tileHeight));

        // AffineTransform[[0.318755336305853, 0.0, 420.03106689453125],
        //                 [0.0, 0.318755336305853, 245.5029296875]]
        AffineTransform transform = new AffineTransform(
                    0.318755336305853, 0.0, 0.0,
                    0.318755336305853, 420.03106689453125, 245.5029296875);
        gr.setTransform(transform);

        Shape s = new Rectangle(0, 0, 96, 83);

        // create an enbedded graphics
        Graphics2D grr = (Graphics2D) gr.create();
        // AffineTransform[[1.0, 0.0, -343.9285583496093],
        //                 [0.0, 1.0, -502.5158386230469]]
        grr.clip(s.getBounds());
        transform = new AffineTransform(
                    1.0, 0.0, 0.0,
                    1.0, -343.9285583496093, -502.5158386230469);
        grr.transform(transform);

        AffineTransform t = transform.createInverse();
        s = t.createTransformedShape(s);

        assertTrue(s.getBounds().intersects(grr.getClip().getBounds2D()));

        grr.setPaint(Color.BLUE);
        grr.draw(s);

        grr.dispose();
        gr.dispose();
    }

}
