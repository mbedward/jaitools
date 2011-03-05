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

    /** General scalar user variable. */
    SCALAR("scalar", "General scalar user var"),
    
    /** A foreach loop variable. */
    LOOP_VAR("loopvar", "Loop var"),
    
    /** A list variable. */
    LIST("list", "List var");
    
    private final String name;
    private final String desc;
    
    private SymbolType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
    
    /**
     * Gets the description of this type.
     * 
     * @return the description
     */
    public String getDesc() {
        return desc;
    }
    
    @Override
    public String toString() {
        return "SymbolType{" + name + '}';
    }
    
}
