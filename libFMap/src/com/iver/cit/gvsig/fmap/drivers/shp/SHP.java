/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib??ez, 50
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
package com.iver.cit.gvsig.fmap.drivers.shp;

import java.io.File;
import java.util.BitSet;

import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.shp.write.SHPMultiLine;
import com.iver.cit.gvsig.fmap.drivers.shp.write.SHPMultiPoint;
import com.iver.cit.gvsig.fmap.drivers.shp.write.SHPPoint;
import com.iver.cit.gvsig.fmap.drivers.shp.write.SHPPolygon;
import com.iver.cit.gvsig.fmap.drivers.shp.write.SHPShape;
import com.iver.cit.gvsig.fmap.drivers.shp.write.ShapefileException;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;


/**
 * Clase con las constantes que representan los diferentes tipos de Shape y
 * m?todos est?ticos relativos a los shapes.
 *
 * @author Vicente Caballero Navarro
 */
public class SHP {
    public static final int NULL = 0;
    public static final int POINT2D = 1;
    public static final int POLYLINE2D = 3;
    public static final int POLYGON2D = 5;
    public static final int MULTIPOINT2D = 8;
    public static final int POINT3D = 11;
    public static final int POLYLINE3D = 13;
    public static final int POLYGON3D = 15;
    public static final int MULTIPOINT3D = 18;
    
    //Geometries with M
    public static final int POINTM = 21;
    public static final int POLYLINEM = 23;
    public static final int POLYGONM = 25;
    public static final int MULTIPOINTM = 28;

    /**
     * Crea a partir del tipo de geometr?a un shape del tipo m?s adecuado.
     *
     * @param type Tipo de geometr?a.
     *
     * @return Geometr?a m?s adecuada.
     *
     * @throws ShapefileException Se lanza cuando es causada por la creaci?n
     *         del shape.
     */
    public static SHPShape create(int type) throws ShapefileException {
        SHPShape shape;

        switch (type) {
            case 1:
            case 11:
            case 21:
                shape = new SHPPoint(type);

                break;

            case 3:
            case 13:
            case 23:
                shape = new SHPMultiLine(type);

                break;

            case 5:
            case 15:
            case 25:
                shape = new SHPPolygon(type);

                break;

            case 8:
            case 18:
            case 28:
                shape = new SHPMultiPoint(type);

                break;

            default:
                shape = null;
        }

        return shape;
    }

    /**
     * Crea un fichero en formato shape con las geometr?as que se pasan como
     * par?metro en forma de array, un bitset para saber las seleccionadas, un
     * SelectableDataSource para obtener los valores y el fichero a crear.
     *
     * @param fgs Array de geometr?as.
     * @param bitset Bitset con la selecci?n.
     * @param sds SelectableDataSource.
     * @param f Fichero a crear o modificar.
     */
    public static void SHPFileFromGeometries(IGeometry[] fgs, BitSet bitset,
        SelectableDataSource sds, File f) {
        SHPOnlyFromGeometries(fgs, f);

        DBFFromGeometries dfg = new DBFFromGeometries(fgs, f);
        dfg.create(sds, bitset);
    }

    /**
     * Obtiene un fichero shape con las geometr?as que se pasan como par?metro,
     * sin crear un dbf.
     *
     * @param fgs Array de IGeometries.
     * @param f Fichero que hay que crear.
     */
    public static void SHPOnlyFromGeometries(IGeometry[] fgs, File f) {
        SHPSHXFromGeometries ssfg = new SHPSHXFromGeometries(fgs, f);
        ssfg.create();
    }

    /**
     * Devuelve un array con dos doubles, el primero representa el m?nimo valor
     * y el segundo el m?ximo de entre los valores que se pasan como par?metro
     * en forma de array.
     *
     * @param zs Valores a comprobar.
     *
     * @return Array de doubles con el valor m?nimo y el valor m?ximo.
     */
    public static double[] getZMinMax(double[] zs) {
        if (zs == null) {
            return null;
        }

        double min = Double.MAX_VALUE;
        double max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < zs.length; i++) {
            if (zs[i] > max) {
                max = zs[i];
            }

            if (zs[i] < min) {
                min = zs[i];
            }
        }

        return new double[] { min, max };
    }
    public static File getDbfFile(File shpFile){
    	String str = shpFile.getAbsolutePath();
		File directory=shpFile.getParentFile();
		File[] files=new File[0];
		if (directory!=null){
			MyFileFilter myFileFilter = new MyFileFilter(str);
			files=directory.listFiles(myFileFilter);
		}
		String[] ends=new String[] {"dbf","DBF","Dbf","dBf","DBf","dbF","DbF","dBF"};
		File dbfFile=findEnd(str,files,ends);
		return dbfFile;
    }
    public static File getShxFile(File shpFile){
    	String str = shpFile.getAbsolutePath();
		File directory=shpFile.getParentFile();
		File[] files=new File[0];
		if (directory!=null){
			MyFileFilter myFileFilter = new MyFileFilter(str);
			files=directory.listFiles(myFileFilter);
		}
		String[] ends=new String[] {"shx","SHX","Shx","sHx","SHx","shX","ShX","sHX"};
		File shxFile=findEnd(str,files,ends);
		return shxFile;
    }
    private static File findEnd(String str,File[] files, String[] ends) {
    	for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[0]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[1]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[2]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[3]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[4]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[5]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[6]))
				return dbfFile;
		}
		for (int i=0;i<files.length;i++) {
			File dbfFile=files[i];
			if (dbfFile.getAbsolutePath().endsWith(ends[7]))
				return dbfFile;
		}
		return new File(str.substring(0, str.length() - 3) + ends[0]);
    }

}
