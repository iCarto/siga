/*
 * 
 * Copyright 2009 Deputaci�n Provincial de Pontevedra
 * Copyright 2010 CartoLab, Universidad de A Coru�a
 *
 * This file is part of openCADTools, developed by the Cartography
 * Engineering Laboratory of the University of A Coru�a (CartoLab).
 * http://www.cartolab.es
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
 */

package com.iver.cit.gvsig;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.gui.cad.tools.InsertVertexCADTool;
import com.iver.cit.gvsig.project.documents.view.gui.View;

/**
 * Extension to insert a new vertex on a geometry of a layer in edition. Layer
 * must have line or poligon geometry.
 * 
 * @author Nacho Uve [Cartolab]
 */
public class InsertVertexExtension extends BaseCADExtension {

    private static final String CAD_TOOL_KEY = "_insertVertex";
    private static final String ICON_KEY = "edition-geometry-insert-vertex";
    private static final String ICON_PATH = "images/icons/anhadir_vertice.png";

    @Override
    public void initialize() {
	tool = new InsertVertexCADTool();
	CADExtension.addCADTool(CAD_TOOL_KEY, tool);
	registerIcon(ICON_KEY, ICON_PATH);
    }

    /**
     * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
     */
    @Override
    public void execute(String s) {
	CADExtension.initFocus();

	if (s.equals(CAD_TOOL_KEY)) {
	    CADExtension.setCADTool(CAD_TOOL_KEY, true);
	    View view = (View) PluginServices.getMDIManager().getActiveWindow();
	    MapControl mapControl = view.getMapControl();
	    CADExtension.getEditionManager().setMapControl(mapControl);
	}
	CADExtension.getCADToolAdapter().configureMenu();
    }

}
