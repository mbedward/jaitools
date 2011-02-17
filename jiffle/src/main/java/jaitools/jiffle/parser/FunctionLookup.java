/*
 * Copyright 2011 Michael Bedward
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

package jaitools.jiffle.parser;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import jaitools.CollectionFactory;

/**
 * A lookup service used by the Jiffle compiler when parsing function
 * calls in scripts.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class FunctionLookup {

    private static final String PROPERTIES_FILE = "META-INF/FunctionLookup.properties";
    private static final List<FunctionInfo> lookup = CollectionFactory.list();

    static {
        InputStream in = null;
        try {
            in = FunctionLookup.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            Properties properties = new Properties();
            properties.load(in);
            
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String value = properties.getProperty(name);

                String[] attr = value.split("[,\\s]+");
                if (attr.length != 5) {
                    throw new IllegalArgumentException(
                            "Error reading Jiffle function definitions from " + PROPERTIES_FILE);
                }
                
                String jiffleName = attr[0];

                String runtimeName = attr[1];
                int numArgs = 0;
                if (attr[2].toUpperCase().contains("VARARG")) {
                    numArgs = FunctionInfo.VARARG;
                } else {
                    numArgs = Integer.parseInt(attr[2]);
                }

                FunctionInfo.Provider provider = FunctionInfo.Provider.get( attr[3] );
                if (provider == null) {
                    throw new IllegalArgumentException(
                            "Unrecognized Jiffle function provider (" + attr[3] + ") in " + PROPERTIES_FILE);
                }

                boolean isVolatile = Boolean.parseBoolean(attr[4]);

                lookup.add( new FunctionInfo(
                        jiffleName, runtimeName, numArgs, provider, isVolatile) );
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException("Internal compiler error", ex);

        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ex) {
                // ignore
            }
        }
    }
    
    /**
     * Checks if a function is defined.
     *
     * @param jiffleName the name of the function used in a Jiffle script
     *
     * @param numArgs number of arguments or {@link FunctionInfo#VARARG} (-1)
     *        for a variable argument function
     *
     * @return {@code true} if defined; {@code false} otherwise
     */
    public static boolean isDefined(String jiffleName, int numArgs) {
        try {
            getInfo(jiffleName, numArgs);
        } catch (UndefinedFunctionException ex) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the info for a function.
     *
     * @param jiffleName the name of the function used in a Jiffle script
     *
     * @param numArgs number of arguments or {@link FunctionInfo#VARARG} (-1)
     *        for a variable argument function
     *
     * @return function info
     * @throws UndefinedFunctionException if {@code jiffleName} is not recognized
     */
    public static FunctionInfo getInfo(String jiffleName, int numArgs)
            throws UndefinedFunctionException {

        List<FunctionInfo> list = getByName(jiffleName);
        for (FunctionInfo info : list) {
            if (info.getJiffleName().equals(jiffleName)
                    && (info.isVarArg() || info.getNumArgs() == numArgs)) {
                return info;
            }
        }
        
        // should never get here
        throw new UndefinedFunctionException("Unrecognized function: " + jiffleName);
    }
    
    /**
     * Gets the runtime source for the function. This will consist of
     * provider name plus function name in the case of {@code JiffleFunction}
     * and {@code java.lang.Math} methods, or runtime class field name in the
     * case of proxy (image info) functions.
     *
     * @param jiffleName the name of the function used in a Jiffle script
     *
     * @param numArgs number of arguments or {@link FunctionInfo#VARARG} (-1)
     *        for a variable argument function
     *
     * @return the runtime source
     * @throws UndefinedFunctionException if {@code jiffleName} is not recognized
     */
    public static String getRuntimeExpr(String jiffleName, int numArgs) throws UndefinedFunctionException {
        return getInfo(jiffleName, numArgs).getRuntimeExpr();
    }
    
    private static List<FunctionInfo> getByName(String jiffleName) throws UndefinedFunctionException {
        List<FunctionInfo> list = CollectionFactory.list();
        for (FunctionInfo info : lookup) {
            if (info.getJiffleName().equals(jiffleName)) {
                list.add(info);
            }
        }

        if (list.isEmpty()) {
            throw new UndefinedFunctionException(jiffleName);
        }
        
        return list;
    }

}
