package com.iver.utiles.extensionPoints;

public class ExtensionPointsSingleton extends ExtensionPoints {

	private static final long serialVersionUID = -630976693542039111L;
	
	private static ExtensionPoints extensionPoints = new ExtensionPointsSingleton();

	private ExtensionPointsSingleton() {
		super();
	}

	public static ExtensionPoints getInstance() {
		return ExtensionPointsSingleton.extensionPoints;
	}
}
