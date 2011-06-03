/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
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

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package jaitools.media.jai.combine;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Vector;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import javax.media.jai.operator.BandCombineDescriptor;
import javax.vecmath.MismatchedSizeException;

import jaitools.numeric.ArrayUtils;

/**
 * Computes a set of arbitrary linear combinations of the bands of many rendered source images,
 * using a specified matrix. The matrix size ({@code numRows}&times;{@code numColumns}) must be
 * equals to the following:
 * <ul>
 *   <li>{@code numRows}: the number of desired destination bands.</li>
 *   <li>{@code numColumns}: the total number of source bands (i.e. the
 *       sum of the number of source bands in all source images) plus one.</li>
 * </ul>
 * The number of source bands used to determine the matrix dimensions is given by the
 * following code regardless of the type of {@link ColorModel} the sources have:
 *
 * <pre><code>
 * int sourceBands = 0;
 * for (int i = 0; i &lt; sources.length; i++) {
 *     sourceBands += sources[i].getSampleModel().getNumBands();
 * }
 * </code></pre>
 * 
 * The extra column in the matrix contains constant values each of which is added to the
 * respective band of the destination. The transformation is therefore defined by the pseudocode:
 *
 * <pre><code>
 * // s = source pixel (not all from the same source image)
 * // d = destination pixel
 * for (int i = 0; i &lt; destBands; i++) {
 *     d[i] = matrix[i][sourceBands];
 *     for (int j = 0; j &lt; sourceBands; j++) {
 *         d[i] += matrix[i][j]*s[j];
 *     }
 * }
 * </code></pre>
 *
 * In the special case where there is only one source, this method is equivalent to JAI's
 * "{@linkplain BandCombineDescriptor BandCombine}" operation.
 *
 * @since 1.1
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Remi Eve
 */
public class CombineOpImage extends PointOpImage {

    /**
     * The minimum tile size.
     */
    private static final int MIN_TILE_SIZE = 256;
    /**
     * The linear combinaison coefficients as a matrix. This matrix may not be the same
     * than the one specified to the constructor, in that the zero coefficients may have
     * been purged (and {@link #sources} and {@link #bands} arrays adjusted accordingly})
     * for performance reason.
     */
    final double[][] matrix;
    /**
     * The source to use for each elements in {@link #matrix}.
     * This matrix size must be the same than {@code matrix}.
     */
    final int[][] sources;
    /**
     * The band to use for each elements in {@link #matrix}.
     * This matrix size must be the same than {@code matrix}.
     */
    final int[][] bands;
    /**
     * The number of source samples. This is the sum of the number of bands in
     * all source images. Each {@link #matrix} row must have this length plus 1.
     */
    final int numSamples;
    /**
     * The transform to apply on sample values before the linear combinaison,
     * or {@code null} if none.
     */
    protected final CombineTransform transform;

    /**
     * Construct an image with the specified matrix.
     *
     * @param images    The rendered sources.
     * @param matrix    The linear combinaison coefficients as a matrix.
     * @param transform The transform to apply on sample values before the linear combinaison,
     *                  or {@code null} if none.
     * @param hints     The rendering hints.
     *
     * @throws MismatchedSizeException if some rows in the {@code matrix} argument doesn't
     *         have the expected length.
     */
    public CombineOpImage(final Vector images,
            double[][] matrix,
            final CombineTransform transform,
            final RenderingHints hints) {
        super(images, createIntersection(
                (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT), images), hints, false);
        final int numRows = matrix.length;
        this.matrix = matrix = matrix.clone();
        this.sources = new int[numRows][];
        this.bands = new int[numRows][];
        this.transform = transform;
        int nSamples = 0;
        for (int i = getNumSources(); --i >= 0;) {
            nSamples += getSourceImage(i).getNumBands();
        }
        this.numSamples = nSamples;
        final boolean isSeparable = (transform == null) || transform.isSeparable();
        for (int j = 0; j < numRows; j++) {
            final double[] row = matrix[j];
            final int numColumns = row.length;
            if (numColumns != nSamples + 1) {
                throw new IllegalArgumentException();
            }
            int source = -1;
            int band = -1;
            int numBands = 0;
            int count = 0; // Number of non-zero coefficients.
            final double[] copy = new double[numColumns];
            final int[] src = new int[numColumns - 1];
            final int[] b = new int[numColumns - 1];
            final int numSources = src.length;
            for (int i = 0; i < numSources; i++) {
                if (++band >= numBands) {
                    band = 0;
                    numBands = getSourceImage(++source).getNumBands();
                }
                if (row[i] != 0 || !isSeparable) {
                    copy[count] = row[i];
                    src[count] = source;
                    b[count] = band;
                    count++;
                }
            }
            copy[count] = row[row.length - 1];
            this.matrix[j] = ArrayUtils.resize(copy, count + 1);
            this.sources[j] = ArrayUtils.resize(src, count);
            this.bands[j] = ArrayUtils.resize(b, count);
        }
        /*
         * Set the sample model according the number of destination bands.
         */
        if (getNumBands() != numRows) {
            throw new UnsupportedOperationException(
                    "Automatic derivation of SampleModel not yet implemented.");
        }
        permitInPlaceOperation();
    }

    /**
     * Computes a new {@link ImageLayout} which is the intersection of the specified
     * {@code ImageLayout} and all {@code RenderedImage}s in the supplied list. If the
     * {@link ImageLayout#getMinX minX}, {@link ImageLayout#getMinY minY},
     * {@link ImageLayout#getWidth width} and {@link ImageLayout#getHeight height}
     * properties are not defined in the {@code layout}, then they will be inherited
     * from the <strong>first</strong> source for consistency with {@link OpImage} constructor.
     *
     * @param  layout The original layout. This object will not be modified.
     * @param  sources The list of sources {@link RenderedImage}.
     * @return A new {@code ImageLayout}, or the original {@code layout} if no change was needed.
     */
    private static ImageLayout createIntersection(final ImageLayout layout, final List<RenderedImage> sources) {
        ImageLayout result = layout;
        if (result == null) {
            result = new ImageLayout();
        }
        final int n = sources.size();
        if (n != 0) {
            // If layout is not set, OpImage uses the layout of the *first*
            // source image according OpImage constructor javadoc.
            RenderedImage source = sources.get(0);
            int minXL = result.getMinX(source);
            int minYL = result.getMinY(source);
            int maxXL = result.getWidth(source) + minXL;
            int maxYL = result.getHeight(source) + minYL;
            for (int i = 0; i < n; i++) {
                source = sources.get(i);
                final int minX = source.getMinX();
                final int minY = source.getMinY();
                final int maxX = source.getWidth() + minX;
                final int maxY = source.getHeight() + minY;
                int mask = 0;
                if (minXL < minX) {
                    mask |= (1 | 4); // set minX and width
                }
                if (minYL < minY) {
                    mask |= (2 | 8); // set minY and height
                }
                if (maxXL > maxX) {
                    mask |= (4);   // Set width
                }
                if (maxYL > maxY) {
                    mask |= (8);   // Set height
                }
                if (mask != 0) {
                    if (layout == result) {
                        result = (ImageLayout) layout.clone();
                    }
                    if ((mask & 1) != 0) {
                        result.setMinX(minXL = minX);
                    }
                    if ((mask & 2) != 0) {
                        result.setMinY(minYL = minY);
                    }
                    if ((mask & 4) != 0) {
                        result.setWidth((maxXL = maxX) - minXL);
                    }
                    if ((mask & 8) != 0) {
                        result.setHeight((maxYL = maxY) - minYL);
                    }
                }
            }
            // If the bounds changed, adjust the tile size.
            if (result != layout) {
                source = sources.get(0);
                if (result.isValid(ImageLayout.TILE_WIDTH_MASK)) {
                    final int oldSize = result.getTileWidth(source);
                    final int newSize = toTileSize(result.getWidth(source), oldSize);
                    if (oldSize != newSize) {
                        result.setTileWidth(newSize);
                    }
                }
                if (result.isValid(ImageLayout.TILE_HEIGHT_MASK)) {
                    final int oldSize = result.getTileHeight(source);
                    final int newSize = toTileSize(result.getHeight(source), oldSize);
                    if (oldSize != newSize) {
                        result.setTileHeight(newSize);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Suggests a tile size for the specified image size. On input, {@code size} is the image's
     * size. On output, it is the tile size. This method write the result directly in the supplied
     * object and returns {@code size} for convenience.
     * <p>
     * This method it aimed to computing a tile size such that the tile grid would have overlapped
     * the image bound in order to avoid having tiles crossing the image bounds and being therefore
     * partially empty. This method will never returns a tile size smaller than
     * {@value ImageUtilities#MIN_TILE_SIZE}. If this method can't suggest a size,
     * then it left the corresponding {@code size} field ({@link Dimension#width width} or
     * {@link Dimension#height height}) unchanged.
     * <p>
     * The {@link Dimension#width width} and {@link Dimension#height height} fields are processed
     * independently in the same way. The following discussion use the {@code width} field as an
     * example.
     * <p>
     * This method inspects different tile sizes close to the {@linkplain JAI#getDefaultTileSize()
     * default tile size}. Lets {@code width} be the default tile width. Values are tried in the
     * following order: {@code width}, {@code width+1}, {@code width-1}, {@code width+2},
     * {@code width-2}, {@code width+3}, {@code width-3}, <cite>etc.</cite> until one of the
     * following happen:
     * <p>
     * <ul>
     *   <li>A suitable tile size is found. More specifically, a size is found which is a dividor
     *       of the specified image size, and is the closest one of the default tile size. The
     *       {@link Dimension} field ({@code width} or {@code height}) is set to this value.</li>
     *
     *   <li>An arbitrary limit (both a minimum and a maximum tile size) is reached. In this case,
     *       this method <strong>may</strong> set the {@link Dimension} field to a value that
     *       maximize the remainder of <var>image size</var> / <var>tile size</var> (in other
     *       words, the size that left as few empty pixels as possible).</li>
     * </ul>
     */
    /**
     * Suggests a tile size close to {@code tileSize} for the specified {@code imageSize}.
     * This method it aimed to computing a tile size such that the tile grid would have
     * overlapped the image bound in order to avoid having tiles crossing the image bounds
     * and being therefore partially empty. This method will never returns a tile size smaller
     * than {@value #MIN_TILE_SIZE}. If this method can't suggest a size, then it returns 0.
     *
     * @param imageSize The image size.
     * @param tileSize  The preferred tile size, which is often {@value #GEOTOOLS_DEFAULT_TILE_SIZE}.
     */
    private static int toTileSize(final int imageSize, final int tileSize) {
        final int MAX_TILE_SIZE = Math.min(tileSize * 2, imageSize);
        final int stop = Math.max(tileSize - MIN_TILE_SIZE, MAX_TILE_SIZE - tileSize);
        int sopt = 0;  // An "optimal" tile size, to be used if no exact dividor is found.
        int rmax = 0;  // The remainder of 'imageSize / sopt'. We will try to maximize this value.
        /*
         * Inspects all tile sizes in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE]. We will begin
         * with a tile size equals to the specified 'tileSize'. Next we will try tile sizes of
         * 'tileSize+1', 'tileSize-1', 'tileSize+2', 'tileSize-2', 'tileSize+3', 'tileSize-3',
         * etc. until a tile size if found suitable.
         *
         * More generally, the loop below tests the 'tileSize+i' and 'tileSize-i' values. The
         * 'stop' constant was computed assuming that MIN_TIME_SIZE < tileSize < MAX_TILE_SIZE.
         * If a tile size is found which is a dividor of the image size, than that tile size (the
         * closest one to 'tileSize') is returned. Otherwise, the loop continue until all values
         * in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE] were tested. In this process, we remind
         * the tile size that gave the greatest reminder (rmax). In other words, this is the tile
         * size with the smallest amount of empty pixels.
         */
        for (int i = 0; i <= stop; i++) {
            int s;
            if ((s = tileSize + i) <= MAX_TILE_SIZE) {
                final int r = imageSize % s;
                if (r == 0) {
                    // Found a size >= to 'tileSize' which is a dividor of image size.
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
            if ((s = tileSize - i) >= MIN_TILE_SIZE) {
                final int r = imageSize % s;
                if (r == 0) {
                    // Found a size <= to 'tileSize' which is a dividor of image size.
                    return s;
                }
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
        }
        /*
         * No dividor were found in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE]. At this point
         * 'sopt' is an "optimal" tile size (the one that left as few empty pixel as possible),
         * and 'rmax' is the amount of non-empty pixels using this tile size. We will use this
         * "optimal" tile size only if it fill at least 75% of the tile. Otherwise, we arbitrarily
         * consider that it doesn't worth to use a "non-standard" tile size. The purpose of this
         * arbitrary test is again to avoid too many small tiles (assuming that
         */
        return (rmax >= tileSize - tileSize / 4) ? sopt : 0;
    }

    /**
     * Compute one tile.
     *
     * @param images    An array of PlanarImage sources.
     * @param dest      A WritableRaster to be filled in.
     * @param destRect  The Rectangle within the destination to be written.
     */
    @Override
    public void computeRect(final PlanarImage[] images,
            final WritableRaster dest,
            final Rectangle destRect) {
        /*
         * Create the iterators. The 'iterRef' array will contains a copy of 'iters' where
         * the reference to an iterator is duplicated for each band in the source image:
         *
         *                   iterRef[i] = iters[sources[band][i]]
         */
        final RectIter[] iters = new RectIter[images.length];
        final RectIter[] iterRef = new RectIter[numSamples];
        double[] samples = null;
        final int length = iters.length;
        for (int i = 0; i < length; i++) {
            iters[i] = RectIterFactory.create(images[i], mapDestRect(destRect, i));
        }
        final WritableRectIter iTarget = RectIterFactory.createWritable(dest, destRect);
        /*
         * Iterates over all destination bands. In many case, the destination image
         * will have only one band.  Consequently, it is more efficient to iterates
         * through bands in the outer loop rather than the inner loop.
         */
        int band = 0;
        iTarget.startBands();
        boolean finished = iTarget.finishedBands();
        while (!finished) {
            final double[] row = this.matrix[band];
            final int[] workingBands = this.bands[band];
            final int[] workingSources = this.sources[band];
            final int ns = workingSources.length;
            if (ns > this.numSamples || ns > workingBands.length || ns >= row.length) {
                // Should not happen if the constructor checks was right.  We performs this
                // check unconditionnaly since it is cheap, and in the hope to help the JIT
                // to remove some array bound checks later in inner loops.
                throw new AssertionError(ns);
            }
            for (int i = 0; i < ns; i++) {
                iterRef[i] = iters[workingSources[i]];
            }
            if (samples == null || samples.length != ns) {
                samples = new double[ns];
            }
            /*
             * Iterates over all lines, then over all pixels. The 'finished' flag is reset
             * to 'nextXXXDone()' at the end of each loop.
             */
            iTarget.startLines();
            finished = iTarget.finishedLines();
            for (int i = 0; i < iters.length; i++) {
                iters[i].startLines();
                if (iters[i].finishedLines() != finished) {
                    // Should not happen, since constructor computed
                    // the intersection of all source images.
                    throw new RasterFormatException("Missing lines");
                }
            }
            while (!finished) {
                iTarget.startPixels();
                finished = iTarget.finishedPixels();
                for (int i = 0; i < iters.length; i++) {
                    iters[i].startPixels();
                    if (iters[i].finishedPixels() != finished) {
                        // Should not happen, since constructor computed
                        // the intersection of all source images.
                        throw new RasterFormatException("Missing pixels");
                    }
                }
                while (!finished) {
                    /*
                     * Computes the sample values.
                     */
                    for (int i = 0; i < ns; i++) {
                        samples[i] = iterRef[i].getSampleDouble(workingBands[i]);
                    }
                    if (transform != null) {
                        transform.transformSamples(samples);
                    }
                    double value = row[ns];
                    for (int i = 0; i < ns; i++) {
                        value += row[i] * samples[i];
                    }
                    iTarget.setSample(value);
                    finished = iTarget.nextPixelDone();
                    for (int i = 0; i < iters.length; i++) {
                        if (iters[i].nextPixelDone() != finished) {
                            // Should not happen, since constructor computed
                            // the intersection of all source images.
                            throw new RasterFormatException("Missing pixels");
                        }
                    }
                }
                finished = iTarget.nextLineDone();
                for (int i = 0; i < iters.length; i++) {
                    if (iters[i].nextLineDone() != finished) {
                        // Should not happen, since constructor computed
                        // the intersection of all source images.
                        throw new RasterFormatException("Missing lines");
                    }
                }
            }
            band++;
            finished = iTarget.nextBandDone();
        }
    }

    /**
     * Optimized {@code CombineOpImage} operation for dyadic (two sources) image. This operation
     * performs a linear combinaison of two images ({@code src0} and {@code src1}).
     * The parameters {@code scale0} and {@code scale1} indicate the scale of source
     * images {@code src0} and {@code src1}. If we consider pixel at coordinate
     * (<var>x</var>,<var>y</var>), its value is determinate by the pseudo-code:
     *
     * <blockquote><pre>
     * value = src0[x][y]*scale0 + src1[x][y]*scale1 + offset
     * </pre></blockquote>
     *
     * @version $Id$
     * @author Remi Eve
     * @author Martin Desruisseaux (IRD)
     */
    final static class Dyadic extends CombineOpImage {

        /**
         * The scale of image {@code src0} for each bands.
         */
        private final double[] scales0;
        /**
         * The scale of image {@code src1} for each bands.
         */
        private final double[] scales1;
        /**
         * The offset for each bands.
         */
        private final double[] offsets;

        /**
         * Construct a new instance of {@code CombineOpImage.Dyadic}.
         *
         * @param images  The rendered sources. This vector must contains exactly 2 sources.
         * @param matrix  The linear combinaison coefficients as a matrix.
         * @param hints   The rendering hints.
         *
         * @throws MismatchedSizeException if some rows in the {@code matrix} argument doesn't
         *         have the expected length.
         */
        public Dyadic(final Vector images,
                final double[][] matrix,
                final RenderingHints hints) {
            super(images, matrix, null, hints);
            if (getNumSources() != 2) {
                throw new IllegalArgumentException();
            }
            final int numBands = getNumBands();
            scales0 = new double[numBands];
            scales1 = new double[numBands];
            offsets = new double[numBands];
            for (int j = 0; j < numBands; j++) {
                final double[] row = this.matrix[j];
                final int[] workingSources = this.sources[j];
                final int[] workingBands = this.bands[j];
                for (int i = 0; i < workingSources.length; i++) {
                    final double coeff = row[i];
                    final int band = workingBands[i];
                    final int source = workingSources[i];
                    if (band != j) {
                        throw new AssertionError(band); // Should not happen.
                    }
                    switch (source) {
                        case 0:
                            scales0[band] = coeff;
                            break;
                        case 1:
                            scales1[band] = coeff;
                            break;
                        default:
                            throw new AssertionError(source); // Should not happen.
                    }
                }
                offsets[j] = row[workingSources.length];
            }
        }

        /**
         * Computes one tile.
         *
         * @param images    An array of PlanarImage sources.
         * @param dest      A WritableRaster to be filled in.
         * @param destRect  The Rectangle within the destination to be written.
         */
        @Override
        public void computeRect(final PlanarImage[] images,
                final WritableRaster dest,
                final Rectangle destRect) {
            final RectIter iSrc0 = RectIterFactory.create(images[0], mapDestRect(destRect, 0));
            final RectIter iSrc1 = RectIterFactory.create(images[1], mapDestRect(destRect, 1));
            final WritableRectIter iTarget = RectIterFactory.createWritable(dest, destRect);
            int band = 0;
            iSrc0.startBands();
            iSrc1.startBands();
            iTarget.startBands();
            if (!iTarget.finishedBands()
                    && !iSrc0.finishedBands()
                    && !iSrc1.finishedBands()) {
                final double scale0 = scales0[Math.min(band, scales0.length - 1)];
                final double scale1 = scales1[Math.min(band, scales1.length - 1)];
                final double offset = offsets[Math.min(band, offsets.length - 1)];
                do {
                    iSrc0.startLines();
                    iSrc1.startLines();
                    iTarget.startLines();
                    if (!iTarget.finishedLines()
                            && !iSrc0.finishedLines()
                            && !iSrc1.finishedLines()) {
                        do {
                            iSrc0.startPixels();
                            iSrc1.startPixels();
                            iTarget.startPixels();
                            if (!iTarget.finishedPixels()
                                    && !iSrc0.finishedPixels()
                                    && !iSrc1.finishedPixels()) {
                                do {
                                    iTarget.setSample(iSrc0.getSampleDouble() * scale0
                                            + iSrc1.getSampleDouble() * scale1 + offset);
                                } while (!iSrc0.nextPixelDone()
                                        && !iSrc1.nextPixelDone()
                                        && !iTarget.nextPixelDone());
                            }
                        } while (!iSrc0.nextLineDone()
                                && !iSrc1.nextLineDone()
                                && !iTarget.nextLineDone());
                    }
                    band++;
                } while (!iSrc0.nextBandDone()
                        && !iSrc1.nextBandDone()
                        && !iTarget.nextBandDone());
            }
        }
    }
}
