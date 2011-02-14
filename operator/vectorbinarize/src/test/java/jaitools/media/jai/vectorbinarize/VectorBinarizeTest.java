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

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import jaitools.jts.CoordinateSequence2D;

import java.awt.Dimension;
import java.awt.image.Raster;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the VectorBinarize operation.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @version $Id$
 */
public class VectorBinarizeTest {
    
    private static final GeometryFactory gf = new GeometryFactory();
    private static final int TILE_WIDTH = 8;
    
    WKTReader reader = new WKTReader(gf);
    
    @Before
    public void setup() {
        JAI.setDefaultTileSize(new Dimension(TILE_WIDTH, TILE_WIDTH));
    }
    
    @Test
    public void rectanglePolyAcrossTiles() throws Exception {
        final int margin = 3;
        final int Ntiles = 3;
        
        int minx = margin;
        int miny = minx;
        int maxx = TILE_WIDTH * Ntiles - 2*margin;
        int maxy = maxx;
        
        String wkt = String.format("POLYGON((%d %d, %d %d, %d %d, %d %d, %d %d))", 
                minx, miny,
                minx, maxy,
                maxx, maxy,
                maxx, miny,
                minx, miny);
                
        Polygon poly = (Polygon) reader.read(wkt);
        
        ParameterBlockJAI pb = new ParameterBlockJAI("VectorBinarize");
        pb.setParameter("width", Ntiles * TILE_WIDTH);
        pb.setParameter("height", Ntiles * TILE_WIDTH);
        pb.setParameter("geometry", poly);
        
        RenderedOp dest = JAI.create("VectorBinarize", pb);
        
        // uncomment for debugging purposes, remember to comment back before committing
        // ImageIO.write(dest, "png", new java.io.File("/tmp/binarized.png"));
        
        CoordinateSequence2D testPointCS = new CoordinateSequence2D(1);
        Point testPoint = gf.createPoint(testPointCS);
        
        for (int ytile = 0; ytile < Ntiles; ytile++) {
            for (int xtile = 0; xtile < Ntiles; xtile++) {
                Raster tile = dest.getTile(xtile, ytile);
                for (int y = tile.getMinY(), iy = 0; iy < tile.getHeight(); y++, iy++) {
                    testPointCS.setY(0, y);
                    for (int x = tile.getMinX(), ix = 0; ix < tile.getWidth(); x++, ix++) {
                        testPointCS.setX(0, x);
                        testPoint.geometryChanged();
                        int expected = poly.intersects(testPoint) ? 1 : 0;
                        assertEquals("Failed test at position " + x + ", " + y + ", " +
                        		"expected " + expected + " but got " + tile.getSample(x, y, 0), 
                        		expected, tile.getSample(x, y, 0));
                    }
                }
            }
        }
    }
}
