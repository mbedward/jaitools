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

import jaitools.utils.CollectionFactory;
import java.awt.RenderingHints;
import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class TileCacheTest implements Observer {

    private static TileCache origCache;
    private static DiskMemTileCache cache;

    private static final int TILE_WIDTH = 128;
    private static RenderingHints testHints;

    private static final float FLOAT_TOL = 0.0001F;

    private List<DiskCachedTile> tiles = CollectionFactory.newList();
    private List<DiskCachedTile> residentTiles = CollectionFactory.newList();

    @BeforeClass
    public static void commonSetup() {
        JAI inst = JAI.getDefaultInstance();
        origCache = inst.getTileCache();
        cache = new DiskMemTileCache();
        inst.setTileCache(cache);

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);

        testHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }

    @AfterClass
    public static void commonTearDown() {
        JAI.getDefaultInstance().setTileCache(origCache);
    }

    @After
    public void afterTests() {
        cache.flush();
        cache.setMemoryCapacity(DiskMemTileCache.DEFAULT_MEMORY_CAPACITY);
    }

    /**
     * Test that JAI has our custom cache set as the default cache
     */
    @Test
    public void testJAIHasCache() {
        System.out.println("   testing JAI set with DiskMemTileCache");
        TileCache jaicache = JAI.getDefaultInstance().getTileCache();
        assertTrue(jaicache instanceof DiskMemTileCache);
    }

    /**
     * Test that the cache was set with the default parameters
     * correctly
     */
    @Test
    public void testDefaultParams() {
        System.out.println("   testing default cache params");

        assertTrue(cache.getMemoryCapacity() == DiskMemTileCache.DEFAULT_MEMORY_CAPACITY);
        assertTrue(cache.getMemoryThreshold() - DiskMemTileCache.DEFAULT_MEMORY_THRESHOLD < FLOAT_TOL);
    }


    /**
     * Test that the cache is used correctly in a simple JAI operation
     */
    @Test
    public void testCacheUsed() {
        System.out.println("   testing cache use in simple op");

        /*
         * Create a rendering chain for an output image 3 tiles x 2 tiles
         */
        RenderedOp op = simpleJAIOp(3, 2);

        /*
         * Reset the cache's memory capacity for resident tiles so
         * that it is enough for 3 tiles only
         */
        cache.setMemoryCapacity((long)TILE_WIDTH * TILE_WIDTH * (Double.SIZE / 8) * 3);

        /*
         * Force computation of tiles. This will cause the cache to
         * be used
         */
        op.getTiles();

        /*
         * Test that the cache has all tiles but that only 3 are
         * resident in memory
         */
        assertTrue(cache.getNumTiles() == 6);
        assertTrue(cache.getNumResidentTiles() == 3);
    }

    /**
     * Test the caches ability to swap tiles in and out of limited memory
     */
    @Test
    public void testMemorySwapping() {
        System.out.println("   testing swapping tiles into memory");

        /*
         * Create a rendering chain for an output image 3 tiles x 2 tiles
         */
        RenderedOp op = simpleJAIOp(3, 2);

        /*
         * Reset the cache's memory capacity for resident tiles so
         * that it is enough for 3 tiles only
         */
        cache.setMemoryCapacity((long)TILE_WIDTH * TILE_WIDTH * (Double.SIZE / 8) * 3);

        /*
         * Register ourselves as an observer
         */
        cache.addObserver(this);
        cache.setDiagnostics(true);

        /*
         * Force computation of tiles. This will cause the cache to
         * be used. The first three tiles will fit into available
         * cache memory while the remaining three will only be in
         * the cache's disk storage.
         */
        op.getTiles();

        /*
         * Request the non-resident tiles to force memory swapping
         */
        boolean[] resident = new boolean[tiles.size()];
        int k = 0;
        for (DiskCachedTile tile : tiles) {
            resident[k++] = residentTiles.contains(tile);
        }

        k = 0;
        for (DiskCachedTile tile : tiles) {
            if (!resident[k++]) {
                int x = tile.getTileX ();
                int y = tile.getTileY();
                op.getTile(x, y);
                assertTrue(residentTiles.contains(tile));
            }
        }

        cache.deleteObserver(this);
        cache.setDiagnostics(false);
    }

    /**
     * Test removing tiles for a given image
     */
    @Test
    public void removeTilesForImage() {
        System.out.println("   testing removal of tiles for an image");

        /*
         * Register ourselves as an observer
         */
        cache.addObserver(this);
        cache.setDiagnostics(true);

        /*
         * Create an image and use getTiles to force the tiles to be cached
         */
        RenderedOp op1 = simpleJAIOp(2, 2);
        op1.getTiles();

        /*
         * Repeat for a second image
         */
        RenderedOp op2 = simpleJAIOp(2, 2);
        op2.getTiles();

        /*
         * Remove tiles for the first image and check
         * that only tiles for the second image remain
         */
        cache.removeTiles(op1.getCurrentRendering());
        assert(cache.getNumTiles() == 4);
        for (DiskCachedTile tile : tiles) {
            assertTrue(tile.getOwner() == op2.getCurrentRendering());
        }

        cache.deleteObserver(this);
        cache.setDiagnostics(false);
    }

    /**
     * Test flushing the cache
     */
    @Test
    public void testFlush() {
        System.out.println("   testing cache flush and flushMemory");

        /*
         * Create an image and use getTiles to force the tiles to be cached
         */
        RenderedOp op = simpleJAIOp(2, 2);
        op.getTiles();

        assertTrue(cache.getNumTiles() == 4);

        cache.flushMemory();
        assertTrue(cache.getNumTiles() == 4);
        assertTrue(cache.getNumResidentTiles() == 0);

        cache.flush();
        assertTrue(cache.getNumTiles() == 0);
    }

    /**
     * Test that cache files are deleted when the associated tiles
     * are removed from the cache
     */
    @Test
    public void testFileHandling() {
        System.out.println("   testing cache file handling");

        cache.addObserver(this);
        cache.setDiagnostics(true);

        RenderedOp op = simpleJAIOp(10, 10);
        File[] files = new File[tiles.size()];
        for (File f : files) {
            assertTrue(f.exists());
            assertTrue(f.canRead());
        }

        cache.flush();

        for (File f : files) {
            assertFalse(f.exists());
        }

        cache.deleteObserver(this);
        cache.setDiagnostics(false);
    }


    /**
     * Creates a simple JAI rendering chain for a single band image
     * that will require use of the cache
     *
     * @param numXTiles image width as number of tiles
     * @param numYTiles image height as number of tiles
     * @return a new RenderedOp instance
     */
    private RenderedOp simpleJAIOp(int numXTiles, int numYTiles) {
        /*
         * First node in a simple rendering chain: create
         * a constant image, 3 tiles x 2 tiles
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("Constant");
        pb.setParameter("bandValues", new Double[]{1.0d});
        pb.setParameter("width", (float)TILE_WIDTH * numXTiles);
        pb.setParameter("height", (float)TILE_WIDTH * numYTiles);
        RenderedOp op1 = JAI.create("constant", pb, testHints);

        /*
         * Second node: multiply the constant image by a constant
         */
        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d});
        RenderedOp op2 = JAI.create("MultiplyConst", pb, testHints);

        return op2;
    }

    /**
     * Observer method to receive cache events
     * @param ocache the tile cache
     * @param otile a cached tile
     */
    public void update(Observable ocache, Object otile) {
        DiskCachedTile tile = (DiskCachedTile)otile;

        int actionValue = tile.getAction();
        switch (DiskCachedTile.TileAction.get(actionValue)) {
            case ACTION_ACCESSED:
                break;

            case ACTION_ADDED:
                tiles.add(tile);
                break;

            case ACTION_ADDED_RESIDENT:
                tiles.add(tile);
                residentTiles.add(tile);
                break;

            case ACTION_RESIDENT:
                residentTiles.add(tile);
                break;

            case ACTION_NON_RESIDENT:
                residentTiles.remove(tile);
                break;

            case ACTION_REMOVED:
                tiles.remove(tile);
                residentTiles.remove(tile);
                break;
        }
    }
}
