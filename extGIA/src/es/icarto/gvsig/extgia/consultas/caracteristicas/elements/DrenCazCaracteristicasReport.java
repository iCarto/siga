package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class DrenCazCaracteristicasReport extends PDFReport {

	public DrenCazCaracteristicasReport(String[] element, String fileName,
			DefaultTableModel table, ConsultasFilters<Field> filters,
			QueryType reportType) {
		super(element, fileName, table, filters, reportType);
	}

	@Override
	protected String[] getColumnNames() {
		String[] columnNames = { "ID Dren-Caz", "Tramo", "Tipo Vía", 
				"Nombre Vía", "PK", "Longitud", "Observaciones" };
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
