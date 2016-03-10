package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.styles.ImageStyle;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.AbstractMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

// Based on PictureMarkerSymbol
public class SignalSymbol extends AbstractMarkerSymbol {

    // transient private Image img;
    private final ImageStyle bgImage;
    private final String file;

    public SignalSymbol(SignalCache cache, String file) throws IOException {
	this.file = file;
	bgImage = cache.getImage(file);
    }

    public SignalSymbol(SignalCache cache, XMLEntity xml) throws IOException {
	String file = xml.getStringProperty("file");
	this.file = file;
	bgImage = cache.getImage(file);
	setXMLEntity(xml);
    }

    @Override
    public ISymbol getSymbolForSelection() {
	return this;
    }

    @Override
    public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp,
	    Cancellable cancel) {
	FPoint2D p = (FPoint2D) shp;
	double x, y;
	int size = (int) Math.round(getSize());
	double halfSize = getSize() / 2;
	x = p.getX() - halfSize;
	y = p.getY() - halfSize;
	int xOffset = (int) getOffset().getX();
	int yOffset = (int) getOffset().getY();

	if (size > 0) {

	    Rectangle rect = new Rectangle(size, size);
	    g.translate(x + xOffset, y + yOffset);
	    g.rotate(getRotation(), halfSize, halfSize);
	    if (bgImage != null) {
		try {
		    bgImage.drawInsideRectangle(g, rect);
		} catch (SymbolDrawingException e) {
		    Logger.getLogger(getClass())
		    .warn(Messages.getString("label_style_could_not_be_painted")
			    + ": " + bgImage.getSource(), e);
		}
	    } else {
		Logger.getLogger(getClass()).warn(
			Messages.getString("label_style_could_not_be_painted")
			+ ": " + bgImage.getSource());
	    }
	    g.rotate(-getRotation(), halfSize, halfSize);
	    g.translate(-(x + xOffset), -(y + yOffset));

	}

    }

    @Override
    public XMLEntity getXMLEntity() {
	XMLEntity xml = new XMLEntity();
	xml.putProperty("className", getClassName());
	xml.putProperty("isShapeVisible", isShapeVisible());
	xml.putProperty("desc", getDescription());
	xml.putProperty("file", file);
	xml.putProperty("size", getSize());
	xml.putProperty("offsetX", getOffset().getX());
	xml.putProperty("offsetY", getOffset().getY());
	xml.putProperty("rotation", getRotation());

	// measure unit
	xml.putProperty("unit", getUnit());

	// reference system
	xml.putProperty("referenceSystem", getReferenceSystem());

	return xml;
    }

    @Override
    public String getClassName() {
	return getClass().getName();
    }

    @Override
    public void setXMLEntity(XMLEntity xml) {
	setDescription(xml.getStringProperty("desc"));
	setIsShapeVisible(xml.getBooleanProperty("isShapeVisible"));
	
	setSize(xml.getDoubleProperty("size"));
	double offsetX = 0.0;
	double offsetY = 0.0;
	if (xml.contains("offsetX")) {
	    offsetX = xml.getDoubleProperty("offsetX");
	}
	if (xml.contains("offsetY")) {
	    offsetY = xml.getDoubleProperty("offsetY");
	}
	setOffset(new Point2D.Double(offsetX, offsetY));
	setReferenceSystem(xml.getIntProperty("referenceSystem"));
	setUnit(xml.getIntProperty("unit"));
	if (xml.contains("rotation")) {
	    setRotation(xml.getDoubleProperty("rotation"));
	}
    }

    public void print(Graphics2D g, AffineTransform at, FShape shape)
	    throws ReadDriverException {
	// TODO Implement it
	throw new Error("Not yet implemented!");

    }
}