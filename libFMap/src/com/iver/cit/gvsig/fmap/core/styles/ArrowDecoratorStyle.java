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
package com.iver.cit.gvsig.fmap.core.styles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.apache.batik.ext.awt.geom.PathLength;

import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ArrowMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;

/**
 * Class ArrowDecoratorStyle. It is used to store the information about the
 * different options to draw an arrow in a line (and draw it too). This
 * information is taken from the panel.
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class ArrowDecoratorStyle implements IStyle {
	private boolean flipAll = false;
	private boolean flipFirst = false;
    private boolean scaleArrow = true;
	private int arrowMarkerCount = 2;
	private boolean followLineAngle = true;
	private IMarkerSymbol marker = new ArrowMarkerSymbol();
	private String desc;

	{
		marker.setSize(10);
		((ArrowMarkerSymbol) marker).setSharpness(30);
	}

    public boolean isScaleArrow() {
        return scaleArrow;
    }

    public void setScaleArrow(boolean scaleArrow) {
        this.scaleArrow = scaleArrow;
    }

	/**
	 * Obtains the number of arrows that the user wants to draw in the same line.
	 * @return
	 */
	public int getArrowMarkerCount() {
		return arrowMarkerCount;
	}

	/**
	 * Defines the number of arrows that the user wants to draw in the same line.
	 * @return
	 */
	public void setArrowMarkerCount(int arrowMarkerCount) {
		this.arrowMarkerCount = arrowMarkerCount;
	}

	/**
	 * Defines the flipAll attribute.If the value of this attribute is true all the
	 * arrows that we had drawn in the same line will be flipped.
	 * @return
	 */
	public boolean isFlipAll() {
		return flipAll;
	}

	/**
	 * Obtains the flipAll attribute.If the value of this attribute is true all the
	 * arrows that we had drawn in the same line will be flipped.
	 * @return
	 */
	public void setFlipAll(boolean flipAll) {
		this.flipAll = flipAll;
	}

	/**
	 * Obtains the flipFirst attribute.If it is true only the first arrow of the line
	 * will be flipped.The rest will keep the same orientation.
	 * @return
	 */
	public boolean isFlipFirst() {
		return flipFirst;
	}

	/**
	 * Sets the flipFirst attribute.If it is true only the first arrow of the line
	 * will be flipped.The rest will keep the same orientation.
	 * @return
	 */
	public void setFlipFirst(boolean flipFirst) {
		this.flipFirst = flipFirst;
	}

	/**
	 * Gets the followLineAngle attribute.This attribute allows the arrow that we are
	 * going to draw to be more or less aligned with the line where it will be included (depending on the angle) .
	 * @return
	 */
	public boolean isFollowLineAngle() {
		return followLineAngle;
	}

	/**
	 * Sets the followLineAngle attribute.This attribute allows the arrow that we are
	 * going to draw to be more or less aligned with the line where it will be included.
	 * (depending on the angle).
	 * @param followingLineAngle
	 * @return
	 */
	public void setFollowLineAngle(boolean followLineAngle) {
		this.followLineAngle = followLineAngle;
	}
	/**
	 * Draws an arrow(or other symbol that substitutes an arrow selected by the user)
	 * in a line.When the line is drawn, the symbol is added and takes care of the different
	 * options of the user(for example if he wants to flip the first symbol or all and
	 * the number of symbols per line to be drawn)
	 * @param g
	 * @param affineTransform
	 * @param shp
	 */
	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp) {
		if (arrowMarkerCount <= 0) return;

		PathLength pl = new PathLength(shp);
        double baseSize = (float) marker.getSize();
        float lineLength = pl.lengthOfPath();
        float size = (float) (this.scaleArrow ? baseSize * lineLength / 100
                : (baseSize > lineLength * 0.25 ? lineLength * 0.25 : baseSize));
		marker.setSize(size);
		float myLineLength = pl.lengthOfPath()-size; // length without the first and last arrow
		float step = arrowMarkerCount>2 ? myLineLength/(arrowMarkerCount-1) : pl.lengthOfPath();
		float rotation1; // rotation at the arrow's vertex
		float rotation2; // rotation at the arrow's back;

		FPoint2D startP;

		// the first arrow at the end of the line
		{
			float theLength = pl.lengthOfPath();

			if ((flipFirst || flipAll) && (flipFirst != flipAll)) { // logical XOR
				startP = new FPoint2D(pl.pointAtLength(theLength-size));
				rotation1 = pl.angleAtLength(theLength-size);
				rotation2 = pl.angleAtLength(theLength);
			} else {
				startP = new FPoint2D(pl.pointAtLength(theLength));
				rotation1 = pl.angleAtLength(theLength-size)+(float) Math.PI;
				rotation2 = pl.angleAtLength(theLength)+(float) Math.PI;

			}

			if (rotation1 == rotation2)	{
				marker.setRotation(rotation1);
				marker.draw(g, affineTransform, startP, null);
			}
		}
		// the other arrows but the first and the last
		float aLength;
		for (int i = 1; i < arrowMarkerCount-1; i++) {
			aLength = step*i;

			rotation1 = pl.angleAtLength(aLength);
			rotation2 = pl.angleAtLength(aLength+size);

			if (flipAll) {
				startP = new FPoint2D(pl.pointAtLength(aLength));

			} else {
				startP = new FPoint2D(pl.pointAtLength(aLength+size));
				rotation1 += Math.PI;
				rotation2 += Math.PI;
			}
			/*
			 *  the following are just visualization improvements,
			 *  being rigurous it just be only these two lines
			 *
			 *  marker.setRotation(rotation1);
			 *  marker.draw(g, affineTransform, startP);
			 *
			 *  but it produces ugly results at the line edges
			 */
			if (rotation1 == rotation2)	{
				marker.setRotation(rotation1);
				marker.draw(g, affineTransform, startP, null);
			} else {
				rotation1 = pl.angleAtLength(aLength+1);
				rotation2 = pl.angleAtLength(aLength+size+1);
				if (flipAll) {
					startP = new FPoint2D(pl.pointAtLength(aLength+1));

				} else {
					startP = new FPoint2D(pl.pointAtLength(aLength+size+1));
					rotation1 += Math.PI;
					rotation2 += Math.PI;
				}

				if (rotation1 == rotation2)	{
					marker.setRotation(rotation1);
					marker.draw(g, affineTransform, startP, null);
				}
			}

		}

		// and the last arrow at the begining of the line
		if (arrowMarkerCount>1) {
			rotation1 = pl.angleAtLength(size);
			rotation2 = pl.angleAtLength(0);

			if (flipAll) {
				startP = new FPoint2D(pl.pointAtLength(0));

			} else {
				startP = new FPoint2D(pl.pointAtLength(size));
				rotation1 += Math.PI;
				rotation2 += Math.PI;
			}

			if (rotation1 == rotation2)	{
				marker.setRotation(rotation1);
				marker.draw(g, affineTransform, startP, null);
			}
		}

        marker.setSize(baseSize);

	}

	public void drawInsideRectangle(Graphics2D g, Rectangle r) {
		// TODO Auto-generated method stub
		throw new Error("Not yet implemented!");
	}

	public void drawOutline(Graphics2D g, Rectangle r) {
		// TODO Auto-generated method stub
		throw new Error("Not yet implemented!");
	}

	public String getDescription() {
		return desc;
	}

	public boolean isSuitableFor(ISymbol symbol) {
		return symbol instanceof ILineSymbol;
	}

	public void setDescription(String desc) {
		this.desc = desc;
	}

	public String getClassName() {
		return getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
        xml.putProperty("scaleArrow", scaleArrow);
		xml.putProperty("flipAll", flipAll);
		xml.putProperty("flipFirst", flipFirst);
		xml.putProperty("arrowMarkerCount", arrowMarkerCount);
		xml.putProperty("desc", desc);
		xml.addChild(marker.getXMLEntity());
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
        try {
            setScaleArrow(xml.getBooleanProperty("scaleArrow"));
        } catch (NotExistInXMLEntity e) {
            setScaleArrow(true);
        }
		setFlipAll(xml.getBooleanProperty("flipAll"));
		setFlipFirst(xml.getBooleanProperty("flipFirst"));
		setArrowMarkerCount(xml.getIntProperty("arrowMarkerCount"));
		setDescription(xml.getStringProperty("desc"));
		marker = (IMarkerSymbol) SymbologyFactory.
		createSymbolFromXML(
				xml.getChild(0), "ArrowDecorator marker symbol");
	}

	public IMarkerSymbol getMarker() {
		return marker;
	}

	public void setMarker(IMarkerSymbol marker) {
		this.marker = marker;
	}

}
