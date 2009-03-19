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

import java.awt.image.Raster;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class TestRegionalize {

    private int numRegions;

    @Test
    public void testRegionalize() throws Exception {
        int width = 200, height = 200;
        PlanarImage img = createTestImage(width, height);
        RenderedOp op = RegionalizeDescriptor.create(img, 0, 0d, false, null);

        PlanarImage regionImg = op.createSnapshot();

        boolean[] found = new boolean[numRegions + 1];
        for (int i = 0; i <= numRegions; i++) {
            found[i] = false;
        }
        Raster data = regionImg.getData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int id = data.getSample(x, y, 0);
                if (id < 1 || id > numRegions) {
                    System.out.println("id = " + id);
                    fail("region ID out of range");
                }
                found[id] = true;
            }
        }

        for (int i = 1; i <= numRegions; i++) {
            assertTrue(String.format("missing region %d", i), found[i]);
        }
    }

    private PlanarImage createTestImage(int width, int height) {
        DiamondImageFunction dif = new DiamondImageFunction(width, height);

        numRegions = dif.getNumDiamonds() + 1;
        System.out.println("number of regions" + numRegions);

        ParameterBlockJAI pb = new ParameterBlockJAI("ImageFunction");
        pb.setParameter("function", dif);
        pb.setParameter("width", width);
        pb.setParameter("height", height);
        RenderedOp op = JAI.create("ImageFunction", pb);

        return op.getRendering();
    }
}
