package es.icarto.gvsig.siga.croplayers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gvsig.exceptions.BaseException;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.udc.cartolab.gvsig.elle.utils.TOCGroupsHandler;

public class Export {

    private static final Logger logger = Logger.getLogger(Export.class);

    private final static String LAYER_NAME_SUFFIX = "_recortada";

    List<FLayer> oldLayers = new ArrayList<FLayer>();
    List<FLayer> newLayers = new ArrayList<FLayer>();

    public Export(String folder, List<FLayer> layers, IGeometry clipperGeom) {

        // Asume que todas las layers a exportar están en la misma View/MapContext
        MapContext mapContext = layers.get(0).getMapContext();

        TOCGroupsHandler tocGroupsHandler = new TOCGroupsHandler(mapContext);
        for (FLayer layer : layers) {
            if (!(layer instanceof FLyrVect)) {
                continue;
            }
            List<FLayer> hierarchy = tocGroupsHandler.getHierarchy(layer);
            String path = folder;
            for (FLayer l : hierarchy) {
                if ((l.getName() != null) && l.getName().length() > 0) {
                    path += l.getName() + File.separator;
                }
            }
            new File(path).mkdirs();
            if (!new File(path).isDirectory()) {
                throw new RuntimeException("No se han podido crear los directorios");
            }
            path += layer.getName() + LAYER_NAME_SUFFIX;
            try {

                Crop crop = new Crop((FLyrVect) layer, path, clipperGeom.toJTSGeometry());
                newLayers.add(crop.getNewLayer());
                oldLayers.add(layer);

            } catch (BaseException e) {
                logger.error(e.getStackTrace(), e);
            } catch (LegendDriverException e) {
                logger.error(e.getStackTrace(), e);
            }
        }

    }

    public void export() {
    }

    public List<FLayer> getNewLayers() {
        return newLayers;
    }

    public List<FLayer> getOldLayers() {
        return oldLayers;
    }

}
