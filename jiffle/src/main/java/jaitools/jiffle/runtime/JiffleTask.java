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

import java.util.concurrent.Callable;

/**
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
class JiffleTask implements Callable<JiffleTaskResult> {
    
    private final JiffleExecutor executor;
    private int id;
    private JiffleRuntime runtime;
    private boolean completed;
    
    /**
     * Constructor
     * @param jiffle a compiled Jiffle object
     */
    public JiffleTask(int id, JiffleExecutor executor, JiffleRuntime jr) 
            throws JiffleExecutorException {
        
        this.id = id;
        this.executor = executor;
        this.runtime = jr;
        
        /*
         * TODO: get progress listeners working for the new runtime system
         *
        runner.addProgressListener(new RunProgressListener() {
            public void onProgress(float progress) {
                JiffleTask.this.interpreter.onTaskProgressEvent(JiffleTask.this, progress);
            }
        });
         * 
         */
        
        completed = false;
    }

    public JiffleTaskResult call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

