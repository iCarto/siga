package org.gvsig.jts.voronoi.dt;
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, Geotools Project Managment Committee (PMC)
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
 *    
 *    @author      Josef Bezdek
 *	  @version     %I%, %G%
 *    @since JDK1.3 
 */


public class LineDT {
	public PointDT A;
	public PointDT B;
	
	/***
	   * Constructor 
	   * 	   
	   * @param PointDT A - start point
	   * @param PointDT B - end point
	   * 
	   */		
	
	public LineDT(PointDT A, PointDT B){
		this.A = A;
		this.B = B;
	}
}
