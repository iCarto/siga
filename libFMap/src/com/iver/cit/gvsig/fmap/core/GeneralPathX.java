/*
 * Created on 10-jun-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.core;

/**
 * @author FJP
 *
 */
/*
 * @(#)GeneralPathX.java	1.58 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import org.cresques.cts.ICoordTrans;
import org.gvsig.geoutils.sun.awt.geom.Crossings;
import org.gvsig.geoutils.sun.awt.geom.Curve;

import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * The <code>GeneralPathX</code> class represents a geometric path
 * constructed from straight lines, and quadratic and cubic
 * (B&eacute;zier) curves.  It can contain multiple subpaths.
 * <p>
 * The winding rule specifies how the interior of a path is
 * determined.  There are two types of winding rules:
 * EVEN_ODD and NON_ZERO.
 * <p>
 * An EVEN_ODD winding rule means that enclosed regions
 * of the path alternate between interior and exterior areas as
 * traversed from the outside of the path towards a point inside
 * the region.
 * <p>
 * A NON_ZERO winding rule means that if a ray is
 * drawn in any direction from a given point to infinity
 * and the places where the path intersects
 * the ray are examined, the point is inside of the path if and only if
 * the number of times that the path crosses the ray from
 * left to right does not equal the  number of times that the path crosses
 * the ray from right to left.
 * @version 1.58, 01/23/03
 * @author Jim Graham
 */
public class GeneralPathX implements Shape, Cloneable, Serializable {
    /**
     * An even-odd winding rule for determining the interior of
     * a path.
     */
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

    /**
     * A non-zero winding rule for determining the interior of a
     * path.
     */
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO  = (byte) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO  = (byte) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO  = (byte) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE   = (byte) PathIterator.SEG_CLOSE;

    byte[] pointTypes;
    double[] pointCoords;
    int numTypes;
    int numCoords;
    int windingRule;

    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;

    private static final int curvesize[] = {2, 2, 4, 6, 0};

    /**
     * Constructs a new <code>GeneralPathX</code> object.
     * If an operation performed on this path requires the
     * interior of the path to be defined then the default NON_ZERO
     * winding rule is used.
     * @see #WIND_NON_ZERO
     */
    public GeneralPathX() {
	// this(WIND_NON_ZERO, INIT_SIZE, INIT_SIZE);
    	this(WIND_EVEN_ODD, INIT_SIZE, INIT_SIZE);
    }

    /**
     * Constructs a new <code>GeneralPathX</code> object with the specified
     * winding rule to control operations that require the interior of the
     * path to be defined.
     * @param rule the winding rule
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public GeneralPathX(int rule) {
	this(rule, INIT_SIZE, INIT_SIZE);
    }

    /**
     * Constructs a new <code>GeneralPathX</code> object with the specified
     * winding rule and the specified initial capacity to store path
     * coordinates. This number is an initial guess as to how many path
     * segments are in the path, but the storage is expanded
     * as needed to store whatever path segments are added to this path.
     * @param rule the winding rule
     * @param initialCapacity the estimate for the number of path segments
     * in the path
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public GeneralPathX(int rule, int initialCapacity) {
	this(rule, initialCapacity, initialCapacity);
    }

    /**
     * Constructs a new <code>GeneralPathX</code> object with the specified
     * winding rule and the specified initial capacities to store point types
     * and coordinates.
     * These numbers are an initial guess as to how many path segments
     * and how many points are to be in the path, but the
     * storage is expanded as needed to store whatever path segments are
     * added to this path.
     * @param rule the winding rule
     * @param initialTypes the estimate for the number of path segments
     * in the path
     * @param initialCapacity the estimate for the number of points
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    GeneralPathX(int rule, int initialTypes, int initialCoords) {
	setWindingRule(rule);
	pointTypes = new byte[initialTypes];
	pointCoords = new double[initialCoords * 2];
    }

    /**
     * Constructs a new <code>GeneralPathX</code> object from an arbitrary
     * {@link Shape} object.
     * All of the initial geometry and the winding rule for this path are
     * taken from the specified <code>Shape</code> object.
     * @param s the specified <code>Shape</code> object
     */
    public GeneralPathX(Shape s) {
	// this(WIND_NON_ZERO, INIT_SIZE, INIT_SIZE);
    	this(WIND_EVEN_ODD, INIT_SIZE, INIT_SIZE);
	PathIterator pi = s.getPathIterator(null);
	setWindingRule(pi.getWindingRule());
	append(pi, false);
    }

    private void needRoom(int newTypes, int newCoords, boolean needMove) {
	if (needMove && numTypes == 0) {
	    throw new IllegalPathStateException("missing initial moveto "+
						"in path definition");
	}
	int size = pointCoords.length;
	if (numCoords + newCoords > size) {
	    int grow = size;
	    if (grow > EXPAND_MAX * 2) {
		grow = EXPAND_MAX * 2;
	    }
	    if (grow < newCoords) {
		grow = newCoords;
	    }
	    double[] arr = new double[size + grow];
	    System.arraycopy(pointCoords, 0, arr, 0, numCoords);
	    pointCoords = arr;
	}
	size = pointTypes.length;
	if (numTypes + newTypes > size) {
	    int grow = size;
	    if (grow > EXPAND_MAX) {
		grow = EXPAND_MAX;
	    }
	    if (grow < newTypes) {
		grow = newTypes;
	    }
	    byte[] arr = new byte[size + grow];
	    System.arraycopy(pointTypes, 0, arr, 0, numTypes);
	    pointTypes = arr;
	}
    }

    /**
     * Adds a point to the path by moving to the specified
     * coordinates.
     * @param x,&nbsp;y the specified coordinates
     */
    public synchronized void moveTo(double x, double y) {
	if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
	    pointCoords[numCoords - 2] = x;
	    pointCoords[numCoords - 1] = y;
	} else {
	    needRoom(1, 2, false);
	    pointTypes[numTypes++] = SEG_MOVETO;
	    pointCoords[numCoords++] = x;
	    pointCoords[numCoords++] = y;
	}
    }

    /**
     * Adds a point to the path by drawing a straight line from the
     * current coordinates to the new specified coordinates.
     * @param x,&nbsp;y the specified coordinates
     */
    public synchronized void lineTo(double x, double y) {
	needRoom(1, 2, true);
	pointTypes[numTypes++] = SEG_LINETO;
	pointCoords[numCoords++] = x;
	pointCoords[numCoords++] = y;
    }

    /**
     * Adds a curved segment, defined by two new points, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the coordinates (x2,&nbsp;y2), using the
     * specified point (x1,&nbsp;y1) as a quadratic parametric control
     * point.
     * @param x1,&nbsp;y1 the coordinates of the first quadratic control
     *		point
     * @param x2,&nbsp;y2 the coordinates of the final endpoint
     */
    public synchronized void quadTo(double x1, double y1, double x2, double y2) {
	needRoom(1, 4, true);
	pointTypes[numTypes++] = SEG_QUADTO;
	pointCoords[numCoords++] = x1;
	pointCoords[numCoords++] = y1;
	pointCoords[numCoords++] = x2;
	pointCoords[numCoords++] = y2;
    }

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a B&eacute;zier curve that intersects both the current
     * coordinates and the coordinates (x3,&nbsp;y3), using the
     * specified points (x1,&nbsp;y1) and (x2,&nbsp;y2) as
     * B&eacute;zier control points.
     * @param x1,&nbsp;y1 the coordinates of the first B&eacute;ezier
     *		control point
     * @param x2,&nbsp;y2 the coordinates of the second B&eacute;zier
     *		control point
     * @param x3,&nbsp;y3 the coordinates of the final endpoint
     */
    public synchronized void curveTo(double x1, double y1,
    		double x2, double y2,
    		double x3, double y3) {
	needRoom(1, 6, true);
	pointTypes[numTypes++] = SEG_CUBICTO;
	pointCoords[numCoords++] = x1;
	pointCoords[numCoords++] = y1;
	pointCoords[numCoords++] = x2;
	pointCoords[numCoords++] = y2;
	pointCoords[numCoords++] = x3;
	pointCoords[numCoords++] = y3;
    }

    /**
     * Closes the current subpath by drawing a straight line back to
     * the coordinates of the last <code>moveTo</code>.  If the path is already
     * closed then this method has no effect.
     */
    public synchronized void closePath() {
	if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
	    needRoom(1, 0, true);
	    pointTypes[numTypes++] = SEG_CLOSE;
	}
    }

    /**
     * Check if the first part is closed.
     * @return
     */
    public boolean isClosed()
    {
		PathIterator theIterator = getPathIterator(null, FConverter.FLATNESS); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
        double xFinal = 0;
        double yFinal = 0;
        double xIni = 0;
        double yIni = 0;
        boolean first = true;

		while (!theIterator.isDone()) {
			//while not done
			int theType = theIterator.currentSegment(theData);

			switch (theType) {
				case PathIterator.SEG_MOVETO:
					xIni = theData[0];
					yIni = theData[1];
					if (!first)
					{
						break;
					}
					first = false;
					break;

				case PathIterator.SEG_LINETO:
					xFinal = theData[0];
					yFinal = theData[1];
					break;
				case PathIterator.SEG_CLOSE:
					return true;

			} //end switch

			theIterator.next();
		}
	      if ((xFinal == xIni) && (yFinal == yIni))
	    	return true;
	    return false;



//        double xFinal = pointCoords[numCoords -2];
//        double yFinal = pointCoords[numCoords -1];
//        double xIni = pointCoords[0];
//        double yIni = pointCoords[1];
//
//        if (pointTypes[numTypes-1] == SEG_CLOSE)
//        	return true;
//        if ((xFinal == xIni) && (yFinal == yIni))
//        	return true;
//        return false;

    }


    /**
     * Appends the geometry of the specified <code>Shape</code> object to the
     * path, possibly connecting the new geometry to the existing path
     * segments with a line segment.
     * If the <code>connect</code> parameter is <code>true</code> and the
     * path is not empty then any initial <code>moveTo</code> in the
     * geometry of the appended <code>Shape</code>
     * is turned into a <code>lineTo</code> segment.
     * If the destination coordinates of such a connecting <code>lineTo</code>
     * segment match the ending coordinates of a currently open
     * subpath then the segment is omitted as superfluous.
     * The winding rule of the specified <code>Shape</code> is ignored
     * and the appended geometry is governed by the winding
     * rule specified for this path.
     * @param s the <code>Shape</code> whose geometry is appended
     * to this path
     * @param connect a boolean to control whether or not to turn an
     * initial <code>moveTo</code> segment into a <code>lineTo</code>
     * segment to connect the new geometry to the existing path
     */
    public void append(Shape s, boolean connect) {
	PathIterator pi = s.getPathIterator(null);
        append(pi,connect);
    }

    /**
     * Appends the geometry of the specified
     * {@link PathIterator} object
     * to the path, possibly connecting the new geometry to the existing
     * path segments with a line segment.
     * If the <code>connect</code> parameter is <code>true</code> and the
     * path is not empty then any initial <code>moveTo</code> in the
     * geometry of the appended <code>Shape</code> is turned into a
     * <code>lineTo</code> segment.
     * If the destination coordinates of such a connecting <code>lineTo</code>
     * segment match the ending coordinates of a currently open
     * subpath then the segment is omitted as superfluous.
     * The winding rule of the specified <code>Shape</code> is ignored
     * and the appended geometry is governed by the winding
     * rule specified for this path.
     * @param pi the <code>PathIterator</code> whose geometry is appended to
     * this path
     * @param connect a boolean to control whether or not to turn an
     * initial <code>moveTo</code> segment into a <code>lineTo</code> segment
     * to connect the new geometry to the existing path
     */
    public void append(PathIterator pi, boolean connect) {
	double coords[] = new double[6];
	while (!pi.isDone()) {
	    switch (pi.currentSegment(coords)) {
	    case SEG_MOVETO:
		if (!connect || numTypes < 1 || numCoords < 2) {
		    moveTo(coords[0], coords[1]);
		    break;
		}
		if (pointTypes[numTypes - 1] != SEG_CLOSE &&
		    pointCoords[numCoords - 2] == coords[0] &&
		    pointCoords[numCoords - 1] == coords[1])
		{
		    // Collapse out initial moveto/lineto
		    break;
		}
		// NO BREAK;
	    case SEG_LINETO:
		lineTo(coords[0], coords[1]);
		break;
	    case SEG_QUADTO:
		quadTo(coords[0], coords[1],
		       coords[2], coords[3]);
		break;
	    case SEG_CUBICTO:
		curveTo(coords[0], coords[1],
			coords[2], coords[3],
			coords[4], coords[5]);
		break;
	    case SEG_CLOSE:
		closePath();
		break;
	    }
	    pi.next();
	    connect = false;
	}
    }

    /**
     * Returns the fill style winding rule.
     * @return an integer representing the current winding rule.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @see #setWindingRule
     */
    public synchronized int getWindingRule() {
        return windingRule;
    }

    /**
     * Sets the winding rule for this path to the specified value.
     * @param rule an integer representing the specified
     * winding rule
     * @exception <code>IllegalArgumentException</code> if
     *		<code>rule</code> is not either
     *		<code>WIND_EVEN_ODD</code> or
     *		<code>WIND_NON_ZERO</code>
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @see #getWindingRule
     */
    public void setWindingRule(int rule) {
	if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
	    throw new IllegalArgumentException("winding rule must be "+
					       "WIND_EVEN_ODD or "+
					       "WIND_NON_ZERO");
	}
	windingRule = rule;
    }

    /**
     * Returns the coordinates most recently added to the end of the path
     * as a {@link Point2D} object.
     * @return a <code>Point2D</code> object containing the ending
     * coordinates of the path or <code>null</code> if there are no points
     * in the path.
     */
    public synchronized Point2D getCurrentPoint() {
	if (numTypes < 1 || numCoords < 2) {
	    return null;
	}
	int index = numCoords;
	if (pointTypes[numTypes - 1] == SEG_CLOSE) {
	loop:
	    for (int i = numTypes - 2; i > 0; i--) {
		switch (pointTypes[i]) {
		case SEG_MOVETO:
		    break loop;
		case SEG_LINETO:
		    index -= 2;
		    break;
		case SEG_QUADTO:
		    index -= 4;
		    break;
		case SEG_CUBICTO:
		    index -= 6;
		    break;
		case SEG_CLOSE:
		    break;
		}
	    }
	}
	return new Point2D.Double(pointCoords[index - 2],
				 pointCoords[index - 1]);
    }

    /**
     * Resets the path to empty.  The append position is set back to the
     * beginning of the path and all coordinates and point types are
     * forgotten.
     */
    public synchronized void reset() {
	numTypes = numCoords = 0;
    }

    /**
     * Transforms the geometry of this path using the specified
     * {@link AffineTransform}.
     * The geometry is transformed in place, which permanently changes the
     * boundary defined by this object.
     * @param at the <code>AffineTransform</code> used to transform the area
     */
    public void transform(AffineTransform at) {
	at.transform(pointCoords, 0, pointCoords, 0, numCoords / 2);
    }

    public void reProject(ICoordTrans ct)
    {
    	Point2D pt = new Point2D.Double();
    	for (int i = 0; i < numCoords; i+=2)
    	{
    		pt.setLocation(pointCoords[i], pointCoords[i+1]);
    		pt = ct.convert(pt,null);
    		pointCoords[i] = pt.getX();
    		pointCoords[i+1] = pt.getY();
    	}

    }


    /**
     * Returns a new transformed <code>Shape</code>.
     * @param at the <code>AffineTransform</code> used to transform a
     * new <code>Shape</code>.
     * @return a new <code>Shape</code>, transformed with the specified
     * <code>AffineTransform</code>.
     */
    public synchronized Shape createTransformedShape(AffineTransform at) {
	GeneralPathX gp = (GeneralPathX) clone();
	if (at != null) {
	    gp.transform(at);
	}
	return gp;
    }

    /**
     * Return the bounding box of the path.
     * @return a {@link java.awt.Rectangle} object that
     * bounds the current path.
     */
    public java.awt.Rectangle getBounds() {
	return getBounds2D().getBounds();
    }

    /**
     * Returns the bounding box of the path.
     * @return a {@link Rectangle2D} object that
     *          bounds the current path.
     */
    public synchronized Rectangle2D getBounds2D() {
	double x1, y1, x2, y2;
	int i = numCoords;
	if (i > 0) {
	    y1 = y2 = pointCoords[--i];
	    x1 = x2 = pointCoords[--i];
	    while (i > 0) {
		double y = pointCoords[--i];
		double x = pointCoords[--i];
		if (x < x1) x1 = x;
		if (y < y1) y1 = y;
		if (x > x2) x2 = x;
		if (y > y2) y2 = y;
	    }
	} else {
	    x1 = y1 = x2 = y2 = 0.0f;
	}
	return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Tests if the specified coordinates are inside the boundary of
     * this <code>Shape</code>.
     * @param x,&nbsp;y the specified coordinates
     * @return <code>true</code> if the specified coordinates are inside this
     * <code>Shape</code>; <code>false</code> otherwise
     */
    public boolean contains(double x, double y) {
	if (numTypes < 2) {
	    return false;
	}
	int cross = Curve.pointCrossingsForPath(getPathIterator(null), x, y);
	if (windingRule == WIND_NON_ZERO) {
	    return (cross != 0);
	} else {
	    return ((cross & 1) != 0);
	}
    }

    /**
     * Tests if the specified <code>Point2D</code> is inside the boundary
     * of this <code>Shape</code>.
     * @param p the specified <code>Point2D</code>
     * @return <code>true</code> if this <code>Shape</code> contains the
     * specified <code>Point2D</code>, <code>false</code> otherwise.
     */
    public boolean contains(Point2D p) {
	return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the specified rectangular area is inside the boundary of
     * this <code>Shape</code>.
     * @param x,&nbsp;y the specified coordinates
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if this <code>Shape</code> contains
     * the specified rectangluar area; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double w, double h) {
	Crossings c = Crossings.findCrossings(getPathIterator(null),
					      x, y, x+w, y+h);
	return (c != null && c.covers(y, y+h));
    }

    /**
     * Tests if the specified <code>Rectangle2D</code>
     * is inside the boundary of this <code>Shape</code>.
     * @param r a specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Shape</code> bounds the
     * specified <code>Rectangle2D</code>; <code>false</code> otherwise.
     */
    public boolean contains(Rectangle2D r) {
	return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the interior of this <code>Shape</code> intersects the
     * interior of a specified set of rectangular coordinates.
     * @param x,&nbsp;y the specified coordinates
     * @param w the width of the specified rectangular coordinates
     * @param h the height of the specified rectangular coordinates
     * @return <code>true</code> if this <code>Shape</code> and the
     * interior of the specified set of rectangular coordinates intersect
     * each other; <code>false</code> otherwise.
     */
    public boolean intersects(double x, double y, double w, double h) {
	Crossings c = Crossings.findCrossings(getPathIterator(null),
					      x, y, x+w, y+h);
	return (c == null || !c.isEmpty());
    }

    /**
     * Tests if the interior of this <code>Shape</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     * @param r the specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Shape</code> and the interior
     * 		of the specified <code>Rectangle2D</code> intersect each
     * 		other; <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle2D r) {
	return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns a <code>PathIterator</code> object that iterates along the
     * boundary of this <code>Shape</code> and provides access to the
     * geometry of the outline of this <code>Shape</code>.
     * The iterator for this class is not multi-threaded safe,
     * which means that this <code>GeneralPathX</code> class does not
     * guarantee that modifications to the geometry of this
     * <code>GeneralPathX</code> object do not affect any iterations of
     * that geometry that are already in process.
     * @param at an <code>AffineTransform</code>
     * @return a new <code>PathIterator</code> that iterates along the
     * boundary of this <code>Shape</code> and provides access to the
     * geometry of this <code>Shape</code>'s outline
     */
    public PathIterator getPathIterator(AffineTransform at) {
	return new GeneralPathXIterator(this, at);
    }

    /**
     * Returns a <code>PathIterator</code> object that iterates along the
     * boundary of the flattened <code>Shape</code> and provides access to the
     * geometry of the outline of the <code>Shape</code>.
     * The iterator for this class is not multi-threaded safe,
     * which means that this <code>GeneralPathX</code> class does not
     * guarantee that modifications to the geometry of this
     * <code>GeneralPathX</code> object do not affect any iterations of
     * that geometry that are already in process.
     * @param at an <code>AffineTransform</code>
     * @param flatness the maximum distance that the line segments used to
     *		approximate the curved segments are allowed to deviate
     *		from any point on the original curve
     * @return a new <code>PathIterator</code> that iterates along the flattened
     * <code>Shape</code> boundary.
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
	return new FlatteningPathIterator(getPathIterator(at), flatness);
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
	try {
	    GeneralPathX copy = (GeneralPathX) super.clone();
	    copy.pointTypes = pointTypes.clone();
	    copy.pointCoords = pointCoords.clone();
	    return copy;
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    GeneralPathX(int windingRule,
		byte[] pointTypes,
		int numTypes,
		double[] pointCoords,
		int numCoords) {

    // used to construct from native

	this.windingRule = windingRule;
	this.pointTypes = pointTypes;
	this.numTypes = numTypes;
	this.pointCoords = pointCoords;
	this.numCoords = numCoords;
    }

    /**
     * Convertimos el path a puntos y luego le damos la vuelta.
     */
    public void flip()
	{
		PathIterator theIterator = getPathIterator(null, FConverter.FLATNESS); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
        Coordinate first = null;
        CoordinateList coordList = new CoordinateList();
        Coordinate c1;
        GeneralPathX newGp = new GeneralPathX();
        ArrayList listOfParts = new ArrayList();
		while (!theIterator.isDone()) {
			//while not done
			int type = theIterator.currentSegment(theData);
        	switch (type)
        	{
        	case SEG_MOVETO:
        		coordList = new CoordinateList();
        		listOfParts.add(coordList);
        		c1= new Coordinate(theData[0], theData[1]);
        		coordList.add(c1, true);
        		break;
        	case SEG_LINETO:
        		c1= new Coordinate(theData[0], theData[1]);
        		coordList.add(c1, true);
        		break;

        	case SEG_CLOSE:
        		coordList.add(coordList.getCoordinate(0));
        		break;

        	}
        	theIterator.next();
		}

		for (int i=listOfParts.size()-1; i>=0; i--)
		{
			coordList = (CoordinateList) listOfParts.get(i);
			Coordinate[] coords = coordList.toCoordinateArray();
			CoordinateArraySequence seq = new CoordinateArraySequence(coords);
			CoordinateSequences.reverse(seq);
			coords = seq.toCoordinateArray();
			newGp.moveTo(coords[0].x, coords[0].y);
			for (int j=1; j < coords.length; j++)
			{
				newGp.lineTo(coords[j].x, coords[j].y);
			}
		}
		reset();
		append(newGp, false);
	}

	/**
	 * Use this function to ensure you get real polygons or holes
	 * En JTS, con bCCW = false obtienes un polígono exterior.
	 * Nota: Solo se le da la vuelta (si es que lo necesita) al
	 * polígono exterior. El resto, por ahora, no se tocan.
	 * Si se necesita tenerlos en cuenta, habría que mirar
	 * si están dentro del otro, y entonces revisar que tiene
	 * un CCW contrario al exterior.
	 * @param bCCW true if you want the GeneralPath in CCW order
	 * @return true si se le ha dado la vuelta. (true if flipped)
	 * TODO: TERMINAR ESTO!! NO ESTÁ COMPLETO!! NO sirve para multipoligonos
	 */
	public boolean ensureOrientation(boolean bCCW) {
        byte[] pointTypesAux = new byte[numTypes+1];
        double[] pointCoordsAux = new double[numCoords+2];
        int i;
        int pointIdx = 0;

        Coordinate c1, c2, c3;
        CoordinateList coordList = new CoordinateList();
        CoordinateList firstList = new CoordinateList();
        boolean bFirstList = true;
        Coordinate cInicio = null;

        for (i=0; i< numTypes; i++)
        {
        	int type = pointTypes[i];

        	switch (type)
        	{
        	case SEG_MOVETO:
        		c1= new Coordinate(pointCoords[pointIdx], pointCoords[pointIdx+1]);
        		cInicio = c1;
        		coordList.add(c1, true);
        		if (i>0) bFirstList = false;
        		if (bFirstList)
        		{
        			firstList.add(c1,true);
        		}
        		break;
        	case SEG_LINETO:
        		c1= new Coordinate(pointCoords[pointIdx], pointCoords[pointIdx+1]);
        		coordList.add(c1, true);
        		if (bFirstList)
        		{
        			firstList.add(c1,true);
        		}
        		break;
        	case SEG_QUADTO:
        		c1= new Coordinate(pointCoords[pointIdx], pointCoords[pointIdx+1]);
        		coordList.add(c1, true);
        		c2= new Coordinate(pointCoords[pointIdx+2], pointCoords[pointIdx+3]);
        		coordList.add(c2, true);
        		if (bFirstList)
        		{
        			firstList.add(c1,true);
        			firstList.add(c2,true);
        		}

        		break;
        	case SEG_CUBICTO:
        		c1= new Coordinate(pointCoords[pointIdx], pointCoords[pointIdx+1]);
        		coordList.add(c1, true);
        		c2= new Coordinate(pointCoords[pointIdx+2], pointCoords[pointIdx+3]);
        		coordList.add(c2, true);
        		c3= new Coordinate(pointCoords[pointIdx+4], pointCoords[pointIdx+5]);
        		coordList.add(c3, true);
        		if (bFirstList)
        		{
        			firstList.add(c1,true);
        			firstList.add(c2,true);
        			firstList.add(c3,true);
        		}

        		break;
        	case SEG_CLOSE:
        		coordList.add(cInicio, true);
        		if (bFirstList)
        		{
        			firstList.add(cInicio,true);
        		}
        		break;

        	}
        	pointIdx += curvesize[type];
        }
		// Guardamos el path dandole la vuelta
		Coordinate[] coords = coordList.toCoordinateArray();
		boolean bFlipped = false;
		if (CGAlgorithms.isCCW(coords) != bCCW) // Le damos la vuelta
		{
			CoordinateArraySequence seq = new CoordinateArraySequence(coords);
			CoordinateSequences.reverse(seq);
			coords = seq.toCoordinateArray();


			// En el primer punto metemos un moveto
			pointCoordsAux[0] = coords[0].x;
			pointCoordsAux[1] = coords[0].y;
			pointTypesAux[0] = SEG_MOVETO;
			int idx = 2;
			i=0;
			int j=1;
			for (int k=0; k < coords.length; k++)
			{
				pointCoordsAux[idx++] = coords[k].x;
				pointCoordsAux[idx++] = coords[k].y;
	        	int type = pointTypes[i++];
	        	pointIdx += curvesize[type];
	        	switch (type)
	        	{
	        	case SEG_MOVETO:
	        		pointTypesAux[j] = SEG_LINETO;
	        		break;
	        	case SEG_LINETO:
	        		pointTypesAux[j] = SEG_LINETO;
	        		break;
	        	case SEG_QUADTO:
	        		pointTypesAux[j] = SEG_QUADTO;
	        		break;
	        	case SEG_CUBICTO:
	        		pointTypesAux[j] = SEG_CUBICTO;
	        		break;
	        	case SEG_CLOSE:
	        		// TODO: IMPLEMENTAR ESTO!!!
	        		break;

	        	}
	        	j++;

			}

	        pointTypes = pointTypesAux;
	        pointCoords = pointCoordsAux;
	        numCoords= numCoords+2;
	        numTypes++;
	        bFlipped  = true;

		}
		return bFlipped;
	}

    /**
     * Check if the first part is CCW.
     * @return
     */
    public boolean isCCW()
    {
        int i;

		PathIterator theIterator = getPathIterator(null, FConverter.FLATNESS); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
        Coordinate first = null;
        CoordinateList coordList = new CoordinateList();
        Coordinate c1;
        boolean bFirst = true;
		while (!theIterator.isDone()) {
			//while not done
			int type = theIterator.currentSegment(theData);
        	switch (type)
        	{
        	case SEG_MOVETO:
        		c1= new Coordinate(theData[0], theData[1]);
        		if (bFirst == false) // Ya tenemos la primera parte.
        			break;
        		if (bFirst)
        		{
        			bFirst=false;
        			first = c1;
        		}
        		coordList.add(c1, true);
        		break;
        	case SEG_LINETO:
        		c1= new Coordinate(theData[0], theData[1]);
        		coordList.add(c1, true);
        		break;

        	}
        	theIterator.next();
		}
		coordList.add(first, true);
        return CGAlgorithms.isCCW(coordList.toCoordinateArray());

    }

}
