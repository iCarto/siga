/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
 *
 * This file is part of ELLE
 *
 * ELLE is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * ELLE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with ELLE.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.SaveLegendsWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SaveAllLegendsExtension extends Extension {

    public void execute(String actionCommand) {

	int option = -1;

	if (actionCommand.equals("ACTIVES")) {
	    option = SaveLegendsWizard.ACTIVES;
	}
	if (actionCommand.equals("VISIBLES")) {
	    option = SaveLegendsWizard.VISIBLES;
	}
	if (actionCommand.equals("ALL")) {
	    option = SaveLegendsWizard.ALL;
	}

	WizardWindow w = new SaveLegendsWizard((View) PluginServices.getMDIManager().getActiveWindow(), option);
	w.open();

    }

    public void initialize() {
	registerIcons();
    }

    protected void registerIcons() {
	PluginServices.getIconTheme().registerDefault(
		"save-all-legends",
		this.getClass().getClassLoader().getResource("images/leyguardar.png")
		);
    }

    public boolean isEnabled() {
	if (DBSession.isActive()
		&& canUseELLE() 
		&& activeWindowIsIWindow()
		&& areLayersInTOC()) {
	    return true;
	}
	return false;
    }

    private boolean canUseELLE() {
	DBSession dbs = DBSession.getCurrentSession();
	try {
	    return dbs.getDBUser().canUseSchema(DBStructure.SCHEMA_NAME);
	} catch (SQLException e) {
	    return false;
	}
    }

    private boolean activeWindowIsIWindow() {
	return PluginServices.getMDIManager().getActiveWindow() instanceof View;
    }

    private boolean areLayersInTOC() {
	FLayers layers = ((View) PluginServices.getMDIManager()
		.getActiveWindow()).getMapControl().getMapContext().getLayers();
	return layers.getLayersCount() > 0;
    }

    public boolean isVisible() {
	return true;
    }

}
