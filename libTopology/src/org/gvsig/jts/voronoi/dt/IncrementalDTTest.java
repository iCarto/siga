package org.gvsig.jts.voronoi.dt;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Date;
import junit.framework.*;
import java.util.LinkedList;
import java.util.Iterator;

public class IncrementalDTTest extends TestCase {
	LinkedList TIN = new LinkedList();
	LinkedList points = new LinkedList();
	
	public IncrementalDTTest(String name){
		super(name);
	}
	
	protected void setUp(){
		
	}
	
	protected void tearDown(){
		
	}

	private static long getTimeMillis() {
		Date d = new Date();
		return d.getTime();
	}

	protected boolean delaunay(TriangleDT T, PointDT A){
		double N=((Math.pow(T.C.x,2)-Math.pow(T.A.x,2)+Math.pow(T.C.y,2)-Math.pow(T.A.y,2))*(T.B.x-T.A.x)-(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)+Math.pow(T.B.y,2)-Math.pow(T.A.y,2))*(T.C.x-T.A.x))/
		 (-2*((T.A.y-T.C.y)*(T.B.x-T.A.x)-(T.A.y-T.B.y)*(T.C.x-T.A.x)));
		double M=(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)-Math.pow((T.A.y-N),2)+Math.pow((T.B.y-N),2))/(2*(T.B.x-T.A.x));
		double R=Math.sqrt(Math.pow((T.A.x-M),2)+Math.pow((T.A.y-N),2)); 			//vypocet polomeru
		if ((R-0.0001)>(Math.sqrt(Math.pow((A.x-M),2)+Math.pow((A.y-N),2))))		//tolerance pro trojuhelniku, ktere jsou soumerne podle osy, kterou prochazi spolecna hrana
			return true;
		else{
			if (R>0){
				return false;
			}
			else{
				return delaunay(new TriangleDT(T.B,T.C,T.A), A);
			}	
		}
	}

	
	protected void countTIN(int numberOfPoints){
		 IncrementalDT triangulace=new IncrementalDT();
		 long before = getTimeMillis();
		 for (int i=0;i<numberOfPoints;i++){
			 Coordinate x=new Coordinate((Math.random()*10000),(Math.random()*10000),0);
			 points.add(x);
			 triangulace.insertPoint(x);
		 }
		 System.out.println("");
		 System.out.println("cas vypoctu>  " +(getTimeMillis()-before));
		 System.out.println("triangulace OK:");
		 System.out.println("pocet trojuhelniku: "+triangulace.number_Triangles);
		 System.out.println("");
		 TIN = triangulace.triangles.getLinkedList();
	}
	
	public void testDelaunayCircles(){
		countTIN(1000);
		Iterator iterTIN = TIN.iterator();
		Iterator itPoints = points.iterator();
	
		while (iterTIN.hasNext()){
			TriangleDT T = (TriangleDT) iterTIN.next();
			while (itPoints.hasNext()){
				PointDT P = new PointDT((Coordinate)itPoints.next());
				if (!T.containsPointAsVertex(P)){
					assertEquals(delaunay(T,P),false);
				}
			}	
		}
	}
	
	public void testRightCountCircle(){
		Iterator iterTIN = TIN.iterator();
			
		while (iterTIN.hasNext()){
			TriangleDT T = (TriangleDT) iterTIN.next();
			double N=((Math.pow(T.C.x,2)-Math.pow(T.A.x,2)+Math.pow(T.C.y,2)-Math.pow(T.A.y,2))*(T.B.x-T.A.x)-(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)+Math.pow(T.B.y,2)-Math.pow(T.A.y,2))*(T.C.x-T.A.x))/
			 (-2*((T.A.y-T.C.y)*(T.B.x-T.A.x)-(T.A.y-T.B.y)*(T.C.x-T.A.x)));
			double M=(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)-Math.pow((T.A.y-N),2)+Math.pow((T.B.y-N),2))/(2*(T.B.x-T.A.x));
			double R=Math.sqrt(Math.pow((T.A.x-M),2)+Math.pow((T.A.y-N),2)); 			//vypocet polomeru

			assertEquals(R>0,true);
		}
		
	}
	
	public void testAllIsTriangle(){
		Iterator iterTIN = TIN.iterator();
	
		while (iterTIN.hasNext()){
			TriangleDT T = (TriangleDT) iterTIN.next();
			assertEquals(T.isTriangle(),true);
		}

	}

	public void testAllPointsExistInTIN(){
		Iterator iterTIN = TIN.iterator();
		Iterator itPoints = points.iterator();
		boolean contain;
		
		while (itPoints.hasNext()){
			contain = false;
			PointDT P = new PointDT((Coordinate)itPoints.next());
			while (iterTIN.hasNext()){
				TriangleDT T = (TriangleDT) iterTIN.next();
				if (T.containsPointAsVertex(P)){
					contain = true;
					break;
				}
			}
			assertEquals(contain, true);
		}
		
	}
	
	public void testDupliciteTriangles(){
		Iterator iterTIN = TIN.iterator();
		Iterator iterTIN2 = TIN.iterator();
		
		while (iterTIN.hasNext()){
			TriangleDT T = (TriangleDT) iterTIN.next();
			while (iterTIN2.hasNext()){
				TriangleDT TT = (TriangleDT) iterTIN2.next();
				assertEquals(T.compare(TT), false);
			}
		}
	}
	
	
	
	public static Test suite(){
		return new TestSuite(IncrementalDTTest.class);
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
