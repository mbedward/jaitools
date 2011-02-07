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

import jaitools.jiffle.JiffleException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * Base class for Jiffle demo application classes.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public abstract class JiffleDemoBase {
    protected final static int WIDTH = 600;
    protected final static int HEIGHT = 600;

    protected File getScriptFile(String[] args, String defaultResource) throws Exception {
        URL url = getClass().getResource(defaultResource);

        if (args.length == 1) {
            String arg = args[0];
            System.out.println(arg);
            File file = new File(arg);
            if (file.exists()) {
                url = file.toURI().toURL();
            } else {
                int dot = arg.lastIndexOf('.');
                if (dot < 0) {
                    arg = arg + ".jfl";
                }
                url = getClass().getResource("/scripts/" + arg);
            }
        }

        return new File(url.toURI());
    }

    
    protected String readScriptFile(File scriptFile) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(scriptFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    sb.append(line);
                    sb.append('\n');  // put the newline back on for the parser
                }
            }

            return sb.toString();

        } catch (IOException ex) {
            throw new JiffleException("Could not read the script file", ex);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    
}
