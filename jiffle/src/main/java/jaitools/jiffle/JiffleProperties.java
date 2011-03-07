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

package jaitools.jiffle;

import java.io.InputStream;
import java.util.Properties;

import jaitools.jiffle.runtime.JiffleRuntime;

/**
 * Helper class which reads the Jiffle properties file and provides values
 * to other classes. It is used by {@link Jiffle} and 
 * {@link jaitools.jiffle.parser.AbstractSourceGenerator}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleProperties {
    
    private static final Properties properties = new Properties();
    
    /** Properties file name. */
    public static final String PROPERTIES_FILE = "META-INF/jiffle/Jiffle.properties";
    
    /** Key: default toot name for a Jiffle instance. */
    public static final String NAME_KEY = "root.name";
    
    /** Key: runtime class package */
    public static final String RUNTIME_PACKAGE_KEY = "runtime.package";
    
    /** Key: direct runtime class name. */
    public static final String DIRECT_CLASS_KEY = "direct.class";

    /** Key: direct runtime base class name. */
    public static final String DIRECT_BASE_CLASS_KEY = "direct.base.class";
    
    /** Key: indirect runtime class name. */
    public static final String INDIRECT_CLASS_KEY = "indirect.class";

    /** Key: indirect runtime base class name. */
    public static final String INDIRECT_BASE_CLASS_KEY = "indirect.base.class";
    
    /** Key: default runtime imports. */
    public static final String IMPORTS_KEY = "runtime.imports";
    
    /** Delimiter used to separate multiple import entries */
    public static final String RUNTIME_IMPORTS_DELIM = ";";
    
    /** Key: common runtime source generation templates. */
    public static final String COMMON_SOURCE_TEMPLATES_KEY = "common.source.templates";

    /** Key: direct runtime source generation templates. */
    public static final String DIRECT_SOURCE_TEMPLATES_KEY = "direct.source.templates";

    /** Key: indirect runtime source generation templates. */
    public static final String INDIRECT_SOURCE_TEMPLATES_KEY = "indirect.source.templates";

    /** Default base class for direct runtime classes. */
    public static final Class<? extends JiffleRuntime> DEFAULT_DIRECT_BASE_CLASS;

    /** Default base class for indirect runtime classes. */
    public static final Class<? extends JiffleRuntime> DEFAULT_INDIRECT_BASE_CLASS;
    
    static {
        InputStream in = null;
        try {
            in = Jiffle.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            properties.load(in);
            
            String className = properties.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                    properties.getProperty(DIRECT_BASE_CLASS_KEY);
            
            DEFAULT_DIRECT_BASE_CLASS = (Class<? extends JiffleRuntime>) Class.forName(className);
            
            className = properties.getProperty(RUNTIME_PACKAGE_KEY) + "." +
                    properties.getProperty(INDIRECT_BASE_CLASS_KEY);
            
            DEFAULT_INDIRECT_BASE_CLASS = (Class<? extends JiffleRuntime>) Class.forName(className);
            
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
     * Gets a property.
     * 
     * @param key the property name (key)
     * 
     * @return property value
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    
}
