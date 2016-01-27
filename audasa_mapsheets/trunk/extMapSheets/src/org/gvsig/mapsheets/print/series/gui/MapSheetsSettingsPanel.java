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
import java.util.Iterator;

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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
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
 * Dialog to create a new map sheet grid, by choosing
 * creation method, overlapping, etc.
 * 
 * 
 * @author jldominguez
 *
 */
public class MapSheetsSettingsPanel extends JPanel implements IWindow, ActionListener, KeyListener {

    private static Logger logger = Logger.getLogger(MapSheetsSettingsPanel.class.getName());

    public static final int WIDTH = 645;
    public static final int HEIGHT = 475 - 175;

    private static String OPEN_TEMPLATE_FILE_CHOOSER_ID = "OPEN_TEMPLATE_FILE_CHOOSER_ID";

    private static final long serialVersionUID = 1L;
    private JPanel areaPanel = null;

    // private JPanel layoutPanel = null;
    private JPanel scalePanel = null;
    // private JPanel metaBoxPanel = null;

    private JPanel templatesPanel;
    private JRadioButton templateDimensiones;
    private JRadioButton templateConsultas;
    private JRadioButton templatePolicia;
    private JLabel templateSpecificLabel;
    private JComboBox templateSpecific;
    private ComboBoxModel templatesDimensiones;
    private ComboBoxModel templatesConsultas;
    private ComboBoxModel templatesPolicia;
//    private JRadioButton templateCustom;
    private JTextField templateFile;
    private JButton templateFileButton;
    private JFileChooser templateFileChooser;
    private JLabel formatLabel;
    private static JComboBox formatCombobox;

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
    // numberField = new JFormattedTextField(NumberFormat.getInstance());

    private JButton acceptButton = null;
    private JButton cancelButton = null;
    private JLabel overlapLabel = null;

    //	private JLabel measureUnitLabel = null;
    //	private JComboBox measureUnitCombo = null;
    //	private JLabel widthLabel = null;
    //	private JTextField widthText = null;
    //	private JLabel heightLabel = null;
    //	private JTextField heightText = null;

    private View view = null;

    /**
     * This is the default constructor
     */
    public MapSheetsSettingsPanel(View v) {
	super();
	view = v;
	// mps = new MapSheetsSettings(v);
	// ViewPort vp = v.getMapControl().getViewPort().cloneViewPort();
	// vp.setImageSize(v.getMapControl().getViewPort().getImageSize());
	// vp.setExtent(v.getMapControl().getViewPort().getAdjustedExtent());
	// mps.setOriginalViewPort(vp);
	initialize();
    }

    public View getView() {
	return view;
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
	selectedTemplate= AudasaPreferences.A3_DIMENSIONES;
	this.setSize(WIDTH, HEIGHT);
	this.setLayout(null);
	this.add(getAreaPanel(), null);
	// this.add(getLayoutPanel(), null);
	//this.add(getScalePanel(), null);
	this.add(getTemplatesPanel(), null);
	// this.add(getBaseLayerPanel(), null);
	// this.add(getMetaboxPanel(), null);
	this.add(getAcceptButton(), null);
	this.add(getCancelButton(), null);

	this.loadLayersCombo(getVectLayerToUseCB(), true);
	// this.loadLayersCombo(getBackLayerCB(), false);

	if (getVectLayerToUseCB().getItemCount() == 0) {
	    // getCoverViewRB().setSelected(true);
	    getBasedOnFeaturesRB().setEnabled(false);
	    getVectLayerToUseCB().setEnabled(false);
	    getSelectedOnlyCB().setEnabled(false);
	} else {
	    getBasedOnFeaturesRB().setEnabled(true);
	    getVectLayerToUseCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	    getSelectedOnlyCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	}
    }



    /**
     * This method initializes areaPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getAreaPanel() {
	if (areaPanel == null) {
	    areaPanel = new JPanel();
	    areaPanel.setLayout(null);
	    areaPanel.setBounds(new Rectangle(5, 5, 631, 157));
	    String bor_txt = PluginServices.getText(this, "Area_selection");
	    areaPanel.setBorder(BorderFactory.createTitledBorder(null, bor_txt, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));

	    areaPanel.add(getCoverViewRB(), null);
	    areaPanel.add(getBasedOnFeaturesRB(), null);
	    areaPanel.add(getVectLayerToUseCB(), null);
	    areaPanel.add(getSelectedOnlyCB(), null);
	    areaPanel.add(getScalePanel(), null);
	    areaPanel.add(getGridCustomRB(), null);
	    areaPanel.add(getGridFileTF(), null);
	    areaPanel.add(getGridFileB(), null);

	    gridFileChooser = new JFileChooser(OPEN_TEMPLATE_FILE_CHOOSER_ID, System.getProperty("user.dir"));
	    gridFileChooser.setAcceptAllFileFilterUsed(false);
	    gridFileChooser.setFileFilter(new SimpleFileFilter("grid",
		    PluginServices.getText(MapSheetsUtils.class, "grid_files")));

	    ArrayList rbb = new ArrayList();
	    rbb.add(getCoverViewRB());
	    rbb.add(getBasedOnFeaturesRB());
	    rbb.add(getGridCustomRB());
	    MapSheetsUtils.joinRadioButtons(rbb);
	}
	return areaPanel;
    }





    /**
     * This method initializes scalePanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getScalePanel() {
	if (scalePanel == null) {
	    scalePanel = new JPanel();
	    scalePanel.setLayout(null);
	    scalePanel.setBounds(new Rectangle(8, 110+13+40-112, 615, 68));//+120-15));

	    String bor_txt = PluginServices.getText(this, "Choose_scale_or_grid_division");
	    scalePanel.setBorder(BorderFactory.createTitledBorder(null, bor_txt, TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
	    ((TitledBorder) scalePanel.getBorder()).setTitleColor(UIManager.getColor("Label.foreground"));

	    scalePanel.add(getScaleLabel(), null);
	    scalePanel.add(getOneLabel(), null);
	    scalePanel.add(getScaleField(), null);

	    // scalePanel.add(getStandardScaleCB(), null);

	    scalePanel.add(getOverlapLabel(), null);
	    scalePanel.add(getOverlapSpinner(), null);

	    //			scalePanel.add(getMeasureUnitLabel(), null);
	    //			scalePanel.add(getMeasureUnitCombo(), null);
	    //			scalePanel.add(getWidthLabel(), null);
	    //			scalePanel.add(getWidthText(), null);
	    //			scalePanel.add(getHeightLabel(), null);
	    //			scalePanel.add(getHeightText(), null);

	}
	return scalePanel;
    }



    /**
     * This method does setEnabled onto all scalePanel's components
     */
    private void setEnabledScalePanel(boolean enabled) {
	for (Component component:scalePanel.getComponents()) {
	    component.setEnabled(enabled);
	    if (component instanceof JTextField) {
		Color tFColor = enabled
			? UIManager.getColor("TextField.background")
				: UIManager.getColor("TextField.inactiveBackground");
			component.setBackground(tFColor);
	    }
	}
    }

    private JPanel getTemplatesPanel() {
	if(templatesPanel == null) {
	    templatesPanel = new JPanel();
	    templatesPanel.setLayout(null);
	    templatesPanel.setBounds(new Rectangle(5, 165+13+40-112+68, 631, 21*4+30));
	    templatesPanel.setBorder(BorderFactory.createTitledBorder(null,
		    PluginServices.getText(this, "Template_type"),
		    TitledBorder.DEFAULT_JUSTIFICATION,
		    TitledBorder.DEFAULT_POSITION,
		    new Font("Dialog", Font.BOLD, 12),
		    new Color(51, 51, 51)));

	    templateDimensiones = new JRadioButton("Audasa");
	    templateDimensiones.setBounds(new Rectangle(15, 21, 200-15-15, 21));
	    templateDimensiones.setSelected(true);
	    templateDimensiones.addActionListener(this);
	    templateConsultas = new JRadioButton("Autoestradas");
	    templateConsultas.setBounds(new Rectangle(15+200, 21, 200-15-15, 21));
	    templateConsultas.setSelected(false);
	    templateConsultas.addActionListener(this);
//	    templatePolicia = new JRadioButton(AudasaPreferences.POLICIA_MARGENES);
//	    templatePolicia.setBounds(new Rectangle(15+200+200, 21, 200-15-15, 21));
//	    templatePolicia.setSelected(false);
//	    templatePolicia.addActionListener(this);
	    String[] plantillas = new String[10];
	    plantillas[0] = AudasaPreferences.A4_CONSULTAS;
	    plantillas[1] = AudasaPreferences.A4_CONSULTAS_LOCALIZADOR;
	    plantillas[2] = AudasaPreferences.A3_CONSULTAS;
	    plantillas[3] = AudasaPreferences.A3_CONSULTAS_LOCALIZADOR;
//	    templatesConsultas = new DefaultComboBoxModel(plantillas);
//	    plantillas = new String[2];
	    plantillas[4] = AudasaPreferences.A3_DIMENSIONES;
	    plantillas[5] = AudasaPreferences.A3_DIMENSIONES_LOCALIZADOR;
//	    templatesDimensiones = new DefaultComboBoxModel(plantillas);
//	    plantillas = new String[4];
	    plantillas[6] = AudasaPreferences.A4_POLICIA_MARGENES;
	    plantillas[7] = AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA;
	    plantillas[8] = AudasaPreferences.A3_POLICIA_MARGENES;
	    plantillas[9] = AudasaPreferences.A3_POLICIA_MARGENES_LEYENDA;
	    templatesPolicia = new DefaultComboBoxModel(plantillas);
	    templateSpecificLabel = new JLabel(PluginServices.getText(this, "Choose_template"));
	    templateSpecificLabel.setBounds(new Rectangle(95, 21+25, 150, 21));
	    templateSpecific = new JComboBox();
	    templateSpecific.addActionListener(this);
	    templateSpecific.setModel(templatesPolicia);
	    templateSpecific.setBounds(new Rectangle(200, 21+25, 300, 21));
//	    templateCustom = new JRadioButton(PluginServices.getText(this, "Other_template"));
//	    templateCustom.setBounds(new Rectangle(15, 21+25+28, 110, 21));
//	    templateCustom.setSelected(false);
//	    templateCustom.addActionListener(this);
//	    templateFile = new JTextField(200);
//	    templateFile.setEditable(false);
//	    templateFile.setEnabled(false);
//	    templateFile.setBounds(new Rectangle(125, 21+25+28, 270, 22));
//	    templateFileButton = new JButton("...");
//	    templateFileButton.setBounds(new Rectangle(400, 21+25+28, 50, 21));
//	    templateFileButton.addActionListener(this);
//	    templateFileButton.setEnabled(false);

//	    templateFileChooser = new JFileChooser(OPEN_TEMPLATE_FILE_CHOOSER_ID, System.getProperty("user.home"));
//	    templateFileChooser.setAcceptAllFileFilterUsed(false);
//	    templateFileChooser.setFileFilter(new FileNameExtensionFilter("GVT", "gvt"));
//
//	    formatLabel = new JLabel();
//	    formatLabel.setText(PluginServices.getText(this, "Format"));
//	    formatLabel.setBounds(new Rectangle(465, 21+25+28, 60, 21));
//	    formatLabel.setEnabled(false);
//	    formatCombobox = new JComboBox();
//	    formatCombobox.addItem(new String(" "));
//	    formatCombobox.addItem(new String("A4"));
//	    formatCombobox.addItem(new String("A3"));
//	    formatCombobox.setBounds(new Rectangle(525, 21+25+28, 40, 21));
//	    formatCombobox.setEnabled(false);

	    templatesPanel.add(templateDimensiones, null);
	    templatesPanel.add(templateConsultas, null);
//	    templatesPanel.add(templatePolicia, null);
	    templatesPanel.add(templateSpecificLabel, null);
	    templatesPanel.add(templateSpecific, null);
//	    templatesPanel.add(templateCustom, null);
//	    templatesPanel.add(templateFile, null);
//	    templatesPanel.add(templateFileButton, null);
//	    templatesPanel.add(formatLabel, null);
//	    templatesPanel.add(formatCombobox, null);

	    ArrayList<JRadioButton> group = new ArrayList<JRadioButton>();
	    group.add(templateDimensiones);
	    group.add(templateConsultas);
//	    group.add(templatePolicia);
//	    group.add(templateCustom);
	    MapSheetsUtils.joinRadioButtons(group);
	}
	return templatesPanel;
    }

    //	private JTextField getHeightText() {
    //		if (heightText == null) {
    //			heightText = new JTextField();
    //			heightText.setDocument(new NumericDocument());
    //			heightText.setBounds(170,28+120-21,130,21);
    //		}
    //		return heightText;
    //	}

    //	private JLabel getHeightLabel() {
    //		if (heightLabel == null) {
    //			heightLabel = new JLabel();
    //			heightLabel.setBounds(25,28+120-21,140,21);
    //			heightLabel.setText(PluginServices.getText(this, "Height_"));
    //		}
    //		return heightLabel;
    //
    //	}

    //	private JTextField getWidthText() {
    //		if (widthText == null) {
    //			widthText = new JTextField();
    //			widthText.setDocument(new NumericDocument());
    //			widthText.setBounds(170,28+40+40-14,130,21);
    //		}
    //		return widthText;
    //	}

    //	private JLabel getWidthLabel() {
    //		if (widthLabel == null) {
    //			widthLabel = new JLabel();
    //			widthLabel.setBounds(25,28+40+40-14,140,21);
    //			widthLabel.setText(PluginServices.getText(this, "Width"));
    //		}
    //		return widthLabel;
    //	}


    //	private JComboBox getMeasureUnitCombo() {
    //		if (measureUnitCombo == null) {
    //			measureUnitCombo = new JComboBox();
    //			measureUnitCombo.addItem(MeasureUnitComboItem.MEASURE_UNIT_CM);
    //			measureUnitCombo.addItem(MeasureUnitComboItem.MEASURE_UNIT_MM);
    //			measureUnitCombo.setBounds(170,28+40-7,130,21);
    //			// measureUnitLabel.setBounds(25,28+40,140,21);
    //			// ngle(25, 28, 100, 21));
    //			// le(170, 28, 130, 21));
    //		}
    //		return measureUnitCombo;
    //	}


    //	private JLabel getMeasureUnitLabel() {
    //		if (measureUnitLabel == null) {
    //			measureUnitLabel = new JLabel();
    //			measureUnitLabel.setBounds(25,28+40-7,140,21);
    //			// ngle(25, 28, 100, 21));
    //			// le(170, 28, 130, 21));
    //			measureUnitLabel.setText(PluginServices.getText(this, "Measure_unit"));
    //		}
    //		return measureUnitLabel;
    //	}


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
	    // scaleText = new JFormattedTextField(NumberFormat.getInstance());
	    scaleText = new JTextField();
	    scaleText.setDocument(new NumericDocument());

	    // scaleText.getDocument().
	    // scaleText = new JTextField();
	    scaleText.setBounds(new Rectangle(170, 28, 130, 21));
	    scaleText.setText("1000");
	}
	return scaleText;

    }

    /**
     * This method initializes userRectRB
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getCoverViewRB() {
	if (coverView == null) {
	    coverView = new JRadioButton();
	    coverView.setText(PluginServices.getText(this, "Cover_view"));
	    coverView.setBounds(new Rectangle(15, 27, 110, 21));
	    coverView.addActionListener(this);
	}
	return coverView;
    }

    /**
     * This method initializes gridCustom
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getGridCustomRB() {
	if (gridCustom == null) {
	    gridCustom = new JRadioButton(PluginServices.getText(this, "Load_grid"));
	    gridCustom.setBounds(new Rectangle(15, 110+13+40-112+75, 200, 21));
	    gridCustom.setSelected(false);
	    gridCustom.addActionListener(this);
	}
	return gridCustom;
    }

    /**
     * This method initializes gridFile
     *
     * @return javax.swing.JTextField
     */
    private JTextField getGridFileTF() {
	if (gridFile == null) {
	    gridFile = new JTextField(200);
	    gridFile.setEditable(false);
	    gridFile.setEnabled(false);
	    gridFile.setBounds(new Rectangle(220, 110+13+40-112+75, 270, 22));
	}
	return gridFile;
    }

    /**
     * This method initializes gridFileButton
     *
     * @return javax.swing.JButton
     */
    private JButton getGridFileB() {
	if (gridFileButton == null) {
	    gridFileButton = new JButton("...");
	    gridFileButton.setBounds(new Rectangle(495, 110+13+40-112+75, 50, 21));
	    gridFileButton.addActionListener(this);
	    gridFileButton.setEnabled(false);
	}
	return gridFileButton;
    }

    /**
     * This method initializes viewRectRB
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getBasedOnFeaturesRB() {
	if (basedOnFeatures == null) {
	    basedOnFeatures = new JRadioButton();
	    basedOnFeatures.setText(PluginServices.getText(this, "Based_on_features"));
	    basedOnFeatures.setBounds(new Rectangle(140, 27, 160, 21));
	    basedOnFeatures.addActionListener(this);
	}
	return basedOnFeatures;
    }

    /**
     * This method initializes horSpinner
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getOverlapSpinner() {
	if (overlapSpinner == null) {
	    overlapSpinner = new JSpinner();
	    JSpinner.NumberEditor editor = (JSpinner.NumberEditor) overlapSpinner.getEditor();
	    editor.getTextField().setEditable(false);
	    editor.getModel().setValue(0);
	    editor.getModel().setMinimum(0);
	    editor.getModel().setMaximum(50);
	    overlapSpinner.setBounds(new Rectangle(430+75, 28, 45, 21));
	}
	return overlapSpinner;
    }



    /**
     * This method initializes acceptButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getAcceptButton() {
	if (acceptButton == null) {
	    acceptButton = new JButton(PluginServices.getText(null, "Aceptar"));
	    acceptButton.setBounds(new Rectangle(205, 470-175, 111, 26));
	    acceptButton.setMnemonic(KeyEvent.VK_A);
	    acceptButton.addActionListener(this);
	}
	return acceptButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
	if (cancelButton == null) {
	    cancelButton = new JButton(PluginServices.getText(null, "Cancel"));
	    cancelButton.setBounds(new Rectangle(331, 470-175, 111, 26));
	    cancelButton.setMnemonic(KeyEvent.VK_C);
	    cancelButton.addActionListener(this);
	}
	return cancelButton;
    }

    private WindowInfo winfo = null;

    private final File layoutFile = null;

    private boolean hasCancelled = true;

    private String empresa;

    private static String selectedTemplate = AudasaPreferences.A3_DIMENSIONES;


    public WindowInfo getWindowInfo() {

	if (winfo == null) {
	    winfo = new WindowInfo(WindowInfo.MODALDIALOG);
	    winfo.setTitle(AudasaPreferences.TITULO_VENTANA);
	    winfo.setHeight(HEIGHT);
	    winfo.setWidth(WIDTH);
	}
	return winfo;
    }

    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    private JLabel getOverlapLabel() {
	if (overlapLabel == null) {
	    overlapLabel = new JLabel();
	    overlapLabel.setText(PluginServices.getText(this, "Overlap_or_clearance"));
	    overlapLabel.setBounds(new Rectangle(270+75, 28, 150, 21));
	}
	return overlapLabel;
    }

    public void actionPerformed(ActionEvent e) {

	Object src = e.getSource();
	if (src == this.getAcceptButton()) {
	    hasCancelled = false;
	    if (templateDimensiones.isSelected()) {
		empresa = "Audasa";		
	    } else {
		empresa = "Autoestradas";
	    }
	    try {
		MapSheetGrid newgrid;
		if (getGridCustomRB().isSelected()) {
		    newgrid = MapSheetsUtils.loadMapSheetsGrid(gridFileChooser.getSelectedFile());
		} else {
		    double grid_width;
		    double grid_height;
		    if(selectedTemplate.equals(AudasaPreferences.A4_CONSULTAS) ||
			    selectedTemplate.equals(AudasaPreferences.A4_CONSULTAS_LOCALIZADOR) ||
			    selectedTemplate.equals(AudasaPreferences.A4_POLICIA_MARGENES) ||
			    selectedTemplate.equals(AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA) 
//			    || (templateCustom.isSelected() && formatCombobox.getSelectedItem().equals("A4"))
			    ) {
			grid_width = AudasaPreferences.VIEW_WIDTH_A4;
			grid_height = AudasaPreferences.VIEW_HEIGHT_A4;
		    } else { // any A3 template
			grid_width = AudasaPreferences.VIEW_WIDTH_A3;
			grid_height = AudasaPreferences.VIEW_HEIGHT_A3;
		    }
		    //Long.parseLong(this.getWidthText().getText());
		    //Long.parseLong(this.getHeightText().getText());
		    double w = 1.0 * grid_width;
		    double h = 1.0 * grid_height;
		    //MeasureUnitComboItem muci = (MeasureUnitComboItem) getMeasureUnitCombo().getSelectedItem();
		    MeasureUnitComboItem muci = MeasureUnitComboItem.MEASURE_UNIT_CM;
		    w = 100 * muci.getMetersPerUnit() * w;
		    h = 100 * muci.getMetersPerUnit() * h;

		    Rectangle2D use_map_r = new Rectangle2D.Double(0,0,w,h);
		    long s = Long.parseLong(this.getScaleField().getText());
		    int opc = Integer.parseInt(this.getOverlapSpinner().getValue().toString());

		    FLyrVect lyrv = null;
		    LayerComboItem lci = (LayerComboItem) getVectLayerToUseCB().getSelectedItem();
		    if (lci != null) {
			lyrv = (FLyrVect) lci.getLayer();
		    }

		    ArrayList[] igs_codes = MapSheetsUtils.createFrames(
			    this.coverView.isSelected(),
			    this.selectedOnly.isSelected(),
			    use_map_r,
			    getView().getMapControl().getViewPort(),
			    s, opc, getView().getProjection(), lyrv);

		    ArrayList igs = igs_codes[0];
		    ArrayList cods = igs_codes[1];
		    HashMap atts_hm = null;

		    newgrid = MapSheetGrid.createMapSheetGrid(
			    MapSheetGrid.createNewName(),
			    getView().getProjection(),
			    MapSheetGrid.createDefaultLyrDesc());

		    int sz = igs.size();
		    for (int i=0; i<sz; i++) {
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

			newgrid.addSheet(
				(IGeometry) igs.get(i),
				atts_hm);
		    }
		}

		//before adding the new layer, delete all MapSheetGrids in TOC
		FLayers layersInTOC = getView().getMapControl().getMapContext().getLayers();
		for (int i=0; i<layersInTOC.getLayersCount(); i++) {
		    if (layersInTOC.getLayer(i) instanceof MapSheetGrid) {
			getView().getMapControl().getMapContext().getLayers().removeLayer(layersInTOC.getLayer(i));
		    }
		}
		getView().getMapControl().getMapContext().getLayers().addLayer(newgrid);
		MapSheetsUtils.setOnlyActive(
			newgrid,
			getView().getMapControl().getMapContext().getLayers());

		// getView().getMapControl().getMapContext().
		PluginServices.getMDIManager().closeWindow(this);

	    } catch (Exception exc) {
		JOptionPane.showMessageDialog(
			this,
			exc.getMessage(),
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


	if (src == getCoverViewRB() || src == getBasedOnFeaturesRB() || src == getGridCustomRB()) {
	    getVectLayerToUseCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	    getSelectedOnlyCB().setEnabled(getBasedOnFeaturesRB().isSelected());
	    getGridFileTF().setEnabled(getGridCustomRB().isSelected());
	    getGridFileB().setEnabled(getGridCustomRB().isSelected());
	    setEnabledScalePanel(!getGridCustomRB().isSelected());
	    Color color = getGridCustomRB().isSelected()
		    ? UIManager.getColor("TextField.background")
			    : UIManager.getColor("TextField.inactiveBackground");
		    getGridFileTF().setBackground(color);
		    color = getGridCustomRB().isSelected()
			    ? UIManager.getColor("Label.disabledForeground")
				    : UIManager.getColor("Label.foreground");
			    ((TitledBorder) getScalePanel().getBorder()).setTitleColor(color);
			    getScalePanel().repaint();
	}

//	if(src == templateDimensiones) {
//	    templateSpecific.setEnabled(true);
//	    templateSpecific.setModel(templatesDimensiones);
//	    templateSpecificLabel.setEnabled(true);
//	    selectedTemplate = (templateSpecific.getSelectedItem().toString());
//	    templateFile.setEnabled(false);
//	    templateFileButton.setEnabled(false);
//	    templateFile.setBackground(UIManager.getColor("TextField.inactiveBackground"));
//	}
//
//	if(src == templateConsultas) {
//	    templateSpecific.setEnabled(true);
//	    templateSpecific.setModel(templatesConsultas);
//	    templateSpecificLabel.setEnabled(true);
//	    selectedTemplate = (templateSpecific.getSelectedItem().toString());
//	    templateFile.setEnabled(false);
//	    templateFileButton.setEnabled(false);
//	    templateFile.setBackground(UIManager.getColor("TextField.inactiveBackground"));
//	}
//
//	if(src == templatePolicia) {
//	    templateSpecific.setEnabled(true);
//	    templateSpecific.setModel(templatesPolicia);
//	    templateSpecificLabel.setEnabled(true);
//	    selectedTemplate = (templateSpecific.getSelectedItem().toString());
//	    templateFile.setEnabled(false);
//	    templateFileButton.setEnabled(false);
//	    templateFile.setBackground(UIManager.getColor("TextField.inactiveBackground"));
//	}
//
	if(src == templateSpecific) {
	    selectedTemplate = (templateSpecific.getSelectedItem().toString());
	}
//
//	if (src == templateCustom) {
//	    templateSpecificLabel.setEnabled(false);
//	    templateSpecific.setEnabled(false);
//	    selectedTemplate = templateFile.getText();
//	    templateFile.setEnabled(true);
//	    templateFileButton.setEnabled(true);
//	    templateFile.setBackground(UIManager.getColor("TextField.background"));
//	}
//
//	if (src == templateFileButton) {
//	    int returnVal = templateFileChooser.showOpenDialog(this);
//
//	    if (returnVal == JFileChooser.APPROVE_OPTION) {
//		templateFile.setText(templateFileChooser.getSelectedFile().getAbsolutePath());
//		selectedTemplate = templateFile.getText();
//		if (templateFile.getText().toLowerCase().endsWith(".gvt") &&
//			templateFile.getText().contains("A4")) {
//		    formatCombobox.setSelectedItem("A4");
//		} else if (templateFile.getText().toLowerCase().endsWith(".gvt") &&
//			templateFile.getText().contains("A3")) {
//		    formatCombobox.setSelectedItem("A3");
//		}
//	    }
//	}

	if (src == gridFileButton) {
	    gridFileChooser.setCurrentDirectory(new File(AudasaPreferences.GRIDS_PATH));
	    int returnVal = gridFileChooser.showOpenDialog(this);

	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		gridFile.setText(gridFileChooser.getSelectedFile().getAbsolutePath());
	    }
	}

	checkAcceptButtonEnabled();
//	checkFormatComponentsEnabled();

    }

    private void checkAcceptButtonEnabled() {
	boolean enabled = true;
	if (gridCustom.isSelected()) {
	    enabled &= gridFile.getText().toLowerCase().endsWith(".grid");
	}
//	if (this.templateCustom.isSelected()) {
//	    enabled &= templateFile.getText().toLowerCase().endsWith(".gvt");
//	}
	getAcceptButton().setEnabled(enabled);
    }

//    private void checkFormatComponentsEnabled() {
//	boolean enabled = this.templateCustom.isSelected();
//	if (gridCustom.isSelected()) {
//	    enabled &= false;
//	}
//	if (this.templateCustom.isSelected()) {
//	    enabled &= templateFile.getText().toLowerCase().endsWith(".gvt");
//	}
//	formatLabel.setEnabled(enabled);
//	formatCombobox.setEnabled(enabled);
//    }

    public boolean hasCancelled() {
	return hasCancelled;
    }

    public Layout getMapLayout() {
	if (selectedTemplate.toLowerCase().endsWith(".gvt")) {
	    File templateFile = new File(selectedTemplate);
	    PathGenerator.getInstance().setBasePath(templateFile.getParent());
	    return getLayoutFromFile(templateFile);
	} else {
	    return getLayoutFromFile(AudasaPreferences.getSelectedFile(selectedTemplate));
	}
    }

    public Layout getLayoutFromFile(File layoutFile) {
	Project project = ((ProjectExtension) PluginServices.getExtension(ProjectExtension.class)).getProject();
	Layout layout=null;

	if (!(layoutFile.getPath().endsWith(".gvt") || layoutFile.getPath().endsWith(".GVT"))){
	    layoutFile = new File(layoutFile.getPath()+".gvt");
	}
	try {
	    File xmlFile = new File(layoutFile.getAbsolutePath());
	    FileInputStream is = new FileInputStream(xmlFile);
	    Reader reader = XMLEncodingUtils.getReader(is);

	    XmlTag tag = (XmlTag) XmlTag.unmarshal(reader);
	    System.out.println(empresa);
	    try {
		XMLEntity xml=new XMLEntity(tag);
		fixLogosAndLabels(xml);
		
		if (xml.contains("followHeaderEncoding")) {
		    layout = Layout.createLayout(xml,project);
		}
		else {
		    reader = new FileReader(xmlFile);
		    tag = (XmlTag) XmlTag.unmarshal(reader);
		    xml=new XMLEntity(tag);
		    layout = Layout.createLayout(xml,project);
		}

		return layout;
	    } catch (OpenException e) {
		e.showError();
		return null;
	    }
	    //fPanelLegendManager.setRenderer(LegendFactory.createFromXML(new XMLEntity(tag)));
	} catch (FileNotFoundException e) {
	    NotificationManager.addError(PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	} catch (MarshalException e) {
	    NotificationManager.addError(PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	} catch (ValidationException e) {
	    NotificationManager.addError(PluginServices.getText(this, "Al_leer_la_leyenda"), e);
	    return null;
	}
    }
    
    private XMLEntity find(XMLEntity xml, String key, String value) {
	for (int i = 0 ; i < xml.getChildrenCount(); i++ ) {
	    XMLEntity child = xml.getChild(i);
	    try {
		String stringProperty = child.getStringProperty(key);
		if (stringProperty.contains(value)) {
		    return child;
		} else {
		    XMLEntity c = find(child, key, value);
			if (c!=null) {
			    return c;
			}
		}
	    } catch (NotExistInXMLEntity ne) {
		XMLEntity c = find(child, key, value);
		if (c!=null) {
		    return c;
		}
	    }
	}
	return null;
    }
    private void fixLogosAndLabels(XMLEntity xml) {
	File a = PluginServices.getPluginServices("es.icarto.gvsig.siga").getPluginDirectory();
	String strA = a.getAbsolutePath() + File.separator + "images" + File.separator;
	System.out.println(strA);

	if (empresa.equalsIgnoreCase("autoestradas")) {
	    // AUTOESTRADAS DE GALICIA Concesionaria Xunta Galicia, S.A.
	    XMLEntity b = find(xml, "m_name", "Fomento.png");
	    b.remove("m_path");
	    b.putProperty("m_path", strA + "logo_xunta_map.png");
	    
	    XMLEntity c = find(xml, "m_name", "Audasa.JPG");
	    c.remove("m_path");
	    c.putProperty("m_path", strA + "logo_autoestradas_map.png");
	    
	    XMLEntity d = find(xml, "s", "AUTOPISTAS DEL ATL");
	    d.remove("s");
	    d.putProperty("s", new String[] {"AUTOESTRADAS DE GALICIA", "Concesionaria Xunta Galicia, S.A."});
	    
	    
	} else {
	    // AUTOPISTAS DEL ATLÁNTICO Concesionaria Española, S.A. 
	    XMLEntity b = find(xml, "m_name", "Fomento.png");
	    b.remove("m_path");
	    b.putProperty("m_path", strA + "logo_fomento_map.png");
	    
	    XMLEntity c = find(xml, "m_name", "Audasa.JPG");
	    c.remove("m_path");
	    c.putProperty("m_path", strA + "logo_audasa_report.png");
	    
//	    XMLEntity d = find(xml, "s", "AUTOPISTAS DEL ATL");
	    //d.remove("s");
	    //d.putProperty("s", "AUTOPISTAS DEL ATLÁNTICO\nConcesionaria Española, S.A. ");
	    
	    
	}
	
    }


    public void keyPressed(KeyEvent e) {
	// TODO Auto-generated method stub

    }

    public void keyReleased(KeyEvent e) {
	// TODO Auto-generated method stub

    }

    public void keyTyped(KeyEvent e) {
	// TODO Auto-generated method stub

    }


    public JComboBox getVectLayerToUseCB() {
	if (vecLayerToUse == null) {
	    vecLayerToUse = new JComboBox();
	    vecLayerToUse.setBounds(new Rectangle(310, 27, 150, 21));
	    vecLayerToUse.setEnabled(false);
	    //
	}
	return vecLayerToUse;
    }


    private void loadLayersCombo(JComboBox cmbo,
	    boolean vector_only) {

	cmbo.removeAllItems();

	IProjectView pv = getView().getModel();
	FLayers lyrs = pv.getMapContext().getLayers();
	int[] types = { FShape.POLYGON, FShape.LINE };
	ArrayList list = null;

	try {
	    list = MapSheetsUtils.getLayers(lyrs, types, vector_only);
	} catch (Exception ex) {
	    NotificationManager.addError("While getting vect layers.", ex);
	}

	LayerComboItem item = null;
	for (int i=0; i<list.size(); i++) {

	    try {
		item = new LayerComboItem((FLayer) list.get(i));
		cmbo.addItem(item);
	    } catch (Exception ex) {
		NotificationManager.addError("While adding layer to vector layer list.", ex);
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
	return selectedTemplate;
    }

//    public static String getFormatComboBox() {
//	return formatCombobox.getSelectedItem().toString();
//    }


}  //  @jve:decl-index=0:visual-constraint="0,-2"
