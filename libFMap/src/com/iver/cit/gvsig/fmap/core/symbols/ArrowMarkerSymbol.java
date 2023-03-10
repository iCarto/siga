/*
 * Created on 25-oct-2006 by azabala
 *
 */
package com.iver.cit.gvsig.fmap.core.symbols;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;


/**
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class ArrowMarkerSymbol extends AbstractMarkerSymbol implements CartographicSupport {
	private ArrowMarkerSymbol symSel;
	private double sharpeness;


	
	public ISymbol getSymbolForSelection() {
		if (symSel == null) {
			symSel = new ArrowMarkerSymbol();
			symSel.setColor(MapContext.getSelectionColor());

		}

		return symSel;
	}


	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("isShapeVisible", isShapeVisible());
		xml.putProperty("desc", getDescription());
		xml.putProperty("size", getSize());
		xml.putProperty("unit", getUnit());
		xml.putProperty("referenceSystem", getReferenceSystem());
		xml.putProperty("sharpness", getSharpness());
		xml.putProperty("color", StringUtilities.color2String(getColor()));
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		setDescription(xml.getStringProperty("desc"));
		setIsShapeVisible(xml.getBooleanProperty("isShapeVisible"));
		setSize(xml.getDoubleProperty("size"));
		setUnit(xml.getIntProperty("unit"));
		setReferenceSystem(xml.getIntProperty("referenceSystem"));
		setSharpness(xml.getDoubleProperty("sharpness"));
		setColor(StringUtilities.string2Color(xml.getStringProperty("color")));
	}

	public String getClassName() {
		return getClass().getName();
	}

	/**
	 * To get the sharpeness attribute.It will determine the final form of the arrow
	 * @return
	 */
	public double getSharpness() {
		return sharpeness;
	}

	/**
	 * To set the sharpeness.It will determine the final form of the arrow
	 * @param sharpeness, the value of the arrow's edge angle IN RADIANS
	 * @see #getSharpeness()
	 */
	public void setSharpness(double sharpeness) {
		this.sharpeness = sharpeness;
	}

	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp, Cancellable cancel) {

		FPoint2D p = (FPoint2D) shp;
		double radian_half_sharpeness = Math.toRadians(getSharpness()*.5); //

		double size = getSize();
		double halfHeight = size * Math.tan(radian_half_sharpeness);
		double theta = getRotation();

		g.setColor(getColor());

		g.setStroke(new BasicStroke());

		if (p == null) return;
		GeneralPathX gp = new GeneralPathX();
		gp.moveTo(0, 0);
		gp.lineTo(size, -halfHeight);
		gp.lineTo(size, halfHeight);
		gp.closePath();

		try {
			g.translate(p.getX(), p.getY());
		} catch (NullPointerException npEx) {
			return;
		}
		g.rotate(theta);

		g.fill(gp);
		g.rotate(-theta);
		g.translate(-p.getX(), -p.getY());

	}
}
