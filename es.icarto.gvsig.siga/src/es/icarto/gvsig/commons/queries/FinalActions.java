package es.icarto.gvsig.commons.queries;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;

public class FinalActions {

    private static final Logger logger = Logger.getLogger(FinalActions.class);

    private final File output;
    private final ReportValidationResult reportValidationResult;


    public FinalActions(File outputFile, ReportValidationResult reportValidationResult) {
        this.output = outputFile;
        this.reportValidationResult = reportValidationResult;
    }

    public void showMessages() {
	if (reportValidationResult.isError()) {
	    JOptionPane.showMessageDialog(null, reportValidationResult.getMsg());
	} else {
	    JOptionPane.showMessageDialog(
		    null,
		    PluginServices.getText(this,
			    "reportSuccessfullyGenerated_msg")
			    + output.getAbsolutePath());
	}
    }

    public void openReport() {
        if (reportValidationResult.isError()) {
            JOptionPane.showMessageDialog(null, reportValidationResult.getMsg());
	} else {
	    showOpenReportDialog("openReport");
	}
    }

    private void showOpenReportDialog(String msg) {
	Object[] reportGeneratedOptions = { PluginServices.getText(this, msg),
		PluginServices.getText(this, "close") };
	int m = JOptionPane.showOptionDialog(null,
		PluginServices.getText(this, "reportSuccessfullyGenerated_msg")
			+ "\"" + output + "\"", null,
		JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.INFORMATION_MESSAGE, null, reportGeneratedOptions,
		reportGeneratedOptions[1]);

	if (m == JOptionPane.OK_OPTION) {
	    Desktop d = Desktop.getDesktop();
	    try {
		d.open(output);
	    } catch (IOException e1) {
		logger.error(e1.getStackTrace(), e1);
	    }
	}
    }

    public void openFolder() {
        if (reportValidationResult.isError()) {
            JOptionPane.showMessageDialog(null, reportValidationResult.getMsg());
        } else {
            showOpenReportDialog("openFolder");    
        }
    }

}
