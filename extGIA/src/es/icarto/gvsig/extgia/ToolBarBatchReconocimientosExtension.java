package es.icarto.gvsig.extgia;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.icarto.gvsig.extgia.preferences.Elements;

public class ToolBarBatchReconocimientosExtension extends AbstractExtension {

    private static final Logger logger = Logger.getLogger(ToolBarBatchReconocimientosExtension.class);

    private FLyrVect layer = null;

    @Override
    public void execute(String actionCommand) {
        if (isSelectionEmpty(layer)) {
            showWarning("Debe tener registros seleccionados para añadir reconocimientos en lote");
            return;
        }
        String name = layer.getName();
        String rec = Elements.valueOf(name).batchReconocimientosBasicName;
        LaunchGIAForms.callBatchReconocimientosSubFormDependingOfElement(name, rec + ".jfrm", rec);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), msg, "Aviso",
                JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public boolean isEnabled() {
        layer = getValidActiveLayer();
        return layer != null;
    }

    private boolean isSelectionEmpty(FLyrVect layer) {
        try {
            SelectableDataSource recordset = layer.getRecordset();
            if (recordset.getSelection().cardinality() < 1) {
                return true;
            }
        } catch (ReadDriverException e) {
            logger.error(e.getStackTrace(), e);
        }
        return false;
    }

    private FLyrVect getValidActiveLayer() {
        View view = getView();
        if (view == null) {
            return null;
        }
        FLayer[] actives = view.getMapControl().getMapContext().getLayers().getActives();
        if ((actives.length != 1) || (!(actives[0] instanceof FLyrVect))) {
            return null;
        }
        FLyrVect activeLayer = (FLyrVect) actives[0];
        Elements e = isGIALayerName(activeLayer.getName());
        if (e != null && e.batchReconocimientosBasicName != null) {
            return activeLayer;
        }
        return null;
    }

    private Elements isGIALayerName(String layerName) {
        for (Elements e : Elements.values()) {
            if (e.layerName.equals(layerName)) {
                return e;
            }
        }
        return null;
    }

}
