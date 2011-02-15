/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.demo;

import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;

import jaitools.jiffle.JiffleBuilder;

/**
 * Serves images to the demo applications. Each image is generated from
 * a Jiffle script.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DemoImages {

    /**
     * Gets an image.
     *
     * @param choice one of CHESSBOARD, INTERFERENCE, RIPPLES, SQUIRCLES
     * @param width image width
     * @param height image height
     *
     * @return the requested image
     */
    public static RenderedImage get(ImageChoice choice, int width, int height) {
        try {
            String name = "/scripts/" + choice + ".jfl";
            URL url = DemoImages.class.getResource(name);
            File file = new File(url.toURI());

            JiffleBuilder jb = new JiffleBuilder();
            jb.script(file).dest("result", width, height).getRuntime().evaluateAll(null);
            RenderedImage image = jb.getImage("result");
            return image;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
