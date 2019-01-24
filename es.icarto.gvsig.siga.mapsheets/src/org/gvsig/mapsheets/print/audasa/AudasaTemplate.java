package org.gvsig.mapsheets.print.audasa;

import java.util.HashMap;

public class AudasaTemplate {

    private HashMap<String, String> properties = null;

    public AudasaTemplate() {
	properties = new HashMap<String, String>();
    }

    public void setProperty(String key, String value) {
	properties.put(key, value);
    }

    public String getProperty(String key) {
	return properties.get(key);
    }
    
    public boolean hasKey(String key) {
	return properties.containsKey(key);
    }
}
