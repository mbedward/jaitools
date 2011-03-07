/*
 * Copyright 2009-2011 Michael Bedward
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import jaitools.CollectionFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Support class for unit tests in the jaitools.tilecache package
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
class TileCacheTestHelper implements Observer {

    private static final int TILE_WIDTH = 128;
    private RenderingHints hints;

    private List<DiskCachedTile> tiles;
    private List<DiskCachedTile> residentTiles;
    
    private CountDownLatch updateLatch;

    /**
     * Constructor
     */
    TileCacheTestHelper() {
        tiles = CollectionFactory.list();
        residentTiles = CollectionFactory.list();

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);
        hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }

    /**
     * Get the memory required to store a tile's data
     * @return tile data size in bytes
     */
    long getTileMemSize() {
        return (long)TILE_WIDTH * TILE_WIDTH * (Double.SIZE / 8);
    }

    /**
     * Begin observing a cache
     */
    void startObserving(DiskMemTileCache cache) {
        cache.addObserver(this);
        cache.setDiagnostics(true);
    }

    /**
     * Stop observing a cache
     */
    void stopObserving(DiskMemTileCache cache) {
        cache.deleteObserver(this);
        cache.setDiagnostics(false);
        tiles.clear();
        residentTiles.clear();
    }

    /**
     * Creates a simple JAI rendering chain for a single band image
     * that will require use of the cache
     *
     * @param numXTiles image width as number of tiles
     * @param numYTiles image height as number of tiles
     * @return a new RenderedOp instance
     */
    RenderedOp simpleJAIOp(int numXTiles, int numYTiles) {
        /*
         * First node in a simple rendering chain: create
         * a constant image, 3 tiles x 2 tiles
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("Constant");
        pb.setParameter("bandValues", new Double[]{1.0d});
        pb.setParameter("width", (float)TILE_WIDTH * numXTiles);
        pb.setParameter("height", (float)TILE_WIDTH * numYTiles);
        RenderedOp op1 = JAI.create("constant", pb, hints);

        /*
         * Second node: multiply the constant image by a constant
         */
        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d});
        RenderedOp op2 = JAI.create("MultiplyConst", pb, hints);

        return op2;
    }

    Collection<DiskCachedTile> getTiles() {
        return Collections.unmodifiableCollection(tiles);
    }

    Collection<DiskCachedTile> getResidentTiles() {
        return Collections.unmodifiableCollection(residentTiles);
    }

    /**
     * Observer method to receive cache events
     * @param ocache the tile cache
     * @param otile a cached tile
     */
    public void update(Observable ocache, Object otile) {
        if (updateLatch != null) {
            updateLatch.countDown();
        }
        
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
    
    boolean waitForUpdate(int numUpdates, long maxTime, TimeUnit units) {
        if (updateLatch != null && updateLatch.getCount() > 0) {
            throw new RuntimeException("Must not call this method when latch is already set");
        }
        
        updateLatch = new CountDownLatch(numUpdates);
        try {
            return updateLatch.await(maxTime, units);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
