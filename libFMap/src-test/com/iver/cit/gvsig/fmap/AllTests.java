package com.iver.cit.gvsig.fmap;

import java.io.File;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.core.TestCartographicSupportForSymbol;
import com.iver.cit.gvsig.fmap.core.rendering.TestILegend;
import com.iver.cit.gvsig.fmap.core.rendering.styling.labeling.TestILabelingMethod;
import com.iver.cit.gvsig.fmap.core.rendering.styling.labeling.TestLabelClass;
import com.iver.cit.gvsig.fmap.core.symbols.TestISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.TestMultiLayerSymbol;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.dbf.TestDbf;
import com.iver.cit.gvsig.fmap.drivers.dgn.TestDgn;
import com.iver.cit.gvsig.fmap.featureiterators.DBFeatureIteratorTest;
import com.iver.cit.gvsig.fmap.featureiterators.FeatureIteratorTest;
import com.iver.cit.gvsig.fmap.featureiterators.PerformanceFeatureIteratorTest;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.LayersIteratorTest;
import com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexTest;
import com.iver.cit.gvsig.fmap.tools.AreaListenerTest;
import com.vividsolutions.jts.operation.overlay.SnappingOverlayOperationTest;

public class AllTests extends TestCase{
	/**
	 * The EPSG:4326 projection
	 */
	public static IProjection TEST_DEFAULT_PROJECTION =
		CRSFactory.getCRS("EPSG:4326");
	
	/**
	 * The EPSG:23030 projection
	 */
	public static IProjection TEST_DEFAULT_MERCATOR_PROJECTION =
		CRSFactory.getCRS("EPSG:23030");
	
	/**
	 * The EPSG:23029 projection
	 */
	public static IProjection test_newProjection =
		CRSFactory.getCRS("EPSG:23029");

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.iver.cit.gvsig.fmap");
		//$JUnit-BEGIN$

		/* LayersIterator */
			suite.addTestSuite(LayersIteratorTest.class);


		// NOTE (jaume): order is important for incremental testing
		/* Symbols subsystem (jaume) */
			// integration tests
				// ISymbol
				suite.addTest(TestISymbol.suite());
				suite.addTestSuite(TestMultiLayerSymbol.class);

		/* CartographicSupport subsystem (jaume) */
			// integration tests for symbols 
			suite.addTestSuite(TestCartographicSupportForSymbol.class);
		
		/* Legend subsystem (jaume) */
			// integration tests
				suite.addTest(TestILegend.suite());
				
		/* Labeling subsystem (jaume) */
			// unit tests
				suite.addTestSuite(TestLabelClass.class);
				
			// integration tests
				suite.addTest(TestILabelingMethod.suite());
		/*
		 * Feature iterators
		 * */
		suite.addTestSuite(FeatureIteratorTest.class);
		
		/*
		 * Other Tests present in FMap (cesar)
		 * Remove them from here and the src-test dir if they are not
		 * useful anymore.
		 */

		suite.addTestSuite(AreaListenerTest.class);
		suite.addTestSuite(DBFeatureIteratorTest.class);
		suite.addTestSuite(PerformanceFeatureIteratorTest.class);
		suite.addTestSuite(SnappingOverlayOperationTest.class);
		suite.addTestSuite(SpatialIndexTest.class);
		suite.addTestSuite(TestDbf.class);
		suite.addTestSuite(TestDgn.class);
		

		//$JUnit-END$
		return suite;
	}

//// jaume
//// PASTED FROM FeatureIteratorTest.java to be globally accessible
	public static final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
	private static File baseDataPath;
	private static File baseDriversPath;


	public static void setUpDrivers() {
		try {
			URL url = AllTests.class.getResource("testdata");
			if (url == null)
				throw new Exception("No se encuentra el directorio con datos de prueba");

			baseDataPath = new File(url.getFile());
			if (!baseDataPath.exists())
				throw new Exception("No se encuentra el directorio con datos de prueba");

			baseDriversPath = new File(fwAndamiDriverPath);
			if (!baseDriversPath.exists())
				throw new Exception("Can't find drivers path: " + fwAndamiDriverPath);

			LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
			if (LayerFactory.getDM().getDriverNames().length < 1)
				throw new Exception("Can't find drivers in path: " + fwAndamiDriverPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static FLayer newLayer(String fileName,
			String driverName)
	throws LoadLayerException {
		File file = new File(baseDataPath, fileName);
		return LayerFactory.createLayer(fileName,
				driverName,
				file, TEST_DEFAULT_MERCATOR_PROJECTION);
	}

	public static MapContext newMapContext(IProjection projection) {
		ViewPort vp = new ViewPort(projection);
		return new MapContext(vp);
	}

//// end past
}