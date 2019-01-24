package org.gvsig.mapsheets.print.series.print;

import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;

import com.iver.cit.gvsig.Print;

/**
 * 
 * Used during print process, keeps a reference to the layout template to be printed.
 * 
 * @author jldominguez
 *
 */
public class MapSheetsPrint extends Print {

	public MapSheetsPrint() {

	}

	public MapSheetsPrint(MapSheetsLayoutTemplate tem) {
		this.setLayout(tem);
	}
}
