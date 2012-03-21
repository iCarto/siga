package com.iver.cit.gvsig.exceptions.layers;

/**
 * @author Vicente Caballero Navarro
 */
public class XMLLayerException extends LoadLayerException {

	public XMLLayerException(String l,Throwable exception) {
		super(l,exception);
		init();
	}
	/**
	 *
	 */
	private void init() {
		messageKey = "error_xml_layer";
		formatString = "Can�t save and open the layer: %(layer) ";
	}

}
