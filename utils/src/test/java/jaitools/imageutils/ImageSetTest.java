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

import java.awt.image.RenderedImage;
import java.util.Map;

import javax.media.jai.TiledImage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class ImageSetTest extends TestBase {

    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final Integer OUTSIDE = Integer.valueOf(-1);

    private static final String[] NAMES = {"foo", "bar", "baz"};
    private final RenderedImage[] images = new RenderedImage[NAMES.length];
    private ImageSet<String> theSet;

    @Before
    public void setup() {
        theSet = new ImageSet<String>();
        for (int i = 0; i < NAMES.length; i++) {
            images[i] = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS, i * 10);
            theSet.add(NAMES[i], images[i], OUTSIDE);
        }
    }

    @Test
    public void getNumImages() {
        assertEquals(NAMES.length, theSet.size());
    }
    
    @Test
    public void getImageByKey() {
        for (int i = 0; i < NAMES.length; i++) {
            // note: just testing identity here
            assertTrue(images[i] == theSet.get(NAMES[i]));
        }
    }

    @Test
    public void getIterSample() {
        ImageSetIterator<String> iterator = theSet.getIterator();

        int x = 0;
        int y = 0;
        do {
            Map<String, Number> sample = iterator.getSample();
            assertSample(sample, x, y, 0);

            x = (x + 1) % WIDTH;
            if (x == 0) {
                y++ ;
            }
        } while (iterator.next());
    }

    @Test
    public void getIterSampleByBand() {
        ImageSetIterator<String> iterator = theSet.getIterator();

        int x = 0;
        int y = 0;
        do {
            for (int band = 0; band < NUM_BANDS; band++) {
                Map<String, Number> sample = iterator.getSample(band);
                assertSample(sample, x, y, band);
            }

            x = (x + 1) % WIDTH;
            if (x == 0) {
                y++ ;
            }
        } while (iterator.next());
    }

    @Test
    public void copySet() {
        ImageSet<String> copy = ImageSet.copy(theSet);
        assertNotNull(copy);
        assertEquals(theSet.size(), copy.size());
        
        for (String key : copy.keySet()) {
            assertTrue(theSet.containsKey(key));
            assertTrue(theSet.get(key) == copy.get(key));
            assertTrue(theSet.getOutsideValue(key) == copy.getOutsideValue(key));
        }
    }

    private void assertSample(Map<String, ? extends Number> sample, int x, int y, int band) {
        assertEquals(NAMES.length, sample.size());
        for (int i = 0; i < NAMES.length; i++) {
            Number value = sample.get(NAMES[i]);
            assertNotNull(value);

            int imgValue = getImageValue(i, x, y, band);
            assertEquals(imgValue, value.intValue());
        }
    }

    private int getImageValue(int index, int x, int y, int band) {
        TiledImage timg = (TiledImage) images[index];
        return timg.getSample(x, y, band);
    }

}
