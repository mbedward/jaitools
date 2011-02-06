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
import jaitools.jiffle.JiffleException;

/**
 * Executes a runtime object in a thread provided by a {@link JiffleExecutor}.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
class JiffleExecutorTask implements Callable<JiffleExecutorResult> {
    
    private final int id;
    private final Jiffle jiffle;
    private final Map<String, RenderedImage> images;
    private final JiffleProgressListener progressListener;
    
    private boolean completed;

    
    /**
     * Creates a new task. The {@code Jiffle} object must be 
     * properly initialized with a script and image parameters 
     * although it need not be compiled. The image variable names
     * in {@code images} must match those used in the {@code Jiffle}
     * objects image parameters.
     * 
     * @param id job ID allocated by the {@link JiffleExecutor}.
     * @param jiffle the {@link Jiffle} object
     * @param images a {@code Map} with image variable name as key and the
     *        corresponding source or destination image as value 
     */
    public JiffleExecutorTask(int id, 
            Jiffle jiffle, 
            Map<String, RenderedImage> images,
            JiffleProgressListener progressListener) {
        
        this.id = id;
        this.jiffle = jiffle;
        this.images = images;
        this.progressListener = progressListener;
        
        completed = false;
    }

    /**
     * Called by the system to execute this task on a thread provided by the
     * {@link JiffleExecutor}.
     * 
     * @return a result object with references to the {@code Jiffle} object,
     *         the images, and the job completion status
     */
    public JiffleExecutorResult call() throws JiffleException {
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
        
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
            runtime.evaluateAll(progressListener);
            
        } catch (Exception ex) {
            gotEx = true;
        }

        completed = !gotEx;
        return new JiffleExecutorResult(id, jiffle, images, completed);
    }

}

