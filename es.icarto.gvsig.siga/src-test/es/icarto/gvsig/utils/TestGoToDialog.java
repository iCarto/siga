package es.icarto.gvsig.utils;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.DBException;

import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.icarto.gvsig.siga.locatorbycoords.LocatorByCoordsDialog;
import es.icarto.gvsig.siga.locatorbycoords.LocatorByCoordsModel;
import es.udc.cartolab.gvsig.testutils.MapControlStub;
import es.udc.cartolab.gvsig.users.utils.DBSessionPostGIS;

public class TestGoToDialog {

    private static final Logger logger = Logger.getLogger(TestGoToDialog.class);

    @BeforeClass
    public static void doSetupBeforeClass() {
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
	try {
	    DBSessionPostGIS.createConnection("localhost", 5432, "audasa_test",
		    null, "postgres", "postgres");
	} catch (DBException e) {
	    e.printStackTrace();
	}

    }

    public static void main(String[] args) {

	// JPanel pane = new JPanel(new MigLayout("insets 10"));

	final String VIEW_PROJ = "EPSG:23029";

	doSetupBeforeClass();

	LocatorByCoordsModel model = new LocatorByCoordsModel();
	model.setDefaultInputProj(model.getProjCodes().get(0));
	model.setDefaultOuputProj(model.getProjCodes().get(1));

	MapContext mapContext = new MapControlStub().getMapContext();
	Rectangle2D fullExtent = null;
	// final PathIterator pathIterator = fullExtent.getPathIterator(null);
	// ShapeFactory.createPolygon2D(pathIterator);
	// Envelope convertRectangle2DtoEnvelope =
	// FConverter.convertRectangle2DtoEnvelope(fullExtent);
	// FConverter.

	// try {
	// fullExtent = mapContext.getFullExtent();
	// new GShape(LocatorByCoordsModel.projCodes[0].getProj(), fullExtent);
	// } catch (ReadDriverException e) {
	// logger.error(e.getStackTrace(), e);
	// }

	LocatorByCoordsDialog pane = new LocatorByCoordsDialog(model);

	// WindowInfo windowInfo = pane.getWindowInfo(new Component() {
	// @Override
	// public int getWidth() {
	// return 500;
	// }
	//
	// @Override
	// public int getHeight() {
	// return 500;
	// }
	// });
	// Dimension dim = new Dimension(windowInfo.getWidth(),
	// windowInfo.getHeight());
	// pane.setPreferredSize(dim);
	// pane.setSize(dim);
	// pane.setMinimumSize(dim);

	JFrame f = new JFrame("Test");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.getContentPane().add(pane, BorderLayout.CENTER);
	f.pack();
	f.setVisible(true);
    }

}
