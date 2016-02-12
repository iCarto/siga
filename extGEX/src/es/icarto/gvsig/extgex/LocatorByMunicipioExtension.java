package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.locators.LocatorByMunicipio;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LocatorByMunicipioExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	LocatorByMunicipio municipioLocator = new LocatorByMunicipio();
	municipioLocator.openDialog();
    }

    @Override
    public boolean isEnabled() {
	if ((DBSession.getCurrentSession() != null) && (getView() != null)
		&& areLayersLoaded()) {
	    return true;
	}
	return false;
    }

    private boolean areLayersLoaded() {
	TOCLayerManager toc = new TOCLayerManager();
	if ((toc.getLayerByName(DBNames.LAYER_MUNICIPIOS) != null)
		&& (toc.getLayerByName(DBNames.LAYER_PARROQUIAS) != null)) {
	    return true;
	}
	return false;
    }

}
