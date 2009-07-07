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

import jaitools.tilecache.DiskBasedTileCache;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

/**
 * Testing and demonstration of {@linkplain jaitools.tilecache.DiskBasedTileCache}
 *
 * @author Michael Bedward
 */
public class DiskTileCacheDemo {

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 384;
    private static final int TILE_WIDTH = 128;

    public static void main(String[] args) {
        DiskTileCacheDemo me = new DiskTileCacheDemo();
        me.demo();
    }

    private void demo() {
        Map<String, Object> cacheParams = new HashMap<String, Object>();
        cacheParams.put(DiskBasedTileCache.INITIAL_MEMORY_CAPACITY, 100L * 1024 * 1024);
        cacheParams.put(DiskBasedTileCache.MAKE_NEW_TILES_RESIDENT, Boolean.FALSE);
        cacheParams.put(DiskBasedTileCache.USE_MEMORY_THRESHOLD, Boolean.TRUE);

        DiskBasedTileCache cache = new DiskBasedTileCache(cacheParams);

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)IMAGE_WIDTH);
        pb.setParameter("height", (float)IMAGE_HEIGHT);
        pb.setParameter("bandValues", new Double[]{0d, 1d, 2d});

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);

        Map<RenderingHints.Key, Object> imgParams = new HashMap<RenderingHints.Key, Object>();
        imgParams.put(JAI.KEY_IMAGE_LAYOUT, layout);
        imgParams.put(JAI.KEY_TILE_CACHE, cache);

        RenderingHints hints = new RenderingHints(imgParams);
        RenderedOp op = JAI.create("constant", pb, hints);

        // force computation
        Raster tile = op.getTile(1, 1);
    }
}
