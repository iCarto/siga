package es.icarto.gvsig.siga.extractvertexestool;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.swing.threads.IMonitorableTask;

public class ExtractVertexesAddResultLayerTask implements IMonitorableTask {
    FLayers layers;
    int initialStep = 1;
    int currentStep = 5;
    int lastStep = 10;
    boolean finished = false;
    ExtractVertexesGeoprocess geoprocess;

    public ExtractVertexesAddResultLayerTask(
            ExtractVertexesGeoprocess geoprocess) {
        this.geoprocess = geoprocess;
    }

    public void setLayers(FLayers layers) {
        this.layers = layers;
    }

    public int getInitialStep() {
        return initialStep;
    }

    public int getFinishStep() {
        return lastStep;
    }

    public int getCurrentStep() {
        if (!finished)
            return currentStep;
        else
            return lastStep;
    }

    public String getStatusMessage() {
        return "Cargando capa...";
    }

    public String getNote() {
        return "";
    }

    public boolean isDefined() {
        return true;
    }

    public void cancel() {
        finished = true;
    }

    private boolean checkToAdd(FLayer layer) throws ExtractVertexesException {
        try {
            if (layer instanceof FLyrVect) {
                FLyrVect result = (FLyrVect) layer;
                if (result.getSource().getShapeCount() > 0) {
                    return true;
                }
                return false;
            } else if (layer instanceof FLayers) {
                FLayers result = (FLayers) layer;
                int numLayers = result.getLayersCount();
                if (numLayers == 0)
                    return false;
                for (int i = 0; i < numLayers; i++) {
                    FLayer lyrI = result.getLayer(i);
                    if (lyrI instanceof FLyrVect) {
                        if (((FLyrVect) lyrI).getSource().getShapeCount() != 0)
                            return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } catch (ReadDriverException e) {
            throw new ExtractVertexesException(
                    "Error al chequear la capa resultado antes de pasarla al TOC");
        }
    }

    public void run() throws ExtractVertexesException {
        try {
            FLayer result = geoprocess.getResult();
            if (checkToAdd(result)) {
                if (result instanceof FLayers) {
                    FLayers resultLayers = (FLayers) result;
                    if (layers.getLayer(resultLayers.getName()) == null) {
                        for (int i = 0; i < resultLayers.getLayersCount(); i++)
                            layers.addLayer(resultLayers.getLayer(i));
                    }
                } else
                    layers.addLayer(result);
            } else {
                JOptionPane.showMessageDialog((JComponent) PluginServices
                        .getMDIManager().getActiveWindow(), PluginServices
                        .getText(this, "Error_capa_vacia"), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (CancelationException e) {
            throw new ExtractVertexesException(
                    "Error al añadir el resultado de un geoproceso a flayers");
        } finally {
            finished = true;
        }
    }

    public boolean isCanceled() {
        return false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finished() {

    }
}
