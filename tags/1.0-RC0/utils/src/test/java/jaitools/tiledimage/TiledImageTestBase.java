/*
 * Copyright 2009 Michael Bedward
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

package jaitools.tiledimage;

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

/**
 * Base class for unit tests of DiskMemTilesImage
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public abstract class TiledImageTestBase {

    protected DiskMemImage image;

    protected DiskMemImage makeImage(int tileWidth, int xTiles, int yTiles) {
        ColorModel cm = ColorModel.getRGBdefault();
        SampleModel sm = cm.createCompatibleSampleModel(tileWidth, tileWidth);
        return new DiskMemImage(0, 0, tileWidth * xTiles, tileWidth * yTiles, 0, 0, sm, cm);
    }

}
