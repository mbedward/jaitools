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

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.Map;
import java.util.concurrent.Callable;

import jaitools.jiffle.Jiffle;

/**
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
class JiffleExecutorTask implements Callable<JiffleExecutorResult> {
    
    private final JiffleExecutor executor;
    private final int id;
    private final Jiffle jiffle;
    private final Map<String, RenderedImage> images;
    
    private boolean completed;
    
    /**
     * Constructor
     * @param jiffle a compiled Jiffle object
     */
    public JiffleExecutorTask(int id, 
            JiffleExecutor executor, 
            Jiffle jiffle, 
            Map<String, RenderedImage> images) 
            throws JiffleExecutorException {
        
        this.id = id;
        this.executor = executor;
        this.jiffle = jiffle;
        this.images = images;
        
        completed = false;
        
        /*
         * TODO: get progress listeners working for the new runtime system
         *
        runner.addProgressListener(new RunProgressListener() {
            public void onProgress(float progress) {
                JiffleExecutorTask.this.interpreter.onTaskProgressEvent(JiffleExecutorTask.this, progress);
            }
        });
         * 
         */
        
    }

    public JiffleExecutorResult call() throws Exception {
        JiffleRuntime runtime = jiffle.getRuntimeInstance(true);
        
        Map<String, Jiffle.ImageRole> imageParams = jiffle.getImageParams();
        for (String imageName : images.keySet()) {
            switch (imageParams.get(imageName)) {
                case DEST:
                    WritableRenderedImage destImg = (WritableRenderedImage) images.get(imageName);
                    runtime.setDestinationImage(imageName, destImg);
                    break;
                    
                case SOURCE:
                    RenderedImage srcImg = images.get(imageName);
                    runtime.setSourceImage(imageName, srcImg);
                    break;
            }
        }
        
        boolean gotEx = false;
        try {
            runtime.evaluateAll();
            
        } catch (Exception ex) {
            gotEx = true;
        }

        completed = !gotEx;
        
        return new JiffleExecutorResult(id, jiffle, images, completed);
    }

}

