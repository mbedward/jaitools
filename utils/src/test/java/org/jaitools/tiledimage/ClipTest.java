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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.media.jai.JAI;

import org.jaitools.testutils.WaitingImageFrame;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A test to verify that clipping works correctly.
 *
 * @author Akos Maroy, akos@maroy.hu
 */
@Ignore("Image comparison test needs to be looser or use perceptualdiff")
public class ClipTest extends TiledImageTestBase {
    
    private final int tileWidth = 600;
    private final int tileHeight = 600;
    private final int xTiles = 1;
    private final int yTiles = 1;
    
    // Set this to true to display the test image on screen
    private static final boolean SHOW_IMAGE = true;
    
    // Set this to true to save the test images as tiff files to the tmp dir
    private static final boolean SAVE_IMAGE = true;
    
    // Check that we are not on the build server or some other headless environment
    private static final boolean HEADLESS =
            GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
    
    
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

        AffineTransform t = new AffineTransform(transform);
        t.invert();
        s = t.createTransformedShape(s);

        assertTrue(s.getBounds().intersects(grr.getClip().getBounds2D()));

        grr.setPaint(Color.BLUE);
        grr.draw(s);

        grr.dispose();
        gr.dispose();
    }
    
}
