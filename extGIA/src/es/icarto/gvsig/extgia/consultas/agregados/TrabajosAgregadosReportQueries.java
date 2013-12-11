package es.icarto.gvsig.extgia.consultas.agregados;

public class TrabajosAgregadosReportQueries {

    private final String element;

    public TrabajosAgregadosReportQueries(String element) {
	this.element = element;

    }

    public String getBaseQuery() {
	if (element.equalsIgnoreCase("isletas")) {
	    return getIsletasBaseQuery();
	}else if (element.equalsIgnoreCase("taludes")) {
	    return getTaludesBaseQuery();
	}
	return null;
    }

    public String getTaludesBaseQuery() {
	return "SELECT distinct(a.id_talud), tr.item, tv.item, nv.item, pk_inicial, pk_final, c.item, " +
		"medicion_audasa " +
		"FROM audasa_extgia." + element + "_trabajos a, audasa_extgia." + element +
		" b, " + "audasa_extgia_dominios.sentido c, " + "audasa_extgia_dominios.tramo tr, "
		+ "audasa_extgia_dominios.tipo_via tv, " + "audasa_extgia_dominios.nombre_via nv " +
		"WHERE a.id_talud = b.id_talud AND b.sentido = c.id AND b.tramo = tr.id " +
		"AND b.tipo_via = tv.id AND b.nombre_via = cast (nv.id as text) " +
		"AND unidad = '";
    }

    public String getIsletasBaseQuery() {
	return "SELECT distinct(a.id_isleta), tr.item, tv.item, nv.item, pk_inicial, pk_final, c.item, " +
		"medicion_audasa " +
		"FROM audasa_extgia." + element + "_trabajos a, audasa_extgia." + element +
		" b, " + "audasa_extgia_dominios.sentido c, " + "audasa_extgia_dominios.tramo tr, "
		+ "audasa_extgia_dominios.tipo_via tv, " + "audasa_extgia_dominios.nombre_via nv " +
		"WHERE a.id_isleta = b.id_isleta AND b.sentido = c.id AND b.tramo = tr.id " +
		"AND b.tipo_via = tv.id AND b.nombre_via = cast (nv.id as text) " +
		"AND unidad = '";
    }

    public String getBaseSumQuery() {
	return "SELECT sum(medicion_audasa) " +
		"FROM audasa_extgia." + element + "_trabajos " +
		"WHERE (unidad = '";
    }

    public String getDesbroceRetroaranhaQuery() {
	return getBaseQuery() + "Desbroce con retroara�a'";
    }

    public String getDesbroceRetroaranhaSumQuery() {
	return getBaseSumQuery() + "Desbroce con retroara�a')";
    }

    public String getDesbroceMecanicoQuery() {
	return getBaseQuery() + "Desbroce mec�nico'";
    }

    public String getDesbroceMecanicoSumQuery() {
	return getBaseSumQuery() + "Desbroce mec�nico')";
    }

    public String getDesbroceManualQuery() {
	return getBaseQuery() + "Tala y desbroce manual'";
    }

    public String getDesbroceManualSumQuery() {
	return getBaseSumQuery() + "Tala y desbroce manual')";
    }

    public String getDesbroceTotalSumQuery() {
	return getBaseSumQuery() + "Desbroce con retroara�a'" +
		" OR unidad = 'Desbroce mec�nico' OR unidad = 'Tala y desbroce manual')";
    }

    public String getSiegaMecanicaIsletasQuery() {
	return getBaseQuery() + "Siega mec�nica de isletas'";
    }

    public String getSiegaMecanicaMedianaQuery() {
	return getBaseQuery() + "Siega mec�nica de medianas'";
    }

    public String getSiegaMecanicaMediana1_5mQuery() {
	return getBaseQuery() + "Siega mec�nica de medianas < 1,5 m'";
    }

    public String getSiegaMecanicaIsletasSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica de isletas')";
    }

    public String getSiegaMecanicaMedianaSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica de medianas')";
    }

    public String getSiegaMecanicaMediana1_5mSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica de medianas < 1,5 m')";
    }

    public String getSiegaTotalSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica de isletas'" +
		" OR unidad = 'Siega mec�nica de medianas'" +
		" OR unidad = 'Siega mec�nica de medianas < 1,5 m')";
    }

    public String getHerbicidadQuery() {
	return getBaseQuery() + "Herbicida'";
    }

    public String getHerbicidaSumQuery() {
	return getBaseSumQuery() + "Herbicida')";
    }

    public String getVegetacionQuery() {
	return getBaseQuery() + "Vegetaci�n mediana de hormig�n'";
    }

    public String getVegetacionSumQuery() {
	return getBaseSumQuery() + "Vegetaci�n mediana de hormig�n')";
    }

}
