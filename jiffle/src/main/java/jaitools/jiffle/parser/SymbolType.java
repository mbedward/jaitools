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

package jaitools.jiffle.parser;

/**
 * Constants representing the type of symbols tracked through scopes
 * during JIffle script compilation.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public enum SymbolType {
    
    /** A user variable with image scope. */
    IMAGE_SCOPE(null, "Image scope var"),
    
    /** A user variable with pixel scope. */
    PIXEL_SCOPE(null, "Pixel scope var"),
    
    /** A foreach loop variable (type of pixel scope variable). */
    LOOP_VAR(PIXEL_SCOPE, "Loop var");
    
    private final SymbolType parent;
    private final String desc;
    
    private SymbolType(SymbolType parent, String desc) {
        this.parent = parent;
        this.desc = desc;
    }
    
    /**
     * Tests if this type is, or is descended from
     * the given type.
     * 
     * @param type type for comparison
     * @return {@code true} if type matches; {@code false otherwise}
     */
    public boolean isType(SymbolType type) {
        if (this == type) {
            return true;
        }
        
        if (parent != null) {
            return parent.isType(type);
        }
        
        return false;
    }

    @Override
    public String toString() {
        return "SymbolType{" + desc + '}';
    }
    
}
