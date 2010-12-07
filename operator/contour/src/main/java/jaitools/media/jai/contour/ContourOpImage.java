/*
 * Copyright 2010 Michael Bedward
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
package jaitools.media.jai.contour;

import jaitools.media.jai.AttributeOpImage;
import java.awt.image.RenderedImage;
import java.util.SortedSet;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * Generate smooth contours from a source image.
 * <p>
 * The interpolation algorithm used is that of Paul Bourke. Originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN.
 * <p>
 * The implementation here is adapted from Paul Bourke's C code for the
 * CONRC algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourOpImage extends AttributeOpImage {

    private static final int TL = 1;
    private static final int TR = 2;
    private static final int BL = 3;
    private static final int BR = 4;
    private int band;
    private SortedSet<Double> contourIntervals;

    public ContourOpImage(RenderedImage source, ROI roi) {
        super(source, roi);
    }

    @Override
    protected Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String[] getAttributeNames() {
        return new String[]{ContourDescriptor.CONTOUR_PROPERTY_NAME};
    }

    private void traceContours() {
        double[] sample = new double[4];
        double[] h = new double[5];
        double[] xh = new double[5];
        double[] yh = new double[5];
        int[] sh = new int[5];
        double temp1, temp2;

        int[][][] castab = {
            {{0, 0, 8}, {0, 2, 5}, {7, 6, 9}},
            {{0, 3, 4}, {1, 3, 1}, {4, 3, 0}},
            {{9, 6, 7}, {5, 2, 0}, {8, 0, 0}}
        };


        RandomIter iter = RandomIterFactory.create(getSourceImage(0), srcBounds);

        for (int y = srcBounds.y, iy = 0; iy < srcBounds.height - 1; y++, iy++) {
            sample[BR] = iter.getSampleDouble(srcBounds.x, y, band);
            sample[TR] = iter.getSampleDouble(srcBounds.x, y + 1, band);

            for (int x = srcBounds.x + 1, ix = 0; ix < srcBounds.width; x++, ix++) {
                sample[BL] = sample[BR];
                sample[BR] = iter.getSampleDouble(x, y, band);
                sample[TL] = sample[TR];
                sample[TR] = iter.getSampleDouble(x, y + 1, band);

                temp1 = Math.min(sample[BL], sample[TL]);
                temp2 = Math.min(sample[BR], sample[TR]);
                double dmin = Math.min(temp1, temp2);

                temp1 = Math.max(sample[BL], sample[TL]);
                temp2 = Math.max(sample[BR], sample[TR]);
                double dmax = Math.max(temp1, temp2);

                if (dmax < contourIntervals.first() || dmin > contourIntervals.last()) {
                    continue;
                }

                // im = 0,1,1,0
                // jm = 0,0,1,1
                for (Double z : contourIntervals) {
                    if (z < dmin || z > dmax) {
                        continue;
                    }

                    h[4] = sample[TL] - z;
                    xh[4] = x - 1;
                    yh[4] = y + 1;
                    sh[4] = Double.compare(h[4], 0.0);

                    h[3] = sample[TR] - z;
                    xh[3] = x;
                    yh[3] = y + 1;
                    sh[3] = Double.compare(h[3], 0.0);

                    h[2] = sample[BR] - z;
                    xh[2] = x;
                    yh[2] = y;
                    sh[2] = Double.compare(h[2], 0.0);

                    h[1] = sample[BL] - z;
                    xh[1] = x - 1;
                    yh[1] = y;
                    sh[1] = Double.compare(h[1], 0.0);

                    h[0] = (h[1] + h[2] + h[3] + h[4]) / 4.0;
                    xh[0] = x - 0.5;
                    yh[0] = y + 0.5;
                    sh[0] = Double.compare(h[0], 0.0);

                    /* Scan each triangle in the box */
                    int m1, m2, m3;
                    for (int m = 1; m <= 4; m++) {
                        m1 = m;
                        m2 = 0;
                        if (m != 4) {
                            m3 = m + 1;
                        } else {
                            m3 = 1;
                        }

                        int config = castab[sh[m1] + 1][sh[m2] + 1][sh[m3] + 1];
                        if (config == 0) {
                            continue;
                        }

                        double x1 = 0.0, y1 = 0.0, x2 = 0.0, y2 = 0.0;
                        switch (config) {
                            case 1: /* Line between vertices 1 and 2 */
                                x1 = xh[m1];
                                y1 = yh[m1];
                                x2 = xh[m2];
                                y2 = yh[m2];
                                break;
                                
                            case 2: /* Line between vertices 2 and 3 */
                                x1 = xh[m2];
                                y1 = yh[m2];
                                x2 = xh[m3];
                                y2 = yh[m3];
                                break;
                                
                            case 3: /* Line between vertices 3 and 1 */
                                x1 = xh[m3];
                                y1 = yh[m3];
                                x2 = xh[m1];
                                y2 = yh[m1];
                                break;
                                
                            case 4: /* Line between vertex 1 and side 2-3 */
                                x1 = xh[m1];
                                y1 = yh[m1];
                                x2 = sect(m2, m3, h, xh);
                                y2 = sect(m2, m3, h, yh);
                                break;
                                
                            case 5: /* Line between vertex 2 and side 3-1 */
                                x1 = xh[m2];
                                y1 = yh[m2];
                                x2 = sect(m3, m1, h, xh);
                                y2 = sect(m3, m1, h, yh);
                                break;
                                
                            case 6: /* Line between vertex 3 and side 1-2 */
                                x1 = xh[m3];
                                y1 = yh[m3];
                                x2 = sect(m3, m2, h, xh);
                                y2 = sect(m3, m2, h, yh);
                                break;
                                
                            case 7: /* Line between sides 1-2 and 2-3 */
                                x1 = sect(m1, m2, h, xh);
                                y1 = sect(m1, m2, h, yh);
                                x2 = sect(m2, m3, h, xh);
                                y2 = sect(m2, m3, h, yh);
                                break;
                                
                            case 8: /* Line between sides 2-3 and 3-1 */
                                x1 = sect(m2, m3, h, xh);
                                y1 = sect(m2, m3, h, yh);
                                x2 = sect(m3, m1, h, xh);
                                y2 = sect(m3, m1, h, yh);
                                break;
                                
                            case 9: /* Line between sides 3-1 and 1-2 */
                                x1 = sect(m3, m1, h, xh);
                                y1 = sect(m3, m1, h, yh);
                                x2 = sect(m1, m2, h, xh);
                                y2 = sect(m1, m2, h, yh);
                                break;
                        }
                        
                        joinContour(x1, y1, x2, y2, z);

                    }
                }
            }
        }

    }

    private double sect(int p1, int p2, double[] h, double[] coord) {
        return (h[p2] * coord[p1] - h[p1] * coord[p2]) / (h[p2] - h[p1]);
    }

    private void joinContour(double x1, double y1, double x2, double y2, Double z) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
