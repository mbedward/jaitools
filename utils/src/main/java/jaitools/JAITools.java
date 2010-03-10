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

package jaitools;

import java.util.Properties;

/**
 * This class contains a small number of general info and utility methods.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JAITools {

    /**
     * Gets the JAI-tools version string
     *
     * @todo use global version vars
     */
    public static final String getVersion() {
        return "1.0.0";
    }

    /**
     * Checks if we are running on OSX and, if we are, turns off
     * the broken native acceleration so that operators work properly
     * with float and double type images
     */
    public static void fixForOSX() {
        Properties sys = new Properties(System.getProperties());
        if (sys.getProperty("os.name").equalsIgnoreCase("mac os x")) {
            sys.put("com.sun.media.jai.disableMediaLib", "true");
        }
        System.setProperties(sys);
    }

}
