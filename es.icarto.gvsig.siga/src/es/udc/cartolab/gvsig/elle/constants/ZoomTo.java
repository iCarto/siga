package es.udc.cartolab.gvsig.elle.constants;

import java.awt.geom.Rectangle2D;

import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;

import es.icarto.gvsig.commons.map.OverlayPoint;
import es.icarto.gvsig.commons.referencing.GPoint;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;

public class ZoomTo {

    private final MapControl mapControl;
    private PostgisTransformation transform;
    private final OverlayPoint overlayPoint;

    public ZoomTo(MapControl mapControl) {
        this.mapControl = mapControl;
        this.overlayPoint = new OverlayPoint(mapControl);
    }

    public void setTranformation(PostgisTransformation transform) {
        this.transform = transform;
    }

    public void zoom(GPoint shape, boolean drawPoint) {
        if (shape == null) {
            return;
        }

        GPoint reShape = new GPoint(shape.getProjection(), shape.getX(), shape.getY());
        if (transform != null) {
            reShape = transform.transform(shape, mapControl.getProjection());
        }
        Rectangle2D bbox = getGeometry(reShape.getBounds2D());
        if (bbox != null) {
            if (drawPoint) {
                overlayPoint.draw(reShape.getX(), reShape.getY());
            }
            zoomTo(bbox);
        }
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
            rectangle.setFrameFromCenter(rectangle.getCenterX(), rectangle.getCenterY(), rectangle.getCenterX() + 100,
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

    public void clearAllGraphics() {
        this.overlayPoint.clearAllGraphics();
    }
}
