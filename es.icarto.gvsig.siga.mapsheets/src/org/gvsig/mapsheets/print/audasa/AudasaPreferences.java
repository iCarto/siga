package org.gvsig.mapsheets.print.audasa;

import java.io.File;

import org.gvsig.tools.file.PathGenerator;

import com.iver.andami.PluginServices;

public class AudasaPreferences {

    public static final String TITULO_VENTANA = "Crear mapa";

    // Templates
    public static final String CONSULTAS = "Expediente consultas";
    public static final String A3_CONSULTAS = "Expediente consultas (A3-Horizontal)";
    public static final String A3_CONSULTAS_LOCALIZADOR = "Expediente consultas (A3-Horizontal/leyenda)";
    public static final String A3_CONSULTAS_VERTICAL = "Expediente consultas (A3-Vertical)";
    public static final String A3_CONSULTAS_VERTICAL_LOCALIZADOR = "Expediente consultas (A3-Vertical/leyenda)";
    public static final String A4_CONSULTAS = "Expediente consultas (A4-Horizontal)";
    public static final String A4_CONSULTAS_LOCALIZADOR = "Expediente consultas (A4-Horizontal/leyenda)";
    public static final String A4_CONSULTAS_VERTICAL = "Expediente consultas (A4-Vertical)";
    public static final String A4_CONSULTAS_VERTICAL_LOCALIZADOR = "Expediente consultas (A4-Vertical/leyenda)";
    public static final String DIMENSIONES = "Estado dimensiones";
    public static final String A3_DIMENSIONES = "Estado dimensiones (A3-Horizontal)";
    public static final String A3_DIMENSIONES_LOCALIZADOR = "Estado dimensiones (A3-Horizontal/leyenda)";
    public static final String POLICIA_MARGENES = "Polic?a de m?rgenes";
    public static final String A3_POLICIA_MARGENES = "Polic?a de m?rgenes (A3-Horizontal)";
    public static final String A3_POLICIA_MARGENES_LEYENDA = "Polic?a de m?rgenes (A3-Horizontal/leyenda)";
    public static final String A3_POLICIA_MARGENES_VERTICAL = "Polic?a de m?rgenes (A3-Vertical)";
    public static final String A3_POLICIA_MARGENES_VERTICAL_LEYENDA = "Polic?a de m?rgenes (A3-Vertical/leyenda)";
    public static final String A4_POLICIA_MARGENES = "Polic?a de m?rgenes (A4-Horizontal)";
    public static final String A4_POLICIA_MARGENES_LEYENDA = "Polic?a de m?rgenes (A4-Horizontal/leyenda)";
    public static final String A4_POLICIA_MARGENES_VERTICAL = "Polic?a de m?rgenes (A4-Vertical)";
    public static final String A4_POLICIA_MARGENES_VERTICAL_LEYENDA = "Polic?a de m?rgenes (A4-Vertical/leyenda)";

    public static String[] getTemplates() {
	String[] plantillas = new String[18];
	plantillas[0] = AudasaPreferences.A4_CONSULTAS;
	plantillas[1] = AudasaPreferences.A4_CONSULTAS_LOCALIZADOR;
	plantillas[2] = AudasaPreferences.A4_CONSULTAS_VERTICAL;
	plantillas[3] = AudasaPreferences.A4_CONSULTAS_VERTICAL_LOCALIZADOR;

	plantillas[4] = AudasaPreferences.A3_CONSULTAS;
	plantillas[5] = AudasaPreferences.A3_CONSULTAS_LOCALIZADOR;
	plantillas[6] = AudasaPreferences.A3_CONSULTAS_VERTICAL;
	plantillas[7] = AudasaPreferences.A3_CONSULTAS_VERTICAL_LOCALIZADOR;

	plantillas[8] = AudasaPreferences.A3_DIMENSIONES;
	plantillas[9] = AudasaPreferences.A3_DIMENSIONES_LOCALIZADOR;

	plantillas[10] = AudasaPreferences.A4_POLICIA_MARGENES;
	plantillas[11] = AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA;
	plantillas[12] = AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL;
	plantillas[13] = AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL_LEYENDA;
	plantillas[14] = AudasaPreferences.A3_POLICIA_MARGENES;
	plantillas[15] = AudasaPreferences.A3_POLICIA_MARGENES_LEYENDA;
	plantillas[16] = AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL;
	plantillas[17] = AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL_LEYENDA;
	return plantillas;
    }

    // Variables for templates
    public static final String INGENIERO_DIRECTOR = "ingeniero_director";
    public static final String PROVINCIA = "provincia";
    public static final String MUNICIPIO = "municipio";
    public static final String PROYECTO = "proyecto";
    public static final String DIBUJO = "dibujo";
    public static final String CONSULTA = "consulta";
    public static final String FECHA = "fecha";
    public static final String TITULO_ESTUDIO = "titulo_estudio";
    public static final String TITULO_PLANO = "titulo_plano";
    public static final String TRAMO = "tramo";
    public static final String NUMERO_PLANO = "numero_plano";
    public static final String NUMERO_REFERENCIA = "referencia";
    public static final String CONSULTORA = "consultora";
    public static final String INGENIERO_AUTOR = "ingeniero_autor";
    public static final String CLAVE = "clave";
    public static final String SUSTITUYE_A = "sustituye_a";
    public static final String SUSTITUIDO_POR = "sustituido_por";

    // units (cm) for inserting the view in the map
    public static final double VIEW_X_POSITION_A4 = 1.5;
    public static final double VIEW_Y_POSITION_A4 = 1;
    public static final double VIEW_X_POSITION_A4_VERTICAL = 1;
    public static final double VIEW_Y_POSITION_A4_VERTICAL = 1;
    public static final double VIEW_X_POSITION = 2.5;
    public static final double VIEW_Y_POSITION = 1;
    public static final double VIEW_X_POSITION_VERTICAL = 1.5;
    public static final double VIEW_Y_POSITION_VERTICAL = 1;
    public static final double VIEW_WIDTH_A4 = 27.2;
    public static final double VIEW_HEIGHT_A4 = 17.8;
    public static final double VIEW_WIDTH_A4_VERTICAL = 19;
    public static final double VIEW_HEIGHT_A4_VERTICAL = 26.5;
    public static final double VIEW_WIDTH_A3 = 37.5;
    public static final double VIEW_HEIGHT_A3 = 26.5;
    public static final double VIEW_WIDTH_A3_VERTICAL = 27.2;
    public static final double VIEW_HEIGHT_A3_VERTICAL = 38.3;

    public static final double OVERVIEW_X = 35.7;
    public static final double OVERVIEW_Y = 1.3;
    public static final double OVERVIEW_WIDTH = 5;
    public static final double OVERVIEW_HEIGHT = 5;

    public static final String GRIDS_PATH = PluginServices
	    .getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
	    .getResource("rejillas/").getPath();

    public static File getSelectedFile(String sourceButton) {
	PathGenerator.getInstance()
		.setBasePath(
			PluginServices
				.getPluginServices("es.icarto.gvsig.extgex")
				.getClassLoader().getResource("plantillas/")
				.toString());
	if (sourceButton.equals(A4_CONSULTAS_LOCALIZADOR)) {
	    return new File(
		    PluginServices
			    .getPluginServices("es.icarto.gvsig.extgex")
			    .getClassLoader()
			    .getResource(
				    "plantillas/Informes_A4_Expedientes_Consultas_Modificado.gvt")
			    .getFile());
	} else if (sourceButton.equals(A4_CONSULTAS_VERTICAL)) {
	    return new File(
			    PluginServices
				    .getPluginServices("es.icarto.gvsig.extgex")
				    .getClassLoader()
				    .getResource(
					    "plantillas/Informes_A4_Expedientes_Consultas_Vertical.gvt")
				    .getFile());
	} else if (sourceButton.equals(A4_CONSULTAS)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource(
			    "plantillas/Informes_A4_Expedientes_Consultas.gvt")
		    .getFile());
	} else if (sourceButton.equals(A3_CONSULTAS_LOCALIZADOR)) {
	    return new File(
		    PluginServices
			    .getPluginServices("es.icarto.gvsig.extgex")
			    .getClassLoader()
			    .getResource(
				    "plantillas/Informes_A3_Expedientes_Consultas_Modificado.gvt")
			    .getFile());
	} else if (sourceButton.equals(A3_CONSULTAS_VERTICAL)) {
	    return new File(
			    PluginServices
				    .getPluginServices("es.icarto.gvsig.extgex")
				    .getClassLoader()
				    .getResource(
					    "plantillas/Informes_A3_Expedientes_Consultas_Vertical.gvt")
				    .getFile());
	} else if (sourceButton.equals(A3_CONSULTAS)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource(
			    "plantillas/Informes_A3_Expedientes_Consultas.gvt")
		    .getFile());
	} else if (sourceButton.equals(A3_DIMENSIONES_LOCALIZADOR)) {
	    return new File(
		    PluginServices
			    .getPluginServices("es.icarto.gvsig.extgex")
			    .getClassLoader()
			    .getResource(
				    "plantillas/Informes_A3_Estado_Dimensiones_Modificado.gvt")
			    .getFile());
	} else if (sourceButton.endsWith(A3_DIMENSIONES)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource(
			    "plantillas/Informes_A3_Estado_Dimensiones.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A3_POLICIA_MARGENES)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A3_Policia_margenes.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A3_POLICIA_MARGENES_LEYENDA)) {
	    return new File(
		    PluginServices
			    .getPluginServices("es.icarto.gvsig.extgex")
			    .getClassLoader()
			    .getResource(
				    "plantillas/Informes_A3_Policia_margenes_leyenda.gvt")
			    .getFile());
	} else if (sourceButton.endsWith(A4_CONSULTAS_VERTICAL_LOCALIZADOR)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A4_Expedientes_Consultas_Modificado_Vertical.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A3_CONSULTAS_VERTICAL_LOCALIZADOR)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A3_Expedientes_Consultas_Modificado_Vertical.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A4_POLICIA_MARGENES_VERTICAL)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A4_Policia_margenes_Vertical.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A4_POLICIA_MARGENES_VERTICAL_LEYENDA)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A4_Policia_margenes_leyenda_Vertical.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A3_POLICIA_MARGENES_VERTICAL)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A3_Policia_margenes_Vertical.gvt")
		    .getFile());
	} else if (sourceButton.endsWith(A3_POLICIA_MARGENES_VERTICAL_LEYENDA)) {
	    return new File(PluginServices
		    .getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("plantillas/Informes_A3_Policia_margenes_leyenda_Vertical.gvt")
		    .getFile());
	} else {
	    return new File(
		    PluginServices
			    .getPluginServices("es.icarto.gvsig.extgex")
			    .getClassLoader()
			    .getResource(
				    "plantillas/Informes_A4_Policia_margenes_leyenda.gvt")
			    .getFile());
	}
    }

}
