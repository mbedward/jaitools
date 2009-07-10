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
 * Testing and demonstration of {@linkplain jaitools.tilecache.DiskMemTileCache}
 *
 * @author Michael Bedward
 */
public class TileCacheDemo implements Observer {

    private static final int TILE_WIDTH = 128;
    private static final int IMAGE_WIDTH = TILE_WIDTH * 3;
    private static final int IMAGE_HEIGHT = TILE_WIDTH * 2;

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
        cacheParams.put(DiskMemTileCache.KEY_NEW_TILES_RESIDENT, DiskMemTileCache.VALUE_NEW_TILES_RESIDENT_ALWAYS);

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
