package es.icarto.gvsig.extgia.forms;

import static es.icarto.gvsig.extgia.preferences.DBFieldNames.AREA_MANTENIMIENTO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.BASE_CONTRATISTA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.DIRECCION;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.DIRECCION_PF;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.DIRECCION_PI;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.MUNICIPIO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.NOMBRE_VIA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.NOMBRE_VIA_PF;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.PK;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.PK_FINAL;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.PK_INICIAL;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.SENTIDO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TIPO_VIA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TIPO_VIA_PF;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TRAMO;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import es.icarto.gvsig.commons.utils.StrUtils;
import es.icarto.gvsig.extgia.batch.AddTrabajosBatchListener;
import es.icarto.gvsig.extgia.batch.reconocimientos.AddReconocimientosBatchListener;
import es.icarto.gvsig.extgia.forms.ramales.RamalesForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.navtableforms.gui.buttons.fileslink.FilesLinkButton;
import es.icarto.gvsig.siga.SIGAConfigExtension;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;

@SuppressWarnings("serial")
public abstract class AbstractFormWithLocationWidgets extends BasicAbstractForm {

    private static final Logger logger = Logger.getLogger(AbstractFormWithLocationWidgets.class);

    protected FilesLinkButton filesLinkButton;

    protected JButton addTrabajosBatchButton;
    protected JButton addReconocimientosBatchButton;
    protected JButton saveRecordsBatchButton;

    AddTrabajosBatchListener addTrabajosBatchListener;
    AddReconocimientosBatchListener addReconocimientosBatchListener;
    SaveRecordsBatchListener saveRecordsBatchListener;

    private final ImagesInForms imagesInForms;

    private JLabel empresaLb;
    private JLabel concesionariaLb;
    private JComboBox tramoCB;
    private final InfoEmpresa infoEmpresa;

    public AbstractFormWithLocationWidgets(FLyrVect layer) {
        super(layer);
        addChained(BASE_CONTRATISTA, AREA_MANTENIMIENTO);
        addChained(TRAMO, BASE_CONTRATISTA);
        final Map<String, JComponent> widgets = getWidgets();
        if (widgets.get(TIPO_VIA) != null) {
            addChained(TIPO_VIA, BASE_CONTRATISTA, TRAMO);
        }
        if (widgets.get(TIPO_VIA_PF) != null) {
            addChained(TIPO_VIA_PF, BASE_CONTRATISTA, TRAMO);
        }
        if (widgets.get(NOMBRE_VIA) != null) {
            addChained(NOMBRE_VIA, BASE_CONTRATISTA, TRAMO, TIPO_VIA);
        }
        if (widgets.get(NOMBRE_VIA_PF) != null) {
            addChained(NOMBRE_VIA_PF, BASE_CONTRATISTA, TRAMO, TIPO_VIA_PF);
        }
        if (widgets.get(PK) != null) {
            addChained(PK, TIPO_VIA, NOMBRE_VIA);
        }
        if (widgets.get(PK_INICIAL) != null) {
            if ((widgets.get(TIPO_VIA) != null) && (widgets.get(NOMBRE_VIA) != null)) {
                addChained(PK_INICIAL, TIPO_VIA, NOMBRE_VIA);
            }
        }
        if (widgets.get(PK_FINAL) != null) {
            if (widgets.get(NOMBRE_VIA_PF) != null) {
                addChained(PK_FINAL, TIPO_VIA_PF, NOMBRE_VIA_PF);
            } else {
                if ((widgets.get(TIPO_VIA) != null) && (widgets.get(NOMBRE_VIA) != null)) {
                    addChained(PK_FINAL, TIPO_VIA, NOMBRE_VIA);
                }
            }
        }
        if (widgets.get(DIRECCION) != null) {
            addChained(DIRECCION, TIPO_VIA);
        }
        if (widgets.get(DIRECCION_PI) != null) {
            addChained(DIRECCION_PI, TIPO_VIA);
        }
        if (widgets.get(DIRECCION_PF) != null) {
            addChained(DIRECCION_PF, TIPO_VIA_PF);
        }

        if (widgets.get(MUNICIPIO) != null) {
            addChained(MUNICIPIO, BASE_CONTRATISTA, TRAMO);
        }
        imagesInForms = new ImagesInForms(formBody, DBFieldNames.GIA_SCHEMA, getImagesDBTableName(), getElementID());
        imagesInForms.setForm(this);

        SIGAConfigExtension ext = (SIGAConfigExtension) PluginServices.getExtension(SIGAConfigExtension.class);
        infoEmpresa = ext.getInfoEmpresa();
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        tramoCB = getFormPanel().getComboBox(TRAMO);
        empresaLb = getFormPanel().getLabel("etiqueta_empresa");
        concesionariaLb = getFormPanel().getLabel("etiqueta_concesion");
        JDateChooser dateWidget = (JDateChooser) getFormPanel().getComponentByName("fecha_actualizacion");
        if (dateWidget != null) {
            dateWidget.setEnabled(false);
            // firme does not have fecha_actualizacion
            final JTextFieldDateEditor uiComponent = (JTextFieldDateEditor) dateWidget.getDateEditor()
                    .getUiComponent();
            uiComponent.setDisabledTextColor(new Color(80, 80, 80));
            uiComponent.setBackground(new Color(236, 233, 216));
            uiComponent.setFont(new Font("Arial", Font.BOLD, 11));
        }
    }

    @Override
    protected void setListeners() {
        super.setListeners();
        imagesInForms.setListeners();

        if (SqlUtils.elementHasType(dataName, "inspecciones")) {
            if (addReconocimientosBatchButton == null) {
                addReconocimientosBatchButton = new JButton();
                java.net.URL imgURL = getClass().getResource("/batch_reconocimiento.png");
                ImageIcon batchReconocimientoIcon = new ImageIcon(imgURL);
                addReconocimientosBatchButton.setIcon(batchReconocimientoIcon);
                addReconocimientosBatchButton.setToolTipText(PluginServices.getText(this,
                        "addBatchReconocimientos_tooltip"));
                getActionsToolBar().add(addReconocimientosBatchButton);
            }
        }

        if (SqlUtils.elementHasType(dataName, "trabajos")) {
            if (addTrabajosBatchButton == null) {
                addTrabajosBatchButton = new JButton();
                java.net.URL imgURL = getClass().getResource("/batch_trabajo.png");
                ImageIcon trabajosBatchIcon = new ImageIcon(imgURL);
                addTrabajosBatchButton.setIcon(trabajosBatchIcon);
                addTrabajosBatchButton.setToolTipText(PluginServices.getText(this, "addBatchTrabajos_tooltip"));
                getActionsToolBar().add(addTrabajosBatchButton);
            }
        }

        if (addTrabajosBatchListener == null && addTrabajosBatchButton != null) {
            addTrabajosBatchListener = new AddTrabajosBatchListener(this);
            addTrabajosBatchButton.addActionListener(addTrabajosBatchListener);
        }

        if (addReconocimientosBatchListener == null && addReconocimientosBatchButton != null) {
            addReconocimientosBatchListener = new AddReconocimientosBatchListener(getElement(),
                    getReconocimientosFormFileName(), getReconocimientosDBTableName());
            addReconocimientosBatchButton.addActionListener(addReconocimientosBatchListener);
        }

        if (saveRecordsBatchButton == null) {
            saveRecordsBatchButton = new JButton();
            java.net.URL imgURL = getClass().getResource("/saveSelected.png");
            ImageIcon saveBatchIcon = new ImageIcon(imgURL);
            saveRecordsBatchButton.setIcon(saveBatchIcon);
            saveRecordsBatchButton.setToolTipText(PluginServices.getText(this, "saveRecordsBatch_tooltip"));
            getActionsToolBar().add(saveRecordsBatchButton);
        }

        if (saveRecordsBatchListener == null && saveRecordsBatchButton != null) {
            saveRecordsBatchListener = new SaveRecordsBatchListener();
            saveRecordsBatchButton.addActionListener(saveRecordsBatchListener);
        }

    }

    public class SaveRecordsBatchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isSaveable()) {
                setSavingValues(true);
                try {
                    if (layer.getRecordset().getSelection().isEmpty()) {
                        JOptionPane.showMessageDialog(null, PluginServices.getText(this, "unselectedElements_msg"),
                                PluginServices.getText(this, "warning"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (layerController.getValuesChanged().containsKey(getElementID())) {
                        JOptionPane.showMessageDialog(null,
                                "La actualización masiva no funciona en campos que forman parte del ID.",
                                "Cambios en el ID", JOptionPane.WARNING_MESSAGE);
                        layerController.clearAll();
                        setChangedValues(false);
                        setSavingValues(false);
                        return;
                    }

                    Object[] options = { PluginServices.getText(this, "optionPane_yes"),
                            PluginServices.getText(this, "optionPane_no") };
                    int m = JOptionPane.showOptionDialog(
                            null,
                            PluginServices.getText(this, "updateInfo_msg_I")
                                    + layer.getRecordset().getSelection().cardinality() + " "
                                    + PluginServices.getText(this, "updateInfo_msg_II"), null,
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options,
                            options[1]);
                    if (m == JOptionPane.OK_OPTION) {
                        saveRecords();
                        JOptionPane.showMessageDialog(
                                null,
                                PluginServices.getText(this, "updatedInfo_msg_I")
                                        + layer.getRecordset().getSelection().cardinality() + " "
                                        + PluginServices.getText(this, "updatedInfo_msg_II"));
                        setChangedValues(false);
                        setSavingValues(false);
                    }
                } catch (ReadDriverException ex) {
                    layerController.clearAll();
                    setChangedValues(false);
                    setSavingValues(false);
                    logger.error(ex.getStackTrace(), ex);
                } catch (StopWriterVisitorException e1) {
                    layerController.clearAll();
                    setChangedValues(false);
                    setSavingValues(false);
                    logger.error(e1.getStackTrace(), e1);
                }
            }
        }

        private void saveRecords() throws ReadDriverException, StopWriterVisitorException {
            int[] indexesofValuesChanged = layerController.getIndexesOfValuesChanged();
            String[] valuesChanged = layerController.getValuesChanged().values().toArray(new String[0]);

            ToggleEditing te = new ToggleEditing();
            boolean wasEditing = layer.isEditing();
            if (!wasEditing) {
                te.startEditing(layer);
            }
            for (int i = 0; i < layer.getRecordset().getRowCount(); i++) {
                if (layer.getRecordset().isSelected(i)) {
                    te.modifyValues(layer, i, indexesofValuesChanged, valuesChanged);
                }
            }
            if (!wasEditing) {
                te.stopEditing(layer, false);
            }
            layerController.read(getPosition());
        }

    }

    @Override
    protected void removeListeners() {

        if (addTrabajosBatchButton != null) {
            addTrabajosBatchButton.removeActionListener(addTrabajosBatchListener);
        }
        if (addReconocimientosBatchButton != null) {
            addReconocimientosBatchButton.removeActionListener(addReconocimientosBatchListener);
        }

        saveRecordsBatchButton.removeActionListener(saveRecordsBatchListener);

        imagesInForms.removeListeners();

        super.removeListeners();
    }

    protected void addNewButtonsToActionsToolBar(final Elements element) {
        JPanel actionsToolBar = this.getActionsToolBar();

        filesLinkButton = new FilesLinkButton(this, new FilesLinkDataImp(element));
        actionsToolBar.add(filesLinkButton);
    }

    @Override
    public boolean saveRecord() throws StopWriterVisitorException {
        sanitizeWidget(NOMBRE_VIA);
        sanitizeWidget(NOMBRE_VIA_PF);
        sanitizeWidget(SENTIDO);

        return super.saveRecord();
    }

    private void sanitizeWidget(String nombreVia) {
        JComboBox widget = (JComboBox) getWidgets().get(nombreVia);
        if (widget != null) {
            String value = layerController.getValue(nombreVia);
            if (StrUtils.isEmptyString(value)) {
                layerController.setValue(nombreVia, "0");
            }
        }
    }

    @Override
    protected void fillSpecificValues() {
        super.fillSpecificValues();
        imagesInForms.fillSpecificValues(getPrimaryKeyValue());
        fillEmpresaLB();

        if (filesLinkButton == null) {
            addNewButtonsToActionsToolBar(getElement());
        }

        fillSentidoInRamalesTable();
    }

    private void fillSentidoInRamalesTable() {
        JTable table = getFormPanel().getTable(RamalesForm.TABLENAME);
        if (table == null) {
            return;
        }
        table.getColumnModel().getColumn(2).setCellRenderer(

        new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (value == null) {
                    label.setText("");
                } else {
                    String text = value.toString().trim();
                    if (text.equals("0") || text.isEmpty()) {
                        label.setText("");
                    } else if (text.equals("1")) {
                        label.setText("Creciente");
                    } else if (text.equals("2")) {
                        label.setText("Decreciente");
                    } else if (text.equals("3")) {
                        label.setText("Ambos");
                    }
                }

                return label;
            }
        });
    }

    private void fillEmpresaLB() {
        Object tramo = tramoCB.getSelectedItem();
        empresaLb.setText(infoEmpresa.getTitle(tramo));
        concesionariaLb.setText(infoEmpresa.getSubtitle(tramo));
    }

    @Override
    protected String getSchema() {
        return DBFieldNames.GIA_SCHEMA;
    }

    protected String getReconocimientosFormFileName() {
        return "forms/" + getBasicName() + "_reconocimientos.jfrm";
    }

    public String getReconocimientosDBTableName() {
        return getBasicName() + "_reconocimientos";
    }

    public String getTrabajosDBTableName() {
        return getBasicName() + "_trabajos";
    }

    @Override
    protected String getPrimaryKeyValue() {
        return getFormController().getValue(getElementID());
    }

    public abstract Elements getElement();

    public abstract String getElementID();

    public abstract String getElementIDValue();

    public abstract JTable getReconocimientosJTable();

    public abstract JTable getTrabajosJTable();

    public String getImagesDBTableName() {
        return getElement().imagenesTableName;
    }

    public String getReconocimientosIDField() {
        return "n_inspeccion";
    }

    public String getTrabajosIDField() {
        return "id_trabajo";
    }

    @Override
    public abstract String getBasicName();

    protected abstract boolean hasSentido();
}
