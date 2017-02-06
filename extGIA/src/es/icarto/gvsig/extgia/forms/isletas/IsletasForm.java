package es.icarto.gvsig.extgia.forms.isletas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComboBox;
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
public class IsletasForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "isletas";

    JComboBox tipoIsletaWidget;
    JTextField numeroIsletaWidget;
    JComboBox baseContratistaWidget;
    JTextField isletaIDWidget;
    CalculateComponentValue isletaid;

    public IsletasForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosVegetacionColNames,
		DBFieldNames.trabajosVegetacionColAlias,
		DBFieldNames.trabajosColWidths, this,
		IsletasTrabajosSubForm.class));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		IsletasReconocimientosSubForm.class));
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	isletaIDWidget = (JTextField) widgets.get(DBFieldNames.ID_ISLETA);

	isletaid = new IsletaCalculateIDValue(this, getWidgetComponents(),
		DBFieldNames.ID_ISLETA, DBFieldNames.NUMERO_ISLETA,
		DBFieldNames.BASE_CONTRATISTA);
	isletaid.setListeners();
    }

    @Override
    protected void removeListeners() {
	isletaid.removeListeners();
	super.removeListeners();
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_isleta")) {
	    if (isletaIDWidget.getText() != "") {
		String query = "SELECT id_isleta FROM audasa_extgia.isletas "
			+ " WHERE id_isleta = '" + isletaIDWidget.getText()
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
	return Elements.Isletas;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_isleta";
    }

    @Override
    public String getElementIDValue() {
	return isletaIDWidget.getText();
    }

}
