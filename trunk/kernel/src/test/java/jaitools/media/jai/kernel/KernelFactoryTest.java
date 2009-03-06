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
package jaitools.media.jai.kernel;

import jaitools.media.jai.kernel.KernelUtil;
import jaitools.media.jai.kernel.KernelFactory.ValueType;
import javax.media.jai.KernelJAI;
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
        ValueType type = KernelFactory.ValueType.BINARY;
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
        System.out.println(KernelUtil.kernelToString(kernel, true));

        float[] data = kernel.getKernelData();
        assertEquals(data.length, expData.length);

        for (int i = 0; i < data.length; i++) {
            assertTrue(feq(data[i], expData[i]));
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
        ValueType type = KernelFactory.ValueType.INVERSE_DISTANCE;
        float centreValue = 1.0F;

        KernelJAI kernel = KernelFactory.createCircle(radius, type, centreValue);
        System.out.println(KernelUtil.kernelToString(kernel, true));

        float[] data = kernel.getKernelData();

        float dist;
        int k = 0;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++, k++) {
                if (i==0 && j==0) {
                    assertTrue(feq(data[k], centreValue));
                } else {
                    dist = (float) Math.sqrt(i * i + j * j);
                    if (dist <= (float)radius) {
                        assertTrue(feq(data[k], 1.0f / dist));
                    } else {
                        assertTrue(feq(data[k], 0.0f));
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

        int outerRadius = 3;
        int innerRadius = 2;
        ValueType type = KernelFactory.ValueType.BINARY;
        float centreValue = 0.0F;
        float[] expData = {
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 1, 0, 1, 1, 0,
            0, 1, 0, 0, 0, 1, 0,
            1, 0, 0, 0, 0, 0, 1,
            0, 1, 0, 0, 0, 1, 0,
            0, 1, 1, 0, 1, 1, 0,
            0, 0, 0, 1, 0, 0, 0
        };

        KernelJAI kernel = KernelFactory.createAnnulus(outerRadius, innerRadius, type, centreValue);
        System.out.println(KernelUtil.kernelToString(kernel, true));

        float[] data = kernel.getKernelData();
        assertEquals(data.length, expData.length);

        for (int i = 0; i < data.length; i++) {
            assertTrue(feq(data[i], expData[i]));
        }
    }

    private boolean feq(float f1, float f2) {
        return Math.abs(f1 - f2) < 1.0e-8f;
    }

}