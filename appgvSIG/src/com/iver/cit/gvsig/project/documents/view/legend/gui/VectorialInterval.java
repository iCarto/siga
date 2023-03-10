/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.project.documents.view.legend.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JBlank;
import org.gvsig.gui.beans.swing.JButton;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.styles.IMarkerFillPropertiesStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ClassifiableVectorial;
import com.iver.cit.gvsig.fmap.rendering.FInterval;
import com.iver.cit.gvsig.fmap.rendering.IInterval;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.NullIntervalValue;
import com.iver.cit.gvsig.fmap.rendering.NullUniqueValue;
import com.iver.cit.gvsig.fmap.rendering.VectorialIntervalLegend;
import com.iver.cit.gvsig.fmap.rendering.VectorialUniqueValueLegend;
import com.iver.cit.gvsig.gui.panels.ColorChooserPanel;
import com.iver.cit.gvsig.gui.styling.JComboBoxColorScheme;


/**
 * DOCUMENT ME!
 *
 * @author Vicente Caballero Navarro
 */
public class VectorialInterval extends JPanel implements ILegendPanel{
	protected static Logger logger = Logger.getLogger(VectorialInterval.class.getName());
	private GridBagLayoutPanel pnlGeneral = null;
	protected JComboBox cmbField = null;
	protected JTextField txtNumIntervals = null;
	protected ColorChooserPanel colorChooserPanel = null;
	protected ColorChooserPanel colorChooserPanel1 = null;
	protected JCheckBox chkdefaultvalues = null;
	protected JComboBox cmbFieldType = null;
	protected JPanel panelS = null;
	private JButton bintervals = null;
	private JButton bInsert = null;
	protected JButton bDelAll = null;
	protected JButton bDel = null;
	private int count = 0;
	protected ClassifiableVectorial layer;
	protected VectorialIntervalLegend theLegend;
	protected VectorialIntervalLegend auxLegend = null;
	protected SymbolTable symbolTable;
	protected MyListener listener = new MyListener();
	protected JPanel pnlCenter = null;
	protected JPanel optionPanel;
	private JPanel pnlNorth;
	protected JSymbolPreviewButton defaultSymbolPrev;
	private GridBagLayoutPanel defaultSymbolPanel = new GridBagLayoutPanel();
	/**
	 * This is the default constructor
	 */
	public VectorialInterval() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(700, 350);
		this.add(getPnlNorth(), BorderLayout.NORTH);

		this.add(getPnlButtons(), BorderLayout.SOUTH);
		this.add(getPnlCenter(), BorderLayout.CENTER);
		setOptionPanel(getOptionPanel());
	}

	private JPanel getPnlNorth() {
		if (pnlNorth == null) {
			pnlNorth = new JPanel(new GridLayout(1, 2));
			pnlNorth.add(getGeneralPanel());
		}
		return pnlNorth;
	}

	/**
	 * This method initializes panelN
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getGeneralPanel() {
		if (pnlGeneral == null) {
			pnlGeneral = new GridBagLayoutPanel();
			pnlGeneral.setBorder(BorderFactory.
					createTitledBorder(null,
							PluginServices.getText(this, "fields")));
			pnlGeneral.addComponent(PluginServices.getText(this, "Campo_de_clasificacion"),
					getCmbFields());
			pnlGeneral.addComponent(PluginServices.getText(this, "tipo_de_intervalo"),
					getCmbIntervalTypes());

			JPanel aux = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
			aux.add(getTxtNumIntervals());
			pnlGeneral.addComponent(PluginServices.getText(this, "No_de_intervalos"),
					aux);


			defaultSymbolPanel.add(getChkDefaultvalues(), null);
			pnlGeneral.addComponent(defaultSymbolPanel);

		}
		return pnlGeneral;
	}

	public JPanel getOptionPanel() {
		if (optionPanel == null) {
			optionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			optionPanel.setBorder(BorderFactory.
					createTitledBorder(null,
							PluginServices.getText(this, "color_ramp")));

			GridBagLayoutPanel aux = new GridBagLayoutPanel();
			aux.addComponent(PluginServices.getText(this, "Color_inicio"),
					getColorChooserPanel());
			aux.addComponent(PluginServices.getText(this, "Color_final"),
					getColorChooserPanel1());
			optionPanel.add(aux);
		}
		return optionPanel;
	}

	private void setOptionPanel(JPanel p) {
		getPnlNorth().remove(getOptionPanel());
		getPnlNorth().add(p, BorderLayout.NORTH);
	}
	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getCmbFields() {
		if (cmbField == null) {
			cmbField = new JComboBox();
			cmbField.setActionCommand("FIELD_SELECTED");
			cmbField.addActionListener(listener);
			cmbField.setVisible(true);
		}

		return cmbField;
	}

	/**
	 * This method initializes txtNumIntervals
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtNumIntervals() {
		if (txtNumIntervals == null) {
			txtNumIntervals = new JTextField(5);
			txtNumIntervals.setText("5");
		}

		return txtNumIntervals;
	}

	/**
	 * This method initializes colorChooserPanel
	 *
	 * @return com.iver.cit.gvsig.gui.Panels.ColorChooserPanel
	 */
	private ColorChooserPanel getColorChooserPanel() {
		if (colorChooserPanel == null) {
			colorChooserPanel = new ColorChooserPanel();
			colorChooserPanel.setBounds(new java.awt.Rectangle(108, 49, 54, 20));
			colorChooserPanel.setAlpha(255);
			colorChooserPanel.setColor(Color.red);
		}

		return colorChooserPanel;
	}

	/**
	 * This method initializes colorChooserPanel1
	 *
	 * @return com.iver.cit.gvsig.gui.Panels.ColorChooserPanel
	 */
	private ColorChooserPanel getColorChooserPanel1() {
		if (colorChooserPanel1 == null) {
			colorChooserPanel1 = new ColorChooserPanel();
			colorChooserPanel1.setBounds(new java.awt.Rectangle(251, 49, 54, 20));
			colorChooserPanel1.setAlpha(255);
			colorChooserPanel1.setColor(Color.blue);
		}

		return colorChooserPanel1;
	}

	/**
	 * This method initializes chkdefaultvalues
	 *
	 * @return javax.swing.JCheckBox
	 */
	protected JCheckBox getChkDefaultvalues() {
		if (chkdefaultvalues == null) {
			chkdefaultvalues = new JCheckBox();
			chkdefaultvalues.setText(PluginServices.getText(this,
			"resto_valores")+": ");
			chkdefaultvalues.setBounds(new java.awt.Rectangle(342, 26, 141, 20));
			chkdefaultvalues.setSelected(false);
			chkdefaultvalues.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (chkdefaultvalues.isSelected()) {
						auxLegend.useDefaultSymbol(true);
					} else {
						auxLegend.useDefaultSymbol(false);
					}
				}
			});
		}

		return chkdefaultvalues;
	}

	/**
	 * This method initializes jComboBox1
	 *
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getCmbIntervalTypes() {
		if (cmbFieldType == null) {
			cmbFieldType = new JComboBox();
			cmbFieldType.setActionCommand("INTERVAL_TYPE");
			cmbFieldType.addActionListener(listener);
			cmbFieldType.addItem(PluginServices.getText(this, "equal_intervals"));
			cmbFieldType.addItem(PluginServices.getText(this,
			"natural_intervals"));
			cmbFieldType.addItem(PluginServices.getText(this,
			"quantile_intervals"));
			cmbFieldType.setVisible(true);
		}

		return cmbFieldType;
	}

	/**
	 * This method initializes panelS
	 *
	 * @return javax.swing.JPanel
	 */
	protected JPanel getPnlButtons() {
		if (panelS == null) {
			panelS = new JPanel();
			panelS.setPreferredSize(new java.awt.Dimension(417, 32));
			panelS.add(getBintervals(), null);
			panelS.add(getBInsert(), null);
			panelS.add(getBDelAll(), null);
			panelS.add(getBDel(), null);
		}

		return panelS;
	}

	/**
	 * This method initializes bintervals
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBintervals() {
		if (bintervals == null) {
			bintervals = new JButton();
			bintervals.setActionCommand("ADD_ALL_VALUES");
			bintervals.addActionListener(listener);
			bintervals.setText(PluginServices.getText(this,
			"Calcular_intervalos"));
		}

		return bintervals;
	}

	/**
	 * This method initializes bInsert
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBInsert() {
		if (bInsert == null) {
			bInsert = new JButton();
			bInsert.setActionCommand("ADD_VALUE");
			bInsert.addActionListener(listener);
			bInsert.setText(PluginServices.getText(this, "Anadir"));
		}

		return bInsert;
	}

	/**
	 * This method initializes bDelAll
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBDelAll() {
		if (bDelAll == null) {
			bDelAll = new JButton();
			bDelAll.setActionCommand("REMOVE_ALL");
			bDelAll.addActionListener(listener);
			bDelAll.setText(PluginServices.getText(this, "Quitar_todos"));
		}

		return bDelAll;
	}

	/**
	 * This method initializes bDel
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBDel() {
		if (bDel == null) {
			bDel = new JButton();
			bDel.setText(PluginServices.getText(this, "Quitar"));
			bDel.setActionCommand("REMOVE");
			bDel.addActionListener(listener);
		}

		return bDel;
	}


	/**
	 * Damos una primera pasada para saber los l�mites inferior y superior y
	 * rellenar un array con los valores. Luego dividimos ese array en
	 * intervalos.
	 */
	protected void fillTableValues() {

		symbolTable.removeAllItems();

		try {
			FInterval[] arrayIntervalos = calculateIntervals();
			if (arrayIntervalos == null)
				return;

			FInterval interval;
			NumberFormat.getInstance().setMaximumFractionDigits(2);
			//theLegend.clear();
			auxLegend.clear();

			int r;
			int g;
			int b;
			int stepR;
			int stepG;
			int stepB;

			// Cogemos el tipo de gradaci�n de colores que quiere el usuario y
			// Creamos el primer y �ltimo color.
			Color startColor = colorChooserPanel.getColor();

			Color endColor = colorChooserPanel1.getColor();

			r = startColor.getRed();
			g = startColor.getGreen();
			b = startColor.getBlue();
			stepR = (endColor.getRed() - r) / arrayIntervalos.length;
			stepG = (endColor.getGreen() - g) / arrayIntervalos.length;
			stepB = (endColor.getBlue() - b) / arrayIntervalos.length;

			auxLegend = LegendFactory.createVectorialIntervalLegend(layer.getShapeType());
			auxLegend.setStartColor(startColor);
			auxLegend.setEndColor(endColor);
			auxLegend.setIntervalType(getCmbIntervalTypes().getSelectedIndex());
			auxLegend.useDefaultSymbol(false);

			int symbolType = layer.getShapeType();
			int numSymbols = 0;

			for (int k = 0; k < arrayIntervalos.length; k++) {
				interval = arrayIntervalos[k];

				ISymbol theSymbol = SymbologyFactory.createDefaultSymbolByShapeType(
						symbolType,
						new Color(r, g, b));
				theSymbol.setDescription(NumberFormat.getInstance().format(interval.getMin()) +
						" - " +
						NumberFormat.getInstance().format(interval.getMax()));

				//////////////////////////////////////
				// CALCULAMOS UN COLOR APROPIADO
				r = r + stepR;
				g = g + stepG;
				b = b + stepB;

				/////////////////////////////////
				auxLegend.addSymbol(interval, theSymbol);
				System.out.println("addSymbol = " + interval +
						" theSymbol = " + theSymbol.getDescription());
				numSymbols++;

				if (numSymbols > 100) {
					int resp = JOptionPane.showConfirmDialog(this,
							PluginServices.getText(this, "mas_de_100_simbolos"),
							PluginServices.getText(this, "quiere_continuar"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);

					if ((resp == JOptionPane.NO_OPTION) ||
							(resp == JOptionPane.DEFAULT_OPTION)) {
						return;
					}
				}

				// }
			} // for

			System.out.println("Num. Simbolos = " +
					auxLegend.getValues().length);
			symbolTable.fillTableFromSymbolList(auxLegend.getSymbols(),
					auxLegend.getValues(),auxLegend.getDescriptions());

		} catch (ReadDriverException e) {
			NotificationManager.addError(PluginServices.getText(this, "could_not_get_shape_type"), e);
		} catch (LegendLayerException e) {
			NotificationManager.addError(PluginServices.getText(this, "failed_computing_intervals"), e);
		}

		bDelAll.setEnabled(true);
		bDel.setEnabled(true);
	}

	protected FInterval[] calculateIntervals() throws LegendLayerException {
		int intervalCount = 1;
		// ensure the interval value is an integer greather than 0
		try {
			intervalCount = (int) Double.
			parseDouble(txtNumIntervals.getText());
			if (intervalCount<1) {
				throw new Exception();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					PluginServices.getText(this, "invalid_interval_count_value"));
			return null;
		}

		try {

			return auxLegend.calculateIntervals(
					//return theLegend.calculateIntervals(
					((AlphanumericData) layer).getRecordset(),
					(String) cmbField.getSelectedItem(),
					intervalCount,
					layer.getShapeType()
			);
		} catch (ReadDriverException e) {
			return null;
		}
	}

	public void setData(FLayer layer, ILegend legend) {
		this.layer = (ClassifiableVectorial) layer;
		int shapeType = 0;
		try {
			shapeType = this.layer.getShapeType();
		} catch (ReadDriverException e) {
			NotificationManager.addError(PluginServices.getText(this, "generating_intervals"), e);
		}

		if (symbolTable != null)
			pnlCenter.remove(symbolTable);


		getDefaultSymbolPrev(shapeType);

		symbolTable = new SymbolTable(this, SymbolTable.INTERVALS_TYPE, shapeType);
		pnlCenter.add(symbolTable);
		fillFieldNames();

		if (VectorialIntervalLegend.class.equals(legend.getClass())) {
			try {
				auxLegend = (VectorialIntervalLegend) legend.cloneLegend();
			} catch (XMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chkdefaultvalues.setSelected(auxLegend.isUseDefaultSymbol());
			cmbField.getModel().setSelectedItem(auxLegend.getClassifyingFieldNames()[0]);
			symbolTable.fillTableFromSymbolList(auxLegend.getSymbols(),
					auxLegend.getValues(),auxLegend.getDescriptions());
			colorChooserPanel.setColor(auxLegend.getStartColor());
			colorChooserPanel1.setColor(auxLegend.getEndColor());
			colorChooserPanel.repaint();
			colorChooserPanel1.repaint();
			if(auxLegend.isUseDefaultSymbol())
				txtNumIntervals.setText(String.valueOf(auxLegend.getSymbols().length - 1));
			else
				txtNumIntervals.setText(String.valueOf(auxLegend.getSymbols().length));
		} else {
			// Si la capa viene con otro tipo de leyenda, creamos
			// una nueva del tipo que maneja este panel
			auxLegend = new VectorialIntervalLegend();
			auxLegend.setShapeType(shapeType);
			auxLegend.useDefaultSymbol(false);
		}
		defaultSymbolPrev.setSymbol(auxLegend.getDefaultSymbol());
		cmbFieldType.setSelectedIndex(auxLegend.getIntervalType());
	}

	public void getDefaultSymbolPrev(int shapeType) {
		if(defaultSymbolPrev == null){
			defaultSymbolPrev = new JSymbolPreviewButton(shapeType);
			defaultSymbolPrev.setPreferredSize(new Dimension(110,20));
			defaultSymbolPanel.add(defaultSymbolPrev,null);
		}
	}

	protected void fillFieldNames() {
		SelectableDataSource rs = null;
		ArrayList nomFields = null;

		try {
//			rs = ((FLyrVect) layer).getSource().getRecordset();
			rs = ((FLyrVect) layer).getRecordset();
			logger.debug("rs.start()");
			rs.start();

			nomFields = new ArrayList();
			int fieldCount=rs.getFieldCount();
			int type;
			for (int i = 0; i < fieldCount; i++) {
				type = rs.getFieldType(i);

				if (type == Types.NULL) {
					continue;
				}

				if ((type == Types.INTEGER) ||
						(type == Types.DOUBLE) ||
						(type == Types.FLOAT) ||
						(type == Types.BIGINT)) {
					nomFields.add(rs.getFieldAlias(i));
				}
			}

			rs.stop();
		} catch (ReadDriverException e) {
			NotificationManager.addError(PluginServices.getText(this, "recovering_recordset"), e);
		}

		DefaultComboBoxModel cM = new DefaultComboBoxModel(nomFields.toArray());
		cmbField.setModel(cM);

		symbolTable.removeAllItems();
	}

	/**
	 * @see com.iver.cit.gvsig.gui.legendmanager.panels.ILegendPanel#getLegend()
	 */
	public ILegend getLegend() {
		fillSymbolListFromTable();

		if(defaultSymbolPrev.getSymbol() != null)
			auxLegend.setDefaultSymbol(defaultSymbolPrev.getSymbol());

		auxLegend.useDefaultSymbol(chkdefaultvalues.isSelected());

		try {
			theLegend = (VectorialIntervalLegend) auxLegend.cloneLegend();
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return theLegend;
	}

	/**
	 * A partir de los registros de la tabla, regenera el FRenderer. (No solo
	 * el symbolList, si no tambi�n el arrayKeys y el defaultRenderer
	 */

	protected void fillSymbolListFromTable() {
		ISymbol theSymbol;
		IInterval theInterval = null;

		// Borramos las anteriores listas:
		auxLegend.clear();

		String fieldName = (String) cmbField.getSelectedItem();
		auxLegend.setClassifyingFieldNames(new String[] {fieldName});

		auxLegend.useDefaultSymbol(chkdefaultvalues.isSelected());

		DataSource rs;
		try {

			rs = ((AlphanumericData) layer).getRecordset();
			rs.start();
			auxLegend.setClassifyingFieldTypes(new int[] {rs.getFieldType(rs.getFieldIndexByName(fieldName))});
			logger.debug("rs.start()");
			rs.stop();

		} catch (ReadDriverException e) {
			NotificationManager.addError(PluginServices.getText(this, "recovering_recordset"), e);
		}



		for (int row = 0; row < symbolTable.getRowCount(); row++) {
			if (!(symbolTable.getFieldValue(row, 1) instanceof FInterval)) {
				theSymbol = (ISymbol) symbolTable.getFieldValue(row, 0);
				theSymbol.setDescription((String) symbolTable.getFieldValue(
						row, 2));
				auxLegend.addSymbol(new NullIntervalValue(), theSymbol);
			} else {
				theInterval = (IInterval) symbolTable.getFieldValue(row, 1);
				theSymbol = (ISymbol) symbolTable.getFieldValue(row, 0);
				theSymbol.setDescription((String) symbolTable.getFieldValue(
						row, 2));
				auxLegend.addSymbol(theInterval, theSymbol);
			}
		}

		if(chkdefaultvalues.isSelected()){
			if(defaultSymbolPrev.getSymbol() != null){
				String description = PluginServices.getText(this,"default");
				defaultSymbolPrev.getSymbol().setDescription(description);
				auxLegend.addSymbol(new NullIntervalValue(), defaultSymbolPrev.getSymbol());
			}
		}
	}

	/**
	 * This method initializes panelC
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPnlCenter() {
		if (pnlCenter == null) {
			pnlCenter = new JPanel();
		}

		return pnlCenter;
	}

	/**
	 * Listener.
	 *
	 * @author Vicente Caballero Navarro
	 */
	class MyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			System.out.println("ActionEvent con " + e.getActionCommand());

			//modificar el combobox de valor
			if (e.getActionCommand() == "FIELD_SELECTED") {
				fieldSelectedActionPerformed((JComboBox) e.getSource());
			} else if (e.getActionCommand() == "INTERVAL_TYPE") {
				JComboBox cb = (JComboBox) e.getSource();

				//if ((theLegend != null) &&
				if ((auxLegend != null) &&
						//    (cb.getSelectedIndex() != theLegend.getIntervalType())) {
						(cb.getSelectedIndex() != auxLegend.getIntervalType())) {
					//theLegend.setIntervalType(cb.getSelectedIndex());
					auxLegend.setIntervalType(cb.getSelectedIndex());
					symbolTable.removeAllItems();
				}
			}

			//A�adir todos los elementos por valor
			if (e.getActionCommand() == "ADD_ALL_VALUES") {
				fillTableValues();
			}

			//A�adir un �nico elemento
			if (e.getActionCommand() == "ADD_VALUE") {
				try {
					symbolTable.addTableRecord(
							SymbologyFactory.createDefaultSymbolByShapeType(layer.getShapeType()),
							new FInterval(0, 0),"0 - 0");
				} catch (ReadDriverException e1) {
					NotificationManager.addError(PluginServices.getText(this, "could_not_get_shape_type"), e1);
				}

			}

			//Vacia la tabla
			if (e.getActionCommand() == "REMOVE_ALL") {
				symbolTable.removeAllItems();
			}

			//Quitar solo el elemento seleccionado
			if (e.getActionCommand() == "REMOVE") {
				symbolTable.removeSelectedRows();
			}
		}
	}

	protected void fieldSelectedActionPerformed(JComboBox cb){
		String fieldName = (String) cb.getSelectedItem();
		System.out.println("Nombre del campo: " + fieldName);
		symbolTable.removeAllItems();

		//theLegend.setClassifyingFieldNames(new String[] {fieldName});
		auxLegend.setClassifyingFieldNames(new String[] {fieldName});
	}

	public String getDescription() {
		return PluginServices.getText(this,"Muestra_los_elementos_de_la_capa_usando_una_gama_de_colores_en_funcion_del_valor_de_un_determinado_campo_de_atributos") + ".";
	}

	public ImageIcon getIcon() {
		return new ImageIcon(this.getClass().getClassLoader().
				getResource("images/Intervalos.png"));
	}

	public Class getParentClass() {
		return Quantities.class;
	}

	public String getTitle() {
		return PluginServices.getText(this,"Intervalos");
	}

	public JPanel getPanel() {
		return this;
	}

	public Class getLegendClass() {
		return VectorialIntervalLegend.class;
	}

	private boolean isNumericField(int fieldType) {
		switch (fieldType) {
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.REAL:
		case Types.SMALLINT:
		case Types.TINYINT:
			return true;
		default:
			return false;
		}

	}
	public boolean isSuitableFor(FLayer layer) {

		if (layer instanceof FLyrVect) {
			SelectableDataSource sds;
			try {
				sds = ((FLyrVect) layer).getRecordset();
				String[] fNames = sds.getFieldNames();
				for (int i = 0; i < fNames.length; i++) {
					if (isNumericField(sds.getFieldType(i))) {
						return true;
					}
				}
			} catch (ReadDriverException e) {
				return false;
			}
		}
		return false;
	}
}
