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

import java.util.HashSet;
import java.util.Set;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;
import org.junit.After;
import org.junit.Before;
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
        final int MEM_TILES = 10;
        final int XTILES = 3;
        final int YTILES = 3;

        TileCacheTestHelper helper = new TileCacheTestHelper();

        /**
         * Set cache memory capacity for a limited number of tiles
         */
        cache.setMemoryCapacity(helper.getTileMemSize() * MEM_TILES);

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
        BasicCacheVisitor visitor = new BasicCacheVisitor();
        visitor.setFilter(BasicCacheVisitor.Key.OWNER, op1.getCurrentRendering());
        cache.accept(visitor);

        assertTrue(visitor.getTiles().size() == XTILES * YTILES);

        visitor.setFilter(BasicCacheVisitor.Key.RESIDENT, Boolean.TRUE);
        visitor.clear();
        cache.accept(visitor);

        assertTrue(visitor.getTiles().size() == MEM_TILES);
    }

}
