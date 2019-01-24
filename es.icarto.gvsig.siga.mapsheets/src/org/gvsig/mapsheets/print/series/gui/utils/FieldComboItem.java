package org.gvsig.mapsheets.print.series.gui.utils;

import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.utiles.NumberUtilities;

/**
 * Utility class to represent a field in a combo box.
 * It will be therefore possible to retrieve all its details when user chooses item.
 * 
 * 
 * @author jldominguez
 *
 */
public class FieldComboItem {
	
	private FieldDescription fd = null;
	
	public static FieldComboItem getComboItem(FieldDescription _fd) {
		return new FieldComboItem(_fd);
	}
	
	public FieldComboItem(FieldDescription _fd) {
		fd = _fd;
	}
	
	public String toString() {
		String typestr =
			NumberUtilities.isNumeric(fd.getFieldType()) ? "num" : "txt";
		return fd.getFieldName() + " [" + typestr + "]";
	}
	
	public FieldDescription getFieldDescription() {
		return fd;
	}

}
