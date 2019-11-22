package es.icarto.gvsig.siga.models;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class InfoEmpresaGIA extends InfoEmpresa {
	
	@Override
	public void infoFromDB() {
		ConnectionWrapper con = new ConnectionWrapper(DBSession
			.getCurrentSession().getJavaConnection());
		String query = "SELECT distinct(tr.id), tr.item, ie.report_logo, ie.title, ie.subtitle, ie.id "
				+ "FROM audasa_extgia_dominios.tramo tr "
				+ "LEFT OUTER JOIN audasa_aplicaciones.info_empresa as ie ON tr.empresa = ie.id";
		result = con.execute(query);
	}

	@Override
	public int getFieldTramoIndex() {
		return 1;
	}
	


}
