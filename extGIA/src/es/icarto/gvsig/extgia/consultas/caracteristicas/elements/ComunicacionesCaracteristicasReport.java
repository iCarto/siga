package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFCaracteristicasReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class ComunicacionesCaracteristicasReport extends PDFCaracteristicasReport {

    public ComunicacionesCaracteristicasReport(String[] element,
	    String fileName, DefaultTableModel table,
	    ConsultasFilters<Field> filters, QueryType reportType) {
	super(element, fileName, table, filters, reportType);
    }

    @Override
    protected String[] getColumnNames() {
	// should be taken from column.properties
	String[] columnNames = { "ID Comunicación", "Tramo",
		"Tramo Constructivo", "PK inicial (Km)", "PK final (Km)",
		"Tipo", "Sección", "Longitud (m)", "Nº elementos",
		"Descripción", "Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 65f;
	columnsWidth[1] = 65f;
	columnsWidth[2] = 85f;
	columnsWidth[3] = 70f;
	columnsWidth[4] = 70f;
	columnsWidth[5] = 65f;
	columnsWidth[6] = 60f;
	columnsWidth[7] = 70f;
	columnsWidth[8] = 70f;
	columnsWidth[9] = 95f;
	columnsWidth[10] = 95f;

	return columnsWidth;
    }

    @Override
    protected boolean hasEmbebedTable() {
	return false;
    }

    @Override
    protected PdfPCell writeAditionalColumnName() {
	return null;
    }

    @Override
    protected PdfPCell writeAditionalColumnValues(String id) {
	return null;
    }

}
