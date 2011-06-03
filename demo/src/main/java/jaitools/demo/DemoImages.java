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

package jaitools.demo;

import java.awt.image.RenderedImage;

import javax.media.jai.TiledImage;

import jaitools.imageutils.ImageUtils;
import java.util.Random;

/**
 * Serves images to the demo applications.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DemoImages {

    public static RenderedImage createChessboardImage(int width, int height) {
        TiledImage image = ImageUtils.createConstantImage(width, height, 0d);
        int squareWidth = (int)Math.max(width / 8, height / 8);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isOddRow = Math.floor(y / squareWidth) % 2 == 1;
                boolean isOddCol = Math.floor(x / squareWidth) % 2 == 1;
                if (isOddRow ^ isOddCol) {
                    image.setSample(x, y, 0, 1);
                }
            }
        }
        
        return image;
    }

    public static RenderedImage createInterferenceImage(int width, int height) {
        TiledImage image = ImageUtils.createConstantImage(width, height, 0d);
        for (int y = 0; y < height; y++) {
            double dy = (double)y / height - 0.5;
            for (int x = 0; x < width; x++) {
                double dx = (double)x / width - 0.5;
                double dxy = Math.sqrt(dx*dx + dy*dy);

                image.setSample(x, y, 0, Math.sin(dx * 100) + Math.sin(dxy * 100));
            }
        }
        return image;
    }

    public static RenderedImage createRipplesImage(int width, int height) {
        final int xc = width / 2;
        final int yc = height / 2;

        TiledImage image = ImageUtils.createConstantImage(width, height, 0d);
        for (int y = 0; y < height; y++) {
            double dy = (double)(y-yc) / yc;
            for (int x = 0; x < width; x++) {
                double dx = (double)(x-xc) / xc;
                double d = Math.sqrt(dx*dx + dy*dy);
                image.setSample(x, y, 0, Math.sin(8 * Math.PI * d));
            }
        }
        return image;
    }

    public static RenderedImage createSquircleImage(int width, int height) {
        final int w = width - 1;
        final int h = height - 1;

        TiledImage image = ImageUtils.createConstantImage(width, height, 0d);
        for (int y = 0; y < height; y++) {
            double dy = 4 * Math.PI * (0.5 - (double)y / h); 
            for (int x = 0; x < width; x++) {
                double dx = 4 * Math.PI * (0.5 - (double)x / w);
                image.setSample(x, y, 0, Math.sqrt(Math.abs(Math.cos(dx) + Math.cos(dy))));
            }
        }
        return image;
    }

    public static RenderedImage createUniformRandomImage(int width, int height, double maxValue) {
        TiledImage image = ImageUtils.createConstantImage(width, height, 0d);
        Random rr = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setSample(x, y, 0, rr.nextDouble() * maxValue);
            }
        }
        return image;
    }
    
    public static RenderedImage createBandedImage(int width, int height, int numBands) {
        int bandWidth = (int) Math.ceil((double)height / numBands);
        int bandIndex = 0;
        
        TiledImage image = ImageUtils.createConstantImage(width, height, Integer.valueOf(0));
        
        for (int y = 0; y < height; y++) {
            if (y % bandWidth == 0) {
                bandIndex++;
            }
            
            for (int x = 0; x < width; x++) {
                image.setSample(x, y, 0, bandIndex);
            }
        }
        
        return image;
    }
    
}
