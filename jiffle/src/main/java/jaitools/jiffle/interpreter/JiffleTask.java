/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.interpreter;

/**
 * A dumb wrapper for a {@link JiffleRunner} object and its
 * associated {@link Jiffle} being run by the interpreter
 * 
 * @author Michael Bedward
 */
class JiffleTask implements Runnable {
    
    private JiffleInterpreter interpreter;
    private int id;
    private Jiffle jiffle;
    private JiffleRunner runner;
    private boolean completed;
    
    /**
     * Constructor
     * @param jiffle a compiled Jiffle object
     */
    public JiffleTask(int id, JiffleInterpreter interpreter, Jiffle jiffle) 
            throws JiffleInterpreterException {
        
        this.id = id;
        this.interpreter = interpreter;
        this.jiffle = jiffle;
        runner = new JiffleRunner(jiffle);
        completed = false;
    }

    /**
     * Run this task
     */
    public void run() {
        
        boolean ok = true;
        
        try {
            runner.run();
        
        } catch (JiffleInterpreterException iex) {
            // @todo error reporting
            ok = false;
            
        } catch (RuntimeException rex) {
            ok = false;
            
        } finally {
            completed = ok;
            interpreter.onTaskEvent(this);
        }
    }
    
    /**
     * Check if the run as successfully completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Package private method used by {@link JiffleInterpreter}
     */
    Jiffle getJiffle() {
        return jiffle;
    }
    
    /**
     * Package private method used by {@link JiffleInterpreter}
     */
    int getId() {
        return id;
    }
}

