package es.icarto.gvsig.extgia.utils;

public class Utils {

    public static String writeValue(String value) {
	if (value == null) {
	    return "";
	}else if (value.equalsIgnoreCase("t")) {
	    return "S�";
	}else if (value.equalsIgnoreCase("f")) {
	    return "No";
	}else {
	    return value;
	}
    }

}
