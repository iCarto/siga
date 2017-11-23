package es.icarto.gvsig.extgia;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class ToolBarBatchTrabajosExtension extends AbstractExtension {

    private static final Logger logger = Logger.getLogger(ToolBarBatchTrabajosExtension.class);
    private final List<FLyrVect> searchableLayers = new ArrayList<FLyrVect>();

    private final List<String> layers = Arrays.asList(new String[] { "Barrera_Rigida", "Isletas", "Taludes",
    "Senhalizacion_Vertical" });

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), msg, "Aviso",
                JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void execute(String actionCommand) {
        FLyrVect layer = searchableLayers.get(0);
        if (isSelectionEmpty(layer)) {
            showWarning("Debe tener registros seleccionados para añadir trabajos en lote");
            return;
        }

        LaunchGIAForms.callBatchTrabajosSubFormDependingOfElement(layer.getName(), layer.getName().toLowerCase()
                + "_trabajos", null);
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

    @Override
    public boolean isEnabled() {
        View view = getView();
        if (view == null) {
            return false;
        }

        FLayer[] actives = view.getMapControl().getMapContext().getLayers().getActives();
        searchableLayers.clear();
        for (FLayer layer : actives) {
            if (layer instanceof FLyrVect) {
                if (layers.contains(layer.getName())) {
                    searchableLayers.add((FLyrVect) layer);
                }
            }
        }
        return searchableLayers.size() == 1;
    }

}
