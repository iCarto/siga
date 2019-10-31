package es.icarto.gvsig.extgia.forms.ramales;

import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.exceptions.layers.ReloadLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.enlaces.EnlacesForm;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

@SuppressWarnings("serial")
public class RamalesForm extends AbstractFormWithLocationWidgets {

    private static final Logger logger = Logger.getLogger(RamalesForm.class);

    public static final String[] colNames = { "gid", "ramal", "sentido", "direccion", "longitud" };
    public static final String[] colAlias = { "ID Ramal", "Nombre Ramal", "Sentido", "Dirección", "Longitud" };

    public RamalesForm(FLyrVect layer) {
        super(layer);
    }

    public static final String TABLENAME = "ramales";

    @Override
    public Elements getElement() {
        return Elements.Ramales;
    }

    @Override
    public String getElementID() {
        return "gid";
    }

    @Override
    public String getElementIDValue() {
        JTextField idWidget = (JTextField) getWidgets().get(getElementID());
        return idWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
        return null;
    }

    @Override
    public JTable getTrabajosJTable() {
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
    public boolean saveRecord() throws StopWriterVisitorException {
        boolean flag = super.saveRecord();
        if (!flag) {
            // Ha habido algún problema en el guardado
            return flag;
        }

        FLyrVect enlacesLyr = new TOCLayerManager().getLayerByName(EnlacesForm.TOCLAYERNAME);
        if (enlacesLyr == null) {
            return flag;
        }

        try {
            enlacesLyr.reload();
        } catch (ReloadLayerException e) {
            logger.error(e.getStackTrace(), e);
            return false;
        }

        IWindow[] allWindows = PluginServices.getMDIManager().getAllWindows();
        for (IWindow iWindow : allWindows) {
            if (iWindow instanceof EnlacesForm) {
                EnlacesForm enlacesForm = (EnlacesForm) iWindow;
                enlacesForm.onPositionChange(null);
            }
        }
        return true;
    }
}
