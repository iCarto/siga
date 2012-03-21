package com.iver.cit.gvsig.exceptions.layers;

/**
 * @author Vicente Caballero Navarro
 */
public class URLLayerException extends LoadLayerException {

	public URLLayerException(String l,Throwable exception) {
		super(l,exception);
		init();
	}
	/**
	 *
	 */
	private void init() {
		messageKey = "error_url_layer";
		formatString = "Can�t create the url of the layer: %(layer) ";
	}

}
