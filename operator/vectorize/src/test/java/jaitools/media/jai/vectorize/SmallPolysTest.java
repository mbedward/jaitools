/*
 * Copyright 2011 Michael Bedward
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

package jaitools.media.jai.vectorize;

import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.media.jai.RenderedOp;

import com.vividsolutions.jts.geom.Polygon;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for filtering small polygons.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SmallPolysTest extends TestBase {
    
    private static final Integer[] DATA = {
        1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1,
        1, 1, 1, 3, 1, 1,
        2, 2, 3, 4, 3, 2,
        2, 2, 2, 3, 2, 2,
        2, 2, 2, 2, 2, 2
    };
    
    private static final int WIDTH = 6;
    
    private Map<String, Object> args;
    
    @Before
    public void setup() {
        args = CollectionFactory.map();
    }

    @Test
    public void removeSmallPolys() throws Exception {
        System.out.println("   remove small polygons");
        RenderedImage img = ImageUtils.createImageFromArray(DATA, WIDTH, WIDTH);
        
        args.put("filterThreshold", 1.1);
        args.put("filterMethod", VectorizeDescriptor.FILTER_DELETE);
        RenderedOp dest = doOp(img, args);
        getPolygons(dest, 2);
    }
    
    
    @Test
    public void mergeSmallPolysIntoLargestNbr() throws Exception {
        System.out.println("   merge small polygons into largest neighbour");
        RenderedImage img = ImageUtils.createImageFromArray(DATA, WIDTH, WIDTH);
        
        args.put("filterThreshold", 1.1);
        args.put("filterMethod", VectorizeDescriptor.FILTER_MERGE_LARGEST);
        RenderedOp dest = doOp(img, args);
        List<Polygon> polygons = getPolygons(dest, 2);

        int[] ids = new int[2];
        int[] areas = new int[2];
        int k = 0;
        for (Polygon poly : polygons) {
            ids[k] = ((Number) poly.getUserData()).intValue();
            areas[k] = (int) poly.getArea();
            k++ ;
        }
        
        /*
         * Depending on the order in which small polys were
         * processed, poly 1 area could have increased by
         * 4 or 5 while poly 2 area could have increased by
         * 1 or remained unchanged
         */
        Map<Integer, Integer> startAreas = getStartAreas();
        
        for (int i = 0; i < 2; i++) {
            int startArea = startAreas.get(ids[i]);
            int diff = areas[i] - startArea;
            switch (ids[i]) {
                case 1:
                    assertTrue(diff == 4 || diff == 5);
                    break;

                case 2:
                    assertTrue(diff == 0 || diff == 1);
                    break;

                default:
                    fail("Unexpected poly id");
            }
        }
    }
    
    @Test
    public void smallIslandPoly() throws Exception {
        System.out.println("   small polygon surrounded by nodata");
        
        // Set 3 to nodata so that 4 will be an island poly
        args.put("outsideValues", Collections.singleton(3d));
        args.put("filterThreshold", 1.1);
        args.put("filterMethod", VectorizeDescriptor.FILTER_MERGE_RANDOM);
        
        RenderedImage img = ImageUtils.createImageFromArray(DATA, WIDTH, WIDTH);
        RenderedOp dest = doOp(img, args);
        
        // There should only be 2 polys because the island has no
        // shared boundary segments
        getPolygons(dest, 2);
    }
    
    @Test
    public void thresholdBelowMinPolySize() throws Exception {
        System.out.println("   filter threshold below min poly size");
        
        args.put("filterThreshold", 0.5);
        args.put("filterMethod", VectorizeDescriptor.FILTER_DELETE);
        
        RenderedImage img = ImageUtils.createImageFromArray(DATA, WIDTH, WIDTH);
        RenderedOp dest = doOp(img, args);
        
        // There should be 7 polygons because none will have been filtered
        getPolygons(dest, 7);
    }
    
    private Map<Integer, Integer> getStartAreas() {
        Map<Integer, Integer> areas = CollectionFactory.map();
        
        int k = 0;
        Integer id, area;
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                id = DATA[k++];
                area = areas.get(id);
                if (area == null) area = 0;
                areas.put(id, area+1);
            }
        }
        
        return areas;
    }
    
}
