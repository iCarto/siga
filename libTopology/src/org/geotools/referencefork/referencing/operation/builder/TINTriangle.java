/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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

import org.geotools.referencefork.geometry.DirectPosition2D;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A triangle, with special methods for use with RubberSheetTransform.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/TINTriangle.java $
 * @version $Id: TINTriangle.java 28984 2008-01-28 18:08:29Z acuster $
 * @author Jan Jezek
 *
 */
public class TINTriangle extends Polygon {
    /** The first vertex. */
    public DirectPosition p0;

    /** The second vertex. */
    public DirectPosition p1;

    /** The third vertex. */
    public DirectPosition p2;
    private final List <TINTriangle> adjacentTriangles = new ArrayList <TINTriangle>();

    /**
     * Creates a Triangle.
     * @param p0 one vertex
     * @param p1 another vertex
     * @param p2 another vertex
     */
    protected TINTriangle(DirectPosition p0, DirectPosition p1,
        DirectPosition p2) {
        super(new DirectPosition[] { p0, p1, p2, p0 });
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Returns the CircumCicle of the this TINTriangle.
     *
     * @return Returns the CircumCicle of the this TINTriangle
     */
    protected Circle getCircumCicle() {
        //DirectPosition center = new DirectPosition2D();
        List <DirectPosition> reducedVertices = reduce();

        CoordinateReferenceSystem crs = ((DirectPosition) reducedVertices
            .get(1)).getCoordinateReferenceSystem();

        double x1 = ((DirectPosition) reducedVertices.get(1)).getCoordinates()[0];
        double y1 = ((DirectPosition) reducedVertices.get(1)).getCoordinates()[1];

        double x2 = ((DirectPosition) reducedVertices.get(2)).getCoordinates()[0];
        double y2 = ((DirectPosition) reducedVertices.get(2)).getCoordinates()[1];

        // Calculation of Circumcicle center
        double t = (0.5 * (((x1 * x1) + (y1 * y1)) - (x1 * x2) - (y1 * y2))) / ((y1 * x2)
            - (x1 * y2));

        //t = Math.abs(t);
        DirectPosition2D center = new DirectPosition2D(crs,
                (x2 / 2) - (t * y2) + p0.getCoordinates()[0],
                (y2 / 2) + (t * x2) + p0.getCoordinates()[1]);

        return new Circle(center.getPosition(),
            center.distance(new DirectPosition2D(p0)));
    }

    /**
     * Returns the three triangles that are created by splitting this
     * TINTriangle at a newVertex.
     *
     * @param newVertex the split point (must be inside triangle).
     *
     * @return three Triangles created by splitting this TINTriangle at a
     *         newVertex.
     */
    public List <TINTriangle> subTriangles(DirectPosition newVertex) {
        ArrayList <TINTriangle> triangles = new ArrayList<TINTriangle>();
        TINTriangle trigA = new TINTriangle(p0, p1, newVertex);
        TINTriangle trigB = new TINTriangle(p1, p2, newVertex);
        TINTriangle trigC = new TINTriangle(p2, p0, newVertex);

        // sub triangles are adjacent to each other.  
        try {
            trigA.addAdjacentTriangle(trigB);
            trigA.addAdjacentTriangle(trigC);

            trigB.addAdjacentTriangle(trigA);
            trigB.addAdjacentTriangle(trigC);

            trigC.addAdjacentTriangle(trigA);
            trigC.addAdjacentTriangle(trigB);
        } catch (TriangulationException e) {
            // should never reach here so we can ignore.           
        }

        //last adjacent triangle of each sub triangle is one of adjacent triangle of this triangle.
        trigA.tryToAddAdjacent(this.getAdjacentTriangles());
        trigB.tryToAddAdjacent(this.getAdjacentTriangles());
        trigC.tryToAddAdjacent(this.getAdjacentTriangles());

        triangles.add(trigA);
        triangles.add(trigB);
        triangles.add(trigC);

        Iterator <TINTriangle> i = this.getAdjacentTriangles().iterator();

        while (i.hasNext()) {
            TINTriangle trig = (TINTriangle) i.next();
            trig.removeAdjacent(this);
        }

        return triangles;
    }

    /**
     * Tries to add {@code adjacent} triangles. Before adding they are checked
     * whether they are really adjacent or not and whether they are not already known.
     * @param adjacents triangles to be added
     * @return number of successfully added triangles
     */
    protected int tryToAddAdjacent(List <TINTriangle> adjacents) {
        Iterator <TINTriangle> i = adjacents.iterator();
        int count = 0;

        while (i.hasNext()) {
            try {
                TINTriangle candidate = (TINTriangle) i.next();

                if (candidate.isAdjacent(this)
                        && !this.adjacentTriangles.contains(candidate)) {
                    this.addAdjacentTriangle(candidate);
                }

                if (candidate.isAdjacent(this)
                        && !candidate.adjacentTriangles.contains(this)) {
                    //this.addAdjacentTriangle(candidate);
                    candidate.addAdjacentTriangle(this);
                    count++;
                }
            } catch (TriangulationException e) {
                // should never reach here so we can ignore                
            }
        }

        return count;
    }

    /**
     * Adds adjacent triangles.
     * @param adjacent triangles to be added
     * @throws TriangulationException if triangle is not adjacent
     * @return true if the triangle is adjacent
     */
    protected boolean addAdjacentTriangle(TINTriangle adjacent)
        throws TriangulationException {
        if (isAdjacent(adjacent)) {
            adjacentTriangles.add(adjacent);

            return true;
        }

        return false;
    }

    /**
     * Checks whether triangle is adjacent.
     * @param adjacent triangle to be tested
     * @return true if triangle is adjacent
     * @throws TriangulationException if 
     */
    private boolean isAdjacent(TINTriangle adjacent)
        throws TriangulationException {
        int identicalVertices = 0;

        if (adjacent.hasVertex(p0)) {
            identicalVertices++;
        }

        if (adjacent.hasVertex(p1)) {
            identicalVertices++;
        }

        if (adjacent.hasVertex(p2)) {
            identicalVertices++;
        }

        if (identicalVertices == 3) {
            throw new TriangulationException("Same triangle");
        }

        if (identicalVertices == 2) {
            return true;
        }

        return false;
    }

    /**
     * Returns adjacent triangles
     * @return adjacent triangles
     */
    public List <TINTriangle> getAdjacentTriangles() {
        return adjacentTriangles;
    }

    /**
     * Removes adjacent triangles
     * @param remAdjacent
     */
    protected void removeAdjacent(TINTriangle remAdjacent) {
        adjacentTriangles.remove(remAdjacent);
    }
}
