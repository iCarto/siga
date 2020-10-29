package es.icarto.gvsig.extgia.consultas.agregados;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.CaracteristicasQueries;
import es.icarto.gvsig.extgia.consultas.ConsultasFieldNames;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.preferences.Elements;

public class TrabajosAgregadosReportQueries {

    private final String element;
    private final ConsultasFilters<Field> filters;
    private final String elementId;
    private final String partialQuery;

    private enum Vegetacion {
	TALUDES("taludes", Elements.Taludes.pk), ISLETAS("isletas", Elements.Isletas.pk), BARRERA_RIGIDA(
		"barrera_rigida", Elements.Barrera_Rigida.pk), VALLA_CIERRE("valla_cierre",  Elements.Valla_Cierre.pk);

	private final String table;
	private final String id;

	private Vegetacion(String table, String id) {
	   
	    this.table = table;
	    this.id = id;
	}

	public String table() {
	    return table;
	}

	public String id() {
	    return id;
	}

    }

    public TrabajosAgregadosReportQueries(String element,
	    ConsultasFilters<Field> filters) {
	this.element = element;
	this.filters = filters;
	this.elementId = ConsultasFieldNames.getElementId(element);
	partialQuery = "COALESCE(sum(medicion),0) from audasa_extgia.%s_trabajos sub JOIN audasa_extgia.%s el ON sub.%s = el.%s WHERE %s "
		+ filters.getWhereClauseFiltersForAgregados();
    }

    private String getBaseQuery(String unidad) {

	String query = "SELECT el.%s, tr.item, tv.item, nv.item, pk_inicial, pk_final, st.item, "
		+ "medicion "
		+ "FROM audasa_extgia.%s_trabajos sub "
		+ "LEFT OUTER JOIN  audasa_extgia.%s el ON sub.%s = el.%s "
		+ CaracteristicasQueries.getJoinedTramo()
		+ CaracteristicasQueries.getJoinedTipoVia() 
		+ CaracteristicasQueries.getJoinedSentido()
		+ CaracteristicasQueries.getJoinedNombreVia()
		+ "WHERE unidad = '%s' " + filters.getWhereClauseFiltersForAgregados() + " ORDER BY 2, 5, 1";

	if (element.equalsIgnoreCase("vegetacion")) {
	    String elementQuery = "";
	    for (Vegetacion eVeg : Vegetacion.values()) {
	        if (eVeg.name().compareToIgnoreCase("VALLA_CIERRE") == 0) {
	        /* VALLA_CIERRE is the only element of vegetation that
	         * it has a SERIAL and not a VARCHAR as PK */
	        query = query.replaceFirst("el.%s", "CAST(el.gid AS Varchar)");
	        elementQuery += String.format(query, eVeg.table, eVeg.table, eVeg.id, eVeg.id, unidad).replace(" ORDER BY 2, 5, 1", " UNION ALL ");
	        }else {
	        elementQuery += String.format(query, eVeg.id, eVeg.table, eVeg.table, eVeg.id, eVeg.id, unidad).replace(" ORDER BY 2, 5, 1", " UNION ALL ");
	        }
	    }
	    return elementQuery.replaceFirst(" UNION ALL $", " ORDER BY 2, 5, 1");
	}
	return String.format(query, elementId, element, element, elementId, elementId, unidad);
    }

    private String getBaseSumQuery(String where) {
	return getBaseSumQuery("TOTAL", where);
    }

    private String getBaseSumQuery(String firstField, String where) {
	String query = String.format("SELECT '%s', '', null, null, null, null, null, ", firstField);
	String elementQuery = "";

	if (element.equalsIgnoreCase("vegetacion")) {
	    for (Vegetacion eVeg : Vegetacion.values()) {
		elementQuery += String.format("(SELECT  " + partialQuery + ") + ", eVeg.table, eVeg.table, eVeg.id, eVeg.id, where);
	    }
	    elementQuery = elementQuery.substring(0, elementQuery.length() - 2);
	} else {
	    elementQuery = String.format(partialQuery, element, element,
		    elementId, elementId, where);
	}

	return query + elementQuery;
    }

    private String getBaseSumQuery2(String where) {
	if (element.equalsIgnoreCase("vegetacion")) {

	    Vegetacion a = Vegetacion.TALUDES;
	    String queryTalud = String.format("(SELECT  " + partialQuery
		    + ") + ", a.table, a.table, a.id, a.id, where);
	    a = Vegetacion.ISLETAS;
	    String queryIsletas = String.format("(SELECT  " + partialQuery
		    + ") + ", a.table, a.table, a.id, a.id, where);
	    a = Vegetacion.BARRERA_RIGIDA;
	    String queryBR = String.format("(SELECT  " + partialQuery
	        + ") + ", a.table, a.table, a.id, a.id, where);
	    a = Vegetacion.VALLA_CIERRE;
        String queryVC = String.format("(SELECT  " + partialQuery + ")",
            a.table, a.table, a.id, a.id, where);

	    String query = String.format("SELECT " + queryTalud + queryIsletas
		    + queryBR + queryVC);
	    return query;
	}

	String queryElement = String.format(partialQuery, element, element,
		elementId, elementId, where);
	return String.format("SELECT " + queryElement);
    }

    public String getDesbroceRetroaranhaQuery() {
	return getBaseQuery("Desbroce con retroaraña");
    }

    public String getDesbroceRetroaranhaSumQuery() {
	return getBaseSumQuery("unidad='Desbroce con retroaraña'");
    }

    public String getDesbroceMecanicoQuery() {
	return getBaseQuery("Desbroce mecánico");
    }

    public String getDesbroceMecanicoSumQuery() {
	return getBaseSumQuery("unidad='Desbroce mecánico'");
    }

    public String getDesbroceManualQuery() {
	return getBaseQuery("Tala y desbroce manual");
    }

    public String getDesbroceManualSumQuery() {
	return getBaseSumQuery("unidad='Tala y desbroce manual'");
    }

    public String getDesbroceTotalSumQuery(String string) {
	return getBaseSumQuery(
		string,
		"unidad IN ('Desbroce con retroaraña', 'Desbroce mecánico', 'Tala y desbroce manual')");
    }

    public String getDesbroceTotalSumQuery() {
	return getBaseSumQuery2("unidad IN ('Desbroce con retroaraña', 'Desbroce mecánico', 'Tala y desbroce manual')");
    }

    public String getSiegaMecanicaIsletasQuery() {
	return getBaseQuery("Siega mecánica de isletas");
    }

    public String getSiegaMecanicaIsletasSumQuery() {
	return getBaseSumQuery("unidad = 'Siega mecánica de isletas'");
    }

    public String getSiegaMecanicaMedianaQuery() {
	return getBaseQuery("Siega mecánica de medianas");
    }

    public String getSiegaMecanicaMedianaSumQuery() {
	return getBaseSumQuery("unidad = 'Siega mecánica de medianas'");
    }

    public String getSiegaMecanicaMediana1_5mQuery() {
	return getBaseQuery("Siega mecánica de medianas < 1,5 m");
    }

    public String getSiegaMecanicaMediana1_5mSumQuery() {
	return getBaseSumQuery("unidad = 'Siega mecánica de medianas < 1,5 m'");
    }

    public String getSiegaTotalSumQuery(String string) {
	return getBaseSumQuery(
		string,
		"unidad IN ('Siega mecánica de isletas', 'Siega mecánica de medianas', 'Siega mecánica de medianas < 1,5 m')");
    }

    public String getSiegaTotalSumQuery() {
	return getBaseSumQuery2("unidad IN ('Siega mecánica de isletas', 'Siega mecánica de medianas', 'Siega mecánica de medianas < 1,5 m')");
    }

    public String getHerbicidadQuery() {
	return getBaseQuery("Herbicida");
    }

    public String getHerbicidaSumQuery() {
	return getBaseSumQuery("unidad = 'Herbicida'");
    }

    public String getVegetacionQuery() {
	return getBaseQuery("Eliminación veg. mediana de HG y transp. a vertedero");
    }

    public String getVegetacionSumQuery() {
	return getBaseSumQuery("unidad = 'Eliminación veg. mediana de HG y transp. a vertedero'");
    }

}
