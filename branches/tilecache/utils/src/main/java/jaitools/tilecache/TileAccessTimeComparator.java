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

import java.util.Comparator;
import javax.media.jai.CachedTile;

/**
 * This is the default comparator for CachedTile objects used by
 * {@linkplain DiskMemTileCache} to determine priority of tiles for
 * storage in memory. It orders tiles from most recent to least
 * recent access time.
 * 
 * @author Michael Bedward
 * @since 1.0
 * $Id$
 */
public class TileAccessTimeComparator implements Comparator<CachedTile> {

    public int compare(CachedTile t1, CachedTile t2) {
        long time1 = t1.getTileTimeStamp();
        long time2 = t2.getTileTimeStamp();

        // t1 > t2 if time1 < time2
        return (time1 > time2 ? -1 : (time1 == time2 ? 0 : 1));
    }

}
