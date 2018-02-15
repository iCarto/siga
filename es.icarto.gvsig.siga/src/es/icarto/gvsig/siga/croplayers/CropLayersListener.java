package es.icarto.gvsig.siga.croplayers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.Events.MeasureEvent;
import com.iver.cit.gvsig.fmap.tools.Listeners.PolylineListener;

import es.icarto.gvsig.siga.CropLayersExtension;

public class CropLayersListener implements PolylineListener {

    private static final Logger logger = Logger.getLogger(CropLayersListener.class);

    /**
     * The image to display when the cursor is active.
     */
    private Image img = null;

    /**
     * The cursor used to work with this tool listener.
     *
     * @see #getCursor()
     */
    private Cursor cur = null;

    private final MapControl mapControl;
    private final List<FLayer> layers;
    private final String folder;

    public CropLayersListener(MapControl mapControl, List<FLayer> layers, String folder) {
        this.mapControl = mapControl;
        this.layers = layers;
        this.folder = folder;
    }

    /**
     * The key of an image registered in the active theme
     */
    public void setCursorImage(String iconThemeKey) {
        img = PluginServices.getIconTheme().get(iconThemeKey).getImage();
        Point size = new Point(16, 16);
        cur = Toolkit.getDefaultToolkit().createCustomCursor(img, size, "");
    }

    @Override
    public Cursor getCursor() {
        return cur;
    }

    @Override
    public boolean cancelDrawing() {
        return false;
    }

    @Override
    public void points(MeasureEvent event) throws BehaviorException {
    }

    @Override
    public void pointFixed(MeasureEvent event) throws BehaviorException {
    }

    @Override
    public void polylineFinished(MeasureEvent event) throws BehaviorException {

        GeneralPathX gp = event.getGP();
        IGeometry geom = ShapeFactory.createPolygon2D(gp);

        if (!geom.toJTSGeometry().isValid()) {
            String title = "Aviso";
            String message = "El polígono dibujado es incorrecto. Probablemente tiene auto-intersecciones.\nDibújelo de nuevo o vuelva a pulsar en el botón para cancelar";
            Component mainFrame = (Component) PluginServices.getMainFrame();
            JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE);
            // To change the icon in the toolbar
            PluginServices.getMainFrame().enableControls();
            return;
        }
        foo(geom);

        //
    }

    private void foo(IGeometry clipperGeom) {
        CropLayersExtension extension = null;
        try {
            PluginServices.getMDIManager().setWaitCursor();
            extension = (CropLayersExtension) PluginServices.getExtension(CropLayersExtension.class);
            Export export = new Export(folder, layers, clipperGeom);
            export.export();

            CropLayersExtension.clipedLayers.addAll(export.getNewLayers());

            for (FLayer l : export.getOldLayers()) {
                CropLayersExtension.dbLayers.add(new Visibility(l, l.isVisible()));
                l.setVisible(false);
            }
        } catch (Exception e) {
            logger.error(e.getStackTrace(), e);
        } finally {
            extension.cleanupTool();
            PluginServices.getMDIManager().restoreCursor();
        }

    }

}
