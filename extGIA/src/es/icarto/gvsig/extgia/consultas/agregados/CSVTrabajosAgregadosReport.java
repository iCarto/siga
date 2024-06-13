package es.icarto.gvsig.extgia.consultas.agregados;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CSVTrabajosAgregadosReport {

    private static final CharSequence CSV_SEPARATOR = "\t";

    private final String element;
    private final ConsultasFilters<Field> filters;
    private final TrabajosAgregadosReportQueries agregadosReportQueries;

    public CSVTrabajosAgregadosReport(String element, String outputFile,
	    ConsultasFilters<Field> filters) {
	this.element = element;
	agregadosReportQueries = new TrabajosAgregadosReportQueries(element, filters);
	this.filters = filters;
	writeCSVFile(outputFile);
    }

    protected String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Tramo", "Tipo Vía",
		"Nombre Vía", "PK Inicial", "PK Final", "Sentido",
		"Medición" };
	return columnNames;
    }

    private void writeCSVFile(String outputFile) {
	if (outputFile != null) {
	    try {
		FileWriter writer = new FileWriter(outputFile);

		writeTable(writer, "Desbroce con retroaraña\n\n",
			agregadosReportQueries.getDesbroceRetroaranhaQuery(),
			agregadosReportQueries.getDesbroceRetroaranhaSumQuery());
		writeTable(writer, "\nDesbroce mecánico\n\n",
			agregadosReportQueries.getDesbroceMecanicoQuery(),
			agregadosReportQueries.getDesbroceMecanicoSumQuery());
		writeTable(writer, "\nTala y desbroce manual\n\n",
			agregadosReportQueries.getDesbroceManualQuery(),
			agregadosReportQueries.getDesbroceManualSumQuery());
		writeTotal(writer, "TOTAL DESBROCES",
			agregadosReportQueries.getDesbroceTotalSumQuery());
		writeTable(writer, "\nSiega mecánica de isletas\n\n",
			agregadosReportQueries.getSiegaMecanicaIsletasQuery(),
			agregadosReportQueries
				.getSiegaMecanicaIsletasSumQuery());
		writeTable(writer, "\nSiega mecánica de medianas\n\n",
			agregadosReportQueries.getSiegaMecanicaMedianaQuery(),
			agregadosReportQueries
				.getSiegaMecanicaMedianaSumQuery());
		writeTable(writer, "\nSiega mecánica de medianas < 1,5 m\n\n",
			agregadosReportQueries
				.getSiegaMecanicaMediana1_5mQuery(),
			agregadosReportQueries
				.getSiegaMecanicaMediana1_5mSumQuery());
		writeTotal(writer, "TOTAL SEGADO DE HIERBAS",
			agregadosReportQueries.getSiegaTotalSumQuery());
		writeTable(writer, "\nHerbicida\n\n",
			agregadosReportQueries.getHerbicidadQuery(),
			agregadosReportQueries.getHerbicidaSumQuery());
		writeTable(
			writer,
			"\nEliminación veg. mediana de HG y transp. a vertedero\n\n",
			agregadosReportQueries.getVegetacionQuery(),
			agregadosReportQueries.getVegetacionSumQuery());

		writer.flush();
		writer.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
    }

    private void writeColumnsNames(FileWriter writer) {
	try {
	    for (int i = 0; i < getColumnNames().length; i++) {

		writer.append(getColumnNames()[i]);
		writer.append(CSV_SEPARATOR);
	    }
	    writer.append("\n");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void writeTable(FileWriter writer, String title, String query,
	    String totalQuery) {
	PreparedStatement statement;
	try {
	    statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    if (rs.next()) {
		writer.append(title);
		writeColumnsNames(writer);
		rs.beforeFirst();
		while (rs.next()) {
		    for (int i = 0; i < getColumnNames().length; i++) {
			writer.append(SIGAFormatter.writeValue(rs.getString(i + 1)));
			writer.append(CSV_SEPARATOR);
		    }
		    writer.append("\n");
		}
		rs = statement.executeQuery(totalQuery);
		writer.append("TOTAL");
		for (int i = 0; i <= 6; i++) {
		    writer.append(CSV_SEPARATOR);
		}
		if (rs.next()) {
		    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
			writer.append(SIGAFormatter.writeValue(rs.getString(i + 1)));
			writer.append(CSV_SEPARATOR);
		    }
		}
		writer.append("\n");
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void writeTotal(FileWriter writer, String title, String query) {
	PreparedStatement statement;
	try {
	    statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    rs.next();
	    if (rs.getString(1) != null) {
		rs.beforeFirst();
		writer.append("\n");
		writer.append(title);
		for (int i = 0; i <= 6; i++) {
		    writer.append(CSV_SEPARATOR);
		}
		rs.next();
		for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
		    writer.append(rs.getString(i + 1));
		    writer.append(CSV_SEPARATOR);
		}
		writer.append("\n");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

}
