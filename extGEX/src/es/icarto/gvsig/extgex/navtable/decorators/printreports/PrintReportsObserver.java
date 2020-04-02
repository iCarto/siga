package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerDrawEvent;
import com.iver.cit.gvsig.fmap.layers.LayerDrawingListener;

import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

/**
 * This class manage the report printing by means of this algorithm:
 *
 * #1 get the event of print button and center view in the bounding box of the
 * feature to print (callback actionPerformed())
 *
 * #2 when the mapControl/viewPort is updated (callback afterLayerGraphicDraw())
 * launch the print operation, which will use the image
 */
public class PrintReportsObserver implements ActionListener, LayerDrawingListener {

    private FLyrVect layer = null;
    private AbstractNavTable dialog = null;

    private File outputFile;

    private MapContext mapContext;
    private final String reportName;

    public PrintReportsObserver(FLyrVect layer, AbstractNavTable dialog, String reportName) {
        this.layer = layer;
        this.dialog = dialog;
        this.reportName = reportName;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        SaveFileDialog sfd = new SaveFileDialog("PDF files", "pdf");
        sfd.setAskForOverwrite(true);
        outputFile = sfd.showDialog();
        if (outputFile != null) {
            // center in view
            mapContext = layer.getMapContext();
            Rectangle2D bbox = getBoundingBox();
            mapContext.zoomToExtent(bbox);
            mapContext.addLayerDrawingListener(this);

            // Cuando no hay geometría y la escala ya está en 2000 el listener no salta
            // por lo que debemos activarlo a mano
            if (bbox == null && mapContext.getScaleView() == 2000) {
                afterLayerGraphicDraw(null);
            } else {
                mapContext.setScaleView(2000);
            }
        }
    }

    private Rectangle2D getBoundingBox() {
        long currentPosition = dialog.getPosition();
        int featurePos = Long.valueOf(currentPosition).intValue();
        try {
            IGeometry geom = layer.getSource().getShape(featurePos);
            if (geom == null) {
                return null;
            }
            return geom.getBounds2D();
        } catch (ExpansionFileReadException e) {
            e.printStackTrace();
            return null;
        } catch (ReadDriverException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void beforeLayerDraw(LayerDrawEvent e) throws CancelationException {
        // nothing to do
    }

    @Override
    public void afterLayerDraw(LayerDrawEvent e) throws CancelationException {
        // nothing to do
    }

    @Override
    public void beforeGraphicLayerDraw(LayerDrawEvent e) throws CancelationException {
        // nothing to do
    }

    @Override
    public void afterLayerGraphicDraw(LayerDrawEvent e) throws CancelationException {
        try {
            // nothing to do
            java.net.URL reportPath = PluginServices.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
                    .getResource("reports/" + this.reportName + ".jasper");
            PrintReportsAction report = new PrintReportsAction();
            PrintReportsData data = new PrintReportsData(layer);
            data.prepareDataSource(dialog.getPosition());
            report.print(outputFile.getPath(), reportPath.getFile(), data);
        } finally {
            mapContext.removeLayerDrawListener(this);
        }

    }
}
