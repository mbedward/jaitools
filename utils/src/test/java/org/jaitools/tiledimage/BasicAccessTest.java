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

package org.jaitools.tiledimage;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Unit tests of DiskMemTilesImage: basic tile getting
 * and querying
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class BasicAccessTest extends TiledImageTestBase {

    private static final int TILE_WIDTH = 128;
    private static final int XTILES = 5;
    private static final int YTILES = 3;

    private DiskMemImage image;

    @Before
    public void setUp() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
    }

    @Test
    public void testGetTile() {
        // System.out.println("   getTile");
        for (int y = 0; y < YTILES; y++) {
            for (int x = 0; x < XTILES; x++) {
                Raster tile = image.getTile(x, y);
                assertTrue(tile != null);
                assertTrue(tile.getMinX() == x * TILE_WIDTH);
                assertTrue(tile.getMinY() == y * TILE_WIDTH);
            }
        }
    }

    @Test
    public void testGetWritableTile() {
        // System.out.println("   getting and releasing writable tiles");
        WritableRaster r = image.getWritableTile(1, 1);
        assertTrue(r != null);
        Rectangle bounds = r.getBounds();
        assertTrue(bounds.x == TILE_WIDTH);
        assertTrue(bounds.y == TILE_WIDTH);
    }


    @Test
    public void testIsTileWritable() {
        // System.out.println("   isTileWritable");

        assertFalse(image.isTileWritable(0, 0));

        image.getWritableTile(0, 0);
        assertTrue(image.isTileWritable(0, 0));

        image.releaseWritableTile(0, 0);
        assertFalse(image.isTileWritable(0, 0));
    }


    @Test
    public void testGetWritableTileIndices() {
        // System.out.println("   getWritableTileIndices");
        
        Point[] pi = {new Point(0, 0), new Point(XTILES-1, YTILES-1)};

        for (Point p : pi) {
            image.getWritableTile(p.x, p.y);
        }

        Point[] indices = image.getWritableTileIndices();
        assertTrue(indices.length == pi.length);

        boolean[] found = new boolean[indices.length];
        for (Point index : indices) {
            for (int i = 0; i < pi.length; i++) {
                if (index.equals(pi[i])) {
                    found[i] = true;
                    break;
                }
            }
        }

        for (int i = 0; i < found.length; i++) {
            assertTrue(found[i]);
        }
    }


    @Test
    public void testHasTileWriters() {
        // System.out.println("   hasTileWriters");

        assertFalse(image.hasTileWriters());

        image.getWritableTile(0, 0);
        assertTrue(image.hasTileWriters());

        image.releaseWritableTile(0, 0);
        assertFalse(image.hasTileWriters());
    }

}
