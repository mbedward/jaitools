/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import jaitools.jiffle.runtime.JiffleFunctions;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author michael
 */
public class FunctionLookup {
    
    Map<String, String> lookup = new HashMap<String, String>();
    
    public FunctionLookup() {
        lookup.put("abs_1", "Math.abs");
        lookup.put("acos_1", "Math.acos");
        lookup.put("asin_1", "Math.asin");
        lookup.put("atan_1", "Math.atan");
        lookup.put("cos_1", "Math.cos");
        lookup.put("degToRad_1", "JiffleFunctions.degToRad");
        lookup.put("floor_1", "Math.floor");
        lookup.put("if_1", "JiffleFunctions.if1Arg");
        lookup.put("if_2", "JiffleFunctions.if2Arg");
        lookup.put("if_3", "JiffleFunctions.if3Arg");
        lookup.put("if_4", "JiffleFunctions.if4Arg");
        lookup.put("isinf_1", "JiffleFunctions.isinf");
        lookup.put("isnan_1", "JiffleFunctions.isnan");
        lookup.put("isnull_1", "JiffleFunctions.isnull");
        lookup.put("log_1", "Math.log");
        lookup.put("log_2", "JiffleFunctions.log2Arg");
        lookup.put("max_v", "JiffleFunctions.max");
        lookup.put("mean_v", "JiffleFunctions.mean");
        lookup.put("median_v", "JiffleFunctions.median");
        lookup.put("min_v", "JiffleFunctions.min");
        lookup.put("mode_v", "JiffleFunctions.mode");
        lookup.put("null_0", "JiffleFunctions.nullValue");
        lookup.put("radToDeg_1", "JiffleFunctions.radToDeg");
        lookup.put("rand_1", "JiffleFunctions.rand");
        lookup.put("randInt_1", "JiffleFunctions.randInt");
        lookup.put("range_1", "JiffleFunctions.range");
        lookup.put("round_1", "Math.round");
        lookup.put("round_2", "JiffleFunctions.range");
        lookup.put("sdev_v", "JiffleFunctions.sdev");
        lookup.put("sin_1", "Math.sin");
        lookup.put("sqrt_1", "Math.sqrt");
        lookup.put("tan_1", "Math.tan");
        lookup.put("variance_v", "JiffleFunctions.variance");
        
        lookup.put("OR_2", "JiffleFunctions.OR");
        lookup.put("AND_2", "JiffleFunctions.AND");
        lookup.put("XOR_2", "JiffleFunctions.XOR");
        lookup.put("GT_2", "JiffleFunctions.GT");
        lookup.put("GE_2", "JiffleFunctions.GE");
        lookup.put("LT_2", "JiffleFunctions.LT");
        lookup.put("LE_2", "JiffleFunctions.LE");
        lookup.put("EQ_2", "JiffleFunctions.EQ");
        lookup.put("NE_2", "JiffleFunctions.NE");
        lookup.put("NOT_1", "JiffleFunctions.NOT");
    }

    public String getRuntimeName(String jiffleName, int numArgs) {
        // Look for a match with vararg functions
        String runtimeName = lookup.get(jiffleName + "_v");
        if (runtimeName != null) {
            return runtimeName;
        }
        
        // Look for a match with functions having the specified number
        // of arguments
        runtimeName = lookup.get(jiffleName + "_" + numArgs);
        if (runtimeName == null) {
            throw new IllegalArgumentException("Unrecognized function name: " + jiffleName);
        }
        
        return runtimeName;
    }
    
    public boolean isDefined(String jiffleName, int numArgs) {
        try {
            getRuntimeName(jiffleName, numArgs);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        
        return true;
    }
}
