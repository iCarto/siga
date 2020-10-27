package es.icarto.gvsig.extgia.consultas;

import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Row;

import es.icarto.gvsig.commons.queries.QueryFiltersI;
import es.icarto.gvsig.commons.queries.XLSReport;
import es.icarto.gvsig.commons.utils.Field;

public class XLSDatedReport extends XLSReport {

    public XLSDatedReport(String outputFile, DefaultTableModel table,
	    QueryFiltersI filters) {
	super(outputFile, table, filters);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void writeFilters() {
	super.writeFilters();

	if (filters instanceof ConsultasFilters) {
	    if (!((ConsultasFilters<Field>) filters).getUltimos()) {
	        ConsultasFilters<Field> cfilters = (ConsultasFilters<Field>) filters;
	        int n;
            if (!filters.getSeleccionados()) {
                n = filters.getLocation().size();
                colNamesRowIdx = 7;
            } else {
                n = 0;
                colNamesRowIdx = 3;
            }
            Row row = sheet.createRow(n);
            row.createCell(0).setCellValue("Desde: ");
            row.createCell(1).setCellValue(cfilters.getFechaInicioFormatted());
    
            row = sheet.createRow(n + 1);
            row.createCell(0).setCellValue("Hasta: ");
            row.createCell(1).setCellValue(cfilters.getFechaFinFormatted());
	    } 
	}
    }
}
