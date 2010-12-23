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

package jaitools.media.jai.vectorbinarize;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;

import jaitools.imageutils.PixelCoordType;
import jaitools.jts.CoordinateSequence2D;

import java.awt.Rectangle;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Map;

import javax.media.jai.PlanarImage;
import javax.media.jai.SourcelessOpImage;

/**
 * Creates a binary image based on tests of pixel inclusion in a polygonal {@code Geometry}.
 * See {@link VectorBinarizeDescriptor} for details.
 *
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @source $URL: https://jai-tools.googlecode.com/svn/trunk/operator/contour/src/main/java/jaitools/media/jai/contour/ContourDescriptor.java $
 * @version $Id: VectorBinarizeDescriptor.java 1256 2010-12-13 11:53:07Z michael.bedward $
 */
public class VectorBinarizeOpImage extends SourcelessOpImage {

    private final PreparedGeometry geom;
    private final PixelCoordType coordType;
    private final CoordinateSequence2D testPointCS;
    private final Point testPoint;
    private final CoordinateSequence2D testRectCS;
    private final Polygon testRect;

    public VectorBinarizeOpImage(SampleModel sm,
            Map configuration,
            int minX, int minY, int width, int height,
            PreparedGeometry geom,
            PixelCoordType coordType) {
        super(null, configuration, sm, minX, minY, width, height);

        this.geom = geom;
        this.coordType = coordType;

        GeometryFactory gf = new GeometryFactory();
        testPointCS = new CoordinateSequence2D(1);
        testPoint = gf.createPoint(testPointCS);

        testRectCS = new CoordinateSequence2D(5);
        testRect = gf.createPolygon(gf.createLinearRing(testRectCS), null);
    }

    @Override
    protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
        if (geomContainsRect(destRect)) {
            int[] data = new int[destRect.width * destRect.height];
            Arrays.fill(data, 1);
            dest.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, data);
            
        } else {
            double delta = (coordType == PixelCoordType.CENTER ? 0.5 : 0.0);
            for (int y = destRect.y, iy = 0; iy < destRect.height; y++, iy++) {
                testPointCS.setY(0, y + delta);
                for (int x = destRect.x, ix = 0; ix < destRect.width; x++, ix++) {
                    testPointCS.setX(0, x + delta);
                    testPoint.geometryChanged();
                    dest.setSample(x, y, 0, (geom.contains(testPoint) ? 1 : 0));
                }
            }
        }
    }

    private boolean geomContainsRect(Rectangle destRect) {
        final double delta = (coordType == PixelCoordType.CENTER ? 0.5 : 0.0);
        testRectCS.setXY(0, destRect.x + delta, destRect.y + delta);
        testRectCS.setXY(1, destRect.x + delta, destRect.y + destRect.height - delta);
        testRectCS.setXY(2, destRect.x + destRect.width - delta, destRect.y + destRect.height - delta);
        testRectCS.setXY(3, destRect.x + destRect.width - delta, destRect.y + delta);
        testRectCS.setXY(4, destRect.x + delta, destRect.y + delta);
        testRect.geometryChanged();

        return geom.contains(testRect);
    }
}
