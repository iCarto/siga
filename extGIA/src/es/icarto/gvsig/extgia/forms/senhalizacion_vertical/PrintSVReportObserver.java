package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.layers.FBitSet;

import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.icarto.gvsig.commons.queries.FinalActions;
import es.icarto.gvsig.commons.queries.ReportValidationResult;
import es.icarto.gvsig.siga.PreferencesPage;
import es.icarto.gvsig.siga.forms.reports.PrintReportAction;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class PrintSVReportObserver implements ActionListener {

    private static final Logger logger = Logger
	    .getLogger(PrintSVReportObserver.class);

    private final AbstractNavTable dialog;
    private File outputFile;
    private final String reportPath;
    private final String idField;
    private final String idValue;

    private int idIdx;

    private final String signalsFolderPath;

    public PrintSVReportObserver(AbstractNavTable dialog, String reportPath, String idField, String idValue) {
	this.dialog = dialog;
	this.reportPath = reportPath;
	this.idField = idField;
	this.idValue = idValue;
	signalsFolderPath = SymbologyFactory.SymbolLibraryPath
		    + File.separator + "senhales" + File.separator;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

	try {
	    idIdx = dialog.getRecordset().getFieldIndexByName(idField);
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}

	FBitSet selection = dialog.getRecordset().getSelection();
	int size = selection.cardinality();
	String msg = "";
	if (size == 0) {
	    msg = "Se va a imprimir la ficha actual";
	    Object[] options = {
		    PluginServices.getText(this, "optionPane_yes"),
		    PluginServices.getText(this, "optionPane_no") };
	    int showConfirmDialog = JOptionPane.showOptionDialog(dialog, msg,
		    null, JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

	    if (showConfirmDialog != JOptionPane.OK_OPTION) {
		return;
	    }

	    SaveFileDialog sfd = new SaveFileDialog("PDF files", "pdf");
	    sfd.setAskForOverwrite(true);
	    outputFile = sfd.showDialog();

	    if (outputFile != null) {

		PrintReportAction reportAction = new PrintReportAction();
		reportAction.print(outputFile.getAbsolutePath(),
			getReportPath(), getReportParameters());

		Object[] reportGeneratedOptions = { "Ver informe", "Cerrar" };
		int m = JOptionPane.showOptionDialog(
			null,
			"Informe generado con éxito en: \n" + "\""
				+ outputFile.getAbsolutePath() + "\"", null,
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null,
			reportGeneratedOptions, reportGeneratedOptions[1]);

		if (m == JOptionPane.OK_OPTION) {
		    Desktop d = Desktop.getDesktop();
		    try {
			d.open(outputFile);
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }

	} else {
	    msg = "Se van a imprimir las " + size
		    + " fichas seleccionadas\n¿Desea continuar?";
	    Object[] options = {
		    PluginServices.getText(this, "optionPane_yes"),
		    PluginServices.getText(this, "optionPane_no") };
	    int showConfirmDialog = JOptionPane.showOptionDialog(dialog, msg,
		    null, JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

	    if (showConfirmDialog != JOptionPane.OK_OPTION) {
		return;
	    }
	    JFileChooser jFileChooser = new JFileChooser(
		    System.getenv("user.dir"));
	    jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    int showDialog = jFileChooser.showDialog(dialog, "Aceptar");
	    if (showDialog == JFileChooser.APPROVE_OPTION) {
		final File selectedFile = jFileChooser.getSelectedFile();
		if (selectedFile != null) {
		    if (!selectedFile.isDirectory()) {
			JOptionPane.showMessageDialog(dialog,
				"El directorio seleccionado no existe", null,
				JOptionPane.ERROR_MESSAGE, null);
			return;
		    }
		    for (int i = selection.nextSetBit(0); i >= 0; i = selection
			    .nextSetBit(i + 1)) {
			HashMap<String, Object> parameters = new HashMap<String, Object>();

			try {
			    Value fieldValue = dialog.getRecordset()
				    .getFieldValue(i, idIdx);
			    int intValue = ((NumericValue) fieldValue)
				    .intValue();
			    parameters.put("SENHALIZACION_VERTICAL_QUERY",
				    intValue);
			    parameters.put("IMAGE_PATH", signalsFolderPath);
			    parameters.put("LOGO_PATH", PreferencesPage.LOGO_PATH);
			    PrintReportAction reportAction = new PrintReportAction();
			    reportAction.print(selectedFile.getAbsolutePath()
				    + "/señalizacion_vertical_" + intValue
				    + ".pdf", getReportPath(), parameters);
			} catch (ReadDriverException e) {
			    logger.error(e.getStackTrace(), e);
			}
		    }
		    FinalActions fa = new FinalActions(selectedFile, new ReportValidationResult(""));
		    fa.openFolder();
		}
	    }

	}

    }

    private String getReportPath() {
	return this.reportPath;
    }

    private HashMap<String, Object> getReportParameters() {
	HashMap<String, Object> parameters = new HashMap<String, Object>();
	parameters.put("SENHALIZACION_VERTICAL_QUERY",
		Integer.parseInt(idValue));
	parameters.put("LOGO_PATH", PreferencesPage.LOGO_PATH);
	parameters.put("IMAGE_PATH", signalsFolderPath);
	return parameters;
    }
}