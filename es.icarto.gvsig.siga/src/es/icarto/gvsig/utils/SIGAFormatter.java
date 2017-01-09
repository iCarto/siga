package es.icarto.gvsig.utils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.icarto.gvsig.commons.format.Format;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.udc.cartolab.gvsig.navtable.format.DoubleFormatNT;
import es.udc.cartolab.gvsig.navtable.format.IntegerFormatNT;

public class SIGAFormatter extends Format {
    private final NumberFormat numberFormat = DoubleFormatNT
	    .getDisplayingFormat();

    @Override
    public String toString(Object o) {
	if (o == null) {
	    return "";
	}
	if ((o instanceof Double) || (o instanceof Float)
		|| (o instanceof BigDecimal)) {
	    return numberFormat.format(o);
	}
	return o.toString();
    }
    
    public static String formatPkForDisplay(String pk) {
	if ((pk == null) || (pk.isEmpty())) {
	    return "";
	}
	String str = pk.trim().replace("+", ",").replace(",", ".");
	try {
	    return String.format("%3.3f", Double.parseDouble(str)).replace(".",
		    ",");
	} catch (NumberFormatException e) {
	    return "";
	}
    }

    public static String formatPkForDouble(String pk) {
	if ((pk == null) || (pk.isEmpty())) {
	    return "";
	}
	String str = pk.trim().replace("+", ",").replace(",", ".");
	try {
	    return String.format("%3.3f", Double.parseDouble(str)).replace(",",
		    ".");
	} catch (NumberFormatException e) {
	    return "";
	}
    }
    
    private final static SimpleDateFormat DATE_FORMAT = DateFormatNT
	    .getDateFormat();
    private final static NumberFormat DOUBLE_FORMAT = DoubleFormatNT
	    .getDisplayingFormat();
    private static final NumberFormat INT_FORMAT = IntegerFormatNT
	    .getDisplayingFormat();
    
    public static String formatValue(Object o) {
	if (o == null) {
	    return "";
	} else if (o instanceof Date) {
	    return DATE_FORMAT.format(o);
	} else if (o instanceof Integer) {
	    return INT_FORMAT.format(o);
	} else if (o instanceof Number) {
	    return DOUBLE_FORMAT.format(o);
	} else if (o instanceof Boolean) {
	    return ((Boolean) o) ? "Sí" : "No";
	}
	return o.toString();
    }
    
    @Deprecated
    public static String writeValue(String value) {
	if (value == null) {
	    return "";
	} else if (value.equalsIgnoreCase("t")) {
	    return "Sí";
	} else if (value.equalsIgnoreCase("f")) {
	    return "No";
	} else if (value.contains(".")) {
	    return value.replace(".", ",");
	} else {
	    return value;
	}
    }

    @Deprecated
    public static String writeDBValueFormatted(ResultSet rs, int column) {
	String valueFormatted = null;
	SimpleDateFormat dateFormat = DateFormatNT.getDateFormat();
	NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
	try {
	    valueFormatted = rs.getString(column);
	    if (rs.getMetaData().getColumnType(column) == 91) {
		Date date = rs.getDate(column);
		valueFormatted = dateFormat.format(date);
		// This is a little 'hack' because of fecha_puesta_servicio
		// is Integer on database instead of Date
	    } else if (rs.getMetaData().getColumnName(column)
		    .equalsIgnoreCase("fecha_puesta_servicio")) {
		valueFormatted = rs.getString(column);
	    } else if (rs.getMetaData().getColumnType(column) == 4
		    || rs.getMetaData().getColumnType(column) == 8
		    || rs.getMetaData().getColumnType(column) == 2) {
		valueFormatted = nf.format(rs.getDouble(column));
	    } else if (rs.getString(column).equals("t")) {
		valueFormatted = "Sí";
	    } else if (rs.getString(column).equals("f")) {
		valueFormatted = "No";
	    } else {
		valueFormatted = rs.getString(column);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return valueFormatted;
    }
    
    public static DecimalFormat latLngFormatter() {
	DecimalFormat fGeo = (DecimalFormat) NumberFormat
		.getNumberInstance(Locale.getDefault());
	fGeo.applyPattern("0.#####");
	return fGeo;
    }
    
    public static DecimalFormat utmFormatter() {
	DecimalFormat fUtm = (DecimalFormat) NumberFormat
		.getNumberInstance(Locale.getDefault());
	fUtm.applyPattern("0.###");
	return fUtm;
    }

}
