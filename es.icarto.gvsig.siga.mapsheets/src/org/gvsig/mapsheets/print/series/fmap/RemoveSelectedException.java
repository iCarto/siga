package org.gvsig.mapsheets.print.series.fmap;

/**
 * Exception class to indicate the special case when several frames are being deleted
 *  
 * @author jldominguez
 *
 */
public class RemoveSelectedException extends Exception {

	private int n = 0;
	
	public RemoveSelectedException(int i) {
		n = i;
	}
	
	public int getN() {
		return n;
	}
		
}
