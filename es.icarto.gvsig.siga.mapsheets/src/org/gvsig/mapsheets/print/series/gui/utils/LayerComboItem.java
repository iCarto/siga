package org.gvsig.mapsheets.print.series.gui.utils;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;

import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

/**
 * Utility class to represent a layer in a combo box. 
 * Keeps a ref. to layer to be used when user chooses item
 * 
 * @author jldominguez
 *
 */
public class LayerComboItem {
	
	private FLayer lyr = null;
	private String str = "[not init]";
	
	public LayerComboItem(FLayer ly) {
		try {
			setLayer(ly);
		} catch (Exception ex) { }
	}
	
	public void setLayer(FLayer _lyr) throws Exception {
		
		if (_lyr == null) {
			return;
		}
		lyr = _lyr;
		str = lyr.getName() + getLayerType(lyr);		
	}

	private String getLayerType(FLayer la) throws Exception {
		
		FLyrVect vect = null;
		if (la instanceof FLyrVect) {
			vect = (FLyrVect) la;
		} else {
			if (la instanceof MapSheetGrid) {
				return " [grid]";
			} else {
				if (la.getClass().getName().toLowerCase().indexOf("raster") != -1) {
					return " [raster]";
				} else {
					return " [other]";
				}
			}
		}
		
		int t = vect.getShapeType();
		if (t == FShape.LINE) {
			return " [line]";
		} else {
			if (t == FShape.POINT) {
				return " [point]";
			} else {
				if (t == FShape.POLYGON) {
					return " [polygon]";
				} else {
					return "";
				}
			}
		}
	}
	
	public FLayer getLayer() {
		return lyr;
	}
	
	public String toString() {
		return str;
	}

}
