package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.styles.ImageStyle;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.symbols.AbstractMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

// Based on PictureMarkerSymbol
public class SignalSymbol extends AbstractMarkerSymbol {

    // transient private Image img;
    private final ImageStyle bgImage;

    public SignalSymbol(SignalCache cache, String file) throws IOException {
	bgImage = cache.getImage(file);
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
	return new XMLEntity();
    }

    @Override
    public String getClassName() {
	return getClass().getName();
    }

    @Override
    public void setXMLEntity(XMLEntity xml) {
    }

    public void print(Graphics2D g, AffineTransform at, FShape shape)
	    throws ReadDriverException {
	// TODO Implement it
	throw new Error("Not yet implemented!");

    }
}