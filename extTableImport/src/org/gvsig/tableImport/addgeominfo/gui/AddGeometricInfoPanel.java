package org.gvsig.tableImport.addgeominfo.gui;

/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.gvsig.fmap.swing.toc.TOC;
import org.gvsig.gui.beans.AcceptCancelPanel;
import org.gvsig.gui.beans.comboboxconfigurablelookup.DefaultComboBoxConfigurableLookUpModel;
import org.gvsig.gui.beans.comboboxconfigurablelookup.JComboBoxConfigurableLookUp;
import org.gvsig.gui.beans.incrementabletask.IncrementableTask;
import org.gvsig.gui.beans.progresspanel.ProgressPanel;
import org.gvsig.gui.beans.specificcaretposition.JTextFieldWithSCP;
import org.gvsig.gui.javax.swing.jLabelCellRenderer.JLabelCellRenderer;
import org.gvsig.tableImport.addgeominfo.GeomInfo;
import org.gvsig.tableImport.addgeominfo.GeomInfoFactory;
import org.gvsig.tableImport.addgeominfo.process.AddGeometricInfoProcess;
import org.gvsig.tableImport.addgeominfo.util.FShapeTypeNames;
import org.gvsig.tableImport.addgeominfo.util.StringUtilitiesExtended;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.table.gui.TablesFor;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.project.documents.view.toc.TocItemBranch;
import com.iver.utiles.swing.JComboBox;

/**
 * <p>Panel where user will select the geometric information of the layer, to be added.</p>
 *
 * @version 23/05/08
 *
 * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
 */
public class AddGeometricInfoPanel extends JPanel implements IWindow {
	private static final long serialVersionUID = 3474731288674500621L;

	private View view;
	private MapControl mapControl;
	private JLabel layersLabel = null;
	private JScrollPane southWestScrollPane = null;
    private JComboBox layersComboBox = null;
    private JPanel northPanel = null;
    private JPanel southPanel = null;
    private JPanel layersPanel = null;
    private JPanel southEastPanel = null;
    private JPanel southCenterPanel = null;
    private JPanel southEastCenterPanel = null;
    private JLabel isEditableLabel = null;
	private AdaptedAcceptCancelPanel acceptCancelPanel = null;
	private JScrollPane addedAttributesScrollPane = null;
    private JCheckBox newColumnCheckBox = null;
    private JTextFieldWithSCP nameOfColumnText = null;
    private JComboBoxConfigurableLookUp nameOfColumnCombo = null;
    private JList attributesList = null;
    private JList attributesAddedList = null;

	private ImageIcon bIcon;
	private ImageIcon rightIcon;
	private ImageIcon leftIcon;
	private ImageIcon doubleRightIcon;
	private ImageIcon doubleLeftIcon;
	private ImageIcon iconYes;
	private ImageIcon iconNo;
	private ImageIcon reloadIcon;
	private ImageIcon saveGPIcon;
	private JButton rightIconButton;
	private JButton leftIconButton;
	private JButton doubleRightIconButton;
	private JButton doubleLeftIconButton;
	private JButton reloadIconButton;
	private JButton saveGeomPropertyButton;
	private JComponent currentColumnNameComponent;

	private GeomInfo currentField;

    private WindowInfo viewInfo = null;
    private final short Window_Width = 550;
    private final short Window_Height = 330;
    private final int layersComboBox_Width = 310;
    private final int layersComboBox_Height = 22;
    private HashSet fieldNames = new HashSet();

    // Layer types:
    private final short UNDEFINED = -1;
    // End Layer types

    private int previous_Type = UNDEFINED;

    // Field name component type
//    private final Class TEXT_TYPE = JTextFieldWithSCP.class;
//    private final Class COMBO_TYPE = JComboBoxConfigurableLookUp.class;
    // End field name component type

    /**
     * <p>Creates a new form where user could select the geometric information to add.</p>
     */
    public AddGeometricInfoPanel(View view) {
    	super();

    	this.view = view;
    	this.mapControl = view.getMapControl();

    	initialize();
    }

    /**
     * <p>Initializes this component.</p>
     */
    private void initialize() {
		setLayout(new FlowLayout());

		currentField = null;

		bIcon = PluginServices.getIconTheme().get("layer-group");

    	add(getNorthPanel());
    	add(getSouthPanel());
    	add(getAdaptedAcceptCancelPanel());

    	getAdaptedAcceptCancelPanel().setOkButtonEnabled(false);

		setVisible(false);
    	if (!refreshVisibleVectorLayers()) {
    		JOptionPane.showMessageDialog(this, PluginServices.getText(this, "No_vector_layer_can_be_save_changes"), PluginServices.getText(this, "Warning"), JOptionPane.WARNING_MESSAGE);
    	} else {
    		setVisible(true);
    		PluginServices.getMDIManager().addWindow(this);
    	}
    }

    /**
     * <p>This method initializes acceptCancelPanel.</p>
     *
     * @return an adapted {@link AcceptCancelPanel AcceptCancelPanel}
     */
    private AdaptedAcceptCancelPanel getAdaptedAcceptCancelPanel() {
    	if (acceptCancelPanel == null) {
    		acceptCancelPanel = new AdaptedAcceptCancelPanel();
    	}

    	return acceptCancelPanel;
    }

    /**
	 * <p>This method initializes northPanel.</p>
	 *
	 * @return javax.swing.JPanel
     */
    private JPanel getNorthPanel() {
    	if (northPanel == null) {
    		northPanel = new JPanel();
    		northPanel.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(this, "Select_layer")));

    		northPanel.setPreferredSize(new Dimension(540, 65));
    		northPanel.add(getLayersPanel());
    		northPanel.add(getIsEditableLabel());
    	}

    	return northPanel;
     }

    /**
	 * <p>This method initializes isEditableLabel.</p>
	 *
	 * @return javax.swing.JLabel
     */
    private JLabel getIsEditableLabel() {
    	if (isEditableLabel == null) {
    		isEditableLabel = new JLabel(PluginServices.getText(this, "Writable"));
    		isEditableLabel.setToolTipText(PluginServices.getText(this, "is_editable_TOOLTIP_HTML_explanation"));
    		isEditableLabel.setPreferredSize(new Dimension(100, 28));
    		iconYes = PluginServices.getIconTheme().get("button-ok-icon");
    		iconNo = PluginServices.getIconTheme().get("button-cancel-icon");
    	}

    	return isEditableLabel;
    }

    /**
     * <p>Updates the lists with the geometric properties of <code>layer</code>.</p>
     *
     * @param layer a vector layer
     */
    private void updateEditableLabel(FLyrVect layer) {
    	if (layer.isWritable()) {
    		// Only can save the changes if layer is writable and its driver is of a PostGree database,
    		//  shape, or GML
    		VectorialDriver vd = layer.getSource().getDriver();

    		if (vd == null)
    			return;

    		if ( vd.getName().equalsIgnoreCase("gvSIG shp driver")
    				|| vd.getName().equalsIgnoreCase("PostGIS JDBC Driver")
    				|| vd.getName().equalsIgnoreCase("gvSIG GML Memory Driver")) {
	    		isEditableLabel.setIcon(iconYes);
				refreshFields(layer);
    		}
    		else {
    			isEditableLabel.setIcon(iconYes);
    			setEnabledFieldAttributeComponents(false);

    			DefaultListModel attrsListModel = (DefaultListModel) getAttributesList().getModel();
    			attrsListModel.removeAllElements();

    			DefaultListModel attrsAddedListModel = (DefaultListModel) getAttributesAddedList().getModel();
    			attrsAddedListModel.removeAllElements();

    			JOptionPane.showMessageDialog(this, PluginServices.getText(this, "gvSIG_cant_save_changes_in_this_kind_of_format"), PluginServices.getText(this, "Information"), JOptionPane.INFORMATION_MESSAGE);
    		}
    	}
    	else {
    		isEditableLabel.setIcon(iconNo);
			setEnabledFieldAttributeComponents(false);

			DefaultListModel attrsListModel = (DefaultListModel) getAttributesList().getModel();
			attrsListModel.removeAllElements();

			DefaultListModel attrsAddedListModel = (DefaultListModel) getAttributesAddedList().getModel();
			attrsAddedListModel.removeAllElements();
    	}
    }

    /**
	 * <p>This method initializes southPanel.</p>
	 *
	 * @return javax.swing.JPanel
     */
    private JPanel getSouthPanel() {
    	if (southPanel == null) {
    		southPanel = new JPanel();
    		southPanel.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(this, "select_geometric_info")));

    		southPanel.setPreferredSize(new Dimension(540, 225));
    		southPanel.add(getSouthWestScrollPane());//getFieldNameLabel());
    		southPanel.add(getSouthCenterPanel());
    		southPanel.add(getSouthEastPanel());
    	}

    	return southPanel;
    }

    /**
	 * <p>This method initializes southWestScrollPane.</p>
	 *
	 * @return javax.swing.JScrollPane
     */
    private JScrollPane getSouthWestScrollPane() {
    	if (southWestScrollPane == null) {
    		southWestScrollPane = new JScrollPane(getAttributesList());
     		southWestScrollPane.setPreferredSize(new Dimension(214, 190));
    	}

    	return southWestScrollPane;
    }

    /**
	 * <p>This method initializes attributesList.</p>
	 *
	 * @return javax.swing.JList
     */
    private JList getAttributesList() {
    	if (attributesList == null) {
    		attributesList = new JList();
    		attributesList.setToolTipText(PluginServices.getText(this, "select_geometric_properties_to_add_TOOLTIP_HTML_explanation"));
    		attributesList.setModel(new DefaultListModel());
    		attributesList.setCellRenderer(new JLabelCellRenderer());
    		attributesList.setAlignmentX(JList.LEFT_ALIGNMENT);
    		attributesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    		attributesList.addMouseListener(new MouseAdapter() {
    			/*
    			 * (non-Javadoc)
    			 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
    			 */
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						rightIconButton.doClick();

						return;
					}
				}
    		});
    	}

    	return attributesList;
    }

    /**
	 * <p>This method initializes southCenterPanel.</p>
	 *
	 * @return javax.swing.JPanel
     */
    private JPanel getSouthCenterPanel() {
    	if (southCenterPanel == null) {
    		southCenterPanel = new JPanel();
    		southCenterPanel.setLayout(new SpringLayout());

    		rightIcon = PluginServices.getIconTheme().get("right-arrow-icon");
     		leftIcon = PluginServices.getIconTheme().get("left-arrow-icon");

     		rightIconButton = new JButton(rightIcon);
     		rightIconButton.setToolTipText(PluginServices.getText(this, "add_selected_button_TOOLTIP_HTML_explanation"));
     		rightIconButton.setPreferredSize(new Dimension(40, 32));
     		rightIconButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					Object[] values = getAttributesList().getSelectedValues();

					DefaultListModel listModel = (DefaultListModel)getAttributesList().getModel();
					int firstIndex = -1;

					if (values.length > 0) {
						firstIndex = listModel.indexOf(values[0]);

						for (int i = 0; i < values.length; i++) {
							((DefaultListModel)getAttributesAddedList().getModel()).addElement(values[i]);
							listModel.removeElement(values[i]);
						}
					}

					// Select another to improve the usability
					if (listModel.size() > 0) {
						if (firstIndex >= listModel.size())
							getAttributesList().setSelectedIndex(listModel.size() -1);
						else {
							getAttributesList().setSelectedIndex(firstIndex);
						}
					}
				}
     		});

     		leftIconButton = new JButton(leftIcon);
     		leftIconButton.setToolTipText(PluginServices.getText(this, "remove_selected_button_TOOLTIP_HTML_explanation"));
     		leftIconButton.setPreferredSize(new Dimension(40, 32));
     		leftIconButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					Object[] values = getAttributesAddedList().getSelectedValues();

					DefaultListModel listModel = (DefaultListModel)getAttributesAddedList().getModel();
					int firstIndex = -1;

					if (values.length > 0) {
						firstIndex = listModel.indexOf(values[0]);

						for (int i = 0; i < values.length; i++) {
							((DefaultListModel)getAttributesList().getModel()).addElement(values[i]);
							listModel.removeElement(values[i]);
						}
					}

					// Select another to improve the usability
					if (listModel.size() > 0) {
						if (firstIndex >= listModel.size())
							getAttributesAddedList().setSelectedIndex(listModel.size() -1);
						else {
							getAttributesAddedList().setSelectedIndex(firstIndex);
						}
					}
					else {
						setEnabledFieldAttributeComponents(false);
					}
				}
     		});

     		doubleRightIcon = PluginServices.getIconTheme().get("double-right-arrow-icon");
     		doubleLeftIcon = PluginServices.getIconTheme().get("double-left-arrow-icon");
     		doubleRightIconButton = new JButton(doubleRightIcon);
     		doubleRightIconButton.setToolTipText(PluginServices.getText(this, "add_all_button_TOOLTIP_HTML_explanation"));
     		doubleRightIconButton.setPreferredSize(new Dimension(40, 32));
     		doubleRightIconButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					Object[] values = ((DefaultListModel)getAttributesList().getModel()).toArray();

					if (values.length > 0) {
						for (int i = 0; i < values.length; i++) {
							((DefaultListModel)getAttributesAddedList().getModel()).addElement(values[i]);
							((DefaultListModel)getAttributesList().getModel()).removeElement(values[i]);
						}
					}
				}
     		});

     		doubleLeftIconButton = new JButton(doubleLeftIcon);
     		doubleLeftIconButton.setToolTipText(PluginServices.getText(this, "remove_all_button_TOOLTIP_HTML_explanation"));
     		doubleLeftIconButton.setPreferredSize(new Dimension(40, 32));
     		doubleLeftIconButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					Object[] values = ((DefaultListModel)getAttributesAddedList().getModel()).toArray();

					if (values.length > 0) {
						for (int i = 0; i < values.length; i++) {
							((DefaultListModel)getAttributesList().getModel()).addElement(values[i]);
							((DefaultListModel)getAttributesAddedList().getModel()).removeElement(values[i]);
						}
					}

					setEnabledFieldAttributeComponents(false);
				}
     		});

     		southCenterPanel.add(doubleRightIconButton);
     		southCenterPanel.add(rightIconButton);
     		southCenterPanel.add(leftIconButton);
     		southCenterPanel.add(doubleLeftIconButton);
     		southCenterPanel.add(getReloadIconButton());
     		doSpringLayoutOfSouthCenterPanel();
    		southCenterPanel.setPreferredSize(new Dimension(40, 190));
    	}

    	return southCenterPanel;
    }

    /**
	 * <p>This method initializes reloadIconButton.</p>
	 *
	 * @return javax.swing.JButton
     */
    private JButton getReloadIconButton() {
    	if (reloadIconButton == null) {
    		reloadIcon = PluginServices.getIconTheme().get("reload-icon");

     		reloadIconButton = new JButton(reloadIcon);
     		reloadIconButton.setToolTipText(PluginServices.getText(this, "reload_button_TOOLTIP_HTML_explanation"));
     		reloadIconButton.setPreferredSize(new Dimension(40, 52));
     		reloadIconButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					refreshVisibleVectorLayers();

					if (getLayersComboBox().getItemCount() > 0) {
						FLayerWrapper fW = (FLayerWrapper)getLayersComboBox().getItemAt(0);
						((DefaultListModel)getAttributesList().getModel()).removeAllElements();
						((DefaultListModel)getAttributesAddedList().getModel()).removeAllElements();

						if (fW != null) {
							updateEditableLabel(fW.getLayer());
						}
					}

					getNewColumnCheckBox().setSelected(true);
					currentColumnNameComponent = getNameOfColumnText();
					getNameOfColumnText().setText("");
					getNameOfColumnText().setEnabled(false);
					getSouthEastPanel().add(currentColumnNameComponent);
					setEnabledFieldAttributeComponents(false);
					getSouthEastPanel().updateUI();
				}
     		});
    	}

    	return reloadIconButton;
    }

    /**
     * <p>Sets the GUI components to <code>b</code>.</p>
     *
     * @param b <code>true</code> to enable the GUI components, <code>false</code> to disable them
     */
    private void setEnabledFieldAttributeComponents(boolean b) {
    	getNewColumnCheckBox().setEnabled(b);
    	getSaveGeomPropertyButton().setEnabled(b);

    	if (b == false) {
    		getSouthEastPanel().remove(currentColumnNameComponent);
    		getNameOfColumnText().setText("");
    		currentColumnNameComponent = getNameOfColumnText();
    		getSouthEastPanel().add(currentColumnNameComponent);
	    	currentColumnNameComponent.setEnabled(b);
	    	getSouthEastPanel().updateUI();
			getAdaptedAcceptCancelPanel().setOkButtonEnabled(false);
    	}
    }

	/**
	 * <p>Creates the <code>Spring</code> layout of the south center panel.</p>
	 */
	private void doSpringLayoutOfSouthCenterPanel() {
		Component[] components = getSouthCenterPanel().getComponents();
        SpringLayout layout = (SpringLayout)getSouthCenterPanel().getLayout();
        Spring yPad = Spring.constant(2);
        Spring xSpring = Spring.constant(0);
        Spring ySpring = yPad;

        // Make every component 5 pixels away from the component to its down.
        for (int i = 0; i < components.length; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(components[i]);
            cons.setY(ySpring);
            ySpring = Spring.sum(yPad, cons.getConstraint("South"));

            cons.setX(xSpring);
        }
	}

	/**
	 * <p>This method initializes southEastPanel.</p>
	 *
	 * @return javax.swing.JPanel
	 */
    private JPanel getSouthEastPanel() {
    	if (southEastPanel == null) {
    		southEastPanel = new JPanel();
    		southEastPanel.setPreferredSize(new Dimension(215, 190));
    		southEastPanel.add(getAddedAttributesScrollPane());
    		southEastPanel.add(getSouthEastCenterPanel());
    		southEastPanel.add(getNameOfColumnText());
    		currentColumnNameComponent = getNameOfColumnText();
    	}

    	return southEastPanel;
    }

	/**
	 * <p>This method initializes southEastCenterPanel.</p>
	 *
	 * @return javax.swing.JPanel
	 */
    private JPanel getSouthEastCenterPanel() {
    	if (southEastCenterPanel == null) {
    		southEastCenterPanel = new JPanel();
    		southEastCenterPanel.setPreferredSize(new Dimension(215, 26));

    		southEastCenterPanel.add(getNewColumnCheckBox());
     		southEastCenterPanel.add(getSaveGeomPropertyButton());
    	}

    	return southEastCenterPanel;
    }

	/**
	 * <p>This method initializes saveGeomPropertyButton.</p>
	 *
	 * @return javax.swing.JButton
	 */
    private JButton getSaveGeomPropertyButton() {
    	if (saveGeomPropertyButton == null) {
    		saveGPIcon = PluginServices.getIconTheme().get("save-icon");

    		saveGeomPropertyButton = new JButton(saveGPIcon);
    		saveGeomPropertyButton.setPreferredSize(new Dimension(20, 20));
    		saveGeomPropertyButton.setToolTipText(PluginServices.getText(this, "save_changes_TOOLTIP_HTML_explanation"));
    		saveGeomPropertyButton.addActionListener(new ActionListener() {
     			/*
     			 * (non-Javadoc)
     			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     			 */
				public void actionPerformed(ActionEvent e) {
					updateAttributesToCurrentField();
				}
     		});
    	}

    	return saveGeomPropertyButton;
    }

	/**
	 * <p>This method initializes addedAttributesScrollPane.</p>
	 *
	 * @return javax.swing.JScrollPane
	 */
    private JScrollPane getAddedAttributesScrollPane() {
    	if (addedAttributesScrollPane == null) {
    		addedAttributesScrollPane = new JScrollPane(getAttributesAddedList());
    		addedAttributesScrollPane.setPreferredSize(new Dimension(214, 126));
    	}

    	return addedAttributesScrollPane;
    }

	/**
	 * <p>This method initializes attributesAddedList.</p>
	 *
	 * @return javax.swing.JList
	 */
    private JList getAttributesAddedList() {
    	if (attributesAddedList == null) {
    		attributesAddedList = new JList();
    		attributesAddedList.setToolTipText(PluginServices.getText(this, "select_how_add_each_geometric_property_TOOLTIP_HTML_explanation"));
    		attributesAddedList.setModel(new DefaultListModel());
    		attributesAddedList.setCellRenderer(new JLabelCellRenderer());
    		attributesAddedList.setAlignmentX(JList.LEFT_ALIGNMENT);
    		DefaultListModel listModel = ((DefaultListModel)attributesAddedList.getModel());
    		final DefaultListModel f_listModel = listModel;

    		listModel.addListDataListener(new ListDataListener() {
    			/*
    			 * (non-Javadoc)
    			 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
    			 */
				public void contentsChanged(ListDataEvent e) {
				}

				/*
				 * (non-Javadoc)
				 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
				 */
				public void intervalAdded(ListDataEvent e) {
					if (f_listModel.size() > 0)
						getAdaptedAcceptCancelPanel().setOkButtonEnabled(true);
				}

				/*
				 * (non-Javadoc)
				 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
				 */
				public void intervalRemoved(ListDataEvent e) {
					if (f_listModel.size() == 0)
						getAdaptedAcceptCancelPanel().setOkButtonEnabled(false);
				}
    		});

    		attributesAddedList.addListSelectionListener(new ListSelectionListener() {
    			/*
    			 * (non-Javadoc)
    			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
    			 */
				public void valueChanged(ListSelectionEvent e) {
					if (! e.getValueIsAdjusting()) {
						Object[] values = attributesAddedList.getSelectedValues();
						if (values.length == 1) {
							currentField = ((GeomInfo)values[0]);
				    		setEnabledFieldAttributeComponents(true);
				    		updateAttributesFromCurrentField();
						}
					}
				}
    		});

    		attributesAddedList.addMouseListener(new MouseAdapter() {
    			/*
    			 * (non-Javadoc)
    			 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
    			 */
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						leftIconButton.doClick();

						return;
					}
				}
    		});
    	}

    	return attributesAddedList;
    }

	/**
	 * <p>This method initializes newColumnCheckBox.</p>
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getNewColumnCheckBox() {
    	if (newColumnCheckBox == null) {
    		newColumnCheckBox = new JCheckBox(PluginServices.getText(this, "New_field"));
    		newColumnCheckBox.setToolTipText(PluginServices.getText(this, "new_property_checkbox_TOOLTIP_HTML_explanation"));
    		newColumnCheckBox.setPreferredSize(new Dimension(175, 22));
    		newColumnCheckBox.setEnabled(false);
    		newColumnCheckBox.setSelected(true);
    		newColumnCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateColumnNameFromField();
				}
    		});
    	}

    	return newColumnCheckBox;
	}

	/**
	 * <p>Updates the component that has the name of the field selected.</p>
	 */
	private void updateColumnNameFromField() {
		getSouthEastPanel().remove(currentColumnNameComponent);

		// Adds all the field names of the layer
		if (getNewColumnCheckBox().isSelected()) {
			currentColumnNameComponent = getNameOfColumnText();
			currentColumnNameComponent.setEnabled(true);

			if (currentField == null) {
				getNameOfColumnText().setText("");
			}
			else {
				getNameOfColumnText().setText(currentField.getName());
			}
		}
		else {
			currentColumnNameComponent = getNameOfColumnCombo();

    		try {
				DefaultComboBoxConfigurableLookUpModel model = ((DefaultComboBoxConfigurableLookUpModel)((JComboBoxConfigurableLookUp)getNameOfColumnCombo()).getModel());
				model.removeAllElements();

	    		FLyrVect layer = ((FLyrVect)((FLayerWrapper)getLayersComboBox().getSelectedItem()).getLayer());

	    		SelectableDataSource sds = layer.getRecordset();

	    		for (int i = 0; i < sds.getFieldCount(); i ++) {
	    			model.addElement(sds.getFieldName(i));
	    		}

	    		if (model.getSize() > 0) {
	    			if ((currentField.getName() == null) || (((DefaultComboBoxConfigurableLookUpModel)getNameOfColumnCombo().getModel()).getIndexOf(currentField.getName()) == -1))
	    				getNameOfColumnCombo().setSelectedItem(model.getElementAt(0));
	    			else {
	    				getNameOfColumnCombo().setSelectedItem(currentField.getName());
	    			}
	    		}
    		}
    		catch (Exception ex) {
    			JOptionPane.showMessageDialog(null, PluginServices.getText(null, "Failed_loading_fields"), PluginServices.getText(null, "Error"), JOptionPane.ERROR_MESSAGE);
    		}
		}

		getSouthEastPanel().add(currentColumnNameComponent);
		getSouthEastPanel().updateUI();
	}

	/**
	 * <p>Updates the component that has the name of the field selected.</p>
	 */
	private void updateAttributesToCurrentField() {
		if (currentField != null) {
			currentField.setNewColumn(getNewColumnCheckBox().isSelected());

			if (currentColumnNameComponent instanceof JTextFieldWithSCP) {
				String userName = applyReplaces(getNameOfColumnText().getText());
				String newName = getNewFieldName(fieldNames, userName);
				if (!newName.equals(userName)) {
					JOptionPane.showMessageDialog(this,
							PluginServices.getText(this, "Field_already_exists__")+userName+"\n"
							+ PluginServices.getText(this, "New_field_name_proposed__"+newName+"\n")
							+ PluginServices.getText(this, "Uncheck_New_Field_Option_if_you_want_to_update_an_existing_field"),
							PluginServices.getText(this, "Warning_Field_exists"),
							JOptionPane.WARNING_MESSAGE);
					getNameOfColumnText().setText(newName);
				}
				currentField.setName(newName);
			}
			else {
				currentField.setName(applyReplaces(((String) getNameOfColumnCombo().getSelectedItem())));
			}
		}
	}

	/**
	 * <p>Applies the different kinds of replacement defined in
	 *  {@link StringUtilitiesExtension StringUtilitiesExtension} to <code>s</code>.</p>
	 *
	 * @param s text to be formatted
	 * @return t text formatted
	 */
	private String applyReplaces(String s) {
		return StringUtilitiesExtended.replaceAllAccents(
				StringUtilitiesExtended.replaceAllCedilla(
					StringUtilitiesExtended.replaceAllNWithTilde(s)));
	}

	/**
	 * <p>Updates the current field properties with the configuration in the GUI.</p>
	 */
	private void updateAttributesFromCurrentField() {
		if (currentField != null) {
			getNewColumnCheckBox().setSelected(currentField.isNewColumn());
			updateColumnNameFromField();
		}
	}

	/**
	 * <p>This method initializes nameOfColumnText.</p>
	 *
	 * @return JTextFieldWithSCP
	 */
	private JTextFieldWithSCP getNameOfColumnText() {
		if (nameOfColumnText == null) {
			nameOfColumnText = new JTextFieldWithSCP();
			nameOfColumnText.setPreferredSize(new Dimension(215, 22));
			nameOfColumnText.setToolTipText(PluginServices.getText(this, "name_of_the_new_property_TOOLTIP_HTML_explanation"));
    	}

    	return nameOfColumnText;
	}

	/**
	 * <p>This method initializes nameOfColumnCombo.</p>
	 *
	 * @return JComboBoxConfigurableLookUp
	 */
	private JComboBoxConfigurableLookUp getNameOfColumnCombo() {
		if (nameOfColumnCombo == null) {
			nameOfColumnCombo = new JComboBoxConfigurableLookUp();
			DefaultComboBoxConfigurableLookUpModel model = ((DefaultComboBoxConfigurableLookUpModel)((JComboBoxConfigurableLookUp)nameOfColumnCombo).getModel());
			model.setShowAllItemsInListBox(true);
			nameOfColumnCombo.setPreferredSize(new Dimension(215, 22));
			nameOfColumnCombo.setToolTipText(PluginServices.getText(this, "name_of_the_new_property_TOOLTIP_HTML_explanation"));
		}

		return nameOfColumnCombo;
	}

    /**
	 * <p>This method initializes layersPanel.</p>
	 *
	 * @return javax.swing.JPanel
     */
    private JPanel getLayersPanel() {
    	if (layersPanel == null) {
    		layersPanel = new JPanel();
    		layersPanel.setLayout(new FlowLayout());
    		layersPanel.setPreferredSize(new Dimension(380, 28));
    		layersPanel.add(getLayersLabel());
    		layersPanel.add(getLayersComboBox());
    	}

    	return layersPanel;
    }

    /**
	 * <p>This method initializes layersLabel.</p>
	 *
	 * @return javax.swing.JLabel
     */
    private JLabel getLayersLabel() {
    	if (layersLabel == null) {
    		layersLabel = new JLabel(PluginServices.getText(this, "Layer"));
    		layersLabel.setPreferredSize(new Dimension(40, 20));
    		layersLabel.setToolTipText(PluginServices.getText(this, "Layer"));
    	}

    	return layersLabel;
    }

    /**
	 * <p>This method initializes layersComboBox.</p>
	 *
	 * @return javax.swing.JComboBox
     */
    private JComboBox getLayersComboBox() {
    	if (layersComboBox == null) {
    		layersComboBox = new JComboBox();
    		layersComboBox.setPreferredSize(new Dimension(layersComboBox_Width, layersComboBox_Height));
    		layersComboBox.addItemListener(new ItemListener() {
    			/*
    			 * (non-Javadoc)
    			 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
    			 */
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						try {
							FLayerWrapper fW = (FLayerWrapper)e.getItem();

							if (fW != null) {
								FLayer layer = fW.getLayer();

								if (layer != null) {
									layersComboBox.setToolTipText("<html>" + PluginServices.getText(this, "Layer") + ": " + layer.getName() + "<br>" +
											PluginServices.getText(this, "Type") + ": " + FShapeTypeNames.getFShapeTypeName(((FLyrVect)layer).getShapeType()) + "</html>");

									updateEditableLabel((FLyrVect)layer);
								}
							}
						} catch (ReadDriverException dE) {
							NotificationManager.showMessageError(PluginServices.getText(this, "problem_loading_layers"), dE);
						}
					}
				}
    		});
    	}

    	return layersComboBox;
    }

	/**
	 * <p>Updates "layersComboBox" with the name of visible layers of the associated <code>MapControl</code> instance.</p>
	 *
	 * @see #refreshVisibleVectorLayers(FLayer, int)
	 *
	 * @return <code>true</code> if there is any layer added that can be written, otherwise <code>false</code>
	 */
	private boolean refreshVisibleVectorLayers() {
		FLayer rootNode = mapControl.getMapContext().getLayers();

		getLayersComboBox().removeAllItems();

		boolean b = refreshVisibleVectorLayers(rootNode, -1);

		// The selectedLayer will be, by default, the first being added.
		if (getLayersComboBox().getItemCount() > 0) {
			getLayersComboBox().setRenderer(new LayersComboBoxCellRenderer());
			getLayersComboBox().setSelectedIndex(0);
		}

		// If there is only one layer -> disable the possibility of selection by the user
		if (getLayersComboBox().getItemCount() <= 1) {
			getLayersComboBox().setEnabled(false);
		}

		return b;
	}

	/**
	 * <p>Updates "layersComboBox" with the name of visible layers down <code>node</code>.</p>
	 *
	 * @param node parent node
	 * @param level <code>node</code> level from the root
	 *
	 * @return <code>true</code> if some layer added can be edited, otherwise <code>false</code>
	 */
	private boolean refreshVisibleVectorLayers(FLayer node, int level) {
		if (node instanceof FLayers) {
			FLayers root = (FLayers)node;
			boolean b = false;

			for (int i = root.getLayersCount() - 1; i >= 0 ; i--) {
				if (root.getLayer(i).isVisible()) {
					b |= refreshVisibleVectorLayers(root.getLayer(i), level + 1);
				}
			}

			return b;
		}
		else {
			String layerName = node.getName();

			if ((node.isVisible()) && (node instanceof FLyrVect) && (layerName != null)) {
				getLayersComboBox().addItem(new FLayerWrapper((FLyrVect)node, level, bIcon, getLayerIcon(node)));

				return node.isWritable();
			}
		}

		return false;
	}

	/**
	 * <p>Returns the icon that represents the layer in the current active view's TOC.</p>
	 *
	 * @param layer the layer
	 * @return the layer's icon in the current active view's TOC
	 */
	private Icon getLayerIcon(FLayer layer) {
	    if (layer.getTocImageIcon() != null) {
	    	return layer.getTocImageIcon();
	    }
	    else {
	    	TocItemBranch branch = new TocItemBranch(layer);

	    	return branch.getIcon();
	    }
	}

	/**
	 * <p>Updates attrsListModel with the geometric attributes according the shape type of the current selected layer.</p>
	 *
	 * @throws DriverException if fails working with the data driver
	 */
	private void refreshFields(FLyrVect seletedLayer) {
		try {
			previous_Type = seletedLayer.getShapeType();

			SelectableDataSource ds =  seletedLayer.getSource().getRecordset();
			fieldNames = new HashSet();
			try {
				String[] fn = ds.getFieldNames();
				for (int i=0; i<fn.length; i++) {
					fieldNames.add(fn[i]);
				}
			} catch (ReadDriverException e) {
				PluginServices.getLogger().error(e);
			}

			setEnabledFieldAttributeComponents(false);

			DefaultListModel attrsListModel = (DefaultListModel) getAttributesList().getModel();
			attrsListModel.removeAllElements();

			DefaultListModel attrsAddedListModel = (DefaultListModel) getAttributesAddedList().getModel();
			attrsAddedListModel.removeAllElements();

			GeomInfo geomInfo = null;

			switch (previous_Type) {
				case FShape.NULL:
					break;
				case FShape.POINT:
					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "X_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "X")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.X);
					attrsListModel.addElement(geomInfo);

					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Y_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "Y")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.Y);
					attrsListModel.addElement(geomInfo);

					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Z_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "Z")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.Z);
					attrsListModel.addElement(geomInfo);
					break;
				case FShape.LINE:
					attrsListModel.addElement(GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Length"), getNewFieldName(fieldNames, PluginServices.getText(this, "Length")), FShape.LINE));
					break;
				case FShape.POLYGON:
					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Area"), getNewFieldName(fieldNames, PluginServices.getText(this, "Area")), FShape.POLYGON);
					geomInfo.setGeomSubType(GeomInfo.AREA);
					attrsListModel.addElement(geomInfo);
					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Perimeter"), getNewFieldName(fieldNames, PluginServices.getText(this, "Perimeter")), FShape.POLYGON);
					geomInfo.setGeomSubType(GeomInfo.PERIMETER);
					attrsListModel.addElement(geomInfo);
					break;
				case FShape.TEXT:
					break;
				case FShape.MULTI: // Can have points, multipoints, lines and polygons
					/* POINT */
					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "X_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "X")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.X);
					attrsListModel.addElement(geomInfo);

					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Y_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "Y")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.Y);
					attrsListModel.addElement(geomInfo);

					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Z_coordinate"), getNewFieldName(fieldNames, PluginServices.getText(this, "Z")), FShape.POINT);
					geomInfo.setGeomSubType(GeomInfo.Z);
					attrsListModel.addElement(geomInfo);

					/* LINE */
					attrsListModel.addElement(GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Length"), getNewFieldName(fieldNames, PluginServices.getText(this, "Length")), FShape.LINE));

					/* POLYGON */
					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Area"), getNewFieldName(fieldNames, PluginServices.getText(this, "Area")), FShape.POLYGON);
					geomInfo.setGeomSubType(GeomInfo.AREA);
					attrsListModel.addElement(geomInfo);

					geomInfo = GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Perimeter"), getNewFieldName(fieldNames, PluginServices.getText(this, "Perimeter")), FShape.POLYGON);
					geomInfo.setGeomSubType(GeomInfo.PERIMETER);
					attrsListModel.addElement(geomInfo);

					/* MULTIPOINT */
					attrsListModel.addElement(GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Number_of_points"), getNewFieldName(fieldNames, PluginServices.getText(this, "N_points")), FShape.MULTIPOINT));
					break;
				case FShape.MULTIPOINT:
					attrsListModel.addElement(GeomInfoFactory.createGeomInfo(PluginServices.getText(this, "Number_of_points"), getNewFieldName(fieldNames, PluginServices.getText(this, "N_points")), FShape.MULTIPOINT));
					break;
				case FShape.CIRCLE:
					break;
				case FShape.ARC:
					break;
				case FShape.ELLIPSE:
					break;
				case FShape.Z:
					break;
				default : // UNDEFINED
			}

			updateUI();
		} catch (ReadDriverException dE) {
			JOptionPane.showMessageDialog(this, PluginServices.getText(this, "Failed_loading_fields"), PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private String getNewFieldName(HashSet currentFieldNames, String proposedName) {
		String newName = proposedName;
		int i=2;
		while (currentFieldNames.contains(newName)) {
			newName = proposedName+i;
			i++;
		}
		return newName;
	}

	/*
     * @see com.iver.andami.ui.mdiManager.View#getViewInfo()
     */
    public WindowInfo getWindowInfo() {
        if (viewInfo == null) {
            viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
            viewInfo.setTitle(PluginServices.getText(this, "add_geometric_info"));
            viewInfo.setWidth(Window_Width);
            viewInfo.setHeight(Window_Height);
        }
        return viewInfo;
    }

    /**
     * <p>Adapts {@link AcceptCancelPanel AcceptCancelPanel} to be used as a component of the <code>AddGeometricInfoPanel</code> panel.</p>
     *
     * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
     */
    private class AdaptedAcceptCancelPanel extends AcceptCancelPanel {
		private static final long serialVersionUID = -1630782817926954788L;

		/**
    	 * <p>Creates a new <code>AdaptedAcceptCancelPanel</code></p>
    	 */
		public AdaptedAcceptCancelPanel () {
    		super();

    		addOkButtonActionListener(getOKAction());
    		addCancelButtonActionListener(getCancelAction());
    		setPreferredSize(new Dimension(500, 28));
    	}

    	/**
     	 * <p>Create the action that will be executed when user pressed the <i>ok</i> button.</p>
    	 *
    	 * @return action that will be executed when user pressed the <i>cancel</i> button
    	 */
    	private ActionListener getOKAction() {
    		// OK button action
    		return new ActionListener() {
    			/*
    			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    			 */
				public void actionPerformed(ActionEvent e) {
					closeThis();

					/* 1- Initial tasks */
					/* 1.1- If layer is in edition, saves it and ends the edition */
					FLyrVect layer = ((FLayerWrapper)getLayersComboBox().getSelectedItem()).getLayer();
					final boolean wasBeingEdited = layer.isEditing();

					if (wasBeingEdited) {
						try {
							saveLayer(layer);
						}
						catch (Exception ex) {
							NotificationManager.showMessageError(PluginServices.getText(this, "Failed_saving_the_layer"), ex);
							PluginServices.getMainFrame().enableControls();
							return;
						}
					}

					/* 1.2- Stores the active layers */
					final FLayer[] activeLayers = getActiveLayers(view.getTOC());

					/* 2- Creates the process */
					AddGeometricInfoProcess iprocess = new AddGeometricInfoProcess(PluginServices.getText(this, "Add_geometric_information_to_layer_process"), PluginServices.getText(this, "Ongoing_process_please_wait"), view, layer, ((DefaultListModel)getAttributesAddedList().getModel()).toArray());

					IncrementableTask iTask = new IncrementableTask(iprocess, new ProgressPanel(false));
					iTask.addIncrementableListener(iprocess);
					iprocess.setIncrementableTask(iTask);
					final AddGeometricInfoProcess f_iprocess = iprocess;
					final IncrementableTask f_iTask = iTask;

					iTask.getProgressPanel().addComponentListener(new ComponentAdapter() {
						/*
						 * (non-Javadoc)
						 * @see java.awt.event.ComponentAdapter#componentHidden(java.awt.event.ComponentEvent)
						 */
						public void componentHidden(ComponentEvent e) {
							PluginServices.getMainFrame().enableControls();
							f_iTask.getProgressPanel().dispose();

							/* 7- Refreshes all the associated project tables */
							ProjectTable pt = f_iprocess.getLayerProjectTable();
							if (pt != null) {
							   	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices.getMDIManager().getAllWindows();

								for (int i = 0 ; i<views.length ; i++) {
									if (views[i] instanceof Table) {
										Table table = (Table)views[i];
										ProjectTable model = table.getModel();
										if (model.equals(pt)) {
											table.setModel(pt);
											table.setVisible(false);

											if ((wasBeingEdited) && (f_iprocess.getVea() != null))
												f_iprocess.getVea().getCommandRecord().addCommandListener(table);

											table.setVisible(true);
										}
									}
								}
							}

							/* 8- Restores the active layers */
							restoreActiveLayers(view.getTOC(), activeLayers);

							/* 9- Writes in the gvSIG log the results of the process */
							String text = "\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n" +
								PluginServices.getText(this, "Summary_of_the_process_of_adding_geometric_information") + ":\n" +
								f_iprocess.getLog() +
								"\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n";
							Logger.getLogger(getClass().getName()).debug(text);

							/* 10- If the process has been canceled -> refreshes the view */
							if (f_iprocess.getPercent() < 100)
								mapControl.drawMap(false);

							/* 11- Refreshes the toolbars and their controls */
							PluginServices.getMainFrame().enableControls();
						}
					});

					/* Starts the process */
					iprocess.start();
					iTask.start();
				}
    		};
    	}

    	/**
    	 * <p>Saves and stops the edition of a vector layer.</p>
    	 *
    	 * @param layer the vector layer to save
    	 *
    	 * @throws DriverException if fails the driver associated to the layer
    	 * @throws ReadDriverException
    	 * @throws InitializeWriterException
    	 * @throws StopWriterVisitorException
    	 * @throws EditionException if fails a edition process with the layer
    	 */
    	private void saveLayer(FLyrVect layer) throws DriverException, ReadDriverException, InitializeWriterException, StopWriterVisitorException {
    		layer.setProperty("stoppingEditing", new Boolean(true));
    		VectorialEditableAdapter vea = (VectorialEditableAdapter) layer.getSource();

    		ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
    		TablesFor.layer(layer).stopEditingCell();
    		
    		vea.cleanSelectableDatasource();
    		layer.setRecordset(vea.getRecordset());

    		// Queremos que el recordset del layer
    		// refleje los cambios en los campos.
    		ILayerDefinition lyrDef = EditionUtilities.createLayerDefinition(layer);
    		String aux = "FIELDS:";
    		FieldDescription[] flds = lyrDef.getFieldsDesc();
    		for (int i=0; i < flds.length; i++)	{
    			aux = aux + ", " + flds[i].getFieldAlias();
    		}

    		System.err.println("Escribiendo la capa " + lyrDef.getName() + " con los campos " + aux);
    		lyrDef.setShapeType(layer.getShapeType());
    		writer.initialize(lyrDef);
    		vea.stopEdition(writer, EditionEvent.GRAPHIC);
    		layer.setProperty("stoppingEditing", new Boolean(false));
    	}


    	/**
    	 * <p>Create the action that will be executed when user pressed the <i>cancel</i> button.</p>
    	 *
    	 * @return action that will be executed when user pressed the <i>cancel</i> button
    	 */
    	private ActionListener getCancelAction() {
    		// Cancel button action
    		return new ActionListener() {
    			/*
    			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    			 */
				public void actionPerformed(ActionEvent e) {
					closeThis();
				}
    		};
    	}
    }

    /**
     * <p>Wrappers a <code>FLayer</code> overwriting the method <code>toString()</code> in order to would
     *  return the name of the layer.</code>
     *
     * <p>Also displays icons and label</p>
     *
     * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
     */
    private class FLayerWrapper extends JPanel {
		private static final long serialVersionUID = -160586150559061104L;

		private FLyrVect layer;
    	private int level;
    	ImageIcon branchIcon;

    	/**
    	 * <p>Creates a new <code>FLayerWrapper</code>.</p>
    	 *
    	 * @param layer the vector layer to be wrappered
    	 * @param level the level in the tree of the layer wrappered
    	 * @param branchIcon icon that represents the branch
    	 * @param leafIcon icon that represents the leaf
    	 */
    	public FLayerWrapper(FLyrVect layer, int level, ImageIcon branchIcon, Icon leafIcon) {
    		super();

    		this.layer = layer;
    		this.level = level;
    		this.branchIcon = branchIcon;

    		setLayout(new SpringLayout());

    		if ((level > 0) && (branchIcon != null)) {
    			for (int i = 0; i < level; i++) {
	    			add(new JLabel(branchIcon));
    			}
    		}

    		JLabel layerLabel;

    		if (layer.getName() != null)
    			layerLabel = new JLabel(layer.getName());
    		else
    			layerLabel = new JLabel("");

    		if (leafIcon != null)
    			layerLabel.setIcon(leafIcon);

    		layerLabel.setFont(new Font("Helvetica", Font.BOLD, 12));

    		add(layerLabel);

    		if (layer instanceof FLyrVect) {
        		JLabel layerTypeLabel;
    			try {
    				layerTypeLabel = new JLabel(PluginServices.getText(null, "Type") + ": " + FShapeTypeNames.getFShapeTypeName(((FLyrVect) layer).getShapeType()));
				} catch (ReadDriverException e) {
					layerTypeLabel = new JLabel(PluginServices.getText(null, "Type") + ": " + PluginServices.getText("", "UNKNOWN"));
					NotificationManager.showMessageError(PluginServices.getText(null, "Unknown_layer_shape_type") + " : " + layer.getName(), e);
				}

				layerTypeLabel.setFont(new Font("Helvetica", Font.ITALIC, 12));

	    		add(layerTypeLabel);
    		}

			doSpringLayout();
    		setPreferredSize(new Dimension(340, 16));
    	}

    	/**
    	 * <p>Creates the <code>Spring</code> layout of this component.</p>
    	 */
    	private void doSpringLayout() {
			Component[] components = getComponents();
	        SpringLayout layout = (SpringLayout)getLayout();
	        Spring xPad = Spring.constant(5);
	        Spring ySpring = Spring.constant(0);
	        Spring xSpring = xPad;

	        // Make every component 5 pixels away from the component to its left.
	        for (int i = 0; i < components.length; i++) {
	            SpringLayout.Constraints cons = layout.getConstraints(components[i]);
	            cons.setX(xSpring);
	            xSpring = Spring.sum(xPad, cons.getConstraint("East"));

	            cons.setY(ySpring);
	        }
    	}

    	/**
    	 * <p>Gets the layer wrappered.</p>
    	 *
    	 * @return the layer wrappered
    	 */
    	public FLyrVect getLayer() {
    		return layer;
    	}

    	/**
    	 * <p>Gets the level in the tree of the layer wrappered.</p>
    	 *
    	 * @return the level in the tree of the layer wrappered
    	 */
    	public int getLevel() {
    		return level;
    	}

    	/*
    	 * (non-Javadoc)
    	 * @see javax.swing.JComponent#setForeground(java.awt.Color)
    	 */
    	public void setForeground(Color fg) {
    		super.setForeground(fg);
       	}

    	/*
    	 * (non-Javadoc)
    	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
    	 */
    	public void setBackground(Color bg) {
    		super.setBackground(bg);
    	}

    	/*
    	 * (non-Javadoc)
    	 * @see java.lang.Object#toString()
    	 */
    	public String toString() {
    		return layer.getName();
    	}
    }

    /**
     * <p>Cell renderer of the combo box with information of the visible vector layers in the current active view.</p>
     *
     * @see ListCellRenderer
     *
     * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
     */
    private class LayersComboBoxCellRenderer implements ListCellRenderer {
    	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    			FLayerWrapper renderer = (FLayerWrapper)value;

    			if (isSelected) {
    				renderer.setForeground(UIManager.getColor( "ComboBox.selectionForeground" ));
    				renderer.setBackground(UIManager.getColor( "ComboBox.selectionBackground" ));
    			}
    			else
    				renderer.setBackground(Color.WHITE);

    		    return renderer;
    	}
    }

    /**
     * <p>Closes this window.</p>
     */
	private void closeThis() {
		PluginServices.getMDIManager().closeWindow(this);
	}

	/**
	 * <p>Gets a list with the indexes of the active layers in the tree.</p>
	 *
	 * @param tree the {@link TOC TOC} tree with teh active layers
	 *
	 * @return indexes of the active layers
	 */
	private ArrayList getActiveLayerIndexes(JTree tree) {
		TreePath tPath;
		Object node;
		Object userObject;
		ArrayList rows = new ArrayList();

		try {
			for (int row = 0; row < tree.getRowCount(); row++) {
				tPath = tree.getPathForRow(row);
				node = tPath.getLastPathComponent();

				if (node instanceof DefaultMutableTreeNode) {
					userObject = ((DefaultMutableTreeNode)node).getUserObject();

					if (userObject instanceof TocItemBranch) {
						FLayer layer = ((TocItemBranch)userObject).getLayer();

						if (layer.isActive())
							rows.add(new Integer(row));
					}
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, PluginServices.getText(this, "Failed_saving_active_layer_indexes"), PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
		}
		return rows;
	}

	/**
	 * <p>Restores all active layers.</p>
	 *
	 * @param tree view's TOC's tree
	 * @param rows the rows to restore
	 */
	private void restoreActiveLayers(JTree tree, ArrayList rows) {
		TreePath tPath;
		Object node;
		Object userObject;
		int row;
		FLayer layer;

		try {
			if ((tree == null) || (rows == null))
				return;

			// First sets all not active
			for (row = 0; row < tree.getRowCount(); row ++) {
				tPath = tree.getPathForRow(row);
				node = tPath.getLastPathComponent();

				if (node instanceof DefaultMutableTreeNode) {
					userObject = ((DefaultMutableTreeNode)node).getUserObject();

					if (userObject instanceof TocItemBranch) {
						layer = ((TocItemBranch)userObject).getLayer();

						layer.setActive(false);
					}
				}
			}

			// Second set active only the required layers
			for (int i = 0; i < rows.size(); i ++) {
				row = ((Integer)rows.get(i)).intValue();

				tPath = tree.getPathForRow(row);
				node = tPath.getLastPathComponent();

				if (node instanceof DefaultMutableTreeNode) {
					userObject = ((DefaultMutableTreeNode)node).getUserObject();

					if (userObject instanceof TocItemBranch) {
						layer = ((TocItemBranch)userObject).getLayer();

						layer.setActive(true);
					}
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, PluginServices.getText(this, "Failed_restoring_active_layers"), PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
	
	   
	private FLayer[] getActiveLayers(TOC toc) {
	    return toc.getSelectedLayers();
	}
	
	   
	private void restoreActiveLayers(TOC toc, FLayer[] ll) {
	        
	        int len = ll.length;
	        toc.clearSelection();
	        for (int i=0; i<len; i++) {
	            toc.selectLayer(ll[i]);
	        }
	}
}
