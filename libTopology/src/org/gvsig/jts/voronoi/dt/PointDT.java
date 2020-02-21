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


import com.vividsolutions.jts.geom.Coordinate;


public class PointDT extends Coordinate{
	
	/***
	   * Constructor
	   *
	   * @param double x - coordinate of x-axis
	   * @param double y - coordinate of y-axis
	   * @param double z - coordinate of z-axis
	   */			
	
	public PointDT (double x, double y, double z){
		super(x,y,z);
	}

	/***
	   * Constructor
	   *
	   * @param Coordinate x - coordinate
	   *
	   */
		
	PointDT (Coordinate P){
		super (P.x,P.y,P.z);
	}
	
	/***
	   *  The method for converting to String
	   *
	   * @return String - "PointDT: x y z
	   *
	   */
	
	public String toString(){
		return ("PointDT: :"+x+" "+y+"   "+z+" ");
	}
	
	/***
	   * The method for comparing two points
	   *
	   * @param Coordinate P - the point which is compare
	   * 
	   * @return boolean - true - are same
	   * 	   			   false - are diffrent
	   */
	
	public boolean compare(Coordinate P){
		if (x==P.x&&y==P.y&&z==P.z)
			return true;
		return false;
	}

}
