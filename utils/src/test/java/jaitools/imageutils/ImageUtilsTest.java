/*
 * Copyright 2011 Michael Bedward
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

package jaitools.imageutils;

import java.awt.Dimension;
import javax.media.jai.JAI;
import javax.media.jai.TiledImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ImageUtils helper class.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ImageUtilsTest {

    @Test
    public void createConstantImage1() {
        System.out.println("   createConstantImage(w, h, value)");

        Dimension tileSize = JAI.getDefaultTileSize();
        int w = (int)(2.5 * tileSize.width);
        int h = (int)(2.5 * tileSize.height);
        TiledImage img = ImageUtils.createConstantImage(w, h, 0d);

        assertNotNull(img);
        assertEquals(w, img.getWidth());
        assertEquals(h, img.getHeight());
        assertEquals(tileSize.width, img.getTileWidth());
        assertEquals(tileSize.height, img.getTileHeight());
    }

    @Test
    public void createConstantImage2() {
        System.out.println("   createConstantImage - all args");
        Dimension defTileSize = JAI.getDefaultTileSize();
        int tileW = defTileSize.width / 2;
        int tileH = defTileSize.height / 2;
        int w = (int) (tileW * 2.5);
        int h = (int) (tileH * 2.5);

        int minx = -10;
        int miny = 10;
        Double[] values = {0.0, 1.0, 2.0};

        TiledImage img = ImageUtils.createConstantImage(minx, miny, w, h, tileW, tileH, values);

        assertNotNull(img);
        assertEquals(minx, img.getMinX());
        assertEquals(miny, img.getMinY());
        assertEquals(w, img.getWidth());
        assertEquals(h, img.getHeight());
        assertEquals(tileW, img.getTileWidth());
        assertEquals(tileH, img.getTileHeight());

        for (int band = 0; band < 3; band++) {
            for (int y = miny, ny = 0; ny < h; y++, ny++) {
                for (int x = minx, nx = 0; nx < w; x++, nx++) {
                    assertEquals(band, img.getSample(x, y, band));
                }
            }
        }
    }
}
