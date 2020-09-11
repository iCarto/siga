package es.icarto.gvsig.extgia.consultas;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;

public class TrabajosReport extends PDFReport {

    public TrabajosReport(String[] element, String fileName,
	    DefaultTableModel table, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, table, filters, tipo);
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Fecha", "Unidad",
		"Medición", "Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 60f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 100f;
	columnsWidth[3] = 63f;
	columnsWidth[4] = 155f;

	return columnsWidth;
    }

    @Override
    protected Rectangle setPageSize() {
	return PageSize.A4;
    }

    @Override
    protected boolean hasEmbebedTable() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    protected PdfPCell writeAditionalColumnName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected PdfPCell writeAditionalColumnValues(String id) {
	// TODO Auto-generated method stub
	return null;
    }

}
