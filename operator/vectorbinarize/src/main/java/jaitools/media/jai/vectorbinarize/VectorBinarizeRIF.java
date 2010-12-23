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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import jaitools.media.jai.vectorbinarize.VectorBinarizeDescriptor.CoordType;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

/**
 * The image factory for the VectorBinarize operator.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorBinarizeRIF implements RenderedImageFactory {

    public VectorBinarizeRIF() {
    }

    /**
     * Creates a new instance of VectorBinarizeOpImage in the rendered layer.
     *
     * @param paramBlock parameter block with parameters minx, miny, width
     *        height, geometry and coordtype
     *
     * @param renderHints optional rendering hints which may be used to pass an {@code ImageLayout}
     *        object containing a {@code SampleModel} to use when creating destination
     *        tiles
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        int minx = paramBlock.getIntParameter(VectorBinarizeDescriptor.MINX_ARG);
        int miny = paramBlock.getIntParameter(VectorBinarizeDescriptor.MINY_ARG);
        int width = paramBlock.getIntParameter(VectorBinarizeDescriptor.WIDTH_ARG);
        int height = paramBlock.getIntParameter(VectorBinarizeDescriptor.HEIGHT_ARG);
        
        Object obj = paramBlock.getObjectParameter(VectorBinarizeDescriptor.GEOM_ARG);
        PreparedGeometry pg = null;
        
        if (obj instanceof Polygonal) {
            // defensively copy the input Geometry
            Geometry g = (Geometry) ((Geometry)obj).clone();
            pg = PreparedGeometryFactory.prepare(g);
            
        } else if (obj instanceof PreparedGeometry) {
            pg = (PreparedGeometry) obj;
        }
        
        VectorBinarizeDescriptor.CoordType coordType = (CoordType) paramBlock.getObjectParameter(VectorBinarizeDescriptor.COORD_TYPE_ARG);
        
        SampleModel sm = null;
        
        if (renderHints != null && renderHints.containsKey(JAI.KEY_IMAGE_LAYOUT)) {
            ImageLayout il = (ImageLayout) renderHints.get(JAI.KEY_IMAGE_LAYOUT);
            if (il != null) {
                sm = il.getSampleModel(null);
            }
        }
        
        if (sm == null) {
            // use default SampleModel
            Dimension tsize = JAI.getDefaultTileSize();
            sm = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tsize.width, tsize.height, 1);
        }
        
        return new VectorBinarizeOpImage(sm, renderHints, minx, miny, width, height, pg, coordType);
    }
}
