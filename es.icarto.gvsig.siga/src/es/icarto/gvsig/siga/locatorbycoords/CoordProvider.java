package es.icarto.gvsig.siga.locatorbycoords;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.fmap.crs.CRSFactory;

import es.icarto.gvsig.commons.referencing.GPoint;
import es.icarto.gvsig.commons.referencing.GShape;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;
import es.udc.cartolab.gvsig.navtable.format.DoubleFormatNT;

public class CoordProvider {

    private final String name;
    private final NumberFormat format;
    private final PostgisTransformation transform;
    private final IProjection proj;
    private GShape extent;
    private NumberFormat outputformat;

    public CoordProvider(String name, String proj) {
	this.name = name;
	this.format = DoubleFormatNT.getDisplayingFormat();
	this.outputformat = this.format;
	this.transform = new PostgisTransformation();
	this.proj = CRSFactory.getCRS(proj);

	// https://www.maptools.com/tutorials/utm/details
	// https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system

	// FIXME, que pasa si es geográfico
	extent = new GShape(this.proj, new Rectangle2D.Double(160000, 0,
		834000 - 160000, 9300000 - 0));
    }

    public void setExtent(GShape extent) {
	GShape newExtent = transform.transform(extent, this.proj);
	this.extent = newExtent == null ? this.extent : newExtent;
    }

    public void setOuputFormat(NumberFormat format) {
	this.outputformat = format;
    }

    public NumberFormat getOutputformat() {
	return outputformat;
    }

    @Override
    public String toString() {
	return name;
    }

    public IProjection getProj() {
	return this.proj;
    }

    public boolean validX(String value) {
	Number v = normalize(value);
	if (v == null) {
	    return false;
	}

	return extent.contains(Math.abs(v.doubleValue()), extent.getBounds2D()
		.getCenterY());
    }

    private Number normalize(String value) {

	ParsePosition pp = new ParsePosition(0);
	Number n = format.parse(value, pp);
	if (value.trim().length() != pp.getIndex() || n == null) {
	    return null;
	}

	return n;
    }

    public boolean validY(String value) {
	Number v = normalize(value);
	if (v == null) {
	    return false;
	}

	return extent.contains(extent.getBounds2D().getCenterX(),
		Math.abs(v.doubleValue()));
    }

    public boolean validDMSValue(String value) {
    Number v = normalize(value);
    if (v == null) {
        return false;
    }else {
        return true;
    }
    }
    
    public boolean numberIsPositive(String value) {
    Number v = normalize(value);
    boolean isPositive = (v.doubleValue() >0)?true:false;
    return isPositive;
    }
    
    public String[] transform(String inputX, String inputY, Object selectedItem) {
	IProjection oProj = null;

	NumberFormat oFormat = outputformat;
	if ((selectedItem instanceof CoordProvider)) {
	    oProj = ((CoordProvider) selectedItem).getProj();
	    oFormat = ((CoordProvider) selectedItem).getOutputformat();
	} else if (selectedItem instanceof IProjection) {
	    oProj = (IProjection) selectedItem;
	}

	if (oProj == null) {
	    throw new IllegalArgumentException("This should not happen");
	}

	if (!validX(inputX) || !validY(inputY)) {
	    throw new IllegalArgumentException("This should not happen");
	}

	double x = normalize(inputX).doubleValue();
	double y = normalize(inputY).doubleValue();
	GPoint point = new GPoint(this.getProj(), x, y);
	GPoint oPoint = transform.transform(point, oProj);
	return new String[] { oFormat.format(oPoint.getX()),
		oFormat.format(oPoint.getY()) };
    }

    public GPoint toGPoint(String inputX, String inputY) {
	double x = normalize(inputX).doubleValue();
	double y = normalize(inputY).doubleValue();
	return new GPoint(this.getProj(), x, y);
    }
    
    public String dmsToDecimalDegrees(String d, String m, String s, String zone) {
    double degrees = 0;
    double minutes = 0;
    double seconds = 0;
    if (!d.isEmpty()) {
        degrees = normalize(d).doubleValue();
    }
    if (!m.isEmpty()) {
        minutes = normalize(m).doubleValue();
    }
    if (!s.isEmpty()) {
        seconds = normalize(s).doubleValue();
    }
    double decimal = Math.signum(degrees) * (Math.abs(degrees) + (minutes/60) + (seconds/3600));
    if (zone.equalsIgnoreCase("S") || zone.equalsIgnoreCase("O")) {
        decimal = decimal * (-1);
    }
    return format.format(decimal);
    }
    
    public String toWGS84DMS(String value, String type) {
    double decimalDegrees = normalize(value).doubleValue();
    double degrees = (int) decimalDegrees;
    double remaining = Math.abs(decimalDegrees - degrees);
    double minutes = (int) (remaining * 60);
    remaining = remaining * 60 - minutes;
    double seconds = new BigDecimal(remaining * 60).setScale(4, RoundingMode.HALF_UP).doubleValue();
    
    String dms = format.format(Math.abs(degrees)) + "° " + format.format(minutes) + "' " + format.format(seconds) + "'' ";
    String zone = null;
    if (type.equalsIgnoreCase("lat")) {
        zone=(degrees>0)?"N":"S";
    }
    if (type.equalsIgnoreCase("lon")) {
        zone=(degrees>0)?"E":"O";
    }
    
    return dms + zone;
    
    }
    
    public String toWGS84(String value, String type) {
    double decimal = normalize(value).doubleValue();
    String zone = null;
    if (type.equalsIgnoreCase("lat")) {
        zone=(decimal>0)?"N":"S";
    }
    if (type.equalsIgnoreCase("lon")) {
        zone=(decimal>0)?"E":"O";
    }
    if (decimal < 0) {
        decimal = decimal * (-1);
    }
    return String.valueOf(decimal) + " " + zone;
    }
}
