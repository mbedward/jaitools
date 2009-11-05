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

import java.util.HashMap;
import java.util.Map;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the use of {@linkplain DiskMemTileCacheVisitor} to
 * retrieve information about cache contents.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class TileCacheVisitorTest {

    private TileCache origCache;
    private DiskMemTileCache cache;

    @Before
    public void setup() {
        cache = new DiskMemTileCache();
        origCache = JAI.getDefaultInstance().getTileCache();
        JAI.getDefaultInstance().setTileCache(cache);
    }

    @After
    public void tearDown() {
        JAI.getDefaultInstance().setTileCache(origCache);
    }

    @Test
    public void testVisitor(){
        System.out.println("   cache visitor");
        final int XTILES = 4;
        final int YTILES = 4;
        final int EXTRA = 2;
        final int MEM_TILES = XTILES * YTILES + EXTRA;

        TileCacheTestHelper helper = new TileCacheTestHelper();

        /*
         * Set cache memory capacity for a limited number of tiles and
         * set the memory threshold such that one tile's worth of space
         * is freed each time the cache swaps tiles to disk
         */
        cache.setMemoryCapacity(helper.getTileMemSize() * MEM_TILES);
        cache.setMemoryThreshold(1.0f - 1.0f / MEM_TILES); // clears one tile from memory

        /*
         * Create s simple rendering chain and use the getNewRendering
         * method to force computation and tile caching. The output
         * images have more tiles than the cache can hold in memory
         * at once.
         */
        RenderedOp op1 = helper.simpleJAIOp(XTILES, YTILES);
        op1.getTiles();

        RenderedOp op2 = helper.simpleJAIOp(XTILES, YTILES);
        op2.getTiles();

        /*
         * Visit the cache and check that we get the correct tile stats
         */
        Map<BasicCacheVisitor.Key, Object> filters = new HashMap<BasicCacheVisitor.Key, Object>();
        filters.put(BasicCacheVisitor.Key.OWNER, op1.getCurrentRendering());
        filters.put(BasicCacheVisitor.Key.RESIDENT, Boolean.TRUE);

        BasicCacheVisitor visitor = new BasicCacheVisitor();
        visitor.setFilters(filters);
        cache.accept(visitor);
        assertTrue(visitor.getTiles().size() == EXTRA);

        visitor.clear();
        filters.put(BasicCacheVisitor.Key.OWNER, op2.getCurrentRendering());
        visitor.setFilters(filters);
        cache.accept(visitor);
        assertTrue(visitor.getTiles().size() == XTILES * YTILES);

        visitor.setFilter(BasicCacheVisitor.Key.RESIDENT, Boolean.TRUE);
        visitor.clear();
        cache.accept(visitor);
        assertTrue(visitor.getTiles().size() == MEM_TILES);
    }

}
