package es.icarto.gvsig.extgex.queries;

import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.iver.andami.messages.NotificationManager;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.icarto.gvsig.commons.queries.CSVReport;
import es.icarto.gvsig.commons.queries.XLSReport;
import es.icarto.gvsig.commons.utils.FileUtils;

public class QueriesOuputWidget {

    public static final String PDF = "PDF";
    public static final String CSV = "CSV";
    public static final String SCREEN = "SCREEN";
    public static final String HTML = "HTML";
    public static final String RTF = "RTF";
    public static final String EXCEL = "EXCEL";
    private final JRadioButton pdfRB;
    private final JRadioButton excelRB;
    private final ButtonGroup buttonGroup;

    public QueriesOuputWidget(FormPanel formPanel, String... formats) {
	pdfRB = (JRadioButton) formPanel.getComponentByName("pdf");
	pdfRB.setActionCommand(QueriesOuputWidget.PDF);
	pdfRB.setSelected(true);
	excelRB = (JRadioButton) formPanel.getComponentByName("excel");
	excelRB.setActionCommand(QueriesOuputWidget.EXCEL);

	buttonGroup = new ButtonGroup();
	buttonGroup.add(excelRB);
	buttonGroup.add(pdfRB);
    }

    public QueriesOuputWidget() {
	pdfRB = null;
	excelRB = null;
	buttonGroup = null;
    }

    public File toPDF(ResultTableModel table, String[] filters) {
	SaveFileDialog sfd = new SaveFileDialog("Archivos PDF", "pdf");
	sfd.setAskForOverwrite(true);
	File f = sfd.showDialog();
	if (f != null && table.getRowCount() > 0) {
	    String filename = f.getAbsolutePath();
	    new Report(Report.PDF, filename, table, filters);
	}
	return f;
    }

    private File toCSV(ResultTableModel table, final String[] filters) {

	SaveFileDialog sfd = new SaveFileDialog("CSV files", "csv");
	sfd.setAskForOverwrite(true);
	File f = sfd.showDialog();
	if (f != null && table.getRowCount() > 0) {
	    new CSVReport(f.getAbsolutePath(), table, table.getQueryFilters());
	}
	return f;
    }

    public void toScreen(ResultTableModel table, String[] filters) {
	QueriesResultPanel resultPanel = new QueriesResultPanel(table, filters);
	resultPanel.open();
    }

    public File toHtml(ResultTableModel table, String[] filters) {
	SaveFileDialog sfd = new SaveFileDialog("HTML files", "html", "htm");
	sfd.setAskForOverwrite(true);
	File f = sfd.showDialog();
	if (f != null && table.getRowCount() > 0) {
	    if (FileUtils.write(table.getHTML(), f)) {
		NotificationManager.showMessageError("error_saving_file", null);
	    }
	}
	return f;
    }

    public File toRTF(ResultTableModel table, String[] filters) {
	SaveFileDialog sfd = new SaveFileDialog("RTF files", "rtf");
	sfd.setAskForOverwrite(true);
	File f = sfd.showDialog();
	if (f != null && table.getRowCount() > 0) {
	    String filename = f.getAbsolutePath();
	    new Report(Report.RTF, filename, table, filters);
	}
	return f;
    }

    public File toXLSX(ResultTableModel table, final String[] filters) {
	SaveFileDialog sfd = new SaveFileDialog("Archivos Excel", "xls");
	sfd.setAskForOverwrite(true);
	File f = sfd.showDialog();
	if (f != null && table.getRowCount() > 0) {
	    String filename = f.getAbsolutePath();
	    new XLSReport(filename, table, table.getQueryFilters());
	}
	return f;
    }

    public File to(ResultTableModel table, String[] filters) {
	String sel = buttonGroup.getSelection().getActionCommand();
	return to(sel, table, filters);
    }

    public File to(String sel, ResultTableModel table, String[] filters) {
	File file = null;
	if (sel.equals(PDF)) {
	    file = toPDF(table, filters);
	} else if (sel.equals(CSV)) {
	    file = toCSV(table, filters);
	} else if (sel.equals(SCREEN)) {
	    toScreen(table, filters);
	} else if (sel.equals(HTML)) {
	    file = toHtml(table, filters);
	} else if (sel.equals(RTF)) {
	    file = toRTF(table, filters);
	} else if (sel.equals(EXCEL)) {
	    file = toXLSX(table, filters);
	}
	return file;
    }
}
