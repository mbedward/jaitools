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

import jaitools.jiffle.Jiffle;

/**
 * Used by {@link JiffleExecutor} to send the results of an executed
 * task to {@link JiffleEventListener}s.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleExecutorResult {

    public static enum Status {
        COMPLETED,
        FAILED;
    }
    
    private final int jobID;
    private final Jiffle jiffle;
    private final Map<String, RenderedImage> images;
    private final Status status;

    public JiffleExecutorResult(int jobID, Jiffle jiffle, Map<String, RenderedImage> images, boolean completed) {
        this.jobID = jobID;
        this.jiffle = jiffle;
        this.images = images;
        
        status = completed ? Status.COMPLETED : Status.FAILED;
    }

    public Map<String, RenderedImage> getImages() {
        return images;
    }

    public Jiffle getJiffle() {
        return jiffle;
    }

    public int getJobID() {
        return jobID;
    }

    public Status getStatus() {
        return status;
    }
    
}
