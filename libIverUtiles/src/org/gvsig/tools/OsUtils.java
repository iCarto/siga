package org.gvsig.tools;

public final class OsUtils {
    private final static String OSNAME = System.getProperty("os.name");

    public static String getOsName() {
	return OSNAME;
    }

    public static boolean isWindows() {
	return OSNAME.toLowerCase().startsWith("Windows");
    }

    public static boolean isLinux() {
	return OSNAME.toLowerCase().startsWith("linux");
	
    }

    public static boolean isMac() {
	return OSNAME.toLowerCase().startsWith("mac os x");
	
    }
}