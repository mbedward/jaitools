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
import java.util.concurrent.TimeUnit;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests running a basic task with the executor. Can be run multiple times with
 * {@code JiffleExecutorTestRunner} to check for concurrency problems.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 *
 */
@RunWith(ExecutorTestRunner.class)
public class ExecutorSimpleTaskTest {
    private static final int WIDTH = 100;
    private static final double TOL = 1.0e-8;
    
    private JiffleExecutor executor;
    private final JiffleProgressListener nullListener = new NullProgressListener();
    

    @Before
    public void setup() {
        executor = new JiffleExecutor();
    }
    
    @After
    public void cleanup() {
        executor.shutdownAndWait(1, TimeUnit.SECONDS);
    }
    
    @Test
    public void simpleTask() throws Exception {
        Map<String, Jiffle.ImageRole> imageParams;
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        Jiffle jiffle = new Jiffle("dest = x() + y();", imageParams);
        
        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("dest", ImageUtils.createConstantImage(WIDTH, WIDTH, 0d));
        
        WaitingListener listener = new WaitingListener();
        executor.addEventListener(listener);
        
        listener.setNumTasks(1);

        int jobID = executor.submit(jiffle, images, nullListener);
        
        if (!listener.await(2, TimeUnit.SECONDS)) {
            fail("Listener time-out period elapsed");
        }
        
        JiffleExecutorResult result = listener.getResults().get(0);
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
