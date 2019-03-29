package es.icarto.gvsig.extgex.slopes_and_curves;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class EjeCalibrado {

    private final Map<String, Float[]> minMaxTramoCache = new HashMap<String, Float[]>();

    public Float[] getMinMaxPk(String tramo, int sentido) {
        String cacheKey = tramo + sentido;
        Float[] minMaxPk = minMaxTramoCache.get(cacheKey);
        if (minMaxPk != null) {
            return minMaxPk;
        }

        String table = sentido == 1 ? "audasa_aplicaciones.eje_calibrado_cre"
                : "audasa_aplicaciones.eje_calibrado_dec";

        String query = "SELECT tramo, ST_M(st_startpoint(st_geometryn(the_geom, 1))) as start, ST_M(st_endpoint(st_geometryn(the_geom, 1))) as end from %s WHERE tramo = '%s'";
        query = String.format(query, table, tramo);

        ConnectionWrapper c = new ConnectionWrapper(DBSession.getCurrentSession().getJavaConnection());
        DefaultTableModel execute = c.execute(query);
        Float start = new Float(execute.getValueAt(0, 1).toString());
        Float end = new Float(execute.getValueAt(0, 2).toString());

        if (start.compareTo(end) < 0) {
            minMaxPk = new Float[] { start, end };
        } else {
            minMaxPk = new Float[] { end, start };
        }
        minMaxTramoCache.put(cacheKey, minMaxPk);
        return minMaxPk;
    }
}
