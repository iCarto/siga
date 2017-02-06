package es.icarto.gvsig.extgia.forms.pasos_mediana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class PasosMedianaForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "pasos_mediana";

    JTextField pasoMedianaIDWidget;
    CalculateComponentValue pasoMedianaid;

    public PasosMedianaForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		PasosMedianaReconocimientosSubForm.class));
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	pasoMedianaIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_PASO_MEDIANA);

	pasoMedianaid = new PasosMedianaCalculateIDValue(this,
		getWidgetComponents(), DBFieldNames.ID_PASO_MEDIANA,
		DBFieldNames.TRAMO, DBFieldNames.PK);
	pasoMedianaid.setListeners();

	setIdWhenPkIsAutomaticallyUpdated();

    }

    /**
     * Workaround. When pk field is not typed id is not update. Maybe
     * Calculation class should use DocumentListener for textfield instead of
     * KeyListener
     */
    private void setIdWhenPkIsAutomaticallyUpdated() {

	JTextField textField = (JTextField) getFormBody().getComponentByName(
		DBFieldNames.PK);
	textField.getDocument().addDocumentListener(new DocumentListener() {
	    @Override
	    public void changedUpdate(DocumentEvent e) {
		doIt();
	    }

	    @Override
	    public void removeUpdate(DocumentEvent e) {
		doIt();
	    }

	    @Override
	    public void insertUpdate(DocumentEvent e) {
		doIt();
	    }

	    private void doIt() {
		if (!isFillingValues()) {
		    pasoMedianaid.setValue(true);
		}
	    }
	});
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_paso_mediana")) {
	    if (pasoMedianaIDWidget.getText() != "") {
		String query = "SELECT id_paso_mediana FROM audasa_extgia.pasos_mediana "
			+ " WHERE id_paso_mediana = '"
			+ pasoMedianaIDWidget.getText() + "';";
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
    public Elements getElement() {
	return Elements.Pasos_Mediana;
    }

    @Override
    protected boolean hasSentido() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getElementID() {
	return "id_paso_mediana";
    }

    @Override
    public String getElementIDValue() {
	return pasoMedianaIDWidget.getText();
    }

}
