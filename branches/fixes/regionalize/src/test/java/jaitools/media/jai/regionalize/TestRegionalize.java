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

import jaitools.tiledimage.DiskMemImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.List;
import javax.media.jai.ImageFunction;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Regionalize operator
 *
 * @author Michael Bedward
 */
public class TestRegionalize {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;
    private static final int SQUARE_WIDTH = 25;

    private static final int TILE_WIDTH = WIDTH / 2;
    private static final int TILE_HEIGHT = HEIGHT / 2;

    @Before
    public void setup() {
        assert(WIDTH % SQUARE_WIDTH == 0);
        assert(HEIGHT % SQUARE_WIDTH == 0);

        JAI.setDefaultTileSize(new Dimension(TILE_WIDTH, TILE_HEIGHT));
    }

    @Ignore
    @Test
    public void testOrthogonal() throws Exception {
        System.out.println("   testing orthogonal connectedness");

        int expOrthoRegions = (WIDTH / SQUARE_WIDTH) * (HEIGHT / SQUARE_WIDTH);

        RenderedImage img = createChessboardImage();

        ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", img);
        pb.setParameter("band", 0);
        pb.setParameter("tolerance", 0.0d);
        pb.setParameter("diagonal", false);

        RenderedOp op = JAI.create("regionalize", pb);

        boolean[] found = new boolean[expOrthoRegions + 1];
        for (int i = 1; i <= expOrthoRegions; i++) {
            found[i] = false;
        }

        RectIter iter = RectIterFactory.create(op, null);
        do {
            do {
                int id = iter.getSample(0);
                if (id < 1 || id > expOrthoRegions) {
                    fail(String.format("region ID %d out of range", id));
                }
                found[id] = true;
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());

        for (int i = 1; i <= expOrthoRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }
    }

    @Ignore
    @Test
    public void testDiagonal() {
        System.out.println("   testing diagonal connectedness");

        int expNumRegions = 2;

        RenderedImage img = createChessboardImage();

        ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", img);
        pb.setParameter("band", 0);
        pb.setParameter("tolerance", 0.0d);
        pb.setParameter("diagonal", true);
        RenderedOp op = JAI.create("regionalize", pb);

        boolean[] found = new boolean[expNumRegions + 1];
        for (int i = 1; i <= expNumRegions; i++) {
            found[i] = false;
        }

        RectIter iter = RectIterFactory.create(op, null);
        do {
            do {
                int id = iter.getSample(0);
                if (id < 1 || id > expNumRegions) {
                    fail(String.format("region ID %d out of range", id));
                }
                found[id] = true;
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());

        for (int i = 1; i <= expNumRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }
    }

    @Ignore
    @Test
    public void testProperty() {
        System.out.println("   testing regiondata property");

        RenderedImage img = createChessboardImage();

        ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", img);
        pb.setParameter("band", 0);
        pb.setParameter("tolerance", 0.0d);
        pb.setParameter("diagonal", false);

        RenderedOp op = JAI.create("regionalize", pb);

        // force rendering so the property will be available
        op.getData();
        List<Region> recs = (List<Region>) op.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);

        int numRegions = (WIDTH / SQUARE_WIDTH) * (HEIGHT / SQUARE_WIDTH);
        boolean[] found = new boolean[numRegions + 1];

        for (Region r : recs) {
            assertFalse(found[r.getId()]);
            found[r.getId()] = true;
        }

        for (int i = 1; i < found.length; i++) {
            assertTrue(found[i]);
        }
    }

    @Ignore
    @Test
    public void testURegion() {
        System.out.println("   testing image with U-shaped region");

        RenderedImage img = createURegionImage();

        ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", img);
        pb.setParameter("band", 0);
        pb.setParameter("tolerance", 0.0d);
        pb.setParameter("diagonal", false);

        RenderedOp op = JAI.create("regionalize", pb);
        op.getData();

        List<Region> recs = (List<Region>) op.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);
        System.out.println("got " + recs.size() + " regions");

        assertTrue(true);
    }

    private RenderedImage createChessboardImage() {
        ImageFunction imageFn = new ChessboardImageFunction(SQUARE_WIDTH);

        ParameterBlockJAI pb = new ParameterBlockJAI("ImageFunction");
        pb.setParameter("function", imageFn);
        pb.setParameter("width", WIDTH);
        pb.setParameter("height", HEIGHT);
        RenderedOp op = JAI.create("ImageFunction", pb);

        return op;
    }

    /**
     * Create a tiled image that has a U shaped region within it that crosses
     * tile boundaries.
     *
     * @return new image
     */
    private DiskMemImage createURegionImage() {
        ColorModel cm = ColorModel.getRGBdefault();
        SampleModel sm = cm.createCompatibleSampleModel(TILE_WIDTH, TILE_HEIGHT);
        DiskMemImage img = new DiskMemImage(WIDTH, HEIGHT, sm, cm);
        Graphics2D gr = img.createGraphics();

        gr.setColor(Color.GRAY);
        gr.fillRect(0, 0, WIDTH, HEIGHT);

        int w = TILE_WIDTH / 3;
        int h = TILE_HEIGHT / 3;

        gr.setColor(Color.CYAN);

        // draw the vertical parts of the U
        gr.fillRect(w, h, w, img.getHeight() - 2*h);
        gr.fillRect(img.getWidth() - 2*w, h, w, img.getHeight() - 2*h);

        // draw the horizontal part of the U
        gr.fillRect(w, img.getHeight() - 2*h, img.getWidth() - 2*w, h);

        return img;
    }
}

