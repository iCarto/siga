package es.icarto.gvsig.extgia.consultas.firme;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.QueryType;
import es.icarto.gvsig.extgia.consultas.TrabajosReport;

public class FirmeTrabajosReport extends TrabajosReport {

    public FirmeTrabajosReport(String[] element, String fileName,
	    DefaultTableModel table, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, table, filters, tipo);

    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Fecha", "PK inicio",
		"PK final", "Sentido", "Descripción" };
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
	columnsWidth[5] = 198f;

	return columnsWidth;
    }

}
