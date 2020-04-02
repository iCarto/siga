package es.icarto.gvsig.extgex.forms.expropiations;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.DomainValues;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.icarto.gvsig.siga.models.InfoEmpresaGEX;
import es.icarto.gvsig.siga.models.InfoEmpresaGIA;

public class ExpropiationsLayerResolver {
    public enum Tramos {
        b("13", "AMPLIACION SANTIAGO NORTE-SANTIAGO SUR", FormExpropiations.TOCNAME_AMPLIACION),
        a("14", "AMPLIACION CANGAS-TEIS", FormExpropiations.TOCNAME_AMPLIACION),
        c("15", "AG-55 A CORUÑA-CARBALLO", FormExpropiations.TOCNAME_AUTOESTRADAS),
        d("16", "AG-57 VIGO-BAIONA", FormExpropiations.TOCNAME_AUTOESTRADAS);

        public final String tramoId;
        public final String tramo;
        public final String layerName;

        private Tramos(String tramoId, String tramo, String layerName) {
            this.tramoId = tramoId;
            this.tramo = tramo;
            this.layerName = layerName;
        }
    }

    public static String getTramoNameForIdFincaORIdTramo(String id) {
        /**
         * Dado un id finca o un id tramo, devuelve el nombre del tramo
         */
        String idTramo = id.substring(0, 2);
        for (Tramos t : Tramos.values()) {
            if (t.tramoId.equals(idTramo)) {
                return t.tramo;
            }
        }
        return null;
    }

    public static List<KeyValue> getTramosWithHardcodedOrder() {
        List<KeyValue> result = new ArrayList<KeyValue>();
        for (Tramos t : Tramos.values()) {
            result.add(new KeyValue(t.tramoId, t.tramo));
        }
        return result;
    }

    public static String getLayerNameBasedOnTramo(String tramoId) {
        for (Tramos t : Tramos.values()) {
            if (t.tramoId.equals(tramoId)) {
                return t.layerName;
            }
        }
        return FormExpropiations.TOCNAME;
    }

    public static boolean areLayersLoaded() {
        TOCLayerManager toc = new TOCLayerManager();
        if ((toc.getLayerByName(FormExpropiations.TOCNAME) != null)
                && (toc.getLayerByName(FormExpropiations.TOCNAME_AMPLIACION) != null)
                && (toc.getLayerByName(FormExpropiations.TOCNAME_AUTOESTRADAS) != null)) {
            return true;
        }
        return false;
    }

    public static String getBaseName(FLyrVect layer) {

        if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME_AMPLIACION)) {
            return FormExpropiations.TABLENAME + "_ampliacion";

        } else if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME_AUTOESTRADAS)) {
            return FormExpropiations.TABLENAME + "_autoestradas";
        } else if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME)) {
            return FormExpropiations.TABLENAME;
        }
        throw new RuntimeException("Should never happen");
    }

    public static boolean isExpropiationLayer(FLayer layer) {
        if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME_AMPLIACION)) {
            return true;

        } else if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME_AUTOESTRADAS)) {
            return true;
        } else if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME)) {
            return true;
        }
        return false;
    }

    public static InfoEmpresa getInfoEmpresa(FLayer layer) {
        if (ExpropiationsLayerResolver.isExpropiationLayer(layer)) {
            return new InfoEmpresaGEX();
        }
        return new InfoEmpresaGIA();
    }

    public static String getLayerNameBasedOnIdFinca(String fincaId) {
        String tramoId = fincaId.substring(0, 2);
        return getLayerNameBasedOnTramo(tramoId);
    }

    public static void removeNotAppropiateTramos(ORMLite ormLite, FLyrVect layer) {

        ArrayList<KeyValue> domainValuesForTramo = ormLite.getAppDomain()
                .getDomainValuesForComponent(DBNames.FIELD_TRAMO_FINCAS).getValues();
        List<KeyValue> tramos = getTramosWithHardcodedOrder();
        boolean addVoidValue = false;
        if (layer.getName().equals(FormExpropiations.TOCNAME)) {
            domainValuesForTramo.removeAll(tramos);
        } else {
            domainValuesForTramo.clear();
            addVoidValue = true;
            for (KeyValue t : tramos) {
                String ttt = getLayerNameBasedOnTramo(t.getKey());
                if (ttt.equals(layer.getName())) {
                    domainValuesForTramo.add(new KeyValue(t.getKey(), t.getValue()));
                }
            }
        }
        DomainValues foo = new DomainValues(domainValuesForTramo, addVoidValue);
        ormLite.getAppDomain().addDomainValues(DBNames.FIELD_TRAMO_FINCAS, foo);
    }
}
