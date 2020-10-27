package es.icarto.gvsig.commons.queries;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import es.icarto.gvsig.commons.utils.Field;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.icarto.gvsig.commons.queries.QueryFiltersI;

public class XLSReport {

    private static final String PATTERN = DateFormatNT.getDateFormat()
	    .toPattern();
    private final Workbook wb;
    protected final Sheet sheet;
    protected final QueryFiltersI filters;
    protected int colNamesRowIdx;
    private final boolean[] columnsStyles;

    public XLSReport(String outputFile, DefaultTableModel table,
	    QueryFiltersI filters) {
	this.filters = filters;
	columnsStyles = new boolean[table.getColumnCount()];
	Arrays.fill(columnsStyles, false);
	if (!filters.getSeleccionados()) {
	    colNamesRowIdx = filters.getLocation().size() + 1;
	} else {
	    colNamesRowIdx = 0;
	}
	if (outputFile == null) {
	    wb = null;
	    sheet = null;
	    return;
	}
	wb = new HSSFWorkbook(); // xls
	String safeName = WorkbookUtil.createSafeSheetName("Listado");
	sheet = wb.createSheet(safeName);

	writeFilters();
	writeTable(table);
	
	sheet.setAutoFilter(new CellRangeAddress(colNamesRowIdx,
	        colNamesRowIdx, 0, table.getColumnCount() - 1));
	try {
	    writeToDisk(outputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    protected void writeFilters() {
    if (!filters.getSeleccionados()) {
        short rowIdx = 0;
        
        for (Field l : filters.getLocation()) {
            Row row = sheet.createRow(rowIdx++);
            String longName = l.getLongName().trim();
            longName = longName.endsWith(":") ? longName.substring(0, longName.length() - 1) : longName;
            row.createCell(0).setCellValue(longName);
            row.createCell(1).setCellValue(l.getValue().toString());
        }
    } 
    }

    private void writeTable(DefaultTableModel table) {
	writeColumnNames(table);
	writeRows(table);
    }

    private void writeColumnNames(DefaultTableModel table) {
	Row row0 = sheet.createRow(colNamesRowIdx);

	for (int i = 0; i < table.getColumnCount(); i++) {
	    row0.createCell(i).setCellValue(table.getColumnName(i));
	}
    }

    private void writeRows(DefaultTableModel tableModel) {

	for (int rowIdx = 0; rowIdx < tableModel.getRowCount(); rowIdx++) {
	    Row row = sheet.createRow(rowIdx + colNamesRowIdx + 1);
	    for (int column = 0; column < tableModel.getColumnCount(); column++) {
		Object value = tableModel.getValueAt(rowIdx, column);
		createCell(row, column, value);
	    }
	}

	for (int column = 0; column < tableModel.getColumnCount(); column++) {
	    sheet.autoSizeColumn(column);
	}
    }

    private void createCell(Row row, int column, Object value) {
	if (value == null) {
	    row.createCell(column);
	} else if (value instanceof String) {
	    row.createCell(column).setCellValue((String) value);
	} else if (value instanceof Date) {
	    if (!columnsStyles[column]) {
		columnsStyles[column] = true;
		CellStyle cellStyle = wb.createCellStyle();
		CreationHelper creationHelper = wb.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat()
			.getFormat(PATTERN));
		sheet.setDefaultColumnStyle(column, cellStyle);
	    }
	    row.createCell(column).setCellValue((Date) value);
	} else if (value instanceof Number) {
	    Number doubleValue = (Number) value;
	    row.createCell(column).setCellValue(doubleValue.doubleValue());
	} else if (value instanceof Boolean) {
	    row.createCell(column).setCellValue((Boolean) value ? "Sí" : "No");
	} else {
	    throw new AssertionError("This should never happen");
	}
    }

    private void writeToDisk(String outputFile) throws IOException {
	FileOutputStream fileOut = new FileOutputStream(outputFile);
	this.wb.write(fileOut);
	fileOut.close();
    }

}
