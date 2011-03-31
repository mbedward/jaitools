/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.media.jai.kernel;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.media.jai.KernelJAI;

import static jaitools.numeric.CompareOp.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Some tests of KernelFactory methods
 *
 * @author Michael Bedward
 */
public class KernelFactoryTest {


    /**
     * Test the {@linkplain KernelFactory#createCircle }
     * method
     */
    @Test
    public void testCreateCircle() {
        System.out.println("createCircle");
        int radius = 3;
        KernelFactory.ValueType type = KernelFactory.ValueType.BINARY;
        float centreValue = 1.0F;
        float[] expData = {
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 1, 1, 1, 1, 0,
            0, 1, 1, 1, 1, 1, 0,
            1, 1, 1, 1, 1, 1, 1,
            0, 1, 1, 1, 1, 1, 0,
            0, 1, 1, 1, 1, 1, 0,
            0, 0, 0, 1, 0, 0, 0
        };

        KernelJAI kernel = KernelFactory.createCircle(radius, type, centreValue);

        float[] data = kernel.getKernelData();
        assertEquals(data.length, expData.length);

        KernelFactoryHelper kh = new KernelFactoryHelper();
        for (int i = 0; i < data.length; i++) {
            assertTrue(aequal(data[i], expData[i]));
        }
    }

    /**
     * Test the {@linkplain KernelFactory#createCircle }
     * method
     */
    @Test
    public void testCreateCircleInverseDistance() {
        System.out.println("createCircleInverseDistance");
        int radius = 3;
        KernelFactory.ValueType type = KernelFactory.ValueType.INVERSE_DISTANCE;
        float centreValue = 1.0F;

        KernelJAI kernel = KernelFactory.createCircle(radius, type, centreValue);

        float[] data = kernel.getKernelData();

        KernelFactoryHelper kh = new KernelFactoryHelper();
        float dist;
        int k = 0;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++, k++) {
                if (i==0 && j==0) {
                    assertTrue(aequal(data[k], centreValue));
                } else {
                    dist = (float) Math.sqrt(i * i + j * j);
                    if (dist <= (float)radius) {
                        assertTrue(aequal(data[k], 1.0f / dist));
                    } else {
                        assertTrue(aequal(data[k], 0.0f));
                    }
                }
            }
        }
    }

    /**
     * Test the {@linkplain KernelFactory#createAnnulus }
     * method
     */
    @Test
    public void testCreateAnnulus() {
        System.out.println("createAnnulus");

        KernelFactoryHelper kh = new KernelFactoryHelper();

        int outerRadius = 4;
        int innerRadius = 2;
        KernelFactory.ValueType type = KernelFactory.ValueType.BINARY;
        float centreValue = 0.0F;
        float[] expData = {
            0, 0, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 1, 1, 1, 1, 1, 0, 0,
            0, 1, 1, 1, 0, 1, 1, 1, 0,
            0, 1, 1, 0, 0, 0, 1, 1, 0,
            1, 1, 0, 0, 0, 0, 0, 1, 1,
            0, 1, 1, 0, 0, 0, 1, 1, 0,
            0, 1, 1, 1, 0, 1, 1, 1, 0,
            0, 0, 1, 1, 1, 1, 1, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0,};

        KernelJAI kernel = KernelFactory.createAnnulus(outerRadius, innerRadius, type, centreValue);

        float[] data = kernel.getKernelData();
        assertEquals(data.length, expData.length);

        for (int i = 0; i < data.length; i++) {
            assertTrue(aequal(data[i], expData[i]));
        }
    }

    @Test
    public void testCreateFromShape() {
        System.out.println("createFromShape");

        int radius = 5;
        Shape shape = new Ellipse2D.Float(100, 200, 2*radius, 2*radius);
        KernelJAI shpKernel = KernelFactory.createFromShape(shape, null, KernelFactory.ValueType.BINARY, radius, radius, 1.0f);
        KernelJAI circleKernel = KernelFactory.createCircle(radius);

        float[] shpData = shpKernel.getKernelData();
        float[] circleData = circleKernel.getKernelData();

        assertTrue(shpData.length == circleData.length);

        KernelFactoryHelper kh = new KernelFactoryHelper();
        for (int i = 0; i < shpData.length; i++) {
            assertTrue(aequal(shpData[i], circleData[i]));
        }
    }
}
