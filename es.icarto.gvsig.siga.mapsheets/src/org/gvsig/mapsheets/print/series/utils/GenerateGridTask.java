package org.gvsig.mapsheets.print.series.utils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cresques.cts.IProjection;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.utils.IProgressListener;

import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * This task prints map sheets as PDF.
 * +
 * @author jldominguez
 *
 */
public class GenerateGridTask implements Runnable, Cancellable {

	private IProgressListener proListener;
    private boolean cover_view_selected;
    private boolean selected_only;
    private Rectangle2D useful_map_cm;
    private ViewPort vp;
    private long scale;
    private int overlap_pc;
    private double w;
    private double h;
    private IProjection iproj;
    private FLyrVect ler;
    private View v;
    private IWindow caller;
    private boolean isCancel = false;

	public GenerateGridTask(
boolean cover_view_selected, boolean selected_only,
            Rectangle2D useful_map_cm, ViewPort vp, long scale, int overlap_pc,
            IProjection iproj, FLyrVect ler, double w, double h, View v,
            IWindow caller, IProgressListener proListener) {
        this.cover_view_selected = cover_view_selected;
        this.selected_only = selected_only;
        this.useful_map_cm = useful_map_cm;
        this.vp = vp;
        this.scale = scale;
        this.overlap_pc = overlap_pc;
        this.iproj = iproj;
        this.ler = ler;
        this.v = v;
        this.w = w;
        this.h = h;
        this.caller = caller;
        this.proListener = proListener;
	}

	public static boolean WORKING = false;
	public void run() {

		WORKING = true;

		if (proListener != null) {
			proListener.started();
		}

        try {
            ArrayList[] igs_codes = MapSheetsUtils.createFrames(
                    cover_view_selected, selected_only,
                    useful_map_cm, vp, scale, overlap_pc, iproj, ler,
                    proListener, this);

            ArrayList igs = igs_codes[0];
            ArrayList cods = igs_codes[1];
            HashMap atts_hm = null;

            MapSheetGrid newgrid = MapSheetGrid.createMapSheetGrid(
                    MapSheetGrid.createNewName(), v.getProjection(),
                    MapSheetGrid.createDefaultLyrDesc());

            int sz = igs.size();
            for (int i = 0; i < sz; i++) {
                atts_hm = new HashMap();

                atts_hm.put(MapSheetGrid.ATT_NAME_CODE,
                        ValueFactory.createValue((String) cods.get(i)));
                atts_hm.put(MapSheetGrid.ATT_NAME_ROT_RAD,
                        ValueFactory.createValue(new Double(0)));
                atts_hm.put(MapSheetGrid.ATT_NAME_OVERLAP,
                        ValueFactory.createValue(new Double(overlap_pc)));
                atts_hm.put(MapSheetGrid.ATT_NAME_SCALE,
                        ValueFactory.createValue(new Double(scale)));
                atts_hm.put(MapSheetGrid.ATT_NAME_DIMX_CM,
                        ValueFactory.createValue(new Double(w)));
                atts_hm.put(MapSheetGrid.ATT_NAME_DIMY_CM,
                        ValueFactory.createValue(new Double(h)));

                newgrid.addSheet((IGeometry) igs.get(i), atts_hm);
            }

            // before adding the new layer, delete all MapSheetGrids in
            // TOC
            FLayers layersInTOC = v.getMapControl().getMapContext()
                    .getLayers();
            for (int i = 0; i < layersInTOC.getLayersCount(); i++) {
                if (layersInTOC.getLayer(i) instanceof MapSheetGrid) {
                    v.getMapControl().getMapContext().getLayers()
                            .removeLayer(layersInTOC.getLayer(i));
                }
            }
            v.getMapControl().getMapContext().getLayers()
                    .addLayer(newgrid);
            MapSheetsUtils.setOnlyActive(newgrid, v.getMapControl()
                    .getMapContext().getLayers());

            PluginServices.getMDIManager().closeWindow(caller);
        } catch (Exception e) {
            JOptionPane.showMessageDialog((JPanel) caller, e.getMessage(),
                    PluginServices.getText(this, "Error"),
                    JOptionPane.ERROR_MESSAGE);
        }

		WORKING = false;

		if (proListener != null) {
			proListener.finished();
		}
	}

	public boolean isCanceled() {
		return isCancel;
	}

	public void setCanceled(boolean canceled) {

		if (canceled) {
			WORKING = false;
		}
		isCancel = canceled;

		if (proListener != null) {
			proListener.cancelled(PluginServices.getText(this, "Cancelled_by_user"));
		}

	}

}
