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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class offers minimal Service Provider Interface lookup functions.
 * Within jai-tools it is used to find implementations of non-operator interfaces
 * such as {@code jaitools.numeric.Processor}. Class names are read from registry
 * files in the META-INF/services folder. Each registry file name corresponds to a
 * fully qualified interface name. File format is one class name per line. Comment
 * lines are prefixed with a hash (#) character. Blank lines are permitted.
 * <p>
 * Each time a registry file is read its contents are cached (weakly) for subsequent
 * lookups.
 * <p>
 * Discovery of image operators is done using the standard JAI mechanisms.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Lookup {

    private static final String prefix = "META-INF/services/";
    private static final Logger LOGGER = Logger.getLogger("jaitools.lookup");

    private static HashMap<String, WeakReference<List<Class>>> cache = new HashMap<String, WeakReference<List<Class>>>();

    /**
     * Get classes that implement the given service provider interface.
     *
     * @param spiName the fully qualified name of the interface
     *
     * @return list of implementing classes
     */
    public static List<Class> getProviders(String spiName) {
        List<Class> providers = null;
        
        WeakReference<List<Class>> ref = cache.get(spiName);
        if (ref != null) {
            providers = ref.get();
        }
        
        if (providers == null) {
            providers = getProvidersFromSpiFile(spiName);
            cache.put(spiName, new WeakReference<List<Class>>(providers));
        }
        
        return providers;
    }

    /**
     * Clear any cached lookups. This forces subsequent lookups to read
     * the registry files.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Returns a copy of the cached lookups as a {@code Map} with
     * service provider interface names as keys and lists of implementing
     * classes as values.
     * <p>
     * This method was added for testing purposes.
     *
     * @return a copy of the cached lookups as a new {@code Map} (may be empty)
     */
    public static Map<String, List<Class>> getCachedProviders() {
        Map<String, List<Class>> copy = new HashMap<String, List<Class>>();

        for (String key : cache.keySet()) {
            WeakReference<List<Class>> ref = cache.get(key);
            if (ref != null) {
                List<Class> list = ref.get();
                if (list != null) {
                    copy.put(key, list);
                }
            }
        }

        return copy;
    }

    /**
     * Read class names from a registry file and return the list of
     * implementing classes.
     *
     * @param spiName a fully qualified interface name
     *
     * @return list of implementing classes
     */
    private static List<Class> getProvidersFromSpiFile(String spiName) {
        List<Class> providers = new ArrayList<Class>();

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
            } else {
                LOGGER.severe("Could not find " + prefix + spiName);
            }
        }

        return providers;
    }
}
