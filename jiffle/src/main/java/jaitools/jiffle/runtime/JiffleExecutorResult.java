/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import jaitools.jiffle.Jiffle;
import java.awt.image.RenderedImage;
import java.util.Map;

/**
 *
 * @author michael
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
