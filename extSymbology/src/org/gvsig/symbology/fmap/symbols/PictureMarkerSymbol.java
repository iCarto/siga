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
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
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

/* CVS MESSAGES:
 *
 * $Id: PictureMarkerSymbol.java 15593 2007-10-29 13:01:13Z jdominguez $
 * $Log$
 * Revision 1.17  2007-09-21 12:25:32  jaume
 * cancellation support extended down to the IGeometry and ISymbol level
 *
 * Revision 1.16  2007/09/19 16:22:04  jaume
 * removed unnecessary imports
 *
 * Revision 1.15  2007/09/11 07:46:55  jaume
 * *** empty log message ***
 *
 * Revision 1.14  2007/08/16 06:55:19  jvidal
 * javadoc updated
 *
 * Revision 1.13  2007/08/09 06:42:24  jvidal
 * javadoc
 *
 * Revision 1.12  2007/08/08 12:05:17  jvidal
 * javadoc
 *
 * Revision 1.11  2007/07/18 06:54:35  jaume
 * continuing with cartographic support
 *
 * Revision 1.10  2007/07/03 10:58:29  jaume
 * first refactor on CartographicSupport
 *
 * Revision 1.9  2007/06/29 13:07:01  jaume
 * +PictureLineSymbol
 *
 * Revision 1.8  2007/06/11 12:25:48  jaume
 * ISymbol drawing integration tests (markers and lines)
 *
 * Revision 1.7  2007/06/07 06:50:40  jaume
 * *** empty log message ***
 *
 * Revision 1.6  2007/05/29 15:46:37  jaume
 * *** empty log message ***
 *
 * Revision 1.5  2007/05/08 08:47:40  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2007/03/21 17:36:22  jaume
 * *** empty log message ***
 *
 * Revision 1.3  2007/03/09 11:20:57  jaume
 * Advanced symbology (start committing)
 *
 * Revision 1.1.2.4  2007/02/21 07:34:09  jaume
 * labeling starts working
 *
 * Revision 1.1.2.3  2007/02/16 10:54:12  jaume
 * multilayer splitted to multilayerline, multilayermarker,and  multilayerfill
 *
 * Revision 1.1.2.2  2007/02/15 16:23:44  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.1  2007/02/09 07:47:05  jaume
 * Isymbol moved
 *
 * Revision 1.1  2007/01/24 17:58:22  jaume
 * new features and architecture error fixes
 *
 *
 */
package org.gvsig.symbology.fmap.symbols;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.styles.BackgroundFileStyle;
import org.gvsig.tools.file.PathGenerator;

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

/**
 * PictureMarkerSymbol allows to use an image file as a definition to be painted
 * instead of a marker symbol.
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class PictureMarkerSymbol extends AbstractMarkerSymbol {
    private static final float SELECTION_OPACITY_FACTOR = .3F;
    // transient private Image img;
    private String imagePath;
    private boolean selected;
    private PictureMarkerSymbol selectionSym;
    // transient private Image selImg;
    private String selImagePath;

    private BackgroundFileStyle bgImage;
    private BackgroundFileStyle bgSelImage;
    private final PathGenerator pathGenerator = PathGenerator.getInstance();

    /**
     * Constructor method
     */
    public PictureMarkerSymbol() {
	super();
    }

    /**
     * Constructor method
     * 
     * @param imageURL
     *            , URL of the normal image
     * @param selImageURL
     *            , URL of the image when it is selected in the map
     * @throws IOException
     */
    public PictureMarkerSymbol(URL imageURL, URL selImageURL)
	    throws IOException {
	setImage(imageURL);
	if (selImageURL != null) {
	    setSelImage(selImageURL);
	} else {
	    setSelImage(imageURL);
	}
	setSize(bgImage.getBounds().getHeight());
	setUnit(-1);

    }

    /**
     * Sets the file for the image to be used as a marker symbol
     * 
     * @param imageFile
     *            , File
     * @throws IOException
     */
    public void setImage(URL imageUrl) throws IOException {

	bgImage = BackgroundFileStyle.createStyleByURL(imageUrl);
	imagePath = imageUrl.toString();
    }

    /**
     * Sets the file for the image to be used as a marker symbol (when it is
     * selected in the map)
     * 
     * @param imageFile
     *            , File
     * @throws IOException
     */
    public void setSelImage(URL imageFileUrl) throws IOException {
	selImagePath = imageFileUrl.toString();
	if (selImagePath.equals(imagePath)) {
	    bgSelImage = bgImage;
	} else {
	    bgSelImage = BackgroundFileStyle.createStyleByURL(imageFileUrl);
	}
    }

    @Override
    public ISymbol getSymbolForSelection() {
	if (selectionSym == null) {
	    selectionSym = (PictureMarkerSymbol) SymbologyFactory
		    .createSymbolFromXML(getXMLEntity(), getDescription());
	    selectionSym.selected = true;
	    selectionSym.selectionSym = selectionSym; // avoid too much lazy
						      // creations
	}
	return selectionSym;
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
	    BackgroundFileStyle bg = (!selected) ? bgImage : bgSelImage;
	    Rectangle rect = new Rectangle(size, size);
	    g.translate(x + xOffset, y + yOffset);
	    g.rotate(getRotation(), halfSize, halfSize);
	    if (bg != null) {
		try {
		    bg.drawInsideRectangle(g, rect);
		} catch (SymbolDrawingException e) {
		    Logger.getLogger(getClass())
			    .warn(Messages.getString("label_style_could_not_be_painted")
				    + ": " + imagePath, e);
		}
	    } else {
		Logger.getLogger(getClass()).warn(
			Messages.getString("label_style_could_not_be_painted")
				+ ": " + imagePath);
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
	xml.putProperty("imagePath", pathGenerator.getURLPath(imagePath));
	xml.putProperty("selImagePath", pathGenerator.getURLPath(selImagePath));
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
	imagePath = pathGenerator.getAbsoluteURLPath(xml
		.getStringProperty("imagePath"));
	selImagePath = pathGenerator.getAbsoluteURLPath(xml
		.getStringProperty("selImagePath"));
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
	File rootDir = new File(SymbologyFactory.SymbolLibraryPath);
	try {
	    try {
		setImage(new URL(imagePath));
	    } catch (MalformedURLException e) {
		try {
		    setImage(new URL(
			    pathGenerator.getAbsoluteURLPath(imagePath)));
		} catch (MalformedURLException e1) {
		    setImage(new URL("file://" + rootDir.getAbsolutePath()
			    + File.separator + imagePath));
		}
	    }
	} catch (MalformedURLException e) {
	    Logger.getLogger(getClass()).error(
		    Messages.getString("invalid_url") + ": " + imagePath);
	} catch (IOException e) {
	    Logger.getLogger(getClass()).error(
		    Messages.getString("invalid_url") + ": " + imagePath);
	}
	try {
	    try {
		setSelImage(new URL(selImagePath));
	    } catch (MalformedURLException e) {
		try {
		    setSelImage(new URL(
			    pathGenerator.getAbsoluteURLPath(selImagePath)));
		} catch (MalformedURLException e1) {
		    setSelImage(new URL("file://" + rootDir.getAbsolutePath()
			    + File.separator + selImagePath));
		}
	    }
	} catch (MalformedURLException e) {
	    Logger.getLogger(getClass()).error(
		    Messages.getString("invalid_url") + ": " + selImagePath);
	} catch (IOException e) {
	    Logger.getLogger(getClass()).error(
		    Messages.getString("invalid_url") + ": " + selImagePath);
	}

    }

    public void print(Graphics2D g, AffineTransform at, FShape shape)
	    throws ReadDriverException {
	// TODO Implement it
	throw new Error("Not yet implemented!");

    }

    /**
     * Returns the path of the image that is used as a marker symbol
     * 
     * @return imagePath,String
     */
    public String getImagePath() {
	return imagePath;
    }

    /**
     * Returns the path of the image that is used as a marker symbol (when it is
     * selected in the map)
     * 
     * @return selimagePath,String
     */
    public String getSelImagePath() {
	return selImagePath;
    }

}