package org.gvsig.mapsheets.print.series.fmap;

import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cresques.cts.ICoordTrans;

import com.hardcode.driverManager.Driver;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FLabel;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.utiles.PostProcessSupport;
import com.iver.utiles.swing.threads.AbstractMonitorableTask;

/**
 * This task exports a grid to SHP in a separate thread.
 * 
 * 
 * @author jldominguez
 *
 */
public class MapSheetsGridWriteTask extends AbstractMonitorableTask
{
	FLyrVect lyrVect;
	IWriter writer;
	int rowCount;
	ReadableVectorial va;
	SelectableDataSource sds;
	FBitSet bitSet;
	MapContext mapContext;
	VectorialDriver reader;

	public MapSheetsGridWriteTask(MapContext mapContext, FLyrVect lyr, IWriter writer, Driver reader) throws ReadDriverException
	{
		this.mapContext = mapContext;
		this.lyrVect = lyr;
		this.writer = writer;
		this.reader = (VectorialDriver) reader;

		setInitialStep(0);
		setDeterminatedProcess(true);
		setStatusMessage(PluginServices.getText(this, "exportando_features"));

		va = lyrVect.getSource();
		sds = lyrVect.getRecordset();

		bitSet = sds.getSelection();

		if (bitSet.cardinality() == 0)
			rowCount = va.getShapeCount();
		else
			rowCount = bitSet.cardinality();

		setFinalStep(rowCount);
	}
	
	public void run() throws Exception {
		lyrVect.setWaitTodraw(true);
		va.start();
		ICoordTrans ct = lyrVect.getCoordTrans();
		DriverAttributes attr = va.getDriverAttributes();
		boolean bMustClone = false;
		if (attr != null) {
			if (attr.isLoadedInMemory()) {
				bMustClone = attr.isLoadedInMemory();
			}
		}
		if (lyrVect instanceof FLyrAnnotation && lyrVect.getShapeType()!=FShape.POINT) {
			SHPLayerDefinition lyrDef=(SHPLayerDefinition)writer.getTableDefinition();
			lyrDef.setShapeType(FShape.POINT);
			writer.initialize(lyrDef);
		}

//		if(writer instanceof ShpWriter) {
//			String charSetName = prefs.get("dbf_encoding", DbaseFile.getDefaultCharset().toString());
//			if(lyrVect.getSource() instanceof VectorialFileAdapter) {
//				((ShpWriter)writer).loadDbfEncoding(((VectorialFileAdapter)lyrVect.getSource()).getFile().getAbsolutePath(), Charset.forName(charSetName));
//			} else {
//				Object s = lyrVect.getProperty("DBFFile");
//				if(s != null && s instanceof String)
//					((ShpWriter)writer).loadDbfEncoding((String)s, Charset.forName(charSetName));
//			}
//		}

		// Creamos la tabla.
		writer.preProcess();

		if (bitSet.cardinality() == 0) {
			rowCount = va.getShapeCount();
			for (int i = 0; i < rowCount; i++) {
				if (isCanceled())
					break;
				IGeometry geom = va.getShape(i);
				if (geom == null) {
					reportStep();
					continue;
				}
				if (lyrVect instanceof FLyrAnnotation && geom.getGeometryType()!=FShape.POINT) {
					Point2D p=FLabel.createLabelPoint((FShape)geom.getInternalShape());
					geom=ShapeFactory.createPoint2D(p.getX(),p.getY());
				}
				if (isCanceled())
					break;
				if (ct != null) {
					if (bMustClone)
						geom = geom.cloneGeometry();
					geom.reProject(ct);
				}
				reportStep();
				setNote(PluginServices.getText(this, "exporting_") + i);
				if (isCanceled())
					break;

				if (geom != null) {
					Value[] values = sds.getRow(i);
					IFeature feat = new DefaultFeature(geom, values, "" + i);
					DefaultRowEdited edRow = new DefaultRowEdited(feat,
							DefaultRowEdited.STATUS_ADDED, i);
					writer.process(edRow);
				}
			}
		} else {
			int counter = 0;
			for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet
			.nextSetBit(i + 1)) {
				if (isCanceled())
					break;
				IGeometry geom = va.getShape(i);
				if (geom == null) {
					reportStep();
					continue;
				}
				if (lyrVect instanceof FLyrAnnotation && geom.getGeometryType()!=FShape.POINT) {
					Point2D p=FLabel.createLabelPoint((FShape)geom.getInternalShape());
					geom=ShapeFactory.createPoint2D(p.getX(),p.getY());
				}
				if (isCanceled())
					break;
				if (ct != null) {
					if (bMustClone)
						geom = geom.cloneGeometry();
					geom.reProject(ct);
				}
				reportStep();
				setNote(PluginServices.getText(this, "exporting_") + counter);
				if (isCanceled())
					break;

				if (geom != null) {
					Value[] values = sds.getRow(i);
					IFeature feat = new DefaultFeature(geom, values, "" + i);
					DefaultRowEdited edRow = new DefaultRowEdited(feat,
							DefaultRowEdited.STATUS_ADDED, i);

					writer.process(edRow);
				}
			}

		}

		writer.postProcess();
		va.stop();
		if (reader != null && !isCanceled()){
			int res = JOptionPane.showConfirmDialog(
					(JComponent) PluginServices.getMDIManager().getActiveWindow()
					, PluginServices.getText(this, "insertar_en_la_vista_la_capa_creada"),
					PluginServices.getText(this,"insertar_capa"),
					JOptionPane.YES_NO_OPTION);

			if (res == JOptionPane.YES_OPTION)
			{
				PostProcessSupport.executeCalls();
				ILayerDefinition lyrDef = (ILayerDefinition) writer.getTableDefinition();
				FLayer newLayer = LayerFactory.createLayer(
						lyrDef.getName(), reader, mapContext.getProjection());
				mapContext.getLayers().addLayer(newLayer);
			}
		}
		lyrVect.setWaitTodraw(false);

	}



}
