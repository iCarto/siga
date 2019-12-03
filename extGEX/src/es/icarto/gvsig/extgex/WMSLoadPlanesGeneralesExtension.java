package es.icarto.gvsig.extgex;

import java.sql.SQLException;
import java.util.List;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.elle.db.DBStructure;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.wms.LoadWMS;
import es.icarto.gvsig.utils.DesktopApi;
import es.udc.cartolab.gvsig.elle.utils.ELLEMap;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class WMSLoadPlanesGeneralesExtension extends AbstractExtension {
	
	private static final String wmsName = "Planes_Generales_WMS";
	private static DBSession dbs = null;
	private static String idMunicipio;
	private static String idDocument;

	@Override
	public void execute(String actionCommand) {
	dbs = DBSession.getCurrentSession();
	getMunicipioCodeParameter();
	if (idMunicipio == null) {
		DesktopApi.showError("Este WMS no funciona con más de un municipio cargado");
	}else {
		getDocumentIDParameter();
		if (idDocument != null && !idDocument.isEmpty()) {
			LoadWMS loadWMS = new LoadWMS(wmsName, getServiceURLByParameters());
			loadWMS.Load();
		}else {
			DesktopApi.showError("No hay planos disponibles para este municipio");
		}
	}
	}

	@Override
	public boolean isEnabled() {
		return DBSession.isActive() && isViewActive();
	}

	@Override
	public void initialize() {
	// Overrided, because there is no toolbar icon for this extension
	}
	
	private String getServiceBaseURL() {
	String baseURL = null;
	try {
		String whereC = DBNames.FIELD_LAYER_WMS + "=" + "'" + wmsName + "'";
		String[][] wmsValues = dbs.getTable(DBNames.TABLE_WMS, DBStructure.getSchema(), whereC);
		baseURL = wmsValues[0][5];
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return baseURL;
	}
	
	private void getMunicipioCodeParameter() {
	List<String> constants = ELLEMap.getConstantValuesSelected();
	if (constants.size() == 1) {
		idMunicipio = constants.get(0);
	}
	}
	
	private void getDocumentIDParameter() {
	String whereC = "id_municipio = " + "'" + idMunicipio + "'";
	try {
		String[][] values = dbs.getTable(DBNames.TABLE_WMS_PLANES, DBStructure.getSchema(), whereC);
		if (values.length > 0) {
		idDocument = values[0][1];
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	}
	
	private String getServiceURLByParameters() {
	String serviceURL = null;
	String baseURL = getServiceBaseURL();
	String municipioParameterURL = baseURL.replaceFirst("%", idMunicipio);
	serviceURL = municipioParameterURL.replaceFirst("%", idDocument);
	return serviceURL;
	}
}
