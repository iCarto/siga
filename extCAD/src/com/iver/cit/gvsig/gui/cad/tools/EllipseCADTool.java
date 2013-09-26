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
package com.iver.cit.gvsig.gui.cad.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
import com.iver.cit.gvsig.gui.cad.exception.CommandException;
import com.iver.cit.gvsig.gui.cad.tools.smc.EllipseCADToolContext;
import com.iver.cit.gvsig.gui.cad.tools.smc.EllipseCADToolContext.EllipseCADToolState;

/**
 * @author Vicente Caballero Navarro
 */
public class EllipseCADTool extends InsertionCADTool {
    protected EllipseCADToolContext _fsm;
    protected Point2D startAxis;
    protected Point2D endAxis;

    public EllipseCADTool() {

    }

    @Override
    public void init() {
	_fsm = new EllipseCADToolContext(this);
    }

    @Override
    public void transition(double x, double y, InputEvent event) {
	_fsm.addPoint(x, y, event);
    }

    @Override
    public void transition(double d) {
	_fsm.addValue(d);
    }

    @Override
    public void transition(String s) throws CommandException {
	if (!super.changeCommand(s)) {
	    _fsm.addOption(s);
	}
    }

    @Override
    public void addPoint(double x, double y, InputEvent event) {
	EllipseCADToolState actualState = (EllipseCADToolState) _fsm
		.getPreviousState();
	String status = actualState.getName();

	if (status.equals("Ellipse.FirstPointAxis")) {
	    startAxis = new Point2D.Double(x, y);
	} else if (status.equals("Ellipse.SecondPointAxis")) {
	    endAxis = new Point2D.Double(x, y);
	} else if (status.equals("Ellipse.DistanceOtherAxis")) {
	    Point2D middle = new Point2D.Double(
		    (startAxis.getX() + endAxis.getX()) / 2,
		    (startAxis.getY() + endAxis.getY()) / 2);
	    Point2D third = new Point2D.Double(x, y);
	    double distance = middle.distance(third);
	    IGeometry ellipse = ShapeFactory.createEllipse(startAxis, endAxis,
		    distance);
	    addGeometry(flattenGeometry(ellipse));
	}
    }

    @Override
    public void drawOperation(Graphics g, double x, double y) {
	EllipseCADToolState actualState = _fsm.getState();
	String status = actualState.getName();

	if (status.equals("Ellipse.SecondPointAxis")) {
	    drawLine((Graphics2D) g, startAxis, new Point2D.Double(x, y),
		    DefaultCADTool.geometrySelectSymbol);
	} else if (status.equals("Ellipse.DistanceOtherAxis")) {
	    Point2D middle = new Point2D.Double(
		    (startAxis.getX() + endAxis.getX()) / 2,
		    (startAxis.getY() + endAxis.getY()) / 2);

	    Point2D third = new Point2D.Double(x, y);

	    double distance = middle.distance(third);
	    IGeometry geom = flattenGeometry(ShapeFactory.createEllipse(
		    startAxis, endAxis, distance));
	    geom.draw((Graphics2D) g, getCadToolAdapter().getMapControl()
		    .getViewPort(), DefaultCADTool.axisReferencesSymbol);

	    Point2D mediop = new Point2D.Double(
		    (startAxis.getX() + endAxis.getX()) / 2,
		    (startAxis.getY() + endAxis.getY()) / 2);
	    drawLine((Graphics2D) g, mediop, third,
		    DefaultCADTool.geometrySelectSymbol);
	}
    }

    @Override
    public void addOption(String s) {
	// TODO Auto-generated method stub
    }

    @Override
    public void addValue(double d) {
	EllipseCADToolState actualState = (EllipseCADToolState) _fsm
		.getPreviousState();
	String status = actualState.getName();

	if (status.equals("Ellipse.DistanceOtherAxis")) {
	    double distance = d;
	    IGeometry geom = flattenGeometry(ShapeFactory.createEllipse(
		    startAxis, endAxis, distance));
	    addGeometry(geom);
	}
    }

    @Override
    public String getName() {
	return PluginServices.getText(this, "ellipse_");
    }

    @Override
    public String toString() {
	return "_ellipse";
    }

    @Override
    public boolean isApplicable(int shapeType) {
	switch (shapeType) {
	case FShape.POINT:
	case FShape.MULTIPOINT:
	    return false;
	}
	return true;
    }

    @Override
    public void drawOperation(Graphics g, ArrayList pointList) {
    }

    @Override
    public boolean isMultiTransition() {
	return false;
    }

    @Override
    public void transition(InputEvent event) {
    }
}
