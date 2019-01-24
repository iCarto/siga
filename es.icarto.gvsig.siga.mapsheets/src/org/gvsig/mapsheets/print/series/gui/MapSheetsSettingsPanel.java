package org.gvsig.mapsheets.print.series.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.gvsig.gui.beans.swing.JFileChooser;
import org.gvsig.mapsheets.print.audasa.AudasaPreferences;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.utils.LayerComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.MeasureUnitComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.NumericDocument;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;
import org.gvsig.tools.file.PathGenerator;

import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.view.IProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.SimpleFileFilter;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.xml.XMLEncodingUtils;
import com.iver.utiles.xmlEntity.generate.XmlTag;

/**
 * Dialog to create a new map sheet grid, by choosing creation method,
 * overlapping, etc.
 *
 *
 * @author jldominguez
 *
 */
public class MapSheetsSettingsPanel extends JPanel implements IWindow,
	ActionListener, KeyListener {

    public static final int WIDTH = 645;
    public static final int HEIGHT = 475 - 175;

    private static String OPEN_TEMPLATE_FILE_CHOOSER_ID = "OPEN_TEMPLATE_FILE_CHOOSER_ID";

    private static final long serialVersionUID = 1L;
    private JPanel areaPanel = null;

    private JPanel scalePanel = null;

    private JPanel templatesPanel;
    private JRadioButton audasaRB;
    private JRadioButton autoestradasRB;

    private JLabel templateCBLabel;
    private static JComboBox templateCB;

    private JRadioButton coverView = null;
    private JRadioButton basedOnFeatures = null;
    private JComboBox vecLayerToUse = null;
    private JCheckBox selectedOnly = null;
    private JRadioButton gridCustom;
    private JTextField gridFile;
    private JButton gridFileButton;
    private JFileChooser gridFileChooser;

    private JSpinner overlapSpinner = null;

    private JLabel oneFixedLabel = null;
    private JLabel scaleLabel = null;
    private JTextField scaleText = null;

    private JButton acceptButton = null;
    private JButton cancelButton = null;
    private JLabel overlapLabel = null;

    private View view = null;

    public MapSheetsSettingsPanel(View v) {
	super();
	view = v;
	initialize();
    }

    public View getView() {
	return view;
    }

    private void initialize() {
	this.setSize(WIDTH, HEIGHT);
	this.setLayout(null);
	this.add(getAreaPanel(), null);
	this.add(getTemplatesPanel(), null);
	this.add(getAcceptButton(), null);
	this.add(getCancelButton(), null);
	this.loadLayersCombo(getVectLayerToUseCB(), true);
	if (getVectLayerToUseCB().getItemCount() == 0) {
	    getBasedOnFeaturesRB().setEnabled(false);
	    getVectLayerToUseCB().setEnabled(false);
	    getSelectedOnlyCB().setEnabled(false);
	} else {
	    getBasedOnFeaturesRB().setEnabled(true);
	    getVectLayerToUseCB().setEnabled(
		    getBasedOnFeaturesRB().isSelected());
	    getSelectedOnlyCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	}
    }

    private JPanel getAreaPanel() {
	if (areaPanel == null) {
	    areaPanel = new JPanel();
	    areaPanel.setLayout(null);
	    areaPanel.setBounds(new Rectangle(5, 5, 631, 157));
	    String bor_txt = PluginServices.getText(this, "Area_selection");
	    areaPanel.setBorder(BorderFactory.createTitledBorder(null, bor_txt,
		    TitledBorder.DEFAULT_JUSTIFICATION,
		    TitledBorder.DEFAULT_POSITION, new Font("Dialog",
			    Font.BOLD, 12), new Color(51, 51, 51)));

	    areaPanel.add(getCoverViewRB(), null);
	    areaPanel.add(getBasedOnFeaturesRB(), null);
	    areaPanel.add(getVectLayerToUseCB(), null);
	    areaPanel.add(getSelectedOnlyCB(), null);
	    areaPanel.add(getScalePanel(), null);
	    areaPanel.add(getGridCustomRB(), null);
	    areaPanel.add(getGridFileTF(), null);
	    areaPanel.add(getGridFileB(), null);

	    gridFileChooser = new JFileChooser(OPEN_TEMPLATE_FILE_CHOOSER_ID,
		    System.getProperty("user.dir"));
	    gridFileChooser.setAcceptAllFileFilterUsed(false);
	    gridFileChooser
		    .setFileFilter(new SimpleFileFilter("grid", PluginServices
			    .getText(MapSheetsUtils.class, "grid_files")));

	    ArrayList<JRadioButton> rbb = new ArrayList<JRadioButton>();
	    rbb.add(getCoverViewRB());
	    rbb.add(getBasedOnFeaturesRB());
	    rbb.add(getGridCustomRB());
	    MapSheetsUtils.joinRadioButtons(rbb);
	}
	return areaPanel;
    }

    private JPanel getScalePanel() {
	if (scalePanel == null) {
	    scalePanel = new JPanel();
	    scalePanel.setLayout(null);
	    scalePanel
		    .setBounds(new Rectangle(8, 110 + 13 + 40 - 112, 615, 68));// +120-15));

	    String bor_txt = PluginServices.getText(this,
		    "Choose_scale_or_grid_division");
	    scalePanel.setBorder(BorderFactory.createTitledBorder(null,
		    bor_txt, TitledBorder.CENTER,
		    TitledBorder.DEFAULT_POSITION, new Font("Dialog",
			    Font.PLAIN, 12), new Color(51, 51, 51)));
	    ((TitledBorder) scalePanel.getBorder()).setTitleColor(UIManager
		    .getColor("Label.foreground"));

	    scalePanel.add(getScaleLabel(), null);
	    scalePanel.add(getOneLabel(), null);
	    scalePanel.add(getScaleField(), null);
	    scalePanel.add(getOverlapLabel(), null);
	    scalePanel.add(getOverlapSpinner(), null);
	}
	return scalePanel;
    }

    /**
     * This method does setEnabled onto all scalePanel's components
     */
    private void setEnabledScalePanel(boolean enabled) {
	for (Component component : scalePanel.getComponents()) {
	    component.setEnabled(enabled);
	    if (component instanceof JTextField) {
		Color tFColor = enabled ? UIManager
			.getColor("TextField.background") : UIManager
			.getColor("TextField.inactiveBackground");
		component.setBackground(tFColor);
	    }
	}
    }

    private JPanel getTemplatesPanel() {
	if (templatesPanel == null) {
	    templatesPanel = new JPanel();
	    templatesPanel.setLayout(null);
	    templatesPanel.setBounds(new Rectangle(5, 165 + 13 + 40 - 112 + 68,
		    631, 21 * 4 + 30));
	    templatesPanel.setBorder(BorderFactory.createTitledBorder(null,
		    PluginServices.getText(this, "Template_type"),
		    TitledBorder.DEFAULT_JUSTIFICATION,
		    TitledBorder.DEFAULT_POSITION, new Font("Dialog",
			    Font.BOLD, 12), new Color(51, 51, 51)));

	    audasaRB = new JRadioButton("Audasa");
	    audasaRB.setBounds(new Rectangle(15, 21, 200 - 15 - 15, 21));
	    audasaRB.setSelected(true);
	    audasaRB.addActionListener(this);
	    autoestradasRB = new JRadioButton("Autoestradas");
	    autoestradasRB.setBounds(new Rectangle(15 + 200, 21, 200 - 15 - 15,
		    21));
	    autoestradasRB.setSelected(false);
	    autoestradasRB.addActionListener(this);

	    ComboBoxModel templateCBModel = new DefaultComboBoxModel(
		    AudasaPreferences.getTemplates());
	    templateCBLabel = new JLabel(PluginServices.getText(this,
		    "Choose_template"));
	    templateCBLabel.setBounds(new Rectangle(95, 21 + 25, 150, 21));
	    templateCB = new JComboBox();
	    templateCB.setModel(templateCBModel);
	    templateCB.setBounds(new Rectangle(200, 21 + 25, 325, 21));

	    templatesPanel.add(audasaRB, null);
	    templatesPanel.add(autoestradasRB, null);

	    templatesPanel.add(templateCBLabel, null);
	    templatesPanel.add(templateCB, null);

	    ArrayList<JRadioButton> group = new ArrayList<JRadioButton>();
	    group.add(audasaRB);
	    group.add(autoestradasRB);

	    MapSheetsUtils.joinRadioButtons(group);
	}
	return templatesPanel;
    }

    private JLabel getScaleLabel() {
	if (scaleLabel == null) {
	    scaleLabel = new JLabel();
	    scaleLabel.setBounds(new Rectangle(25, 28, 100, 21));
	    scaleLabel.setText(PluginServices.getText(this, "Scale"));
	}
	return scaleLabel;

    }

    private JLabel getOneLabel() {
	if (oneFixedLabel == null) {
	    oneFixedLabel = new JLabel();
	    oneFixedLabel.setBounds(new Rectangle(135, 28, 35, 21));
	    oneFixedLabel.setText("1 :");
	}
	return oneFixedLabel;

    }

    private JTextField getScaleField() {
	if (scaleText == null) {
	    scaleText = new JTextField();
	    scaleText.setDocument(new NumericDocument());
	    scaleText.setBounds(new Rectangle(170, 28, 130, 21));
	    scaleText.setText("1000");
	}
	return scaleText;

    }

    private JRadioButton getCoverViewRB() {
	if (coverView == null) {
	    coverView = new JRadioButton();
	    coverView.setText(PluginServices.getText(this, "Cover_view"));
	    coverView.setBounds(new Rectangle(15, 27, 110, 21));
	    coverView.addActionListener(this);
	}
	return coverView;
    }

    private JRadioButton getGridCustomRB() {
	if (gridCustom == null) {
	    gridCustom = new JRadioButton(PluginServices.getText(this,
		    "Load_grid"));
	    gridCustom.setBounds(new Rectangle(15, 110 + 13 + 40 - 112 + 75,
		    200, 21));
	    gridCustom.setSelected(false);
	    gridCustom.addActionListener(this);
	}
	return gridCustom;
    }

    private JTextField getGridFileTF() {
	if (gridFile == null) {
	    gridFile = new JTextField(200);
	    gridFile.setEditable(false);
	    gridFile.setEnabled(false);
	    gridFile.setBounds(new Rectangle(220, 110 + 13 + 40 - 112 + 75,
		    270, 22));
	}
	return gridFile;
    }

    private JButton getGridFileB() {
	if (gridFileButton == null) {
	    gridFileButton = new JButton("...");
	    gridFileButton.setBounds(new Rectangle(495, 110 + 13 + 40 - 112
		    + 75, 50, 21));
	    gridFileButton.addActionListener(this);
	    gridFileButton.setEnabled(false);
	}
	return gridFileButton;
    }

    private JRadioButton getBasedOnFeaturesRB() {
	if (basedOnFeatures == null) {
	    basedOnFeatures = new JRadioButton();
	    basedOnFeatures.setText(PluginServices.getText(this,
		    "Based_on_features"));
	    basedOnFeatures.setBounds(new Rectangle(140, 27, 160, 21));
	    basedOnFeatures.addActionListener(this);
	}
	return basedOnFeatures;
    }

    private JSpinner getOverlapSpinner() {
	if (overlapSpinner == null) {
	    overlapSpinner = new JSpinner();
	    JSpinner.NumberEditor editor = (JSpinner.NumberEditor) overlapSpinner
		    .getEditor();
	    editor.getTextField().setEditable(false);
	    editor.getModel().setValue(0);
	    editor.getModel().setMinimum(0);
	    editor.getModel().setMaximum(50);
	    overlapSpinner.setBounds(new Rectangle(430 + 75, 28, 45, 21));
	}
	return overlapSpinner;
    }

    private JButton getAcceptButton() {
	if (acceptButton == null) {
	    acceptButton = new JButton(PluginServices.getText(null, "Aceptar"));
	    acceptButton.setBounds(new Rectangle(205, 470 - 175, 111, 26));
	    acceptButton.setMnemonic(KeyEvent.VK_A);
	    acceptButton.addActionListener(this);
	}
	return acceptButton;
    }

    private JButton getCancelButton() {
	if (cancelButton == null) {
	    cancelButton = new JButton(PluginServices.getText(null, "Cancel"));
	    cancelButton.setBounds(new Rectangle(331, 470 - 175, 111, 26));
	    cancelButton.setMnemonic(KeyEvent.VK_C);
	    cancelButton.addActionListener(this);
	}
	return cancelButton;
    }

    private WindowInfo winfo = null;

    private boolean hasCancelled = true;

    @Override
    public WindowInfo getWindowInfo() {

	if (winfo == null) {
	    winfo = new WindowInfo(WindowInfo.MODALDIALOG);
	    winfo.setTitle(AudasaPreferences.TITULO_VENTANA);
	    winfo.setHeight(HEIGHT);
	    winfo.setWidth(WIDTH);
	}
	return winfo;
    }

    @Override
    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    private JLabel getOverlapLabel() {
	if (overlapLabel == null) {
	    overlapLabel = new JLabel();
	    overlapLabel.setText(PluginServices.getText(this,
		    "Overlap_or_clearance"));
	    overlapLabel.setBounds(new Rectangle(270 + 75, 28, 150, 21));
	}
	return overlapLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	Object src = e.getSource();
	if (src == this.audasaRB) {
		Object templateSelected = templateCB.getSelectedItem();
	    ComboBoxModel templateCBModel = new DefaultComboBoxModel(
		    AudasaPreferences.getTemplates());
	    templateCB.setModel(templateCBModel);
	    templateCB.setSelectedItem(templateSelected);
	    return;
	} else if (src == this.autoestradasRB) {
		Object templateSelected = templateCB.getSelectedItem();
	    DefaultComboBoxModel templateCBModel = new DefaultComboBoxModel(
		    AudasaPreferences.getTemplates());
	    templateCBModel.removeElement(AudasaPreferences.A3_POLICIA_MARGENES_LEYENDA);
	    templateCBModel.removeElement(AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA);
	    templateCBModel.removeElement(AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL_LEYENDA);
	    templateCBModel.removeElement(AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL_LEYENDA);
	    templateCB.setModel(templateCBModel);
	    templateCB.setSelectedItem(templateSelected);
	    return;
	}

	if (src == this.getAcceptButton()) {
	    hasCancelled = false;
	    try {
		MapSheetGrid newgrid;
		if (getGridCustomRB().isSelected()) {
		    newgrid = MapSheetsUtils.loadMapSheetsGrid(gridFileChooser
			    .getSelectedFile());
		} else {
		    double grid_width;
		    double grid_height;

		    String selTmpl = templateCB.getSelectedItem().toString();
		    if (selTmpl.equals(AudasaPreferences.A4_CONSULTAS)
			    || selTmpl
				    .equals(AudasaPreferences.A4_CONSULTAS_LOCALIZADOR)
			    || selTmpl
				    .equals(AudasaPreferences.A4_POLICIA_MARGENES)
			    || selTmpl
				    .equals(AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA)) {
			grid_width = AudasaPreferences.VIEW_WIDTH_A4;
			grid_height = AudasaPreferences.VIEW_HEIGHT_A4;
		    } else if (selTmpl.equals(AudasaPreferences.A4_CONSULTAS_VERTICAL) ||
		    		selTmpl.equals(AudasaPreferences.A4_CONSULTAS_VERTICAL_LOCALIZADOR) ||
		    		selTmpl.equals(AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL) ||
		    		selTmpl.equals(AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL_LEYENDA)) {
		    	grid_width = AudasaPreferences.VIEW_WIDTH_A4_VERTICAL;
		    	grid_height = AudasaPreferences.VIEW_HEIGHT_A4_VERTICAL;
		    } else if (selTmpl.equals(AudasaPreferences.A3_CONSULTAS_VERTICAL) ||
		    		selTmpl.equals(AudasaPreferences.A3_CONSULTAS_VERTICAL_LOCALIZADOR) ||
		    		selTmpl.equals(AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL) ||
		    		selTmpl.equals(AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL_LEYENDA)) {
				grid_width = AudasaPreferences.VIEW_WIDTH_A3_VERTICAL;
				grid_height = AudasaPreferences.VIEW_HEIGHT_A3_VERTICAL;
		    } else { // any A3 template
			grid_width = AudasaPreferences.VIEW_WIDTH_A3;
			grid_height = AudasaPreferences.VIEW_HEIGHT_A3;
		    }

		    double w = 1.0 * grid_width;
		    double h = 1.0 * grid_height;
		    MeasureUnitComboItem muci = MeasureUnitComboItem.MEASURE_UNIT_CM;
		    w = 100 * muci.getMetersPerUnit() * w;
		    h = 100 * muci.getMetersPerUnit() * h;

		    Rectangle2D use_map_r = new Rectangle2D.Double(0, 0, w, h);
		    long s = Long.parseLong(this.getScaleField().getText());
		    int opc = Integer.parseInt(this.getOverlapSpinner()
			    .getValue().toString());

		    FLyrVect lyrv = null;
		    LayerComboItem lci = (LayerComboItem) getVectLayerToUseCB()
			    .getSelectedItem();
		    if (lci != null) {
			lyrv = (FLyrVect) lci.getLayer();
		    }

		    ArrayList[] igs_codes = MapSheetsUtils.createFrames(
			    this.coverView.isSelected(),
			    this.selectedOnly.isSelected(), use_map_r,
			    getView().getMapControl().getViewPort(), s, opc,
			    getView().getProjection(), lyrv);

		    ArrayList igs = igs_codes[0];
		    ArrayList cods = igs_codes[1];
		    HashMap atts_hm = null;

		    newgrid = MapSheetGrid.createMapSheetGrid(MapSheetGrid
			    .createNewName(), getView().getProjection(),
			    MapSheetGrid.createDefaultLyrDesc());

		    int sz = igs.size();
		    for (int i = 0; i < sz; i++) {
			atts_hm = new HashMap();

			atts_hm.put(MapSheetGrid.ATT_NAME_CODE,
				ValueFactory.createValue((String) cods.get(i)));
			atts_hm.put(MapSheetGrid.ATT_NAME_ROT_RAD,
				ValueFactory.createValue(new Double(0)));
			atts_hm.put(MapSheetGrid.ATT_NAME_OVERLAP,
				ValueFactory.createValue(new Double(opc)));
			atts_hm.put(MapSheetGrid.ATT_NAME_SCALE,
				ValueFactory.createValue(new Double(s)));
			atts_hm.put(MapSheetGrid.ATT_NAME_DIMX_CM,
				ValueFactory.createValue(new Double(w)));
			atts_hm.put(MapSheetGrid.ATT_NAME_DIMY_CM,
				ValueFactory.createValue(new Double(h)));

			newgrid.addSheet((IGeometry) igs.get(i), atts_hm);
		    }
		}

		// before adding the new layer, delete all MapSheetGrids in TOC
		FLayers layersInTOC = getView().getMapControl().getMapContext()
			.getLayers();
		for (int i = 0; i < layersInTOC.getLayersCount(); i++) {
		    if (layersInTOC.getLayer(i) instanceof MapSheetGrid) {
			getView().getMapControl().getMapContext().getLayers()
				.removeLayer(layersInTOC.getLayer(i));
		    }
		}
		getView().getMapControl().getMapContext().getLayers()
			.addLayer(newgrid);
		MapSheetsUtils.setOnlyActive(newgrid, getView().getMapControl()
			.getMapContext().getLayers());

		PluginServices.getMDIManager().closeWindow(this);

	    } catch (Exception exc) {
		JOptionPane.showMessageDialog(this, exc.getMessage(),
			PluginServices.getText(this, "Error"),
			JOptionPane.ERROR_MESSAGE);
		// NotificationManager.addError("While creating maps: ", exc);
	    }
	    return;
	}

	if (src == this.getCancelButton()) {
	    hasCancelled = true;
	    PluginServices.getMDIManager().closeWindow(this);
	    return;
	}

	if (src == getCoverViewRB() || src == getBasedOnFeaturesRB()
		|| src == getGridCustomRB()) {
	    getVectLayerToUseCB().setEnabled(
		    getBasedOnFeaturesRB().isSelected());
	    getSelectedOnlyCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	    getGridFileTF().setEnabled(getGridCustomRB().isSelected());
	    getGridFileB().setEnabled(getGridCustomRB().isSelected());
	    setEnabledScalePanel(!getGridCustomRB().isSelected());
	    Color color = getGridCustomRB().isSelected() ? UIManager
		    .getColor("TextField.background") : UIManager
		    .getColor("TextField.inactiveBackground");
	    getGridFileTF().setBackground(color);
	    color = getGridCustomRB().isSelected() ? UIManager
		    .getColor("Label.disabledForeground") : UIManager
		    .getColor("Label.foreground");
	    ((TitledBorder) getScalePanel().getBorder()).setTitleColor(color);
	    getScalePanel().repaint();
	}

	if (src == gridFileButton) {
	    gridFileChooser.setCurrentDirectory(new File(
		    AudasaPreferences.GRIDS_PATH));
	    int returnVal = gridFileChooser.showOpenDialog(this);

	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		gridFile.setText(gridFileChooser.getSelectedFile()
			.getAbsolutePath());
	    }
	}

	checkAcceptButtonEnabled();
    }

    private void checkAcceptButtonEnabled() {
	boolean enabled = true;
	if (gridCustom.isSelected()) {
	    enabled &= gridFile.getText().toLowerCase().endsWith(".grid");
	}
	getAcceptButton().setEnabled(enabled);
    }

    public boolean hasCancelled() {
	return hasCancelled;
    }

    public Layout getMapLayout() {
	String selTmpl = templateCB.getSelectedItem().toString();
	if (selTmpl.toLowerCase().endsWith(".gvt")) {
	    File templateFile = new File(selTmpl);
	    PathGenerator.getInstance().setBasePath(templateFile.getParent());
	    return getLayoutFromFile(templateFile);
	} else {
	    return getLayoutFromFile(AudasaPreferences.getSelectedFile(selTmpl));
	}
    }

    public Layout getLayoutFromFile(File layoutFile) {
	Project project = ((ProjectExtension) PluginServices
		.getExtension(ProjectExtension.class)).getProject();
	Layout layout = null;

	if (!(layoutFile.getPath().endsWith(".gvt") || layoutFile.getPath()
		.endsWith(".GVT"))) {
	    layoutFile = new File(layoutFile.getPath() + ".gvt");
	}
	try {
	    File xmlFile = new File(layoutFile.getAbsolutePath());
	    FileInputStream is = new FileInputStream(xmlFile);
	    Reader reader = XMLEncodingUtils.getReader(is);

	    XmlTag tag = (XmlTag) XmlTag.unmarshal(reader);
	    try {
		XMLEntity xml = new XMLEntity(tag);
		fixLogosAndLabels(xml);

		if (xml.contains("followHeaderEncoding")) {
		    layout = Layout.createLayout(xml, project);
		} else {
		    reader = new FileReader(xmlFile);
		    tag = (XmlTag) XmlTag.unmarshal(reader);
		    xml = new XMLEntity(tag);
		    layout = Layout.createLayout(xml, project);
		}

		return layout;
	    } catch (OpenException e) {
		e.showError();
		return null;
	    }

	} catch (FileNotFoundException e) {
	    NotificationManager.addError(
		    PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	} catch (MarshalException e) {
	    NotificationManager.addError(
		    PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	} catch (ValidationException e) {
	    NotificationManager.addError(
		    PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	}
    }

    private XMLEntity find(XMLEntity xml, String key, String value) {
	for (int i = 0; i < xml.getChildrenCount(); i++) {
	    XMLEntity child = xml.getChild(i);
	    try {
		String stringProperty = child.getStringProperty(key);
		if (stringProperty.contains(value)) {
		    return child;
		} else {
		    XMLEntity c = find(child, key, value);
		    if (c != null) {
			return c;
		    }
		}
	    } catch (NotExistInXMLEntity ne) {
		XMLEntity c = find(child, key, value);
		if (c != null) {
		    return c;
		}
	    }
	}
	return null;
    }

    private void fixLogosAndLabels(XMLEntity xml) {
	File a = PluginServices.getPluginServices("es.icarto.gvsig.siga")
		.getPluginDirectory();
	String strA = a.getAbsolutePath() + File.separator + "images"
		+ File.separator;

	if (audasaRB.isSelected()) {
	    // AUTOPISTAS DEL ATLÁNTICO Concesionaria Española, S.A.
	    XMLEntity b = find(xml, "m_name", "Fomento.png");
	    b.remove("m_path");
	    b.putProperty("m_path", strA + "logo_fomento_map.png");

	    XMLEntity c = find(xml, "m_name", "Audasa.JPG");
	    c.remove("m_path");
	    c.putProperty("m_path", strA + "logo_audasa_report.png");

	    // XMLEntity d = find(xml, "s", "AUTOPISTAS DEL ATL");
	    // d.remove("s");
	    // d.putProperty("s",
	    // "AUTOPISTAS DEL ATLÁNTICO\nConcesionaria Española, S.A. ");
	} else {
	    // AUTOESTRADAS DE GALICIA Concesionaria Xunta Galicia, S.A.
	    XMLEntity b = find(xml, "m_name", "Fomento.png");
	    b.remove("m_path");
	    b.putProperty("m_path", strA + "logo_xunta_map.png");

	    XMLEntity c = find(xml, "m_name", "Audasa.JPG");
	    c.remove("m_path");
	    c.putProperty("m_path", strA + "logo_autoestradas_map.png");

	    XMLEntity d = find(xml, "s", "AUTOPISTAS DEL ATL");
	    d.remove("s");
	    d.putProperty("s", new String[] { "AUTOESTRADAS DE GALICIA",
	    "Concesionaria Xunta Galicia, S.A." });
	}

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public JComboBox getVectLayerToUseCB() {
	if (vecLayerToUse == null) {
	    vecLayerToUse = new JComboBox();
	    vecLayerToUse.setBounds(new Rectangle(310, 27, 150, 21));
	    vecLayerToUse.setEnabled(false);
	}
	return vecLayerToUse;
    }

    private void loadLayersCombo(JComboBox cmbo, boolean vector_only) {

	cmbo.removeAllItems();

	IProjectView pv = getView().getModel();
	FLayers lyrs = pv.getMapContext().getLayers();
        int[] types = { FShape.POLYGON, FShape.LINE, FShape.POINT };
	ArrayList list = null;

	try {
	    list = MapSheetsUtils.getLayers(lyrs, types, vector_only);
	} catch (Exception ex) {
	    NotificationManager.addError("While getting vect layers.", ex);
	}

	LayerComboItem item = null;
	for (int i = 0; i < list.size(); i++) {

	    try {
		item = new LayerComboItem((FLayer) list.get(i));
		cmbo.addItem(item);
	    } catch (Exception ex) {
		NotificationManager.addError(
			"While adding layer to vector layer list.", ex);
	    }
	}

    }

    private JCheckBox getSelectedOnlyCB() {
	if (selectedOnly == null) {
	    selectedOnly = new JCheckBox();
	    selectedOnly.setText(PluginServices.getText(this, "Selected_only"));
	    selectedOnly.setBounds(new Rectangle(470, 27, 155, 21));
	    selectedOnly.setEnabled(false);
	}
	return selectedOnly;
    }

    public static String getSelectedTemplate() {
	return templateCB.getSelectedItem().toString();
    }

}
