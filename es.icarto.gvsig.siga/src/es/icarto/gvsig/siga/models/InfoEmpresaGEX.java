package es.icarto.gvsig.siga.models;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class InfoEmpresaGEX extends InfoEmpresa {

    @Override
    public void infoFromDB() {
        ConnectionWrapper con = new ConnectionWrapper(DBSession.getCurrentSession().getJavaConnection());
        String query = "SELECT distinct(tr.id_tramo), tr.nombre_tramo, ie.report_logo, ie.title, ie.subtitle, ie.id, ie.report_name "
                + "FROM audasa_expropiaciones.tramos tr "
                + "LEFT OUTER JOIN audasa_aplicaciones.info_empresa as ie ON tr.empresa = ie.id";
        result = con.execute(query);
    }

    @Override
    public int getFieldTramoIndex() {
        return 0;
    }

}
