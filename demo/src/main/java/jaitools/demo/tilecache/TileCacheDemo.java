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

package jaitools.demo.tilecache;

import jaitools.tilecache.DiskCachedTile;
import jaitools.tilecache.DiskMemTileCache;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

/**
 * Demonstrates basic use of {@code DiskMemTileCache}.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class TileCacheDemo implements Observer {

    private static final int TILE_WIDTH = 128;
    private static final int IMAGE_WIDTH = TILE_WIDTH * 3;
    private static final int IMAGE_HEIGHT = TILE_WIDTH * 2;

    /**
     * Run the example app.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        TileCacheDemo me = new TileCacheDemo();
        me.demo();
    }

    private void demo() {
        /*
         * We set the memory capacity of the cache to be too small for all tiles
         * to be in memory concurrently in order to demonstrate cache actions.
         */
        Map<String, Object> cacheParams = new HashMap<String, Object>();
        cacheParams.put(DiskMemTileCache.KEY_INITIAL_MEMORY_CAPACITY, 1L * 1024 * 1024);

        /*
         * Create a new instance of DiskMemTileCache and add this as an observer
         * so that we are notified about cache actions
         */
        DiskMemTileCache cache = new DiskMemTileCache(cacheParams);
        cache.setDiagnostics(true);
        cache.addObserver(this);

        /*
         * Create rendering hints, specifying the desired tile size and
         * the use of our custom cache
         */
        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);

        Map<RenderingHints.Key, Object> imgParams = new HashMap<RenderingHints.Key, Object>();
        imgParams.put(JAI.KEY_IMAGE_LAYOUT, layout);
        imgParams.put(JAI.KEY_TILE_CACHE, cache);

        RenderingHints hints = new RenderingHints(imgParams);

        /*
         * Create a simple chain of JAI operations that will use the cache.
         * In the first node we create an image with three bands filled with
         * constant values. The second node multiplies the values by a
         * constant.
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)IMAGE_WIDTH);
        pb.setParameter("height", (float)IMAGE_HEIGHT);
        pb.setParameter("bandValues", new Double[]{0d, 1d, 2d});

        RenderedOp op1 = JAI.create("constant", pb, hints);

        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d});

        RenderedOp op2 = JAI.create("MultiplyConst", pb, hints);

        /*
         * Force computation of the image tiles. This will cause the
         * cache to be used. There is only enough memory capacity for
         * two tiles.
         */
        System.out.println("Requesting tiles. Cache has only enough memory capacity");
        System.out.println("for 2 tiles");
        System.out.println();

        Raster[] tiles = op2.getTiles();
        System.out.println(String.format("%d tiles cached; %d resident in memory",
                cache.getNumTiles(), cache.getNumResidentTiles()));

        /*
         * Now we increase memory capacity and repeat the getTiles
         * request
         */
        cache.setMemoryCapacity(5L * 1024 * 1024);

        System.out.println();
        System.out.println("Repeating the tile request after increasing the");
        System.out.println("memory capacity of the cache");
        System.out.println();

        tiles = op2.getTiles();

        System.out.println(String.format("%d tiles cached; %d resident in memory",
                cache.getNumTiles(), cache.getNumResidentTiles()));
    }

    /**
     * This method handles Observer notifications from the cache
     * @param ocache the cache
     * @param otile a cached tile
     */
    public void update(Observable ocache, Object otile) {
        DiskCachedTile tile = (DiskCachedTile)otile;

        StringBuffer sb = new StringBuffer();
        sb.append("Tile at ");
        sb.append(tile.getLocation());
        sb.append(" ");

        int actionValue = tile.getAction();
        sb.append(DiskCachedTile.TileAction.get(actionValue).getDescription());

        System.out.println(sb.toString());
    }
}
