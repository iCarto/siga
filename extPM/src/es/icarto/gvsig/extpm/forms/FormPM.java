package es.icarto.gvsig.extpm.forms;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.extpm.forms.filesLink.NavTableComponentsFilesLinkButton;
import es.icarto.gvsig.extpm.forms.reports.NavTableComponentsPrintButton;
import es.icarto.gvsig.extpm.preferences.Preferences;
import es.icarto.gvsig.extpm.utils.managers.ToggleEditingManager;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormPM extends AbstractForm {

    private FormPanel form;
    private final FLyrVect layer;
    private final boolean newRegister;
    private final int insertedRow;

    private static ArrayList<String> parcelasAfectadas;

    // WIDGETS
    private JButton editParcelasButton;
    private JComboBox area;
    private JTextField fecha;
    private JTextField numeroPM;
    private JComboBox municipio;
    private JComboBox parroquia;

    EditParcelasAfectadasListener editParcelasAfectadasListener;
    CalculatePMNumberListener calculatePMNumberListener;
    UpdateParroquiaListener updateParroquiaListener;

    public FormPM(FLyrVect layer, boolean newRegister, int insertedRow) {
	super(layer);
	this.layer = layer;
	this.newRegister = newRegister;
	this.insertedRow = insertedRow;
	initWindow();
	initWidgets();
	addNewButtonsToActionsToolBar();
	parcelasAfectadas = new ArrayList<String>();
    }

    public static ArrayList<String> getParcelasAfectadas() {
	return parcelasAfectadas;
    }

    public static void setParcelasAfectadas (ArrayList<String> parcelasAfectadasByPM) {
	if (parcelasAfectadas == null) {
	    parcelasAfectadas = new ArrayList<String>();
	}
	parcelasAfectadas = parcelasAfectadasByPM;
    }

    private void addNewButtonsToActionsToolBar() {
	JPanel actionsToolBar = this.getActionsToolBar();
	NavTableComponentsFilesLinkButton ntFilesLinkButton = new NavTableComponentsFilesLinkButton();
	NavTableComponentsPrintButton ntPrintButton = new NavTableComponentsPrintButton();
	JButton filesLinkB = ntFilesLinkButton.getFilesLinkButton(layer,
		this);
	JButton printReportB = ntPrintButton.getPrintButton(this);
	if (filesLinkB != null && printReportB != null) {
	    actionsToolBar.add(filesLinkB);
	    actionsToolBar.add(printReportB);
	}
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	HashMap<String, JComponent> widgets = getWidgetComponents();

	editParcelasAfectadasListener = new EditParcelasAfectadasListener();
	calculatePMNumberListener = new CalculatePMNumberListener();
	updateParroquiaListener = new UpdateParroquiaListener();

	numeroPM = (JTextField) widgets.get(Preferences.PM_FORM_WIDGET_PM_NUMBER);
	numeroPM.setEnabled(false);

	parroquia = (JComboBox) widgets.get(Preferences.PM_FORM_WIDGET_PARROQUIA);

	editParcelasButton = (JButton) form.getComponentByName(Preferences.PM_FORM_WIDGET_PARCELAS_BUTTON);
	editParcelasButton.addActionListener(editParcelasAfectadasListener);

	area = (JComboBox) widgets.get(Preferences.PM_FORM_WIDGET_AREA);
	area.addActionListener(calculatePMNumberListener);

	fecha = (JTextField) widgets.get(Preferences.PM_FORM_WIDGET_FECHA);
	fecha.addKeyListener(calculatePMNumberListener);

	municipio = (JComboBox) widgets.get(Preferences.PM_FORM_WIDGET_MUNICIPIO);
	municipio.addActionListener(updateParroquiaListener);

    }

    @Override
    protected void removeListeners() {
	super.removeListeners();

	editParcelasButton.removeActionListener(editParcelasAfectadasListener);
	area.removeActionListener(calculatePMNumberListener);
	fecha.removeKeyListener(calculatePMNumberListener);
	municipio.removeActionListener(updateParroquiaListener);
    }

    @Override
    public String getXMLPath() {
	return PluginServices.getPluginServices("es.icarto.gvsig.extpm")
	.getClassLoader()
	.getResource(Preferences.XML_ORMLITE_RELATIVE_PATH).getPath();
    }

    private void initWindow() {
	viewInfo.setHeight(830);
	viewInfo.setWidth(700);
	viewInfo.setTitle(Preferences.PM_FORM_TITLE);
    }

    @Override
    protected void fillSpecificValues() {
	parcelasAfectadas.clear();
	PreparedStatement statement;
	try {
	    // Parcelas afected by this PM File
	    String parcelasQuery = "SELECT " + Preferences.FINCAS_PM_FIELD_IDFINCA +
	    " FROM " + Preferences.FINCAS_PM_TABLENAME +
	    " WHERE " + Preferences.FINCAS_PM_FIELD_NUMEROPM + " = '" + numeroPM.getText() + "'";
	    statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(parcelasQuery);
	    statement.execute();
	    ResultSet parcelasRs = statement.getResultSet();
	    while (parcelasRs.next()) {
		parcelasAfectadas.add(parcelasRs.getString(1));
	    }

	    // Parroquia
	    String parroquiaQuery = "SELECT " + Preferences.PM_FIELD_PARROQUIA +
	    " FROM " + Preferences.PM_TABLENAME +
	    " WHERE " + Preferences.PM_FIELD_NUMEROPM + " = '" + numeroPM.getText() + "'";
	    statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(parroquiaQuery);
	    statement.execute();
	    ResultSet parroquiaRs = statement.getResultSet();
	    while (parroquiaRs.next()) {
		if (parroquiaRs.getString(1) != null) {
		    parroquia.addItem(parroquiaRs.getString(1));
		    parroquia.setSelectedItem(parroquiaRs.getString(1));
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	// Fecha (how should be written)
	if (fecha.getText().equalsIgnoreCase("")) {
	    fecha.setText("dd/mm/aaaa");
	    fecha.setForeground(Color.GRAY);
	}
    }

    @Override
    public boolean saveRecord() {
	PreparedStatement statement;
	String query = null;
	try {
	    for (String idParcela : parcelasAfectadas) {
		query = "INSERT INTO audasa_pm.fincas_pm "
		    + "VALUES (" + "'" + idParcela + "', '" + numeroPM.getText() + "')";

		statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
		statement.execute();
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return super.saveRecord();
    }

    @Override
    protected void enableSaveButton(boolean bool) {
	if (!isChangedValues()) {
	    saveB.setEnabled(false);
	} else {
	    saveB.setEnabled(bool);
	}
    }

    @Override
    public FormPanel getFormBody() {
	if (form == null) {
	    form = new FormPanel(Preferences.PM_FORM_FILE);
	}
	return form;
    }

    @Override
    public Logger getLoggerName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void windowClosed() {
	super.windowClosed();
	ToggleEditingManager tem = new ToggleEditingManager();
	if (layer.isEditing()) {
	    tem.stopEditing(layer, false);
	}
    }

    /**
     * This method calculates the PM Number. It depends on the selected values in
     * area (JComboBox) and fecha (JTextField)
     * 
     * PM Number is generated like this: "PM.Year.Area.ID(serial)"
     * Ej: "PM.2012.N.001"
     * 
     * @return PM Number
     */
    private String calculatePMNumber() {
	String area;
	String year = "";
	String currentPmNumber;

	String date;
	date = fecha.getText();
	if (date.contains("/")) {
	    String[] dateArray = date.split("/");
	    if (dateArray.length == 3) {
		year = dateArray[2];
	    }
	}

	String areaSelectedValue = this.area.getSelectedItem().toString();
	if (areaSelectedValue.equalsIgnoreCase("Norte")) {
	    area = "N";
	} else if (areaSelectedValue.equalsIgnoreCase("Sur")) {
	    area = "S";
	} else {
	    area = "";
	}

	String id = "";
	currentPmNumber = numeroPM.getText();
	if (!newRegister && currentPmNumber.contains(".")) {
	    String[] pmNumberArray = currentPmNumber.split("\\.");
	    if (pmNumberArray.length ==4 && !year.equals("") &&
		    !area.equals("")) {
		id = pmNumberArray[3];
		return "PM" + "." + year +"." + area + "." + id;
	    }
	    return currentPmNumber;
	}

	return "PM" + "." + year +"." + area + "." + getID();
    }

    private String getID() {
	try {
	    SelectableDataSource recordset = layer.getRecordset();
	    int columnIndex = recordset.getFieldIndexByName("gid");
	    ArrayList<Integer> pmID = new ArrayList<Integer>();
	    for (int rowIndex=0; rowIndex<recordset.getRowCount(); rowIndex++) {
		String id = recordset.getFieldValue(rowIndex, columnIndex).toString();
		if (!id.equals("")) {
		    pmID.add(Integer.parseInt(id));
		}
	    }
	    Arrays.sort(pmID.toArray(new Integer[] {0}));
	    int biggerPmID = pmID.get(pmID.size()-1);
	    return String.format("%1$03d", biggerPmID+1);
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
	return null;
    }

    private void setNumeroPMValue() {
	String calculatePMNumber = calculatePMNumber();
	numeroPM.setText(calculatePMNumber);
	getFormController().setValue(Preferences.PM_FORM_WIDGET_PM_NUMBER, calculatePMNumber);
    }

    public class CalculatePMNumberListener implements ActionListener, KeyListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    setNumeroPMValue();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	    setNumeroPMValue();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

    }

    public class EditParcelasAfectadasListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    SubFormPMParcelasAfectadas subForm = new SubFormPMParcelasAfectadas(layer, insertedRow);
	    PluginServices.getMDIManager().addWindow(subForm);
	}

    }

    private boolean isFillingValuesFormPM() {
	return ((AbstractForm) this).isFillingValues();
    }

    public class UpdateParroquiaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    if (!isFillingValues()) {
		parroquia.removeAllItems();
		parroquia.addItem(" ");
		PreparedStatement statement;
		String query ;
		try {
		    query = "SELECT " + Preferences.PARROQUIAS_FIELD_NAME +
		    " FROM " + Preferences.PARROQUIAS_TABLENAME +
		    " WHERE " + Preferences.PARROQUIAS_FIELD_CODIGO +
		    " = " + "(SELECT " + Preferences.MUNICIPIOS_FIELD_CODIGO +
		    " FROM " + Preferences.MUNICIPIOS_TABLENAME +
		    " WHERE " + Preferences.MUNICIPIOS_FIELD_NAME +
		    " = " + "'" + municipio.getSelectedItem() + "');";
		    statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
		    statement.execute();
		    ResultSet rs = statement.getResultSet();
		    ArrayList<String> parroquias = new ArrayList<String>();
		    while (rs.next()) {
			parroquias.add(rs.getString(1));
		    }
		    rs.close();
		    for (String parroquia_name : parroquias) {
			parroquia.addItem(parroquia_name);
		    }
		} catch (SQLException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

}
