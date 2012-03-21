package com.iver.cit.gvsig.exceptions.visitors;


/**
 * @author Vicente Caballero Navarro
 */
public class ProcessWriterVisitorException extends ProcessVisitorException {
	public ProcessWriterVisitorException(String layer,Throwable exception) {
		super(layer,exception);
		init();
		// initCause(exception);
	}

	private void init() {
		messageKey = "error_process_writer_visitor";
		formatString = "Can�t process writer visitor the layer: %(layer) ";
	}
}

// [eiel-gestion-excepciones]
