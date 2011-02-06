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

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.util.Map;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for JiffleExecutor.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
@Ignore("thread deadlock problem with WaitingListener needs to be fixed")
public class JiffleExecutorTest {
    private static final int WIDTH = 100;
    private static final double TOL = 1.0e-8;
    
    private final JiffleProgressListener nullListener = new NullProgressListener();
    
    @Test
    public void simpleJob() throws Exception {
        Map<String, Jiffle.ImageRole> imageParams;
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        Jiffle jiffle = new Jiffle("dest = x() + y();", imageParams);
        
        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("dest", ImageUtils.createConstantImage(WIDTH, WIDTH, 0d));
        
        JiffleExecutor executor = new JiffleExecutor();
        
        WaitingListener listener = new WaitingListener();
        executor.addEventListener(listener);
        
        listener.setNumJobs(1);
        int jobID = executor.submit(jiffle, images, nullListener);
        listener.await();
        
        JiffleExecutorResult result = listener.getResult(jobID);
        assertNotNull(result);
        
        RenderedImage dest = result.getImages().get("dest");
        assertNotNull(dest);
        
        RectIter iter = RectIterFactory.create(dest, null);
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                assertEquals((double)x + y, iter.getSampleDouble(), TOL);
                iter.nextPixel();
            }
            iter.startPixels();
            iter.nextLine();
        }
    }
    
}
