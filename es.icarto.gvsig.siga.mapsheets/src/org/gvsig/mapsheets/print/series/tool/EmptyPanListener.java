package org.gvsig.mapsheets.print.series.tool;

import java.awt.Cursor;

import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.Events.MoveEvent;
import com.iver.cit.gvsig.fmap.tools.Listeners.PanListener;

/**
 * Dummy listener needed to comply with constructor.
 * 
 * @author jldominguez
 *
 */
public class EmptyPanListener implements PanListener {

	public EmptyPanListener() {}
	
	public void move(MoveEvent event) throws BehaviorException {
	}

	
	public boolean cancelDrawing() {
		
		return false;
	}

	
	public Cursor getCursor() {
		
		return null;
	}

}
