package es.icarto.gvsig.siga.extractvertexestool;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.gvsig.gui.beans.swing.GridBagLayoutPanel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.format.FormatPool;
import es.icarto.gvsig.commons.gui.TOCLayerManager;
import es.icarto.gvsig.commons.gui.TexfieldFactory;

public class ExtractVertexesGeoprocessPanel extends GridBagLayoutPanel {

    private static final String prototypeDisplayValue = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    private static final Logger logger = Logger.getLogger(ExtractVertexesGeoprocessPanel.class);

    private static final long serialVersionUID = 6833607794721720432L;

    private final Insets insets = new Insets(5, 5, 5, 5);

    private JComboBox layersComboBox;
    private JCheckBox selectedOnlyCheckBox;
    private JCheckBox mergeLayerVertexesCheckBox;
    private JCheckBox alwaysAddLastVertexCheckBox;
    private JLabel numSelectedLabel;
    private JTextField distToleranceTextField;

    private final TOCLayerManager tocLayerManager;

    public ExtractVertexesGeoprocessPanel(View view) {
        super();
        tocLayerManager = new TOCLayerManager(view.getMapControl());
        initialize();
    }

    private void initialize() {
        initLayersWidgets();
        initSelectionWidgets();
        addSpecificDesign();
        updateLayersComboBox();
    }

    private void initLayersWidgets() {
        JLabel firstLayerLab = new JLabel(PluginServices.getText(this, "extract_vertexes_input") + ":");
        layersComboBox = new JComboBox();
        layersComboBox.setPrototypeDisplayValue(prototypeDisplayValue);
        addComponent(firstLayerLab, layersComboBox, GridBagConstraints.BOTH, insets);
        layersComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    initSelectedItemsJCheckBox();
                    updateAlwaysAddLastVertexCheckbox();
                }
            }
        });
    }

    private void updateLayersComboBox() {
        DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getLayerNames());
        layersComboBox.setModel(defaultModel);
        String selectedItem = tocLayerManager.getNameOfActiveLayer();
        if (selectedItem == null) {
            layersComboBox.setSelectedIndex(0);
        } else {
            // sobre el modelo no lanza el evento
            // defaultModel.setSelectedItem(selectedItem);
            layersComboBox.setSelectedItem(selectedItem);
        }
        // Si se vuelve a seleccionar el elemento ya seleccionado, no salta un ItemEvent, aunque si un ActionEvent
        // Este código en realidad sólo está para cuando se abra el Panel y no haya una capa activa, para refrescar
        // el número de features
        initSelectedItemsJCheckBox();
        updateAlwaysAddLastVertexCheckbox();

    }

    private String[] getLayerNames() {
        List<FLyrVect> layers = tocLayerManager.getAllLayers(FLyrVect.class);
        List<String> layerNames = new ArrayList<String>(layers.size());
        for (FLyrVect l : layers) {
            layerNames.add(l.getName());
        }
        Collections.sort(layerNames);
        return layerNames.toArray(new String[0]);
    }

    private void initSelectedItemsJCheckBox() {
        FLyrVect inputLayer = getInputLayer();

        try {
            FBitSet fBitSet = inputLayer.getRecordset().getSelection();
            if (fBitSet.cardinality() == 0) {
                selectedOnlyCheckBox.setEnabled(false);
                selectedOnlyCheckBox.setSelected(false);
            } else {
                selectedOnlyCheckBox.setEnabled(true);
                selectedOnlyCheckBox.setSelected(true);
            }
        } catch (ReadDriverException e) {
            logger.error(e.getMessage(), e);
        }

        updateNumSelectedFeaturesLabel();
    }

    public FLyrVect getInputLayer() {
        String selectedLayer = (String) layersComboBox.getSelectedItem();
        FLyrVect inputLayer = tocLayerManager.getVectorialLayerByName(selectedLayer);
        return inputLayer;
    }

    private void updateNumSelectedFeaturesLabel() {
        String text = " - ";
        try {
            SelectableDataSource recordset = getInputLayer().getRecordset();
            if (selectedOnlyCheckBox.isSelected() && numSelectedLabel != null) {

                int num = recordset.getSelection().cardinality();
                text = new Long(num).toString();
            } else {
                long num = recordset.getRowCount();
                text = new Long(num).toString();
            }
        } catch (ReadDriverException e1) {
            logger.error(e1.getStackTrace(), e1);
        }

        numSelectedLabel.setText(text);
    }

    private void updateAlwaysAddLastVertexCheckbox() {
        boolean enabled = false;
        try {
            enabled = (FShape.LINE == getInputLayer().getTypeIntVectorLayer());
        } catch (ReadDriverException e) {
            logger.error(e.getMessage(), e);
        }
        alwaysAddLastVertexCheckBox.setEnabled(enabled);
    }

    public FLayers getFLayers() {
        return tocLayerManager.getFLayers();
    }

    public void error(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void initSelectionWidgets() {
        selectedOnlyCheckBox = new JCheckBox();
        selectedOnlyCheckBox.setText(PluginServices.getText(this, "extract_vertexes_only_selected"));
        selectedOnlyCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                updateNumSelectedFeaturesLabel();
            }
        });
        addComponent(selectedOnlyCheckBox, GridBagConstraints.BOTH, insets);

        String numSelectedText = PluginServices.getText(this, "extract_vertexes_input_elements") + ":";
        numSelectedLabel = new JLabel("00");
        addComponent(numSelectedText, numSelectedLabel, insets);
    }

    private void addSpecificDesign() {
        String text = PluginServices.getText(this, "extract_vertexes_tolerance") + ":";
        distToleranceTextField = TexfieldFactory.getNumberTextField(-1);

        // mergeLayerVertexesCheckBox = new JCheckBox();
        // mergeLayerVertexesCheckBox.setText(PluginServices.getText(this, "extract_vertexes_merge_layer_vertexes"));
        // addComponent(mergeLayerVertexesCheckBox, GridBagConstraints.BOTH, insets);

        alwaysAddLastVertexCheckBox = new JCheckBox();
        alwaysAddLastVertexCheckBox.setText(PluginServices.getText(this, "extract_vertexes_always_add_last_vertex"));
        addComponent(alwaysAddLastVertexCheckBox, GridBagConstraints.BOTH, insets);
        alwaysAddLastVertexCheckBox.setSelected(true);

        addComponent(text, distToleranceTextField, GridBagConstraints.BOTH, insets);
    }

    public boolean isMergeLayerVertexesSelected() {
        return mergeLayerVertexesCheckBox != null && mergeLayerVertexesCheckBox.isEnabled()
                && mergeLayerVertexesCheckBox.isSelected();
    }

    public boolean isAlwaysAddLastVertexSelected() {
        return alwaysAddLastVertexCheckBox != null && alwaysAddLastVertexCheckBox.isEnabled()
                && alwaysAddLastVertexCheckBox.isSelected();
    }

    public boolean isOnlySelectionSelected() {
        return selectedOnlyCheckBox != null && selectedOnlyCheckBox.isEnabled() && selectedOnlyCheckBox.isSelected();
    }

    public double getClusterTolerance() {
        String strDist = this.distToleranceTextField.getText();
        return FormatPool.instance().toNumber(strDist, 0).doubleValue();
    }
}
