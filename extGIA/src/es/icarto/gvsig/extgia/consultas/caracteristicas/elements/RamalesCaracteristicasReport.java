package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFCaracteristicasReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class RamalesCaracteristicasReport extends PDFCaracteristicasReport {

    public RamalesCaracteristicasReport(String[] element, String fileName,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "Tramo", "Tipo Vía", "Nombre Vía", "PK",
		"Ramal", "Sentido", "Dirección", "Municipio", "Longitud",
		"Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 70f;
	columnsWidth[1] = 70f;
	columnsWidth[2] = 70f;
	columnsWidth[3] = 70f;
	columnsWidth[4] = 70f;
	columnsWidth[5] = 70f;
	columnsWidth[6] = 70f;
	columnsWidth[7] = 70f;
	columnsWidth[8] = 70f;
	columnsWidth[8] = 100f;

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
