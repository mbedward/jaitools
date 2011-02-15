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
package jaitools.demo.jiffle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;

import jaitools.demo.ImageChoice;
import jaitools.jiffle.JiffleException;

/**
 * Helper class with for Jiffle demo applications.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleDemoHelper {
    
    /**
     * Gets an example script.
     * 
     * @param choice specifies the example script
     * @throws JiffleException on errors getting the script file
     */
    public static String getScript(ImageChoice choice) throws JiffleException {
        File scriptFile = getScriptFile(null, choice);
        return readScriptFile(scriptFile);
    }

    /**
     * Gets a file specified in the command line args, or the default
     * example image if no name is supplied.
     *
     * @param args command lines args passed from an application
     * @param defaultScript default example script
     * @return the script file
     * @throws JiffleException on problems getting the file
     */
    public static File getScriptFile(String[] args, ImageChoice defaultScript) 
            throws JiffleException {
                
        String fileName = null;
        File file = null;
        
        if (args == null || args.length < 1) {
            try {
                fileName = defaultScript.toString() + ".jfl";
                URL url = JiffleDemoHelper.class.getResource("/scripts/" + fileName);
                file = new File(url.toURI());
                
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        
        } else {
            fileName = args[0];
            file = new File(fileName);
        }
        
        if (file.exists()) {
            return file;
        }
        
        throw new JiffleException("Can't find script file:" + fileName);
    }

    /**
     * Reads the contents of a script file.
     *
     * @param scriptFile the file
     * @return the script as a String
     * @throws JiffleException on problems reading the file
     */
    public static String readScriptFile(File scriptFile) throws JiffleException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(scriptFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (IOException ex) {
            throw new JiffleException("Could not read the script file", ex);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
