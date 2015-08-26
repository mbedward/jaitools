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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.jai.JAI;
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
        cache.flush();
    }

    @Test
    public void testVisitor() throws InterruptedException{
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

        //This forces Tile access time to be different. This is needed to find the
        //right tiles to dispose within memory. Otherwise fast computers could make
        //this test fail.
        Thread.sleep(1000);
        
        RenderedOp op2 = helper.simpleJAIOp(XTILES, YTILES);
        op2.getTiles();

        /*
         * Visit the cache and check that we get the correct tile stats
         */
        Map<BasicCacheVisitor.Key, Object> filters = new LinkedHashMap<BasicCacheVisitor.Key, Object>();
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
