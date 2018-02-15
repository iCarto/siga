package es.icarto.gvsig.siga.croplayers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.vividsolutions.jts.geom.Geometry;

import es.icarto.gvsig.commons.datasources.SHPFactory;
import es.icarto.gvsig.commons.utils.FileNameUtils;
import es.icarto.gvsig.elle.style.LayerLabeling;
import es.icarto.gvsig.elle.style.LayerSimbology;

public class Crop {

    private final FLyrVect newLayer;
    private final FLyrVect layer;
    private final String path;

    /*
     * Podríamos intentar el geoproceso de clip ClipVisitor, ClipGeoprocess y ClipGeoprocessController. Pero tiene el
     * código muy acomplado y trabaja sobre los convexhull y no sobre las geometrías ens í, por lo que no no nos
     * interesa ahora
     */

    /*
     * Hacer bien un clip es complicado. Habría que descartar las geometrías que no sean del mismo tipo. Gestionar las
     * coleciones tras el intersection, ...
     */
    public Crop(FLyrVect layer, String tmpPath, Geometry g) throws BaseException, LegendDriverException {

        this.layer = layer;
        path = FileNameUtils.ensureExtension(tmpPath, "shp");

        List<IFeature> features = clipedFeatures(g);

        exportShapeFile(features);

        String crs = layer.getProjection().getAbrev();
        newLayer = SHPFactory.getFLyrVectFromSHP(new File(path), crs);

        addToTOC();

        ILegend simbology = new LayerSimbology(layer).transformForSHP();
        LayerSimbology newLayerSimbology = new LayerSimbology(newLayer);
        newLayerSimbology.set(simbology);
        newLayerSimbology.save();

        ILabelingStrategy labelling = new LayerLabeling(layer).transformForSHP();
        if (labelling != null) {
            LayerLabeling newLayerLabeling = new LayerLabeling(newLayer);
            newLayerLabeling.set(labelling);
            newLayerLabeling.save();
        }

        newLayer.setMaxScale(layer.getMaxScale());
        newLayer.setMinScale(layer.getMinScale());
        newLayer.setVisible(layer.isVisible());

    }

    private void exportShapeFile(List<IFeature> features) throws BaseException {
        File file = new File(path);
        FieldDescription[] fieldsDesc = layer.getRecordset().getFieldsDescription();
        int geometryType = layer.getShapeType();
        IFeature[] featureArray = features.toArray(new IFeature[0]);
        SHPFactory.createSHP(file, fieldsDesc, geometryType, featureArray);
    }

    private List<IFeature> clipedFeatures(Geometry g) throws ReadDriverException, ExpansionFileReadException {
        ReadableVectorial source = layer.getSource();
        List<IFeature> features = new ArrayList<IFeature>();
        int nFeatures = source.getShapeCount();
        for (int i = 0; i < nFeatures; i++) {
            IFeature feat = source.getFeature(i);
            Geometry geom = feat.getGeometry().toJTSGeometry();
            if (geom.intersects(g)) {
                Geometry clipedGeom = geom.intersection(g);
                IGeometry geom2 = FConverter.jts_to_igeometry(clipedGeom);
                Value[] attrs = feat.getAttributes();
                DefaultFeature df = new DefaultFeature(geom2, attrs, i + "");
                features.add(df);
            }

        }
        return features;
    }

    private void addToTOC() {
        FLayers parentLayer = layer.getParentLayer();
        int layerPos = -1;
        for (int i = 0; i < parentLayer.getLayersCount(); i++) {
            // Yes. We are comparing objects id
            if (parentLayer.getLayer(i) == layer) {
                layerPos = i;
                break;
            }
        }
        parentLayer.addLayer(layerPos, newLayer);
    }

    public FLayer getNewLayer() {
        return newLayer;
    }
}
