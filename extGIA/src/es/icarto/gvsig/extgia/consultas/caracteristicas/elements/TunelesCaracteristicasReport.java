package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFCaracteristicasReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class TunelesCaracteristicasReport extends PDFCaracteristicasReport {

	public TunelesCaracteristicasReport(String[] element, String fileName,
			DefaultTableModel table, ConsultasFilters<Field> filters,
			QueryType reportType) {
		super(element, fileName, table, filters, reportType);
	}

	@Override
	protected String[] getColumnNames() {
		String[] columnNames = { "ID Túnel", "Nombre", "Tramo",
				"PK Inicial", "PK Final", "Longitud", "Observaciones" };
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
		columnsWidth[6] = 90f;

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
