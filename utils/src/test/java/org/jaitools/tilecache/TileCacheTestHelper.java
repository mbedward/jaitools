/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.tilecache;

import java.awt.RenderingHints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.jaitools.CollectionFactory;

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
