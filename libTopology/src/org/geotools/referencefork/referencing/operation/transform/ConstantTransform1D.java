/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
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

import java.util.Arrays;


/**
 * A one dimensional, constant transform. Output values are set to a constant value regardless
 * of input values. This class is really a special case of {@link LinearTransform1D} in which
 * <code>{@link #scale} = 0</code> and <code>{@link #offset} = constant</code>. However, this
 * specialized {@code ConstantTransform1D} class is faster.
 *
 * @since 2.0
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/transform/ConstantTransform1D.java $
 * @version $Id: ConstantTransform1D.java 28520 2007-12-27 18:19:42Z desruisseaux $
 * @author Martin Desruisseaux
 */
final class ConstantTransform1D extends LinearTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1583675681650985947L;

    /**
     * Constructs a new constant transform.
     *
     * @param offset The {@code offset} term in the linear equation.
     */
    protected ConstantTransform1D(final double offset) {
        super(0, offset);
    }

    /**
     * Transforms the specified value.
     */
    @Override
    public double transform(double value) {
        return offset;
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    @Override
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, (float)offset);
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    @Override
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, offset);
    }
}
