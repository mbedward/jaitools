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

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests of DiskMemTilesImage: writing and retrieving data
 * at the tile level
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class TileWritingTest extends TiledImageTestBase {

    private static final int TILE_WIDTH = 128;
    private static final int XTILES = 5;
    private static final int YTILES = 3;

    private DiskMemImage image;

    @Before
    public void setUp() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
    }

    @Test
    public void testTileWriting(){
        System.out.println("   read/write with individual tiles");

        int numBands = image.getNumBands();
        int[] data = new int[numBands];
        for (int i = 0; i < numBands; i++) {
            data[i] = i+1;
        }

        for (int y = image.getMinTileY(); y < image.getMaxTileY(); y++) {
            int py = TILE_WIDTH * y;
            for (int x = image.getMinTileX(); x < image.getMaxTileX(); x++) {
                int px = TILE_WIDTH * x;
                WritableRaster tile = image.getWritableTile(x, y);
                tile.setPixel(px, py, data);
                image.releaseWritableTile(x, y);
            }
        }

        int[] tileData = new int[numBands];
        for (int y = image.getMinTileY(); y < image.getMaxTileY(); y++) {
            int py = TILE_WIDTH * y;
            for (int x = image.getMinTileX(); x < image.getMaxTileX(); x++) {
                int px = TILE_WIDTH * x;
                Raster tile = image.getTile(x, y);
                tile.getPixel(px, py, tileData);

                for (int i = 0; i < numBands; i++) {
                    assertTrue(tileData[i] == data[i]);
                }
            }
        }

    }

}
