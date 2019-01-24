package org.gvsig.mapsheets.print.series.gui.utils;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;

/**
 * Utility class to hold info about a single sheet in a list or combo box.
 * 
 * @author jldominguez
 *
 */
public class SheetComboItem {
	
	private MapSheetGridGraphic theitem = null;
	
	public SheetComboItem(MapSheetGridGraphic gphic) {
		theitem = gphic;
	}
	
	public MapSheetGridGraphic getObject() {
		return theitem;
	}
	
	public String toString() {
		return (theitem.getAttributes()[0]).toString();
	}

}
