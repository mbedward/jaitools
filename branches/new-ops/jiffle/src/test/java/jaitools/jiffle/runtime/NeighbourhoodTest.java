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

package jaitools.jiffle.runtime;

import jaitools.jiffle.Jiffle;
import jaitools.utils.CollectionFactory;
import jaitools.utils.ImageUtils;
import java.util.Map;
import javax.media.jai.TiledImage;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the neighbourhood accessor: imgVar[xOffset, yOffset]
 * @author Michael Bedward
 */
public class NeighbourhoodTest {

    @Test
    public void testNeighbourhood() throws Exception {
        int width = 10, height = 10;
        
        TiledImage testImg = createTestImage(width, height);
        
        TiledImage tImg = ImageUtils.createDoubleImage(width, height);
        Map<String, TiledImage> imgParams = CollectionFactory.newMap();
        imgParams.put("testImg", testImg);
        imgParams.put("result", tImg);
        
        String prog = "result = testImg[0,-1] + " +
                      "testImg[0, 1] + testImg[-1,0] + testImg[1,0]";
        
        Jiffle jiffle = new Jiffle(prog, imgParams);
        if (!jiffle.isCompiled()) {
            throw new RuntimeException("couldn't compile script for neighbourhood test");
        }
       
        JiffleRunner runner = new JiffleRunner(jiffle);
        boolean completed = runner.run();
        
        assertTrue(completed);
        
        for (int y = 0; y < height; y++) {
            double above = (y == 0 ? Double.NaN : y-1);
            double below = (y == height-1 ? Double.NaN : y+1);

            for (int x = 0; x < width; x++) {
                double val = tImg.getSampleDouble(x, y, 0);
                double left = (x == 0 ? Double.NaN : y);
                double right = (x == width-1 ? Double.NaN : y);
                //System.out.println("" + val + " " + above + " " + below + " " + left + " " + right);
                assertEquals(val, above + below + left + right);
            }
        }
    }
    
    /**
     * Create a test image where pixel value == row index (from 0)
     */
    private TiledImage createTestImage(int width, int height) throws Exception {
        TiledImage tImg = ImageUtils.createDoubleImage(width, height);
        Map<String, TiledImage> imgParams = CollectionFactory.newMap();
        imgParams.put("result", tImg);
        
        Jiffle jiffle = new Jiffle("result = y()", imgParams);
        if (!jiffle.isCompiled()) {
            throw new RuntimeException("couldn't compile script to create test image");
        }
        
        JiffleRunner runner = new JiffleRunner(jiffle);
        runner.run();
        
        return tImg;
    }
}
