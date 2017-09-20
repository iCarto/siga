package es.icarto.gvsig.extgia.forms;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.PointSelectionListener;
import com.iver.cit.gvsig.fmap.tools.Events.PointEvent;

public class ViewPointListener extends PointSelectionListener {

    private static final int TOLERANCE = 4;

    private static final Logger logger = Logger.getLogger(ViewPointListener.class);

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

    private IWindow lastOpenDialog;

    private boolean selectFeatures = false;

    private List<FLyrVect> searchableLayers;

    public ViewPointListener(MapControl mc) {
        super(mc);
    }

    /**
     * The key of an image registered in the active theme
     */
    public void setCursorImage(String iconThemeKey) {
        img = PluginServices.getIconTheme().get(iconThemeKey).getImage();
        Point size = new Point(16, 16);
        cur = Toolkit.getDefaultToolkit().createCustomCursor(img, size, "");
    }

    /**
     * If true, changes the selection on the layers to select the features on
     * the point
     *
     */
    public void setSelectFeatures(boolean selectFeatures) {
        this.selectFeatures = selectFeatures;
    }

    @Override
    public Cursor getCursor() {
        return cur != null ? cur : super.getCursor();
    }

    /**
     *
     * @return the layers (actives or not) that have a feature in that point
     */
    private Map<FLyrVect, FBitSet> layersInPoint(Point2D p) {

        Map<FLyrVect, FBitSet> layers = new HashMap<FLyrVect, FBitSet>();

        // Tolerancia = TOLERANCE
        final double tol = mapCtrl.getViewPort().toMapDistance(TOLERANCE);
        final Point2D mapPoint = mapCtrl.getViewPort().toMapPoint((int) p.getX(), (int) p.getY());

        for (FLyrVect lyrVect : searchableLayers) {

            FBitSet newBitSet = null;
            try {
                newBitSet = lyrVect.queryByPoint(mapPoint, tol);
            } catch (ReadDriverException e) {
                logger.error(e.getStackTrace(), e);
            } catch (VisitorException e) {
                logger.error(e.getStackTrace(), e);
            }
            if ((newBitSet != null) && (newBitSet.size() > 0)) {
                layers.put(lyrVect, newBitSet);
            }

        }
        return layers;

    }

    @Override
    public void point(PointEvent event) throws BehaviorException {

        try {
            PluginServices.getMDIManager().setWaitCursor();
            Map<FLyrVect, FBitSet> layersInPoint = layersInPoint(event.getPoint());
            for (FLyrVect layer : layersInPoint.keySet()) {
                // if (EnabilityConditions.isFormOpenable(layer)) {

                FBitSet bs = layersInPoint.get(layer);
                if (selectFeatures) {
                    SelectableDataSource sds = layer.getRecordset();
                    sds.setSelection(bs);
                }
                for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                    openForm(layer, i);
                }

                // }
            }
        } catch (ReadDriverException e) {
            throw new BehaviorException("No se pudo hacer la selección");
        } catch (VisitorException e) {
            throw new BehaviorException("No se pudo hacer la selección");
        } finally {
            PluginServices.getMDIManager().restoreCursor();
        }
    }

    /**
     * returns true if a form has been opened
     */
    private boolean openForm(FLyrVect layer, int selectedPos) throws ReadDriverException, VisitorException {
        ImagesInView a = new ImagesInView(layer, selectedPos);

        // if (selectedPos != -1) {
        // final AbstractForm dialog = FormFactory
        // .createFormRegistered(lyrVect);
        // if ((dialog != null) && (dialog.init())) {
        // if (lastOpenDialog != null) {
        // PluginServices.getMDIManager().closeWindow(lastOpenDialog);
        // }
        // lastOpenDialog = dialog;
        // dialog.setPosition(selectedPos);
        // PluginServices.getMDIManager().addWindow(dialog);
        // return true;
        // }
        // }
        // return false;
        return true;
    }

    @Override
    public void pointDoubleClick(PointEvent event) throws BehaviorException {
    }

    public void setLayers(List<FLyrVect> searchableLayers) {
        this.searchableLayers = searchableLayers;
    }

}
