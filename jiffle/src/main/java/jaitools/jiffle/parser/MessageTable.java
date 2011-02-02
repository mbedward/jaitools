/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.jiffle.parser;

import java.util.List;
import java.util.Map;
import jaitools.CollectionFactory;
import java.util.Collections;

/**
 *
 * @author michael
 */
public class MessageTable {

    private Map<String, List<Message>> errors = CollectionFactory.map();

    public void add(String varName, Message code) {
        List<Message> codes = errors.get(varName);
        if (codes == null) {
            codes = CollectionFactory.list();
            errors.put(varName, codes);
        }
        codes.add(code);
    }
    
    public boolean hasErrors() {
        for (List<Message> codes : errors.values()) {
            for (Message code : codes) {
                if (code.isError()) return true;
            }
        }
        
        return false;
    }
    
    public boolean hasWarnings() {
        for (List<Message> codes : errors.values()) {
            for (Message code : codes) {
                if (code.isWarning()) return true;
            }
        }
        
        return false;
    }
    
    public Map<String, List<Message>> getMessages() {
        return Collections.unmodifiableMap(errors);
    }
}
