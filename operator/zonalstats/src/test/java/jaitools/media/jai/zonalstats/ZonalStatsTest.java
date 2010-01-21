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

import jaitools.numeric.DoubleComparison;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the ZonalStats operator
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsTest {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 300;
    private static final int MIN_DATUM = -5;
    private static final int MAX_DATUM = 5;
    private static RenderedImage dataImage;
    private static RenderedImage constant1Image;
    private static RenderedImage twoValueImage;
    private static RenderedImage multibandImage;
    private static Random rand = new Random();

    @BeforeClass
    public static void setup() {
        dataImage = createRandomImage(MIN_DATUM, MAX_DATUM);
        multibandImage = createMultibandImage();
        constant1Image = createConstantImage(new Double[]{1.0});
        twoValueImage = createTwoValuesImage();
    }

    @Test
    public void testValidateNumSources() {
        System.out.println("   validate number of sources");

        boolean gotException = false;
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
            gotException = true;
        }
        assertTrue("Failed validation of no sources", gotException);

        gotException = false;
        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
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
        System.out.println("   validate zone image type");
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Float[]{0f}));
        boolean gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
            gotException = true;
        }
        assertTrue("Failed to reject non-integral zone image", gotException);

        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}));
        gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
            gotException = true;
        }
        assertFalse("Failed to accept integral zone image", gotException);
    }

    @Test
    public void testZoneImageOverlap() {
        System.out.println("   validate data - zone image overlap");
        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}, new Point(WIDTH, WIDTH)));
        boolean gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            gotException = true;
        }
        assertTrue("Failed to reject zone image that does not overlap when transformed",
                gotException);

        pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", createConstantImage(new Integer[]{0}, new Point(WIDTH / 2,
                WIDTH / 2)));
        gotException = false;
        try {
            JAI.create("ZonalStats", pb);
        } catch (Exception ex) {
            gotException = true;
        }
        assertFalse("Failed to accept partially overlapping zone image", gotException);
    }

    @Test
    public void testMin() {
        System.out.println("   test min");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);
        assertTrue(stats.containsKey(Statistic.MIN));
        assertTrue(stats.get(Statistic.MIN).intValue() == MIN_DATUM);
    }

    @Test
    public void testMax() {
        System.out.println("   test max");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MAX});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);
        assertTrue(stats.containsKey(Statistic.MAX));
        assertTrue(stats.get(Statistic.MAX).intValue() == MAX_DATUM);
    }

    @Test
    public void testMean() {
        System.out.println("   test mean");

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
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);
        assertTrue(stats.containsKey(Statistic.MEAN));
        assertTrue(DoubleComparison.dequal(expMean, stats.get(Statistic.MEAN)));
    }

    @Test
    public void testStdDev() {
        System.out.println("   test standard deviation");

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
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);
        assertTrue(stats.containsKey(Statistic.SDEV));
        assertTrue(DoubleComparison.dequal(expSD, stats.get(Statistic.SDEV)));
    }

    @Test
    public void testRange() {
        System.out.println("   test range");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.RANGE});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);

        assertTrue(stats.containsKey(Statistic.RANGE));
        assertTrue(stats.get(Statistic.RANGE).intValue() == MAX_DATUM - MIN_DATUM);
    }

    @Test
    public void testExactMedian() {
        System.out.println("   test exact median");

        final int expMedian = 0;

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MEDIAN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);

        assertTrue(stats.containsKey(Statistic.MEDIAN));
        assertTrue(stats.get(Statistic.MEDIAN).intValue() == expMedian);
    }

    @Test
    public void testApproxMedian() {
        System.out.println("   test approximate median");

        final int expMedian = 0;

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", dataImage);
        pb.setParameter("stats", new Statistic[]{Statistic.APPROX_MEDIAN});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);

        assertTrue(stats.containsKey(Statistic.APPROX_MEDIAN));
        assertTrue(stats.get(Statistic.APPROX_MEDIAN).intValue() == expMedian);
    }

    @Test
    public void testMultiband() {
        System.out.println("   test multiband");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", multibandImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.MEAN,
                Statistic.RANGE});

        // make the test a bit more testing by skipping a band
        pb.setParameter("bands", new Integer[]{0, 2});

        RenderedOp op = JAI.create("ZonalStats", pb);

        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);

        ZonalStats zonalStats0 = result.get(0);
        Map<Statistic, Double> stats0 = zonalStats0.getZoneStats(0);

        ZonalStats zonalStats2 = result.get(2);
        Map<Statistic, Double> stats2 = zonalStats2.getZoneStats(0);

        assertTrue(stats0.containsKey(Statistic.MIN));
        assertTrue(stats0.get(Statistic.MIN).doubleValue() == -9999.0);
        assertTrue(stats2.containsKey(Statistic.MIN));
        assertTrue(stats2.get(Statistic.MIN).doubleValue() == -9999.0);
        assertTrue(stats0.containsKey(Statistic.MAX));
        System.out.println("max band 0: " + stats0.get(Statistic.MAX).doubleValue());
        assertTrue(stats0.get(Statistic.MAX).doubleValue() == 1.0);
        assertTrue(stats2.containsKey(Statistic.MAX));
        System.out.println("max band 2: " + stats2.get(Statistic.MAX).doubleValue());
        assertTrue(stats2.get(Statistic.MAX).doubleValue() == 3.0);
        assertTrue(stats0.containsKey(Statistic.RANGE));
        assertTrue(stats0.get(Statistic.RANGE).doubleValue() == 10000.0);
        assertTrue(stats2.containsKey(Statistic.RANGE));
        assertTrue(stats2.get(Statistic.RANGE).doubleValue() == 10002.0);
    }
    
    @Test
    public void testSum() {
        System.out.println("   test sum");

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
        pb.setSource("dataImage", constant1Image);
        pb.setParameter("stats", new Statistic[]{Statistic.SUM});
        RenderedOp op = JAI.create("ZonalStats", pb);
        Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
                .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        ZonalStats zonalStats = result.get(0);
        Map<Statistic, Double> stats = zonalStats.getZoneStats(0);
        assertTrue(stats.containsKey(Statistic.SUM));
        assertTrue(stats.get(Statistic.SUM).intValue() == (1*WIDTH*WIDTH));
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
        return JAI.create("constant", pb, hints).getRendering();
    }

    private static PlanarImage createTwoValuesImage() {
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_INT, WIDTH, WIDTH, 1, WIDTH,
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
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_INT, WIDTH, WIDTH, 1, WIDTH,
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

    private static PlanarImage createMultibandImage() {
        final int numBands = 3;
        final int[] bandOffsets = {0, 1, 2};

        SampleModel sm = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_DOUBLE,
                WIDTH, WIDTH,
                numBands, numBands*WIDTH,
                bandOffsets);

        TiledImage img = new TiledImage(0, 0, WIDTH, WIDTH, 0, 0, sm, null);

        WritableRectIter iter = RectIterFactory.createWritable(img, null);

        final double[] values = new double[numBands];
        int i = 0;
        do {
            int j = 0;
            do {
                for( int m = 0; m < numBands; m++ ) {
                    if (i < 2 && j < 2) {
                        values[m] = Double.NaN;
                    } else if (i > 10 && j > 7) {
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
