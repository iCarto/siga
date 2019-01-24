package org.gvsig.mapsheets.print.series.tool.action;

import java.util.ArrayList;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;

/**
 * Undo-able add action. You can only add one sheet at a time.
 * 
 * @author jldominguez
 *
 */
public class ActionAdd implements ActionOnGrid {

	protected MapSheetGridGraphic[] grfs = null;
	protected MapSheetGrid grid = null;
	
	public ActionAdd(MapSheetGridGraphic[] gg, MapSheetGrid gr) {
		grfs = gg;
		grid = gr;
	}
	
	public int getType() {
		return ActionOnGrid.GRID_ACTION_ADD;
	}

	public boolean undo() {
		if (grfs == null || grid == null) {
			return false;
		} else {
			
			ArrayList sel_geoms = null;
			sel_geoms = grid.getSelectedGraphics();

			int len = grfs.length;
			for (int i=0; i<len; i++) {
				grid.getTheMemoryDriver().removeGraphic(grfs[i], false);
			}
			grid.getTheMemoryDriver().updateExtent();
			grid.setSelectedGraphics(sel_geoms);
			return true;
		}
	}

}
