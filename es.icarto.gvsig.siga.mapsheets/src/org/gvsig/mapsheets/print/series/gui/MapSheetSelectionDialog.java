package org.gvsig.mapsheets.print.series.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.utils.FieldComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.LayerComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.NumericDocument;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.ProjectFactory;
import com.iver.cit.gvsig.project.documents.layout.Attributes;
import com.iver.cit.gvsig.project.documents.layout.ProjectMap;
import com.iver.cit.gvsig.project.documents.layout.Size;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.layout.gui.dialogs.FConfigLayoutDialog;

/**
 * This dialog lets user choose a grid and choose
 * active fields from which a new layout template will be created.
 *  
 * @author jldominguez
 *
 */
public class MapSheetSelectionDialog extends JPanel implements IWindow, ActionListener {

	private static Logger logger = Logger.getLogger(MapSheetSelectionDialog.class);

	private static int pg_half_sep = 25;
	
	public static final int WIDTH = 336+50;
	public static final int HEIGHT = 310 + 2*pg_half_sep;
	
	private static final int BUTTON_LEN = 111;
	private static final int BUTTON_SEP = 15;
	
	private WindowInfo winfo = null;
	public WindowInfo getWindowInfo() {
		
		if (winfo == null) {
			winfo = new WindowInfo(WindowInfo.MODALDIALOG);
			winfo.setHeight(HEIGHT);
			winfo.setTitle(PluginServices.getText(this, "Create_layout_template"));
			winfo.setWidth(WIDTH);
		}
		return winfo;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}

	// private JPanel mainPanel = null;
	private JComboBox gridToUseCB = null;
	private JLabel gridLabel = null;
	private JButton acceptButton = null;
	private JButton cancelButton = null;
	
	private MapContext mctxt = null;
	private MapSheetGrid selected = null;
	
	private JLabel marginsLabel = null;
	private JLabel leftLabel = null;
	private JLabel topLabel = null;
	private JTextField leftTextField = null;
	private JTextField topTextField = null;

	private JLabel chooseActiveFieldsLabel = null;
	
	private JCheckBox actFldsChk_1, actFldsChk_2, actFldsChk_3;
	private JComboBox actFldsCom_1, actFldsCom_2, actFldsCom_3;
	private JTextField actFldsTemplate_1,
	actFldsTemplate_2, actFldsTemplate_3;
	
	private JButton  pageSettingsButton = null; 
	private JLabel pageSettingsLabel = null; 
	
	private Layout aux_Layout = null;
	private MapSheetGrid initGrid = null;
	
	public MapSheetSelectionDialog(
			MapContext mc,
			MapSheetGrid ini_grid) {
		this(mc,ini_grid,null);
	}

	private void initAudasaPreferences() {
	    try {
		loadGridsCombo(getGridToUseCB());
		// set grid selected
		selected = getLastGridCreated();
		// set margins
		leftTextField = getLeftText();
		leftTextField.setText("2");
		topTextField = getTopText();
		topTextField.setText("2");
		// set active fields: none
		actFldsChk_1 = getField_1_Chk();
		actFldsChk_1.setSelected(false);
		actFldsChk_2 = getField_1_Chk();
		actFldsChk_2.setSelected(false);
		actFldsChk_3 = getField_1_Chk();
		actFldsChk_3.setSelected(false);
	    
		// let the flow continue
		PluginServices.getMDIManager().closeWindow(this);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    
	}

	public MapSheetSelectionDialog(
			MapContext mc,
			MapSheetGrid ini_grid,
			Layout lyt) {
		
		super();
		initGrid = ini_grid;
		mctxt = mc;
		
		if (lyt == null) {
			ProjectMap pm = ProjectFactory.createMap("");
			aux_Layout = pm.getModel();
		} else {
			aux_Layout = lyt;
		}
//		initialize();
	    initAudasaPreferences();
	}
	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);
		
		this.add(getGridLabel(), null);
		this.add(getGridToUseCB(), null);

		if (true) {
			this.add(getMarginsLabel(), null);
			this.add(getLeftMarginLabel(), null);
			this.add(getTopMarginLabel(), null);
			this.add(getLeftText(), null);
			this.add(getTopText(), null);
			
			this.add(getPageSettButton(), null);
			this.add(getPageSettLabel(), null);
			
			this.add(getActiveFieldsLabel(), null);
			this.add(getField_1_Chk(), null);
			this.add(getField_2_Chk(), null);
			this.add(getField_3_Chk(), null);
			this.add(getField_1_Com(), null);
			this.add(getField_2_Com(), null);
			this.add(getField_3_Com(), null);
			this.add(getField_1_Tem(), null);
			this.add(getField_2_Tem(), null);
			this.add(getField_3_Tem(), null);
			this.add(getAcceptButton(), null);
			this.add(getCancelButton(), null);

			try {
				updatePageDesc();
			} catch (Exception ex) {
				NotificationManager.addError("While updating page desc. ", ex);
			}
		}

		
		try {
			loadGridsCombo(getGridToUseCB());
		} catch (Exception ex) {
			NotificationManager.addError("While loading grid combo. ", ex);
		}
		getAcceptButton().setEnabled(getGridToUseCB().getItemCount() > 0);

	}
	
	private JLabel getPageSettLabel() {
		if (pageSettingsLabel == null) {
			pageSettingsLabel = new JLabel();
			pageSettingsLabel.setBounds(new Rectangle(
					22+180, 22+10+40*2 +pg_half_sep,
					200, 26));
		}
		return pageSettingsLabel;
	}

	
	private JButton getPageSettButton() {
		if (pageSettingsButton == null) {
			pageSettingsButton = new JButton(
					PluginServices.getText(this, "Page_settings_dots"));
			pageSettingsButton.setBounds(new Rectangle(
					22+40, 22+10+40*2 +pg_half_sep,
					130, 26));
			pageSettingsButton.addActionListener(this);
		}
		return pageSettingsButton;
	}
	

	
	

	public Object[] getSelectedAndAuxLayout() {
		Object[] resp = new Object[2];
		resp[0] = selected;
		resp[1] = aux_Layout;
		return resp;
	}

	
	private JLabel getGridLabel() {
		if (gridLabel == null) {
			gridLabel = new JLabel();
			gridLabel.setBounds(20,20,140,21);
			gridLabel.setText(PluginServices.getText(this, "Grid_to_use"));
		}
		return gridLabel;
	}
	
	public JComboBox getGridToUseCB() {
		if (gridToUseCB == null) {
			gridToUseCB = new JComboBox();
			gridToUseCB.setBounds(new Rectangle(210-50, 20, 150+50, 21));
			gridToUseCB.addActionListener(this);
			// gridToUseCB.setEnabled(false);
		}
		return gridToUseCB;
	}





	/**
	 * This method initializes acceptButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAcceptButton() {
		if (acceptButton == null) {
			acceptButton = new JButton(PluginServices.getText(null, "Aceptar"));
			acceptButton.setBounds(new Rectangle(
					(WIDTH - 2*BUTTON_LEN - BUTTON_SEP) / 2,
					HEIGHT-14, 111, 26));
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
			cancelButton.setBounds(new Rectangle(
					WIDTH - BUTTON_LEN - (WIDTH - 2*BUTTON_LEN - BUTTON_SEP) / 2,
					HEIGHT-14, 111, 26));
			cancelButton.addActionListener(this);
		}
		return cancelButton;
	}


	public void actionPerformed(ActionEvent e) {
		
		Object src = e.getSource();
		if (src == this.getAcceptButton()) {
			selected = getLastGridCreated();
			PluginServices.getMDIManager().closeWindow(this);
		}
		
		if (src == this.getCancelButton()) {
			selected = null;
			PluginServices.getMDIManager().closeWindow(this);
		}
		
		if (src == this.getField_1_Chk()) {
			boolean act = getField_1_Chk().isSelected();
			getField_1_Com().setEnabled(act);
			getField_1_Tem().setEnabled(act);
		}
		
		if (src == this.getField_2_Chk()) {
			boolean act = getField_2_Chk().isSelected();
			getField_2_Com().setEnabled(act);
			getField_2_Tem().setEnabled(act);
		}
		
		if (src == this.getField_3_Chk()) {
			boolean act = getField_3_Chk().isSelected();
			getField_3_Com().setEnabled(act);
			getField_3_Tem().setEnabled(act);
		}
		
		if (src == getGridToUseCB()) {
			
			clearFields();
			selected = getLastGridCreated();
			ArrayList fld_descs = null;
			
			try {
				fld_descs = selected.getFieldDescs();
			} catch (Exception e1) {
				NotificationManager.addError("While getting selected item fields. ", e1);
			}
			
			fillCombo(getField_1_Com(), fld_descs);
			fillCombo(getField_2_Com(), fld_descs);
			fillCombo(getField_3_Com(), fld_descs);
		}
		
		if (src == getPageSettButton()) {
	        FConfigLayoutDialog m_configLayout =
	        	new FConfigLayoutDialog(aux_Layout);
	        PluginServices.getMDIManager().addWindow(m_configLayout);
	        updatePageDesc();
		}

	}

	private MapSheetGrid getLastGridCreated() {
	    // as it's possible to create several grids, we will use the last of them 
	    int lastGridCreated = getGridToUseCB().getItemCount() - 1;
	    LayerComboItem sel = (LayerComboItem) getGridToUseCB().getItemAt(lastGridCreated);
	    return (MapSheetGrid) sel.getLayer();
	}
	
	
	private void updatePageDesc() {
		String txt = pageSettingsDesc(this.aux_Layout);
		getPageSettLabel().setText(txt);
	}

	private String pageSettingsDesc(Layout lyt) {

		Attributes atts = lyt.getLayoutContext().getAtributes();
		
		// String aux = Attributes.getMediaSizeNameForId(atts.getType()).toString();
		String resp = "";
		int page_fmt_type = atts.getType();

		Size aux_size = null;
		
		if (page_fmt_type == Attributes.PREPARE_PAGE_ID_CUSTOM) {
			aux_size = atts.getPaperSize();
			resp = resp + " " + MapSheetsUtils.getFormattedDouble(aux_size.getAncho(),2) + 
			" x " + MapSheetsUtils.getFormattedDouble(aux_size.getAlto(),2) + " cm";
		} else {
			
			if (page_fmt_type == Attributes.PREPARE_PAGE_ID_PRINT) {
				aux_size = atts.getPaperSize();

				resp = resp + " " + MapSheetsUtils.getFormattedDouble(aux_size.getAncho(),2) + 
				" x " + MapSheetsUtils.getFormattedDouble(aux_size.getAlto(),2) + " cm";

			} else {
				aux_size = Attributes.sizeForId(page_fmt_type);
				if (atts.getPageFormat().getOrientation() == PageFormat.LANDSCAPE) {
					resp = resp + " " +
					MapSheetsUtils.getFormattedDouble(aux_size.getAlto(),2) + " x " + 
					MapSheetsUtils.getFormattedDouble(aux_size.getAncho(),2) + " cm";
				} else {
					resp = resp + " " +
					MapSheetsUtils.getFormattedDouble(aux_size.getAncho(),2) + " x " + 
					MapSheetsUtils.getFormattedDouble(aux_size.getAlto(),2) + " cm";
				}
			}
		}
		// ==================
		resp = resp + ", " + atts.DPI + " DPI";
		return resp;
	}

	private void fillCombo(JComboBox com, ArrayList flds) {
		int len = flds.size();
		FieldDescription fd = null;
		for (int i=0; i<len; i++) {
			fd = (FieldDescription) flds.get(i);
			com.addItem(FieldComboItem.getComboItem(fd));
		}
	}

	
	
	
	private void clearFields() {
		getField_1_Chk().setSelected(false);
		getField_2_Chk().setSelected(false);
		getField_3_Chk().setSelected(false);

		getField_1_Com().removeAllItems();
		getField_1_Com().setEnabled(false);
		getField_2_Com().removeAllItems();
		getField_2_Com().setEnabled(false);
		getField_3_Com().removeAllItems();
		getField_3_Com().setEnabled(false);

		getField_1_Tem().setText("");
		getField_1_Tem().setEnabled(false);
		getField_2_Tem().setText("");
		getField_2_Tem().setEnabled(false);
		getField_3_Tem().setText("");
		getField_3_Tem().setEnabled(false);
	}

	private void loadGridsCombo(JComboBox cmbo) throws Exception {
		
		cmbo.removeAllItems();
		int sel_ind = -1;
		int len = 0;
		LayerComboItem item = null;

		if (mctxt == null) {

		} else {
			
			FLayers lyrs = mctxt.getLayers();
			len = lyrs.getLayersCount();
			for (int i=0; i<len; i++) {
				if (lyrs.getLayer(i) instanceof MapSheetGrid) {
					item = new LayerComboItem((FLayer) lyrs.getLayer(i));
					cmbo.addItem(item);
					if (initGrid != null && initGrid == lyrs.getLayer(i)) {
						sel_ind = i;
					}
				}
			}
			
		}

		if (sel_ind != -1) {
			cmbo.setSelectedIndex(sel_ind);
		}
		
	}

	private JLabel getMarginsLabel() {
		if (marginsLabel == null) {
			marginsLabel = new JLabel();
			marginsLabel.setBounds(20, 20+40*1, 350, 21);
			marginsLabel.setText(PluginServices.getText(this,
					"Map_position_in_sheet_from_borders_mm"));
		}
		return marginsLabel;
	}
	
	private JLabel getLeftMarginLabel() {
		if (leftLabel == null) {
			leftLabel = new JLabel();
			leftLabel.setBounds(20, 10+40*2, 100, 21);
			leftLabel.setText(PluginServices.getText(this, "Left"));
		}
		return leftLabel;
	}

	
	private JLabel getTopMarginLabel() {
		if (topLabel == null) {
			topLabel = new JLabel();
			topLabel.setBounds(20+160+25, 10+40*2, 100, 21);
			topLabel.setText(PluginServices.getText(this, "Top"));
		}
		return topLabel;
	}
	
	private JTextField getLeftText() {
		if (leftTextField == null) {
			leftTextField = new JTextField();
			leftTextField.setBounds(20+100-25, 10+40*2, 60, 21);
			leftTextField.setDocument(new NumericDocument());
		}
		return leftTextField;
	}

	
	private JTextField getTopText() {
		if (topTextField == null) {
			topTextField = new JTextField();
			topTextField.setBounds(280, 10+40*2, 60, 21);
			topTextField.setDocument(new NumericDocument());
		}
		return topTextField;
	}

	
	private JLabel getActiveFieldsLabel() {
		if (chooseActiveFieldsLabel == null) {
			chooseActiveFieldsLabel = new JLabel();
			chooseActiveFieldsLabel.setBounds(20, 15+40*3+ 2*pg_half_sep, 300, 21);
			chooseActiveFieldsLabel.setText(PluginServices.getText(this,
					"Choose_active_fields"));
		}
		return chooseActiveFieldsLabel;
	}
	
	private JCheckBox getField_1_Chk() {
		if (actFldsChk_1 == null) {
			actFldsChk_1 = new JCheckBox();
			actFldsChk_1.setBounds(25+20, 10+40*4+ 2*pg_half_sep, 35, 21);
			actFldsChk_1.addActionListener(this);
		}
		return actFldsChk_1;
	}
	
	private JCheckBox getField_2_Chk() {
		if (actFldsChk_2 == null) {
			actFldsChk_2 = new JCheckBox();
			actFldsChk_2.setBounds(25+20, 5+40*5+ 2*pg_half_sep, 35, 21);
			actFldsChk_2.addActionListener(this);
		}
		return actFldsChk_2;
	}

	private JCheckBox getField_3_Chk() {
		if (actFldsChk_3 == null) {
			actFldsChk_3 = new JCheckBox();
			actFldsChk_3.setBounds(25+20, 40*6+ 2*pg_half_sep, 35, 21);
			actFldsChk_3.addActionListener(this);
		}
		return actFldsChk_3;
	}
	
	
	
	private JComboBox getField_1_Com() {
		if (actFldsCom_1 == null) {
			actFldsCom_1 = new JComboBox();
			actFldsCom_1.setBounds(25+55, 10+40*4+ 2*pg_half_sep, 100, 21);
			actFldsCom_1.setEnabled(false);
		}
		return actFldsCom_1;
	}
	
	private JComboBox getField_2_Com() {
		if (actFldsCom_2 == null) {
			actFldsCom_2 = new JComboBox();
			actFldsCom_2.setBounds(25+55, 5+40*5+ 2*pg_half_sep, 100, 21);
			actFldsCom_2.setEnabled(false);
		}
		return actFldsCom_2;
	}

	private JComboBox getField_3_Com() {
		if (actFldsCom_3 == null) {
			actFldsCom_3 = new JComboBox();
			actFldsCom_3.setBounds(25+55, 40*6+ 2*pg_half_sep, 100, 21);
			actFldsCom_3.setEnabled(false);
		}
		return actFldsCom_3;
	}

	
	private JTextField getField_1_Tem() {
		if (actFldsTemplate_1 == null) {
			actFldsTemplate_1 = new JTextField();
			actFldsTemplate_1.setBounds(25+170, 10+40*4+ 2*pg_half_sep, 135, 21);
			actFldsTemplate_1.setEnabled(false);
		}
		return actFldsTemplate_1;
	}
	
	private JTextField getField_2_Tem() {
		if (actFldsTemplate_2 == null) {
			actFldsTemplate_2 = new JTextField();
			actFldsTemplate_2.setBounds(25+170, 5+40*5+ 2*pg_half_sep, 135, 21);
			actFldsTemplate_2.setEnabled(false);
		}
		return actFldsTemplate_2;
	}

	private JTextField getField_3_Tem() {
		if (actFldsTemplate_3 == null) {
			actFldsTemplate_3 = new JTextField();
			actFldsTemplate_3.setBounds(25+170, 40*6+ 2*pg_half_sep, 135, 21);
			actFldsTemplate_3.setEnabled(false);
		}
		return actFldsTemplate_3;
	}
	
	public double getLeftMargin() {
		double resp = 0;
		try {
			resp = Double.parseDouble(this.getLeftText().getText());
		} catch (Exception ex) { }
		return resp;
	}

	
	public double getTopMargin() {
		double resp = 0;
		try {
			resp = Double.parseDouble(this.getTopText().getText());
		} catch (Exception ex) { }
		return resp;
	}
	
	public ArrayList getActiveFieldsList() {
		ArrayList resp = new ArrayList();
		FieldDescription fd = null;
		if (this.getField_1_Chk().isSelected()) {
			fd = ((FieldComboItem)
					getField_1_Com().getSelectedItem()).getFieldDescription();
			resp.add(fd);
		}
		
		if (this.getField_2_Chk().isSelected()) {
			fd = ((FieldComboItem)
					getField_2_Com().getSelectedItem()).getFieldDescription();
			resp.add(fd);
		}

		if (this.getField_3_Chk().isSelected()) {
			fd = ((FieldComboItem)
					getField_3_Com().getSelectedItem()).getFieldDescription();
			resp.add(fd);
		}
		return resp;
	}

	
	public ArrayList getActiveFieldsTemplateList() {
		ArrayList resp = new ArrayList();
		if (this.getField_1_Chk().isSelected()) {
			resp.add(this.getField_1_Tem().getText());
		}
		
		if (this.getField_2_Chk().isSelected()) {
			resp.add(this.getField_2_Tem().getText());
		}

		if (this.getField_3_Chk().isSelected()) {
			resp.add(this.getField_3_Tem().getText());
		}
		return resp;
	}

	
	public ArrayList getActiveFieldsIndexList() {
		ArrayList resp = new ArrayList();
		int ind = 0;
		if (this.getField_1_Chk().isSelected()) {
			ind = this.getField_1_Com().getSelectedIndex();
			resp.add(new Integer(ind));
		}
		
		if (this.getField_2_Chk().isSelected()) {
			ind = this.getField_2_Com().getSelectedIndex();
			resp.add(new Integer(ind));
		}

		if (this.getField_3_Chk().isSelected()) {
			ind = this.getField_3_Com().getSelectedIndex();
			resp.add(new Integer(ind));
		}
		return resp;
	}

	


}
