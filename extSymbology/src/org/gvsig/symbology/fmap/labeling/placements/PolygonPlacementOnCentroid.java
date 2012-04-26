/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package org.gvsig.symbology.fmap.labeling.placements;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IPlacementConstraints;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelLocationMetrics;
import com.iver.utiles.swing.threads.Cancellable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
/**
 *
 * PolygonPlacementOnCentroid.java
 *
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es Apr 1, 2008
 *
 */
//public class PolygonPlacementOnCentroid extends MarkerCenteredAtPoint implements ILabelPlacement {
public class PolygonPlacementOnCentroid extends MarkerPlacementOnPoint implements ILabelPlacement {

	public ArrayList<LabelLocationMetrics> guess(LabelClass lc, IGeometry geom,
			IPlacementConstraints placementConstraints,
			double cartographicSymbolSize, Cancellable cancel, ViewPort vp) {

		if (cancel.isCanceled()) return CannotPlaceLabel.NO_PLACES;

		FShape shp = (FShape)geom.getInternalShape();

		Geometry geo = FConverter.java2d_to_jts(shp);

		if (geo == null) {
			return CannotPlaceLabel.NO_PLACES;
		}

		Point pJTS = geo.getCentroid();
		FPoint2D fp2d=new FPoint2D(pJTS.getX(), pJTS.getY());
//		fp2d=(FPoint2D)FConverter.transformToInts(ShapeFactory.createPoint2D(fp2d), vp.getAffineTransform());

		return super.guess(lc,ShapeFactory.createPoint2D(fp2d) , placementConstraints, cartographicSymbolSize, cancel,vp);
	}

	public ArrayList<LabelLocationMetrics> guess(LabelClass lc, FShape shp,
			IPlacementConstraints placementConstraints, double cartographicSymbolSize, Cancellable cancel) {

		if (cancel.isCanceled()) return CannotPlaceLabel.NO_PLACES;

		ArrayList<LabelLocationMetrics> guessed = new ArrayList<LabelLocationMetrics>();
		Geometry geo = FConverter.java2d_to_jts(shp);
		if (geo == null) {
			return CannotPlaceLabel.NO_PLACES;
		}
		Point pJTS = geo.getCentroid();
		Point2D p = new Point2D.Double(pJTS.getX(), pJTS.getY());
		Rectangle2D bounds = lc.getBounds();
		p.setLocation(p.getX() - (bounds.getWidth()*0.5), p.getY() - (bounds.getHeight()*0.5));// - 2);
		guessed.add(new LabelLocationMetrics(
				p, 0, true));
		return guessed;
	}

	public boolean isSuitableFor(IPlacementConstraints placementConstraints,
			int shapeType) {
		if ((shapeType % FShape.Z) == FShape.POLYGON) {
			return (placementConstraints.isHorizontal()&&!placementConstraints.isFitInsidePolygon());
		}
		return false;
	}

}
