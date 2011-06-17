/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class offers minimal Service Provider Interface lookup functions.
 * Within JAITools it is used to find implementations of non-operator interfaces
 * such as {@code org.jaitools.numeric.Processor}. Class names are read from registry
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
 * @version $Id$
 */
public class Lookup {

    private static final String prefix = "META-INF/services/";
    private static final Logger LOGGER = Logger.getLogger("org.jaitools.lookup");

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
                                LOGGER.log(Level.WARNING, "Class not found: {0}", text);
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Problem reading services file: {0}", spiName);
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
                LOGGER.log(Level.SEVERE,"Could not find " + prefix + "{0}", spiName);
            }
        }

        return providers;
    }
}
