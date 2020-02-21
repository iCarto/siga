/*
 * Created on 10-abr-2006
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
/* CVS MESSAGES:
 *
 * $Id: 
 * $Log: 
 */
package org.gvsig.referencing;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cresques.cts.IProjection;
import org.geotools.referencefork.geometry.GeneralDirectPosition;
import org.geotools.referencefork.geometry.ShapeUtilities;
import org.geotools.referencefork.referencing.operation.builder.RubberSheetBuilder;
import org.geotools.referencefork.referencing.operation.builder.TINTriangle;
import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.ShapePointExtractor;
import org.gvsig.jts.voronoi.FTriangle;
import org.gvsig.jts.voronoi.TriangleFeature;
import org.gvsig.topology.Messages;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.Gt2Geometry;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.gt2.FLiteShape;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLayerGenericVectorial;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;


/**
 * This class has convenience methods to work simultaneusly with different
 * versions of geoapi and geotools libraries.
 * 
 * This is needed because libFMap works with geoapi-2.0 and an old version of
 * geotools, while topology needs geoapi-2.2 and the latest version of geotools.
 * 
 * The main version has been consideered the older version of geotools, because
 * projection code works with this version.
 * 
 * @author Alvaro Zabala
 * 
 */
public class ReferencingUtil {

	private static final ReferencingUtil _instance = new ReferencingUtil();

	/**
	 * Number of created dialogs counter.
	 */
	private int numberOfAdjustSessions = 0;
	
	public static ReferencingUtil getInstance() {
		return _instance;
	}
	
	
	
	public  int getNumberOfSpatialAdjustSessions(){
		return numberOfAdjustSessions;
	}
	
	public void incrementAdjustSessions(){
		numberOfAdjustSessions++;
	}


	public DirectPosition create(double[] coordinates,
			CoordinateReferenceSystem crs) {
		double z = 0;
		if(coordinates.length > 2)
			z = coordinates[2];
		return new GeneralDirectPosition(coordinates[0], coordinates[1], z);
	}
	
	
	/*
	 * AZABALA: We have added this method to geotools original code to work with
	 * Shape whose coordinate's type is double, instead of floats (GeneralPathX
	 * instead GeneralPath)
	 */
	public void transform(final double[] srcPts, 
						  final int srcOff,
						  final double[] dstPts, 
						  final int dstOff,
						  final /*Abstract*/MathTransform trans,
						  final int numPts)throws TransformException {
		
		final int dimSource = trans.getSourceDimensions();
		final int dimTarget = trans.getTargetDimensions();
		final double[] tmpPts = new double[numPts
				* Math.max(dimSource, dimTarget)];
		for (int i = numPts * dimSource; --i >= 0;) {
			tmpPts[i] = srcPts[srcOff + i];
		}
		trans.transform(tmpPts, 0, tmpPts, 0, numPts);
		for (int i = numPts * dimTarget; --i >= 0;) {
			dstPts[dstOff + i] = (double) tmpPts[i];
		}
	}
	
	
	public final IGeometry createTransformedGeometry(final IGeometry geometry,
			 final AffineTransform preTransform,
			 final AffineTransform postTransform,
			 final /*Abstract*/MathTransform trans) throws TransformException, FactoryException{
		return createTransformedGeometry(geometry, preTransform, postTransform, trans, ShapeUtilities.PARALLEL);
	}
	
	
	public final IGeometry createTransformedGeometry(final IGeometry geometry,
													 final AffineTransform preTransform,
													 final AffineTransform postTransform,
													 final /*Abstract*/MathTransform trans,
													 final int orientation) throws TransformException, FactoryException{
		IGeometry solution = null;
		
		int dim;
		if ((dim = trans.getSourceDimensions()) != 2
				|| (dim = trans.getTargetDimensions()) != 2) {
			throw new MismatchedDimensionException("mismatched dimension");
		}
		
		Class<IGeometry> clazz = (Class<IGeometry>) geometry.getClass(); 
		if(clazz.isAssignableFrom(FGeometry.class)){
			FGeometry fgeo = (FGeometry) geometry;
			FShape fshape = (FShape) fgeo.getInternalShape();
			solution = ShapeFactory.createGeometry(createTransformedGeometry(fshape, preTransform, postTransform, trans, orientation));
		}else if(clazz.isAssignableFrom(FGeometryCollection.class)){
			FGeometryCollection col = (FGeometryCollection)geometry;
			IGeometry[] colGeoms = col.getGeometries();
			IGeometry[] geometries = new IGeometry[colGeoms.length];
			for (int i = 0; i < geometries.length; i++) {
				geometries[i] = createTransformedGeometry(colGeoms[i], 
														preTransform, 
														postTransform, 
														trans, orientation);
			}
			solution = new FGeometryCollection(geometries);
		}else if(clazz.isAssignableFrom(FMultiPoint2D.class)){
			FMultiPoint2D mPt = (FMultiPoint2D) geometry;
			int numPt = mPt.getNumPoints();
			FPoint2D[] solutionPt = new FPoint2D[numPt];
			double[] trgCoords = new double[2];
			for(int i = 0; i < numPt; i++){
				FPoint2D point = mPt.getPoint(i);
				double[] srcCoords = new double[]{point.getX(), point.getY()};
				transform(srcCoords, 0, trgCoords, 0, trans, 1);
				solutionPt[i] = new FPoint2D(trgCoords[0], trgCoords[1]);
			}
			solution = new FMultiPoint2D(solutionPt);
		}
//		else if(clazz.isAssignableFrom(FMultipoint3D.class)){
//			
//		}
		else if(clazz.isAssignableFrom(FNullGeometry.class)){
			solution = geometry;
		}else if(clazz.isAssignableFrom(Gt2Geometry.class)){
			Gt2Geometry gt2 = (Gt2Geometry) geometry;
			FLiteShape shp = (FLiteShape) gt2.getInternalShape();
			FLiteShape transformedShp = new FLiteShape(shp.getGeometry(), preTransform, trans, false);
			transformedShp.transform(postTransform);
			solution = ShapeFactory.createGeometry(transformedShp);
		}
		
		return solution;
	}
	
	/**
	 * Transform the specified FShape instance.
	 * 
	 * Only returns instances of FPoint2D, FPolyline2D or FPolygon2D
	 * (for example, curves are converted to polylines applying a flatness digitizing criteria)
	 * 
	 * @param shp
	 * @param preTransform
	 * @param postTransform
	 * @param trans
	 * @param orientation
	 * @return
	 * @throws TransformException
	 */
	public final FShape createTransformedGeometry(final FShape shp,
							final AffineTransform preTransform,
							final AffineTransform postTransform, 
							final /*Abstract*/MathTransform trans,
							final int orientation)
			throws TransformException {
		FShape solution = null;
		int dim;
		if ((dim = trans.getSourceDimensions()) != 2
				|| (dim = trans.getTargetDimensions()) != 2) {
			throw new MismatchedDimensionException("mismatched dimension");
		}
		
		final PathIterator it = shp.getPathIterator(preTransform);
		
		final GeneralPathX path = new GeneralPathX(it.getWindingRule());
		final Point2D.Double ctrl = new Point2D.Double();
		final double[] buffer = new double[6];

		double ax = 0, ay = 0; // Coordinate of the last point before
								// transform.
		
		double px = 0, py = 0; // Coordinate of the last point after transform.
	
		for (; !it.isDone(); it.next()) {
			switch (it.currentSegment(buffer)) {
			default: {
				throw new IllegalPathStateException();
			}
			case PathIterator.SEG_CLOSE: {
				/*
				 * Closes the geometric shape and continues the loop. We use the
				 * 'continue' instruction here instead of 'break' because we
				 * don't want to execute the code after the switch (addition of
				 * transformed points into the path - there is no such point in
				 * a SEG_CLOSE).
				 */
				path.closePath();
				continue;
			}
			
			case PathIterator.SEG_MOVETO: {
				/*
				 * Transforms the single point and adds it to the path. We use
				 * the 'continue' instruction here instead of 'break' because we
				 * don't want to execute the code after the switch (addition of
				 * a line or a curve - there is no such curve to add here; we
				 * are just moving the cursor).
				 */
				ax = buffer[0];
				ay = buffer[1];
				transform(buffer, 0, buffer, 0, trans, 1);
				px = buffer[0];
				py = buffer[1];
				path.moveTo( px,  py);
				continue;
			}
			
			case PathIterator.SEG_LINETO: {
				/*
				 * Inserts a new control point at 'buffer[0,1]'. This control
				 * point will be initialised with coordinates in the middle of
				 * the straight line:
				 * 
				 * x = 0.5*(x1+x2) y = 0.5*(y1+y2)
				 * 
				 * This point will be transformed after the 'switch', which is
				 * why we use the 'break' statement here instead of 'continue'
				 * as in previous case.
				 */
				buffer[0] = 0.5 * (ax + (ax = buffer[0]));
				buffer[1] = 0.5 * (ay + (ay = buffer[1]));
				buffer[2] = ax;
				buffer[3] = ay;
				break;
			}
			case PathIterator.SEG_QUADTO: {
				/*
				 * Replaces the control point in 'buffer[0,1]' by a new control
				 * point lying on the quadratic curve. Coordinates for a point
				 * in the middle of the curve can be computed with:
				 * 
				 * x = 0.5*(ctrlx + 0.5*(x1+x2)) y = 0.5*(ctrly + 0.5*(y1+y2))
				 * 
				 * There is no need to keep the old control point because it was
				 * not lying on the curve.
				 */
				buffer[0] = 0.5 * (buffer[0] + 0.5 * (ax + (ax = buffer[2])));
				buffer[1] = 0.5 * (buffer[1] + 0.5 * (ay + (ay = buffer[3])));
				break;
			}
			case PathIterator.SEG_CUBICTO: {
				/*
				 * Replaces the control point in 'buffer[0,1]' by a new control
				 * point lying on the cubic curve. Coordinates for a point in
				 * the middle of the curve can be computed with:
				 * 
				 * x = 0.25*(1.5*(ctrlx1+ctrlx2) + 0.5*(x1+x2)); y =
				 * 0.25*(1.5*(ctrly1+ctrly2) + 0.5*(y1+y2));
				 * 
				 * There is no need to keep the old control point because it was
				 * not lying on the curve.
				 * 
				 * NOTE: Le point calculé est bien sur la courbe, mais n'est
				 * pas nécessairement représentatif. Cet algorithme remplace
				 * les deux points de contrôles par un seul, ce qui se traduit
				 * par une perte de souplesse qui peut donner de mauvais
				 * résultats si la courbe cubique était bien tordue. Projeter
				 * une courbe cubique ne me semble pas être un problème
				 * simple, mais ce cas devrait être assez rare. Il se produira
				 * le plus souvent si on essaye de projeter un cercle ou une
				 * ellipse, auxquels cas l'algorithme actuel donnera quand même
				 * des résultats tolérables.
				 */
				buffer[0] = 0.25 * (1.5 * (buffer[0] + buffer[2]) + 0.5 * (ax + (ax = buffer[4])));
				buffer[1] = 0.25 * (1.5 * (buffer[1] + buffer[3]) + 0.5 * (ay + (ay = buffer[5])));
				buffer[2] = ax;
				buffer[3] = ay;
				break;
			}
			}
			/*
			 * Applies the transform on the point in the buffer, and append the
			 * transformed points to the general path. Try to add them as a
			 * quadratic line, or as a straight line if the computed control
			 * point is colinear with the starting and ending points.
			 */
			transform(buffer, 0, buffer, 0, trans, 2);
			
			final Point2D ctrlPoint = ShapeUtilities.parabolicControlPoint(px,
					py, buffer[0], buffer[1], buffer[2], buffer[3],
					orientation, ctrl);
			px = buffer[2];
			py = buffer[3];
			if (ctrlPoint != null) {
				assert ctrl == ctrlPoint;
				path.quadTo(ctrl.x, ctrl.y,  px,  py);
			} else {
				path.lineTo( px,  py);
			}
		}
		/*
		 * La projection de la forme géométrique est terminée. Applique une
		 * transformation affine si c'était demandée, puis retourne une
		 * version si possible simplifiée de la forme géométrique.
		 */
		if (postTransform != null) {
			path.transform(postTransform);
		}
		
		/*
		 * At this point, we have a GeneralPathX with the transformation.
		 * Now we reconstruct the original shp
		 * */
		int numDimensions = FGeometryUtil.getXyDimensions(shp);
		switch(numDimensions){
			case 0:
				List<Point2D[]> points = ShapePointExtractor.extractPoints(path.getPathIterator(null));
				solution = new FPoint2D(points.get(0)[0]);
				break;
			case 1:
				solution = new FPolyline2D(path);
				break;
			case 2:
				solution = new FPolygon2D(path);
				break;
			
		}
		return solution;
	}
	
	
	public DirectPosition truncateOrShrinkDimension(DirectPosition directPosition, int desiredDimensions){
		DirectPosition solution = null;
		double[] newCoordinates = new double[desiredDimensions];
		int pointDim = directPosition.getDimension();
		if(pointDim < desiredDimensions){
			double[] src = directPosition.getCoordinates();
			System.arraycopy(src, 0, newCoordinates, 0, pointDim);
//	    	for (int i = directPosition.getCoordinates().length; i < newCoordinates.length; i++) {
//				newCoordinates[i] = 0d;
//			}
		}
//    		throw new MismatchedDimensionException("mismatched dimension");
		else if (pointDim == desiredDimensions)
			return directPosition;
		else{
	    	System.arraycopy(directPosition.getCoordinates(), 0, newCoordinates, 0, desiredDimensions);
	    	
		}
		solution = new GeneralDirectPosition(newCoordinates);
    	return solution;
    	
	}
	
	
	public Point2D convert(DirectPosition position){
		double[] coordinates = position.getCoordinates();
		return new Point2D.Double(coordinates[0], coordinates[1]);
	}
	public FLyrVect getTinAsFMapLyr(RubberSheetBuilder rbBuilder, IProjection projection){
		FLyrVect solution = null;
		List<IFeature> features = new ArrayList<IFeature>();
		Set tinCollection = rbBuilder.getMapTriangulation().keySet();
		if(tinCollection.size() > 0){
			Iterator triangles = tinCollection.iterator();	
			while(triangles.hasNext()){
				TINTriangle t = (TINTriangle) triangles.next();
				FTriangle ftriangle = new FTriangle(convert(t.p0), convert(t.p1), convert(t.p2));
				Value fid = ValueFactory.createValue(features.size());
				Value[] values = new Value[] { fid };
				TriangleFeature feature = new TriangleFeature(ftriangle, values, new UID().toString());
				features.add(feature);
			}
			
			String lyrName = Messages.getText("RUBBER_SHEET_TIN_LAYER");
			LayerDefinition def = new LayerDefinition(){
				public int getShapeType(){
					return FShape.POLYGON;
				}	
			};
			
			FieldDescription fidDescription = new FieldDescription();
			fidDescription.setFieldName("fid");
			fidDescription.setFieldType(FieldDescription.stringToType("Integer"));
			fidDescription.setFieldLength(10);
			FieldDescription[] fields = {fidDescription};
			def.setFieldsDesc(fields);
			
			
			FeatureCollectionMemoryDriver driver = 
				new FeatureCollectionMemoryDriver(lyrName, features, def );
		
			solution =  new FLayerGenericVectorial();
			solution.setName(lyrName);
			solution.setProjection(projection);
			((FLayerGenericVectorial)solution).setDriver(driver);
			try {
				solution.load();
				IVectorLegend defaultLegend = 
					LegendFactory.createSingleSymbolLegend(FShape.POLYGON);
				defaultLegend.setDefaultSymbol(SymbologyFactory.
						createDefaultSymbolByShapeType(FShape.POLYGON, Color.GREEN));
				solution.setLegend(defaultLegend);
				
			} catch (LoadLayerException e) {
				e.printStackTrace();
			}
		}//if
		return solution;
	}
}
