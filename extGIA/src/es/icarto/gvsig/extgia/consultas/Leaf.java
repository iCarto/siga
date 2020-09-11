package es.icarto.gvsig.extgia.consultas;

import java.io.File;

import javax.swing.table.DefaultTableModel;

import com.iver.andami.PluginServices;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.icarto.gvsig.commons.queries.Component;
import es.icarto.gvsig.commons.queries.FinalActions;
import es.icarto.gvsig.commons.queries.XLSReport;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.agregados.TrabajosAgregadosReport;
import es.icarto.gvsig.extgia.consultas.agregados.XLSTrabajosAgregadosReport;
import es.icarto.gvsig.extgia.consultas.firme.FirmeTrabajosReport;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class Leaf implements Component {

    private final String[] element;
    private final ConsultasFilters<Field> consultasFilters;
    private final KeyValue tipoConsulta;
    private final boolean pdf;

    private File outputFile;
    private boolean emptyQuery = false;

    public Leaf(String[] element, ConsultasFilters<Field> consultasFilters,
	    KeyValue tipoConsulta, boolean pdf) {
	this.element = element;
	this.consultasFilters = consultasFilters;
	this.outputFile = new File("./" + element[0]);
	this.tipoConsulta = tipoConsulta;
	this.pdf = pdf;
    }

    @Override
    public boolean setOutputPath(File path) {
	String extension;
	String extensionDescription;
	if (pdf) {
	    extension = "pdf";
	    extensionDescription = PluginServices.getText(this, "pdfFiles");
	} else {
	    extension = "xls";
	    extensionDescription = PluginServices.getText(this, "excelFiles");
	}
	if (path == null) {
	    SaveFileDialog sfd = new SaveFileDialog(extensionDescription, extension);
	    sfd.setAskForOverwrite(true);
	    outputFile = sfd.showDialog();
	} else {
	    if (path.isDirectory()) {
		outputFile = new File(path.getAbsolutePath() + File.separator
			+ element[0] + "." + extension);
	    } else {
		outputFile = path;
	    }
	}

	return outputFile != null;
    }

    @Override
    public void generateReportFile() {
	QueryType tipo = getTipo();
	if (tipo == QueryType.TRABAJOS_AGREGADOS) {
	    if (pdf) {
		createPdfReportAgregados(outputFile.getAbsolutePath(), element,
			consultasFilters);
	    } else {
		createCsvReportAgregados(outputFile.getAbsolutePath(), element,
			consultasFilters);
	    }

	} else {

	    String query = getReportQuery(tipo, consultasFilters, element[0]);
	    ConnectionWrapper con = new ConnectionWrapper(DBSession
		    .getCurrentSession().getJavaConnection());
	    DefaultTableModel table = con.execute(query);
	    if (table.getRowCount() == 0) {
		emptyQuery = true;
		return;
	    }

	    if (pdf) {
		createPdfReport(tipo, outputFile.getAbsolutePath(), element,
			consultasFilters, table);
	    } else {
		if (tipo == QueryType.CARACTERISTICAS) {
		    new XLSReport(outputFile.getAbsolutePath(), table,
			    consultasFilters);
		} else {
		    new XLSDatedReport(outputFile.getAbsolutePath(), table,
			    consultasFilters);
		}

	    }
	}
    }

    private void createPdfReportAgregados(String outputFile, String[] element,
	    ConsultasFilters<Field> filters) {
	new TrabajosAgregadosReport(element, outputFile, null, filters,
		QueryType.TYPE_NOT_SET); // TODO
    }

    private void createCsvReportAgregados(String outputFile, String[] element,
	    ConsultasFilters<Field> filters) {
	new XLSTrabajosAgregadosReport(element[0], outputFile, consultasFilters);
    }

    /**
     * fpuga. 8/07/2015. Este tipo de datos probablemente habría que mantenerlos
     * en la base de datos y "cruzados" mediante claves foráneas, de modo que un
     * cambio en la base de datos no rompa todo el código
     */
    private QueryType getTipo() {
	QueryType tipo = QueryType.TYPE_NOT_SET;
	if (tipoConsulta.equals("Trabajos") && element[1].equals("Firme")) {
	    tipo = QueryType.TRABAJOS_FIRME;
	} else if (tipoConsulta.equals("Trabajos")
		&& (element[1].equals("Taludes")
			|| element[1].equals("Isletas")
			|| element[1].equals("Barrera Rígida") || element[1]
				.equals("Señalización Vertical"))) {
	    tipo = QueryType.TRABAJOS_VEGETACION;
	} else if (tipoConsulta.equals("Trabajos")) {
	    tipo = QueryType.TRABAJOS;
	} else if (tipoConsulta.equals("Inspecciones")) {
	    tipo = QueryType.RECONOCIMIENTOS;
	} else if (tipoConsulta.equals("Características")) {
	    tipo = QueryType.CARACTERISTICAS;
	} else if (tipoConsulta.equals("Trabajos Agrupados")) {
	    tipo = QueryType.TRABAJOS_AGREGADOS;
	}

	return tipo;
    }

    private String getFields(QueryType tipo, String elementId) {
	switch (tipo) {
	case TRABAJOS_FIRME:
	    return ConsultasFieldNames.getFirmeTrabajosFieldNames(elementId);
	case TRABAJOS:
	    return ConsultasFieldNames.getTrabajosFieldNames(elementId);
	case TRABAJOS_VEGETACION:
	    return ConsultasFieldNames
		    .getTrabajosVegetacionFieldNames(elementId);
	case RECONOCIMIENTOS:
	    return ConsultasFieldNames.getReconocimientosFieldNames(element[0]);
	case CARACTERISTICAS:
	    return ConsultasFieldNames
		    .getPDFCaracteristicasFieldNames(element[0]);
	default:
	    return "";
	}
    }

    private String getReportQuery(QueryType tipo,
	    ConsultasFilters<Field> filters, String element) {
	String query;

	if (tipo == QueryType.CARACTERISTICAS) {
	    if (filters.getQueryType().equals("CUSTOM")) {
		query = CaracteristicasQueries.getCustomCaracteristicasQuery(
			filters, element);
	    } else if (pdf) {
		query = CaracteristicasQueries.getPDFCaracteristicasQuery(
			element, filters);
	    } else {
		query = CaracteristicasQueries.getCSVCaracteristicasQuery(
			element, filters);
	    }
	} else {
	    String elementId = ConsultasFieldNames.getElementId(element);
	    String fields;
	    if (filters.getQueryType().equals("CUSTOM")) {
		fields = CaracteristicasQueries.buildFields(filters, "",
			element);
	    } else {
		fields = getFields(tipo, elementId);
	    }
	    query = CaracteristicasQueries.getReconocimientosTrabajosQuery(
		    element, filters, fields, elementId, tipoConsulta.getKey());

	    if (tipo == QueryType.TRABAJOS || tipo == QueryType.TRABAJOS_FIRME
	            || tipo == QueryType.TRABAJOS_VEGETACION) {
		query += filters.getWhereClauseByDates("fecha");
	    } else {
		query += filters.getWhereClauseByDates("fecha_inspeccion");
	    }
	    if (filters.getQueryType().equals("CUSTOM")) {
		query = CaracteristicasQueries.buildOrderBy(filters, query);
	    }

	}
	return query;
    }

    private void createPdfReport(QueryType tipo, String outputFile,
	    String[] element, ConsultasFilters<Field> filters,
	    DefaultTableModel table) {

	if (filters.getQueryType().equals("CUSTOM")) {
	    new CustomPDFReport(element, outputFile, table, filters, tipo);
	    return;
	}

	switch (tipo) {
	case TRABAJOS:
	    new TrabajosReport(element, outputFile, table, filters, tipo);
	    break;
	case TRABAJOS_FIRME:
	    new FirmeTrabajosReport(element, outputFile, table, filters, tipo);
	    break;
	case TRABAJOS_VEGETACION:
	    new TrabajosVegetacionReport(element, outputFile, table, filters,
		    tipo);
	    break;
	case CARACTERISTICAS:
	    ConsultasFieldNames.createCaracteristicasReport(element,
		    outputFile, table, filters, tipo);
	    break;
	case RECONOCIMIENTOS:
	    new ReconocimientosReport(element, outputFile, table, filters, tipo);
	    break;
	default:
	    break;
	}

    }

    @Override
    public void finalActions() {
	FinalActions finalActions = new FinalActions(emptyQuery, outputFile);
	finalActions.openReport();
    }

}
