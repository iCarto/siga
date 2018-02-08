package es.icarto.gvsig.extgia.consultas;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.Document;

import es.icarto.gvsig.commons.utils.Field;

public abstract class PDFCaracteristicasReport extends PDFReport {

    public PDFCaracteristicasReport(String[] element, String fileName, DefaultTableModel table,
            ConsultasFilters<Field> filters, QueryType reportType) {
        super(element, fileName, table, filters, reportType);
    }

    @Override
    protected void writeDatesRange(Document document, ConsultasFilters<Field> filters) {
        // CaracteristicasReport does not have date ranges
    }

}
