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


import java.awt.geom.GeneralPath;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;


public class TriangleDT {
	public PointDT A;
	public PointDT B;
	public PointDT C;
	public double[] key;
	TriangleDT[] neighbour=new TriangleDT[3];

	/***
	   * Constructor
	   *
	   * @param TriangleDT - trinagle will be cloned
	   *
	   */
	
	public TriangleDT(TriangleDT T){
		A=T.A;
		B=T.B;
		C=T.C;
		setKey();
	}
	
	/***
	   * Constructor
	   *
	   * @param PointDT A - first vertex
	   * @param PointDT B - second vertex
	   * @param PointDT C - third vertex
	   *
	   */
	
	public TriangleDT (PointDT A,PointDT B, PointDT C){
		this.A=A;
		this.B=B;
		this.C=C;
		setKey();
	}

	/***
	   * Constructor
	   *
	   * @param Coordinate A - first vertex
	   * @param Coordinate B - second vertex
	   * @param Coordinate C - third vertex
	   *
	   */
	
	public TriangleDT (Coordinate A,Coordinate B, Coordinate C){
		this.A=new PointDT(A.x,A.y, A.z);
		this.B=new PointDT(B.x,B.y, B.z);
		this.C=new PointDT(C.x,C.y, C.z);
		setKey();
	}
	
	/***
	   * Constructor
	   *
	   * @param double[] - array of vertexes index 0-2 - Coordinate of vertex A (x,y,z)
	   *                                     index 3-5 - Coordinate of vertex B (x,y,z) 
	   *                                     index 6-8 - Coordinate of vertex C (x,y,z)
	   */
	
	public TriangleDT (double[] coordinate){
		A=new PointDT(coordinate[0],coordinate[1],coordinate[2]);
		B=new PointDT(coordinate[3],coordinate[4],coordinate[5]);
		C=new PointDT(coordinate[6],coordinate[7],coordinate[8]);
		setKey();
	}
	
	/***
	   * implicit Constructor
	   */
	
	public TriangleDT (){
	}
		
	/***
	   * The method for setting key of triangle
	   *
	   */
	
	
	private void setKey(){
		key=new double[2];
		key[0]=(A.x+B.x+C.x)/3;
		key[1]=(A.y+B.y+C.y)/3;
	}
	
	/***
	   * The method which testing, if the line intersect the triangle
	   *
	   * @param LineString - Geometry of line
	   * 
	   * @return boolean true - line intersect triangle
	   * 			     false - line doesn't intersect triangle
	   */
	
	
	public boolean containsLine(LineString newL){
		
		Coordinate[] newPoints={A,B,C,A}; 
		CoordinateArraySequence newPointsTriangle=new CoordinateArraySequence(newPoints);
		LinearRing trianglesPoints=new LinearRing(newPointsTriangle,new GeometryFactory());
		
		return newL.crosses(trianglesPoints.convexHull());
	}

	/***
	   * The method which testing, if the triangle contains the point
	   *
	   * @param PointDT - point which will be tested
	   * 
	   * @return boolean true - the triangle contains the point
	   * 				 false - the triangle doesn't contains point
	   * 
	   */
	
	
	public boolean contains(PointDT P){
      GeneralPath triangle = new GeneralPath();
	     
      triangle.moveTo((float)A.x,(float) A.y);
      triangle.lineTo((float)B.x,(float) B.y);
      triangle.lineTo((float)C.x,(float) C.y);
      triangle.lineTo((float)A.x,(float) A.y);
      triangle.closePath();
      return triangle.contains(P.x, P.y);
	}

	/***
	   * The method which testing, if the triangle contains the point
	   *
	   * @param PointDT - point which will be tested
	   * 
	   * @return boolean true - the triangle contains the point
	   * 				 false - the triangle doesn't contains point
	   * 
	   */

	
	protected boolean containsPointAsVertex(PointDT P){
		if(A.compare(P)||B.compare(P)||C.compare(P))
			return true;
		else
			return false;

	}
	
	/***
	   * The method which testing two triangles, if the triangles have one same point
	   * 
	   * @param TriangleDT - triangle to test
	   * 
	   * @return boolean true - the triangles have one same point
	   * 				 false - the triangles haven't one same point
	   * 
	   */
	
	protected boolean containsOneSamePointWith(TriangleDT T){
		if (T.A.compare(A)||T.A.compare(B)||T.A.compare(C))
			return true;
		if (T.B.compare(A)||T.B.compare(B)||T.B.compare(C))
			return true;
		if (T.C.compare(A)||T.C.compare(B)||T.C.compare(C))
			return true;
		else
			return false;
		
	}

	/***
	   * The method which testing two triangles, if the triangles have two same points
	   * 
	   * @param TriangleDT - triangle to test
	   * 
	   * @return boolean true - the triangles have two same point
	   * 				 false - the triangles haven't two same point
	   * 
	   */

	
	public boolean containsTwoPoints(PointDT P1,PointDT P2){
		if ((A.compare(P1)||B.compare(P1)||C.compare(P1))&&
				(A.compare(P2)||B.compare(P2)||C.compare(P2)))
			return true;
		return false;
	}
	
	/***
	   *  The method for converting to String
	   *
	   * @return String - "TriangleDT: A B C
	   *
	   */
	
	public String toString(){
		return ("TriangleDT: "+A.toString()+B.toString()+C.toString());
/*		System.out.print("Triangle  ");
		A.printPoint();
		B.printPoint();
		C.printPoint();
		System.out.print(" k:"+key[0]+" "+key[1]+"   "+key[2]);
		System.out.println();
		if (neighbour[0]!=null){
			System.out.print("           Soused0   ");
			neighbour[0].A.printPoint();
			neighbour[0].B.printPoint();
			neighbour[0].C.printPoint();
			System.out.println();
		}
		if (neighbour[1]!=null){
			System.out.print("           Soused1   ");
			neighbour[1].A.printPoint();
			neighbour[1].B.printPoint();
			neighbour[1].C.printPoint();
			System.out.println();
		}
		if (neighbour[2]!=null){
			System.out.print("           Soused2   ");
			neighbour[2].A.printPoint();
			neighbour[2].B.printPoint();
			neighbour[2].C.printPoint();
			System.out.println();
		}*/
		
	}
	
	/***
	   * The method which testing triangle, if the triangles is'nt line
	   * 
	   * @return boolean true - the triangle is triangle
	   * 				 false - the triangle is line
	   * 
	   */
	
	protected boolean isTriangle(){
		Coordinate[] newPoint=new Coordinate[4]; 
		newPoint[0]=A;
		newPoint[1]=B;
		newPoint[2]=C;
		newPoint[3]=A;
		CoordinateArraySequence newPointsTriangle=new CoordinateArraySequence(newPoint);
		
		LinearRing trianglesPoints=new LinearRing(newPointsTriangle,new GeometryFactory());

	
		if (trianglesPoints.convexHull().getGeometryType()=="Polygon")
			return true;
		else 
			return false;
	}
	
	/***
	   * The method which comparing two triangles, if the triangles have same coordinates of vertexes
	   *  
	   * @param TriangleDT - triangle to test
	   * 
	   * @return boolean true - the triangles are same
	   * 				 false - the triangles aren't same 
	   * 
	   */
	
	boolean compare(TriangleDT T){
		if ((T.A==A||T.A==B||T.A==C)&&
			(T.B==A||T.B==B||T.B==C)&&
			(T.C==A||T.C==B||T.C==C)){

			return true;
		}	

		return false;
	}
}
