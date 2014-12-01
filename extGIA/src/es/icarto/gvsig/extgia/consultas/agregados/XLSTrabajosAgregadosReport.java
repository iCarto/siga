package es.icarto.gvsig.extgia.consultas.agregados;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;

import es.icarto.gvsig.commons.queries.ConnectionWrapper;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class XLSTrabajosAgregadosReport {

    private static final Logger logger = Logger
	    .getLogger(XLSTrabajosAgregadosReport.class);

    private final String element;
    private final ConsultasFilters<Field> filters;
    private final TrabajosAgregadosReportQueries agregadosReportQueries;
    private final Workbook wb;
    private final Sheet sheet;
    private final boolean[] columnsStyles;
    
    private int nextRowIdx = 0;

    private CellStyle boldStyle;

    public XLSTrabajosAgregadosReport(String element, String outputFile,
	    ConsultasFilters<Field> filters) {
	this.element = element;
	agregadosReportQueries = new TrabajosAgregadosReportQueries(element);
	this.filters = filters;
	columnsStyles = new boolean[getColumnNames().length];
	if (outputFile == null) {
	    wb = null;
	    sheet = null;
	    return;
	}
	wb = new HSSFWorkbook();
	String safeName = WorkbookUtil.createSafeSheetName("Listado");
	sheet = wb.createSheet(safeName);
	
	createBoldStyle();

	writeFilters();
	writeTables();

	try {
	    writeToDisk(outputFile);
	} catch (IOException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    private void createBoldStyle() {
	boldStyle = wb.createCellStyle();
	    Font font = wb.createFont();
	    font.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    boldStyle.setFont(font);
    }

    private void writeFilters() {

	for (Field l : filters.getLocation()) {
	    Row row = sheet.createRow(nextRowIdx++);
	    row.createCell(0).setCellValue(l.getLongName());
	    row.createCell(1).setCellValue(l.getValue().toString());
	}

	Row row = sheet.createRow(nextRowIdx++);
	row.createCell(0).setCellValue("Desde: ");
	row.createCell(1).setCellValue(filters.getFechaInicioFormatted());

	row = sheet.createRow(nextRowIdx++);
	row.createCell(0).setCellValue("Hasta: ");
	row.createCell(1).setCellValue(filters.getFechaFinFormatted());
	
	nextRowIdx += 2;
    }
    
    private void writeTables() {

	writeTable("Desbroce con retroara�a", agregadosReportQueries.getDesbroceRetroaranhaQuery(), agregadosReportQueries.getDesbroceRetroaranhaSumQuery());
	writeTable("Desbroce mec�nico",
		agregadosReportQueries.getDesbroceMecanicoQuery(),
		agregadosReportQueries.getDesbroceMecanicoSumQuery());
	writeTable("Tala y desbroce manual",
		agregadosReportQueries.getDesbroceManualQuery(),
		agregadosReportQueries.getDesbroceManualSumQuery());
	writeTotal(
		agregadosReportQueries
			.getDesbroceTotalSumQuery("TOTAL DESBROCES")
		);
	
	writeTable("Siega mec�nica de isletas",
		agregadosReportQueries.getSiegaMecanicaIsletasQuery(),
		agregadosReportQueries.getSiegaMecanicaIsletasSumQuery());
	writeTable("Siega mec�nica de medianas",
		agregadosReportQueries.getSiegaMecanicaMedianaQuery(),
		agregadosReportQueries.getSiegaMecanicaMedianaSumQuery());
	writeTable("Siega mec�nica de medianas < 1,5 m",
		agregadosReportQueries.getSiegaMecanicaMediana1_5mQuery(),
		agregadosReportQueries.getSiegaMecanicaMediana1_5mSumQuery());
	writeTotal(
		agregadosReportQueries
			.getSiegaTotalSumQuery("TOTAL SEGADO DE HIERBAS"));
	writeTable("Herbicida",
		agregadosReportQueries.getHerbicidadQuery(),
		agregadosReportQueries.getHerbicidaSumQuery());
	writeTable(
		"Eliminaci�n veg. mediana de HG y transp. a vertedero",
		agregadosReportQueries.getVegetacionQuery(),
		agregadosReportQueries.getVegetacionSumQuery());

	for (int column = 0; column < getColumnNames().length; column++) {
	    sheet.autoSizeColumn(column);
	}

    }

    private void writeTable(String title, String query, String totalQuery) {

	ConnectionWrapper con = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	String q = query
		+ filters.getWhereClauseFiltersForAgregados(element, false)
		+ " UNION " + totalQuery
		+ filters.getWhereClauseFiltersForAgregados(element, true);
	DefaultTableModel table = con.execute(q);
	if (table.getRowCount() > 1) {
	    writeTitle(title);
	    writeColumnNames();
	    writeRows(table);
	    nextRowIdx += 2;
	}
    }

    private void writeTitle(String title) {
	Row row0 = sheet.createRow(nextRowIdx++);
	Cell cell = row0.createCell(0);
	cell.setCellValue(title);
	cell.setCellStyle(boldStyle);
    }
    
    private void writeColumnNames() {
	Row row0 = sheet.createRow(nextRowIdx++);
	for (int i = 0; i < getColumnNames().length; i++) {
	    row0.createCell(i).setCellValue(getColumnNames()[i]);
	}
    }

    private String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Tramo", "Tipo V�a",
		"Nombre V�a", "PK Inicial", "PK Final", "Sentido",
		"Medici�n AUDASA" };
	return columnNames;
    }

    private void writeRows(DefaultTableModel tableModel) {
	int totalRow = -1;
	for (int i = 0; i < tableModel.getRowCount(); i++) {
	    String t = tableModel.getValueAt(i, 0).toString();
	    if (t.equalsIgnoreCase("total")) {
		totalRow = i;
		continue;
	    }
	    Row row = sheet.createRow(nextRowIdx++);
	    for (int column = 0; column < tableModel.getColumnCount(); column++) {
		Object value = tableModel.getValueAt(i, column);
		createCell(row, column, value);
	    }
	}
	if (totalRow != -1) {
	    Row row = sheet.createRow(nextRowIdx++);
	    for (int column = 0; column < tableModel.getColumnCount(); column++) {
		Object value = tableModel.getValueAt(totalRow, column);
		createCell(row, column, value);
	    }
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
			.getFormat("d/m/yyyy"));
		sheet.setDefaultColumnStyle(column, cellStyle);
	    }
	    row.createCell(column).setCellValue((Date) value);
	} else if (value instanceof Number) {
	    Number doubleValue = (Number) value;
	    row.createCell(column).setCellValue(doubleValue.doubleValue());
	} else if (value instanceof Boolean) {
	    row.createCell(column).setCellValue((Boolean) value ? "S�" : "No");
	} else {
	    throw new AssertionError("This should never happen");
	}
    }

    private void writeTotal(String query) {
	ConnectionWrapper con = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	String q = query
		+ filters.getWhereClauseFiltersForAgregados(element, true);
	DefaultTableModel table = con.execute(q);
	Object total = table.getValueAt(0, getColumnNames().length - 1);
	if ((total != null) && !total.toString().equals("0")) {
	    writeRows(table);
	    nextRowIdx += 2;
	}
    }

    private void writeToDisk(String outputFile) throws IOException {
	FileOutputStream fileOut = new FileOutputStream(outputFile);
	this.wb.write(fileOut);
	fileOut.close();
    }

}
