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

public class ObrasPasoCaracteristicasReport extends PDFReport {

    public ObrasPasoCaracteristicasReport(String element[], String fileName,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Obra Paso", "Tramo", "Tipo Vía",
		"Nombre Vía", "PK", "Código", "Nombre", "Tipo", "Tipología",
		"Longitud (m)", "Anchura (m)", "Altura (m)",
		"Gálibo V Cre. (m)", "Gálibo V Dec. (m)", "Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 60f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 60f;
	columnsWidth[3] = 90f;
	columnsWidth[4] = 60f;
	columnsWidth[5] = 60f;
	columnsWidth[6] = 120f;
	columnsWidth[7] = 120f;
	columnsWidth[8] = 90f;
	columnsWidth[9] = 60f;
	columnsWidth[10] = 60f;
	columnsWidth[11] = 60f;
	columnsWidth[12] = 60f;
	columnsWidth[13] = 60f;
	columnsWidth[14] = 90f;

	return columnsWidth;
    }

    @Override
    protected void writeDatesRange(Document document,
	    ConsultasFilters<Field> filters) {

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
