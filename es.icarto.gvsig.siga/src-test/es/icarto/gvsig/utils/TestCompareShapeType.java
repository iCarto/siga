package es.icarto.gvsig.utils;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.iver.cit.gvsig.fmap.core.FArc2D;
import com.iver.cit.gvsig.fmap.core.FCircle2D;
import com.iver.cit.gvsig.fmap.core.FEllipse2D;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FMultipoint3D;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint2DM;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2DM;
import com.iver.cit.gvsig.fmap.core.FPolygon3D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2DM;
import com.iver.cit.gvsig.fmap.core.FPolyline3D;
import com.iver.cit.gvsig.fmap.core.FPolyline3DM;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver;
import com.iver.cit.gvsig.fmap.layers.FLyrText;

public class TestCompareShapeType {

    private static int point;
    private static int pointZ;
    private static int pointM;

    private static int lineZM;
    private static int lineZ;
    private static int lineM;
    private static int line;

    private static int poly;
    private static int polyZ;
    private static int polyM;

    private static int text;
    private static int multi;
    private static int multipoint;
    private static int multipointZ;
    private static int circle;
    private static int arc;
    private static int ellipse;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        point = new FPoint2D().getShapeType();
        pointZ = new FPoint3D(0, 0, 0).getShapeType();
        pointM = new FPoint2DM(0, 0, 0).getShapeType();

        line = new FPolyline2D(null).getShapeType();
        lineZ = new FPolyline3D(null, null).getShapeType();
        lineM = new FPolyline2DM(null, null).getShapeType();
        lineZM = new FPolyline3DM(null, null, null).getShapeType();

        poly = new FPolygon2D(null).getShapeType();
        polyZ = new FPolygon3D(null, null).getShapeType();
        polyM = new FPolygon2DM(null, null).getShapeType();

        text = new FLyrText().getShapeType();

        // multi = new FGeometryCollection(null).getGeometryType();
        multi = new DXFMemoryDriver().getShapeType();

        multipoint = new FMultiPoint2D(new FPoint2D[0]).getGeometryType();
        multipointZ = new FMultipoint3D(new FPoint2D[0], null).getGeometryType();

        circle = new FCircle2D(null, null, 0).getShapeType();

        arc = new FArc2D(null, null, null, null).getShapeType();

        ellipse = new FEllipse2D(null, null, null, 0).getShapeType();
    }

    @Test
    /*
     * Harcodeamos los tipos para comprobar que no metemos algún gazapo en el test
     */
    public void testShapeTypes() {
        assertEquals(point, 1);
        assertEquals(pointZ, 1 + 512);
        // assertEquals(pointM, 1 + 1024); // FPoint2DM no está bien. Devuelve un tipo incorrecto

        assertEquals(line, 2);
        assertEquals(lineZ, 2 + 512);
        assertEquals(lineM, 2 + 1024);
        // assertEquals(lineZM, 2 + 512 + 1024); // FPolyline3DM no está bien. Devuelve un tipo incorrecto

        assertEquals(poly, 4);
        assertEquals(polyZ, 4 + 512);
        assertEquals(polyM, 4 + 1024);

        assertEquals(text, 8);

        assertEquals(multi, 16);
        assertEquals(multipoint, 32);
        assertEquals(multipointZ, 32 + 512);
        assertEquals(circle, 64);
        assertEquals(arc, 128);
        assertEquals(ellipse, 256);
    }

    @Test
    public void testSimbologyFactory() {
        assertEquals(FShape.POINT, typeWithoutZM(point));
        assertEquals(FShape.POINT, typeWithoutZM(pointZ));
        assertEquals(FShape.POINT, typeWithoutZM(pointM));

        assertEquals(FShape.LINE, typeWithoutZM(line));
        assertEquals(FShape.LINE, typeWithoutZM(lineZ));
        assertEquals(FShape.LINE, typeWithoutZM(lineM));
        assertEquals(FShape.LINE, typeWithoutZM(lineZM));

        assertEquals(FShape.POLYGON, typeWithoutZM(poly));
        assertEquals(FShape.POLYGON, typeWithoutZM(polyZ));
        assertEquals(FShape.POLYGON, typeWithoutZM(polyM));

        assertEquals(FShape.TEXT, typeWithoutZM(text));

        assertEquals(FShape.MULTI, typeWithoutZM(multi));
        assertEquals(FShape.MULTIPOINT, typeWithoutZM(multipoint));
        assertEquals(FShape.MULTIPOINT, typeWithoutZM(multipointZ));
        assertEquals(FShape.CIRCLE, typeWithoutZM(circle));
        assertEquals(FShape.ARC, typeWithoutZM(arc));
        assertEquals(FShape.ELLIPSE, typeWithoutZM(ellipse));

    }

    @Test
    public void testSimbologyFactory2() {
        assertEquals(FShape.POINT, foo3(point));
        assertEquals(FShape.POINT, foo3(pointZ));
        assertEquals(FShape.POINT, foo3(pointM));

        assertEquals(FShape.LINE, foo3(line));
        assertEquals(FShape.LINE, foo3(lineZ));
        assertEquals(FShape.LINE, foo3(lineM));
        assertEquals(FShape.LINE, foo3(lineZM));

        assertEquals(FShape.POLYGON, foo3(poly));
        assertEquals(FShape.POLYGON, foo3(polyZ));
        assertEquals(FShape.POLYGON, foo3(polyM));

    }

    public int typeWithoutZM(int shapeType) {
        // return shapeType % FShape.Z; // esto es suficiente, pero para que quede más claro
        return shapeType % FShape.Z % FShape.M;
    }

    public int foo3(int shapeType) {
        if ((shapeType & FShape.POINT) != 0) {
            return FShape.POINT;
        } else if ((shapeType & FShape.LINE) != 0) {
            return FShape.LINE;
        } else if ((shapeType & FShape.POLYGON) != 0) {
            return FShape.POLYGON;
        } else if ((shapeType & FShape.MULTIPOINT) != 0) {
            return FShape.MULTIPOINT;
        } else if ((shapeType & FShape.TEXT) != 0) {
            return FShape.TEXT;
        } else if ((shapeType & FShape.MULTI) != 0) {
            return FShape.MULTI;
        } else if (shapeType == FShape.NULL) {
            return FShape.NULL;
        }
        throw new Error("shape type not yet supported");
    }

    public String foo2(int shapeType) {
        switch (shapeType % FShape.Z) {
        case FShape.POINT:
            return "Point";
        case FShape.LINE:
            return "Line";
        case FShape.POLYGON:
            return "Polygon";
        default:
            throw new Error("Shape type not yet supported for multilayer symbols");
        }
    }

}
