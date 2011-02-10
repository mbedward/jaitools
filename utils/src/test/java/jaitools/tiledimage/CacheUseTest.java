/*
 * Copyright 2009 Michael Bedward
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

package jaitools.tiledimage;

import jaitools.tilecache.DiskMemTileCache;
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
        System.out.println("   using common cache");

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
        System.out.println("   swapping tile cache");

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
        System.out.println("   tile cache shared between images");

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
