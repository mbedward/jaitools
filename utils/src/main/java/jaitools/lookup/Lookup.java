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

package jaitools.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class offers minimal Service Provider Interface lookup functions.
 * Within jai-tools it is used to find implementations of non-operator interfaces
 * such as {@code jaitools.numeric.Processor}. Discovery of image operators
 * is done using the standard JAI mechanisms.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Lookup {

    private static final String prefix = "META-INF/services/";
    private static final Logger LOGGER = Logger.getLogger("jaitools.lookup");

    public static List<Class> getProviders(String spiName) {
        List providers = new ArrayList<Class>();

        ClassLoader cl = Lookup.class.getClassLoader();
        if (cl != null) {
            InputStream str = cl.getResourceAsStream(prefix + spiName);
            if (str != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(str));
                String line = null;
    
                try {
                    while ((line = reader.readLine()) != null) {
                        String text = line.trim();
                        if (text.length() > 0 && !text.startsWith("#")) {
                            try {
                                providers.add(Class.forName(text));
                            } catch (ClassNotFoundException ex) {
                                LOGGER.warning("Class not found: " + text);
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.severe("Problem reading services file: " + spiName);
                } finally {
                    
                    try {
                            str.close();
                    } catch (Throwable e) {
                        // ignore
                    }
                    
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Throwable e) {
                        // ignore
                    }    
                }
    
                
            }
        }

        return providers;
    }
}
