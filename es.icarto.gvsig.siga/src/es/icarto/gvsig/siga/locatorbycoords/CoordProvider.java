package es.icarto.gvsig.siga.locatorbycoords;

import java.awt.geom.Rectangle2D;
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

    public CoordProvider(String name) {
	this.name = name;
	this.format = DoubleFormatNT.getDisplayingFormat();
	this.outputformat = this.format;
	this.transform = new PostgisTransformation();
	this.proj = CRSFactory.getCRS(name);

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

    public String[] transform(String inputX, String inputY, Object selectedItem) {
	IProjection oProj = null;

	if ((selectedItem instanceof CoordProvider)) {
	    oProj = ((CoordProvider) selectedItem).getProj();
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
	return new String[] { outputformat.format(oPoint.getX()),
		outputformat.format(oPoint.getY()) };
    }

    public GPoint toGPoint(String inputX, String inputY) {
	double x = normalize(inputX).doubleValue();
	double y = normalize(inputY).doubleValue();
	return new GPoint(this.getProj(), x, y);
    }
}
