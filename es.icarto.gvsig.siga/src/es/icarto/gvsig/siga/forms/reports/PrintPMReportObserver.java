package es.icarto.gvsig.siga.forms.reports;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.icarto.gvsig.commons.imagefilechooser.ImageFileChooser;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class PrintPMReportObserver implements ActionListener {

    private File inputImageFile;
    private File outputFile;
    private final String reportPath;
    private final String tableName;
    private final String idField;
    private final AbstractForm form;

    public PrintPMReportObserver(String reportPath, String tableName, String idField, AbstractForm form) {
	this.reportPath = reportPath;
	this.tableName = tableName;
	this.idField = idField;
	this.form = form;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	SaveFileDialog sfd = new SaveFileDialog("PDF files", "pdf");
	sfd.setAskForOverwrite(true);
	outputFile = sfd.showDialog();

	if (outputFile != null) {
	    Object[] selectImageOptions = { "Sí, seleccionar imagen", "No" };
	    int n = JOptionPane.showOptionDialog(null,
		    "¿Desea incluir una imagen en el informe?",
		    "Incluir imagen", JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE, null, selectImageOptions,
		    selectImageOptions[1]);

	    if (n == JOptionPane.YES_OPTION) {
		ImageFileChooser ifs = new ImageFileChooser();
		inputImageFile = ifs.showDialog();
	    }

	    PrintReportAction reportAction = new PrintReportAction();
	    reportAction.print(outputFile.getAbsolutePath(), this.reportPath,
		    getReportParameters());

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
    }

    private HashMap<String, Object> getReportParameters() {
	HashMap<String, Object> parameters = new HashMap<String, Object>();
	parameters.put("PM_QUERY_WHERE", getPMFileID());
	parameters.put("LOGO_PATH", PreferencesPage.LOGO_PATH);
	if (inputImageFile != null) {
	    parameters.put("MAIN_IMAGE_PATH", inputImageFile.getAbsolutePath());
	}
	return parameters;
    }

    private int getPMFileID() {
	int id;
	String postgresIdField;
	if (tableName.contains("audasa_pm")) {
	    postgresIdField = "id";
	} else {
	    postgresIdField = "gid";
	}
	PreparedStatement statement;
	JTextField numeroPM = (JTextField) form.getWidgets().get(idField);
	String idValue = numeroPM.getText();
	String query = "SELECT " + postgresIdField + " FROM " + tableName
		+ " WHERE " + idField + "= '" + idValue  + "';";
	try {
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    rs.next();
	    id = rs.getInt(1);
	    return id;
	} catch (SQLException e) {
	    e.printStackTrace();
	    return -1;
	}

	// long currentPosition = dialog.getPosition();
	// return Long.valueOf(currentPosition).intValue();
    }
}