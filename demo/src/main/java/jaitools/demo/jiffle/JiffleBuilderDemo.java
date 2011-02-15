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

import jaitools.demo.ImageChoice;
import java.io.File;

import jaitools.jiffle.JiffleBuilder;
import jaitools.swing.ImageFrame;

/**
 * Demonstrates using JiffleBuilder to compile and run a script.
 * <p>
 * Jiffle saves you from having to write lots of tedious JAI and Java AWT code.<br>
 * JiffleBuilder saves you from having to write lots of tedious Jiffle code !
 * Specifically, it uses concise chained methods to set the script, associate
 * variable names with images, and optionally create an image to receive the
 * processing results.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleBuilderDemo extends JiffleDemoBase {

    /**
     * Compiles and runs the "ripple" script using {@link JiffleBuilder).
     * @param args ignored
     * @throws Exception if there are errors compiling the script.
     */
    public static void main(String[] args) throws Exception {
        JiffleBuilderDemo me = new JiffleBuilderDemo();
        File f = JiffleDemoHelper.getScriptFile(args, ImageChoice.RIPPLES);
        String script = JiffleDemoHelper.readScriptFile(f);
        JiffleBuilder jb = new JiffleBuilder();
        jb.script(script).dest("result", WIDTH, HEIGHT).getRuntime().evaluateAll(null);

        ImageFrame frame = new ImageFrame(jb.getImage("result"), "Jiffle image demo");
        frame.setVisible(true);
    }
}
