/*
 * Created on 22-nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.rendering;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.renderer.style.GraphicStyle2D;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.TextStyle2D;


/**
 * A simple class that knows how to paint a Shape object onto a Graphic given a
 * Style2D. It's the last step of the rendering engine, and has been factored
 * out since both renderers do use the same painting logic.
 *
 * @author Andrea Aime
 */
public class FStyledShapePainter {
    private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    /** Observer for image loading */
    private static Canvas imgObserver = new Canvas();

    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(FStyledShapePainter.class
            .getName());

    /**
     * Invoked automatically when a polyline is about to be draw. This
     * implementation paints the polyline according to the rendered style
     *
     * @param graphics The graphics in which to draw.
     * @param shape The polyline to draw.
     * @param style The style to apply, or <code>null</code> if none.
     * @param scale The scale denominator for the current zoom level
     */
    public void paint(final Graphics2D graphics, final Shape shape,
        final Style2D style, final double scale) {
        if (style == null) {
            // TODO: what's going on? Should not be reached...
            LOGGER.severe("ShapePainter has been asked to paint a null style!!");

            return;
        }

        // Is the current scale within the style scale range? 
        if (!style.isScaleInRange(scale)) {
            LOGGER.fine("Out of scale");

            return;
        } 
        
//        if(LOGGER.isLoggable(Level.FINE)) {
//            LOGGER.fine("Graphics transform: " + graphics.getTransform());
//        }

        if (style instanceof MarkStyle2D) {
            // get the point onto the shape has to be painted
        	// SI ES UN RECTANGULO, ES MEJOR USAR g.fillRect, es mucho m�s
        	// r�pido. Para el resto, va m�s lento.
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords); 

            MarkStyle2D ms2d = (MarkStyle2D) style;
            Shape transformedShape = ms2d.getTransformedShape(coords[0],
                    coords[1]);  
            // Shape transformedShape = shape;

            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    // graphics.setComposite(ms2d.getFillComposite());
                    if (transformedShape instanceof Rectangle2D)
                    {
                    	Rectangle2D rAux = (Rectangle2D) transformedShape;
                    	graphics.fillRect((int) rAux.getX(), (int) rAux.getY(),
                    			(int) rAux.getWidth(), (int) rAux.getHeight());
                    }
                    else
                    {
                    	graphics.fill(transformedShape);
                    }
                    // graphics.fillRect((int) coords[0], (int) coords[1], 10,10);
                }

                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    // graphics.setComposite(ms2d.getContourComposite());
                    graphics.draw(transformedShape);
                }
            }
        } else if (style instanceof GraphicStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            GraphicStyle2D gs2d = (GraphicStyle2D) style;

            renderImage(graphics, coords[0], coords[1],
                (Image) gs2d.getImage(), gs2d.getRotation(), gs2d.getOpacity());
        } else if (style instanceof TextStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            AffineTransform old = graphics.getTransform();
            AffineTransform temp = new AffineTransform(old);
            TextStyle2D ts2d = (TextStyle2D) style;
            GlyphVector textGv = ts2d.getTextGlyphVector(graphics);
            Rectangle2D bounds = textGv.getVisualBounds();

            temp.translate(coords[0], coords[1]);

            double x = 0;
            double y = 0;

            if (ts2d.isAbsoluteLineDisplacement()) {
                double offset = ts2d.getDisplacementY();

                if (offset > 0.0) { // to the left of the line
                    y = -offset;
                } else if (offset < 0) {
                    y = -offset + bounds.getHeight();
                } else {
                    y = bounds.getHeight() / 2;
                }

                x = -bounds.getWidth() / 2;
            } else {
                x = (ts2d.getAnchorX() * (-bounds.getWidth()))
                    + ts2d.getDisplacementX();
                y = (ts2d.getAnchorY() * (bounds.getHeight()))
                    + ts2d.getDisplacementY();
            }

            temp.rotate(ts2d.getRotation());
            temp.translate(x, y);

            try {
                graphics.setTransform(temp);
                
                if (ts2d.getHaloFill() != null) {
                    // float radious = ts2d.getHaloRadius();

                    // graphics.translate(radious, -radious);
                    graphics.setPaint(ts2d.getHaloFill());
                    graphics.setComposite(ts2d.getHaloComposite());
                    graphics.fill(ts2d.getHaloShape(graphics));

                    // graphics.translate(radious, radious);
                }

                if (ts2d.getFill() != null) {
                    graphics.setPaint(ts2d.getFill());
                   /// TODO graphics.setComposite(ts2d.getComposite());
                    graphics.drawGlyphVector(textGv, 0, 0);
                }
            } finally {
                graphics.setTransform(old);
            }
        } else {
            // if the style is a polygon one, process it even if the polyline is not
            // closed (by SLD specification)
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D ps2d = (PolygonStyle2D) style;

                if (ps2d.getFill() != null) {
                    Paint paint = ps2d.getFill();

                    if (paint instanceof TexturePaint) {
                        TexturePaint tp = (TexturePaint) paint;
                        BufferedImage image = tp.getImage();
                        Rectangle2D rect = tp.getAnchorRect();
                        AffineTransform at = graphics.getTransform();
                        double width = rect.getWidth() * at.getScaleX();
                        double height = rect.getHeight() * at.getScaleY();
                        Rectangle2D scaledRect = new Rectangle2D.Double(0, 0,
                                width, height);
                        paint = new TexturePaint(image, scaledRect);
                    }

                    graphics.setPaint(paint);
                    if (ps2d.getContourComposite() != null)
                    	graphics.setComposite(ps2d.getFillComposite());
                    graphics.fill(shape);
                }
            }

            if (style instanceof LineStyle2D) {
                LineStyle2D ls2d = (LineStyle2D) style;
                if (ls2d.getStroke() != null) {
                    // see if a graphic stroke is to be used, the drawing method is completely
                    // different in this case
                    if (ls2d.getGraphicStroke() != null) {
                        drawWithGraphicsStroke(graphics, shape,
                            ls2d.getGraphicStroke());
                    } else {
                        Paint paint = ls2d.getContour();

                        if (paint instanceof TexturePaint) {
                            TexturePaint tp = (TexturePaint) paint;
                            BufferedImage image = tp.getImage();
                            Rectangle2D rect = tp.getAnchorRect();
                            AffineTransform at = graphics.getTransform();
                            double width = rect.getWidth() * at.getScaleX();
                            double height = rect.getHeight() * at.getScaleY();
                            Rectangle2D scaledRect = new Rectangle2D.Double(0,
                                    0, width, height);
                            paint = new TexturePaint(image, scaledRect);
                        }

                        graphics.setPaint(paint);
                        graphics.setStroke(ls2d.getStroke());
                        if (ls2d.getContourComposite() != null)
                        	graphics.setComposite(ls2d.getContourComposite());
                        graphics.draw(shape);
                    }
                }
            }
        }
    }

    // draws the image along the path
    private void drawWithGraphicsStroke(Graphics2D graphics, Shape shape,
        BufferedImage image) {
        PathIterator pi = shape.getPathIterator(null, 10.0);
        double[] coords = new double[2];
        int type;

        // I suppose the image has been already scaled and its square
        int imageSize = image.getWidth();

        double[] first = new double[2];
        double[] previous = new double[2];
        type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("starting at " + first[0] + "," + first[1]);
        }

        pi.next();

        while (!pi.isDone()) {
            type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:

                // nothing to do?
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("moving to " + coords[0] + "," + coords[1]);
                }

                break;

            case PathIterator.SEG_CLOSE:

                // draw back to first from previous
                coords[0] = first[0];
                coords[1] = first[1];

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("closing from " + previous[0] + ","
                        + previous[1] + " to " + coords[0] + "," + coords[1]);
                }

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("drawing from " + previous[0] + ","
                        + previous[1] + " to " + coords[0] + "," + coords[1]);
                }

                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                double len = Math.sqrt((dx * dx) + (dy * dy)); // - imageWidth;

                double theta = Math.atan2(dx, dy);
                dx = (Math.sin(theta) * imageSize);
                dy = (Math.cos(theta) * imageSize);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("dx = " + dx + " dy " + dy + " step = "
                        + Math.sqrt((dx * dx) + (dy * dy)));
                }

                double rotation = -(theta - (Math.PI / 2d));
                double x = previous[0] + (dx / 2.0);
                double y = previous[1] + (dy / 2.0);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("len =" + len + " imageSize " + imageSize);
                }

                double dist = 0;

                for (dist = 0; dist < (len - imageSize); dist += imageSize) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(graphics, x, y, image, rotation, 1);

                    x += dx;
                    y += dy;
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("loop end dist " + dist + " len " + len + " "
                        + (len - dist));
                }

                double remainder = len - dist;
                int remainingWidth = (int) remainder;

                if (remainingWidth > 0) {
                    //clip and render image
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("about to use clipped image " + remainder);
                    }

                    BufferedImage img = new BufferedImage(remainingWidth,
                            imageSize, image.getType());
                    Graphics2D ig = img.createGraphics();
                    ig.drawImage(image, 0, 0, imgObserver);

                    renderImage(graphics, x, y, img, rotation, 1);
                }

                break;

            default:
                LOGGER.warning(
                    "default branch reached in drawWithGraphicStroke");
            }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }

    /**
     * Renders an image on the device
     *
     * @param graphics the image location on the screen, x coordinate
     * @param x the image location on the screen, y coordinate
     * @param y the image
     * @param image DOCUMENT ME!
     * @param rotation the image rotatation
     * @param opacity DOCUMENT ME!
     */
    private void renderImage(Graphics2D graphics, double x, double y,
        Image image, double rotation, float opacity) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("drawing Image @" + x + "," + y);
        }

        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new java.awt.geom.Point2D.Double(x, y);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation);
        graphics.setTransform(markAT);
        graphics.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

        // we moved the origin to the centre of the image.
        graphics.drawImage(image, -image.getWidth(imgObserver) / 2,
            -image.getHeight(imgObserver) / 2, imgObserver);

        graphics.setTransform(temp);

        return;
    }
}
