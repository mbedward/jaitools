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

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

/**
 * The default abstract base class for runtime classes that implement
 * direct evaluation.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class AbstractDirectRuntime implements JiffleDirectRuntime {

    /* 
     * Note: not using generics here because they are not
     * supported by the Janino compiler.
     */
    protected Map images = new HashMap();
    protected Map readers = new HashMap();
    protected Map writers = new HashMap();
    
    /* TODO: add band support to scripts */
    protected int _band = 0;
    
    protected int _width;
    protected int _height;
    protected WritableRenderedImage _refImage;
    

    public void setDestinationImage(String imageName, WritableRenderedImage image) {
        images.put(imageName, image);
        
        if (writers.isEmpty()) {
            _refImage = image;
            _width = image.getWidth();
            _height = image.getHeight();
        }
        
        writers.put(imageName, RandomIterFactory.createWritable(image, null));
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        images.put(imageName, image);
        readers.put(imageName, RandomIterFactory.create(image, null));
    }

    public void evaluateAll(JiffleProgressListener pl) {
        JiffleProgressListener listener = pl == null ? new NullProgressListener() : pl;

        final long numPixels = (long)_refImage.getWidth() * _refImage.getHeight();
        listener.setTaskSize(numPixels);
        long count = 0;
        
        listener.start();
        for (int y = _refImage.getMinY(), iy = 0; iy < _height; y++, iy++) {
            for (int x = _refImage.getMinX(), ix = 0; ix < _height; x++, ix++) {
                evaluate(x, y);
                listener.update( ++count );
            }
        }
        listener.finish();
    }
    
    public double readFromImage(String imageName, int x, int y, int band) {
        RandomIter iter = (RandomIter) readers.get(imageName);
        return iter.getSampleDouble(x, y, band);
    }
    
    public void writeToImage(String imageName, int x, int y, int band, double value) {
        WritableRandomIter iter = (WritableRandomIter) writers.get(imageName);
        iter.setSample(x, y, band, value);
    }
    
}
