/* 
 *  Copyright (c) 2009-2010, Michael Bedward. All rights reserved. 
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

package org.jaitools.imageutils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

import javax.media.jai.PlanarImage;

import org.jaitools.tiledimage.DiskMemImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 */
public class FloodFillerTest {

    private static final int TILE_WIDTH = 128;
    private static final int IMAGE_WIDTH = 2 * TILE_WIDTH;


    public FloodFillerTest() {
    }

    /**
     * Test of fill method, of class FloodFiller.
     */
    @Test
    public void testFill() {
        // System.out.println("   flood fill with single image");

        boolean diagonal;
        int fillValue;
        FloodFiller filler = null;
        FillResult fill = null;

        SampleModel sm = new ComponentSampleModel(
                DataBuffer.TYPE_BYTE,
                TILE_WIDTH,
                TILE_WIDTH,
                1, TILE_WIDTH,  // pixel stride and scan-line stride
                new int[]{0});  // band offset

        ColorModel cm = PlanarImage.createColorModel(sm);

        DiskMemImage image = new DiskMemImage( IMAGE_WIDTH, IMAGE_WIDTH, sm, cm );
        Graphics2D gr = image.createGraphics();
        gr.setBackground(Color.WHITE);
        gr.clearRect(0, 0, IMAGE_WIDTH, IMAGE_WIDTH);

        gr.setColor(Color.BLACK);

        // two rectangles meeting diagonally
        gr.fillRect(TILE_WIDTH/2, TILE_WIDTH/2, TILE_WIDTH/2, TILE_WIDTH/2);
        gr.fillRect(TILE_WIDTH, TILE_WIDTH, TILE_WIDTH/2, TILE_WIDTH/2);

        // diagonal fill into first rectangle should fill
        // second rectangle as well
        diagonal = true;
        fillValue = 64;
        filler = new FloodFiller(image, 0, image, 0, 0, diagonal);
        fill = filler.fill(TILE_WIDTH / 2, TILE_WIDTH / 2, fillValue);
        assertTrue(fill.getNumPixels() == 2 * (TILE_WIDTH/2) * (TILE_WIDTH/2));

        // orthogonal fill into first rectangle should not fill
        // second rectangle
        diagonal = false;
        fillValue = 128;
        filler = new FloodFiller(image, 0, image, 0, 0, diagonal);
        fill = filler.fill(TILE_WIDTH / 2, TILE_WIDTH / 2, fillValue);
        assertTrue(fill.getNumPixels() == (TILE_WIDTH/2) * (TILE_WIDTH/2));

        // fill with a specified radius
        diagonal = false;
        fillValue = 192;
        filler = new FloodFiller(image, 0, image, 0, 0, diagonal);
        fill = filler.fillRadius(TILE_WIDTH/2, TILE_WIDTH/2, fillValue, TILE_WIDTH/2);

        assertTrue(fill.getBounds().width == TILE_WIDTH / 2);
        assertTrue(fill.getBounds().height == TILE_WIDTH / 2);
    }

}
