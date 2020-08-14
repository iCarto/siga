package es.icarto.gvsig.siga.locatorbycoords;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cresques.cts.IProjection;

import es.icarto.gvsig.commons.referencing.GShape;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

public class LocatorByCoordsModel {

    public final List<CoordProvider> projCodes = new ArrayList<CoordProvider>();

    private CoordProvider defaultInputProj;
    private CoordProvider defaultOuputProj;

    private ZoomTo zoomTo;

    public LocatorByCoordsModel() {

	CoordProvider epsg4326 = new CoordProvider("WGS84", "EPSG:4326");
	GShape extent4326 = new GShape(epsg4326.getProj(),
		new Rectangle2D.Double(Math.abs(-6.734324529), 41.8072541522,
			Math.abs(-9.29885967 - -6.734324529),
			43.7924041112 - 41.8072541522));
	epsg4326.setExtent(extent4326);
	epsg4326.setOuputFormat(SIGAFormatter.latLngFormatter());

	CoordProvider epsg4326dms = new CoordProvider("WGS84 (GMS)", "EPSG:4326");
	GShape extent4326dms = new GShape(epsg4326.getProj(),
		new Rectangle2D.Double(Math.abs(-6.734324529), 41.8072541522,
			Math.abs(-9.29885967 - -6.734324529),
			43.7924041112 - 41.8072541522));
	epsg4326dms.setExtent(extent4326);
	//epsg4326.setOuputFormat(SIGAFormatter.latLngFormatter());

	CoordProvider epsg23029 = new CoordProvider("ED50 UTM29", "EPSG:23029");
	GShape extent23029 = new GShape(epsg23029.getProj(),
		new Rectangle2D.Double(472175.48, 4751978.58,
			691493.78 - 472175.48, 4851029.48 - 600280.42));
	epsg23029.setExtent(extent23029);
	epsg23029.setOuputFormat(SIGAFormatter.utmFormatter());

	CoordProvider epsg25829 = new CoordProvider("ETRS89 UTM29",
		"EPSG:25829");
	GShape extent25829 = new GShape(epsg25829.getProj(),
		new Rectangle2D.Double(472637.79, 4626294.5,
			690330.44 - 472637.79, 4850713.62 - 4626294.5));
	epsg25829.setExtent(extent25829);
	epsg25829.setOuputFormat(SIGAFormatter.utmFormatter());

	projCodes.add(epsg4326);
	projCodes.add(epsg4326dms);
	projCodes.add(epsg23029);
	projCodes.add(epsg25829);
	defaultInputProj = projCodes.get(0);
	defaultOuputProj = projCodes.get(1);
    }

    public CoordProvider getDefaultInputProj() {
	return defaultInputProj;
    }

    public void setDefaultInputProj(CoordProvider defaultInputProj) {
	this.defaultInputProj = defaultInputProj;
    }

    public CoordProvider getDefaultOuputProj() {
	return defaultOuputProj;
    }

    public void setDefaultOuputProj(CoordProvider defaultOuputProj) {
	this.defaultOuputProj = defaultOuputProj;
    }

    public void setDefaultOuputProj(IProjection oProj) {
	for (CoordProvider p : projCodes) {
	    if (p.getProj().getAbrev().equals(oProj.getAbrev())) {
		this.defaultOuputProj = p;
		return;
	    }
	}
    }

    public List<CoordProvider> getProjCodes() {
	return projCodes;
    }

    public void setZoomTo(ZoomTo zoomTo) {
	this.zoomTo = zoomTo;
    }

    public ZoomTo getZoomTo() {
	return zoomTo;
    }

}