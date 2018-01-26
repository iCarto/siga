package es.icarto.gvsig.extgia.forms.tuneles;

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
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class TunelesForm extends AbstractFormWithLocationWidgets {
	
	public static final String TABLENAME = "tuneles";
	
	JTextField tunelIDWidget;
    CalculateComponentValue tunelid;

	public TunelesForm(FLyrVect layer) {
		super(layer);
	
	}

	@Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (tunelIDWidget.getText().isEmpty()) {
	    tunelid = new TunelesCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    tunelid.setValue(true);
	}
    }
    
    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	tunelIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_TUNELES);

	tunelid = new TunelesCalculateIDValue(this,
		getWidgetComponents(), getElementID(), getElementID());
	tunelid.setListeners();
    }
    
    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("gid")) {
	    if (tunelIDWidget.getText() != "") {
		String query = "SELECT gid FROM audasa_extgia.tuneles "
			+ " WHERE gid = '"
			+ tunelIDWidget.getText() + "';";
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
		return Elements.Tuneles;
	}

	@Override
	public String getElementID() {
		return "gid";
	}

	@Override
	public String getElementIDValue() {
		return tunelIDWidget.getText();
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
