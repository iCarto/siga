package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFCaracteristicasReport;
import es.icarto.gvsig.extgia.consultas.QueryType;

public class CentrosMandoCaracteristicasReport extends PDFCaracteristicasReport {

    public CentrosMandoCaracteristicasReport(String[] element, String fileName, DefaultTableModel table,
            ConsultasFilters<Field> filters, QueryType reportType) {
        super(element, fileName, table, filters, reportType);
    }

    @Override
    protected String[] getColumnNames() {
        String[] columnNames = { "ID Centro Mando", "Tramo", "Tipo Vía", "Nombre Vía", "PK", "Grupo", "CUPS",
                "Nº Farolas", "Observaciones" };
        return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
        float[] columnsWidth = new float[columnCount];

        columnsWidth[0] = 65f;
        columnsWidth[1] = 65f;
        columnsWidth[2] = 65f;
        columnsWidth[3] = 65f;
        columnsWidth[4] = 65f;
        columnsWidth[5] = 65f;
        columnsWidth[6] = 65f;
        columnsWidth[7] = 65f;
        columnsWidth[8] = 95f;

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