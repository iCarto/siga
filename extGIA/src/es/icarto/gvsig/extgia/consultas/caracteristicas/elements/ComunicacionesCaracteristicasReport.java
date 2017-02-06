package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class ComunicacionesCaracteristicasReport extends PDFReport {

    public ComunicacionesCaracteristicasReport(String[] element, String fileName,
	    DefaultTableModel table, ConsultasFilters<Field> filters,
	    QueryType reportType) {
	super(element, fileName, table, filters, reportType);
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID", "Fecha actualización", "Área mantenimiento", "Base contratista",
		"Tramo", "Tramo Constructivo", "PK inicial (Km)", "PK final (Km)", "Municipio", "Tipo", "Sección", "Longitud (m)",
		"N. elementos", "Descripción" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 35f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 70f;
	columnsWidth[3] = 60f;
	columnsWidth[4] = 45f;
	columnsWidth[5] = 60f;
	columnsWidth[6] = 50f;
	columnsWidth[7] = 60f;
	columnsWidth[8] = 60f;
	columnsWidth[9] = 60f;
	columnsWidth[10] = 50f;
	columnsWidth[11] = 60f;
	columnsWidth[12] = 50f;
	columnsWidth[13] = 90f;

	return columnsWidth;
    }
    
    @Override
    protected void writeDatesRange(Document document,
	    ConsultasFilters<Field> filters) {

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
