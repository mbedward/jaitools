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

package jaitools.jiffle.runtime;

import jaitools.imageutils.ImageUtils;
import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import java.util.Map;
import javax.media.jai.TiledImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests using a non-default base class for the Jiffle runtime class.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class CustomBaseClassTest {
    
    private final int WIDTH = 10;
    
    @Test
    public void customBaseClass() throws Exception {
        Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
        imageParams.put("out", Jiffle.ImageRole.DEST);
        
        Jiffle jiffle = new Jiffle("out = x() + y()", imageParams);
        JiffleDirectRuntime jr = (JiffleDirectRuntime) jiffle.getRuntimeInstance(MockBaseClass.class);
        
        assertTrue(jr instanceof MockBaseClass);
        
        TiledImage img = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        jr.setDestinationImage("out", img);
        jr.evaluateAll(null);
        
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                assertEquals(x + y, img.getSample(x, y, 0));
            }
        }
    }
}
