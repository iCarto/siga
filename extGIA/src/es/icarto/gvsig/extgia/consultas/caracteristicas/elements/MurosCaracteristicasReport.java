package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class MurosCaracteristicasReport extends PDFReport {

    public MurosCaracteristicasReport(String element[], String fileName,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected Rectangle setPageSize() {
	return PageSize.A4.rotate();
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Muro", "Tramo", "Tipo V�a", "Nombre V�a",
		"PK Inicial", "PK Final", "Material", "Longitud (m)",
		"Altura M�xima (m)", "Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 60f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 60f;
	columnsWidth[3] = 60f;
	columnsWidth[4] = 60f;
	columnsWidth[5] = 60f;
	columnsWidth[6] = 60f;
	columnsWidth[7] = 60f;
	columnsWidth[8] = 90f;

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
