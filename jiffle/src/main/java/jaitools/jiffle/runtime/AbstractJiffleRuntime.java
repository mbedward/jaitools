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

import java.awt.Rectangle;

/**
 * Provides default implementations of {@link JiffleRuntime} methods plus 
 * some common fields. The fields include those involved in handling image-scope
 * variables and script options; an instance of {@link JiffleFunctions}; and an
 * integer stack used in evaluating {@code con} statements.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AbstractJiffleRuntime implements JiffleRuntime {
    
    /** Processing bounds */
    protected Rectangle _bounds;
    
    /** Whether the image-scope variables have been initialized. */
    protected boolean _imageScopeVarsInitialized;
    
    /** Holds information about an image-scope variable. */
    public class ImageScopeVar {
        
        public String name;
        public boolean hasDefaultValue;
        public boolean isSet;
        public double value;

        public ImageScopeVar(String name, boolean hasDefaultValue) {
            this.name = name;
            this.hasDefaultValue = hasDefaultValue;
        }
    }

    // Used to size / resize the _vars array as required
    private static final int VAR_ARRAY_CHUNK = 100;
    
    /** Image-scope variables. */
    protected ImageScopeVar[] _vars = new ImageScopeVar[VAR_ARRAY_CHUNK];
    
    /** The number of image-scope variables defined. */
    protected int _numVars;
    
    /** Advertizes the image-scope variable getter syntax to source generators. */
    public static final String VAR_STRING = "_vars[_VAR_].value";
    
    /** Whether the <i>outside</i> option is set. */
    protected boolean _outsideValueSet;
    
    /** 
     * The value to return for out-of-bounds image data requests if the
     * <i>outside</i> option is set.
     */
    protected double _outsideValue;

    /** 
     * A stack of integer values used in the evaluation of if statements.
     */
    protected IntegerStack _stk;
    
    /** 
     * Provides runtime function support.
     */
    protected final JiffleFunctions _FN;

    /**
     * Creates a new instance of this class and initializes its 
     * {@link JiffleFunctions} and {@link IntegerStack} objects.
     */
    public AbstractJiffleRuntime() {
        _FN = new JiffleFunctions();
        _stk = new IntegerStack();
    }
    
    /**
     * {@inheritDoc}
     */
    public void setBounds(int minx, int miny, int width, int height) {
        this._bounds = new Rectangle(minx, miny, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public Double getVar(String varName) {
        int index = getVarIndex(varName);
        if (index < 0) {
            return null;
        }
        
        return _vars[index].isSet ? _vars[index].value : null; 
    }

    /**
     * {@inheritDoc}
     */
    public void setVar(String varName, Double value) throws JiffleRuntimeException {
        int index = getVarIndex(varName);
        if (index < 0) {
            throw new JiffleRuntimeException("Undefined variable: " + varName);
        }
        setVarValue(index, value);
    }

    /**
     * Sets the value of an image-scope variable. If {@code value} is {@code null}
     * the variable is set to its default value if one is defined, otherwise an
     * exception is thrown.
     * 
     * @param index variable index
     * @param value the new value or {@code null} for default value
     * @throws JiffleRuntimeException if {@code value} is {@code null} but no default
     *         value is defined for the variable
     */
    protected void setVarValue(int index, Double value) throws JiffleRuntimeException {
        if (value == null) {
            if (!_vars[index].hasDefaultValue) {
                throw new JiffleRuntimeException(
                        "Value cannot be null for variable with no default: " + _vars[index].name);
            }
            
            _imageScopeVarsInitialized = false;
            _vars[index].isSet = false;
            
        } else {
            _vars[index].value = value;
            _vars[index].isSet = true;
        }
    }

    /**
     * Gets the index for an image-scope variable by name.
     * 
     * @param varName variable name
     * @return the index or -1 if the name is not found
     */
    protected int getVarIndex(String varName) {
        for (int i = 0; i < _numVars; i++) {
            if (_vars[i].name.equals(varName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Initializes image-scope variables. These are fields in the runtime class.
     * They are initialized in a separate method rather than the constructor
     * because they may depend on expressions involving values which are not
     * known until the processing area is set (e.g. Jiffle's width() function).
     * 
     * @throws JiffleRuntimeException if any variables do not have either a
     *         default or provided value
     */
    protected void initImageScopeVars() {
        for (int i = 0; i < _numVars; i++) {
            if (!_vars[i].isSet) {
                Double value = getDefaultValue(i);
                if (value == null) {
                    throw new JiffleRuntimeException(
                            "No default value set for " + _vars[i].name);
                }
                _vars[i].value = value;
                _vars[i].isSet = true;
            }
        }
        _imageScopeVarsInitialized = true;
    }
    
    /**
     * Gets the default value for an image-scope variable. This method is 
     * overridden as part of the generated run-time class code.
     * 
     * @param index the index of the variable
     * @return the default value or {@code null} if one is not defined
     */
    protected abstract Double getDefaultValue(int index);

    /**
     * Initializes runtime class fields related to Jiffle script options.
     */
    protected abstract void initOptionVars();

    /**
     * 
     * @param isvNames
     * @param evals 
     */
    protected void registerVar(String name, boolean hasDefault) {
        // check that the variable is not already registered
        if (getVarIndex(name) >= 0) {
            throw new JiffleRuntimeException("Variable already defined: " + name);
        }
        
        _numVars++ ;
        ImageScopeVar var = new ImageScopeVar(name, hasDefault);
        if (_numVars > _vars.length) {
            growVarsArray();
        }
        _vars[_numVars - 1] = var;
    }
    
    private void growVarsArray() {
        ImageScopeVar[] temp = _vars;
        _vars = new ImageScopeVar[_vars.length + VAR_ARRAY_CHUNK];
        System.arraycopy(temp, 0, _vars, 0, temp.length);
    }
    
    /**
     * Gets the min X ordinate of the processing area.
     * 
     * @return min X ordinate
     */
    public int getMinX() {
        return _bounds.x;
    }

    /**
     * Gets the max X ordinate of the processing area.
     * 
     * @return max X ordinate
     */
    public int getMaxX() {
        return _bounds.x + _bounds.width - 1;
    }

    /**
     * Gets the min Y ordinate of the processing area.
     * 
     * @return min Y ordinate
     */
    public int getMinY() {
        return _bounds.y;
    }

    /**
     * Gets the max Y ordinate of the processing area.
     * 
     * @return max Y ordinate
     */
    public int getMaxY() {
        return _bounds.y + _bounds.height - 1;
    }
    
    /**
     * Gets the width of the processing area.
     * 
     * @return the width
     */
    public int getWidth() {
        return _bounds.width;
    }
    
    /**
     * Gets the height of the processing area.
     * 
     * @return the height
     */
    public int getHeight() {
        return _bounds.height;
    }
    
    /**
     * Gets the total number of pixels in the processing area.
     * 
     * @return the number of pixels
     */
    public long getSize() {
        return (long)_bounds.width * _bounds.height;
    }
}
