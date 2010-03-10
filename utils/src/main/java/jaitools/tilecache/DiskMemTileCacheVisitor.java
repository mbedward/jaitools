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

/**
 * A visitor to collect information about tiles in a {@linkplain DiskMemTileCache}.
 * This can be used to examine cache performance in more detail than with
 * the cache's diagnostic methods and observer events.
 *
 * @see DiskMemTileCache
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public interface DiskMemTileCacheVisitor {

    /**
     * Called by the cache once for each tile 
     * 
     * @param tile the tile being visited
     * @param isResident set by the cache to indicate whether the tile is
     * currently resident in memory
     */
    public void visit(DiskCachedTile tile, boolean isResident);

}
