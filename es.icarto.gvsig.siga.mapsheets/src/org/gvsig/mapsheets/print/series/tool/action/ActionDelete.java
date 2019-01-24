package org.gvsig.mapsheets.print.series.tool.action;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;

/**
 * Undo-able delete action. You can delete several sheets at a time.
 * 
 * @author jldominguez
 *
 */
public class ActionDelete implements ActionOnGrid {

	protected MapSheetGridGraphic[] grfs = null;
	protected MapSheetGrid grid = null;
	
	public ActionDelete(MapSheetGridGraphic[] gg, MapSheetGrid gr) {
		grfs = gg;
		grid = gr;
	}
	
	public int getType() {
		return ActionOnGrid.GRID_ACTION_DELETE;
	}
	
	public boolean undo() {
		
		if (grfs == null || grid == null) {
			return false;
		} else {
			int len = grfs.length;
			for (int i=0; i<len; i++) {
				grid.getTheMemoryDriver().addGraphic(grfs[i], false);
			}
			grid.getTheMemoryDriver().updateExtent();
			return true;
		}
	}

}
