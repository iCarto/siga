package es.icarto.gvsig.extgia.consultas;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;

public class CaracteristicasQueries {

    public static String getPDFCaracteristicasQuery(String element, ConsultasFilters<Field> filters) {
        switch (Elements.valueOf(element)) {
        case Senhalizacion_Vertical:
            return "SELECT gid, " + ConsultasFieldNames.getPDFCaracteristicasFieldNames(element)
                    + CaracteristicasQueries.getFromClauseCaracteristicas(element)
                    + filters.getWhereClauseByLocationWidgets() + " ORDER BY el.id_elemento_senhalizacion";
        default:
            return "SELECT gid, " + ConsultasFieldNames.getPDFCaracteristicasFieldNames(element)
                    + CaracteristicasQueries.getFromClauseCaracteristicas(element)
                    + filters.getWhereClauseByLocationWidgets() + " ORDER BY gid";

        }
    }

    public static String getReconocimientosTrabajosQuery(String element, ConsultasFilters<Field> filters,
            String fields, String elementId, String tipoConsulta) {
        String query = "";
        if (element.equalsIgnoreCase("senhalizacion_vertical")) {
            query = "SELECT " + fields + getFromClauseReconocimientosTrabajos(element, elementId, tipoConsulta);
        } else {
            query = "SELECT " + fields + getFromClauseReconocimientosTrabajos(element, elementId, tipoConsulta);
        }

        if (!filters.getWhereClauseByLocationWidgets().isEmpty()) {
            query = query + " WHERE el." + elementId + " IN (SELECT " + elementId + " FROM " + DBFieldNames.GIA_SCHEMA
                    + "." + element + filters.getWhereClauseByLocationWidgets();
        }

        return query;
    }

    public static String getFromClauseReconocimientosTrabajos(String element, String elementId, String tipoConsulta) {
        switch (Elements.valueOf(element)) {
        case Senhalizacion_Vertical:
            return " FROM "
            + DBFieldNames.GIA_SCHEMA
            + "."
            + element
            + "_"
            + tipoConsulta
            + " AS sub JOIN "
            + DBFieldNames.GIA_SCHEMA
            + "."
            + element
            + " AS el ON sub."
            + elementId
            + "= el."
            + elementId
            + " LEFT OUTER JOIN audasa_extgia.senhalizacion_vertical_senhales se ON (el.id_elemento_senhalizacion = se.id_elemento_senhalizacion) "
            + CaracteristicasQueries.get(element);
        default:
            return " FROM " + DBFieldNames.GIA_SCHEMA + "." + element + "_" + tipoConsulta + " AS sub JOIN "
            + DBFieldNames.GIA_SCHEMA + "." + element + " AS el ON sub." + elementId + "= el." + elementId
            + CaracteristicasQueries.get(element);
        }

    }

    public static String getFromClauseCaracteristicas(String element) {
        switch (Elements.valueOf(element)) {
        case Senhalizacion_Vertical:
            return " FROM  audasa_extgia.senhalizacion_vertical_senhales se LEFT OUTER JOIN audasa_extgia.senhalizacion_vertical el ON (el.id_elemento_senhalizacion = se.id_elemento_senhalizacion) "
            + get(element);
        default:
            return " FROM audasa_extgia." + element + " el " + get(element);
        }
    }

    public static String get(String element) {
        switch (Elements.valueOf(element)) {
        case Senhalizacion_Vertical:
            return getLocalizationTablesWithSentido();
        case Areas_Descanso:
        case Areas_Servicio:
        case Areas_Mantenimiento:
        case Juntas:
        case Isletas:
        case Barrera_Rigida:
        case Estructuras:
        case Obras_Drenaje:
        case Areas_Peaje:
        case Lecho_Frenado:
        case Lineas_Distribucion_UFD:
        case Senhalizacion_Variable:
        case Transformadores:
        case Tuneles:
        case Ramales:
        case Dren_Caz:
        case Bajantes:
        case Cunetas:
        case Arquetas:
        case Pozos:
        case Centros_Mando:
        case Pretiles:
        case Barrera_Metalica:
            return getLocalizationTablesWithSentido();

        case Firme:
            return getFirmeLocalizationTables();

        case Valla_Cierre:
        case Taludes:
        case Muros:
        case Lineas_Suministro:
            return getLocalizationTablesWithSentidoAndPF();

        case Comunicaciones:
            return getJoinedArea() + getJoinedBase() + getJoinedTramo() + getJoinedMunicipio();
        case Enlaces:
        default:
            return getLocalizationTables();
        }

    }

    public static String getCSVCaracteristicasQuery(String element, ConsultasFilters<Field> filters) {

        String query = "";

        switch (Elements.valueOf(element)) {
        case Areas_Descanso:
            query = ConsultasFieldNames.areasDescansoCSVFieldNames();
            break;
        case Areas_Servicio:
            query = ConsultasFieldNames.areasServicioCSVFieldNames();
            break;
        case Areas_Mantenimiento:
            query = ConsultasFieldNames.areasMantenimientoCSVFieldNames();
            break;
        case Enlaces:
            query = ConsultasFieldNames.enlacesCSVFieldNames();
            break;
        case Senhalizacion_Vertical:
            return "SELECT " + ConsultasFieldNames.senhalizacionCSVFieldNames()
                    + getFromClauseCaracteristicas(element) + filters.getWhereClauseByLocationWidgets()
                    + " ORDER BY el.id_elemento_senhalizacion";
        case Firme:
            query = ConsultasFieldNames.firmeCSVFieldNames();
            break;
        case Valla_Cierre:
            query = ConsultasFieldNames.vallaCierreCSVFieldNames();
            break;
        case Taludes:
            query = ConsultasFieldNames.taludesCSVFieldNames();
            break;
        case Juntas:
            query = ConsultasFieldNames.juntasCSVFieldNames();
            break;
        case Isletas:
            query = ConsultasFieldNames.isletasCSVFieldNames();
            break;
        case Barrera_Rigida:
            query = ConsultasFieldNames.barreraRigidaCSVFieldNames() + query;
            break;
        case Estructuras:
            query = ConsultasFieldNames.estructurasCSVFieldNames() + query;
            break;
        case Obras_Drenaje:
            query = ConsultasFieldNames.obrasDrenajeCSVFieldNames();
            break;
        case Areas_Peaje:
            query = ConsultasFieldNames.areasPeajeCSVFieldNames();
            break;
        case Lecho_Frenado:
            query = ConsultasFieldNames.lechoFrenadoCSVFieldNames();
            break;
        case Muros:
            query = ConsultasFieldNames.murosCSVFieldNames();
            break;
        case Senhalizacion_Variable:
            query = ConsultasFieldNames.senhalizacionVariableCSVFieldNames() + query;
            break;
        case Lineas_Suministro:
            query = ConsultasFieldNames.lineasSuministroCSVFieldNames();
            break;
        case Transformadores:
            query = ConsultasFieldNames.transformadoresCSVFieldNames();
            break;
        case Pasos_Mediana:
            query = ConsultasFieldNames.pasosMedianaCSVFieldNames();
            break;
        case Ramales:
            query = ConsultasFieldNames.ramalesCSVFieldNames();
            break;
        case Comunicaciones:
            query = ConsultasFieldNames.comunicacionesCSVFieldNames();
            break;
        case Tuneles:
            query = ConsultasFieldNames.tunelesCSVFieldNames();
            break;
        case Dren_Caz:
            query = ConsultasFieldNames.drenCazCSVFieldNames();
            break;
        case Bajantes:
            query = ConsultasFieldNames.bajantesCSVFieldNames();
            break;
        case Cunetas:
            query = ConsultasFieldNames.cunetasCSVFieldNames();
            break;
        case Arquetas:
            query = ConsultasFieldNames.arquetasCSVFieldNames();
            break;
        case Pozos:
            query = ConsultasFieldNames.pozosCSVFieldNames();
            break;
        case Centros_Mando:
            query = ConsultasFieldNames.centrosMandoCSVFieldNames();
            break;
        case Pretiles:
            query = ConsultasFieldNames.pretilesCSVFieldNames();
            break;
        case Barrera_Metalica:
            query = ConsultasFieldNames.barreraMetalicaCSVFieldNames();
            break;
        case Lineas_Distribucion_UFD:
            query = ConsultasFieldNames.lineasDistribucionUFDCSVFieldNames();
            break;
        }
        return "SELECT " + query + getFromClauseCaracteristicas(element) + filters.getWhereClauseByLocationWidgets()
                + " ORDER BY gid";

    }

    private static String getLocalizationTables() {
        return getJoinedArea() + getJoinedBase() + getJoinedTramo() + getJoinedMunicipio() + getJoinedTipoVia()
                + getJoinedNombreVia();
    }

    private static String getLocalizationTablesWithSentido() {
        return getJoinedArea() + getJoinedBase() + getJoinedTramo() + getJoinedMunicipio() + getJoinedTipoVia()
                + getJoinedNombreVia() + getJoinedSentido();
    }

    private static String getLocalizationTablesWithSentidoAndPF() {
        return getJoinedArea()
                + getJoinedBase()
                + getJoinedTramo()
                + getJoinedMunicipio()
                + getJoinedTipoVia()
                + getJoinedNombreVia()
                + getJoinedSentido()
                + " LEFT OUTER JOIN audasa_extgia_dominios.tipo_via tvf ON (tvf.id = el.tipo_via_pf AND tvf.id_tramo = el.tramo AND tvf.id_bc = el.base_contratista) LEFT OUTER JOIN audasa_extgia_dominios.nombre_via nvf ON (cast (nvf.id as text) = el.nombre_via_pf AND nvf.id_tv = el.tipo_via AND nvf.id_bc = el.base_contratista AND nvf.id_tramo = el.tramo) ";
    }

    private static String getFirmeLocalizationTables() {
        return getJoinedArea() + getJoinedBase() + getJoinedTramo();
    }

    private static String getJoinedArea() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.area_mantenimiento am ON am.id = el.area_mantenimiento ";
    }

    private static String getJoinedBase() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.base_contratista bc ON bc.id = el.base_contratista ";
    }

    public static String getJoinedTramo() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.tramo tr ON (tr.id = el.tramo AND tr.id_bc = el.base_contratista) ";
    }

    private static String getJoinedMunicipio() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.municipio mu ON (mu.id = el.municipio AND mu.id_tramo = el.tramo AND mu.id_bc = el.base_contratista)";
    }

    public static String getJoinedTipoVia() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.tipo_via tv ON (tv.id = el.tipo_via AND tv.id_tramo = el.tramo AND tv.id_bc = el.base_contratista) ";
    }

    public static String getJoinedNombreVia() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.nombre_via nv ON (cast (nv.id as text) = el.nombre_via AND nv.id_tv = el.tipo_via AND nv.id_bc = el.base_contratista AND nv.id_tramo = el.tramo) ";
    }

    public static String getJoinedSentido() {
        return " LEFT OUTER JOIN audasa_extgia_dominios.sentido st ON el.sentido = st.id ";
    }

    public static String getCustomCaracteristicasQuery(ConsultasFilters<Field> filters, String element) {
        String query = CaracteristicasQueries.getCSVCaracteristicasQuery(element, filters);
        String subquery = query;
        if (filters.getFields().size() > 0) {
            subquery = query.substring(query.indexOf(" FROM"));
            subquery = buildFields(filters, "SELECT ", element) + subquery;
        }
        subquery = buildOrderBy(filters, subquery);
        return subquery;
    }

    public static String buildOrderBy(ConsultasFilters<Field> filters, String subquery) {
        if (filters.getOrderBy().size() > 0) {

            int indexOf = subquery.indexOf("ORDER BY ");
            if (indexOf != -1) {
                subquery = subquery.substring(0, indexOf + 9);
            } else {
                if (subquery.endsWith(";")) {
                    subquery = subquery.substring(0, subquery.length() - 1);
                }

                subquery = subquery + " ORDER BY ";
            }

            for (Field field : filters.getOrderBy()) {
                subquery = subquery + field.getKey() + ", ";
            }
            subquery = subquery.substring(0, subquery.length() - 2);
        }
        return subquery;
    }

    public static String buildFields(ConsultasFilters<Field> filters, String select, String element) {
        for (Field field : filters.getFields()) {
            if (field.getKey().endsWith("area_mantenimiento")) {
                select += "am.item AS  \"Área Mantenimiento\", ";
            } else if (field.getKey().endsWith("base_contratista")) {
                select += "bc.item AS  \"Base Contratista\", ";
            } else if (field.getKey().endsWith("tramo")) {
                select += "tr.item AS  \"Tramo\", ";
            } else if (field.getKey().endsWith("tipo_via")) {
                select += "tv.item AS  \"Tipo Vía\", ";
            } else if (field.getKey().endsWith("nombre_via")) {
                select += "nv.item AS  \"Nombre Vía\", ";
            } else if (field.getKey().endsWith("municipio")) {
                select += "mu.item AS  \"Municipio\", ";
                // workaround. Sentido is in trabajos table and not in the main
                // table
            } else if (field.getKey().endsWith("sentido") && !element.equalsIgnoreCase("firme")) {
                select += "st.item AS  \"Sentido\", ";
            } else {
                select = select + field.getKey() + String.format(" AS \"%s\"", field.getLongName().replace("\"", "'"))
                        + ", ";
            }
        }
        return select.substring(0, select.length() - 2);
    }

}
