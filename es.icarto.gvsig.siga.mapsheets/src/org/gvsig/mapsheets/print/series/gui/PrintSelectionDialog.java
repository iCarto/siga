package org.gvsig.mapsheets.print.series.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.gvsig.gui.beans.swing.JFileChooser;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;
import org.gvsig.mapsheets.print.series.gui.utils.LayerComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.SheetComboItem;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.project.documents.view.ProjectView;

/**
 * Dialog to browse map sheets (layout template will refresh)
 * and print them with various options.
 * 
 * @author jldominguez
 *
 */
public class PrintSelectionDialog extends JPanel implements IWindow, ActionListener, ListSelectionListener {

    private static Logger logger = Logger.getLogger(PrintSelectionDialog.class);
    private static String PDF_OUTPUT_DIR_FILE_CHOOSER_ID = "PDF_OUTPUT_DIR_FILE_CHOOSER_ID";

    private MapSheetsLayoutTemplate layout_template = null;
    private WindowInfo winfo = null;

    private JPanel sheetListPanel = null;
    private JPanel otherSettingsPanel = null;
    private JPanel buttonsPanel = null;

    private JList sheetList = null;
    private JRadioButton printAllRB = null;
    private JRadioButton printSelRB = null;
    private JCheckBox useThisBackLayerChk = null;
    private JComboBox backLayerCombo = null;

    private JCheckBox highlightSelectionCheckbox;

    private JCheckBox printPdfCheckbox;
    private JLabel pdfOutputDirLabel;
    private JTextField pdfOutputDirTextfield;
    private JButton pdfOutputDirButton;
    private JFileChooser pdfOutputDirFileChooser;
    private JLabel pdfFilesPrefixLabel;
    private JTextField pdfFilesPrefixTextfield;

    private JButton printPrinterButton = null;
    private JCheckBox winPrinterSettsChk = null;
    private JPanel winPrinterSettsPanel = null;
    private boolean isWindows = false;

    private JButton closeButton = null;
    private JScrollPane sheetScroll = null;

    public WindowInfo getWindowInfo() {

	if (winfo == null) {
	    winfo = new WindowInfo(WindowInfo.MODELESSDIALOG);
	    winfo.setTitle(PluginServices.getText(this, "Browse_print_sheets"));
	    winfo.setHeight(otherSettingsPanel.getPreferredSize().height + buttonsPanel.getPreferredSize().height);
	    winfo.setWidth(otherSettingsPanel.getPreferredSize().width + sheetListPanel.getPreferredSize().width);
	}
	return winfo;
    }


    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    public PrintSelectionDialog(MapSheetsLayoutTemplate lyt_template) {
	super();
	layout_template = lyt_template;
	init();
    }


    private void init() {

	isWindows = findOutWindows();

	this.setLayout(new BorderLayout());

	this.add(getOtherSettingsPanel(), BorderLayout.EAST);

	try {
	    this.add(getSheetListPanel(), BorderLayout.CENTER);
	} catch (Exception ex) {
	    NotificationManager.addError("Getting sheet list. ", ex);
	}

	this.add(getButtonsPanel(), BorderLayout.SOUTH);

	try {
	    loadBackListCombo(getBackLayerCombo());
	} catch (Exception ex) {
	    NotificationManager.addError("While loading BG layer combo. ", ex);
	}

    }


    private boolean findOutWindows() {
	String osname = System.getProperty("os.name");
	osname = osname.toLowerCase();
	return (osname.indexOf("windows") != -1);
    }


    private JPanel getButtonsPanel() {

	if (buttonsPanel == null) {

	    buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new FlowLayout());
	    buttonsPanel.add(getCancelButton(), null);

	}
	return buttonsPanel;
    }


    private void loadBackListCombo(JComboBox combo) {

	ProjectView pv = layout_template.getProjectView();
	FLayers lyrs = pv.getMapContext().getLayers();
	addAll(combo, lyrs, false);
    }

    private void addAll(JComboBox cmb, FLayers lyrs, boolean alsothis) {

	if (alsothis) {
	    cmb.addItem(new LayerComboItem(lyrs));
	}

	int n = lyrs.getLayersCount();
	for (int i=0; i<n; i++) {
	    if (lyrs.getLayer(i) instanceof FLayers) {
		addAll(cmb, (FLayers) lyrs.getLayer(i), true);
	    } else {
		cmb.addItem(new LayerComboItem(lyrs.getLayer(i)));
	    }

	}
    }


    private JList getSheetList() throws Exception {
	if (sheetList == null) {

	    MapSheetGrid gr = layout_template.getGrid();
	    int cont = gr.getSource().getShapeCount();
	    DefaultListModel dlm = new DefaultListModel();
	    for (int i=0; i<cont; i++) {
		SheetComboItem item = new SheetComboItem(
			gr.getGraphic(i));
		int position = 0;
		if (dlm.getSize() > 0) {
		    do {
			if (item.toString().compareTo(
				dlm.get(position).toString()) < 0) {
			    break;
			}
			position++;
		    } while (position < dlm.getSize());
		}
		dlm.add(position, new SheetComboItem(
			gr.getGraphic(i)));
	    }
	    sheetList = new JList(dlm);
	    // sheetList.setPreferredSize(new Dimension(100,100));
	    sheetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    sheetList.addListSelectionListener(this);

	    //int[] sheets = MapSheetsUtils.filterMapSheetGridSheets(dlm, gr, "MUNICIPIO", ValueFactory.createValue("TEST"));
	    //sheetList.setSelectedIndices(sheets);

	}
	return sheetList;
    }







    /**
     * This method initializes HighlightSelection checkbox
     *
     * @return javax.swing.JButton
     */
    private JCheckBox getHighlightSelectionCheckBox() {
	if (highlightSelectionCheckbox == null) {
	    highlightSelectionCheckbox = new JCheckBox(PluginServices.getText(null, "Highlight_selection"));
	    highlightSelectionCheckbox.addActionListener(this);
	}
	return highlightSelectionCheckbox;
    }


    /**
     * This method initializes PrintPdf checkbox
     * 
     * @return javax.swing.JButton
     */
    private JCheckBox getPrintPdfCheckBox() {
	if (printPdfCheckbox == null) {
	    printPdfCheckbox = new JCheckBox(PluginServices.getText(null, "Print_as_pdf"));
	    printPdfCheckbox.addActionListener(this);
	}
	return printPdfCheckbox;
    }

    /**
     * This method initializes acceptButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getPrintPrinterButton(boolean less_space) {
	if (printPrinterButton == null) {
	    printPrinterButton = new JButton(PluginServices.getText(null, "Print_button"));
	    printPrinterButton.setPreferredSize(new Dimension(
		    (int) (printPrinterButton.getPreferredSize().width * 2.5),
		    (int) (printPrinterButton.getPreferredSize().height * 1.2)));
	    printPrinterButton.addActionListener(this);
	}
	return printPrinterButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
	if (closeButton == null) {
	    closeButton = new JButton(PluginServices.getText(null, "Close"));
	    closeButton.setPreferredSize(new Dimension(
		    (int) (closeButton.getPreferredSize().width * 2.5),
		    (int) (closeButton.getPreferredSize().height * 1.2)));
	    closeButton.addActionListener(this);
	}
	return closeButton;
    }

    /**
     * This method initializes the PDF output directory label
     *
     * @return javax.swing.JLabel
     */
    private JLabel getPdfOutputDirLabel() {
	if (pdfOutputDirLabel == null) {
	    pdfOutputDirLabel = new JLabel(PluginServices.getText(null, "Print_in_label"));
	    pdfOutputDirLabel.setEnabled(false);
	}
	return pdfOutputDirLabel;
    }

    /**
     * This method initializes the text field which displays the chosen PDF output directory
     *
     * @return javax.swing.JTextField
     */
    private JTextField getPdfOutputDirTextField() {
	if (pdfOutputDirTextfield == null) {
	    pdfOutputDirTextfield = new JTextField(System.getProperty("user.home"), 15);
	    pdfOutputDirTextfield.setEditable(false);
	    pdfOutputDirTextfield.setEnabled(false);
	}
	return pdfOutputDirTextfield;
    }

    /**
     * This method initializes the PDF output directory selection button
     *
     * @return javax.swing.JButton
     */
    private JButton getPdfOutputDirButton() {
	if (pdfOutputDirButton == null) {
	    pdfOutputDirButton = new JButton("...");
	    pdfOutputDirButton.setEnabled(false);
	    pdfOutputDirButton.addActionListener(this);
	}
	return pdfOutputDirButton;
    }

    /**
     * This method initializes the name prefix for the PDF output files label
     *
     * @return javax.swing.JLabel
     */
    private JLabel getPdfFilesPrefixLabel() {
	if (pdfFilesPrefixLabel == null) {
	    pdfFilesPrefixLabel = new JLabel(PluginServices.getText(null, "File_name_prefix"),
		    new ImageIcon(getClass().getClassLoader().getResource("images" + File.separator + "help.png")),
		    JLabel.LEADING);
	    pdfFilesPrefixLabel.setEnabled(false);
	    pdfFilesPrefixLabel.setToolTipText(PluginServices.getText(null, "File_name_prefix_help"));
	}
	return pdfFilesPrefixLabel;
    }


    /**
     * This method initializes the text field which displays the chosen name prefix for the PDF output files
     *
     * @return javax.swing.JTextField
     */
    private JTextField getPdfFilesPrefixTextField() {
	if (pdfFilesPrefixTextfield == null) {
	    pdfFilesPrefixTextfield = new JTextField(PluginServices.getText(this, "sheet"), 15);
	    pdfFilesPrefixTextfield.setEnabled(false);
	    pdfFilesPrefixTextfield.setBackground(UIManager.getColor("TextField.inactiveBackground"));
	}
	return pdfFilesPrefixTextfield;
    }


    private JPanel getOtherSettingsPanel() {

	if (otherSettingsPanel == null) {
	    otherSettingsPanel = new JPanel();
	    GroupLayout layout = new GroupLayout(otherSettingsPanel);
	    otherSettingsPanel.setLayout(layout);
	    layout.setAutoCreateGaps(true);

	    otherSettingsPanel.setBorder(BorderFactory.createTitledBorder(
		    null, PluginServices.getText(this, "Print_options"),
		    TitledBorder.DEFAULT_JUSTIFICATION,
		    TitledBorder.DEFAULT_POSITION,
		    new Font("Dialog", Font.BOLD, 12),
		    new Color(51, 51, 51)));

	    int sepDistance = LayoutStyle.getInstance().getPreferredGap(getPdfOutputDirLabel(),
		    getPdfOutputDirTextField(), LayoutStyle.ComponentPlacement.UNRELATED,
		    SwingConstants.EAST, this);

	    ParallelGroup horizontal = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
	    SequentialGroup vertical = layout.createSequentialGroup();
	    vertical.addGap(LayoutStyle.getInstance().getPreferredGap(getPdfOutputDirLabel(),
		    getPdfOutputDirTextField(), LayoutStyle.ComponentPlacement.INDENT,
		    SwingConstants.EAST, this));
	    horizontal.addComponent(getPrintAllRB());
	    vertical.addComponent(getPrintAllRB());
	    horizontal.addComponent(getPrintSelRB());
	    vertical.addComponent(getPrintSelRB());
	    vertical.addGap(sepDistance);
	    horizontal.addComponent(getUseThisBackLayerChk());
	    vertical.addComponent(getUseThisBackLayerChk());
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
	    horizontal.addComponent(getBackLayerCombo(), GroupLayout.PREFERRED_SIZE,
		    getBackLayerCombo().getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
	    vertical.addComponent(getBackLayerCombo(),GroupLayout.PREFERRED_SIZE,
		    getBackLayerCombo().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
	    vertical.addGap(sepDistance);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
	    horizontal.addComponent(getHighlightSelectionCheckBox());
	    vertical.addComponent(getHighlightSelectionCheckBox());
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
	    vertical.addGap(sepDistance);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
	    horizontal.addComponent(getPrintPdfCheckBox());
	    vertical.addComponent(getPrintPdfCheckBox());
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
	    horizontal.addComponent(getPdfOutputDirLabel());
	    vertical.addComponent(getPdfOutputDirLabel());
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

	    SequentialGroup pdfOutputHorizontal = layout.createSequentialGroup();
	    pdfOutputHorizontal.addGap(LayoutStyle.getInstance().getPreferredGap(getPdfOutputDirLabel(),
		    getPdfOutputDirTextField(), LayoutStyle.ComponentPlacement.INDENT,
		    SwingConstants.EAST, this));
	    pdfOutputHorizontal.addComponent(getPdfOutputDirTextField(), GroupLayout.PREFERRED_SIZE,
		    getPdfOutputDirTextField().getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
	    pdfOutputHorizontal.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
	    pdfOutputHorizontal.addComponent(getPdfOutputDirButton(), GroupLayout.PREFERRED_SIZE,
		    getPdfOutputDirButton().getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
	    horizontal.addGroup(pdfOutputHorizontal);

	    ParallelGroup pdfOutputVertical = layout.createParallelGroup();
	    pdfOutputVertical.addComponent(getPdfOutputDirTextField(), GroupLayout.PREFERRED_SIZE,
		    getPdfOutputDirTextField().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    pdfOutputVertical.addComponent(getPdfOutputDirButton(), GroupLayout.PREFERRED_SIZE,
		    getPdfOutputDirButton().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    vertical.addGroup(pdfOutputVertical);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

	    horizontal.addComponent(getPdfFilesPrefixLabel());
	    vertical.addComponent(getPdfFilesPrefixLabel());
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

	    SequentialGroup pdfPrefixHorizontal = layout.createSequentialGroup();
	    pdfPrefixHorizontal.addGap(LayoutStyle.getInstance().getPreferredGap(getPdfFilesPrefixTextField(),
		    getPdfFilesPrefixTextField(), LayoutStyle.ComponentPlacement.INDENT,
		    SwingConstants.EAST, this));
	    pdfPrefixHorizontal.addComponent(getPdfFilesPrefixTextField(), GroupLayout.PREFERRED_SIZE,
		    getPdfFilesPrefixTextField().getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
	    horizontal.addGroup(pdfPrefixHorizontal);

	    ParallelGroup pdfPrefixVertical = layout.createParallelGroup();
	    pdfPrefixVertical.addComponent(getPdfFilesPrefixLabel(), GroupLayout.PREFERRED_SIZE,
		    getPdfFilesPrefixLabel().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    pdfPrefixVertical.addComponent(getPdfFilesPrefixTextField(), GroupLayout.PREFERRED_SIZE,
		    getPdfFilesPrefixTextField().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    vertical.addGroup(pdfPrefixVertical);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

	    pdfOutputDirFileChooser = new JFileChooser(PDF_OUTPUT_DIR_FILE_CHOOSER_ID,System.getProperty("user.home"));
	    pdfOutputDirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    if (pdfOutputDirFileChooser.getLastPath() != null) {
		getPdfOutputDirTextField().setText(pdfOutputDirFileChooser.getLastPath().getAbsolutePath());
	    }

	    vertical.addGap(sepDistance);
	    vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

	    ParallelGroup printSettings = layout.createParallelGroup(GroupLayout.Alignment.CENTER);

	    printSettings.addComponent(getPrintPrinterButton(getIsWindows()), GroupLayout.PREFERRED_SIZE,
		    getPrintPrinterButton(getIsWindows()).getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
	    vertical.addComponent(getPrintPrinterButton(getIsWindows()), GroupLayout.PREFERRED_SIZE,
		    getPrintPrinterButton(getIsWindows()).getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    if (getIsWindows()) {
		printSettings.addComponent(getWindowsPrinterSettsPanel(), GroupLayout.PREFERRED_SIZE,
			getWindowsPrinterSettsPanel().getPreferredSize().width, GroupLayout.PREFERRED_SIZE);
		vertical.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		vertical.addComponent(getWindowsPrinterSettsPanel(), GroupLayout.PREFERRED_SIZE,
			getWindowsPrinterSettsPanel().getPreferredSize().height, GroupLayout.PREFERRED_SIZE);
	    }

	    // We add gaps before and after the main groups and also a group which contains the leading one and the centered one
	    layout.setHorizontalGroup(layout.createSequentialGroup()
		    .addGap(LayoutStyle.getInstance().getPreferredGap(getPdfOutputDirLabel(),
			    getPdfOutputDirTextField(), LayoutStyle.ComponentPlacement.INDENT, SwingConstants.EAST, this))
			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(horizontal)
				    .addGroup(printSettings)).addGap(LayoutStyle.getInstance().getPreferredGap(getPdfOutputDirLabel(),
					    getPdfOutputDirTextField(), LayoutStyle.ComponentPlacement.INDENT, SwingConstants.EAST, this)));
	    layout.setVerticalGroup(vertical);

	    ArrayList<JRadioButton> lis = new ArrayList<JRadioButton>();
	    lis.add(getPrintAllRB());
	    lis.add(getPrintSelRB());
	    MapSheetsUtils.joinRadioButtons(lis);


	}
	return otherSettingsPanel;
    }


    private JPanel getWindowsPrinterSettsPanel() {

	if (winPrinterSettsPanel == null) {
	    winPrinterSettsPanel = new JPanel();
	    winPrinterSettsPanel.setLayout(new GridLayout(1,2));

	    winPrinterSettsPanel.add(getWindowsPrinterSettsChk());

	    JLabel lbl = new JLabel();
	    lbl.setToolTipText(PluginServices.getText(this, "Tip_show_printer_settings_dlg_before_printing"));
	    ImageIcon ico = PluginServices.getIconTheme().get("printer-opts");
	    lbl.setIcon(ico);
	    winPrinterSettsPanel.add(lbl);
	}
	return winPrinterSettsPanel;
    }

    private JCheckBox getWindowsPrinterSettsChk() {

	if (winPrinterSettsChk == null) {
	    winPrinterSettsChk = new JCheckBox();
	}
	return winPrinterSettsChk;
    }



    private boolean getIsWindows() {
	return isWindows;
    }

    public boolean getUserWantsPrinterSettings() {
	return getWindowsPrinterSettsChk().isSelected();
    }


    private JPanel getSheetListPanel() throws Exception {

	if (sheetListPanel == null) {

	    sheetListPanel = new JPanel();
	    sheetListPanel.setLayout(new BorderLayout());
	    sheetListPanel.setBorder(BorderFactory.createTitledBorder(
		    null, PluginServices.getText(this, "Preview"),
		    TitledBorder.DEFAULT_JUSTIFICATION,
		    TitledBorder.DEFAULT_POSITION,
		    new Font("Dialog", Font.BOLD, 12),
		    new Color(51, 51, 51)));

	    sheetListPanel.add(getSheetsScroll(), BorderLayout.CENTER);
	    sheetListPanel.setPreferredSize(new Dimension(125, 300));
	}
	return sheetListPanel;
    }


    private JScrollPane getSheetsScroll() throws Exception {

	if (sheetScroll == null) {
	    sheetScroll = new JScrollPane(getSheetList());
	    sheetScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}
	return sheetScroll;
    }


    public JRadioButton getPrintAllRB() {
	if (printAllRB == null) {
	    printAllRB = new JRadioButton();
	    printAllRB.setText(PluginServices.getText(this, "Print_all_sheets"));
	    printAllRB.addActionListener(this);
	}
	return printAllRB;
    }


    public JRadioButton getPrintSelRB() {
	if (printSelRB == null) {
	    printSelRB = new JRadioButton();
	    printSelRB.setText(PluginServices.getText(this, "Print_sel_sheets"));
	    printSelRB.addActionListener(this);
	}
	return printSelRB;
    }


    public JCheckBox getUseThisBackLayerChk() {
	if (useThisBackLayerChk == null) {
	    useThisBackLayerChk = new JCheckBox();
	    useThisBackLayerChk.setText(PluginServices.getText(this,
		    "Use_back_layer"));
	    useThisBackLayerChk.addActionListener(this);
	}
	return useThisBackLayerChk;

    }

    private int selectListMapsOnGrid() {

	try {
	    Object[] values = this.getSheetList().getSelectedValues();
	    SelectionSupport selection = layout_template.getGrid().getSelectionSupport();
	    selection.clearSelection();

	    for(Object value:values) {
		SheetComboItem item = (SheetComboItem) value;
		for (int i = 0; i < layout_template.getGrid().getTheMemoryDriver().getShapeCount(); i++) {
		    MapSheetGridGraphic grid_item = layout_template.getGrid().getGraphic(i);
		    if (item.getObject().equals(grid_item)) {
			selection.getSelection().set(i);
		    }
		}
	    }

	    return values.length;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return 0;

    }
    private JComboBox getBackLayerCombo() {
	if (backLayerCombo == null) {
	    backLayerCombo = new JComboBox();
	    backLayerCombo.setEnabled(false);
	    backLayerCombo.setPreferredSize(new Dimension(200, backLayerCombo.getPreferredSize().height));
	}
	return backLayerCombo;
    }


    public void actionPerformed(ActionEvent e) {

	Object src = e.getSource();

	if (src == getUseThisBackLayerChk()) {
	    getBackLayerCombo().setEnabled(getUseThisBackLayerChk().isSelected());
	    return;
	}

	if (src == getPrintPdfCheckBox()) {
	    if (printPdfCheckbox.isSelected()) {
		pdfOutputDirTextfield.setEnabled(true);
		pdfOutputDirButton.setEnabled(true);
		pdfOutputDirTextfield.setBackground(UIManager.getColor("TextField.background"));
		pdfOutputDirLabel.setEnabled(true);
		pdfFilesPrefixLabel.setEnabled(true);
		pdfFilesPrefixTextfield.setEnabled(true);
		pdfFilesPrefixTextfield.setBackground(UIManager.getColor("TextField.background"));
	    } else {
		pdfOutputDirTextfield.setEnabled(false);
		pdfOutputDirButton.setEnabled(false);
		pdfOutputDirTextfield.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		pdfOutputDirLabel.setEnabled(false);
		pdfFilesPrefixLabel.setEnabled(false);
		pdfFilesPrefixTextfield.setEnabled(false);
		pdfFilesPrefixTextfield.setBackground(UIManager.getColor("TextField.inactiveBackground"));
	    }
	}

	if (src == pdfOutputDirButton) {
	    int returnVal = pdfOutputDirFileChooser.showOpenDialog(this);

	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		pdfOutputDirTextfield.setText(pdfOutputDirFileChooser.getSelectedFile().getAbsolutePath());
	    }

	}

	if (src == getPrintPrinterButton(getIsWindows())) {

	    if (!this.getPrintAllRB().isSelected()) {
		selectListMapsOnGrid();
	    }

	    if (printPdfCheckbox.isSelected()) {

		//				if (!new File(pdfOutputDirTextfield.getText()).canWrite()) {
		//					JOptionPane.showMessageDialog(this,
		//							PluginServices.getText(this, "Directory_not_writable"),
		//							PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
		//					return;
		//
		//				}
		File file = new File(pdfOutputDirTextfield.getText() + File.separator + "test");
		try {
		    file.createNewFile();
		    file.delete();
		} catch (IOException e1) {
		    JOptionPane.showMessageDialog(this,
			    PluginServices.getText(this, "Directory_not_writable"),
			    PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
		    return;
		}

		LayerComboItem sel_back = null;

		if (this.getUseThisBackLayerChk().isSelected()) {
		    sel_back = (LayerComboItem) getBackLayerCombo().getSelectedItem();
		}

		PrintTaskWindow.startPrintTask(
			layout_template,
			this.getPrintAllRB().isSelected(),
			highlightSelectionCheckbox.isSelected(),
			sel_back == null ? null : sel_back.getLayer(),
				pdfOutputDirTextfield.getText(),
				pdfFilesPrefixTextfield.getText(), this, false);
		getBackLayerCombo().setEnabled(getUseThisBackLayerChk().isSelected());
		return;

	    } else {

		LayerComboItem sel_back = null;
		FLayer b_l = null;

		PluginServices.getMDIManager().closeWindow(this);

		JOptionPane.showMessageDialog(
			layout_template,
			PluginServices.getText(this, "Se_va_a_cerrar_mapa_para_acelerar_proceso"),
			PluginServices.getText(this, "Print"),
			JOptionPane.INFORMATION_MESSAGE);

		PluginServices.getMDIManager().closeWindow(layout_template);

		if (this.getUseThisBackLayerChk().isSelected()) {
		    sel_back = (LayerComboItem) getBackLayerCombo().getSelectedItem();
		}

		if (sel_back != null) {
		    b_l = sel_back.getLayer();
		    b_l.setVisible(true);
		    MapSheetsUtils.addBackLayer(layout_template, b_l);
		}

		boolean p_all = getPrintAllRB().isSelected();
		layout_template.setPrintSelectedOnly(!p_all);

		MapSheetsUtils.printMapSheetsLayout(layout_template,
			sel_back == null ? null : sel_back.getLayer(),
				getUserWantsPrinterSettings());

		if (b_l != null) {
		    MapSheetsUtils.removeLayer(layout_template, b_l);
		}

	    }
	}

	if (src == getCancelButton()) {
	    PluginServices.getMDIManager().closeWindow(this);
	}

    }


    public void valueChanged(ListSelectionEvent e) {

	Object src = e.getSource();

	try {
	    if ((src == this.getSheetList()) && (this.getSheetList().getSelectedValues().length == 1)) {
		SheetComboItem sci = (SheetComboItem) getSheetList().getSelectedValue();
		layout_template.updateWithSheet(sci.getObject());
		String code = getSheetList().getSelectedValue().toString();
		layout_template.updateAudasaSheetCode(code);
	    }
	} catch (Exception ex) {
	    NotificationManager.addError("Getting sheet list. ", ex);
	}

    }


}
