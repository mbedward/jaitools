/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

import java.io.InputStream;
import java.util.Properties;

import jaitools.jiffle.runtime.JiffleRuntime;

/**
 *
 * @author michael
 */
public class JiffleProperties {
    
    private static final Properties properties = new Properties();
    
    public static final String PROPERTIES_FILE = "META-INF/jiffle/Jiffle.properties";
    
    public static final String NAME_KEY = "root.name";
    public static final String RUNTIME_PACKAGE_KEY = "runtime.package";
    
    public static final String DIRECT_CLASS_KEY = "direct.class";
    public static final String DIRECT_BASE_CLASS_KEY = "direct.base.class";
    
    public static final String INDIRECT_CLASS_KEY = "indirect.class";
    public static final String INDIRECT_BASE_CLASS_KEY = "indirect.base.class";
    
    public static final String IMPORTS_KEY = "runtime.imports";
    
    /** Delimiter used to separate multiple import entries */
    public static final String RUNTIME_IMPORTS_DELIM = ";";
    
    public static final Class<? extends JiffleRuntime> DEFAULT_DIRECT_BASE_CLASS;
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

    public static String get(String key) {
        return properties.getProperty(key);
    }

    
}
