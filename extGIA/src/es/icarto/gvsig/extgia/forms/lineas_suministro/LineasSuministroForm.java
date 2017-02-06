package es.icarto.gvsig.extgia.forms.lineas_suministro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class LineasSuministroForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "lineas_suministro";

    JTextField lineaSuministroIDWidget;
    CalculateComponentValue lineaSuministroid;

    public LineasSuministroForm(FLyrVect layer) {
	super(layer);

	// int[] trabajoColumnsSize = { 1, 30, 90, 70, 200 };
	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosWhitoutIndexColNames,
		DBFieldNames.reconocimientosWhitoutIndexColAlias, null, this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (lineaSuministroIDWidget.getText().isEmpty()) {
	    lineaSuministroid = new LineasSuministroCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    lineaSuministroid.setValue(true);
	}

	// Embebed Tables
	repaint();
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();
	lineaSuministroIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_LINEAS_SUMINISTRO);

	lineaSuministroid = new LineasSuministroCalculateIDValue(this,
		getWidgetComponents(), getElementID(), getElementID());
	lineaSuministroid.setListeners();

    }

    @Override
    protected void removeListeners() {
	lineaSuministroid.removeListeners();
	super.removeListeners();
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_linea_suministro")) {
	    if (lineaSuministroIDWidget.getText() != "") {
		String query = "SELECT id_linea_suministro FROM audasa_extgia.lineas_suministro "
			+ " WHERE id_linea_suministro = '"
			+ lineaSuministroIDWidget.getText() + "';";
		PreparedStatement statement = null;
		Connection connection = DBSession.getCurrentSession()
			.getJavaConnection();
		try {
		    statement = connection.prepareStatement(query);
		    statement.execute();
		    ResultSet rs = statement.getResultSet();
		    if (rs.next()) {
			JOptionPane.showMessageDialog(null,
				"El ID está en uso, por favor, escoja otro.",
				"ID en uso", JOptionPane.WARNING_MESSAGE);
			return true;
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}
	return super.validationHasErrors();
    }

    @Override
    public Elements getElement() {
	return Elements.Lineas_Suministro;
    }

    @Override
    public String getElementID() {
	return "id_linea_suministro";
    }

    @Override
    public String getElementIDValue() {
	return lineaSuministroIDWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public JTable getTrabajosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

}
