package com.iver.cit.gvsig.fmap.drivers.gvl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.drivers.legend.IFMapLegendDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.xmlEntity.generate.XmlTag;


public class FMapGVLDriver implements IFMapLegendDriver{

	private static final String DESCRIPTION = "gvSIG legend";
	private static final String FILE_EXTENSION = "gvl";

	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		String fName = f.getAbsolutePath();
		if (fName!=null) {
			fName = fName.toLowerCase();
			return fName.endsWith("." + FILE_EXTENSION);
		}
		return false;
	}


	public String getDescription() {
		return DESCRIPTION;
	}

	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	public Hashtable read(FLayers layers,FLayer layer, File file) throws LegendDriverException {

		Hashtable table = new Hashtable();
		File xmlFile = new File(file.getAbsolutePath());
		FileReader reader = null;

		try {
			reader = new FileReader(xmlFile);

			XmlTag tag = (XmlTag) XmlTag.unmarshal(reader);
			ILegend myLegend = LegendFactory.createFromXML(new XMLEntity(tag));

			if(myLegend != null ) {
				//CAPA DE LINEAS
				if (layer instanceof FLyrVect) {
					FLyrVect m = (FLyrVect) layer;
					IVectorLegend l = (IVectorLegend) myLegend;
					int errors = 0;

					// check general conditions for IVectorLegend
					try {

						// check shape type
//						if (l.getShapeType() != 0 && m.getShapeType() != l.getShapeType()) {
						//Remove the Z and the M coordinate...
						int shapeType = m.getShapeType();
						if (shapeType > FShape.M){
							shapeType = shapeType - FShape.M;
						}
						if (shapeType > FShape.Z){
							shapeType = shapeType - FShape.Z;
						}						
						if((l.getDefaultSymbol().getSymbolType() != shapeType)
								&& !l.isSuitableForShapeType(shapeType)){
							errors |= LegendDriverException.LAYER_SHAPETYPE_MISMATCH;
						}

					} catch (ReadDriverException e) {
						errors |= LegendDriverException.READ_DRIVER_EXCEPTION;
					}


					if(myLegend instanceof IClassifiedVectorLegend) {
						IClassifiedVectorLegend cl = (IClassifiedVectorLegend) myLegend;

						String[] fNames = cl.getClassifyingFieldNames();
						int[] fTypes = cl.getClassifyingFieldTypes();


						try {
							for (int i = 0; i < fNames.length; i++) {
								SelectableDataSource sds = m.getRecordset();
								int fieldIndex = sds.getFieldIndexByName(fNames[i]);
								if (fieldIndex != -1) {
									if(fTypes != null)
										if(fTypes[i] !=  sds.getFieldType(fieldIndex)) {
											errors |= LegendDriverException.CLASSIFICATION_FIELDS_TYPE_MISMATCH;
										}
								} else {
									errors |= LegendDriverException.CLASSIFICATION_FIELDS_NOT_FOUND;
								}
							}
						} catch (ReadDriverException e) {
							errors |= LegendDriverException.READ_DRIVER_EXCEPTION;
						}
					}
					if (errors == 0) {
						table.put(layer, myLegend);
						//return myLegend;
						return table;
					}
					else throw new LegendDriverException(errors);
				}
			}
		} catch (FileNotFoundException e) {
			// should be unreachable code
			throw new Error ("file_not_found");
		} catch (MarshalException e) {
			throw new Error ("file_corrupt. layer: " + layer.getName() + ". file: " + file);
		} catch (ValidationException e) {
			// should be unreachable code
			throw new Error ("ValidationException");
		} catch (XMLException e) {
			throw new Error ("unsupported_legend");
		} finally {
			try {
				if (reader!=null) {
					reader.close();
				}
			} catch (IOException e) {

				throw new Error ("file_closing_failed");
			}
		}
		return null;

	}

	public void write(FLayers layers, FLayer layer, ILegend legend, File file, String version) throws LegendDriverException  {
		FileWriter writer;
		int errors = 0;
		try {
			writer = new FileWriter(file.getAbsolutePath());
			Marshaller m = new Marshaller(writer);
			m.setEncoding("ISO-8859-1");
			m.marshal(legend.getXMLEntity().getXmlTag());
		} catch (MarshalException e) {
			errors |= LegendDriverException.SAVE_LEGEND_ERROR;
		} catch (ValidationException e) {
			errors |= LegendDriverException.SAVE_LEGEND_ERROR;
		} catch (IOException e) {
			errors |= LegendDriverException.SYSTEM_ERROR;
		}
		if (errors != 0) throw new LegendDriverException(errors);

	}

	public ArrayList<String> getSupportedVersions() {
		ArrayList<String> versions = new ArrayList<String>();
		versions.add("gvSIG 1.0.0");
		return versions;
	}

}
