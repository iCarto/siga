package org.gvsig.jts.voronoi.dt;


import com.vividsolutions.jts.geom.Coordinate;
import java.util.Date;

public class Testing {
	
	 private static long getTimeMillis() {
			Date d = new Date();
			return d.getTime();
		    }

	/**
	 * @param args
	 */
	 public static void main(String[] args) {
	 float [][] datain=new float[30][2];
		 	datain[0][0]=37;datain[0][1]=50;
		 	datain[1][0]=26;datain[1][1]=64;
		 	datain[2][0]=2;datain[2][1]=70;
		 	datain[3][0]=71;datain[3][1]=52;
		 	datain[4][0]=45;datain[4][1]=82;
		 	datain[5][0]=48;datain[5][1]=92;
		  	datain[6][0]=6;datain[6][1]=4;
		 	datain[7][0]=40;datain[7][1]=20;
		 	datain[8][0]=13;datain[8][1]=0;
		 	datain[9][0]=9;datain[9][1]=3;
		 	datain[10][0]=10;datain[10][1]=10;
		 	datain[11][0]=10;datain[11][1]=20;
		 	datain[12][0]=20;datain[12][1]=10;
		 	datain[13][0]=20;datain[13][1]=20;
		 	datain[14][0]=15;datain[14][1]=20;
		 	datain[15][0]=15;datain[15][1]=30;
		 	datain[16][0]=2;datain[16][1]=11;
		 	datain[17][0]=3;datain[17][1]=1;
		 	datain[18][0]=5;datain[18][1]=8;
		 	datain[19][0]=2;datain[19][1]=1;
		 	datain[20][0]=9;datain[20][1]=15;
		 	datain[21][0]=7;datain[21][1]=13;
		 	datain[22][0]=6;datain[22][1]=2;
		 	datain[23][0]=13;datain[23][1]=13;
		 	datain[24][0]=11;datain[24][1]=4 ;
		 	datain[25][0]=14;datain[25][1]=7;
		 	datain[26][0]=11;datain[26][1]=10;
		 	datain[27][0]=15;datain[27][1]=15;
		 	datain[28][0]=12;datain[28][1]=8; 
		 	datain[29][0]=14;datain[29][1]=11;
	//System.out.println((int)(Math.random()*1000000));
	int a=0;
/*
IncrementalDT triangulace=new IncrementalDT();
	for (int i=0;i<=25;i++){
		
		 System.out.println(i+"================");
	//	 System.out.println("");
		 
		 Coordinate x=new Coordinate(datain[i][0],datain[i][1]);
		 System.out.println(x.toString());
	triangulace.insertPoint(x);
	}
	triangulace.printAllTriangles();
	 } 
	
		 
/*/
		 while (true){
			 IncrementalDT triangulace=new IncrementalDT();
			 long before = getTimeMillis();
			 for (int i=0;i<10000;i++){
				 Coordinate x=new Coordinate((Math.random()*10000),(Math.random()*10000),0);
				// System.out.println(i);
				// System.out.println(x.toString());
			 	//System.out.println();
				 triangulace.insertPoint(x);
			 }
			 System.out.println("");
			 System.out.println("cas vypoctu>  " +(getTimeMillis()-before));
			 System.out.println("triangulace OK:");
			 System.out.println("pocet trojuhelniku: "+triangulace.number_Triangles);
			// System.out.println(triangulace.numb);
			 System.out.println("");
		//	 triangulace.getShapeFile();
		 }
	 }//*/
}	 