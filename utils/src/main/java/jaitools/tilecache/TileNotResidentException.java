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

package jaitools.tilecache;

import java.awt.image.RenderedImage;

/**
 * Exception thrown when by <code>DiskMemTileCache</code> when a tile was
 * not resident in cache memory when required to be.
 *
 * @see DiskMemTileCache
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class TileNotResidentException extends Exception {

    TileNotResidentException(RenderedImage owner, int tileX, int tileY) {
        super(String.format("tile at %d,%d of image %s is not in cache memory",
                tileX, tileY, owner.toString()));
    }

}
