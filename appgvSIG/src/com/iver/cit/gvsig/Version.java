package com.iver.cit.gvsig;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.iver.cit.gvsig.gui.panels.FPanelAbout;

public class Version {
	private static Logger logger = Logger.getLogger(Version.class.getName());
	/**
	 * @deprecated Use method getMajor 
	 */
	public static int MAJOR_VERSION_NUMBER = 0;
	/**
	 * @deprecated Use method getMinor 
	 */
	public static int MINOR_VERSION_NUMBER = 0;
	/**
	 * @deprecated Use method getRelease 
	 */
	public static int RELEASE_NUMBER = 0;
	private static boolean loaded = false;

	private static String BUILD = null;
	private static String STATE = null;

	static {
		Version version = new Version();
		version.loadVersion();
	}
	/**
	 * @deprecated Use method getFormat 
	 */
	public static String format() {

		Version version = new Version();
		return version.getFormat();
	}

	/**
	 * @deprecated Use method getLongFormat 
	 */
	public static String longFormat() {

		Version version = new Version();
		return version.getLongFormat();
	}
	
	/**
	 * @deprecated Use method getBuildId 
	 */
	public static String getBuild() {

		Version version = new Version();
		return version.getBuildId();
	}
	
	private void loadVersion() {
		if (!loaded) {
			try {
				Properties props=new Properties();
				props.load(FPanelAbout.class.getResourceAsStream("/package.info"));
				String version = props.getProperty("version", "0.0.0");
				String[] versionSplit = version.split("[.]");
				MAJOR_VERSION_NUMBER = Integer.parseInt(versionSplit[0]);
				MINOR_VERSION_NUMBER = Integer.parseInt(versionSplit[1]);
				RELEASE_NUMBER = Integer.parseInt(versionSplit[2]);
				STATE = props.getProperty("state", "");
				props=new Properties();
				props.load(FPanelAbout.class.getResourceAsStream("/build.number"));
				BUILD = props.getProperty("build.number", "0");
				loaded = true;
			} catch (Exception e) {
				logger.error("cant_load_properties", e);
			}

		}
		
	}
	
	
	public int getMinor() {
		loadVersion();
		return MINOR_VERSION_NUMBER;
	}

	public int getMajor() {
		loadVersion();
		return MAJOR_VERSION_NUMBER;
	}

	public int getRelease() {
		loadVersion();
		return RELEASE_NUMBER;
	}

	public String getBuildId() {
		loadVersion();
		return BUILD;
	}

	public String getState() {
		loadVersion();
		return STATE;
	}

	public String toString() {
		return getLongFormat();		
	}

    public String getFormat() {
		return this.getMajor() +"."+this.getMinor()+"."+this.getRelease();
    }

    public String getLongFormat() {
		return this.getMajor() + "." + this.getMinor() + "." + this.getRelease() + 
		" " + this.getState() + " (Build " + this.getBuildId() + ")";
    }

}
