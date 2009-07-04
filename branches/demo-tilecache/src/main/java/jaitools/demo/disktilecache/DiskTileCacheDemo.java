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
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.JAI;

/**
 * Testing and demonstration of {@linkplain jaitools.tilecache.DiskBasedTileCache}
 *
 * @author Michael Bedward
 */
public class DiskTileCacheDemo {

    public static void main(String[] args) {
        DiskTileCacheDemo me = new DiskTileCacheDemo();
        me.demo();
    }

    private void demo() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DiskBasedTileCache.INITIAL_MEMORY_CAPACITY, 100L * 1024 * 1024);
        params.put(DiskBasedTileCache.MAKE_NEW_TILES_RESIDENT, Boolean.FALSE);
        params.put(DiskBasedTileCache.USE_MEMORY_THRESHOLD, Boolean.TRUE);

        DiskBasedTileCache cache = new DiskBasedTileCache(params);
        JAI.getDefaultInstance().setTileCache(cache);
    }
}
