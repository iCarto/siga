package org.gvsig.copypastegeom;

import org.gvsig.copypastegeom.toc.CopyFeaturesTocMenuEntry;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.Version;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ISpatialDB;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;
import com.vividsolutions.jts.io.WKTWriter;

import es.icarto.gvsig.commons.AbstractExtension;

public class CopyFeaturesExtension extends AbstractExtension {

    private static WKTWriter geometryWriter = new WKTWriter();
    private FLyrVect layer;

    public void execute(String actionCommand) {
        try {
            copyFeatures();
        } catch (ReadDriverException e) {
            NotificationManager.addError(PluginServices.getText(this, "error_ejecutando_la_herramienta"), e);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        registerExtensionPoint();
    }

    public void registerExtensionPoint() {
        ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
        extensionPoints.add("View_TocActions", "CopyFeatures", new CopyFeaturesTocMenuEntry());
    }

    public boolean isEnabled() {
        View view = getView();
        MapControl mapControl = view.getMapControl();
        FLayer[] layers = mapControl.getMapContext().getLayers().getActives();
        if (layers.length != 1) {
            return false;
        }
        FLayer layer = layers[0];
        if (layer instanceof FLyrVect) {
            FLyrVect lyrVect = (FLyrVect) layer;
            SelectableDataSource rs;
            try {
                if (!lyrVect.isAvailable()) {
                    return false;
                }
                rs = lyrVect.getRecordset();
                if (rs != null) {
                    FBitSet selection = rs.getSelection();
                    if (!selection.isEmpty()) {
                        this.layer = (FLyrVect) layer;
                        return true;
                    }
                }
            } catch (ReadDriverException e) {
                NotificationManager.addWarning(
                        PluginServices.getText(this, "no_se_ha_podido_habilitar_la_herramienta"), e);
            }
        }
        return false;
    }

    @Override
    public boolean isVisible() {
        return getView() != null;
    }

    public void copyFeatures() throws ReadDriverException {
        if (layer != null) {
            XMLEntity xmlRoot = newRootNode();
            xmlRoot.putProperty("shapeType", layer.getShapeType());
            SelectableDataSource rs;

            // rs.start();
            try {
                rs = layer.getRecordset();
                ReadableVectorial rv = layer.getSource();
                rv.start();
                SelectionSupport selectionSupport = rs.getSelectionSupport();

                FieldDescription[] fieldsDescription = rs.getFieldsDescription();
                String[] fields = new String[fieldsDescription.length];
                XMLEntity xmlFields = new XMLEntity();
                xmlFields.setName("fields");
                for (int i = 0; i < fieldsDescription.length; i++) {
                    xmlFields.addChild(getFieldXML(fieldsDescription[i]));
                    fields[i] = fieldsDescription[i].getFieldName();
                }
                xmlRoot.addChild(xmlFields);

                XMLEntity xmlFeatures = new XMLEntity();
                xmlFeatures.setName("features");

                IFeature feat = null;
                XMLEntity featureXML;
                for (int numReg = 0; numReg < rv.getShapeCount(); numReg++) {
                    feat = rv.getFeature(numReg);
                    int selectionIndex = -1;
                    if (rv instanceof ISpatialDB) {
                        selectionIndex = ((ISpatialDB) rv).getRowIndexByFID(feat);
                    } else {
                        selectionIndex = Integer.parseInt(feat.getID());
                    }
                    if (selectionIndex != -1) {
                        if (!selectionSupport.isSelected(selectionIndex)) {
                            continue;
                        }
                    }

                    featureXML = getFeatureXML(feat, fieldsDescription);
                    xmlFeatures.addChild(featureXML);

                }
                xmlRoot.addChild(xmlFeatures);
                PluginServices.putInClipboard(xmlRoot.toString());
                rv.stop();
            } finally {
                // rs.stop();
            }

        }
    }

    private XMLEntity newRootNode() {
        XMLEntity xml = new XMLEntity();
        fillXMLRootNode(xml);
        return xml;
    }

    private void fillXMLRootNode(XMLEntity xml) {
        xml.putProperty("applicationName", "gvSIG");
        xml.putProperty("version", Version.format());
        xml.putProperty("type", "GeometryCopy");
    }

    private XMLEntity getFeatureXML(IFeature feat, FieldDescription[] fieldsDescription) throws ReadDriverException {
        XMLEntity xml = new XMLEntity();
        xml.setName("feature");
        for (int i = 0; i < fieldsDescription.length; i++) {
            if (fieldsDescription[i].getFieldName().equalsIgnoreCase("gid")) {
                xml.putProperty(fieldsDescription[i].getFieldName(), null);
            } else {
                xml.putProperty(fieldsDescription[i].getFieldName(), feat.getAttribute(i).toString());
            }
        }
        xml.addChild(getGeometryXML(feat.getGeometry()));
        return xml;
    }

    private XMLEntity getFieldXML(FieldDescription fd) {
        XMLEntity xml = new XMLEntity();

        xml.setName("field");
        xml.putProperty("fieldAlias", fd.getFieldAlias());
        xml.putProperty("fieldName", fd.getFieldName());
        xml.putProperty("fieldDecimalCount", fd.getFieldDecimalCount());
        xml.putProperty("fieldType", fd.getFieldType());

        return xml;
    }

    private XMLEntity getGeometryXML(IGeometry geom) {
        XMLEntity xml = new XMLEntity();

        xml.setName("geometry");
        xml.putProperty("geometry", geometryWriter.write(geom.toJTSGeometry()));

        return xml;
    }

}
