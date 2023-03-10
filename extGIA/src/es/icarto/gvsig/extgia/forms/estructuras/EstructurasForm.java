package es.icarto.gvsig.extgia.forms.estructuras;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.forms.barrera_rigida.BarreraRigidaReconocimientosSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class EstructurasForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "estructuras";

    JTextField estructuraIDWidget;
    CalculateComponentValue estructuraid;
    private final CodigoXopa codigoXopa;

    public EstructurasForm(FLyrVect layer) {
        super(layer);

        addTableHandler(new GIAAlphanumericTableHandler(getTrabajosDBTableName(), getWidgets(), getElementID(),
                DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias, DBFieldNames.trabajosColWidths, this));
        
        addTableHandler(new GIAAlphanumericTableHandler(getReconocimientosDBTableName(), getWidgets(), getElementID(),
                DBFieldNames.reconocimientosEstructurasColNames, DBFieldNames.reconocimientosEstructurasColAlias,
                DBFieldNames.reconocimientosEstructurasColWidths, this, EstructurasReconocimientosSubForm.class));

        codigoXopa = new CodigoXopa(formBody, tramoCB);
        codigoXopa.setForm(this);
    }

    @Override
    protected void fillSpecificValues() {
        super.fillSpecificValues();

        if (estructuraIDWidget.getText().isEmpty()) {
            estructuraid = new EstructurasCalculateIDValue(this, getWidgetComponents(), getElementID(), getElementID());
            estructuraid.setValue(true);
        }
        codigoXopa.fillSpecificValues();
        repaint();
    }

    @Override
    protected void setListeners() {
        super.setListeners();
        Map<String, JComponent> widgets = getWidgets();

        estructuraIDWidget = (JTextField) widgets.get(getElementID());
        codigoXopa.setListeners();
    }

    @Override
    protected void removeListeners() {
        codigoXopa.removeListeners();
        super.removeListeners();
    }

    @Override
    public Elements getElement() {
        return Elements.Estructuras;
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

    @Override
    public String getElementID() {
        return "id_estructura";
    }

    @Override
    public String getElementIDValue() {
        return estructuraIDWidget.getText();
    }

}
