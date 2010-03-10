/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.numeric;


import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@code Histogram}.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class HistogramTest {

    Histogram<Double> hist;

    @Before
    public void setupBinner() {
        hist = new Histogram<Double>();
    }

    @Test
    public void testWithNoBinsDefined() {
        assertEquals(Histogram.NO_BIN, hist.getBinForValue(0.0));
    }

    @Test
    public void testWithSingleBin() {
        final double lower = 10;
        final double upper = 20;
        Range<Double> r = Range.create(lower, true, upper, false);
        hist.addBin(r);

        assertEquals(0, hist.getBinForValue(10.0));
        assertEquals(Histogram.NO_BIN, hist.getBinForValue(20.0));
    }

    @Test
    public void testOverlappingBinsThrowsException() {
        hist.addBin(Range.create(0.0, true, 0.1, true));

        try {
            // this bin overlap the first bin
            hist.addBin(Range.create(0.1, true, 0.2, true));
            fail("Expected an exception");
            
        } catch (HistogramException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testBinsAddedOutOfOrder() {
        hist.addBin(Range.create(0.2, true, 0.3, false));
        hist.addBin(Range.create(0.1, true, 0.2, false));
        hist.addBin(Range.create(0.0, true, 0.1, false));

        final double d = 0.1;
        for (int i = 0; i < 3; i++) {
            assertEquals(i, hist.getBinForValue(d * i));
        }
    }

    @Test
    public void testAddValue() {
        hist.addBin(Range.create(0.1, true, 0.2, false));
        hist.addBin(Range.create(0.2, true, 0.3, false));
        hist.addBin(Range.create(0.3, true, 0.4, false));

        hist.addValue(0.0);  // no bin
        hist.addValue(0.1);  // bin 0
        hist.addValue(0.15); // bin 0
        hist.addValue(0.2);  // bin 1
        hist.addValue(0.25); // bin 1
        hist.addValue(0.25); // bin 1
        hist.addValue(0.5);  // no bin

        assertEquals(Arrays.asList(2, 3, 0), hist.getCounts());
    }

}
