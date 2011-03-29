/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.demo;

import java.awt.image.RenderedImage;
import java.util.Random;

import javax.media.jai.TiledImage;

import jaitools.imageutils.ImageUtils;

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
