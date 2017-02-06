package es.icarto.gvsig.extgia.forms.lecho_frenado;

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
public class LechoFrenadoForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "lecho_frenado";

    JTextField lechoFrenadoIDWidget;
    CalculateComponentValue lechoFrenadoid;

    public LechoFrenadoForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		LechoFrenadoReconocimientosSubForm.class));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (lechoFrenadoIDWidget.getText().isEmpty()) {
	    lechoFrenadoid = new LechoFrenadoCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    lechoFrenadoid.setValue(true);
	}
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey(getElementID())) {
	    if (getElementIDValue() != "") {
		String query = "SELECT id_lecho_frenado FROM audasa_extgia.lecho_frenado "
			+ " WHERE id_lecho_frenado = '"
			+ getElementIDValue()
			+ "';";
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
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	lechoFrenadoIDWidget = (JTextField) widgets.get(getElementID());
    }

    @Override
    public Elements getElement() {
	return Elements.Lecho_Frenado;
    }

    @Override
    public String getElementID() {
	return "id_lecho_frenado";
    }

    @Override
    public String getElementIDValue() {
	return lechoFrenadoIDWidget.getText();
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
