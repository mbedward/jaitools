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

import jaitools.jiffle.parser.JiffleWalker;
import org.antlr.runtime.RecognitionException;

/**
 *
 * @author Michael Bedward and Murray Ellis
 */
class JiffleRunnable implements Runnable {
    
    private Jiffle j;

    public JiffleRunnable(Jiffle j) {
        this.j = j;
    }

    public void run() {
        JiffleWalker w = new JiffleWalker(j.getTree());
        try {
            w.prog();
            
        } catch (RecognitionException re) {
            // @todo WRITE ME
        }
    }
}

