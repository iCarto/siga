package org.gvsig.mapsheets.print.series.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.gvsig.mapsheets.print.series.utils.IMapSheetsIdentified;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameView;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.utiles.XMLEntity;

/**
 * Special view frame to prevent the user from resizing it.
 * 
 * @author jldominguez
 *
 */
public class MapSheetFrameView extends FFrameView implements IMapSheetsIdentified{

    private boolean highlight = false;

    public MapSheetFrameView() {
	super();
	setId(System.currentTimeMillis());
    }

    /**
     * Devuelve un entero que representa donde esta contenido el punto que se
     * pasa como par�metro.
     *
     * @param p punto a comparar.
     *
     * @return entero que representa como esta contenido el punto.
     */
    @Override
    public int getContains(Point2D p) {
	if (getBoundingBox(null).contains(p.getX(), p.getY())) {
	    return RECT;
	} else {
	    return NOSELECT;
	}
    }

    @Override
    public IFFrame cloneFFrame(Layout lyt) {
	// return super.cloneFFrame(lyt);
	return this;
    }

    /**
     * helps in persistency
     */
    private long msid = -1;
    public long getId() {
	return msid;
    }

    public void setId(long id) {
	msid = id;
	try { Thread.sleep(40); } catch (Exception e) {}
    }

    @Override
    public XMLEntity getXMLEntity() throws SaveException {
	XMLEntity xml = super.getXMLEntity();
	xml.remove("className");
	xml.putProperty("className", MapSheetFrameView.class.getName());
	return xml;
    }

    @Override
    protected void printX(Graphics2D g, AffineTransform at) {
	Rectangle2D.Double r = getBoundingBox(at);

	// Dibujamos en impresora
	Rectangle rclip = g.getClipBounds();
	g.clipRect((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(),
		(int) r.getHeight());
	this.getMapContext().getViewPort().setOffset(new Point2D.Double(r.x, r.y));
	this.getMapContext().getViewPort().setImageSize(new Dimension((int) r.width,
		(int) r.height));

	ViewPort viewPort = this.getMapContext().getViewPort();
	Color theBackColor = viewPort.getBackColor();
	if (theBackColor != null) {
	    g.setColor(theBackColor);
	    g.fillRect((int) r.x, (int) r.y, viewPort
		    .getImageWidth(), viewPort
		    .getImageHeight());
	}

	try {
	    this.getMapContext().print(g, getScale(),getLayout().getLayoutContext().getAtributes().toPrintAttributes(), highlight);
	} catch (ReadDriverException e) {
	    NotificationManager.addError(e.getMessage(), e);
	}
	g.setClip(rclip.x, rclip.y, rclip.width, rclip.height);
    }

    public void setHighlight(boolean highlight) {
	this.highlight = highlight;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	Object resp = super.clone();
	this.getId();
	return resp;
    }

}
