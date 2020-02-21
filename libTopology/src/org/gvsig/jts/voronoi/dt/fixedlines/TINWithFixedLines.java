package org.gvsig.jts.voronoi.dt.fixedlines;

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


import java.util.Iterator;
import java.util.LinkedList;

import org.gvsig.jts.voronoi.dt.IncrementalDT;
import org.gvsig.jts.voronoi.dt.LineDT;
import org.gvsig.jts.voronoi.dt.PointDT;
import org.gvsig.jts.voronoi.dt.TriangleDT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class TINWithFixedLines {
	static LinkedList triangles;	// list of TIN triangles
	static LinkedList hardLines;	// list of hard lines
	
	
	/***
	   * The private method for searching a triangle which are intersect by new hard line
	   *
	   * @param PointDT - coordinate of new hard line
	   */			
	
	private static LinkedList getTrianglesIntersectLine(LineDT line){
		LinkedList trianglesToChange = new LinkedList();
		TriangleDT T = new TriangleDT();
		
		
		Coordinate[] newPoints={line.A,line.B}; 	// zalozeni nove geometrie
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoints);
		LineString newL=new LineString(newPointP,new GeometryFactory());

		Iterator iter = triangles.iterator();
		while (iter.hasNext()){
			T = (TriangleDT)iter.next();
			if (T.containsLine(newL)){	// test zda linie prochazi trojuhelnikem
					iter.remove();
					trianglesToChange.add(T);
			}
			else{	// testovani jestli linie ci bod trojuhelnika je obsazen v pevne hrane
				if (lineContainsPoint(newL, T.A)&&!T.A.compare(line.A)&&!T.A.compare(line.B)){
					iter.remove();
					trianglesToChange.add(T);
				}
				else{
					if (lineContainsPoint(newL, T.B)&&!T.B.compare(line.A)&&!T.B.compare(line.B)){
						iter.remove();
						trianglesToChange.add(T);
					}
					else{
						if (lineContainsPoint(newL, T.C)&&!T.C.compare(line.A)&&!T.C.compare(line.B)){
							iter.remove();
							trianglesToChange.add(T);
						}
					}	
				}	
			}
		}
		//hledani vnitrnich trojuhelniku
		return trianglesToChange;
		
	}
	
	
	/***
	   * The private method, which finds out if the line contains the point P
	   *
	   * @param PointDT - coordinates of the point P
	   *
	   * @return boolean - true : when the line contains the point P
	   *                   false: when the line doesn't contain the point P
	   *    
       */		
	
	private static boolean lineContainsPoint(LineString line, PointDT P){
		Coordinate[] newPoint={P}; 
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoint);
		Point newP=new Point(newPointP,new GeometryFactory());

		return line.covers(newP);
	}
	
	/***
	   * The private method for testing, if the LinkedList contains point P
	   *
	   * @param LinkedList - List of existing points
	   * @param PointDT - coordinates of the point P
	   *
	   * @return boolean - true : when the list contains the point P
	   *                   false: when the list doesn't contain the point P
	   *    
       */		
	
	private static boolean listContainsPoint(LinkedList points, PointDT P){
		Iterator iter = points.iterator();
		while (iter.hasNext()){		// pruchod seznamem bodu pro novou triangulaci
			if (((PointDT)iter.next()).compare(P))
				return true;
		}
		return false;
	}
	
	/***
	   * The private method for testing, if the LinkedList contains point P
	   *
	   * @param LinkedList - List of existing TIN triangles
	   * @param PointDT A- coordinates of start point of line
	   * @param PointDT B- coordinates of end point of line
	   *
	   * @return LinkedList - return List of point, which generate new TIN around hard line
	   * 						without start and end point of hard line
	   *    
	   */		
	
	private static LinkedList getPoints(LinkedList triangles, PointDT A, PointDT B){
		Iterator iter = triangles.iterator();
		LinkedList points = new LinkedList();
		TriangleDT T = (TriangleDT) iter.next();
		points.add(T.A);
		points.add(T.B);
		points.add(T.C);
		while (iter.hasNext()){  //test duplicity bodu v seznamu
			T = (TriangleDT) iter.next();
			if (!listContainsPoint(points,T.A))
				points.add(T.A);
			if (!listContainsPoint(points,T.B))
				points.add(T.B);
			if (!listContainsPoint(points,T.C))
				points.add(T.C);
		}
		//vymazani bodu A a B
		iter = points.iterator();
		PointDT P;
		while (iter.hasNext()){	//vymazani yacatku a konce linie ze seznamu bodu
			P = (PointDT)iter.next();
			if (P.compare(A)||P.compare(B))
					iter.remove();
		}
		return points;
	}

	/***
	   * The private method for testing, if the old triangles contains new triangle
	   *
	   * @param TriangleDT - triangle for test
	   * @param LinkedList - List of triangles of old triangulation,which will be deleted
	   *
	   * @return boolean - true : when the triangles contains the new triangle
	   *                   false: when the triangles don't contains the new triangle
	   *                       
       */		
	
	private static boolean testIsInside(TriangleDT T, LinkedList trians){
		Iterator iter = trians.iterator();
		while (iter.hasNext()){			//testovani zda teziste noveho trojuhelnika je uvnitr zrusenych trojuhelniku
			if (((TriangleDT)iter.next()).contains(new PointDT(T.key[0],T.key[1],0))){
				return true;
			}
		}
		return false;
	}
	
	/***
	   * The private method for testing and adding in old triangulation triangles
	   *
	   * @param LinkedList left - List of new triangles, which are on the left side of the line
	   * @param LinkedList right - List of new triangles, which are on the right side of the line
	   * @param LinkedList trianglesToChange - List of old triangles, which will be deleted from triangulation
	   *	
	   */		
	
	private static void testAndAddTrianglesToTIN(LinkedList left, LinkedList right, LinkedList trianglesToChange){
		TriangleDT T;
		if (left!=null){						//test leve triangulace
			Iterator it = left.iterator();
			
			while (it.hasNext()){
				T = (TriangleDT)it.next();

				if (testIsInside(T, trianglesToChange)){
					triangles.add(T);				//vlozeni do celkove triangulace
				}
			}
		}
		if (right!=null){						//test prave triagulace
			Iterator it = right.iterator();
			while (it.hasNext()){
				T = (TriangleDT)it.next();

				if (testIsInside(T, trianglesToChange)){
					triangles.add(T);				//vlozeni do celkove triangulace
				}
				
			}
		}

	}
	
	/***
	   * The static method for creating new triangles around the fixed lines in existing triangulation
	   *
	   * @param LinkedList trians - List of triangles, which generate TIN
	   * @param LinkedList hLines - List of fixed lines, which will be triangulated
	   * 
	   * @return LinkedList - list of new triangles, which generate TIN	
	   */		
	
	
	public static LinkedList countTIN(LinkedList trians, LinkedList hLines){
		triangles = trians;				// list TIN
		hardLines = hLines;				// list pevnych hran
		
		Iterator iter = hardLines.iterator();
		LineDT line;					// pomocna linie
		LinkedList trianglesToChange;  //nalezene trojuhelniky pres ktere prochazi linie
		LinkedList points; 				// body kterych se budou nove triangulovat
		LinkedList leftPoints;				// body nad primkou
		LinkedList rightPoints;			// body pod primkou
		IncrementalDT lTIN;				// triangulace na primkou
		IncrementalDT rTIN;				// triangulace pod primkou
		LinkedList leftTIN;				// TIN nad primkou 
		LinkedList rightTIN;			// TIN pod primkou
		PointDT P;						// pomocny bod
		double alfa;					// uhel, ktery svira pevna hrana s osou x
		double yTrans,xTrans;			// transfomovane souradnice (shodnostni transformace
										// uhel pooteceni alfa, posunuti 0 v x-ove i v y-ove ose
		while (iter.hasNext()){			//cyklus pro pevne hrany
			leftTIN = null;
			rightTIN = null;
			line = (LineDT) iter.next();
			leftPoints = new LinkedList();
			rightPoints = new LinkedList();
			
			
			trianglesToChange = getTrianglesIntersectLine(line);	// ziskani trojuhelniku, ktere protina pevna hrana
			
			
			if (trianglesToChange.size() != 0){
				points = getPoints(trianglesToChange, line.A, line.B);	// ziskani bodu z protnutych trojuhelniku
			
																	// vypocet uhlu natoceni v shodnostni transfomaci
				if ((line.B.x - line.A.x)!= 0)
					alfa = -1*(Math.atan((line.B.y-line.A.y)/(line.B.x - line.A.x)));
				else
					alfa = Math.PI/2;
			
				double yTransNullPoints = Math.cos(alfa)*line.A.y + Math.sin(alfa)*line.A.x;	// ziskani hodnoty y podle ktere se rozdeluje triangulace na pravou a levou
				System.out.println(yTransNullPoints);
				Iterator it = points.iterator();
			
				while (it.hasNext()){			// rozdeleni bodu do prave a leve triangulace
					P = (PointDT)it.next();
					yTrans = Math.cos(alfa)*P.y + Math.sin(alfa)*P.x;		// shodnostni transfomace
					xTrans = Math.cos(alfa)*P.x - Math.sin(alfa)*P.y;
					if (yTrans>=yTransNullPoints-0.002){
						leftPoints.add(P);
					}
					if (yTrans<=yTransNullPoints+0.002){
						rightPoints.add(P);
					}
				}

				lTIN = new IncrementalDT();    	// vypocet triangulace leve
				if (!leftPoints.isEmpty()){
				
					//P = (PointDT) leftPoints.firstKey();
					it = leftPoints.iterator();
					while (it.hasNext()){
						P = (PointDT)it.next();
						lTIN.insertPoint(P);
					}
			
					lTIN.insertPoint(line.A);
					lTIN.insertPoint(line.B);
					leftTIN = lTIN.triangles.getLinkedList();
				}
			
				rTIN = new IncrementalDT();		// vypocet triangulace prave
				if (!rightPoints.isEmpty()){
				
					//P = (PointDT) leftPoints.firstKey();
					it = rightPoints.iterator();
					while (it.hasNext()){
						P =(PointDT)it.next();
						rTIN.insertPoint(P);
					}
				
					rTIN.insertPoint(line.A);
					rTIN.insertPoint(line.B);
					rightTIN = rTIN.triangles.getLinkedList();
				}
			
					//test novych trojuhelniku, zda jsou v shapu
				testAndAddTrianglesToTIN(leftTIN, rightTIN, trianglesToChange);
			}
		}
		return triangles;
	}
}
