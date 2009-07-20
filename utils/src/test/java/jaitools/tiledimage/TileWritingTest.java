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

package jaitools.tiledimage;

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
