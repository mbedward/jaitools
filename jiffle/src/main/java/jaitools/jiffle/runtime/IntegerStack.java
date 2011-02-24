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

/**
 * A simple, array-based stack for Integer values used by {@link AbstractJiffleRuntime}.
 * This class is here to avoid using generic collections (which the Janino compiler
 * does not support) or littering the runtime source code with casts.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class IntegerStack {
    /** Initial size of stack and grow increment */
    public static final int CHUNK_SIZE = 1000;
    
    /** 
     * Stack size beyond which the data array will be shrunk 
     * when {@link #clear()} is called. 
     */
    public static final int CLEAR_SIZE = 10 * CHUNK_SIZE;
    
    /** Data array */
    private Integer[] data = new Integer[CHUNK_SIZE];
    private int index = -1;

    /**
     * Push a value onto the stack.
     * @param x the value
     * @return the value
     */
    public synchronized Integer push(Integer x) {
        if (++index == data.length) {
            grow();
        }
        data[index] = x;
        return x;
    }

    /**
     * Pop the top value off the stack.
     * 
     * @return the value
     * @throws RuntimeException if the stack is empty
     */
    public synchronized Integer pop() {
        if (index >= 0) {
            Integer val = data[index];
            index--;
            return val;
        }
        throw new RuntimeException("Stack is empty");
    }

    /**
     * Peek at the top value without removing it.
     * 
     * @return the value
     * @throws RuntimeException if the stack is empty
     */
    public synchronized Integer peek() {
        if (index >= 0) {
            return data[index];
        }
        throw new RuntimeException("Stack is empty");
    }

    /**
     * Clear the stack. If the stack size if above {@link #CLEAR_SIZE}
     * the data array is shrunk to its initial size.
     */
    public synchronized void clear() {
        if (data.length > CLEAR_SIZE) {
            data = new Integer[CHUNK_SIZE];
        }
        index = -1;
    }
    
    /**
     * Gets the number of items on the stack.
     * 
     * @return number of items.
     */
    public int size() {
        return index + 1;
    }

    /**
     * Grow the data array by adding {@link #CHUNK_SIZE} elements.
     */
    private void grow() {
        Integer[] temp = new Integer[data.length + CHUNK_SIZE];
        System.arraycopy(data, 0, temp, 0, data.length);
        data = temp;
    }

}
