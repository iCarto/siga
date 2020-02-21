/*
 * Created on 09-sep-2007
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
/* CVS MESSAGES:
*
* $Id$
* $Log: RobustLocationIndexedLine.java,v $
* Revision 1.1  2007/09/09 11:44:09  azabala
* Improvements to JTS (robustness problems)
*
*
*/
package org.gvsig.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LinearIterator;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class RobustLocationIndexedLine extends LocationIndexedLine {

	
	private Geometry linearGeom;
	
	
	public RobustLocationIndexedLine(Geometry arg0) {
		super(arg0);
		this.linearGeom = arg0;
	}
	
	 public LinearLocation indexOf(Coordinate pt)
	  {
	    return indexOfFromStart(pt, null);
	  }
	 
//	 These methods are needed because JTS LocationIndexedLine doesnt have an
		// indexOfAlter
		// method and LocationIndexPoint, which makes all these computations is not
		// a public class
		// report it to JTS project

		/*
		 * M.Davids has said to me that he has added these methods to JTS (1.9
		 * version).
		 * 
		 * TODO REMOVE THESE METHODS WITH JTS 1.9
		 */

		public  LinearLocation indexOfAfter(LocationIndexedLine indexedLine, 
										Coordinate inputPt, 
										LinearLocation minIndex) {
			if (minIndex == null)
				return indexedLine.indexOf(inputPt);
			LinearLocation endLoc = LinearLocation.getEndLocation(linearGeom);
			if (endLoc.compareTo(minIndex) <= 0)
				return endLoc;

			LinearLocation closestAfter = indexOfFromStart(inputPt, minIndex);
			return closestAfter;
		}

	
	
	private LinearLocation indexOfFromStart(Coordinate inputPt, LinearLocation minIndex)
	  {
	    double minDistance = Double.MAX_VALUE;
	    int minComponentIndex = 0;
	    int minSegmentIndex = 0;
	    double minFrac = -1.0;

	    LineSegment seg = new LineSegment();
	    for (LinearIterator it = new LinearIterator(linearGeom);
	         it.hasNext(); it.next()) {
	      if (! it.isEndOfLine()) {
	        seg.p0 = it.getSegmentStart();
	        seg.p1 = it.getSegmentEnd();
	        double segDistance = seg.distance(inputPt);
	        
	        /*
	         * Changes to LocationIndexedLine to avoid robustness problems
	         * */
	        PrecisionModel pm = linearGeom.getPrecisionModel();
	        segDistance = pm.makePrecise(segDistance);
	        
	        double segFrac = segmentFraction(seg, inputPt);

	        int candidateComponentIndex = it.getComponentIndex();
	        int candidateSegmentIndex = it.getVertexIndex();
	        
	        
	        if (segDistance < minDistance) {
	          // ensure after minLocation, if any
	          if (minIndex == null ||
	              minIndex.compareLocationValues(
	              candidateComponentIndex, candidateSegmentIndex, segFrac)
	              < 0
	              ) {
	            // otherwise, save this as new minimum
	            minComponentIndex = candidateComponentIndex;
	            minSegmentIndex = candidateSegmentIndex;
	            minFrac = segFrac;
	            minDistance = segDistance;
	          }
	        }
	      }
	    }
	    LinearLocation loc = new LinearLocation(minComponentIndex, minSegmentIndex, minFrac);
	    return loc;
	  }
	
	public static double segmentFraction(
		      LineSegment seg,
		      Coordinate inputPt)
		  {
		    double segFrac = seg.projectionFactor(inputPt);
		    if (segFrac < 0.0)
		      segFrac = 0.0;
		    else if (segFrac > 1.0)
		      segFrac = 1.0;
		    return segFrac;
		  }

}

