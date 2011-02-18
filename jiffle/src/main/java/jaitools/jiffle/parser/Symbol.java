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
 * Represents a symbol in a Jiffle script. Used by {@link SymbolScopeStack}
 * during script compilation.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class Symbol {
    private final String name;
    private final SymbolType type;

    /**
     * Creates a new symbol.
     * 
     * @param name name as used in the Jiffle script
     * @param type type of symbol
     */
    public Symbol(String name, SymbolType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets this symbol's name.
     * 
     * @return symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets this symbol's type.
     * 
     * @return symbol type
     */
    public SymbolType getType() {
        return type;
    }
    
    /**
     * Tests if this symbol's type is, or is descended from
     * the given type.
     * 
     * @param t a symbol type
     * @return {@code true} if type matches; {@code false otherwise}
     */
    public boolean isType(SymbolType t) {
        return type.isType(t);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Symbol other = (Symbol) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

}
