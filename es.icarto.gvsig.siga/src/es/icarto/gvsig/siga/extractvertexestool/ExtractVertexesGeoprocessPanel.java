package es.icarto.gvsig.siga.extractvertexestool;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.layerOperations.SingleLayer;
import com.iver.cit.gvsig.project.documents.view.legend.CreateSpatialIndexMonitorableTask;
import com.iver.utiles.swing.threads.IMonitorableTask;

public class ExtractVertexesGeoprocessPanel extends GridBagLayoutPanel {

    private static final long serialVersionUID = 6833607794721720432L;

    protected FLayers layers;

    protected JComboBox layersComboBox;

    protected JCheckBox selectedOnlyCheckBox;

    protected JCheckBox mergeLayerVertexesCheckBox;

    protected JCheckBox alwaysAddLastVertexCheckBox;

    protected JLabel numSelectedLabel;

    private JTextField distToleranceTextField;

    public ExtractVertexesGeoprocessPanel(FLayers layers) {
        super();
        this.layers = layers;
        initialize();
    }

    protected void initialize() {
        Insets insets = new Insets(5, 5, 5, 5);
        JLabel firstLayerLab = new JLabel(PluginServices.getText(this,
                "extract_vertexes_input") + ":");
        JComboBox layersComboBox = getLayersComboBox();
        addComponent(firstLayerLab, layersComboBox, GridBagConstraints.BOTH,
                insets);
        addComponent(getSelectedOnlyCheckBox(), GridBagConstraints.BOTH, insets);

        String numSelectedText = PluginServices.getText(this,
                "extract_vertexes_input_elements") + ":";
        numSelectedLabel = new JLabel("00");
        addComponent(numSelectedText, numSelectedLabel, insets);

        addSpecificDesign();

        setBounds(0, 0, 520, 410);
    }

    protected void initSelectedItemsJCheckBox() {
        String selectedLayer = (String) layersComboBox.getSelectedItem();
        FLyrVect inputLayer = (FLyrVect) layers.getLayer(selectedLayer);
        FBitSet fBitSet = null;
        try {
            fBitSet = inputLayer.getRecordset().getSelection();
        } catch (ReadDriverException e) {
            e.printStackTrace();
        }

        if (selectedOnlyCheckBox != null) {
            if (fBitSet.cardinality() == 0) {
                selectedOnlyCheckBox.setEnabled(false);
                selectedOnlyCheckBox.setSelected(false);
            } else {
                selectedOnlyCheckBox.setEnabled(true);
                selectedOnlyCheckBox.setSelected(true);
            }
            updateNumSelectedFeaturesLabel();
        }

    }

    protected void updateNumSelectedFeaturesLabel() {
        if (selectedOnlyCheckBox != null && selectedOnlyCheckBox.isSelected()
                && numSelectedLabel != null) {
            FLyrVect inputSelectable = (FLyrVect) (layers
                    .getLayer((String) layersComboBox.getSelectedItem()));
            FBitSet fBitSet = null;
            try {
                fBitSet = inputSelectable.getRecordset().getSelection();
            } catch (ReadDriverException e) {
                e.printStackTrace();
            }
            numSelectedLabel.setText(new Integer(fBitSet.cardinality())
                    .toString());
        } else {
            ReadableVectorial va = ((SingleLayer) (layers
                    .getLayer((String) layersComboBox.getSelectedItem())))
                    .getSource();
            try {
                numSelectedLabel.setText(new Integer(va.getShapeCount())
                        .toString());
            } catch (ReadDriverException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updateAlwaysAddLastVertexCheckbox() {
        boolean enabled = false;
        try {
            enabled = (FShape.LINE == getInputLayer().getTypeIntVectorLayer());
        } catch (ReadDriverException e) {
            e.printStackTrace();
        }
        alwaysAddLastVertexCheckBox.setEnabled(enabled);
    }

    public FLyrVect getInputLayer() {
        FLyrVect solution = null;
        String selectedLayer = (String) layersComboBox.getSelectedItem();
        solution = (FLyrVect) layers.getLayer(selectedLayer);
        return solution;
    }

    public void setFLayers(FLayers layers) {
        this.layers = layers;
    }

    public FLayers getFLayers() {
        return layers;
    }

    public void error(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title,
                JOptionPane.ERROR_MESSAGE);
    }

    public IMonitorableTask askForSpatialIndexCreation(FLyrVect layer) {
        String title = PluginServices.getText(this, "Crear_Indice");
        String confirmDialogText = PluginServices.getText(this,
                "Crear_Indice_Pregunta_1")
                + " "
                + layer.getName()
                + " "
                + PluginServices.getText(this, "Crear_Indice_Pregunta_2");
        int option = JOptionPane.showConfirmDialog(this, confirmDialogText,
                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null);
        if (option == JOptionPane.YES_OPTION) {
            try {
                CreateSpatialIndexMonitorableTask task = new CreateSpatialIndexMonitorableTask(
                        layer);
                return task;
            } catch (ReadDriverException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;

    }

    public void updateLayers(FLayers layers) {
        this.setFLayers(layers);
        this.updateLayersComboBox();
        this.initSelectedItemsJCheckBox();
        this.updateNumSelectedFeaturesLabel();
        this.updateAlwaysAddLastVertexCheckbox();
    }

    private void updateLayersComboBox() {
        if (layersComboBox != null && layers != null) {
            DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(
                    getLayerNames(layers));
            String selectedItem = getFirstActiveLayer(layers);
            if (selectedItem != null) {
                defaultModel.setSelectedItem(selectedItem);
            }
            layersComboBox.setModel(defaultModel);
        }
    }

    protected JComboBox getLayersComboBox() {
        if (layersComboBox == null) {
            layersComboBox = new JComboBox();
            updateLayersComboBox();
            layersComboBox.setBounds(142, 63, 260, 21);
            layersComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        initSelectedItemsJCheckBox();
                        updateNumSelectedFeaturesLabel();
                        updateAlwaysAddLastVertexCheckbox();
                    }
                }
            });
        }
        return layersComboBox;
    }

    private static String[] getLayerNames(FLayers layers) {
        String[] solution = null;
        if (layers != null && layers.getLayersCount() > 0) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0, iLen = layers.getLayersCount(); i < iLen; i++) {
                FLayer layer = layers.getLayer(i);
                if (layer instanceof FLyrVect) {
                    list.add(layer.getName());
                }
                if (layer instanceof FLayers) {
                    FLayers tempLayers = (FLayers) layer;
                    FLyrVect[] vectorials = getVectorialLayers(tempLayers);
                    for (int j = 0; j < vectorials.length; j++) {
                        list.add(vectorials[j].getName());
                    }
                }
            }
            solution = new String[list.size()];
            Collections.sort(list);
            list.toArray(solution);
        }
        return solution;
    }

    private static String getFirstActiveLayer(FLayers layers) {
        if (layers != null && layers.getLayersCount() > 0) {
            for (int i = layers.getLayersCount() - 1; i >= 0; i--) {
                FLayer layer = layers.getLayer(i);
                if (layer instanceof FLyrVect && layer.isActive()) {
                    return layer.getName();
                }
                if (layer instanceof FLayers) {
                    FLayers tempLayers = (FLayers) layer;
                    FLyrVect[] vectorials = getVectorialLayers(tempLayers);
                    for (int j = vectorials.length - 1; j >= 0; j--) {
                        if (vectorials[j].isActive()) {
                            return vectorials[j].getName();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static FLyrVect[] getVectorialLayers(FLayers layers) {
        FLyrVect[] solution = null;
        ArrayList<FLyrVect> list = new ArrayList<FLyrVect>();
        int numLayers = layers.getLayersCount();
        for (int i = 0; i < numLayers; i++) {
            FLayer layer = layers.getLayer(i);
            if (layer instanceof FLyrVect)
                list.add((FLyrVect) layer);
            else if (layer instanceof FLayers)
                list.addAll(Arrays.asList(getVectorialLayers((FLayers) layer)));
        }
        solution = new FLyrVect[list.size()];
        list.toArray(solution);
        return solution;

    }

    protected JCheckBox getSelectedOnlyCheckBox() {
        if (selectedOnlyCheckBox == null) {
            selectedOnlyCheckBox = new JCheckBox();
            selectedOnlyCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent arg0) {
                    updateNumSelectedFeaturesLabel();
                }
            });
            selectedOnlyCheckBox.setText(PluginServices.getText(this,
                    "extract_vertexes_only_selected"));
        }
        return selectedOnlyCheckBox;
    }

    protected void addSpecificDesign() {

        JPanel aux = new JPanel(new BorderLayout());
        String text = PluginServices
                .getText(this, "extract_vertexes_tolerance") + ":";
        distToleranceTextField = getDistToleranceTextField();
        aux.add(distToleranceTextField, BorderLayout.WEST);

        mergeLayerVertexesCheckBox = new JCheckBox();
        mergeLayerVertexesCheckBox.setText(PluginServices.getText(this,
                "extract_vertexes_merge_layer_vertexes"));
        // addComponent(mergeLayerVertexesCheckBox, GridBagConstraints.BOTH,
        // new Insets(5, 5, 5, 5));

        alwaysAddLastVertexCheckBox = new JCheckBox();
        alwaysAddLastVertexCheckBox.setText(PluginServices.getText(this,
                "extract_vertexes_always_add_last_vertex"));
        addComponent(alwaysAddLastVertexCheckBox, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5));

        alwaysAddLastVertexCheckBox.setSelected(true);

        updateAlwaysAddLastVertexCheckbox();

        addComponent(text, aux, GridBagConstraints.HORIZONTAL, new Insets(5, 5,
                5, 5));
        initSelectedItemsJCheckBox();
        updateNumSelectedFeaturesLabel();
    }

    private JTextField getDistToleranceTextField() {
        if (this.distToleranceTextField == null) {
            this.distToleranceTextField = new JTextField(15);
        }
        return distToleranceTextField;
    }

    public boolean isMergeLayerVertexesSelected() {
        return mergeLayerVertexesCheckBox != null
                && mergeLayerVertexesCheckBox.isEnabled()
                && mergeLayerVertexesCheckBox.isSelected();
    }

    public boolean isAlwaysAddLastVertexSelected() {
        return alwaysAddLastVertexCheckBox != null
                && alwaysAddLastVertexCheckBox.isEnabled()
                && alwaysAddLastVertexCheckBox.isSelected();
    }

    public boolean isOnlySelectionSelected() {
        return selectedOnlyCheckBox != null && selectedOnlyCheckBox.isEnabled()
                && selectedOnlyCheckBox.isSelected();
    }

    public double getClusterTolerance() throws ExtractVertexesException {
        try {
            String strDist = this.distToleranceTextField.getText();
            return (strDist.trim().length() == 0) ? 0 : Double
                    .parseDouble(strDist);
        } catch (NumberFormatException ex) {
            throw new ExtractVertexesException(
                    "Distancia de fusionado introducida no numérica");
        }
    }
}
