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

package jaitools.media.jai.vectorize;

import com.vividsolutions.jts.geom.Polygon;

import jaitools.imageutils.ImageUtils;

import java.util.Collection;
import java.util.Collections;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.registry.RenderedRegistryMode;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL: $
 * @version $Id: $
 */
public class VectorizeTest {
    
    @BeforeClass
    public static void setup() {
        ensureRegistered();
    }

    @Test
    public void singleSmallRegion_WithOutside() {
        final int IMAGE_WIDTH = 10;
        final int OUTSIDE_WIDTH = 2;
        
        TiledImage img = ImageUtils.createConstantImage(
                IMAGE_WIDTH, IMAGE_WIDTH, Integer.valueOf(0));
        
        for (int y = OUTSIDE_WIDTH; y < IMAGE_WIDTH - OUTSIDE_WIDTH; y++) {
            for (int x = OUTSIDE_WIDTH; x < IMAGE_WIDTH - OUTSIDE_WIDTH; x++) {
                img.setSample(x, y, 0, 1);
            }
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", img);
        pb.setParameter("outsideValues", Collections.singleton(Integer.valueOf(0)));
        
        RenderedOp outImg = JAI.create("Vectorize", pb);
        Object prop = outImg.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);

        assertTrue(prop instanceof Collection);
        
        Collection coll = (Collection) prop;
        for (Object obj : coll) {
            assertTrue(obj instanceof Polygon);
        }
    }
    
    /**
     * Register the operator with JAI if it is not already registered
     */
    private static void ensureRegistered() {
        OperationRegistry reg = JAI.getDefaultInstance().getOperationRegistry();
        String[] names = reg.getDescriptorNames(RenderedRegistryMode.MODE_NAME);
        VectorizeDescriptor desc = new VectorizeDescriptor();
        String descName = desc.getName();
        for (String name : names) {
            if (descName.equalsIgnoreCase(name)) {
                return;
            }
        }

        VectorizeSpi spi = new VectorizeSpi();
        spi.updateRegistry(reg);
    }
    
}
