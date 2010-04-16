/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.zonalstats;

import jaitools.CollectionFactory;
import jaitools.numeric.Range;
import jaitools.numeric.Statistic;

import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the ZonalStats operator
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @author Simone Giannecchini, GeoSolutions SAS
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsTest {

    private static final Logger LOGGER = Logger.getLogger("ZonalStatsTest");

    private static final double EPS = 1.0e-6;
    
    private static final int WIDTH = 128;
    private static final int HEIGHT = 64;
    private static final int TILE_WIDTH = 64;
    private static final int MIN_DATUM = -5;
    private static final int MAX_DATUM = 5;

    private static Random rand = new Random();

    private static RenderedImage dataImage = createRandomImage(MIN_DATUM, MAX_DATUM);
    private static RenderedImage constant1Image = createConstantImage(new Double[]{1.0});
    private static RenderedImage twoValueImage = createTwoValuesImage();
    private static RenderedImage multibandImage = createMultibandImage();
    private static RenderedImage multibandImageNoData = createMultibandImageNoData();


    @Test
    public void testValidateNumSources() {
    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   validate number of sources");

        boolean gotException = false;
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertTrue("Failed validation of no sources", gotException);

        gotException = false;
        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertFalse("Failed validation of 1 source", gotException);

        gotException = false;
        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}));
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
            gotException = true;
        }
        assertFalse("Failed validation of 2 sources", gotException);
    }

    @Test
    public void testZoneImageType() {
    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   validate zone image type");
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Float[]{0f}));
        boolean gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertTrue("Failed to reject non-integral zone image", gotException);

        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}));
        gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertFalse("Failed to accept integral zone image", gotException);
    }

    @Test
    public void testZoneImageOverlap() {
    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   validate data - zone image overlap");
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}, new Point(WIDTH, WIDTH)));
        boolean gotException = false;
        try { 
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertTrue("Failed to reject non-overlapping zone image", gotException);

        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}));
        pb.setParameter("zoneTransform", AffineTransform.getTranslateInstance(WIDTH, WIDTH));
        gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertTrue("Failed to reject zone image that does not overlap when transformed",
                gotException);

        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}, new Point(WIDTH / 2,WIDTH / 2)));
        gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Throwable ex) {
            gotException = true;
        }
        assertFalse("Failed to accept partially overlapping zone image", gotException);
    }

    @Test
    public void testMin() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test min");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.MIN, Double.valueOf(MIN_DATUM));
    }

    @Test
    public void testMax() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test max");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MAX});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.MAX, Double.valueOf(MAX_DATUM));
    }
    
    @Test
    public void testMean() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test mean");

        double expMean = 0d;

        RectIter iter = RectIterFactory.create(dataImage, null);
        do {
            do {
                expMean += iter.getSample();
            } while( !iter.nextPixelDone() );
            iter.startPixels();
        } while( !iter.nextLineDone() );
        expMean /= (WIDTH * WIDTH);

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MEAN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.MEAN, expMean);
    }

    @Test
    public void testStdDev() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test standard deviation");

        double expSD;
        double mOld = 0d, mNew;
        double variance = 0d;

        RectIter iter = RectIterFactory.create(dataImage, null);
        long n = 0;
        do {
            do {
                double val = iter.getSample();
                n++;
                if (n == 1) {
                    mOld = mNew = val;
                } else {
                    mNew = mOld + (val - mOld) / n;
                    variance = variance + (val - mOld) * (val - mNew);
                    mOld = mNew;
                }
            } while( !iter.nextPixelDone() );
            iter.startPixels();
        } while( !iter.nextLineDone() );

        expSD = Math.sqrt(variance / (n - 1));

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.SDEV});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.SDEV, expSD);
    }

    @Test
    public void testRange() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test range");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.RANGE});
        RenderedOp op = JAI.create("ZonalStats", pb);
        double exp = MAX_DATUM - MIN_DATUM;
        assertSingleResult(op, Statistic.RANGE, exp);
    }

    @Test
    public void testSum() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test sum");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", constant1Image);
        pb.setParameter("stats", new Statistic[]{Statistic.SUM});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.SUM, Double.valueOf(WIDTH * WIDTH));
    }

    @Test
    public void testExactMedian() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test exact median");

        final double expMedian = 0.0;

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MEDIAN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.MEDIAN, expMedian);
    }

    @Test
    public void testApproxMedian() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test approximate median");

        final double expMedian = 0.0;

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.APPROX_MEDIAN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        assertSingleResult(op, Statistic.APPROX_MEDIAN, expMedian);
    }

    @Test
    public void testMultiband() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test multiband");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", multibandImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.RANGE});

        // make the test a bit more testing by skipping a band
        pb.setParameter("bands", new Integer[]{0, 2});

        RenderedOp op = JAI.create("ZonalStats", pb);
        ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);

        final int MIN = 0, MAX = 1, RANGE = 2;
        final int[] flag = new int[3];

        for (Result r : stats.results()) {
            switch (r.getStatistic()) {
                case MIN:
                    assertEquals(-9999.0, r.getValue(), EPS);
                    flag[MIN]++ ;
                    break;

                case MAX:
                    assertEquals(r.getImageBand() + 1, r.getValue(), EPS);
                    flag[MAX]++;
                    break;

                case RANGE:
                    assertEquals(r.getImageBand() + 10000.0, r.getValue(), EPS);
                    flag[RANGE]++;
                    break;

                default:
                    fail("unexpected statistic: " + r.getStatistic());
            }
        }

        for (int i = 0; i < flag.length; i++) {
            assertEquals(2, flag[i]);
        }

    }

    @Test
    public void testExclusionRanges() {
        if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("   test excluding ranges of values");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX});

        List<Range<Double>> exclusions = CollectionFactory.list();

        // exclude lower end of values
        double min = MIN_DATUM + 1;
        exclusions.add(Range.create(null, true, min, false));

        // exclude upper end of values
        double max = MAX_DATUM - 1;
        exclusions.add(Range.create(max, false, null, true));

        pb.setParameter("ranges", exclusions);
        pb.setParameter("rangesType", Range.Type.EXCLUDE);

        RenderedOp op = JAI.create("ZonalStats", pb);
        ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);

        assertTrue(stats.statistic(Statistic.MIN).results().get(0).getValue() >= min);
        assertTrue(stats.statistic(Statistic.MAX).results().get(0).getValue() <= max);
    }
    
    @Test
    public void testNoDataRanges() {
        if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("   test testNoDataRanges");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", multibandImageNoData);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.RANGE});

        // set NoData range values
        List<Range<Double>> noRanges = CollectionFactory.list();
        noRanges.add(Range.create(-9999.0d, null));
        pb.setParameter("noDataRanges", noRanges);
        
        pb.setParameter("bands", new Integer[]{0, 2});

        RenderedOp op = JAI.create("ZonalStats", pb);
        ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);

        for (Result r : stats.results()) {
            switch (r.getStatistic()) {
                case MIN:
                case MAX:
                    assertEquals(r.getImageBand(), r.getValue()-1, EPS);
                    break;

                case RANGE:
                    assertEquals(0.0, r.getValue(), EPS);
                    break;

                default:
                    fail("unexpected statistic: " + r.getStatistic());
            }

            assertEquals(2360, r.getNumNoData());
            assertEquals(992, r.getNumNaN());
            assertEquals(5832, r.getNumAccepted());
        }
    }

    private void assertSingleResult(RenderedOp op, Statistic stat, Double value) {
        ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        Result r = stats.band(0).zone(0).statistic(stat).results().get(0);
        assertEquals(stat, r.getStatistic());
        assertEquals(value, r.getValue(), EPS);
    }

    private static PlanarImage createConstantImage( Number[] bandValues ) {
        return createConstantImage(bandValues, new Point(0, 0));
    }

    private static PlanarImage createConstantImage( Number[] bandValues, Point origin ) {
        RenderingHints hints = null;
        if (origin != null && !(origin.x == 0 && origin.y == 0)) {
            ImageLayout layout = new ImageLayout(origin.x, origin.y, WIDTH, WIDTH);
            hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float) WIDTH);
        pb.setParameter("height", (float) WIDTH);
        pb.setParameter("bandValues", bandValues);
        
        if(hints!=null&&hints.containsKey(JAI.KEY_IMAGE_LAYOUT)){
        	
            final ImageLayout layout =(ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
            layout.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(128).setTileWidth(128);
        }

        return JAI.create("constant", pb, hints).getRendering();
    }

    private static PlanarImage createTwoValuesImage() {
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_INT, 128, 128, 1, 128,
                new int[]{0});

        TiledImage img = new TiledImage(0, 0, WIDTH, WIDTH, 0, 0, sm, null);

        WritableRectIter iter = RectIterFactory.createWritable(img, null);
        int index = 0;
        do {
            do {
                if (index < (WIDTH*WIDTH/2)) {
                    iter.setSample(1);
                }else{
                    iter.setSample(10);
                }
                index++;
            } while( !iter.nextPixelDone() );
            iter.startPixels();
        } while( !iter.nextLineDone() );

        return img;
    }

    private static PlanarImage createRandomImage( int min, int max ) {
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_INT, 128, 128, 1, 128,
                new int[]{0});

        TiledImage img = new TiledImage(0, 0, WIDTH, WIDTH, 0, 0, sm, null);

        WritableRectIter iter = RectIterFactory.createWritable(img, null);
        int range = max - min + 1;
        do {
            do {
                iter.setSample(rand.nextInt(range) + min);
            } while( !iter.nextPixelDone() );
            iter.startPixels();
        } while( !iter.nextLineDone() );

        return img;
    }

    /**
     * Create a 3 band image. Each band has an outer border of Double.NaN; an
     * inner border of -9999; and then interior values of band index + 1.
     */
    private static PlanarImage createMultibandImage() {
        final int numBands = 3;
        final int[] bandOffsets = {0, 1, 2};

        SampleModel sm = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_DOUBLE,
                TILE_WIDTH, TILE_WIDTH,
                numBands, numBands*TILE_WIDTH,
                bandOffsets);

        TiledImage img = new TiledImage(0, 0, WIDTH, HEIGHT, 0, 0, sm, null);

        WritableRectIter iter = RectIterFactory.createWritable(img, null);

        final double[] values = new double[numBands];
        int i = 0;
        do {
            int iToEdge = Math.min(i, WIDTH - i - 1);
            int j = 0;
            do {
                int jToEdge = Math.min(j, WIDTH - j - 1);

                for( int m = 0; m < numBands; m++ ) {
                    if (iToEdge < 5 || jToEdge < 5) {
                        values[m] = Double.NaN;
                    } else if (iToEdge < 10 || jToEdge < 10) {
                        values[m] = -9999.0;
                    } else
                        values[m] = m + 1.0;  // make band values different
                }
                iter.setPixel(values);
                j++;
            } while( !iter.nextPixelDone() );
            iter.startPixels();
            i++;
        } while( !iter.nextLineDone() );

        return img;
    }
    
    /**
     * Create a 3 band image. Each band has an outer border of Double.NaN; an
     * inner border of -9999; and then interior values of band index + 1.
     */
    private static PlanarImage createMultibandImageNoData() {
        final int numBands = 3;
        final int[] bandOffsets = {0, 1, 2};

        SampleModel sm = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_DOUBLE,
                TILE_WIDTH, TILE_WIDTH,
                numBands, numBands*TILE_WIDTH,
                bandOffsets);

        TiledImage img = new TiledImage(0, 0, WIDTH, HEIGHT, 0, 0, sm, null);

        WritableRectIter iter = RectIterFactory.createWritable(img, null);

        final double[] values = new double[numBands];
        int i = 0;
        do {
            int iToEdge = Math.min(i, WIDTH - i - 1);
            int j = 0;
            do {
                int jToEdge = Math.min(j, WIDTH - j - 1);

                for( int m = 0; m < numBands; m++ ) {
                    if (iToEdge < 4 || jToEdge < 4) {
                        values[m] = Double.NaN;
                    } else if (iToEdge < 10 || jToEdge < 10) {
                        values[m] = -9999.0;
                    } else
                        values[m] = m + 1.0;  // make band values different
                }
                iter.setPixel(values);
                j++;
            } while( !iter.nextPixelDone() );
            iter.startPixels();
            i++;
        } while( !iter.nextLineDone() );

        return img;
    }
}
