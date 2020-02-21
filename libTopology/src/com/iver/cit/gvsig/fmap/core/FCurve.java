/*
 * Created on 10-abr-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
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
* $Id: 
* $Log: 
*/
package com.iver.cit.gvsig.fmap.core;

import java.awt.geom.Point2D;

import com.graphbuilder.curve.BSpline;
import com.graphbuilder.curve.BezierCurve;
import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.CatmullRomSpline;
import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.CubicBSpline;
import com.graphbuilder.curve.Curve;
import com.graphbuilder.curve.GroupIterator;
import com.graphbuilder.curve.LagrangeCurve;
import com.graphbuilder.curve.NURBSpline;
import com.graphbuilder.curve.NaturalCubicSpline;
import com.graphbuilder.curve.Point;


/**
 * FMap geometry based in curve API classes (com.graphbuilder.curve).
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public class FCurve extends FPolyline2D {
	public static final int BEZIER = 0;
	public static final int B_SPLINE = 1;
	public static final int CARDINAL_SPLINE = 2;
	public static final int CATMULLROM_SPLINE = 3;
	public static final int CUBIC_BSPLINE = 4;
	public static final int LAGRANGE_CURVE = 5;
	public static final int NATURAL_CUBIC_SPLINE = 6;
	public static final int NURB_SPLINE = 7;
	
	private int shapeType;
	
	private static final long serialVersionUID = -8518882527338477090L;
	
	private Point2D[] controlPoints;
	
	public FCurve(Point2D[] points, int curveType, int shapeType) {
		super(getGeneralPathX(points, curveType));
		controlPoints = points;
		this.shapeType = shapeType;
	}
	
	
	public static GeneralPathX getGeneralPathX(Point2D[] controlPoints, int curveType){
		ControlPath cp = new ControlPath();
		for(int i = 0; i < controlPoints.length; i++){
			Point2D point = controlPoints[i];
			Point capiPoint = createCapiPoint(point.getX(), point.getY());
			cp.addPoint(capiPoint);
		}
		GroupIterator gi = new GroupIterator("0:n-1", cp.numPoints());
		MultiPathGeneralPathX gpx = new MultiPathGeneralPathX();
		
		/*
		 TODO
		 Parametrizar o dejar que el usuario elija la forma de la curva,
		 con los parámetros grado (b-spline), alpha (cardinal-spline), etc.
		 * */
		Curve curve = null;
		switch(curveType){	
		case B_SPLINE:
			curve = new BSpline(cp, gi);
			((BSpline)curve).setDegree(cp.numPoints() - 2);
		break;
		
		case CARDINAL_SPLINE:
			curve = new CardinalSpline(cp, gi);
			((CardinalSpline)curve).setAlpha(1);
			break;
		case CATMULLROM_SPLINE:
			curve = new CatmullRomSpline(cp, gi);
			
			break;
		case CUBIC_BSPLINE:
			curve = new CubicBSpline(cp, gi);
			((CubicBSpline)curve).setInterpolateEndpoints(true);
			break;
		case LAGRANGE_CURVE:
			curve = new LagrangeCurve(cp, gi);
			((LagrangeCurve)curve).setInterpolateFirst(true);
			((LagrangeCurve)curve).setInterpolateLast(true);
			break;
		case NATURAL_CUBIC_SPLINE:
			curve = new NaturalCubicSpline(cp, gi);
			
			break;
		case NURB_SPLINE:
			curve = new NURBSpline(cp, gi);
			((NURBSpline)curve).setUseWeightVector(false);
			((NURBSpline)curve).setUseDefaultInterval(true);
			break;
		case BEZIER:
		default:
			curve = new BezierCurve(cp, gi);
			break;
		}
		
		curve.appendTo(gpx.getMultiPath());
		
		return gpx;
		
	}
	
	protected static Point createCapiPoint(double x, double y){
		final double[] arr = new double[] {x, y};

		return new Point() {
			public double[] getLocation() {
				return arr;
			}

			public void setLocation(double[] loc) {
				arr[0] = loc[0];
				arr[1] = loc[1];
			}
		};

	}

	@Override
	public int getShapeType() {		
		return shapeType;
	}


	public Point2D[] getControlPoints() {
		return controlPoints;
	}
}
