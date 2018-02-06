package org.gvsig.tableImport.addgeominfo.process;

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

import java.awt.Component;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.gvsig.gui.beans.buttonspanel.ButtonsPanel;
import org.gvsig.gui.beans.incrementabletask.IncrementableProcess;
import org.gvsig.gui.beans.incrementabletask.IncrementableTask;
import org.gvsig.tableImport.addgeominfo.GeomInfo;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.EditionManager;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FMultipoint3D;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeometryUtilities;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.edition.rules.IRule;
import com.iver.cit.gvsig.fmap.edition.rules.RulePolygon;
import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.gui.cad.CADToolAdapter;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.ProjectTableFactory;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.table.gui.TablesFor;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.project.documents.view.legend.CreateSpatialIndexMonitorableTask;

/**
 * Process that adds the selected geometric information.</a>.
 *
 * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
 */
public class AddGeometricInfoProcess extends IncrementableProcess {

	private boolean				 layerWasBeingEdited = false;

	private View				 view				 = null;
	private FLyrVect			 layer				 = null;
	private Object[]			 fields				 = null;
	private ProjectTable		 layerProjectTable	 = null;
	private VectorialEditableAdapter vea			 = null;

	/**
	 * Creates a new <p>GeoVisorImportProcess</p>.
	 *
	 * @param title of the progress dialog
	 * @param label the label that explains the process
	 * @param view the view where the layer is added
	 * @param layer the vector layer
	 * @param fields fields to add
	 */
	public AddGeometricInfoProcess(String title, String label, View view, FLyrVect layer, Object[] fields) {
		super(title);

		this.label = label;
		this.view = view;
		this.layer = layer;
		this.fields = fields;
		this.isPausable = true;
	}

	/**
	 * Sets the object that will display the evolution of this loading process as a progress dialog.
	 *
	 * @param iTask the object that will display the evolution of this loading process
	 */
	public void setIncrementableTask(IncrementableTask iTask) {
		this.iTask = iTask;
		iTask.setAskCancel(true);
		iTask.getButtonsPanel().addAccept();
		iTask.getButtonsPanel().setEnabled(ButtonsPanel.BUTTON_ACCEPT, false);

		JButton jButton = iTask.getButtonsPanel().getButton(ButtonsPanel.BUTTON_ACCEPT);
		jButton.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				processFinalize();
			}
		});
	}

	/**
	 * <p>Gets the project table of the active layer.</p>
	 *
	 * @return the project table of the active layer, or <code>null</code> if there wasn't any
	 */
	public ProjectTable getLayerProjectTable() {
		return layerProjectTable;
	}

	/**
	 * <p>Gets the vectorial editable adapter of the active layer.</p>
	 *
	 * @return the vectorial editable adapter of the active layer
	 */
	public VectorialEditableAdapter getVea() {
		return vea;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		String text = null;

		try {
			process();
			while (! ended) {
				t0 += 500;
                Thread.currentThread().sleep(150);
			}
		} catch (Exception ie) {
			if (! cancelProcess.isCanceled()) {
				Logger.getLogger(IncrementableProcess.class).error(ie);
				label = PluginServices.getText(null, "Process_failed");
				iTask.getProgressPanel().setLabel(label);
				text = PluginServices.getText(null, "Failed_the_process_Shouldnt_work_with_the_layer");
			}
			else {
				label = PluginServices.getText(null, "Process_canceled");
				iTask.getProgressPanel().setLabel(label);
				text = PluginServices.getText(null, "Process_canceled");
			}
		}
		finally {
			iTask.setAskCancel(false);
			iTask.getButtonsPanel().setEnabled(ButtonsPanel.BUTTON_ACCEPT, true);
			iTask.getButtonsPanel().setEnabled(ButtonsPanel.BUTTON_CANCEL, false);

			if (text != null) {
				log.addLine(PluginServices.getText(null, "Percent") + ": " + getPercent());
				log.addLine(text);

				if (cancelProcess.isCanceled())
					JOptionPane.showMessageDialog(iTask.getButtonsPanel(), text, PluginServices.getText(this, "Information"), JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(iTask.getButtonsPanel(), text, PluginServices.getText(this, "Error"), JOptionPane.ERROR_MESSAGE);
			}

			if (percentage == 100) {
				label = PluginServices.getText(null, "Process_finished");
				iTask.getProgressPanel().setLabel(label);
//				iTask.getProgressPanel().setPercent(100); // Forces setting the progress bar at 100 %
			}

			// Ends this process
			ended = true;

			// Ends the progress panel
			iTask.stop();
		}
	}

	/**
	 * Importation process.
	 *
	 * @throws InterruptedException if fails the process
	 */
	public void process() throws InterruptedException {

		MapControl mapControl = null;
		String previousTool_ID = null;
		short n_fields_added = 0;

		percentage = 5;

		if (cancelProcess.isCanceled()) {
			throw new InterruptedException();
		}

		try {
			mapControl = view.getMapControl();

			// Saves the current tool
			previousTool_ID = mapControl.getCurrentTool();

			layerWasBeingEdited = layer.isEditing();
		}
		catch(Exception e) {
			NotificationManager.showMessageError(PluginServices.getText(null, "Failed_the_process"), e);
			throw new InterruptedException();
		}
		layer.setWaitTodraw(true);
		CADExtension cad_extension = null;
		VectorialEditableAdapter vea = null;
		EditionManager editionManager = null;

		try {
			/* 3- Starts layer in edition */
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			log.addLine(PluginServices.getText(null, "Starting_the_layer_in_edition_mode"));
			percentage = 12;

			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			editionManager = CADExtension.getEditionManager();
			editionManager.setMapControl(mapControl);

			layer.addLayerListener(editionManager);

			ILegend legendOriginal = layer.getLegend();

			if (! layer.isWritable()) {
				JOptionPane.showMessageDialog((Component) PluginServices.getMDIManager().getActiveWindow(),
					PluginServices.getText(this, "this_layer_is_not_self_editable"),
					PluginServices.getText(this, "warning_title"),
					JOptionPane.WARNING_MESSAGE);

				throw new InterruptedException();
			}

			/* 3.1- Sets the cad tool adapter if wasn't added */
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			CADToolAdapter cta = CADExtension.getCADToolAdapter();
			if (! mapControl.getNamesMapTools().containsKey("cadtooladapter")) {
				mapControl.addMapTool("cadtooladapter", cta);
			}

			layer.setEditing(true);
			percentage = 20;
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			vea = (VectorialEditableAdapter) layer.getSource();

			vea.getRules().clear();
			if (vea.getShapeType() == FShape.POLYGON) {
				IRule rulePol = new RulePolygon();
				vea.getRules().add(rulePol);
			}

			if (! (layer.getSource().getDriver() instanceof IndexedShpDriver)) {
				VectorialLayerEdited vle=(VectorialLayerEdited)editionManager.getLayerEdited(layer);
				vle.setLegend(legendOriginal);
			}

			vea.getCommandRecord().addCommandListener(mapControl);

			/* 3.2- If exits any layer associated, changes its model by the VectorialEditableAdapter's one */
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			ProjectExtension pe = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
			percentage = 25;

			ProjectTable pt = pe.getProject().getTable(layer);
			if (pt != null){
				pt.setModel(vea);

				/* 3.3- If there is any view with the table associated to this vector layer -> updates the table */
				// This step is executed after finishing the process, to avoid problems with Swing threads
			}

			/* 3.4- Repaints the view */
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

//			mapControl.drawMap(false);

			percentage = 33;

			GeomInfo field;
			AddedFieldInfo addedField = null;
			int i, j;
			String operationName = PluginServices.getText(null, "setGeomInfo_");
			IGeometry geometry = null;
			Value value = null;
			Value[] values;
			ArrayList list;
			short inc = 0;
			double c_value;

			mapControl.getMapContext().beginAtomicEvent();
			vea.startComplexRow();
			try{

			/* 4- For each field selected */
			if (fields.length > 0)
				inc = (short) ((75 - 33) / fields.length);

			for (i = 0; i < fields.length; i++) {

				if (cancelProcess.isCanceled()) {
					throw new InterruptedException();
				}

				field = (GeomInfo) fields[i];

				/* 4.1- If its required -> creates a new field */
				if (field.isNewColumn()) {
					addedField = addField(vea, field);

					/* 4.1.1- If didn't added the field */
					if (addedField.getColumn() == -1)
						continue;
				}
				else {
					/* 4.2- Else -> Validates the properties of the selected field (if can add the data -> fails that subtask) */
					/* 4.2.1- Finds the column of the field */

					if (cancelProcess.isCanceled()) {
						throw new InterruptedException();
					}

					FieldDescription[] fieldDescriptions = vea.getFieldsDescription();

					for (j = 0; j < fieldDescriptions.length; j++) {
						if (cancelProcess.isCanceled()) {
							throw new InterruptedException();
						}

						if (fieldDescriptions[j].getFieldName().compareTo(field.getName()) == 0) {
							addedField = new AddedFieldInfo(j, fieldDescriptions[j]);
							break;
						}
					}

					if (addedField == null) {
						log.addLine(PluginServices.getText(this, "Error") + ": " + PluginServices.getText(this, "Couldnt_find_column") + " \"" + field.getName() + "\"");
					}
					else  {
						/* 4.2.2- Validates if the properties of the column are compatible with the data to add, otherwise doesn't add the information */
						if (cancelProcess.isCanceled()) {
							throw new InterruptedException();
						}

						switch (addedField.getFieldAdded().getFieldType()) {
							case Types.DOUBLE:
								if ( (addedField.getFieldAdded().getFieldLength() < 13) || (addedField.getFieldAdded().getFieldDecimalCount() < 1) ) {
									log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "Incompatible_data_type_in_column_wont_modify_that_column") + " \"" + field.getName() + "\"");
									continue;
								}
								break;
							case Types.BIGINT:
								if ( addedField.getFieldAdded().getFieldLength() < 13) {
									log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "Incompatible_data_type_in_column_wont_modify_that_column") + " \"" + field.getName() + "\"");
									continue;
								}
								break;
							default:
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "Incompatible_data_type_in_column_wont_modify_that_column") + " \"" + field.getName() + "\"");
								continue;
						}
						if (! ((addedField.getFieldAdded().getFieldType() == Types.DOUBLE) && (addedField.getFieldAdded().getFieldLength() > 0))) {
							log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "Incompatible_data_type_in_column_wont_modify_that_column") + " \"" + field.getName() + "\"");
							break;
						}
					}
				}

				/* 5- Calculates the information and adds it to the selected field */
				if (cancelProcess.isCanceled()) {
					throw new InterruptedException();
				}
				vea.start();
				switch(field.getShapeType()) {
					case FShape.NULL:
						break;
					case FShape.POINT:
						/* 5.1- For each geometry: calculates the new geometric information and sets to the new field */
						for (j = 0; j < vea.getRowCount(); j ++) {
							if (cancelProcess.isCanceled()) {
								throw new InterruptedException();
							}

//							vea.start();
							geometry = (IGeometry) vea.getShape(j);
//							vea.stop();

							Shape shape = null;

							if (geometry != null) {
								shape = geometry.getInternalShape();
							}
							else {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "unassigned_geometry_at_row") + j);
								continue;
							}

							if (shape == null) {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "geometry_without_internal_shape_at_row") + j);
								continue;
							}
							else {
								/* 5.2- Enables the edition of the row */
								if (cancelProcess.isCanceled()) {
									throw new InterruptedException();
								}

								//vea.startComplexRow();
								DefaultRowEdited row = (DefaultRowEdited) vea.getRow(j);
								DefaultFeature feature = (DefaultFeature) row.getLinkedRow().cloneRow();

								/* 5.3- Gets the feature of each row */
								values = feature.getAttributes();
								list = new ArrayList(Arrays.asList(values));

								/* 5.4- Calculates the geometric information for that geometry / feature */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								if (shape instanceof FPoint2D) {
									FPoint2D point = (FPoint2D)shape;

									switch (field.getGeomSubType()) {
										case GeomInfo.X:
											/* X */
											value = ValueFactory.createValueByType(Double.toString(point.getX()), Types.DOUBLE);
											break;
										case GeomInfo.Y:
											/* Y */
											value = ValueFactory.createValueByType(Double.toString(point.getY()), Types.DOUBLE);
											break;
										case GeomInfo.Z:
										case GeomInfo.UNDEFINED:
										default:
											value = ValueFactory.createValue(0.0d);
											break;
									}
								}
								else { // instance of FPoint3D
									FPoint3D point = (FPoint3D)shape;

									switch (field.getGeomSubType()) {
									case GeomInfo.X:
										/* X */
										value = ValueFactory.createValueByType(Double.toString(point.getX()), Types.DOUBLE);
										break;
									case GeomInfo.Y:
										/* Y */
										value = ValueFactory.createValueByType(Double.toString(point.getY()), Types.DOUBLE);
										break;
									case GeomInfo.Z:
										/* Z */
										value = ValueFactory.createValueByType(Double.toString(point.getZs()[0]), Types.DOUBLE);
										break;
									case GeomInfo.UNDEFINED:
									default:
										value = value = ValueFactory.createValue(0.0d);
										break;
									}
								}

								/* 5.5- Sets the new value */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								list.remove(addedField.getColumn());
								list.add(addedField.getColumn(), value);
								feature.setAttributes((Value[])list.toArray(new Value[0]));

								vea.modifyRow(row.getIndex(), feature, operationName, EditionEvent.ALPHANUMERIC);

								/* 5.6- Disables the edition of the row */
//								vea.endComplexRow(operationName);

								/* 5.7- Increments the counter of the fields added */
								n_fields_added ++;
							}
						}
						break;
					case FShape.LINE:
						/* 5.1- For each geometry: calculates the new geometric information and sets to the new field */
						for (j = 0; j < vea.getRowCount(); j ++) {
							if (cancelProcess.isCanceled()) {
								throw new InterruptedException();
							}

//							vea.start();
							geometry = (IGeometry) vea.getShape(j);
//							vea.stop();

							Shape shape = null;

							if (geometry != null) {
								shape = geometry.getInternalShape();
							}
							else {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "unassigned_geometry_at_row") + j);
								continue;
							}

							if (shape == null) {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "geometry_without_internal_shape_at_row") + j);
								continue;
							}
							else {
								try {
									c_value = GeometryUtilities.getLength(layer.getMapContext().getViewPort(), geometry);
								}
								catch (Exception e) {
									NotificationManager.showMessageError(PluginServices.getText(null, "Failed_calculating_perimeter_of_geometry"), e);

									percentage += inc;
									continue;
								}

								/* 5.2- Enables the edition of the row */
								if (cancelProcess.isCanceled()) {
									throw new InterruptedException();
								}

//								vea.startComplexRow();
								DefaultRowEdited row = (DefaultRowEdited) vea.getRow(j);
								DefaultFeature feature = (DefaultFeature) row.getLinkedRow().cloneRow();

								/* 5.3- Gets the feature of each row */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								values = feature.getAttributes();
								list = new ArrayList(Arrays.asList(values));

								/* 5.4- Calculates the geometric information for that geometry / feature */
								/* LENGTH */
								value = ValueFactory.createValueByType(Double.toString(c_value), Types.DOUBLE);

								/* 5.5- Sets the new value */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								list.remove(addedField.getColumn());
								list.add(addedField.getColumn(), value);
								feature.setAttributes((Value[])list.toArray(new Value[0]));

								vea.modifyRow(row.getIndex(), feature, operationName, EditionEvent.ALPHANUMERIC);

								/* 5.6- Disables the edition of the row */
//								vea.endComplexRow(operationName);

								/* 5.7- Increments the counter of the fields added */
								n_fields_added ++;
							}
						}
						break;
					case FShape.POLYGON:
						/* 5.1- For each geometry: calculates the new geometric information and sets to the new field */
						for (j = 0; j < vea.getRowCount(); j ++) {
							if (cancelProcess.isCanceled()) {
								throw new InterruptedException();
							}


							geometry = (IGeometry) vea.getShape(j);
//							vea.stop();

							Shape shape = null;

							if (geometry != null) {
								shape = geometry.getInternalShape();
							}
							else {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "unassigned_geometry_at_row") + j);
								continue;
							}

							if (shape == null) {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "geometry_without_internal_shape_at_row") + j);
								continue;
							}
							else {
								/* 5.2- Enables the edition of the row */
								if (cancelProcess.isCanceled()) {
									throw new InterruptedException();
								}

//								vea.startComplexRow();
								DefaultRowEdited row = (DefaultRowEdited) vea.getRow(j);
								DefaultFeature feature = (DefaultFeature) row.getLinkedRow().cloneRow();

								/* 5.3- Gets the feature of each row */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								values = feature.getAttributes();
								list = new ArrayList(Arrays.asList(values));

								/* 5.4- Calculates the geometric information for that geometry / feature */
								switch (field.getGeomSubType()) {
									case GeomInfo.PERIMETER:
										/* PERIMETER */
										try {
											c_value = GeometryUtilities.getLength(layer.getMapContext().getViewPort(), geometry);
										}
										catch (Exception e) {
											NotificationManager.showMessageError(PluginServices.getText(null, "Failed_calculating_perimeter_of_geometry"), e);
											percentage += inc;
											continue;
										}

										value = ValueFactory.createValueByType(Double.toString(c_value), Types.DOUBLE);
										break;
									case GeomInfo.AREA:
										/* AREA */
										try {
											c_value = GeometryUtilities.getArea(layer, geometry);
										}
										catch (Exception e) {
											NotificationManager.showMessageError(PluginServices.getText(null, "Failed_calculating_area_of_geometry"), e);

											percentage += inc;
											continue;
										}

										value = ValueFactory.createValueByType(Double.toString(c_value), Types.DOUBLE);
										break;
									case GeomInfo.UNDEFINED:
										// Do nothing
										break;
								}

								/* 5.5- Sets the new value */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								list.remove(addedField.getColumn());
								list.add(addedField.getColumn(), value);
								feature.setAttributes((Value[])list.toArray(new Value[0]));

								vea.modifyRow(row.getIndex(), feature, operationName, EditionEvent.ALPHANUMERIC);

								/* 5.6- Disables the edition of the row */
//								vea.endComplexRow(operationName);

								/* 5.7- Increments the counter of the fields added */
								n_fields_added ++;
							}
						}
						break;
					case FShape.TEXT:
						break;
					case FShape.MULTI: // Other types
						break;
					case FShape.MULTIPOINT:
						/* 5.1- For each geometry: calculates the new geometric information and sets to the new field */
						for (j = 0; j < vea.getRowCount(); j ++) {
							if (cancelProcess.isCanceled()) {
//								vea.endComplexRow(operationName);
								throw new InterruptedException();
							}

//							vea.start();
							geometry = (IGeometry) vea.getShape(j);
//							vea.stop();

							Shape shape = null;

							if (geometry != null) {
								shape = geometry.getInternalShape();
							}
							else {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "unassigned_geometry_at_row") + j);
								continue;
							}

							if (shape == null) {
								log.addLine(PluginServices.getText(this, "Warning") + ": " + PluginServices.getText(this, "geometry_without_internal_shape_at_row") + j);
								continue;
							}
							else {
								/* 5.2- Enables the edition of the row */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
								}

//								vea.startComplexRow();
								DefaultRowEdited row = (DefaultRowEdited) vea.getRow(j);
								DefaultFeature feature = (DefaultFeature) row.getLinkedRow().cloneRow();

								/* 5.3- Gets the feature of each row */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								values = feature.getAttributes();
								list = new ArrayList(Arrays.asList(values));

								/* 5.4- Calculates the geometric information for that geometry / feature */
								/* NUMBER OF POINTS */
								if (shape instanceof FMultiPoint2D) {
									value = ValueFactory.createValueByType(Integer.toString(((FMultiPoint2D)shape).getNumPoints()), Types.BIGINT);
								}
								else {
									if (shape instanceof FMultipoint3D) {
										value = ValueFactory.createValueByType(Integer.toString(((FMultipoint3D)shape).getNumPoints()), Types.BIGINT);
									}
									else
										continue;
								}

								/* 5.5- Sets the new value */
								if (cancelProcess.isCanceled()) {
//									vea.endComplexRow(operationName);
									throw new InterruptedException();
								}

								list.remove(addedField.getColumn());
								list.add(addedField.getColumn(), value);
								feature.setAttributes((Value[])list.toArray(new Value[0]));

								vea.modifyRow(row.getIndex(), feature, operationName, EditionEvent.ALPHANUMERIC);

								/* 5.6- Disables the edition of the row */
//								vea.endComplexRow(operationName);

								/* 5.7- Increments the counter of the fields added */
								n_fields_added ++;
							}
						}
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
				vea.stop();
				percentage += inc;
			}
		    }finally{
		    	vea.endComplexRow(operationName);
		    	mapControl.getMapContext().endAtomicEvent();
		    }

			/* 6- Stops layer in edition */
			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			percentage = 75;
			log.addLine(PluginServices.getText(null, "Stopping_the_layer_of_edition_mode"));
			mapControl.getCanceldraw().setCanceled(true);

			VectorialLayerEdited lyrEd = (VectorialLayerEdited)	editionManager.getActiveLayerEdited();
			if (lyrEd != null)
				lyrEd.clearSelection(true);
			try {
				layer.getRecordset().removeSelectionListener(lyrEd);
			} catch (ReadDriverException e) {
				NotificationManager.addError("Remove Selection Listener",e);
			}
//			if (pt != null){
//				pt.createAlias();
//
//			}

			percentage = 80;

			// Can't cancel next subtasks
			iTask.getButtonsPanel().getButton(ButtonsPanel.BUTTON_CANCEL).setEnabled(false);

			if (cancelProcess.isCanceled()) {
				throw new InterruptedException();
			}

			/* 6.1- Saves the layer */
			/* This part can't be cancelled */
			if (layer.isWritable()) {
				try {
					saveLayer(layer);
				}
				catch (Exception e) {
					log.addLine(PluginServices.getText(null, "Failed_saving_the_layer"));
					throw e;
				}

				percentage = 90;

				/* 6.2- Only finish the edition mode if wasn't being edited */
				vea.getCommandRecord().removeCommandListener(mapControl);
				layer.setEditing(false);
				if (layer.isSpatiallyIndexed()) {
	            	if (layer.getISpatialIndex() != null) {
						PluginServices.cancelableBackgroundExecution(new CreateSpatialIndexMonitorableTask((FLyrVect)layer));
	                }
				}

				/* 6.3- If has ended successfully the editing */
				layer.removeLayerListener(editionManager);
				if (layer instanceof FLyrAnnotation) {
					FLyrAnnotation lva = (FLyrAnnotation)layer;
		            lva.setMapping(lva.getMapping());
				}

				/* 6.4.a- If layer was being edited, restores it to that state */
				if (layerWasBeingEdited) {

					// Restores the previous tool
					mapControl.setTool(previousTool_ID);
					percentage = 91;

					view.hideConsole();
					percentage = 93;

					mapControl.drawMap(false);
					percentage = 96;
					CADExtension.clearView();

					startLayerInEdition(mapControl, cad_extension, editionManager, vea, layer);
				}
				else {
					/* 6.4.b- Restores the previous tool */
					mapControl.setTool(previousTool_ID);
					percentage = 91;

					view.hideConsole();

					percentage = 98;
					CADExtension.clearView();
				}

				percentage = 100;
				log.addLine(PluginServices.getText(null, "Process_finished_successfully"));
				layer.setWaitTodraw(false);
				mapControl.drawMap(false);
				return;
			}

			// Shouldn't execute this code!
			// This code can't be cancelled
			cancelEdition(layer);
			vea.getCommandRecord().removeCommandListener(mapControl);
			if (!(layer.getSource().getDriver() instanceof IndexedShpDriver)){
				VectorialLayerEdited vle=(VectorialLayerEdited)CADExtension.getEditionManager().getLayerEdited(layer);
				layer.setLegend((IVectorLegend)vle.getLegend());
			}

			layer.setEditing(false);

			/* 6.5- If layer was being edited, restores it to that state */
			if (layerWasBeingEdited) {
				startLayerInEdition(mapControl, cad_extension, editionManager, vea, layer);
			}

//			PluginServices.getMainFrame().enableControls();
			percentage = 100;
			log.addLine(PluginServices.getText(null, "Process_finished_successfully"));
			layer.setWaitTodraw(false);
			mapControl.drawMap(false);
			return;
		}
		catch (Exception e) {
			if (! cancelProcess.isCanceled())
				PluginServices.getLogger().error(PluginServices.getText(null, "Exception_adding_geometric_info"), e);

			try {
				try {
					// Removes the fields added
					while (n_fields_added >= 0) {
						vea.undo();
						n_fields_added --;
					}
				}
				catch (Exception ex) {
					PluginServices.getLogger().error(ex);
					log.addLine(PluginServices.getText(null, "Failed_restoring_layer_fields_should_remove_and_add_the_layer_to_have_consistent_data"));
					JOptionPane.showMessageDialog(iTask.getProgressPanel(), PluginServices.getText(null, "Failed_restoring_layer_fields_should_remove_and_add_the_layer_to_have_consistent_data"), PluginServices.getText(null, "Error"), JOptionPane.ERROR_MESSAGE);
				}

				// Emergency restore
				// This code can't be cancelled
				// Only finish the edition mode if wasn't being edited
				cancelEdition(layer);
				layer.setEditing(false);

				mapControl.setTool(previousTool_ID);
				view.hideConsole();

				CADExtension.clearView();

				// If layer was being edited, restores it to that state
				if (layerWasBeingEdited) {
					startLayerInEdition(mapControl, cad_extension, editionManager, vea, layer);
				}
				layer.setWaitTodraw(false);
				mapControl.drawMap(false);
//				PluginServices.getMainFrame().enableControls();
			}
			catch (Exception ex) {
				NotificationManager.showMessageError(PluginServices.getText(null, "Failed_restoring_layer_in_edition_mode"), ex);
				log.addLine(PluginServices.getText(null, "Failed_restoring_layer_in_edition_mode"));
			}
			throw new InterruptedException();
		}

	}

	/**
	 * <p>Adds a new field as a new column in a vector layer data source.</p>
	 *
	 * @param vea adapter of a vector layer that allows edit the layer
	 * @param geomInfo information about the new field to add
	 *
	 * @return information about the field added
	 */
	private AddedFieldInfo addField(VectorialEditableAdapter vea, GeomInfo geomInfo) {
		try {
			FieldDescription fD = null;

			if (geomInfo.getShapeType() == FShape.MULTIPOINT)
				fD = GeomInfo.getFieldDescription(geomInfo, Types.BIGINT, 13, (short) 0);
			else
				fD = GeomInfo.getFieldDescription(geomInfo, Types.DOUBLE, 13, (short) 6);

			DefaultRowEdited row = (DefaultRowEdited) vea.getRow(0);
			int column = row.getAttributes().length;

			vea.addField(fD);
			log.addLine(fD.getFieldName() + ": " + PluginServices.getText(null, "field_added_successfully"));
			return new AddedFieldInfo(column, fD);
		}
		catch (Exception e) {
			NotificationManager.showMessageError(PluginServices.getText(null, "Failed_creting_new_field") + " \"" + geomInfo.getName() + "\"", e);
			log.addLine(PluginServices.getText(null, "Failed_creting_new_field") + " \"" + geomInfo.getName() + "\"");
			return new AddedFieldInfo(-1, null);
		}
	}

	/**
	 * <p>Information about the field added.</p>
	 *
	 * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
	 */
	private class AddedFieldInfo {
		/**
		 * Column index in the data source, where was added.
		 */
		private int column;

		/**
		 * Field added.
		 */
		private FieldDescription fieldAdded;

		/**
		 * <p>Creates a new <code>AddedFieldInfo</code>.</p>
		 *
		 * @param column the column index in the data source, where was added
		 * @param fieldAdded the field added
		 */
		public AddedFieldInfo(int column, FieldDescription fieldAdded) {
			super();
			this.column = column;
			this.fieldAdded = fieldAdded;
		}

		/**
		 * <p>Gets the column index in the data source, where was added.</p>
		 *
		 * @return the column index in the data source, where was added
		 */
		public int getColumn() {
			return column;
		}

		/**
		 * <p>Gets the field added.</p>
		 *
		 * @return the field added
		 */
		public FieldDescription getFieldAdded() {
			return fieldAdded;
		}
	}

	/**
	 * <p>Starts layer in edition mode.</p>
	 *
	 * @param mapControl the <code>MapControl</code> object that contains the layer
	 * @param cad_extension extension that allows edit a layer
	 * @param editionManager manager for editing layers
	 * @param vea adapter of the editable vector layers
	 * @param layer the layer to start in edition mode
	 *
	 * @throws Exception any exception produced starting in edition the layer
	 */
	private void startLayerInEdition(MapControl mapControl, CADExtension cad_extension, EditionManager editionManager, VectorialEditableAdapter vea, FLyrVect layer) throws Exception {
		log.addLine(PluginServices.getText(null, "Starting_the_layer_in_edition_mode"));
		editionManager = CADExtension.getEditionManager();
		editionManager.setMapControl(mapControl);

		layer.addLayerListener(editionManager);

		ILegend legendOriginal = layer.getLegend();

		if (! layer.isWritable()) {
			JOptionPane.showMessageDialog((Component) PluginServices.getMDIManager().getActiveWindow(),
				PluginServices.getText(this, "this_layer_is_not_self_editable"),
				PluginServices.getText(this, "warning_title"),
				JOptionPane.WARNING_MESSAGE);

			throw new InterruptedException();
		}

		/* N.1- Sets the cad tool adapter if wasn't added */
		CADToolAdapter cta = CADExtension.getCADToolAdapter();
		if (! mapControl.getNamesMapTools().containsKey("cadtooladapter")) {
			mapControl.addMapTool("cadtooladapter", cta);
		}

		layer.setEditing(true);
		vea = (VectorialEditableAdapter) layer.getSource();

		vea.getRules().clear();
		if (vea.getShapeType() == FShape.POLYGON) {
			IRule rulePol = new RulePolygon();
			vea.getRules().add(rulePol);
		}

		if (! (layer.getSource().getDriver() instanceof IndexedShpDriver)) {
			VectorialLayerEdited vle=(VectorialLayerEdited)editionManager.getLayerEdited(layer);
			vle.setLegend(legendOriginal);
		}

		vea.getCommandRecord().addCommandListener(mapControl);

		/* N.2- If exits any layer associated, changes its model by the VectorialEditableAdapter's one */
		ProjectExtension pe = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);

		ProjectTable pt = pe.getProject().getTable(layer);
		this.layerProjectTable = pt;

		if (pt != null){
			pt.setModel(vea);

			/* N.3- If there is any view with the table associated to this vector layer -> updates the table */
			// This step is executed after finishing the process, to avoid problems with Swing threads
//		   	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices.getMDIManager().getAllWindows();
//
//			for (int i = 0 ; i < views.length ; i++) {
//				if (views[i] instanceof Table) {
//					Table table = (Table)views[i];
//					ProjectTable model = table.getModel();
//
//					if (model.equals(pt)) {
//						table.setModel(pt);
//						vea.getCommandRecord().addCommandListener(table);
//					}
//				}
//			}
		}

		/* N.4- Repaints the view */
		mapControl.drawMap(false);
	}

	/**
	 * <p>Saves and stops the edition of a vector layer.</p>
	 *
	 * @param layer the vector layer to save
	 *
	 * @throws Exception if fails saving the layer
	 */
	private void saveLayer(FLyrVect layer) throws Exception {
		try {
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
		catch (Exception e) {
			log.addLine(PluginServices.getText(null, "Failed_saving_the_layer"));
			throw e;
		}
	}

	/**
	 * <p>Cancels the edition process without saving.</p>
	 *
	 * @param layer the layer being edited
	 *
	 * @throws Exception if fails canceling the layer
	 */
	private void cancelEdition(FLyrVect layer) throws Exception {
		try {
			layer.setProperty("stoppingEditing",new Boolean(true));
			TablesFor.layer(layer).cancelEdition();
			com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices.getMDIManager().getAllWindows();
			VectorialEditableAdapter vea = (VectorialEditableAdapter) layer.getSource();
			vea.cancelEdition(EditionEvent.GRAPHIC);

			for (int j = 0; j < views.length; j++) {
				if (views[j] instanceof Table) {
					Table table = (Table) views[j];
					if ((table.getModel().getAssociatedTable() != null) && (table.getModel().getAssociatedTable().equals(layer))) {
						// Avoid conflicts with the Swing threads
				    	table.cancelEditingCell();
				        table.getModel().getModelo().cancelEdition(EditionEvent.ALPHANUMERIC);
						//table.cancelEditing();
					}
				}
			}

			layer.setProperty("stoppingEditing", new Boolean(false));
		}
		catch (Exception e) {
			log.addLine(PluginServices.getText(null, "Failed_canceling_the_layer"));
			throw e;
		}
	}
	
	protected void processFinalize() {
		super.processFinalize();
		ProjectExtension pe = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
		Project project=pe.getProject();
		ProjectTable pt = project.getTable(layer);
		if (pt==null)
			return;
		try {
			pt.createAlias();
		} catch (ReadDriverException e1) {
			e1.printStackTrace();
		}
		try {
            TablesFor.projectTable(pt).cancelEditing();
        } catch (CancelEditingTableException e1) {
            e1.printStackTrace();
        }
	}
}
