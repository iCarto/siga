package org.gvsig.mapsheets.print.series.test;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.iver.cit.gvsig.fmap.layers.LayerFactory;

public class AllTests {
	
	static final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
	
	public static File TEST_DATA_FOLDER_URL = new File("." + File.separator + "pdftest-data");

	private static void setup() {
		File baseDriversPath = new File(fwAndamiDriverPath);
		LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
	}
	
	public static Test suite() {

		setup();
		
		TestSuite ts = new TestSuite();
		ts.addTestSuite(GridTest.class);
		ts.addTestSuite(LayoutTest.class);
		ts.addTestSuite(PrintTest.class);
		ts.addTestSuite(PdfTest.class);
		// ts.addTestSuite(PersistenceTest.class);
		return ts;
	}

	public static void waitSome() {
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
	}
	
}
