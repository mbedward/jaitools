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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jaitools.CollectionFactory;


/**
 * An event listener that uses a {@code CountDownLatch} to force the client to
 * wait for the expected number of tasks to be completed.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class WaitingListener implements JiffleEventListener {

    private CountDownLatch latch = null;

    private final List<JiffleExecutorResult> results = CollectionFactory.list();
    
    /**
     * Sets the number of job completions and/or failures to wait for.
     * 
     * @param n number of jobs
     */
    public void setNumJobs(int n) {
        if (latch != null && latch.getCount() > 0) {
            throw new IllegalStateException("Method called during wait period");
        }

        latch = new CountDownLatch(n);
    }
    
    /**
     * Waits for jobs to finish.
     */
    public boolean await(long timeOut, TimeUnit units) {
        try {
            boolean isZero = latch.await(timeOut, units);
            if (!isZero) {
                return false;
            }
            return true;
            
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public JiffleExecutorResult getResult(int jobID) {
        return results.get(jobID);
    }

    public void onCompletionEvent(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        results.add(result);
        latch.countDown();
    }

    public void onFailureEvent(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        results.add(result);
        latch.countDown();
    }

}
