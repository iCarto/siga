/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencefork.geometry;


import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;




/**
 * Base class for {@linkplain DirectPosition direct position} implementations. This base class
 * provides default implementations for {@link #toString}, {@link #equals} and {@link #hashCode}
 * methods.
 * <p>
 * This class do not holds any state. The decision to implement {@link java.io.Serializable}
 * or {@link org.geotools.util.Cloneable} interfaces is left to implementors.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/geometry/AbstractDirectPosition.java $
 * @version $Id: AbstractDirectPosition.java 28779 2008-01-16 11:00:24Z desruisseaux $
 * @author Martin Desruisseaux
 */
public abstract class AbstractDirectPosition implements DirectPosition {
    /**
     * Constructs a direct position.
     */
    protected AbstractDirectPosition() {
    }

    /**
     * Returns always {@code this}, the direct position for this
     * {@linkplain org.opengis.geometry.coordinate.Position position}.
     */
    public DirectPosition getPosition() {
        return this;
    }
    
    public abstract Object clone();

    /**
     * Sets this direct position to the given position. If the given position is
     * {@code null}, then all ordinate values are set to {@linkplain Double#NaN NaN}.
     *
     * @since 2.5
     */
    public void setPosition(final DirectPosition position) {
        final int dimension = getDimension();
        if (position != null) {
            ensureDimensionMatch("position", position.getDimension(), dimension);
            for (int i=0; i<dimension; i++) {
                setOrdinate(i, position.getOrdinate(i));
            }
        } else {
            for (int i=0; i<dimension; i++) {
                setOrdinate(i, Double.NaN);
            }
        }
    }

    /**
     * Returns a sequence of numbers that hold the coordinate of this position in its
     * reference system.
     *
     * @return The coordinates.
     */
    public double[] getCoordinates() {
        final double[] ordinates = new double[getDimension()];
        for (int i=0; i<ordinates.length; i++) {
            ordinates[i] = getOrdinate(i);
        }
        return ordinates;
    }

    /**
     * Convenience method for checking coordinate reference system validity.
     *
     * @param  crs The coordinate reference system to check.
     * @param  expected the dimension expected.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    static void checkCoordinateReferenceSystemDimension(final CoordinateReferenceSystem crs,
                                                        final int expected)
            throws MismatchedDimensionException
    {
        if (crs != null) {
            final int dimension = crs.getCoordinateSystem().getDimension();
            if (dimension != expected) {
                throw new MismatchedDimensionException("mismatched dimensino");
            }
        }
    }

    /**
     * Convenience method for checking object dimension validity.
     * This method is usually invoked for argument checking.
     *
     * @param  name The name of the argument to check.
     * @param  dimension The object dimension.
     * @param  expectedDimension The Expected dimension for the object.
     * @throws MismatchedDimensionException if the object doesn't have the expected dimension.
     */
    static void ensureDimensionMatch(final String name,
                                     final int dimension,
                                     final int expectedDimension)
            throws MismatchedDimensionException
    {
        if (dimension != expectedDimension) {
            throw new MismatchedDimensionException("mismatched dimension");
        }
    }



    
    /**
     * Returns a hash value for this coordinate.
     */
    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * Returns a hash value for the given coordinate.
     */
    static int hashCode(final DirectPosition position) {
        final int dimension = position.getDimension();
        int code = 1;
        for (int i=0; i<dimension; i++) {
            final long bits = Double.doubleToLongBits(position.getOrdinate(i));
            code = 31 * code + ((int)(bits) ^ (int)(bits >>> 32));
        }
        final CoordinateReferenceSystem crs = position.getCoordinateReferenceSystem();
        if (crs != null) {
            code += crs.hashCode();
        }
        return code;
    }

    /**
     * Returns {@code true} if the specified object is also a {@linkplain DirectPosition
     * direct position} with equals {@linkplain #getCoordinates coordinates} and
     * {@linkplain #getCoordinateReferenceSystem CRS}.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof DirectPosition) {
            final DirectPosition that = (DirectPosition) object;
            final int dimension = getDimension();
            if (dimension == that.getDimension()) {
                for (int i=0; i<dimension; i++) {
                    if (Double.doubleToLongBits(this.getOrdinate(i)) !=
                        Double.doubleToLongBits(that.getOrdinate(i)))
                    {
                        return false;
                    }
                }
                if (this.getCoordinateReferenceSystem().equals(that.getCoordinateReferenceSystem()))
                {
                    assert hashCode() == that.hashCode() : this;
                    return true;
                }
            }
        }
        return false;
    }
}
