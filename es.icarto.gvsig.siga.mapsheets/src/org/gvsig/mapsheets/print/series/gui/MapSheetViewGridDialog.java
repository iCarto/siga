package org.gvsig.mapsheets.print.series.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.utils.LayerComboItem;
import org.gvsig.mapsheets.print.series.gui.utils.ProjectViewComboItem;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.view.ProjectView;

/**
 * Dialog to associate a new grid  to a layout template.
 * 
 * @author jldominguez
 *
 */
public class MapSheetViewGridDialog extends JPanel implements IWindow, ActionListener {

	private static Logger logger = Logger.getLogger(MapSheetViewGridDialog.class);

	private static final int label_w = 90;
	private static final int button_w = 111;
	private static final int combo_w = 150;
	private static final int label_h = 21;
	private static final int button_h = 26;
	private static final int margin = 20;
	private static final int sep = 15;
	
	public static final int WIDTH = 2 * margin + sep + label_w + combo_w;
	public static final int HEIGHT = 3 * margin + 2 * label_h + button_h + sep - 35;
	
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

	private JComboBox gridToUseCB = null;
	private JLabel gridLabel = null;
	private JComboBox viewToUseCB = null;
	private JLabel viewLabel = null;
	private JButton acceptButton = null;
	private JButton cancelButton = null;
	
	private Project project = null;
	private boolean accepted = false;
	
	public MapSheetViewGridDialog() {
		project = MapSheetsUtils.getProject();
		initialize();
	}
	
	

	private void initialize() {
		
		this.setLayout(null);
		
		this.add(getGridToUseCB());
		this.add(getGridLabel());
		this.add(getViewToUseCB());
		this.add(getViewLabel());
		this.add(getAcceptButton());
		this.add(getCancelButton());
		
		loadWithViews(getViewToUseCB());
		
		if (getViewToUseCB().getItemCount() > 0) {
			ActionEvent ae = new ActionEvent(getViewToUseCB(), 0, null);
			actionPerformed(ae);
		}
		
		
	}

	private void loadWithViews(JComboBox cmb) {

		if (project != null) {
			
			ArrayList alldocs = project.getDocuments();
			int len = alldocs.size();
			ProjectViewComboItem pvci = null;
			for (int i=0; i<len; i++) {
				if (alldocs.get(i) instanceof ProjectView) {
					pvci = new ProjectViewComboItem(
							(ProjectView) alldocs.get(i));
					cmb.addItem(pvci);
				}
			}
		}
	}

	public boolean isAccepted() {
		return accepted;
	}
	
	private void loadWithGridsFrom(JComboBox cmb, ProjectView pvi) {

		cmb.removeAllItems();
		FLayers lyrs = pvi.getMapContext().getLayers();
		ArrayList all_lyr = MapSheetsUtils.getAllLayersFrom(lyrs);
		int len = all_lyr.size();
		LayerComboItem lci = null;
		for (int i=0; i<len; i++) {
			if (all_lyr.get(i) instanceof MapSheetGrid) {
				lci = new LayerComboItem((MapSheetGrid) all_lyr.get(i));
				cmb.addItem(lci);
			}
		}
	}
	
	private void updateAcceptButton() {
		int ic = this.getGridToUseCB().getItemCount();
		this.getAcceptButton().setEnabled(ic > 0);
	}

	public void actionPerformed(ActionEvent e) {
		
		Object src = e.getSource();
		
		if (src == getViewToUseCB()) {
			ProjectViewComboItem pvci =
				(ProjectViewComboItem) getViewToUseCB().getSelectedItem();
			loadWithGridsFrom(getGridToUseCB(), pvci.getProjectView());
			updateAcceptButton();
		}
		
		if (src == getAcceptButton()) {
			accepted = true;
			PluginServices.getMDIManager().closeWindow(this);
		}
		
		if (src == getCancelButton()) {
			accepted = false;
			PluginServices.getMDIManager().closeWindow(this);
		}
	}

	public JComboBox getGridToUseCB() {
		if (gridToUseCB == null) {
			gridToUseCB = new JComboBox();
			gridToUseCB.addActionListener(this);
			gridToUseCB.setBounds(margin+label_w+sep,margin+label_h+sep,combo_w,label_h);
		}
		return gridToUseCB;
	}

	public JLabel getGridLabel() {
		if (gridLabel == null) {
			gridLabel = new JLabel();
			gridLabel.setText(PluginServices.getText(this, "Rejilla"));
			gridLabel.setBounds(margin,margin+label_h+sep,label_w,label_h);
		}
		return gridLabel;
	}

	public JComboBox getViewToUseCB() {
		if (viewToUseCB == null) {
			viewToUseCB = new JComboBox();
			viewToUseCB.addActionListener(this);
			viewToUseCB.setBounds(margin+label_w+sep,margin,combo_w,label_h);
		}
		return viewToUseCB;
	}

	public JLabel getViewLabel() {
		if (viewLabel == null) {
			viewLabel = new JLabel();
			viewLabel.setText(PluginServices.getText(this, "View"));
			viewLabel.setBounds(margin,margin,label_w,label_h);
		}
		return viewLabel;
	}

	public JButton getAcceptButton() {
		if (acceptButton == null) {
			acceptButton = new JButton();
			acceptButton.setText(PluginServices.getText(this, "Accept"));
			acceptButton.addActionListener(this);
			acceptButton.setBounds(
					(WIDTH-2*button_w-sep) / 2,
					HEIGHT-margin-button_h+37,
					button_w, button_h);
			acceptButton.setEnabled(false);
		}
		return acceptButton;
	}

	public JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(PluginServices.getText(this, "Cancel"));
			cancelButton.addActionListener(this);
			cancelButton.setBounds(
					WIDTH - (button_w + (WIDTH-2*button_w-sep) / 2),
					HEIGHT-margin-button_h+37,
					button_w, button_h);
		}
		return cancelButton;
	}
	
	public MapSheetGrid getGrid() {
		
		if (getGridToUseCB().getItemCount() > 0) {
			LayerComboItem lci = (LayerComboItem) getGridToUseCB().getSelectedItem();
			if (lci.getLayer() instanceof MapSheetGrid) {
				return (MapSheetGrid) lci.getLayer(); 
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	public ProjectView getView() {
		
		if (getViewToUseCB().getItemCount() > 0) {
			ProjectViewComboItem pvci = (ProjectViewComboItem) getViewToUseCB().getSelectedItem();
			return pvci.getProjectView();
		} else {
			return null;
		}
		
	}

	
	

}
