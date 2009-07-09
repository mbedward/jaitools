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

package jaitools.demo.disktilecache;

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

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 384;
    private static final int TILE_WIDTH = 128;

    public static void main(String[] args) {
        TileCacheDemo me = new TileCacheDemo();
        me.demo();
    }

    private void demo() {
        Map<String, Object> cacheParams = new HashMap<String, Object>();
        cacheParams.put(DiskMemTileCache.KEY_INITIAL_MEMORY_CAPACITY, 100L * 1024 * 1024);
        cacheParams.put(DiskMemTileCache.KEY_NEW_TILES_RESIDENT, DiskMemTileCache.VALUE_NEW_TILES_RESIDENT_TRY);

        DiskMemTileCache cache = new DiskMemTileCache(cacheParams);
        cache.addObserver(this);

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);

        Map<RenderingHints.Key, Object> imgParams = new HashMap<RenderingHints.Key, Object>();
        imgParams.put(JAI.KEY_IMAGE_LAYOUT, layout);
        imgParams.put(JAI.KEY_TILE_CACHE, cache);

        RenderingHints hints = new RenderingHints(imgParams);

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)IMAGE_WIDTH);
        pb.setParameter("height", (float)IMAGE_HEIGHT);
        pb.setParameter("bandValues", new Double[]{0d, 1d, 2d});

        RenderedOp op1 = JAI.create("constant", pb, hints);

        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d});

        RenderedOp op2 = JAI.create("MultiplyConst", pb, hints);

        // force computation
        Raster[] tiles = op2.getTiles();
    }

    public void update(Observable ocache, Object otile) {
        DiskCachedTile tile = (DiskCachedTile)otile;

        StringBuffer sb = new StringBuffer();
        sb.append("Tile at ");
        sb.append(tile.getLocation());

        switch (tile.getAction()) {
            case DiskCachedTile.ACTION_ADDED:
                sb.append(" added to cache");
                break;

            case DiskCachedTile.ACTION_ADDED_RESIDENT:
                sb.append(" added to cache and placed into memory");
                break;

            case DiskCachedTile.ACTION_NON_RESIDENT:
                sb.append(" removed from memory");
                break;

            case DiskCachedTile.ACTION_REMOVED:
                sb.append(" removed from the cache");
                break;

            case DiskCachedTile.ACTION_RESIDENT:
                sb.append(" loaded into memory");
                break;
        }

        System.out.println(sb.toString());
    }
}
