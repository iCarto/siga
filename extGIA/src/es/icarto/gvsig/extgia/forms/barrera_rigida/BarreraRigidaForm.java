package es.icarto.gvsig.extgia.forms.barrera_rigida;

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
public class BarreraRigidaForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "barrera_rigida";

    JTextField barreraRigidaIDWidget;
    CalculateComponentValue barreraRigidaid;
    CalculateComponentValue barreraRigidaCodigo;

    public BarreraRigidaForm(FLyrVect layer) {
        super(layer);

        addTableHandler(new GIAAlphanumericTableHandler(getTrabajosDBTableName(), getWidgets(), getElementID(),
                DBFieldNames.trabajosVegetacionColNames, DBFieldNames.trabajosVegetacionColAlias,
                DBFieldNames.trabajosColWidths, this, BarreraRigidaTrabajosSubForm.class));

        addTableHandler(new GIAAlphanumericTableHandler(getReconocimientosDBTableName(), getWidgets(), getElementID(),
                DBFieldNames.reconocimientosColNames, DBFieldNames.reconocimientosColAlias, null, this,
                BarreraRigidaReconocimientosSubForm.class));
    }

    @Override
    protected void setListeners() {
        super.setListeners();
        Map<String, JComponent> widgets = getWidgets();

        barreraRigidaIDWidget = (JTextField) widgets.get(DBFieldNames.ID_BARRERA_RIGIDA);

        barreraRigidaid = new BarreraRigidaCalculateIDValue(this, getWidgetComponents(),
                DBFieldNames.ID_BARRERA_RIGIDA, DBFieldNames.NUMERO_BARRERA_RIGIDA, DBFieldNames.BASE_CONTRATISTA,
                DBFieldNames.TRAMO);
        barreraRigidaid.setListeners();

        barreraRigidaCodigo = new BarreraRigidaCalculateCodigo(this, getWidgetComponents(), DBFieldNames.CODIGO,
                DBFieldNames.TIPO, DBFieldNames.METODO_CONSTRUCTIVO, DBFieldNames.PERFIL);
        barreraRigidaCodigo.setListeners();
    }

    @Override
    protected boolean validationHasErrors() {
        if (this.getFormController().getValuesChanged().containsKey("id_barrera_rigida")) {
            if (barreraRigidaIDWidget.getText() != "") {
                String query = "SELECT id_barrera_rigida FROM audasa_extgia.barrera_rigida "
                        + " WHERE id_barrera_rigida = '" + barreraRigidaIDWidget.getText() + "';";
                PreparedStatement statement = null;
                Connection connection = DBSession.getCurrentSession().getJavaConnection();
                try {
                    statement = connection.prepareStatement(query);
                    statement.execute();
                    ResultSet rs = statement.getResultSet();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(null, "El ID está en uso, por favor, escoja otro.", "ID en uso",
                                JOptionPane.WARNING_MESSAGE);
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
        return Elements.Barrera_Rigida;
    }

    @Override
    protected boolean hasSentido() {
        return true;
    }

    @Override
    public String getElementID() {
        return "id_barrera_rigida";
    }

    @Override
    public String getElementIDValue() {
        return barreraRigidaIDWidget.getText();
    }
}