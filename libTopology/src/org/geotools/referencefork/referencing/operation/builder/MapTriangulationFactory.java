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
import org.geotools.referencefork.referencing.operation.builder.MappedPosition;
import org.gvsig.referencing.ReferencingUtil;
import org.gvsig.topology.Messages;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.iver.utiles.swing.threads.CancellableProgressTask;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Implements methods for triangulation for  {@linkplain org.geotools.referencefork.referencing.operation.builder.RubberSheetBuilder
 * RubberSheeting} transformation.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/MapTriangulationFactory.java $
 * @version $Id: MapTriangulationFactory.java 28984 2008-01-28 18:08:29Z acuster $
 * @author Jan Jezek
 */
public class MapTriangulationFactory {
    private final Quadrilateral quad;
    private final List <MappedPosition> vectors;

    /**
     *
     * @param quad defines the area for transformation.
     * @param vectors represents pairs of identical points.
     * @throws TriangulationException thrown when the source points are outside the quad.
     */
    public MapTriangulationFactory(Quadrilateral quad,
        List <MappedPosition> vectors) throws TriangulationException {
        this.quad = quad;
        this.vectors = vectors;
    }
    
    public Map<TINTriangle, TINTriangle> getTriangleMap() throws TriangulationException{
    	return getTriangleMap(null);
    }

    /**
     * Generates map of source and destination triangles.
     *
     * @return Map of a source and destination triangles.
     *
     * @throws TriangulationException thrown when the source points are outside
     *         the quad.
     */
    public Map<TINTriangle, TINTriangle> getTriangleMap(CancellableProgressTask progressMonitor) throws TriangulationException {
        //FIXME Use progressMonitor to report progression of process
    	Quadrilateral mQuad = mappedQuad(quad, vectors);
        ExtendedPosition[] vertices = new ExtendedPosition[vectors.size()];

        // converts MappedPosition to ExtendedPosition     
        for (int i = 0; i < vectors.size(); i++) {
        	ReferencingUtil refUtil = ReferencingUtil.getInstance();
        	MappedPosition mappedPosition = vectors.get(i);
        	DirectPosition source = refUtil.truncateOrShrinkDimension(mappedPosition.getSource(), 2);
        	DirectPosition target = refUtil.truncateOrShrinkDimension(mappedPosition.getTarget(), 2);
        	
//            vertices[i] = new ExtendedPosition(((MappedPosition) vectors.get(i))
//                    .getSource(), ((MappedPosition) vectors.get(i)).getTarget());
        	
        	 vertices[i] = new ExtendedPosition(source, target);
        }

        TriangulationFactory triangulator = new TriangulationFactory(mQuad,
                vertices);
        List<TINTriangle> taggedSourceTriangles = triangulator.getTriangulation();
        final HashMap<TINTriangle, TINTriangle> triangleMap = new HashMap<TINTriangle, TINTriangle>();

        for (Iterator<TINTriangle> i = taggedSourceTriangles.iterator(); i.hasNext();) {
            final TINTriangle sourceTriangle = (TINTriangle) i.next();
            triangleMap.put(sourceTriangle,
                new TINTriangle(((ExtendedPosition) sourceTriangle.p0)
                    .getMappedposition(),
                    ((ExtendedPosition) sourceTriangle.p1).getMappedposition(),
                    ((ExtendedPosition) sourceTriangle.p2).getMappedposition()));
        }

        return triangleMap;
    }

    /**
     * Generates mapped quad from destination quad and source quad. The
     * vertices of destination quad are calculated from source quad and
     * difference of nearest pair of identical points.
     *
     * @param sourceQuad the quad that defines the area for triangulating.
     * @param vectors of identical points (MappedCoordinates).
     *
     * @return destination quad
     */
    private Quadrilateral mappedQuad(Quadrilateral sourceQuad, List<MappedPosition> vectors) {
        if (vectors.isEmpty()) {
            return (Quadrilateral) sourceQuad.clone();
        }

        //super.setMappedPositions(vectors);
        MappedPosition[] mappedVertices = new MappedPosition[4];

        for (int i = 0; i < mappedVertices.length; i++) {
            mappedVertices[i] = generateCoordFromNearestOne(sourceQuad.getPoints()[i],
                    vectors);
        }

        return new Quadrilateral(new ExtendedPosition(
                mappedVertices[0].getSource(), mappedVertices[0].getTarget()),
            new ExtendedPosition(mappedVertices[1].getSource(),
                mappedVertices[1].getTarget()),
            new ExtendedPosition(mappedVertices[2].getSource(),
                mappedVertices[2].getTarget()),
            new ExtendedPosition(mappedVertices[3].getSource(),
                mappedVertices[3].getTarget()));
    }

    /**
     * Calculate the destination position for the quad vertex as source
     * position using the difference between nearest source and destination
     * point pair.
     *
     * @param x the original coordinate.
     * @param vertices List of the MappedPosition.
     *
     * @return MappedPosition from the original and new coordinate, so the
     *         difference between them is the same as for the nearest one
     *         MappedPosition. It is used for calculating destination quad.
     */
    protected MappedPosition generateCoordFromNearestOne(DirectPosition x,
        List <MappedPosition> vertices) {
        MappedPosition nearestOne = nearestMappedCoordinate(x, vertices);

        double dstX = x.getCoordinates()[0]
            + (nearestOne.getTarget().getCoordinates()[0]
            - nearestOne.getSource().getCoordinates()[0]);
        double dstY = x.getCoordinates()[1]
            + (nearestOne.getTarget().getCoordinates()[1]
            - nearestOne.getSource().getCoordinates()[1]);
        DirectPosition dst = new DirectPosition2D(nearestOne.getTarget()
                                                            .getCoordinateReferenceSystem(),
                dstX, dstY);

        return new MappedPosition(x, dst);
    }

    /**
     * Returns the nearest MappedPosition to specified point P.
     *
     * @param dp P point.
     * @param vertices the List of MappedCoordinates.
     *
     * @return the MappedPosition to the x Coordinate.
     */
    protected MappedPosition nearestMappedCoordinate(DirectPosition dp,
        List <MappedPosition> vertices) {
        DirectPosition2D x = new DirectPosition2D(dp);

        // Assert.isTrue(vectors.size() > 0);
        MappedPosition nearestOne = (MappedPosition) vertices.get(0);

        //FIXME Rehacer todas las clases que gestionan los triangulos del rubber sheeting para unificarlas con los geoprocesos
        //de TIN de gvSIG
        for (Iterator <MappedPosition> i = vertices.iterator(); i.hasNext();) {
            MappedPosition candidate = (MappedPosition) i.next();

            DirectPosition source = candidate.getSource();
            DirectPosition target = candidate.getTarget();
            ReferencingUtil refUtil = ReferencingUtil.getInstance();
            DirectPosition2D source2D = new DirectPosition2D(refUtil.truncateOrShrinkDimension(source, 2));
            DirectPosition2D target2D = new DirectPosition2D(refUtil.truncateOrShrinkDimension(target, 2));
            
            if (source2D.distance(x.toPoint2D()) < target2D.distance(x.toPoint2D())) {
            	nearestOne = candidate;
            }//if
            
//            if (((DirectPosition2D) candidate.getSource()).distance(
//                        x.toPoint2D()) < ((DirectPosition2D) nearestOne
//                    .getSource()).distance(x.toPoint2D())) {
//                nearestOne = candidate;
//            }
        }//for

        return nearestOne;
    }
}
