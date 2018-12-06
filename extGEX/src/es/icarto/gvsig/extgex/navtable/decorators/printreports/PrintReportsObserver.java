package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerDrawEvent;
import com.iver.cit.gvsig.fmap.layers.LayerDrawingListener;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;

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
public class PrintReportsObserver implements ActionListener,
	LayerDrawingListener {

    private FLyrVect layer = null;
    private AbstractNavTable dialog = null;

    private File outputFile;

    private MapContext mapContext;

    public PrintReportsObserver(FLyrVect layer, AbstractNavTable dialog) {
	this.layer = layer;
	this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	SaveFileDialog sfd = new SaveFileDialog("PDF files", "pdf");
	sfd.setAskForOverwrite(true);
	outputFile = sfd.showDialog();
	if (outputFile != null) {
	    // center in view
	    mapContext = layer.getMapContext();
	    mapContext.addLayerDrawingListener(this);
	    ViewPort vp = mapContext.getViewPort();
	    Rectangle2D bbox = getBoundingBox();
	    if (bbox.getWidth() < 200) {
		bbox.setFrameFromCenter(bbox.getCenterX(), bbox.getCenterY(),
			bbox.getCenterX() + 100, bbox.getCenterY() + 100);
	    }
	    vp.setExtent(bbox);
	    vp.refreshExtent();
	}
    }

    private Rectangle2D getBoundingBox() {
	long currentPosition = dialog.getPosition();
	try {
	    return layer.getSource()
		    .getShape(Long.valueOf(currentPosition).intValue())
		    .getBounds2D();
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
    public void beforeGraphicLayerDraw(LayerDrawEvent e)
	    throws CancelationException {
	// nothing to do
    }

    @Override
    public void afterLayerGraphicDraw(LayerDrawEvent e)
	    throws CancelationException {
	// nothing to do
	java.net.URL reportPath = PluginServices
		.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
		.getResource("reports/audasa.jasper");
	PrintReportsAction report = new PrintReportsAction();
	PrintReportsData data = new PrintReportsData();
	data.prepareDataSource(layer.getName(), dialog.getPosition());
	report.print(outputFile.getPath(), reportPath.getFile(), data);
	mapContext.removeLayerDrawListener(this);
    }
}
