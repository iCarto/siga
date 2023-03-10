package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFCaracteristicasReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class PretilesCaracteristicasReport extends PDFCaracteristicasReport {

    public PretilesCaracteristicasReport(String[] element, String fileName, DefaultTableModel table,
            ConsultasFilters<Field> filters, QueryType reportType) {
        super(element, fileName, table, filters, reportType);
    }

    @Override
    protected String[] getColumnNames() {
        String[] columnNames = { "ID Pretil", "Tramo", "Tipo V?a", "Nombre V?a", "PK Inicial", "PK Final", "Tipo",
                "Sistema contenci?n OC 35/2014", "Longitud", "Observaciones" };
        return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
        float[] columnsWidth = new float[columnCount];

        columnsWidth[0] = 55f;
        columnsWidth[1] = 55f;
        columnsWidth[2] = 55f;
        columnsWidth[3] = 55f;
        columnsWidth[4] = 55f;
        columnsWidth[5] = 55f;
        columnsWidth[6] = 55f;
        columnsWidth[7] = 55f;
        columnsWidth[8] = 55f;
        columnsWidth[9] = 85f;

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