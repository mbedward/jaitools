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

package jaitools.tilecache;

import java.awt.RenderingHints;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class TileCacheTest {

    private static TileCache origCache;

    private static final int TILE_WIDTH = 128;
    private static RenderingHints testHints;

    private static final float FLOAT_TOL = 0.0001F;

    @Before
    public void beforeTests() {
        JAI inst = JAI.getDefaultInstance();
        origCache = inst.getTileCache();
        TileCache diskMemCache = new DiskMemTileCache();
        inst.setTileCache(diskMemCache);

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);

        testHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }

    /**
     * Test that JAI has our custom cache set as the default cache
     */
    @Test
    public void testJAIHasCache() {
        System.out.println("   JAI set with DiskMemTileCache");
        TileCache cache = JAI.getDefaultInstance().getTileCache();
        assertTrue(cache instanceof DiskMemTileCache);
    }

    /**
     * Test that the cache was set with the default parameters
     * correctly
     */
    @Test
    public void testDefaultParams() {
        System.out.println("   default params");
        TileCache cache = JAI.getDefaultInstance().getTileCache();

        assertTrue(cache.getMemoryCapacity() == DiskMemTileCache.DEFAULT_MEMORY_CAPACITY);
        assertTrue(cache.getMemoryThreshold() - DiskMemTileCache.DEFAULT_MEMORY_THRESHOLD < FLOAT_TOL);
    }


    /**
     * Test that the cache is used correctly in a simple JAI operation
     */
    @Test
    public void testCacheUsed() {
        System.out.println("   cache use in simple op");
        DiskMemTileCache cache = (DiskMemTileCache) JAI.getDefaultInstance().getTileCache();

        /*
         * First node in a simple rendering chain: create
         * a constant image, 3 tiles x 2 tiles
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("Constant");
        pb.setParameter("bandValues", new Double[]{1.0d, 2.0d});
        pb.setParameter("width", (float)TILE_WIDTH * 3);
        pb.setParameter("height", (float)TILE_WIDTH * 2);
        RenderedOp op1 = JAI.create("constant", pb, testHints);

        /*
         * Second node: multiply the constant image by a constant
         */
        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d, 3.0d});
        RenderedOp op2 = JAI.create("MultiplyConst", pb, testHints);

        /*
         * Reset the cache's memory capacity for resident tiles so
         * that it is enough for 3 tiles only
         */
        cache.setMemoryCapacity((long)TILE_WIDTH * TILE_WIDTH * (Double.SIZE / 8) * 3 * 2);

        /*
         * Force computation of tiles. This will cause the cache to
         * be used
         */
        op2.getTiles();

        /*
         * Test that the cache has all tiles but that only 3 are
         * resident in memory
         */
        assertTrue(cache.getNumTiles() == 6);
        assertTrue(cache.getNumResidentTiles() == 3);
    }

    @Test
    /**
     * Test that the cache is empty now that the image created in the
     * previous test is no longer in scope
     */
    public void testCacheEmptied() {
        System.out.println("   cache emptied correctly");

        DiskMemTileCache cache = (DiskMemTileCache) JAI.getDefaultInstance().getTileCache();
        assertTrue(cache.getNumTiles() == 0);
    }
}
