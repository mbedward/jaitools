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
package jaitools.media.jai.regionalize;

import jaitools.media.jai.regionalize.RegionRec;
import java.util.Iterator;
import java.util.List;
import javax.media.jai.ImageFunction;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Regionalize operator
 *
 * @author Michael Bedward
 */
public class TestRegionalize {

    private static final int DIAMONDS = 0;
    private static final int CHESSBOARD = 1;
    private int numRegions;

    @Test
    public void testRegionalizeDisjunct() throws Exception {
        System.out.println("   testing with disjunct raster regions");
        int width = 200, height = 200;
        PlanarImage img = createTestImage(DIAMONDS, width, height);
        RenderedOp op = RegionalizeDescriptor.create(img, 0, 0d, false, null);

        boolean[] found = new boolean[numRegions + 1];
        for (int i = 0; i <= numRegions; i++) {
            found[i] = false;
        }

        RectIter iter = RectIterFactory.create(op, null);
        do {
            do {
                int id = iter.getSample(0);
                if (id < 1 || id > numRegions) {
                    fail("region ID out of range");
                }
                found[id] = true;
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());

        for (int i = 1; i <= numRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }
    }

    @Test
    public void testRegionalizeDiagonal() throws Exception {
        System.out.println("   testing with chessboard pattern");
        int width = 80, height = 80, squareW = 10;
        int expOrthoRegions = 64;
        int expDiagRegions = 2;

        PlanarImage img = createTestImage(CHESSBOARD, width, height, squareW);

        /*
         * First test orthogonal connectedness.
         */
        System.out.println("      - orthogonal connectedness");
        RenderedOp op = RegionalizeDescriptor.create(img, 0, 0d, false, null);

        boolean[] found = new boolean[expOrthoRegions + 1];
        for (int i = 0; i <= expOrthoRegions; i++) {
            found[i] = false;
        }

        RectIter iter = RectIterFactory.create(op, null);
        do {
            do {
                int id = iter.getSample(0);
                if (id < 1 || id > expOrthoRegions) {
                    fail("region ID out of range");
                }
                found[id] = true;
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());

        for (int i = 1; i <= expOrthoRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }

        /*
         * Now test diagonal connectedness. This should reduce
         * the chessboard pattern to two regions
         */
        System.out.println("      - diagonal connectedness");
        op = RegionalizeDescriptor.create(img, 0, 0d, true, null);

        found = new boolean[expDiagRegions + 1];
        for (int i = 0; i <= expDiagRegions; i++) {
            found[i] = false;
        }

        iter = RectIterFactory.create(op, null);
        do {
            do {
                int id = iter.getSample(0);
                if (id < 1 || id > expDiagRegions) {
                    fail("region ID out of range");
                }
                found[id] = true;
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());

        for (int i = 1; i <= expDiagRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }
    }

    @Test
    public void testProperty() {
        System.out.println("   testing regiondata property");

        int width = 200, height = 200;
        PlanarImage img = createTestImage(DIAMONDS, width, height);
        RenderedOp op = RegionalizeDescriptor.create(img, 0, 0d, false, null);

        // here we are crudely forcing a rendering so that the
        // property will be available
        op.getData();

        RegionData regData = (RegionData) op.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);

        List<RegionRec> recs = regData.getData();
        assertTrue(recs.size() == numRegions);

        Iterator<RegionRec> iter = recs.iterator();
        for (int id = 1; id <= recs.size(); id++) {
            assertTrue(iter.hasNext());
            assertTrue(iter.next().getId() == id);
        }
    }

    private PlanarImage createTestImage(int pattern, int width, int height, int ...extras) {

        ImageFunction imageFn = null;
        switch (pattern) {
            case DIAMONDS:
                imageFn = new DiamondImageFunction(width, height);
                numRegions = ((DiamondImageFunction)imageFn).getNumDiamonds() + 1;
                break;

            case CHESSBOARD:
                imageFn = new ChessboardImageFunction(extras[0]);
                break;
        }


        ParameterBlockJAI pb = new ParameterBlockJAI("ImageFunction");
        pb.setParameter("function", imageFn);
        pb.setParameter("width", width);
        pb.setParameter("height", height);
        RenderedOp op = JAI.create("ImageFunction", pb);

        return op.getRendering();
    }
}
