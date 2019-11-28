package es.icarto.gvsig.utils;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;

/**
 * Based on: http://stackoverflow.com/a/18004334
 * 
 * You must configure your binary library path to take newer versions of
 * libz.so, libm.so and libm.so.6 that those package with gvsig
 * 
 * An easy way to do that:
 * cd ${workspace_loc}/binaries/linux
 * rm libz.so libm.so libm.so.6
 * ln -s /usr/lib/i386-linux-gnu/libz.so
 * ln -s /usr/lib/i386-linux-gnu/libm.so
 * ln -s /usr/lib/i386-linux-gnu/libm.so.6
 * 
 * 
 */
public class DesktopApi {

    private static final Logger logger = Logger.getLogger(DesktopApi.class);

    public static boolean browse(URI uri) {

	if (browseDESKTOP(uri)) {
	    return true;
	}

	if (openSystemSpecific(uri.toString())) {
	    return true;
	}

	return false;
    }

    public static boolean open(File file) {

	if (openDESKTOP(file)) {
	    return true;
	}

	if (openSystemSpecific(file.getPath())) {
	    return true;
	}

	return false;
    }

    public static boolean edit(File file) {

	// you can try something like
	// runCommand("gimp", "%s", file.getPath())
	// based on user preferences.

	if (editDESKTOP(file)) {
	    return true;
	}

	if (openSystemSpecific(file.getPath())) {
	    return true;
	}

	return false;
    }

    private static boolean openSystemSpecific(String what) {

	EnumOS os = getOs();

	if (os.isLinux()) {
	    if (runCommand("gnome-open", "%s", what)) {
		return true;
	    }

	    if (runCommand("kde-open", "%s", what)) {
		return true;
	    }

	    if (runCommand("xdg-open", "%s", what)) {
		return true;
	    }
	}

	if (os.isMac()) {
	    if (runCommand("open", "%s", what)) {
		return true;
	    }
	}

	if (os.isWindows()) {
	    if (runCommand("explorer", "%s", what)) {
		return true;
	    }
	}

	return false;
    }

    private static boolean browseDESKTOP(URI uri) {

	try {
	    if (!Desktop.isDesktopSupported()) {
		return false;
	    }

	    if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		return false;
	    }

	    Desktop.getDesktop().browse(uri);

	    return true;
	} catch (Throwable t) {
	    logger.error(t.getMessage(), t);
	    return false;
	}
    }

    private static boolean openDESKTOP(File file) {

	try {
	    if (!Desktop.isDesktopSupported()) {
		return false;
	    }

	    if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
		return false;
	    }

	    Desktop.getDesktop().open(file);

	    return true;
	} catch (Throwable t) {
	    logger.error(t.getMessage(), t);
	    return false;
	}
    }

    private static boolean editDESKTOP(File file) {

	try {
	    if (!Desktop.isDesktopSupported()) {
		return false;
	    }

	    if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
		return false;
	    }

	    Desktop.getDesktop().edit(file);

	    return true;
	} catch (Throwable t) {
	    logger.error(t.getMessage(), t);
	    return false;
	}
    }

    private static boolean runCommand(String command, String args, String file) {

	String[] parts = prepareCommand(command, args, file);

	try {
	    Process p = Runtime.getRuntime().exec(parts);

	    if (p == null) {
		return false;
	    }

	    int retval = p.waitFor();
	    
	    if (retval == 0) {
		return true;
	    } else {
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line = null;
		while ((line = br.readLine()) != null) {
		    System.out.println(line);
		}
		return false;
	    }

	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	    return false;
	} catch (InterruptedException e) {
	    logger.error(e.getStackTrace(), e);
	    return false;
	} catch (Throwable e) {
	    logger.error(e.getStackTrace(), e);
	    return false;
	}
    }

    private static String[] prepareCommand(String command, String args,
	    String file) {

	List<String> parts = new ArrayList<String>();
	parts.add(command);

	if (args != null) {
	    for (String s : args.split(" ")) {
		s = String.format(s, file); // put in the filename thing

		parts.add(s.trim());
	    }
	}

	return parts.toArray(new String[parts.size()]);
    }

    public static enum EnumOS {
	linux, macos, solaris, unknown, windows;

	public boolean isLinux() {

	    return this == linux || this == solaris;
	}

	public boolean isMac() {

	    return this == macos;
	}

	public boolean isWindows() {

	    return this == windows;
	}
    }

    public static EnumOS getOs() {

	String s = System.getProperty("os.name").toLowerCase();

	if (s.contains("win")) {
	    return EnumOS.windows;
	}

	if (s.contains("mac")) {
	    return EnumOS.macos;
	}

	if (s.contains("solaris")) {
	    return EnumOS.solaris;
	}

	if (s.contains("sunos")) {
	    return EnumOS.solaris;
	}

	if (s.contains("linux")) {
	    return EnumOS.linux;
	}

	if (s.contains("unix")) {
	    return EnumOS.linux;
	} else {
	    return EnumOS.unknown;
	}
    }
    
    public static void showError(String msg) {
    	JOptionPane.showMessageDialog(
    		(Component) PluginServices.getMainFrame(), msg, "Error",
    		JOptionPane.ERROR_MESSAGE);
   }
}