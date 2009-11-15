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
 * Exception thrown when by (@code DiskMemTileCache} when a an attempt
 * to cache a tile to disk has failed.
 *
 * @see DiskMemTileCache
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DiskCacheFailedException extends DiskMemCacheException {

    public DiskCacheFailedException(RenderedImage owner, int tileX, int tileY) {
        super(owner, tileX, tileY, "could not be written to disk");
    }

}
