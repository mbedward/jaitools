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
package jaitools.media.jai.kernelfactory;

import javax.media.jai.KernelJAI;

/**
 * A factory class with static methods to create a variety of
 * KernelJAI configurations
 *
 * @author Michael Bedward
 *
 */
public class KernelFactory {

    /**
     * Meaning of the kernel values
     */
    public static enum ValueType {

        /**
         * Simple binary kernel with values of 1 or 0
         */
        BINARY,
        /**
         * The value of each kernel element is its distance to the
         * kernel's key element (the element that is placed over
         * image pixels during kernel operations).
         */
        DISTANCE,
        /**
         * The value of each kernel element is the inverse distance to the
         * kernel's key element (the element that is placed over
         * image pixels during kernel operations).
         */
        INVERSE_DISTANCE;
    };

    /**
     * Creates a new KernelJAI object with a pseudo-circular configuration.
     * The kernel width is 2*radius + 1.
     * The kernel's key element is at position x=radius, y=radius
     *
     * @param radius the radius of the circle expressed in pixels
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param centreValue the value to assign to the kernel centre (key element)
     * 
     * @return a new instance of KernelJAI
     */
    public static KernelJAI createCircle(int radius, ValueType type, float centreValue) {

        if (radius <= 0) {
            throw new IllegalArgumentException(
                    "Invalid radius (" + radius + "); must be > 0");
        }

        int width = 2 * radius + 1;
        float[] weights = new float[width * width];

        int r2 = radius * radius;
        int k0 = 0;
        int k1 = weights.length - 1;

        for (int y = radius; y > 0; y--) {
            int y2 = y * y;
            for (int x = -radius; x <= radius; x++, k0++, k1--) {
                float dist2 = x * x + y2;
                float value = 0f;

                if (fcomp(r2, dist2) >= 0) {
                    if (type == ValueType.DISTANCE) {
                        value = (float) Math.sqrt(dist2);
                    } else if (type == ValueType.INVERSE_DISTANCE) {
                        value = 1.0f / (float) Math.sqrt(dist2);
                    } else {
                        value = 1.0f;
                    }

                    weights[k0] = weights[k1] = value;
                }
            }
        }

        for (int x = -radius; x <= radius; x++, k0++) {
            float value;
            if (x == 0) {
                value = centreValue;
            } else {
                if (type == ValueType.DISTANCE) {
                    value = (float) Math.sqrt(x * x);
                } else if (type == ValueType.INVERSE_DISTANCE) {
                    value = 1.0f / (float) Math.sqrt(x * x);
                } else {
                    value = 1.0f;
                }
            }
            weights[k0] = value;
        }

        return new KernelJAI(width, width, weights);
    }

    /**
     * Creates a new KernelJAI object with an annular configuration
     * (like a doughnut).
     *
     * The kernel width is 2*outerRadius + 1.
     * The kernel's key element is at position x=outerRadius, y=outerRadius
     *
     * <p>
     * An IllegalArgumentException will be thrown if:
     * <ul>
     * <li> The value of outerRadius not greater than 0
     * <li> The value of innerRadius is not less than outerRadius
     * </ul>
     *
     * Calling this method with innerRadius == 0 is equivalent to
     * calling {@linkplain #createCircle }
     *
     * @param outerRadius the radius of the circle expressed in pixels
     * @param innerRadius the radius of the 'hole' of the annulus
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param centreValue the value to assign to the kernel centre (key element)
     *
     * @return a new instance of KernelJAI
     *
     */
    public static KernelJAI createAnnulus(int outerRadius, int innerRadius, ValueType type, float centreValue) {

        if (innerRadius < 0) {
            throw new IllegalArgumentException(
                    "Invalid innerRadius (" + innerRadius + "); must be >= 0");
        }

        if (outerRadius <= innerRadius) {
            throw new IllegalArgumentException("outerRadius must be greater than innerRadius");
        }

        if (innerRadius == 0) {
            return createCircle(outerRadius, type, centreValue);
        }

        int width = 2 * outerRadius + 1;
        float[] weights = new float[width * width];

        int outer2 = outerRadius * outerRadius;
        int inner2 = innerRadius * innerRadius;
        int k0 = 0;
        int k1 = weights.length - 1;

        for (int y = outerRadius; y > 0; y--) {
            int y2 = y * y;
            for (int x = -outerRadius; x <= outerRadius; x++, k0++, k1--) {
                float dist2 = x * x + y2;
                float value = 0f;

                if (fcomp(dist2, outer2) <= 0 && fcomp(dist2, inner2) > 0) {
                    if (type == ValueType.DISTANCE) {
                        value = (float) Math.sqrt(dist2);
                    } else if (type == ValueType.INVERSE_DISTANCE) {
                        value = 1.0f / (float) Math.sqrt(dist2);
                    } else {
                        value = 1.0f;
                    }

                    weights[k0] = weights[k1] = value;
                }
            }
        }

        for (int x = -outerRadius; x <= outerRadius; x++, k0++) {
            float value = 0f;
            if (x == 0) {
                value = centreValue;

            } else if (x < -innerRadius || x > innerRadius) {

                if (type == ValueType.DISTANCE) {
                    value = (float) Math.sqrt(x * x);
                } else if (type == ValueType.INVERSE_DISTANCE) {
                    value = 1.0f / (float) Math.sqrt(x * x);
                } else {
                    value = 1.0f;
                }
            }
            
            weights[k0] = value;
        }

        return new KernelJAI(width, width, weights);
    }

    /**
     * A utility function that returns a string representation of
     * a KernelJAI object's data.
     *
     * @param kernel the input kernel
     * @param multiLine if true, each row of kernel data is followed by a newline;
     * if false, the string contains no newlines
     *
     * @return a String
     */
    public static String kernelToString(KernelJAI kernel, boolean multiLine) {
        float[] data = kernel.getKernelData();
        int w = kernel.getWidth();
        int h = kernel.getHeight();
        StringBuilder sb = new StringBuilder();

        boolean binaryData = true;
        for (int i = 0; i < data.length && binaryData; i++) {
            if (!(feq(data[i], 0) || feq(data[i], 1))) {
                binaryData = false;
            }
        }

        int k = 0;
        sb.append("[");
        for (int i = 0; i < w; i++) {
            sb.append("[");
            for (int j = 0; j < h; j++, k++) {
                if (binaryData) {
                    sb.append((int) data[k]);
                } else {
                    sb.append(String.format("%.4g", data[k]));
                    if (j < w-1) sb.append(" ");
                }
            }
            sb.append("]");
            if (i < w - 1) {
                if (multiLine) {
                    sb.append("\n ");
                } else {
                    sb.append(" ");
                }
            }
        }
        sb.append("]");

        return sb.toString();
    }

    // round-off tolerance: used in the fcomp method below
    private static final float TOL = 1.0e-8f;

    /**
     * Equivalent to {@linkplain java.lang.Float#compare(float, float) } but
     * with a round-off tolerance
     * @param f1 first value
     * @param f2 second value
     * @return 0 if f1 and f2 are equal; less than 0 if f1 is less than f2;
     * greater than 0 if f1 is greater than f2
     */
    private static int fcomp(float f1, float f2) {
        if (Math.abs(f1 - f2) < TOL) {
            return 0;
        } else {
            return Float.compare(f1, f2);
        }
    }

    /**
     * Test if two float values are equal, taking into accont a
     * round-off tolerance
     * @param f1 first value
     * @param f2 second value
     * @return true if equal, false otherwise
     */
    private static boolean feq(float f1, float f2) {
        return Math.abs(f1 - f2) < TOL;
    }
}
