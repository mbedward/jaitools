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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import javax.media.jai.TiledImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Most of the methods in this class are inherited from its base class and
 * tested in SimpleIteratorTest. Here we have tests of the setSample methods.
 *
 * @author michael
 */
public class WritableSimpleIteratorTest extends TestBase {
    
    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;

    private TiledImage image;
    private WritableSimpleIterator iter;

    @Test
    public void setSequentialDefaultBand() {
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, 0);
        iter = new WritableSimpleIterator(image, null, 0);
        int k = 100;
        do {
            iter.setSample(k++);
        } while (iter.next());

        assertImageValues(new int[]{100});
    }

    @Test
    public void setSequentialSpecifiedBand() {
        final int[] startValues = {100, 200, 300};
        
        Integer[] fill = new Integer[startValues.length];
        Arrays.fill(fill, 0);
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, fill);
        iter = new WritableSimpleIterator(image, null, 0);

        int k = 0;
        do {
            for (int band = 0; band < startValues.length; band++) {
                iter.setSample(band, startValues[band] + k);
            }
            k++ ;
        } while (iter.next());

        assertImageValues(startValues);
    }

    @Test
    public void setSampleReturnsFalseOutsideImage() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        final Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = createAdjustedBounds(imageBounds, 5);
        iter = new WritableSimpleIterator(image, iterBounds, 0);

        do {
            boolean rtnValue = iter.setSample(1);
            assertEquals(imageBounds.contains(iter.getPos()), rtnValue);
        } while (iter.next());
    }

    @Test
    public void setSampleForPosition() {
        final int[] startValues = {100, 200, 300};
        
        Integer[] fill = new Integer[startValues.length];
        Arrays.fill(fill, 0);
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, fill);
        iter = new WritableSimpleIterator(image, null, 0);

        final int w = image.getWidth();
        final int h = image.getHeight();

        // set samples in row-col order but with values as if set
        // by col-row order so that assertImageValues still works
        Point pos = new Point();
        for (int x = image.getMinX(), ix = 0; ix < w; x++, ix++) {
            for (int y = image.getMinY(), iy = 0; iy < h; y++, iy++) {
                pos.setLocation(x, y);
                for (int band = 0; band < image.getNumBands(); band++) {
                    iter.setSample(pos, band, startValues[band] + iy * w + ix);
                }
            }
        }

        assertImageValues(startValues);
    }

    private void assertImageValues(int[] startValues) {
        if (image == null) {
            throw new IllegalStateException("You forgot to create the image first");
        }
        if (startValues.length != image.getNumBands()) {
            throw new IllegalArgumentException(
                    "startValues length should equals number of image bands");
        }

        final int w = image.getWidth();
        final int h = image.getHeight();
        
        int k = 0; 
        for (int y = image.getMinY(), iy = 0; iy < h; y++, iy++) {
            for (int x = image.getMinX(), ix = 0; ix < w; x++, ix++) {
                for (int band = 0; band < image.getNumBands(); band++) {
                    assertEquals(startValues[band] + k, image.getSample(x, y, band));
                }
                k++ ;
            }
        }
    }
}
