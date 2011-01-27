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

import jaitools.jiffle.runtime.JiffleDirectRuntime;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import javax.media.jai.TiledImage;
import javax.swing.SwingUtilities;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.swing.ImageFrame;

/**
 * Demonstrates how to retrieve and use a runtime object from a compiled 
 * Jiffle script.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DirectRuntimeDemo {
    private final static int WIDTH = 600;
    private final static int HEIGHT = 600;

    /**
     * Run the demonstration. The optional {@code arg} can be either
     * the path to a user-supplied script or one of "chessboard",
     * "interference", "ripple" or "squircle".
     * 
     * @param args (optional) the script to run
     */
    public static void main(String[] args) throws Exception {
        DirectRuntimeDemo demo = new DirectRuntimeDemo();

        URL url = demo.getClass().getResource("/scripts/ripple.jfl");

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
                url = demo.getClass().getResource("/scripts/" + arg);
            }
        }

        File f = new File(url.toURI());
        demo.compileAndRun(f);        
    }

    /**
     * Compiles a script read from a file and submits it for execution.
     * 
     * @param scriptFile file containing the Jiffle script
     */
    public void compileAndRun(File scriptFile) throws Exception {
        Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
        imageParams.put("result", Jiffle.ImageRole.DEST);

        Jiffle jiffle = new Jiffle(scriptFile, imageParams);

        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("result",
                ImageUtils.createConstantImage(WIDTH, HEIGHT, Double.valueOf(0d)));

        if (jiffle.isCompiled()) {
            JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();

            final TiledImage destImg = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0d);
            runtime.setDestinationImage("result", destImg);
            
            runtime.evaluateAll(null);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ImageFrame frame = new ImageFrame(destImg, "Jiffle image demo");
                    frame.setVisible(true);
                }
            });
        }
    }

}
