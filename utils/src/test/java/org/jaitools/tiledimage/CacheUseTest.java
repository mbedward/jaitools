/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

import org.jaitools.tilecache.DiskMemTileCache;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the use of a common tile cache by DiskMemImages
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class CacheUseTest extends TiledImageTestBase {

    /**
     * Basic test of a single image using the common cache
     */
    @Test
    public void testCommonCache() {
        // System.out.println("   using common cache");

        DiskMemTileCache common = DiskMemImage.getCommonTileCache();

        DiskMemImage image = makeImage(128, 2, 2);
        image.getTiles();
        assertFalse(image.isUsingCommonCache());
        assertFalse(image.getTileCache() == common);

        image.setUseCommonCache(true);
        assertTrue(image.isUsingCommonCache());
        assertTrue(image.getTileCache() == common);

        assertTrue(common.getTiles(image).length == image.getNumXTiles() * image.getNumYTiles());
    }

    /**
     * Tests an image first using its own cache, then swapping to the
     * common cache, then swapping back to its own cache
     */
    @Test
    public void testSwapCache() {
        // System.out.println("   swapping tile cache");

        DiskMemTileCache common = DiskMemImage.getCommonTileCache();

        DiskMemImage image = makeImage(128, 2, 2);
        image.getTiles();
        assertTrue(image.getTileCache().getTiles(image).length == image.getNumXTiles() * image.getNumYTiles());

        DiskMemTileCache imageCache = image.getTileCache();
        image.setUseCommonCache(true);
        assertTrue(image.isUsingCommonCache());
        assertTrue(image.getTileCache() == common);
        assertTrue(common.getTiles(image).length == image.getNumXTiles() * image.getNumYTiles());
        assertTrue(imageCache.getTiles(image).length == 0);

        image.setUseCommonCache(false);
        assertFalse(image.isUsingCommonCache());
        assertFalse(image.getTileCache() == common);
        assertTrue(image.getTileCache().getTiles(image).length == image.getNumXTiles() * image.getNumYTiles());
        assertTrue(common.getTiles(image).length == 0);
    }

    /**
     * Test two images sharing the common tile cache
     */
    @Test
    public void testShareCache() {
        // System.out.println("   tile cache shared between images");

        DiskMemTileCache common = DiskMemImage.getCommonTileCache();

        // flush any tiles from previous tests
        common.flush();
        
        DiskMemImage image1 = makeImage(128, 2, 1);
        image1.setUseCommonCache(true);

        DiskMemImage image2 = makeImage(128, 3, 1);
        image2.setUseCommonCache(true);

        image1.getTiles();
        image2.getTiles();

        assertTrue(common.getNumTiles() == image1.getNumXTiles() * image1.getNumYTiles() +
                image2.getNumXTiles() * image2.getNumYTiles());

        assertTrue(common.getTiles(image1).length == image1.getNumXTiles() * image1.getNumYTiles());
        assertTrue(common.getTiles(image2).length == image2.getNumXTiles() * image2.getNumYTiles());
    }
}
