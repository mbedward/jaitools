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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.jaitools.testutils.WaitingImageFrame;
import org.jaitools.tilecache.DiskMemTileCache;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class DrawingIntoDiskCachedTilesTest extends TiledImageTestBase {
    
    private final int tileWidth = 128;
    private final int tileHeight = 128;
    private final int xTiles = 2;
    private final int yTiles = 2;
    
    // Set this to true to display the test image on screen
    private final boolean SHOW_IMAGE = false;
    
    @Test
    public void test() throws Exception {
        ColorModel cm = ColorModel.getRGBdefault();
        int bitsPerPixel = cm.getPixelSize();
        
        long tileMemorySize = tileWidth * tileHeight * bitsPerPixel / 8;
        
        // Cache with memory capacity for just oen tile
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DiskMemTileCache.KEY_INITIAL_MEMORY_CAPACITY, tileMemorySize);
        DiskMemTileCache cache = new DiskMemTileCache(params);

        DiskMemImage.setCommonTileCache(cache);
        
        SampleModel sm = cm.createCompatibleSampleModel(tileWidth, tileWidth);
        DiskMemImage image = new DiskMemImage(0, 0, tileWidth * xTiles, tileWidth * yTiles, 0, 0, sm, cm);
        image.setUseCommonCache(true);
        
        // Draw into each image tile. This will cause all but the last
        // tile to be flushed to disk
        Graphics2D gr = image.createGraphics();
        gr.setPaint(Color.BLUE);
        gr.setStroke(new BasicStroke(5.0f));
        
        for (int tileY = 0; tileY < yTiles; tileY++) {
            for (int tileX = 0; tileX < xTiles; tileX++) {
                gr.drawRect(5 + tileX * tileWidth,
                            5 + tileY * tileHeight, 
                            tileWidth - 10, tileHeight - 10);
            }
        }

        // Draw into the tiles again. This means that all tiles will
        // now have been flushed to, and restored from, disk
        gr.setPaint(Color.RED);
        for (int tileY = 0; tileY < yTiles; tileY++) {
            for (int tileX = 0; tileX < xTiles; tileX++) {
                gr.drawRect(20 + tileX * tileWidth,
                            20 + tileY * tileHeight, 
                            tileWidth - 40, tileHeight - 40);
            }
        }
        gr.dispose();
        
        // Draw into a reference tile
        BufferedImage refImg = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
        gr = refImg.createGraphics();
        gr.setStroke(new BasicStroke(5.0f));
        gr.setPaint(Color.BLUE);
        gr.drawRect(5, 5, tileWidth - 10, tileHeight - 10);
        gr.setPaint(Color.RED);
        gr.drawRect(20, 20, tileWidth - 40, tileHeight - 40);
        
        // Test for matching pixels between image tiles and reference tile
        Raster refTile = refImg.getTile(0, 0);
        for (int tileY = 0; tileY < yTiles; tileY++) {
            for (int tileX = 0; tileX < xTiles; tileX++) {
                Raster tile = image.getTile(tileX, tileY);
                for (int y = 0; y < tileHeight; y++) {
                    for (int x = 0; x < tileWidth; x++) {
                        for (int band = 0; band < image.getNumBands(); band++) {
                            assertEquals(refTile.getSample(x, y, band),
                                    tile.getSample(x + tile.getMinX(), y + tile.getMinY(), band));
                        }
                    }
                }
            }
        }
        
        if (SHOW_IMAGE) {
            CountDownLatch latch = new CountDownLatch(1);
            WaitingImageFrame.showImage(image, "Test image", latch);
            latch.await();
        }
    }

}
