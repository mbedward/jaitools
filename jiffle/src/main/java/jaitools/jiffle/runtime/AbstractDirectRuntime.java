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
public abstract class AbstractDirectRuntime extends AbstractJiffleRuntime implements JiffleDirectRuntime {

    /* 
     * Note: not using generics here because they are not
     * supported by the Janino compiler.
     */
    
    /** 
     * Maps image variable names ({@link String}) to images
     * ({@link RenderedImage}).
     * 
     */
    protected Map images = new HashMap();
    
    /** 
     * Maps source image variable names ({@link String}) to image
     * iterators ({@link RandomIter}).
     */
    protected Map readers = new HashMap();
    
    /**
     * Maps destination image variable names ({@link String} to
     * image iterators ({@link WritableRandomIter}).
     */
    protected Map writers = new HashMap();

    /** Processing bounds min X ordinate */
    protected int _minx;

    /** Processing bounds min Y ordinate */
    protected int _miny;

    /** Processing bounds width */
    protected int _width;
    
    /** Processing bounds height */
    protected int _height;
    
    public void setDestinationImage(String imageName, WritableRenderedImage image) {
        images.put(imageName, image);
        
        if (writers.isEmpty()) {
            setBounds(image.getMinX(), image.getMinY(),
                    image.getWidth(), image.getHeight());
        }
        
        writers.put(imageName, RandomIterFactory.createWritable(image, null));
    }

    public void setBounds(int minx, int miny, int width, int height) {
        _minx = minx;
        _miny = miny;
        _width = width;
        _height = height;

        initImageScopeVars();
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        images.put(imageName, image);
        readers.put(imageName, RandomIterFactory.create(image, null));
    }

    public void evaluateAll(JiffleProgressListener pl) {
        JiffleProgressListener listener = pl == null ? new NullProgressListener() : pl;

        final long numPixels = (long)_width * _height;
        listener.setTaskSize(numPixels);
        long count = 0;
        
        listener.start();
        for (int y = _miny, iy = 0; iy < _height; y++, iy++) {
            for (int x = _minx, ix = 0; ix < _height; x++, ix++) {
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

    /**
     * Initializes image-scope variables. These are fields in the runtime class.
     * They are initialized in a separate method rather than the constructor
     * because they may depend on expressions involving values that are not
     * known until the processing area is set (e.g. Jiffle's width() function).
     */
    protected abstract void initImageScopeVars();

}
