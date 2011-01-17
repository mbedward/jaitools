/*
 * Copyright 2009-11 Michael Bedward
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

package jaitools.jiffle.parser;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

/**
 * A custom AST node class which holds a double value. Used
 * by classes generated from ANTLR grammars.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class FixedValueNode extends CommonTree {
    private double value;

    /**
     * Constructor taking a double value
     */
    public FixedValueNode(int tokType, double value) {
        super(new CommonToken(tokType));
        this.value = value; 
    }

    /**
     * Constructor taking a String representation of a double value
     */
    public FixedValueNode(int tokType, String text) {
        this(tokType, Double.valueOf(text));
    }

    /**
     * Get the value stored by this node
     */
    public double getValue() { return value; }
    
    /**
     * Used by ANTLR during AST re-writing to create
     * a copy of this node
     */
    @Override
    public FixedValueNode dupNode() {
        return new FixedValueNode(getType(), value);
    }    
    
    /**
     * Get a formatted string representation of this node.
     * It takes the form: {@code FIX<value>}
     */
    @Override
    public String toString() {
        return "FIX<" + String.valueOf(value) + ">";
    }
}
