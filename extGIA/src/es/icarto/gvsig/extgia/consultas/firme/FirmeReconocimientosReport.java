package es.icarto.gvsig.extgia.consultas.firme;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.QueryType;
import es.icarto.gvsig.extgia.consultas.ReconocimientosReport;

public class FirmeReconocimientosReport extends ReconocimientosReport {

    public FirmeReconocimientosReport(String[] element, String fileName,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Tipo Inspecci�n",
		"Nombre Revisor", "Aparato Medici�n", "Fecha Inspecci�n",
		"Observaciones" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 60f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 100f;
	columnsWidth[3] = 60f;
	columnsWidth[4] = 60f;
	columnsWidth[5] = 258f;

	return columnsWidth;
    }
}
