package it.geosolutions.media.jai.operator;

import java.awt.Color;

import javax.media.jai.OperationDescriptorImpl;
import javax.swing.text.StyledEditorKit.BoldAction;

public class ColorMaskDescriptor extends OperationDescriptorImpl {

    
    /**
     * The resource strings that provide the general documentation
     * and specify the parameter list for this operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "ColorMask"},
        {"LocalName",   "ColorMask"},
        {"Vendor",      "it.geosolutions.media.jai"},
//        {"Description", JaiI18N.getString("ColorMaskDescriptor0")},
        {"DocURL",      "FILL ME!"},
//        {"Version",     JaiI18N.getString("DescriptorVersion")},
//        {"arg0Desc",    JaiI18N.getString("ColorMaskDescriptor1")}
//      {"arg1Desc",    JaiI18N.getString("ColorMaskDescriptor2")}
    };
    
    /**
     * The parameter class list for this operation.
     */
    private static final Class[] paramClasses = {
        java.awt.Color.class, Boolean.class
    };
    
    /** The parameter name list for this operation. */
    private static final String[] paramNames = {
        "color", "preserveTransparency"
    };
    
    /** The parameter name list for this operation. */
    private static final Object[] paramDefaults = {
        new Color(255,255,255), Boolean.FALSE
    };
    
    public ColorMaskDescriptor(){
        super(resources, 1, paramClasses, paramNames, paramDefaults);
    }
    
}
