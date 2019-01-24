package org.gvsig.mapsheets.print.series.gui.utils;

import com.iver.andami.PluginServices;

/**
 * Used to keep item in measure unit combo box.
 * 
 * @author jldominguez
 *
 */
public class MeasureUnitComboItem {

	String nameKey = "";
	double metersPerUnit = 1; 
	
	public MeasureUnitComboItem(double metersperunit, String name_key) {
		metersPerUnit = metersperunit;
		nameKey = PluginServices.getText(this, name_key);
	}
	
	public String toString() {
		return nameKey;
	}
	
	public double getMetersPerUnit() {
		return metersPerUnit;
	}
	
	public static final MeasureUnitComboItem MEASURE_UNIT_MM = new MeasureUnitComboItem(0.001, "milimeter");
	public static final MeasureUnitComboItem MEASURE_UNIT_CM = new MeasureUnitComboItem(0.01, "centimeter");
}
