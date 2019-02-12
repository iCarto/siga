package org.gvsig.mapsheets.print.series.print;

import java.io.File;

import org.gvsig.mapsheets.print.series.gui.utils.IProgressListener;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * This task prints map sheets as PDF.
 * +
 * @author jldominguez
 *
 */
public class PdfPrintTask implements Runnable, Cancellable {

	// private MapSheetsSettings msu = null;
	private File baseFolder = null;
	private String baseName = null;
	private IProgressListener proListener;

	private boolean isCancel = false;
	private boolean highlight = false;

	private MapSheetsLayoutTemplate layoutTemplate = null;
	private boolean allSheets = false;
	private FLayer backLayer = null;

	public PdfPrintTask(
			MapSheetsLayoutTemplate lay_template,
			boolean all_sheets,
			boolean highlight,
			FLayer back_lyr,
			String out_folder,
			String bname,
			IProgressListener pro_listener) {

		layoutTemplate = lay_template;
		allSheets = all_sheets;
		backLayer = back_lyr;
		baseFolder = new File(out_folder);
		baseName = bname;
		proListener = pro_listener;
		this.highlight = highlight;
	}

	public static boolean WORKING = false;
	public void run() {

		WORKING = true;

		if (proListener != null) {
			proListener.started();
		}

        long t1 = System.currentTimeMillis();

		MapSheetsUtils.createPdfMaps(
				layoutTemplate,
				allSheets,
				highlight,
				backLayer,
				baseFolder,
				baseName,
				proListener,
				this);

        long t2 = System.currentTimeMillis();
        System.err.println("Tiempo de impresión de los PDF:" + (t2 - t1)
                + " ms.");

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
