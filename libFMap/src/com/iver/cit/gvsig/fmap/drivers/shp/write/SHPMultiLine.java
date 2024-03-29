/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.drivers.shp.write;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;

import com.iver.cit.gvsig.fmap.core.FGeometryM;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IGeometry3D;
import com.iver.cit.gvsig.fmap.core.IGeometryM;
import com.iver.cit.gvsig.fmap.core.v02.FConstant;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.shp.SHP;


/**
 * Elemento shape de tipo multil�nea.
 *
 * @author Vicente Caballero Navarro
 */
public class SHPMultiLine implements SHPShape {
	protected int m_type;
	protected int[] parts;
	protected FPoint2D[] points;
	protected double[] zs;
	//double flatness = 0.8; // Por ejemplo. Cuanto m�s peque�o, m�s segmentos necesitar� la curva

	/**
	 * Crea un nuevo SHPMultiLine.
	 */
	public SHPMultiLine() {
		m_type = FConstant.SHAPE_TYPE_POLYLINE;
	}

	/**
	 * Crea un nuevo SHPMultiLine.
	 *
	 * @param type Tipo de multil�nea.
	 *
	 * @throws ShapefileException
	 */
	public SHPMultiLine(int type) throws ShapefileException {
		if ((type != FConstant.SHAPE_TYPE_POLYLINE) &&
				(type != FConstant.SHAPE_TYPE_POLYLINEM) &&
				(type != FConstant.SHAPE_TYPE_POLYLINEZ)) {
			throw new ShapefileException("No es de tipo 3,13 ni 23");
		}

		m_type = type;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.shp.SHPShape#getShapeType()
	 */
	public int getShapeType() {
		return m_type;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.shp.SHPShape#read(MappedByteBuffer, int)
	 */
	public IGeometry read(MappedByteBuffer buffer, int type) {
		double minX = buffer.getDouble();
		double minY = buffer.getDouble();
		double maxX = buffer.getDouble();
		double maxY = buffer.getDouble();
		Rectangle2D rec = new Rectangle2D.Double(minX, minY, maxX - minX,
				maxY - maxY);

		int numParts = buffer.getInt();
		int numPoints = buffer.getInt(); //total number of points

		int[] partOffsets = new int[numParts];

		for (int i = 0; i < numParts; i++) {
			partOffsets[i] = buffer.getInt();
		}

		FPoint2D[] points = new FPoint2D[numPoints];

		for (int t = 0; t < numPoints; t++) {
			points[t] = new FPoint2D(buffer.getDouble(), buffer.getDouble());
		}

		/*   if (type == FConstant.SHAPE_TYPE_POLYLINEZ) {
		   //z min, max
		   buffer.position(buffer.position() + (2 * 8));
		   for (int t = 0; t < numPoints; t++) {
		       points[t].z = buffer.getDouble(); //z value
		   }
		   }
		 */
		return (IGeometry) new FPolyline2D(getGeneralPathX(points, partOffsets));
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.shp.SHPShape#write(ByteBuffer, IGeometry)
	 */
	public void write(ByteBuffer buffer, IGeometry geometry) {
		Rectangle2D rec = geometry.getBounds2D();
		buffer.putDouble(rec.getMinX());
		buffer.putDouble(rec.getMinY());
		buffer.putDouble(rec.getMaxX());
		buffer.putDouble(rec.getMaxY());
		int numParts = parts.length;
		int npoints = points.length;
		buffer.putInt(numParts);
		buffer.putInt(npoints);

		for (int i = 0; i < numParts; i++) {
			buffer.putInt(parts[i]);
		}

		for (int t = 0; t < npoints; t++) {
			buffer.putDouble(points[t].getX());
			buffer.putDouble(points[t].getY());
		}

		if (m_type == FConstant.SHAPE_TYPE_POLYLINEZ) {
			double[] zExtreame = SHP.getZMinMax(zs);
			if (Double.isNaN(zExtreame[0])) {
				buffer.putDouble(0.0);
				buffer.putDouble(0.0);
			} else {
				buffer.putDouble(zExtreame[0]);
				buffer.putDouble(zExtreame[1]);
			}
			for (int t = 0; t < npoints; t++) {
				double z = zs[t];
				if (Double.isNaN(z)) {
					buffer.putDouble(0.0);
				} else {
					buffer.putDouble(z);
				}
			}

		}
		if (m_type == FConstant.SHAPE_TYPE_POLYLINEM) {
			buffer.putDouble(-10E40);
			buffer.putDouble(-10E40);
			if (geometry instanceof IGeometryM){
				double[] ms = ((IGeometryM)geometry).getMs();
				for (int t = 0; t < npoints; t++) {
					buffer.putDouble(ms[t]);
				}
			}else{
				for (int t = 0; t < npoints; t++) {
					buffer.putDouble(0.0);
				}
			}
		}
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.shp.SHPShape#getLength(int)
	 */
	public int getLength(IGeometry fgeometry) {
		int numlines;
		int numpoints;
		int length;

		numlines = parts.length;
		numpoints = points.length;
		if (m_type == FConstant.SHAPE_TYPE_POLYLINE) {
			length = 44 + (4 * numlines) + (numpoints * 16);
		} else if (m_type == FConstant.SHAPE_TYPE_POLYLINEM) {
			length = 44 + (4 * numlines) + (numpoints * 16) +
			(8 * numpoints) + 16;
		} else if (m_type == FConstant.SHAPE_TYPE_POLYLINEZ) {
			length = 44 + (4 * numlines) + (numpoints * 16) +
			(8 * numpoints) + 16;
		} else {
			throw new IllegalStateException("Expected ShapeType of Arc, got " +
					m_type);
		}

		return length;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param po DOCUMENT ME!
	 * @param pa DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected GeneralPathX getGeneralPathX(FPoint2D[] po, int[] pa) {
		GeneralPathX gPX = new GeneralPathX(GeneralPathX.WIND_EVEN_ODD,
				po.length);
		int j = 0;

		for (int i = 0; i < po.length; i++) {
			if (i == pa[j]) {
				gPX.moveTo(po[i].getX(), po[i].getY());

				if (j < (pa.length - 1)) {
					j++;
				}
			} else {
				gPX.lineTo(po[i].getX(), po[i].getY());
			}
		}

		return gPX;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.shp.SHPShape#obtainsPoints(com.iver.cit.gvsig.fmap.core.GeneralPathXIterator)
	 */
	public void obtainsPoints(IGeometry g) {
		boolean is3D=false;
		if (FConstant.SHAPE_TYPE_POLYLINEZ == m_type || FConstant.SHAPE_TYPE_POLYGONZ == m_type){
			zs=((IGeometry3D)g).getZs();
			is3D=true;
		}
		ArrayList arrayPoints = null;
		ArrayList arrayParts = new ArrayList();
		PathIterator theIterator = g.getPathIterator(null, FConverter.FLATNESS); //polyLine.getPathIterator(null, flatness);
		double[] theData = new double[6];
		int numParts = 0;
		FPoint2D pFirst=null;
		int pos=0;
		ArrayList arrayZs=new ArrayList();
		Double firstZ = null;
		while (!theIterator.isDone()) {
			int theType = theIterator.currentSegment(theData);

			switch (theType) {
			case PathIterator.SEG_MOVETO:
				if (arrayPoints == null) {
					arrayPoints = new ArrayList();
					arrayParts.add(new Integer(0));
				} else {
					if (m_type==FConstant.SHAPE_TYPE_POLYGON ||
							m_type==FConstant.SHAPE_TYPE_POLYGONZ ||
							m_type==FConstant.SHAPE_TYPE_POLYGONM)
						if (!arrayPoints.get(arrayPoints.size()-1).equals(pFirst)){
							arrayPoints.add(new FPoint2D(pFirst.getX(),
									pFirst.getY()));
							if (is3D){
								arrayZs.add(firstZ);
							}
						}
					arrayParts.add(new Integer(arrayPoints.size()));
				}
				numParts++;
				pFirst=new FPoint2D(theData[0], theData[1]);
				arrayPoints.add(new FPoint2D(theData[0], theData[1]));
				if (is3D){
					firstZ=new Double(zs[pos]);
					arrayZs.add(new Double(zs[pos]));
					pos++;
				}
				break;

			case PathIterator.SEG_LINETO:
				arrayPoints.add(new FPoint2D(theData[0], theData[1]));
				if (is3D){
					arrayZs.add(new Double(zs[pos]));
					pos++;
				}
				break;

			case PathIterator.SEG_QUADTO:
				System.out.println("Not supported here");

				break;

			case PathIterator.SEG_CUBICTO:
				System.out.println("Not supported here");

				break;

			case PathIterator.SEG_CLOSE:
				if (!arrayPoints.get(arrayPoints.size()-1).equals(pFirst)){
					arrayPoints.add(new FPoint2D(pFirst.getX(),
							pFirst.getY()));
					if (is3D){
						arrayZs.add(firstZ);
					}
					// TODO: METER isM => add(firsM)
				}

				break;
			} //end switch

			theIterator.next();
		}

		Integer[] integers = (Integer[]) arrayParts.toArray(new Integer[0]);
		parts = new int[integers.length];
		for (int i = 0; i < integers.length; i++) {
			parts[i] = integers[i].intValue();
		}
		if (arrayPoints==null){
			points=new FPoint2D[0];
			return;
		}
		points = (FPoint2D[]) arrayPoints.toArray(new FPoint2D[0]);
		if (is3D){
			Double[] doubleZs=(Double[])arrayZs.toArray(new Double[0]);
			zs=new double[doubleZs.length];
			for (int i=0;i<doubleZs.length;i++){
				zs[i]=doubleZs[i].doubleValue();
			}
		}
	}

}
