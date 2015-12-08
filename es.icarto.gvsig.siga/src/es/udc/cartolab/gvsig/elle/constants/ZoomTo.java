package es.udc.cartolab.gvsig.elle.constants;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.layers.GraphicLayer;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;

import es.icarto.gvsig.commons.referencing.GPoint;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;

public class ZoomTo {

    private final MapControl mapControl;
    private PostgisTransformation transform;

    public ZoomTo(MapControl mapControl) {
	this.mapControl = mapControl;
    }

    public void setTranformation(PostgisTransformation transform) {
	this.transform = transform;
    }

    public void zoom(GPoint shape, boolean drawPoint) {
	if (shape == null) {
	    return;
	}
	GPoint reShape = transform.transform(shape, mapControl.getProjection());
	Rectangle2D bbox = getGeometry(reShape.getBounds2D());
	if (bbox != null) {
	    if (drawPoint) {
		drawPoint(reShape.getX(), reShape.getY());
	    }
	    zoomTo(bbox);
	}
    }

    private void drawPoint(double x, double y) {
	GraphicLayer lyr = mapControl.getMapContext().getGraphicsLayer();
	lyr.clearAllGraphics();
	Color color = Color.red;
	ISymbol theSymbol = SymbologyFactory.createDefaultSymbolByShapeType(
		FShape.POINT, color);

	int idSymbol = lyr.addSymbol(theSymbol);
	IGeometry geom = ShapeFactory.createPoint2D(x, y);
	FGraphic theGraphic = new FGraphic(geom, idSymbol);
	lyr.addGraphic(theGraphic);
	// mapControl.drawGraphics();

    }

    public void zoom(Rectangle2D rectangle) {
	if (rectangle == null) {
	    return;
	}
	Rectangle2D bbox = getGeometry(rectangle);
	if (bbox != null) {
	    zoomTo(bbox);
	}
    }

    private Rectangle2D getGeometry(Rectangle2D rectangle) {
	if (rectangle.getWidth() < 200) {
	    rectangle.setFrameFromCenter(rectangle.getCenterX(),
		    rectangle.getCenterY(), rectangle.getCenterX() + 100,
		    rectangle.getCenterY() + 100);
	}
	return rectangle;
    }

    private Rectangle2D zoomTo(Rectangle2D rectangle) {
	if (rectangle != null) {
	    final ViewPort viewPort = mapControl.getMapContext().getViewPort();
	    viewPort.setExtent(rectangle);
	    viewPort.refreshExtent();
	}
	return rectangle;
    }

    public void resetPoint() {
	GraphicLayer lyr = mapControl.getMapContext().getGraphicsLayer();
	lyr.clearAllGraphics();
    }
}
