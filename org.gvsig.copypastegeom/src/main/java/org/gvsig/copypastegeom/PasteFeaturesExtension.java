package org.gvsig.copypastegeom;

import java.awt.geom.Rectangle2D;
import java.text.ParseException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.gvsig.copypastegeom.toc.PasteFeaturesTocMenuEntry;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.EditionManager;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.Version;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SpatialCache;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Paste the features
 * 
 * @author Vicente Caballero NAvarro
 *
 */
public class PasteFeaturesExtension extends Extension {

	private View view;
	private MapControl mapControl;
	FLyrVect lv;
	private XMLEntity xml=null;
	private static WKTReader geometryReader = new WKTReader();


	/* (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
	 */
	public void execute(String actionCommand) {
		try {
			pasteFeatures();
		} catch (Exception e) {
			NotificationManager.addError(PluginServices.getText(this, "error_ejecutando_la_herramienta"),e);
		}

	}

	/* (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#initialize()
	 */
	public void initialize() {
		registerIcons();
		registerExtensionPoint();
	}
	/**
	 * Register the icons.
	 */
	private void registerIcons(){
		PluginServices.getIconTheme().registerDefault(
				"paste_features",
				this.getClass().getClassLoader().getResource("images/paste_features.png")
			);
	}
	/**
	 * Register toc action.
	 */
    public void registerExtensionPoint() {
    	ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
       	extensionPoints.add("View_TocActions","PasteFeatures",new PasteFeaturesTocMenuEntry());
    }



	/* (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isEnabled()
	 */
	public boolean isEnabled() {
		if (EditionUtilities.getEditionStatus() == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE) {
			view = (View) PluginServices.getMDIManager().getActiveWindow();
			mapControl = view.getMapControl();
			EditionManager em=CADExtension.getEditionManager();
			if (em.getActiveLayerEdited()==null)
				return false;
			VectorialLayerEdited vle=(VectorialLayerEdited)em.getActiveLayerEdited();
			FLyrVect lv=(FLyrVect)vle.getLayer();
			if (lv != null){
				this.xml = this.getCheckedXMLFromClipboard();
				/*
				 * Comprobamos también que el tipo de shape coincida para saber si podemos
				 * pegar en la capa las features que tenemos en el portapapeles.
				 */
				if(this.xml != null){
					try {
						int shapeType = this.xml.getIntProperty("shapeType");
						if (shapeType != lv.getShapeType()){
							return false;
						}
					} catch (NotExistInXMLEntity e) {
						return false;
					} catch (ReadDriverException e) {
						NotificationManager.addError(PluginServices.getText(this, "error_comprobando_el_tipo_de_shape_de_la_capa"),e);
					}
				}
				return this.xml != null;
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isVisible()
	 */
	public boolean isVisible() {
		if (EditionUtilities.getEditionStatus() == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
			return true;
		return false;
	}
	/**
	 * Paste features
	 * @throws Exception
	 */
	public void pasteFeatures() throws Exception{
		if(xml != null){
			CADExtension.initFocus();
			CADExtension.getEditionManager().setMapControl(mapControl);

			View vista = (View)PluginServices.getMDIManager().getActiveWindow();
			mapControl = vista.getMapControl();
			EditionManager em=CADExtension.getEditionManager();
			if (em.getActiveLayerEdited() != null) {
				VectorialLayerEdited vle=(VectorialLayerEdited)em.getActiveLayerEdited();
				VectorialEditableAdapter vea = vle.getVEA();
				lv=(FLyrVect)vle.getLayer();
				MapContext mapContext = lv.getMapContext();

				int shapeType = xml.getIntProperty("shapeType");
				if (shapeType != lv.getShapeType()){
					return;
				}
				int child = xml.firstIndexOfChild("name", "fields");
				if (child == -1) {
					return;
				}
				FieldDescription[] fieldsDescription = getFieldsDescription(xml.getChild(child));

				child = xml.firstIndexOfChild("name", "features");
				if (child == -1) {
					return;
				}

				XMLEntity featuresXML = xml.getChild(child);
				if (featuresXML == null){
					return;
				}

//				ReadableVectorial mySource = lv.getSource();
				SelectableDataSource myRs = lv.getRecordset();
				mapContext.beginAtomicEvent();
				myRs.start();
				try{
					int featuresCount = featuresXML.getChildrenCount();
					for (int i = 0; i< featuresCount; i++){ //bucle por las features
						XMLEntity featureXML = featuresXML.getChild(i);
						Value[] values = new Value[myRs.getFieldCount()];
						for(int j=0; j<values.length; j++){ //bucle por los campos de myRs para rellenarlos
							String name = myRs.getFieldName(j);
							int type = myRs.getFieldType(j);
							values[j] = ValueFactory.createNullValue();
							for(int k=0; k<fieldsDescription.length; k++){ //bucle buscando el campo que queremos rellenar
								if (fieldsDescription[k].getFieldName().compareTo(name)==0 &&
										fieldsDescription[k].getFieldType()==type){
									String stringValue = featureXML.getStringProperty(name);
									try {
										values[j] = ValueFactory.createValueByType(stringValue, type);
									} catch (ParseException pe){
										values[j] = ValueFactory.createNullValue();
									}
								}
							}
						}

						child = featureXML.firstIndexOfChild("name", "geometry");
						if (child == -1) {
							continue;
						}
						XMLEntity geometryXML = featureXML.getChild(child);
						if (geometryXML == null){
							return;
						}
						IGeometry geom = FConverter.jts_to_igeometry(geometryReader.read(geometryXML.getStringProperty("geometry"))); //.cloneGeometry();
						if (geom != null) {
							String newFID = vea.getNewFID();
							DefaultFeature df = new DefaultFeature(geom, values, newFID);
							vea.addRow(df, getName(), EditionEvent.GRAPHIC);

							SpatialCache spatialCache=lv.getSpatialCache();
							Rectangle2D r=geom.getBounds2D();
							if (geom.getGeometryType()==FShape.POINT) {
								r = new Rectangle2D.Double(r.getX(),r.getY(),1,1);
							}
							spatialCache.insert(r,geom);

							CADExtension.getCADToolAdapter().getMapControl().rePaintDirtyLayers();

						}
					}
				} catch (Exception e){
					throw e;
				} finally {
					myRs.stop();
					mapContext.endAtomicEvent();
				}

			}
		}
	}

	private XMLEntity getCheckedXMLFromClipboard() {
		String sourceString = PluginServices.getFromClipboard();
		if (sourceString == null) return null;

		XMLEntity xml;
		try {
			xml = XMLEntity.parse(sourceString);
		} catch (MarshalException e) {
			return null;
		} catch (ValidationException e) {
			return null;
		}
		try {
			if(xml.getStringProperty("applicationName").compareTo("gvSIG")!=0){
				return null;
			}
			//Comentarizar esto para que se admita la copia entre versiones
			if(xml.getStringProperty("version").compareTo(Version.format())!=0){
				return null;
			}
			if(xml.getStringProperty("type").compareTo("GeometryCopy")!=0){
				return null;
			}
		} catch (NotExistInXMLEntity e){
			return null;
		}
		return xml;
	}

	private FieldDescription[] getFieldsDescription(XMLEntity xml){
		if (xml.getName().compareTo("fields")!=0){
			throw new NotExistInXMLEntity();
		}
		int childrenCount = xml.getChildrenCount();
		FieldDescription[] fieldsDescription = new FieldDescription[childrenCount];
		for (int i=0; i<childrenCount; i++){
			XMLEntity fieldChild = xml.getChild(i);
			fieldsDescription[i] = getFieldDescription(fieldChild);
		}
		return fieldsDescription;
	}
	private FieldDescription getFieldDescription(XMLEntity xml){
		if (xml.getName().compareTo("field")!=0){
			throw new NotExistInXMLEntity();
		}
		FieldDescription fieldDescription = new FieldDescription();
		fieldDescription.setFieldAlias(xml.getStringProperty("fieldAlias"));
		fieldDescription.setFieldName(xml.getStringProperty("fieldName"));
		fieldDescription.setFieldDecimalCount(xml.getIntProperty("fieldDecimalCount"));
		fieldDescription.setFieldType(xml.getIntProperty("fieldType"));

		return fieldDescription;
	}

	public String getName() {
		return PluginServices.getText(this,"paste_feature_");
	}

}