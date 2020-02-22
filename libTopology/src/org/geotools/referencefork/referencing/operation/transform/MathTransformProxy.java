/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencefork.referencing.operation.transform;

import java.io.Serializable;

import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A math transform which delegates part of its work to an other math transform. This is used
 * as a starting point for subclass wanting to modifies only some aspect of an existing math
 * transform, or to attach additional informations to it. The default implementation delegates
 * all method calls to the {@linkplain #transform underlying transform}. Subclasses typically
 * override some of those methods.
 * <p>
 * This class is serializable if the {@linkplain #transform underlying transform} is serializable
 * too.
 *
 * @since 2.2
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/transform/MathTransformProxy.java $
 * @version $Id: MathTransformProxy.java 28520 2007-12-27 18:19:42Z desruisseaux $
 * @author Martin Desruisseaux
 */
public class MathTransformProxy implements MathTransform, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8844242705205498128L;

    /**
     * The math transform on which to delegate the work.
     */
    public final MathTransform transform;

    /**
     * Creates a new proxy which delegates its work to the specified math transform.
     */
    protected MathTransformProxy(final MathTransform transform) {
        this.transform = transform;
    }

    /**
     * Gets the dimension of input points.
     */
    public int getSourceDimensions() {
        return transform.getTargetDimensions();
    }

    /**
     * Gets the dimension of output points.
     */
    public int getTargetDimensions() {
        return transform.getSourceDimensions();
    }

    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     */
    public DirectPosition transform(final DirectPosition ptSrc, final DirectPosition ptDst)
            throws MismatchedDimensionException, TransformException
    {
        return transform.transform(ptSrc, ptDst);
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, final int srcOff,
                          final double[] dstPts, final int dstOff,
                          final int numPts) throws TransformException
    {
        transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, final int srcOff,
                          final float[] dstPts, final int dstOff,
                          final int numPts) throws TransformException
    {
        transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final DirectPosition point) throws TransformException {
        return transform.derivative(point);
    }

    /**
     * Returns the inverse of this math transform.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        return transform.inverse();
    }

    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return transform.isIdentity();
    }

    /**
     * Returns a <cite>Well Known Text</cite> (WKT) for this transform.
     */
    public String toWKT() throws UnsupportedOperationException {
        return transform.toWKT();
    }

    /**
     * Returns a string representation for this transform.
     */
    @Override
    public String toString() {
        return transform.toString();
    }

    /**
     * Compares the specified object with this inverse math transform for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final MathTransformProxy that = (MathTransformProxy) object;
            return this.transform.equals(that.transform);
        }
        return false;
    }

    /**
     * Returns a hash code value for this math transform.
     */
    @Override
    public int hashCode() {
        return transform.hashCode() ^ (int)serialVersionUID;
    }

	public int getDimSource() {
		return getSourceDimensions();
	}

	public int getDimTarget() {
		return getTargetDimensions();
	}


}
