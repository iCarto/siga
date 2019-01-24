package org.gvsig.mapsheets.print.series.gui.utils;

/**
 * Utility interface for objects which need to listen to progress.
 * 
 * @author jldominguez
 *
 */
public interface IProgressListener {
	
	public void started();
	public void progress(int done, int tot);
	public void cancelled(String msg);
	public void finished();

}
