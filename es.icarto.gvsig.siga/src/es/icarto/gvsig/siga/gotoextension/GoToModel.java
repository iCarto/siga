package es.icarto.gvsig.siga.gotoextension;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import es.icarto.gvsig.commons.referencing.GShape;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

public class GoToModel {

    public final static CoordProvider[] projCodes = new CoordProvider[3];

    private CoordProvider defaultInputProj = projCodes[0];
    private CoordProvider defaultOuputProj = projCodes[1];

    private ZoomTo zoomTo;

    public GoToModel() {

	DecimalFormat fGeo = (DecimalFormat) NumberFormat
		.getNumberInstance(Locale.getDefault());
	fGeo.applyPattern("0.#######");
	CoordProvider epsg4326 = new CoordProvider("EPSG:4326");
	GShape extent4326 = new GShape(epsg4326.getProj(),
		new Rectangle2D.Double(Math.abs(-6.734324529), 41.8072541522,
			Math.abs(-9.29885967 - -6.734324529),
			43.7924041112 - 41.8072541522));
	epsg4326.setExtent(extent4326);
	epsg4326.setOuputFormat(fGeo);

	DecimalFormat fUtm = (DecimalFormat) NumberFormat
		.getNumberInstance(Locale.getDefault());
	fUtm.applyPattern("0.###");
	CoordProvider epsg23029 = new CoordProvider("EPSG:23029");
	GShape extent23029 = new GShape(epsg23029.getProj(),
		new Rectangle2D.Double(472175.48, 4751978.58,
			691493.78 - 472175.48, 4851029.48 - 600280.42));
	epsg23029.setExtent(extent23029);
	epsg23029.setOuputFormat(fUtm);

	CoordProvider epsg25829 = new CoordProvider("EPSG:25829");
	GShape extent25829 = new GShape(epsg25829.getProj(),
		new Rectangle2D.Double(472637.79, 4626294.5,
			690330.44 - 472637.79, 4850713.62 - 4626294.5));
	epsg25829.setExtent(extent25829);
	epsg25829.setOuputFormat(fUtm);

	projCodes[0] = epsg4326;
	projCodes[1] = epsg23029;
	projCodes[2] = epsg25829;
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

    public CoordProvider[] getProjCodes() {
	return projCodes;
    }

    public void setZoomTo(ZoomTo zoomTo) {
	this.zoomTo = zoomTo;
    }

    public ZoomTo getZoomTo() {
	return zoomTo;
    }

}