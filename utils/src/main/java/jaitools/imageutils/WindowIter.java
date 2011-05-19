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

import jaitools.numeric.NumberOperations;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.Arrays;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * An image iterator that passes a moving window over an image.
 * <p>
 * Example of use:
 * <pre><code>
 * RenderedImage myImage = ...
 * // Pass a 3x3 window, with the key element at pos (1,1), over band 0
 * // of the image
 * WindowIter iter = new WindowIter(myImage(myImage, null, new Dimension(3,3), new Point(1,1));
 * int[][] dataWindow = new int[3][3];
 * do {
 *     iter.getWindow(dataWindow);
 *     // do something with data
 * } while (iter.next());
 * </code></pre>
 * 
 * As with the JAI {@code RectIter.getSample} methods, alternative {@code getWindow} methods
 * are provided to return values as either integers, floats or doubles, optionally for a 
 * specified image band.
 * <p>
 * Note that control of the iterator position is different to the {@code RectIter} class which
 * has separate methods to advance and reset pixel, line and band position:
 * <ul>
 * <li>
 * The iterator is advanced with the {@link #next} method which handles movement in both
 * X and Y directions.
 * </li>
 * <li>
 * The iterator can be configured to move more than a single pixel / line via the {@code xstep}
 * and {@code ystep} arguments to the full constructor. If the step distance is larger than
 * the corresponding window dimension then some source image pixels will be absent from
 * the data windows returned by the iterator.
 * </li>
 * <li>
 * It is always safe to call the {@code next} method speculatively, although the 
 * {@link #hasNext} method is also provided for convenience.
 * </li>
 * <li>
 * The iterator's position is defined as the coordinates of the source image pixel 
 * at the data window's key element. The current position can be retrieved using the 
 * {@link #getPos} method.
 * </li>
 * </ul>
 * When the moving window is positioned over an edge of the image, those data window cells
 * beyond the image will be filled with a padding value. By default this is zero but an
 * alternative value can be specified via the {@code paddingValue} argument to the full
 * constructor.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class WindowIter {

    private static final Number DEFAULT_PADDING_VALUE = Integer.valueOf(0);

    private final Dimension windowDim;
    private final Point keyElement;

    // data buffer dimensions: band, line, pixel
    private final Number[][][] buffers;
    
    private final Number[][] destBuffer;
    
    private final RectIter iter;
    private final Rectangle iterBounds;
    private final int numImageBands;
    private final int xstep;
    private final int ystep;
    
    private int numLinesRead;
    private boolean firstAccess;
    private boolean finished;

    // X position (from 0) in the buffer
    private int bufferX;

    // Y-ordinate (source image space) of the data line positioned at the 
    // window's key element
    private int imageY;

    // Value to use for padding out-of-bounds parts of the data window
    private Number paddingValue;

    /**
     * Creates a new iterator. The iterator will advance one pixel at each
     * step and the data window will be padded with zeroes when required.
     * 
     * @param image the source image
     * @param bounds the bounds for this iterator or {@code null} for the whole image
     * @param windowDim the dimensions of the data window
     * @param keyElement the position of the key element in the data window
     * 
     * @throws IllegalArgumentException if any arguments other than bounds are {@code null};
     *         or if {@code keyElement} does not lie within {@code windowDim}
     */
    public WindowIter(RenderedImage image, Rectangle bounds, 
            Dimension windowDim, Point keyElement) {
        this(image, bounds, windowDim, keyElement, 1, 1, DEFAULT_PADDING_VALUE);
    }

    /**
     * Creates a new iterator. 
     * 
     * @param image the source image
     * @param bounds the bounds for this iterator or {@code null} for the whole image
     * @param windowDim the dimensions of the data window
     * @param keyElement the position of the key element in the data window
     * @param xstep step distance in X-direction (pixels)
     * @param ystep step distance in Y-direction (lines)
     * @param paddingValue value to use for padding out-of-bounds parts of the data window
     * 
     * @throws IllegalArgumentException if any arguments other than bounds are {@code null};
     *         or if {@code keyElement} does not lie within {@code windowDim};
     *         or if either step distance is less than 1
     */
    public WindowIter(RenderedImage image, Rectangle bounds, 
            Dimension windowDim, Point keyElement,
            int xstep, int ystep, Number paddingValue) {

        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        if (windowDim == null) {
            throw new IllegalArgumentException("windowDim must not be null");
        }
        if (keyElement == null) {
            throw new IllegalArgumentException("keyElement must not be null");
        }
        if (keyElement.x < 0 || keyElement.x >= windowDim.width ||
            keyElement.y < 0 || keyElement.y >= windowDim.height) {
            throw new IllegalArgumentException(String.format(
                    "The supplied key element position (%d, %d) is invalid for"
                  + "data window dimensions: width=%d height=%D",
                    keyElement.x, keyElement.y, windowDim.width, windowDim.height));
        }
        if (xstep < 1 || ystep < 1) {
            throw new IllegalArgumentException(
                    "The value of both xstep and ystep must be 1 or greater");
        }
        if (paddingValue == null) {
            throw new IllegalArgumentException("paddingValue must not be null");
        }

        this.iter = RectIterFactory.create(image, bounds);

        if (bounds == null) {
            this.iterBounds = new Rectangle(
                    image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());

        } else {
            this.iterBounds = new Rectangle(bounds);
        }

        this.windowDim = new Dimension(windowDim);
        this.keyElement = new Point(keyElement);
        this.paddingValue = NumberOperations.newInstance(paddingValue, paddingValue.getClass());

        this.numImageBands = image.getSampleModel().getNumBands();
        buffers = new Number[numImageBands][][];
        for (int b = 0; b < numImageBands; b++) {
            Number[][] bandBuffer = new Number[windowDim.height][];
            for (int i = 0; i < windowDim.height; i++) {
                Number[] ar = new Number[this.iterBounds.width];
                Arrays.fill(ar, this.paddingValue);
                bandBuffer[i] = ar;
            }
            buffers[b] = bandBuffer;
        }

        this.destBuffer = new Number[numImageBands][];
        for (int b = 0; b < numImageBands; b++) {
            destBuffer[b] = new Number[windowDim.width * windowDim.height];
        }

        this.firstAccess = true;
        this.finished = false;
        this.bufferX = 0;
        this.imageY = iterBounds.y;
        this.numLinesRead = 0;
        this.xstep = xstep;
        this.ystep = ystep;
    }

    /**
     * Gets the source image coordinates of the pixel currently at the 
     * window key element position. Note that when the iterator has
     * finished this method returns {@code null}.
     * 
     * @return the pixel coordinates
     */
    public Point getPos() {
        Point p = null;
        if (!finished) {
            p = new Point(iterBounds.x + bufferX, imageY);
        }
        return p;
    }

    /**
     * Tests if this iterator has more data.
     * 
     * @return {@code true} if more data are available; {@code false} otherwise
     */
    public boolean hasNext() {
        return !finished;
    }

    /**
     * Advances the iterator using the specified X and Y step distances. 
     * When the right-hand edge of bound rectangle is reached the iterator
     * automatically increments its Y (line) position. If the iterator is already
     * at the end of bounding rectangle this method safely returns {@code false}.
     * 
     * @return {@code true} if the iterator was advanced; {@code false} if it was
     *         already finished
     */
    public boolean next() {
        if (!finished) {
            bufferX = Math.min(bufferX + xstep, iterBounds.width) % iterBounds.width;
            if (bufferX == 0) {
                if (numLinesRead < iterBounds.height) {
                    moveLinesUp();
                    positionIter();
                    final int topLine = ystep > windowDim.height ? 0 : windowDim.height - ystep;
                    for (int y = topLine; y < windowDim.height; y++) {
                        readIntoLine(y);
                    }
                } else if (imageY < iterBounds.y + iterBounds.height - 1) {
                    moveLinesUp();
                } else {
                    finished = true;
                }
            }
        }
        
        return !finished;
    }

    /**
     * Gets the data window at the current iterator position in image band 0 as integer values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @return the filled destination array
     */
    public int[][] getWindow(int[][] dest) {
        return getWindow(dest, 0);
    }

    /**
     * Gets the data window at the current iterator position and specified image band 
     * as integer values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @param band the image band from which to retrieve data
     * @return the filled destination array
     */
    public int[][] getWindow(int[][] dest, int band) {
        checkBandArg(band);
        
        if (dest == null || dest.length != windowDim.height || dest[0].length != windowDim.width) {
            dest = new int[windowDim.height][windowDim.width];
        }

        loadDestBuffer(band);
        int k = 0;
        for (int y = 0; y < windowDim.height; y++) {
            for (int x = 0; x < windowDim.width; x++) {
                dest[y][x] = destBuffer[band][k++].intValue();
            }
        }
        return dest;
    }

    /**
     * Gets the data window at the current iterator position in image band 0 as float values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @return the filled destination array
     */
    public float[][] getWindowFloat(float[][] dest) {
        return getWindowFloat(dest, 0);
    }

    /**
     * Gets the data window at the current iterator position and specified image band 
     * as float values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @param band the image band from which to retrieve data
     * @return the filled destination array
     */
    public float[][] getWindowFloat(float[][] dest, int band) {
        checkBandArg(band);
        
        if (dest == null || dest.length != windowDim.height || dest[0].length != windowDim.width) {
            dest = new float[windowDim.height][windowDim.width];
        }

        loadDestBuffer(band);
        int k = 0;
        for (int y = 0; y < windowDim.height; y++) {
            for (int x = 0; x < windowDim.width; x++) {
                dest[y][x] = destBuffer[band][k++].floatValue();
            }
        }
        return dest;
    }

    /**
     * Gets the data window at the current iterator position in image band 0 as double values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @return the filled destination array
     */
    public double[][] getWindowDouble(double[][] dest) {
        return getWindowDouble(dest, 0);
    }

    /**
     * Gets the data window at the current iterator position and specified image band 
     * as double values.
     * If {@code dest} is {@code null} or not equal in size to the data window
     * dimensions a new array will be allocated, otherwise the provided array
     * is filled. In either case, the destination array is returned for convenience.
     * 
     * @param dest destination array or {@code null}
     * @param band the image band from which to retrieve data
     * @return the filled destination array
     */
    public double[][] getWindowDouble(double[][] dest, int band) {
        checkBandArg(band);
        
        if (dest == null || dest.length != windowDim.height || dest[0].length != windowDim.width) {
            dest = new double[windowDim.height][windowDim.width];
        }

        loadDestBuffer(band);
        int k = 0;
        for (int y = 0; y < windowDim.height; y++) {
            for (int x = 0; x < windowDim.width; x++) {
                dest[y][x] = destBuffer[band][k++].doubleValue();
            }
        }
        return dest;
    }

    /**
     * Helper for the getWindow methods. Loads the destination buffer from
     * the line buffers.
     */
    private void loadDestBuffer(int band) {
        if (firstAccess) {
            int destLine = keyElement.y;
            while (destLine < windowDim.height && numLinesRead < iterBounds.height) {
                readIntoLine(destLine++); 
            }
            firstAccess = false;
        }

        final int minx = bufferX - keyElement.x;
        final int maxx = bufferX + windowDim.width - keyElement.x - 1;
        int k = 0;
        for (int y = 0; y < windowDim.height; y++) {
            for (int x = minx, winX = 0; x <= maxx; x++, winX++) {
                if (x >= 0 && x < iterBounds.width) {
                    destBuffer[band][k] = buffers[band][y][x];
                } else {
                    destBuffer[band][k] = paddingValue;
                }
                k++ ;
            }
        }
    }

    /**
     * Reads a line of image data into the specified data buffer.
     * 
     * @param destLine index of the data buffer to receive the data
     */
    private void readIntoLine(int destLine) {
        if (iter.finishedLines()) {
            return;
        }
        
        int x = 0;
        do {
            for (int b = 0; b < numImageBands; b++) {
                buffers[b][destLine][x] = iter.getSampleDouble(b);
            }
            x++ ;
        } while (!iter.nextPixelDone());

        iter.startPixels();
        iter.nextLineDone();
        numLinesRead++ ;
    }

    /**
     * Moves lines up in the data buffer by ystep.
     */
    private void moveLinesUp() {
        for (int b = 0; b < numImageBands; b++) {
            if (ystep >= windowDim.height) {
                // just fill lines with pad value
                for (int y = 0; y < windowDim.height; y++) {
                    Arrays.fill(buffers[b][y], paddingValue);
                }
            } else {
                // shuffle lines up, avoiding cost of allocating new memory
                for (int y = ystep, ynew = 0; y < windowDim.height; y++, ynew++) {
                    Number[] temp = buffers[b][ynew];
                    buffers[b][ynew] = buffers[b][y];
                    Arrays.fill(temp, paddingValue);
                    buffers[b][y] = temp;
                }
            }
        }

        imageY += ystep;
    }

    /**
     * Positions the delegate iterator at the next line to be read into
     * the data buffers. This method only does anything if ystep is greater
     * than the height of the data window.
     */
    private void positionIter() {
        if (ystep > windowDim.height) {
            int nSkip = ystep - windowDim.height;
            while (nSkip > 0 && !iter.finishedLines()) { 
                iter.nextLineDone();
                numLinesRead++ ;
                nSkip-- ;
            }
        }
    }

    /**
     * Helper method to check that a band value is valid.
     * 
     * @param band band value
     */
    private void checkBandArg(int band) {
        if (band < 0 || band >= numImageBands) {
            throw new IllegalArgumentException( String.format(
                    "band argument (%d) is out of range: number of image bands is %d",
                    band, numImageBands) );
        }
    }

}
