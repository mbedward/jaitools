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

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Map;

import javax.media.jai.TiledImage;

import jaitools.CollectionFactory;
import jaitools.demo.ImageChoice;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.swing.ImageFrame;

/**
 * Demonstrates how to retrieve and use a runtime object from a compiled 
 * Jiffle script.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class DirectRuntimeDemo extends JiffleDemoBase {

    /**
     * Run the demonstration. The optional {@code arg} can be either
     * the path to a user-supplied script file or one of "chessboard",
     * "interference", "ripple" or "squircle".
     * 
     * @param args (optional) the script to run
     * @throws Exception on an error in the Jiffle compiler
     */
    public static void main(String[] args) throws Exception {
        DirectRuntimeDemo demo = new DirectRuntimeDemo();
        File f = JiffleDemoHelper.getScriptFile(args, ImageChoice.RIPPLES);
        demo.compileAndRun(f);
    }

    /**
     * Compiles a script read from a file and submits it for execution.
     * 
     * @param scriptFile file containing the Jiffle script
     * @throws Exception on an error in the Jiffle compiler
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
            
            ImageFrame frame = new ImageFrame(destImg, "Jiffle image demo");
            frame.setVisible(true);
        }
    }

}
