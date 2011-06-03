/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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
