/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
 */
package org.gvsig.rastertools.histogram;

import org.gvsig.raster.RasterProcess;
import org.gvsig.raster.datastruct.Histogram;
import org.gvsig.raster.datastruct.HistogramClass;
import org.gvsig.rastertools.BaseTest;
/**
 * Test para comprobar el buen funcionamiento del panel de Histograma
 * 
 * @version 07/05/2008
 * @author BorSanZa - Borja S�nchez Zamorano (borja.sanchez@iver.es)
 */
public class HistogramProcessTest extends BaseTest {
// TODO: shortImg, intImg, double: Falta por comprobar estos tipos
//   mrsid y ecw no son capas fiables pq da valores distintos en linux y windows
	
	// Variables para que en vez de comprobar si funciona, genere el codigo a pegar
	// en este test.
	boolean printCode = false;
	boolean printCodeStats = false;
	
	/**
	 * Capas para testear el histograma
	 */
	String[] layers = {band1, band2, band3, byteImg, floatImg};

	/**
	 * Tipo de las capas para ahorrar memoria. Un mismo numero es que comparten 
	 * minimos y maximos.
	 */
	int[] layersType = {0, 0, 0, 0, 1};
	boolean[] layersExist = {false, false, false};

	/*** Codigo autogenerado ***/
	//Testeando: ./test-images/band1-30x28byte.tif
	double[] min0 = {-128.0, -127.0, -126.0, -125.0, -124.0, -123.0, -122.0, -121.0, -120.0, -119.0, -118.0, -117.0, -116.0, -115.0, -114.0, -113.0, -112.0, -111.0, -110.0, -109.0, -108.0, -107.0, -106.0, -105.0, -104.0, -103.0, -102.0, -101.0, -100.0, -99.0, -98.0, -97.0, -96.0, -95.0, -94.0, -93.0, -92.0, -91.0, -90.0, -89.0, -88.0, -87.0, -86.0, -85.0, -84.0, -83.0, -82.0, -81.0, -80.0, -79.0, -78.0, -77.0, -76.0, -75.0, -74.0, -73.0, -72.0, -71.0, -70.0, -69.0, -68.0, -67.0, -66.0, -65.0, -64.0, -63.0, -62.0, -61.0, -60.0, -59.0, -58.0, -57.0, -56.0, -55.0, -54.0, -53.0, -52.0, -51.0, -50.0, -49.0, -48.0, -47.0, -46.0, -45.0, -44.0, -43.0, -42.0, -41.0, -40.0, -39.0, -38.0, -37.0, -36.0, -35.0, -34.0, -33.0, -32.0, -31.0, -30.0, -29.0, -28.0, -27.0, -26.0, -25.0, -24.0, -23.0, -22.0, -21.0, -20.0, -19.0, -18.0, -17.0, -16.0, -15.0, -14.0, -13.0, -12.0, -11.0, -10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0, 40.0, 41.0, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0, 61.0, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 68.0, 69.0, 70.0, 71.0, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 78.0, 79.0, 80.0, 81.0, 82.0, 83.0, 84.0, 85.0, 86.0, 87.0, 88.0, 89.0, 90.0, 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0, 110.0, 111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0, 120.0, 121.0, 122.0, 123.0, 124.0, 125.0, 126.0, 127.0};
	double[] max0 = {-127.0, -126.0, -125.0, -124.0, -123.0, -122.0, -121.0, -120.0, -119.0, -118.0, -117.0, -116.0, -115.0, -114.0, -113.0, -112.0, -111.0, -110.0, -109.0, -108.0, -107.0, -106.0, -105.0, -104.0, -103.0, -102.0, -101.0, -100.0, -99.0, -98.0, -97.0, -96.0, -95.0, -94.0, -93.0, -92.0, -91.0, -90.0, -89.0, -88.0, -87.0, -86.0, -85.0, -84.0, -83.0, -82.0, -81.0, -80.0, -79.0, -78.0, -77.0, -76.0, -75.0, -74.0, -73.0, -72.0, -71.0, -70.0, -69.0, -68.0, -67.0, -66.0, -65.0, -64.0, -63.0, -62.0, -61.0, -60.0, -59.0, -58.0, -57.0, -56.0, -55.0, -54.0, -53.0, -52.0, -51.0, -50.0, -49.0, -48.0, -47.0, -46.0, -45.0, -44.0, -43.0, -42.0, -41.0, -40.0, -39.0, -38.0, -37.0, -36.0, -35.0, -34.0, -33.0, -32.0, -31.0, -30.0, -29.0, -28.0, -27.0, -26.0, -25.0, -24.0, -23.0, -22.0, -21.0, -20.0, -19.0, -18.0, -17.0, -16.0, -15.0, -14.0, -13.0, -12.0, -11.0, -10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0, 40.0, 41.0, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0, 61.0, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 68.0, 69.0, 70.0, 71.0, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 78.0, 79.0, 80.0, 81.0, 82.0, 83.0, 84.0, 85.0, 86.0, 87.0, 88.0, 89.0, 90.0, 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0, 110.0, 111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0, 120.0, 121.0, 122.0, 123.0, 124.0, 125.0, 126.0, 127.0, 128.0};
	double[] value00 = {21.0, 23.0, 20.0, 20.0, 15.0, 17.0, 12.0, 9.0, 20.0, 15.0, 7.0, 11.0, 5.0, 10.0, 6.0, 5.0, 8.0, 4.0, 10.0, 6.0, 5.0, 5.0, 4.0, 1.0, 5.0, 2.0, 3.0, 3.0, 6.0, 3.0, 3.0, 3.0, 4.0, 1.0, 1.0, 2.0, 1.0, 3.0, 0.0, 2.0, 4.0, 2.0, 1.0, 2.0, 2.0, 3.0, 3.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 9.0, 12.0, 23.0, 33.0, 32.0, 29.0, 17.0, 29.0, 11.0, 9.0, 9.0, 8.0, 3.0, 3.0, 3.0, 1.0, 1.0, 0.0, 1.0, 6.0, 4.0, 5.0, 3.0, 2.0, 7.0, 6.0, 10.0, 8.0, 12.0, 12.0, 4.0, 5.0, 16.0, 13.0, 11.0, 13.0, 13.0, 13.0, 21.0, 20.0, 16.0, 27.0, 14.0, 14.0};
	double[] stats0 = {-128.0, 127.0, 19.646428571428572, 88.5, 840.0};
	// Testeando: ./test-images/band2-30x28byte.tif
	double[] value10 = {15.0, 9.0, 8.0, 5.0, 5.0, 7.0, 5.0, 5.0, 6.0, 7.0, 3.0, 9.0, 5.0, 1.0, 2.0, 8.0, 2.0, 1.0, 2.0, 1.0, 1.0, 4.0, 3.0, 3.0, 3.0, 2.0, 1.0, 4.0, 3.0, 2.0, 0.0, 2.0, 2.0, 3.0, 0.0, 1.0, 3.0, 2.0, 2.0, 0.0, 1.0, 0.0, 0.0, 3.0, 2.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 4.0, 12.0, 17.0, 30.0, 34.0, 22.0, 17.0, 11.0, 4.0, 15.0, 11.0, 16.0, 5.0, 4.0, 5.0, 3.0, 5.0, 1.0, 2.0, 1.0, 1.0, 2.0, 1.0, 1.0, 2.0, 0.0, 1.0, 3.0, 0.0, 3.0, 1.0, 0.0, 1.0, 0.0, 2.0, 0.0, 3.0, 3.0, 4.0, 2.0, 5.0, 9.0, 5.0, 9.0, 6.0, 8.0, 6.0, 5.0, 7.0, 7.0, 5.0, 6.0, 8.0, 5.0, 12.0, 9.0, 21.0, 12.0, 18.0, 7.0, 13.0, 21.0, 13.0, 22.0, 16.0, 13.0, 13.0, 15.0, 14.0, 16.0, 14.0, 13.0, 21.0, 10.0, 4.0, 12.0, 13.0, 11.0};
	double[] stats1 = {-128.0, 127.0, 52.45595238095238, 90.5, 840.0};
	// Testeando: ./test-images/band3-30x28byte.tif
	double[] value20 = {7.0, 4.0, 4.0, 7.0, 8.0, 6.0, 10.0, 7.0, 6.0, 6.0, 11.0, 9.0, 8.0, 5.0, 6.0, 8.0, 14.0, 10.0, 12.0, 11.0, 7.0, 7.0, 5.0, 8.0, 7.0, 8.0, 11.0, 10.0, 7.0, 10.0, 8.0, 8.0, 3.0, 2.0, 4.0, 1.0, 5.0, 4.0, 5.0, 6.0, 8.0, 6.0, 7.0, 8.0, 4.0, 4.0, 4.0, 5.0, 3.0, 3.0, 5.0, 5.0, 1.0, 3.0, 5.0, 4.0, 3.0, 3.0, 1.0, 2.0, 1.0, 3.0, 0.0, 2.0, 2.0, 1.0, 2.0, 5.0, 3.0, 1.0, 0.0, 2.0, 0.0, 3.0, 0.0, 1.0, 1.0, 1.0, 2.0, 0.0, 2.0, 1.0, 0.0, 0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 5.0, 6.0, 14.0, 31.0, 28.0, 32.0, 27.0, 23.0, 12.0, 10.0, 5.0, 3.0, 2.0, 5.0, 3.0, 1.0, 2.0, 1.0, 4.0, 0.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 2.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 2.0, 1.0, 3.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 3.0, 1.0, 0.0, 0.0, 1.0, 4.0, 2.0, 1.0, 2.0, 3.0, 3.0, 5.0, 3.0, 5.0, 1.0, 5.0, 4.0, 3.0, 3.0, 2.0, 3.0, 4.0, 5.0, 7.0, 9.0, 2.0, 3.0, 5.0, 4.0, 3.0, 6.0, 4.0, 4.0, 4.0, 6.0, 4.0, 8.0, 10.0, 9.0, 5.0, 13.0, 6.0};
	double[] stats2 = {-128.0, 127.0, -11.504761904761905, 28.5, 840.0};
	// Testeando: ./test-images/byte.tif
	double[] value30 = {1374.0, 1579.0, 1311.0, 1323.0, 1531.0, 1543.0, 1729.0, 1455.0, 1486.0, 1348.0, 1329.0, 1507.0, 1299.0, 1644.0, 1338.0, 1460.0, 1674.0, 1417.0, 1654.0, 1390.0, 1431.0, 1280.0, 1151.0, 1292.0, 1061.0, 1287.0, 1069.0, 1368.0, 1152.0, 1249.0, 1400.0, 1262.0, 1409.0, 1200.0, 1272.0, 1098.0, 1071.0, 1311.0, 1105.0, 1241.0, 1165.0, 1324.0, 1234.0, 1233.0, 1409.0, 1211.0, 1267.0, 1129.0, 973.0, 1117.0, 968.0, 1133.0, 1011.0, 1236.0, 1006.0, 902.0, 1166.0, 964.0, 1078.0, 961.0, 1080.0, 953.0, 949.0, 1060.0, 876.0, 1006.0, 869.0, 963.0, 803.0, 841.0, 1018.0, 844.0, 1095.0, 774.0, 691.0, 770.0, 628.0, 785.0, 602.0, 657.0, 617.0, 627.0, 669.0, 704.0, 776.0, 640.0, 748.0, 588.0, 552.0, 644.0, 535.0, 703.0, 526.0, 620.0, 529.0, 504.0, 629.0, 510.0, 572.0, 503.0, 439.0, 473.0, 384.0, 392.0, 321.0, 318.0, 276.0, 274.0, 282.0, 254.0, 301.0, 192.0, 208.0, 169.0, 160.0, 131.0, 106.0, 123.0, 114.0, 99.0, 85.0, 82.0, 74.0, 91.0, 59.0, 21.0, 7.0, 2.0, 260.0, 140.0, 291.0, 275.0, 495.0, 662.0, 367.0, 462.0, 538.0, 552.0, 575.0, 633.0, 564.0, 661.0, 679.0, 541.0, 622.0, 553.0, 685.0, 677.0, 577.0, 755.0, 678.0, 757.0, 673.0, 775.0, 655.0, 633.0, 654.0, 598.0, 763.0, 673.0, 697.0, 745.0, 706.0, 819.0, 730.0, 899.0, 810.0, 740.0, 894.0, 763.0, 828.0, 750.0, 867.0, 736.0, 759.0, 845.0, 790.0, 932.0, 794.0, 903.0, 772.0, 793.0, 932.0, 767.0, 926.0, 875.0, 873.0, 988.0, 870.0, 1027.0, 880.0, 1005.0, 910.0, 968.0, 1084.0, 1005.0, 1092.0, 913.0, 1039.0, 941.0, 892.0, 1084.0, 929.0, 1070.0, 927.0, 1089.0, 962.0, 1077.0, 1224.0, 1012.0, 1147.0, 1067.0, 1284.0, 1222.0, 1284.0, 1578.0, 1383.0, 1690.0, 1512.0, 1520.0, 1759.0, 1482.0, 1792.0, 1466.0, 1753.0, 1708.0, 1723.0, 1917.0, 1760.0, 1937.0, 1671.0, 2067.0, 1724.0, 1731.0, 2033.0, 1618.0, 1847.0, 1509.0, 1701.0, 1561.0, 1405.0, 1584.0, 1377.0, 1568.0, 1451.0, 1453.0, 1630.0, 1521.0, 1666.0, 1429.0, 1456.0, 1238.0, 1235.0, 1416.0, 1246.0, 1449.0};
	double[] stats3 = {-128.0, 127.0, 4.99680207783775, 22.5, 246410.0};
	// Testeando: ./test-images/float.tif
	double[] min1 = {514.0, 544.0952380952381, 574.1904761904761, 604.2857142857143, 634.3809523809524, 664.4761904761905, 694.5714285714286, 724.6666666666666, 754.7619047619048, 784.8571428571429, 814.952380952381, 845.047619047619, 875.1428571428571, 905.2380952380952, 935.3333333333333, 965.4285714285714, 995.5238095238095, 1025.6190476190477, 1055.7142857142858, 1085.8095238095239, 1115.904761904762, 1146.0, 1176.095238095238, 1206.1904761904761, 1236.2857142857142, 1266.3809523809523, 1296.4761904761904, 1326.5714285714284, 1356.6666666666665, 1386.7619047619048, 1416.857142857143, 1446.952380952381, 1477.047619047619, 1507.142857142857, 1537.2380952380952, 1567.3333333333333, 1597.4285714285713, 1627.5238095238096, 1657.6190476190477, 1687.7142857142858, 1717.8095238095239, 1747.904761904762, 1778.0, 1808.095238095238, 1838.1904761904761, 1868.2857142857142, 1898.3809523809523, 1928.4761904761904, 1958.5714285714287, 1988.6666666666667, 2018.7619047619048, 2048.857142857143, 2078.9523809523807, 2109.0476190476193, 2139.142857142857, 2169.2380952380954, 2199.333333333333, 2229.4285714285716, 2259.5238095238096, 2289.6190476190477, 2319.714285714286, 2349.809523809524, 2379.904761904762, 2410.0};
	double[] max1 = {544.0952380952381, 574.1904761904761, 604.2857142857143, 634.3809523809524, 664.4761904761905, 694.5714285714286, 724.6666666666666, 754.7619047619048, 784.8571428571429, 814.952380952381, 845.047619047619, 875.1428571428571, 905.2380952380952, 935.3333333333333, 965.4285714285714, 995.5238095238095, 1025.6190476190477, 1055.7142857142858, 1085.8095238095239, 1115.904761904762, 1146.0, 1176.095238095238, 1206.1904761904761, 1236.2857142857142, 1266.3809523809523, 1296.4761904761904, 1326.5714285714284, 1356.6666666666665, 1386.7619047619048, 1416.857142857143, 1446.952380952381, 1477.047619047619, 1507.142857142857, 1537.2380952380952, 1567.3333333333333, 1597.4285714285713, 1627.5238095238096, 1657.6190476190477, 1687.7142857142858, 1717.8095238095239, 1747.904761904762, 1778.0, 1808.095238095238, 1838.1904761904761, 1868.2857142857142, 1898.3809523809523, 1928.4761904761904, 1958.5714285714287, 1988.6666666666667, 2018.7619047619048, 2048.857142857143, 2078.9523809523807, 2109.0476190476193, 2139.142857142857, 2169.2380952380954, 2199.333333333333, 2229.4285714285716, 2259.5238095238096, 2289.6190476190477, 2319.714285714286, 2349.809523809524, 2379.904761904762, 2410.0, 2440.095238095238};
	double[] value40 = {992.0, 2009.0, 2334.0, 2509.0, 2536.0, 2813.0, 2783.0, 2733.0, 3013.0, 3293.0, 3348.0, 3238.0, 3401.0, 3407.0, 3699.0, 3814.0, 4129.0, 3931.0, 4010.0, 4352.0, 4510.0, 5879.0, 6496.0, 6550.0, 7343.0, 7435.0, 7089.0, 6348.0, 6031.0, 6283.0, 5393.0, 5795.0, 5893.0, 6024.0, 5775.0, 6085.0, 5755.0, 4791.0, 5017.0, 5256.0, 4741.0, 4842.0, 5428.0, 4386.0, 4380.0, 4148.0, 4078.0, 3839.0, 3622.0, 3605.0, 2798.0, 2543.0, 2973.0, 2402.0, 2378.0, 2215.0, 1844.0, 1307.0, 1094.0, 733.0, 472.0, 323.0, 166.0, 1.0};
	double[] stats4 = {514.0, 2410.0, 1410.917971402676, 1401.8095238095239, 246410.0};
	/*** Fin del codigo autogenerado ***/

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() {
		resetTime();
		if (!printCode && !printCodeStats) {
			System.err.println("***************************************");
			System.err.println("*** HistogramProcessTest running... ***");
			System.err.println("***************************************");
		}
	}

	/**
	 * Testea un histograma pasado por parametro comparandolo con su backup
	 * 
	 * @param layer     Posicion en el array de ficheros para saber cual estamos tratando
	 * @param type      Indica si es 0:Normal, 1:Acumulado, 2:Logaritmico 
	 * @param classes   Origen de datos que acabamos de leer
	 * @param min       Backup de los valores cuando se hizo el test para minimos
	 * @param max       Backup de los valores cuando se hizo el test para maximos
	 * @param histogram Backup de los valores cuando se hizo el test para histogramas
	 */
	private void testHistogram(int layer, int type, HistogramClass[][] classes, double[] min, double[] max, double[] histogram) {
		// Aqui se genera el codigo de los arrays de histogramas
		if (printCode) {
			boolean exist = false;
			if (layersExist[layersType[layer]] == false) {
				layersExist[layersType[layer]] = true;
				System.out.print("double[] min" + layersType[layer] + " = {");
				exist = false;
				for (int i = 0; i < classes.length; i++) {
					for (int j = 0; j < classes[i].length; j++) {
						if (exist)
							System.out.print(", ");
						System.out.print(classes[i][j].getMin());
						exist = true;
					}
				}
				System.out.println("};");
				System.out.print("double[] max" + layersType[layer] + " = {");
				exist = false;
				for (int i = 0; i < classes.length; i++) {
					for (int j = 0; j < classes[i].length; j++) {
						if (exist)
							System.out.print(", ");
						System.out.print(classes[i][j].getMax());
						exist = true;
					}
				}
				System.out.println("};");
			}
	
			System.out.print("double[] value" + layer + type + " = {");
			exist = false;
			for (int i = 0; i < classes.length; i++) {
				for (int j = 0; j < classes[i].length; j++) {
					if (exist)
						System.out.print(", ");
					System.out.print(classes[i][j].getValue());
					exist = true;
				}
			}
			System.out.println("};");
		}
		
		// Comprobar si el valor corresponde con el valor que teniamos guardado en el array
		if (!printCode) {
			int cont = 0;
			for (int i = 0; i < classes.length; i++) {
				for (int j = 0; j < classes[i].length; j++) {
					assertEquals(classes[i][j].getMin(), min[j], 0);
					assertEquals(classes[i][j].getMax(), max[j], 0);
					assertEquals(classes[i][j].getValue(), histogram[cont], 0);
					
					cont++;
				}
			}
		}
	}

	/**
	 * Testea una estadistica pasada por parametro comparandola con su backup
	 * 
	 * @param layer  Posicion en el array de ficheros para saber cual estamos tratando
	 * @param stats  Estadisticas que acabamos de leer
	 * @param stats2 Backup de las estadisticas de cuando se genero el Test
	 */
	private void testStats(int layer, double[][] stats, double[] stats2) {
		// Aqui se genera el codigo de los arrays de las estadisticas
		if (printCodeStats) {
			System.out.print("double[] stats" + layer + " = {");
			boolean exist = false;
			for (int i = 0; i < stats.length; i++) {
				for (int j = 0; j < stats[i].length; j++) {
					if (exist)
						System.out.print(", ");
					System.out.print(stats[i][j]);
					exist = true;
				}
			}
			System.out.println("};");
		}

		// Comprobar si el valor corresponde con el valor que teniamos guardado en el
		// array
		if (!printCodeStats) {
			int cont = 0;
			for (int i = 0; i < stats.length; i++) {
				for (int j = 0; j < stats[i].length; j++) {
					assertEquals(stats[i][j], stats2[cont], 0);
					cont++;
				}
			}
		}
	}
	
	/**
	 * Proceso para testear una capa pasada por parametro con todos sus backups
	 * @param pos Posicion en el array de ficheros para saber cual estamos tratando
	 * @param min
	 * @param max
	 * @param histogram
	 * @param histogramA
	 * @param histogramL
	 * @param stats
	 */
	private void testLayer(int pos, double[] min, double[] max, double[] histogram, double[] stats) {
		String name = layers[pos];
		if (printCode)
			System.out.println("// Testeando: " + name);
		openLayer(name);
		RasterProcess histogramProcess = new HistogramProcess();
		histogramProcess.addParam("histogramable", lyr.getDataSource());
		try {
			histogramProcess.execute();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Histogram histogramAux = (Histogram) histogramProcess.getResult();
		if (histogramAux != null) {
			testHistogram(pos, 0, histogramAux.getHistogram(), min, max, histogram);

			boolean[] bands = new boolean[histogramAux.getNumBands()];
			for (int i = 0; i < bands.length; i++)
				bands[i] = true;
			testStats(pos, histogramAux.getBasicStats(0.0, 100.0, bands), stats);
		}
	}

	public void testStack(){
		for (int i = 0; i < layers.length; i++) {
			if (printCode)
				System.out.println("testLayer(" + i + ", min" + layersType[i] + ", max" + layersType[i] + ", value" + i + "0, stats" + i +");");
			deleteRMF(layers[i]);
		}

		/*** Codigo autogenerado ***/
		testLayer(0, min0, max0, value00, stats0);
		testLayer(1, min0, max0, value10, stats1);
		testLayer(2, min0, max0, value20, stats2);
		testLayer(3, min0, max0, value30, stats3);
		testLayer(4, min1, max1, value40, stats4);
		/*** Fin del codigo autogenerado ***/

		if (!printCode && !printCodeStats) {
			System.err.println("***************************************");
			System.err.println("*** Time:" + getTime());
			System.err.println("*** HistogramProcessTest ending...  ***");
			System.err.println("***************************************");
		}
	}
}