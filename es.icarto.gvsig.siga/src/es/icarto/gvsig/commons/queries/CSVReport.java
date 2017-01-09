package es.icarto.gvsig.commons.queries;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.utils.SIGAFormatter;

public class CSVReport {

    protected static final String CSV_SEPARATOR = "\t";
    private FileWriter writer;

    public CSVReport(String outputFile, DefaultTableModel tableModel,
	    QueryFiltersI filters) {
	try {
	    writer = new FileWriter(outputFile);

	    writeFilters(filters);

	    writeColumnNames(tableModel);
	    writer.append("\n");

	    writeRows(tableModel);

	    writer.flush();
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    protected void writeFilters(QueryFiltersI filters) throws IOException {

	for (Field l : filters.getLocation()) {
	    writer.append(l.getLongName());
	    writer.append(CSV_SEPARATOR);
	    writer.append(l.getValue().toString());
	    writer.append("\n");
	}

	writer.append("\n");
    }

    private void writeRows(DefaultTableModel tableModel) throws IOException {
	for (int row = 0; row < tableModel.getRowCount(); row++) {
	    for (int column = 0; column < tableModel.getColumnCount(); column++) {
		Object value = tableModel.getValueAt(row, column);
		writer.append(SIGAFormatter.formatValue(value));
		writer.append(CSV_SEPARATOR);
	    }
	    writer.append("\n");
	}
    }

    private void writeColumnNames(DefaultTableModel tableModel)
	    throws IOException {
	for (int i = 0; i < tableModel.getColumnCount(); i++) {
	    writer.append(tableModel.getColumnName(i));
	    writer.append(CSV_SEPARATOR);
	}
    }

}
