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

import javax.media.jai.TiledImage;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.StatementsTestBase;

import org.junit.Test;

/**
 * Unit tests for the JiffleBuilder helper class.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JiffleBuilderTest extends StatementsTestBase {

    @Test
    public void runBasicScript() throws Exception {
        String script = "dest = if (src1 > 10, src1, NULL);" ;

        TiledImage srcImg1 = ImageUtils.createConstantImage(WIDTH, WIDTH, 1.0d);
        TiledImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);

        JiffleBuilder jiffy = new JiffleBuilder();
        jiffy.script(script).source("src1", srcImg1).dest("dest", destImg);
        JiffleDirectRuntime runtime = jiffy.getRuntime();
        runtime.evaluateAll(null);

        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return val > 10 ? val : Double.NaN;
            }
        };

        assertScript(srcImg1, destImg, e);
    }

}
