/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencefork.referencing.operation.builder;

// J2SE dependencies
import java.io.Serializable;

import org.geotools.referencefork.geometry.DirectPosition2D;
import org.geotools.referencefork.geometry.GeneralDirectPosition;
import org.gvsig.referencing.ReferencingUtil;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;






/**
 * An association between a {@linkplain #getSource source} and {@linkplain #getTarget target}
 * direct positions. Accuracy information and comments can optionnaly be attached.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/MappedPosition.java $
 * @version $Id: MappedPosition.java 28081 2007-11-27 23:17:34Z desruisseaux $
 * @author Jan Jezek
 * @author Martin Desruisseaux
 */
public class MappedPosition implements Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 3262172371858749543L;

    /**
     * The source position.
     */
    private final DirectPosition source;

    /**
     * The target position.
     */
    private final DirectPosition target;

    /**
     * An estimation of mapping accuracy in units of target CRS axis,
     * or {@link Double#NaN} if unknow.
     */
    private double accuracy = Double.NaN;

    /**
     * Optionnal comments attached to this mapping, or {@code null} if none.
     */
    private String comments;

    /**
     * Creates a mapped position with {@linkplain #getSource source} and
     * {@linkplain #getTarget target} position of the specified dimension.
     * The initial coordinate values are 0.
     */
    public MappedPosition(final int dimension) {
        if (dimension == 2) {
            source = new DirectPosition2D();
            target = new DirectPosition2D();
        } else {
            source = new GeneralDirectPosition(dimension);
            target = new GeneralDirectPosition(dimension);
        }
    }

    /**
     * Creates a mapped position from the specified direct positions.
     *
     * @param source The original direct position.
     * @param target The associated direct position.
     */
    public MappedPosition(final DirectPosition source, final DirectPosition target) {
        ensureNonNull("source", source);
        ensureNonNull("target", target);
        this.source = source;
        this.target = target;
    }

    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the source direct position. For performance reasons, the current
     * implementation returns a reference to the internal object. However users
     * should avoid to modify directly the returned position and use
     * {@link #setSource} instead.
     */
    public DirectPosition getSource() {
        return source;
    }

    /**
     * Set the source direct position to the specified value.
     */
    public void setSource(final DirectPosition point) {
        if (source instanceof DirectPosition2D) {
            ((DirectPosition2D) source).setLocation(point);
        } else {
            ((GeneralDirectPosition) source).setLocation(point);
        }
    }

    /**
     * Returns the target direct position. For performance reasons, the current
     * implementation returns a reference to the internal object. However users
     * should avoid to modify directly the returned position and use
     * {@link #setTarget} instead.
     */
    public DirectPosition getTarget() {
        return target;
    }

    /**
     * Set the target direct position to the specified value.
     */
    public void setTarget(final DirectPosition point) {
        if (source instanceof DirectPosition2D) {
            ((DirectPosition2D) target).setLocation(point);
        } else {
            ((GeneralDirectPosition) target).setLocation(point);
        }
    }

    /**
     * Returns the comments attached to this mapping, or {@code null} if none.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Set the comments attached to this mapping. May be {@code null} if none.
     */
    public void setComments(final String comments) {
        this.comments = comments;
    }

    /**
     * Returns an estimation of mapping accuracy in units of target CRS axis,
     * or {@link Double#NaN} if unknow.
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Set the accuracy.
     */
    public void setAccuracy(final double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Computes the distance between the {@linkplain #getSource source point} transformed
     * by the supplied math transform, and the {@linkplain #getTarget target point}.
     *
     * @param  transform The transform to use for computing the error.
     * @param  buffer An optionnaly pre-computed direct position to use as a buffer,
     *         or {@code null} if none. The content of this buffer will be overwritten.
     * @return The distance in units of the target CRS axis.
     */
    public final double getError(final MathTransform transform, final DirectPosition buffer)
            throws TransformException
    {
        return distance(transform.transform(source, buffer), target);
    }

    /**
     * Returns the distance between the specified points.
     */
    private static double distance( DirectPosition source,  DirectPosition target) {
         int otherDim  = source.getDimension();
         int dimension = target.getDimension();
//        if (otherDim != dimension) {
//            throw new MismatchedDimensionException("mismatched dimension");
//        }
   
        if(otherDim > dimension){
        	source = ReferencingUtil.getInstance().truncateOrShrinkDimension(source, dimension);
        	otherDim = source.getDimension();
        }else if ( otherDim < dimension){
        	target = ReferencingUtil.getInstance().truncateOrShrinkDimension(target, otherDim);
        	dimension = target.getDimension();
        }
        double sum = 0;
        for (int i=0; i<dimension; i++) {
            final double delta = source.getOrdinate(i) - target.getOrdinate(i);
            sum += delta*delta;
        }
        return Math.sqrt(sum / dimension);
    }

    /**
     * Returns a hash code value for this mapped position.
     */
    public int hashCode() {
        return source.hashCode() + 37*target.hashCode();
    }

    /**
     * Compares this mapped position with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final MappedPosition that = (MappedPosition) object;
            return this.source.equals(that.source)   &&
                   this.target.equals(that.target)   &&
                   this.comments.equals(that.comments) &&
                   Double.doubleToLongBits(this.accuracy) ==
                   Double.doubleToLongBits(that.accuracy);
        }
        return false;
    }

    
}
