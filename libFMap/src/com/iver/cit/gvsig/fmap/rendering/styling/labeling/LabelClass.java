/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */

/* CVS MESSAGES:
 *
 * $Id: LabelClass.java 13953 2007-09-21 12:26:04Z jaume $
 * $Log$
 * Revision 1.14  2007-09-21 12:26:04  jaume
 * cancellation support extended down to the IGeometry and ISymbol level
 *
 * Revision 1.13  2007/09/17 14:16:11  jaume
 * multilayer symbols sizing bug fixed
 *
 * Revision 1.12  2007/08/22 09:48:13  jvidal
 * javadoc
 *
 * Revision 1.11  2007/05/09 11:04:58  jaume
 * refactored legend hierarchy
 *
 * Revision 1.10  2007/05/08 08:47:40  jaume
 * *** empty log message ***
 *
 * Revision 1.9  2007/04/26 11:41:00  jaume
 * attempting to let defining size in world units
 *
 * Revision 1.8  2007/04/18 15:35:11  jaume
 * *** empty log message ***
 *
 * Revision 1.7  2007/04/12 14:28:43  jaume
 * basic labeling support for lines
 *
 * Revision 1.6  2007/04/11 16:01:08  jaume
 * maybe a label placer refactor
 *
 * Revision 1.5  2007/04/10 16:34:01  jaume
 * towards a styled labeling
 *
 * Revision 1.4  2007/04/05 16:07:14  jaume
 * Styled labeling stuff
 *
 * Revision 1.3  2007/04/02 16:34:56  jaume
 * Styled labeling (start commiting)
 *
 * Revision 1.2  2007/03/09 08:33:43  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.6  2007/02/15 16:23:44  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.5  2007/02/09 07:47:05  jaume
 * Isymbol moved
 *
 * Revision 1.1.2.4  2007/02/02 16:21:24  jaume
 * start commiting labeling stuff
 *
 * Revision 1.1.2.3  2007/02/01 17:46:49  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.2  2007/02/01 11:42:47  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.1  2007/01/30 18:10:45  jaume
 * start commiting labeling stuff
 *
 *
 */
package com.iver.cit.gvsig.fmap.rendering.styling.labeling;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.ext.awt.geom.PathLength;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.CartographicSupportToolkit;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.styles.ILabelStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.utiles.IPersistence;
import com.iver.utiles.XMLEntity;

/**
 *
 * LabelClass is the model of the label in the new simbology of gvSIG. In this
 * class is contained its definition, the expresion that defines the text which
 * is going to be showed, if it will be visible or not, the text symbol that is
 * going to paint the label and the style for its background.
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class LabelClass implements IPersistence, CartographicSupport {
	private String name;
	private ITextSymbol textSymbol;
	private String[] labelExpressions;
	private boolean isVisible = true;
	private ILabelStyle labelStyle;
	private String[] texts;
	private int priority;
	private double scale = 1;
	private String sqlQuery;
	private boolean useSqlQuery = false;


	/**
	 * Returns true if the label will be showed in the map
	 *
	 * @return isVisible boolean
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Sets the visibility of the label in the map.
	 *
	 * @param isVisible boolean
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * Returns the expression that defines the text which will be showed in
	 * the label
	 *
	 * @return labelExpressions String[]
	 */
	public String[] getLabelExpressions() {
		return labelExpressions;
	}
	/**
	 * Returns the expression that defines the text which will be showed in
	 * the label ready to be used for the label parser
	 *
	 * @return labelExpression String
	 */
	public String getStringLabelExpression(){
		String expr = "";

		if(labelExpressions != null && labelExpressions.length > 0){

			for (int i = 0; i < labelExpressions.length-1; i++) {
				expr += (String) labelExpressions[i] +  ":";//EOFIELD
			}
			expr += labelExpressions[labelExpressions.length - 1] +";";//EOEXPRESSION

		}
		else expr = ";";
		return expr;
	}


	/**
	 * Stablishes the expresion that, when it is evaluated, returns the text
	 * which will be showed by the label.
	 *
	 * @param labelExpression String
	 */
	public void setLabelExpressions(String[] labelExpressions) {
		this.labelExpressions = labelExpressions;
	}

	/**
	 * Returns the text symbol that is being used for the text(the font,
	 * size,style,aligment)
	 *
	 * @return label ITextSymbol
	 */
	public ITextSymbol getTextSymbol() {
		if (textSymbol == null) {
			textSymbol = new SimpleTextSymbol();
		}
		return textSymbol;
	}

	private Dimension getSize() {
		if (labelStyle == null) {
			if (texts!=null && texts.length >0) {
				String t = "";
				for (int i = 0; i < texts.length; i++) {
					t += texts[i];
				}
				getTextSymbol().setText(t);
			}

			Rectangle bounds = getTextSymbol().getBounds();
//			bounds.setLocation(
//					(int) Math.round(bounds.getX()),
//					(int) Math.round(bounds.getY()+bounds.getHeight()));
			return new Dimension(bounds.width, bounds.height);
		} else {
			labelStyle.setTextFields(texts);
			return labelStyle.getSize();
		}
	}
	/**
	 * Stablishes the text symbol that is going to be used for the text(the
	 * font,size,style,aligment)
	 *
	 * @param textSymbol ITextSymbol
	 */
	public void setTextSymbol(ITextSymbol textSymbol) {
		this.textSymbol = textSymbol;
		if (textSymbol == null) {
			this.textSymbol = new SimpleTextSymbol();
		}
		setReferenceSystem(referenceSystem);
		setUnit(unit);
	}

	/**
	 * Stablishes the style for the label.
	 *
	 * @param labelStyle ILabelStyle
	 */
	public void setLabelStyle(ILabelStyle labelStyle) {
		this.labelStyle = labelStyle;
	}

	/**
	 * Returns the style of the label
	 *
	 */
	public ILabelStyle getLabelStyle() {
		return this.labelStyle;
	}

	/**
	 * Returns the name of the label
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stablishes the name of the label
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		// for debugging
//		return name+"{label expression="+labelExpression+", visible="+isVisible+", priority="+priority+"}";
		return getName();
	}

	/**
	 * Sets the text for the label
	 *
	 * @param texts String[]
	 */
	public void setTexts(String[] texts) {
		this.texts = texts;

	}

	/**
	 * Return the text for the label
	 *
	 * @param texts String[]
	 */
	public String[] getTexts() {
		return this.texts;
	}


	/**
	 * <p>
	 * LabelLocationMetrics, contains the anchor point, rotation, and some
	 * other geometric calculations computed by the PlacementManager.
	 * </p>
	 *
	 * <p>
	 * The shp argument is passed as an accessory for subclasses of this
	 * class in case they need futher geometric calculations
	 * </p>
	 * @param graphics, graphics to use to paint the label.
	 * @param llm, concrete settings of the placement of this layer
	 * @param shp, the Shape over whose the label is painted
	 */
	public void draw(Graphics2D graphics, LabelLocationMetrics llm, FShape shp) {
		if (scale == 0)
			return;
		if (!isVisible)
			return;
		Dimension size = getSize();
		int width = (int) Math.round(size.getWidth()*scale);
		if (width  < 1)
			return;

		int height = (int) Math.round(size.getHeight()*scale);
		if (height < 1)
			return;

		Rectangle r = new Rectangle(0,0, width, height);
		FPoint2D anchor = new FPoint2D(llm.getAnchor());
		double xAnchor = anchor.getX();
		double yAnchor = anchor.getY();
		double theta = llm.getRotation();

		if (theta < 0) {
			theta = theta + (2 * Math.PI);
		}

		if ((theta > (Math.PI / 2)) &&
			(theta < ((3 * Math.PI) / 2))) {
			theta = theta - (float) Math.PI;
		}


//		PathLength pathLen = new PathLength(shp);
//
//		// if (pathLen.lengthOfPath() < width / mT.getScaleX()) return;
//		float midDistance = pathLen.lengthOfPath() / 2;
//		pAux = pathLen.pointAtLength(midDistance);
//		angle = pathLen.angleAtLength(midDistance);

//		if (angle < 0) {
//			angle = angle + (float) (2 * Math.PI);
//		}
//
//		if ((angle > (Math.PI / 2)) &&
//				(angle < ((3 * Math.PI) / 2))) {
//			angle = angle - (float) Math.PI;
//		}
//
//		theLabel.setRotation(Math.toDegrees(angle));




		graphics.translate(xAnchor, yAnchor);
		graphics.rotate(theta);
		synchronized (this) {
			float fontSizeBefore = textSymbol.getFont().getSize2D();
			try {
				textSymbol.setFontSize(fontSizeBefore*scale);
				drawInsideRectangle(graphics, r);
				textSymbol.setFontSize(fontSizeBefore);
			} catch (SymbolDrawingException e) {
				e.printStackTrace();
			}
		}
		graphics.rotate(-theta);
		graphics.translate(-xAnchor, -yAnchor);

	}


	private void relativeToAbsolute(double[] xy, Rectangle r, Dimension labelSz, double ratioLabel, double ratioViewPort) {
		int x;
		int y;
		if (ratioViewPort > ratioLabel) {
			// size is defined by the viewport height
			y = (int) (r.height*xy[1]);
			x = (int) ((0.5*r.width) - (0.5-xy[0])*(ratioLabel*r.height));
		} else {
			// size is defined by the viewport width
			x = (int) (r.width * xy[0]);
			y = (int) ((0.5 * r.height) - (0.5-xy[1])*(r.width/ratioLabel));
		}
		xy[0] = x;
		xy[1] = y;
	}

	/**
	 * Useful to render a Label with size inside little rectangles.
	 *
	 * @param graphics Graphics2D
	 * @param bounds Rectangle
	 * @throws SymbolDrawingException
	 */
	public void drawInsideRectangle(Graphics2D graphics, Rectangle bounds) throws SymbolDrawingException {
		if (labelStyle != null) {
			labelStyle.drawInsideRectangle(graphics, bounds);
			Rectangle2D[] textBounds = labelStyle.getTextBounds();
			Dimension labelSz = getSize();
			final double ratioLabel = labelSz.getWidth()/labelSz.getHeight();
			final double ratioViewPort = bounds.getWidth() / bounds.getHeight();
			final double[] xy = new double[2];


			// draw the text fields
			if (textBounds.length > 0 && texts!=null) {
				for (int i = 0; i < textBounds.length && i < texts.length; i++) {
					getTextSymbol().setText(texts[i]);
					Rectangle2D textFieldArea = textBounds[i];
					xy[0] = textFieldArea.getX();
					xy[1] = textFieldArea.getY();
					relativeToAbsolute(xy, bounds, labelSz, ratioLabel, ratioViewPort);
					int x = (int) Math.round(xy[0]);
					int y = (int) Math.round(xy[1]);

					xy[0] = textFieldArea.getMaxX();
					xy[1] = textFieldArea.getMaxY();
					relativeToAbsolute(xy, bounds, labelSz, ratioLabel, ratioViewPort);
					int width = (int) Math.round(xy[0]) -x;
					int height = (int) Math.round(xy[1] - y) ;

					Rectangle textRect = new Rectangle(x, y, width, height);
//					Shape oldClip = graphics.getClip();
//					graphics.setClip(textRect);
					getTextSymbol().drawInsideRectangle(graphics, null, textRect, null);
//					graphics.setClip(oldClip);
//					graphics.draw(textRect);
				}
			}
		} else {

			if (texts != null && texts.length>0)
				getTextSymbol().setText(texts[0]);
			getTextSymbol().drawInsideRectangle(graphics, null, bounds, null);
		}
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public FShape getShape(LabelLocationMetrics llm) {
		if (llm==null)
			return null;
		Point2D anchor = llm.getAnchor();
		FPoint2D p = new FPoint2D(anchor);
		double theta = llm.getRotation();

		// 2. calculate the container shape
		FShape returnedValue;
		Rectangle bounds = getBounds();

		AffineTransform at = AffineTransform.getTranslateInstance(p.getX(), p.getY());
		at.concatenate(AffineTransform.getRotateInstance(theta));

		returnedValue = new FPolygon2D(new GeneralPathX(bounds));

		returnedValue.transform(at);
		return returnedValue;
	}


	public String getClassName() {
		return getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("isVisible", isVisible);
		xml.putProperty("name", name);
		if(labelExpressions != null)
			xml.putProperty("labelExpressions", labelExpressions);
		xml.putProperty("unit", getUnit());
		xml.putProperty("referenceSystem", getReferenceSystem());
		xml.putProperty("priority", getPriority());
		xml.putProperty("useSqlQuery",isUseSqlQuery());

		if (texts != null) {
			xml.putProperty("texts", texts);
		}
		if (sqlQuery != null) {
			xml.putProperty("sqlQuery", getSQLQuery());
		}

		if (labelStyle!=null) {
			XMLEntity labelStyleXML = labelStyle.getXMLEntity();
			labelStyleXML.putProperty("id", "labelStyle");
			xml.addChild(labelStyleXML);
		}

		XMLEntity textSymXML = getTextSymbol().getXMLEntity();
		textSymXML.putProperty("id", "TextSymbol");
		xml.addChild(textSymXML);

		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		isVisible = xml.getBooleanProperty("isVisible");
		name = xml.getStringProperty("name");
		if(xml.contains("labelExpressions"))
			labelExpressions = xml.getStringArrayProperty("labelExpressions");
		setUnit(xml.getIntProperty("unit"));
		setReferenceSystem(xml.getIntProperty("referenceSystem"));
		setTextSymbol(
				(ITextSymbol) SymbologyFactory.
				createSymbolFromXML(xml.firstChild("id", "TextSymbol"), null));

		if(xml.contains("useSqlQuery")){
			setUseSqlQuery(xml.getBooleanProperty("useSqlQuery"));
		}

		if (xml.contains("texts")) {
			setTexts(xml.getStringArrayProperty("texts"));
		}
		if (xml.contains("sqlQuery")) {
			setSQLQuery(xml.getStringProperty("sqlQuery"));
		}

		if (xml.contains("priority")) {
			setPriority(xml.getIntProperty("priority"));
		}
		// labelStyle
		XMLEntity aux = xml.firstChild("id", "labelStyle");
		if (aux!= null) {
			setLabelStyle((ILabelStyle) SymbologyFactory.
							createStyleFromXML(aux, "labelStyle"));
		}
	}

	private int unit = CartographicSupportToolkit.DefaultMeasureUnit;
	private int referenceSystem = CartographicSupportToolkit.DefaultReferenceSystem;

	public double getCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		Dimension sz = getSize();
		double width = sz.getWidth();
		double height = sz.getHeight();
		return CartographicSupportToolkit.
			getCartographicLength(this,
							  Math.max(width, height),
							  viewPort,
							  dpi);
	}

	public int getReferenceSystem() {
		return referenceSystem;
	}

	public int getUnit() {
		return unit;
	}

	public void setCartographicSize(double cartographicSize, FShape shp) {
		Dimension sz = getSize();
		double width = sz.getWidth();
		double height = sz.getHeight();
		if (width >= height) {
			scale = cartographicSize / width;
		} else {
			scale = cartographicSize / height;
		}
	}

	public void setReferenceSystem(int referenceSystem) {
		this.referenceSystem = referenceSystem;
		if (textSymbol != null && textSymbol instanceof CartographicSupport) {
			((CartographicSupport) textSymbol).setReferenceSystem(referenceSystem);
		}
	}

	public void setUnit(int unitIndex) {
		this.unit = unitIndex;
		if (textSymbol != null && textSymbol instanceof CartographicSupport) {
			((CartographicSupport) textSymbol).setUnit(unitIndex);
		}
	}

	public double toCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		setCartographicSize(getCartographicSize(
								viewPort,
								dpi,
								shp),
							shp);
		return 0;
	}

	public Rectangle getBounds() {
		Dimension cBounds = getSize();
		return new Rectangle(
				0,
				0,
				(int) Math.round(cBounds.width*scale),
				(int) Math.round(cBounds.height*scale));
	}

	public String getSQLQuery() {
		if (sqlQuery == null) sqlQuery = "";
		return sqlQuery;
	}

	public void setSQLQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public boolean isUseSqlQuery() {
		return useSqlQuery;
	}

	public void setUseSqlQuery(boolean useSqlQuery) {
		this.useSqlQuery = useSqlQuery;
	}
}