package org.gvsig.mapsheets.print.series.tool.action;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;

import com.iver.cit.gvsig.fmap.core.IGeometry;

/**
 * Undo-able move action. You can move several frames at a time.
 *  
 * @author jldominguez
 *
 */
public class ActionMove implements ActionOnGrid {

	protected MapSheetGridGraphic[] grfs = null;
	protected AffineTransform undoT = null;
	
	public ActionMove(MapSheetGridGraphic[] gg, Point2D offs) {
		grfs = gg;
		undoT = AffineTransform.getTranslateInstance(-offs.getX(), -offs.getY());
	}
	
	public int getType() {
		return ActionOnGrid.GRID_ACTION_MOVE;
	}

	public boolean undo() {
		
		if (grfs == null || grfs.length == 0) {
			return false;
		}
		
		int len = grfs.length;
		MapSheetGridGraphic item = null;
		for (int i=0; i<len; i++) {
			item = grfs[i];
			undoOffset(item);
		}
		return true;
	}

	private void undoOffset(MapSheetGridGraphic mg) {
		IGeometry geo = mg.getGeom();
		geo.transform(undoT);
	}

}
