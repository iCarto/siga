package es.icarto.gvsig.extgia.consultas;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;

public class ReconocimientosReport extends PDFReport {

    public ReconocimientosReport(String[] element, String fileName,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected String[] getColumnNames() {

	String[] columnNames = { "ID Elemento", "Nombre Revisor",
		"Fecha Inspecci�n", "�ndice Estado", "Observaciones" };

	String[] columnNamesWithoutIndex = { "ID Elemento", "Nombre Revisor",
		"Fecha Inspecci�n", "Observaciones" };

	if (!ConsultasFieldNames
		.hasIndiceFieldOnReconocimientos(getElementID())) {
	    return columnNamesWithoutIndex;
	} else {
	    return columnNames;
	}
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	if (!ConsultasFieldNames
		.hasIndiceFieldOnReconocimientos(getElementID())) {
	    columnsWidth[0] = 70f;
	    columnsWidth[1] = 170f;
	    columnsWidth[2] = 70f;
	    columnsWidth[3] = 215f;
	} else {
	    columnsWidth[0] = 70f;
	    columnsWidth[1] = 170f;
	    columnsWidth[2] = 70f;
	    columnsWidth[3] = 70f;
	    columnsWidth[4] = 215f;
	}

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
