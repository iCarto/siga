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
	return "SELECT a.id_talud, pk_inicial, pk_final, c.item, medicion_audasa " +
		"FROM audasa_extgia." + element + "_trabajos a, audasa_extgia." + element +
		" b, " + "audasa_extgia_dominios.sentido c " +
		"WHERE a.id_talud = b.id_talud AND b.sentido = c.id " +
		"AND unidad = '";
    }

    public String getIsletasBaseQuery() {
	return "SELECT a.id_isleta, pk_inicial, pk_final, c.item, medicion_audasa " +
		"FROM audasa_extgia." + element + "_trabajos a, audasa_extgia." + element +
		" b, " + "audasa_extgia_dominios.sentido c " +
		"WHERE a.id_isleta = b.id_isleta AND b.sentido = c.id " +
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
	return getBaseQuery() + "Siega mec�nica isletas'";
    }

    public String getSiegaMecanicaMedianaQuery() {
	return getBaseQuery() + "Siega mec�nica mediana'";
    }

    public String getSiegaMecanicaIsletasSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica isletas')";
    }

    public String getSiegaMecanicaMedianaSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica mediana')";
    }

    public String getSiegaTotalSumQuery() {
	return getBaseSumQuery() + "Siega mec�nica isletas'" +
		" OR unidad = 'Siega mec�nica mediana')";
    }

    public String getHerbicidadQuery() {
	return getBaseQuery() + "Herbicida'";
    }

    public String getHerbicidaSumQuery() {
	return getBaseSumQuery() + "Herbicida')";
    }

    public String getVegeracionQuery() {
	return getBaseQuery() + "Vegeraci�n mediana de hormig�n'";
    }

    public String getVegeracionSumQuery() {
	return getBaseSumQuery() + "Vegeraci�n mediana de hormig�n')";
    }

}
