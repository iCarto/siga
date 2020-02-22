/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2002, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencefork.referencing.operation.transform;


/**
 * A one dimensional, identity transform. Output values are identical to input values.
 * This class is really a special case of {@link LinearTransform1D} optimized for speed.
 *
 * @since 2.0
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/transform/IdentityTransform1D.java $
 * @version $Id: IdentityTransform1D.java 28520 2007-12-27 18:19:42Z desruisseaux $
 * @author Martin Desruisseaux
 */
final class IdentityTransform1D extends LinearTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7378774584053573789L;

    /**
     * The shared instance of the identity transform.
     */
    public static final LinearTransform1D ONE = new IdentityTransform1D();

    /**
     * Constructs a new identity transform.
     */
    private IdentityTransform1D() {
        super(1, 0);
    }

    /**
     * Transforms the specified value.
     */
    @Override
    public double transform(double value) {
        return value;
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    @Override
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    @Override
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts);
    }
}
