package org.gvsig.mapsheets.print.series.fmap;

import org.gvsig.mapsheets.print.series.MapSheetsCreationExtension;

import com.iver.cit.gvsig.fmap.core.CartographicSupportToolkit;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;

/**
 * Symbol used to draw sheet grids (semi-transparent)
 * 
 * @author jldominguez
 *
 */
public class MapSheetGrigSymbol extends SimpleFillSymbol {
	 
	public static final MapSheetGrigSymbol selSymbol =
		new MapSheetGrigSymbol(false,true);
	
	public MapSheetGrigSymbol(boolean edit, boolean sel) {
		
		setOutline(SymbologyFactory.createDefaultLineSymbol());
		getOutline().setLineWidth(1);
		
		if (sel) {
			// sel
			this.getOutline().setLineColor(MapSheetsCreationExtension.GRID_COLOR_SEL_BORDER);
			setFillColor(MapSheetsCreationExtension.GRID_COLOR_SEL_FILL); 
		} else {
			if (edit) {
				// edit
				this.getOutline().setLineColor(MapSheetsCreationExtension.GRID_COLOR_EDIT_BORDER);
				setFillColor(MapSheetsCreationExtension.GRID_COLOR_EDIT_FILL); 
			} else {
				// normal
				this.getOutline().setLineColor(MapSheetsCreationExtension.GRID_COLOR_BORDER);
				setFillColor(MapSheetsCreationExtension.GRID_COLOR_FILL); 
			}
		}
		setUnit(CartographicSupportToolkit.DefaultMeasureUnit);
		setReferenceSystem(CartographicSupportToolkit.DefaultReferenceSystem);
	}
	
	public ISymbol getSymbolForSelection() {
		return selSymbol;
	}
	
	
	
	

}
