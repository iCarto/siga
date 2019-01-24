package org.gvsig.mapsheets.print.series.test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.gvsig.mapsheets.print.series.MapSheetsCreationExtension;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;

public class GridTest extends TestCase {
	
	public static String shpname = "japan_4326.shp";
	
	public void setUp() {
		
	}
	
	public void test() {
		
		File shp = new File(
				AllTests.TEST_DATA_FOLDER_URL.getAbsolutePath() +
				File.separator + shpname);
		
		FLyrVect lyr = null;
		
		try {
			lyr = (FLyrVect) LayerFactory.createLayer(
					"Japan EPSG:4326",
					new IndexedShpDriver().getName(),
					shp,
					CRSFactory.getCRS("EPSG:4326"));
		} catch (Exception ex) {
			fail("While creating shp layer. " + ex.getMessage());
		}
		
		ArrayList[] igs_codes = null;
		
		try {
			igs_codes = MapSheetsUtils.createFrames(
					false,
					false,
					new Rectangle2D.Double(0,0,18,17),
					null, 1600000, 10, lyr.getProjection(), lyr);
		} catch (Exception ex) {
			fail("While creating grid geometries. " + ex.getMessage());
		}
		
		MapSheetGrid newgrid = null;
		
		try {
			newgrid = MapSheetGrid.createMapSheetGrid(
					MapSheetGrid.createNewName(),
					lyr.getProjection(),
					MapSheetGrid.createDefaultLyrDesc());
		} catch (Exception ex) {
			fail("While init of grid. " + ex.getMessage());
		}
		
		ArrayList igs = igs_codes[0];
		ArrayList cods = igs_codes[1];
		HashMap atts_hm = null;
		
		int sz = igs.size();
		for (int i=0; i<sz; i++) {
			atts_hm = new HashMap();
			
			atts_hm.put(MapSheetGrid.ATT_NAME_CODE,
					ValueFactory.createValue((String) cods.get(i)));
			atts_hm.put(MapSheetGrid.ATT_NAME_ROT_RAD,
					ValueFactory.createValue(new Double(0)));
			atts_hm.put(MapSheetGrid.ATT_NAME_OVERLAP,
					ValueFactory.createValue(new Double(10)));
			atts_hm.put(MapSheetGrid.ATT_NAME_SCALE,
					ValueFactory.createValue(new Double(1600000)));
			atts_hm.put(MapSheetGrid.ATT_NAME_DIMX_CM,
					ValueFactory.createValue(new Double(18)));
			atts_hm.put(MapSheetGrid.ATT_NAME_DIMY_CM,
					ValueFactory.createValue(new Double(17)));
			
			try {
				newgrid.addSheet(
						(IGeometry) igs.get(i),
						atts_hm);
			} catch (Exception ex) {
				fail("While adding sheet. " + ex.getMessage());
			}
		}
		
		// select E-6;
		try {
			select(newgrid, "E" + MapSheetsCreationExtension.CODE_ID_SEPARATOR + "6");
		} catch (Exception ex) {
			fail("While selecting E_6. " + ex.getMessage());
		}
		
		LayoutTest.grid = newgrid;
		try {
			lyr.setLegend(getTestLegend());
		} catch (Exception ex) {
			fail("While setting legend. " + ex.getMessage());
		}
		
		LayoutTest.layerv = lyr;
	}

	private IVectorLegend getTestLegend() {
		
		SimpleFillSymbol testSym = (SimpleFillSymbol) SymbologyFactory.createDefaultFillSymbol();
		
		testSym.setHasFill(true);
		testSym.setFillColor(new Color(0,255,0,153));
		testSym.getOutline().setLineColor(new Color(255,0,255));
		
		if (true) {
			testSym.getOutline().setLineWidth(25);
			int mpos = MapContext.getDistancePosition("Kilometros");
			testSym.setUnit(mpos);
			testSym.setReferenceSystem(CartographicSupport.WORLD);
		}

		SingleSymbolLegend resp = new SingleSymbolLegend(testSym);
		return resp;
	}

	private void select(MapSheetGrid gr, String code) throws Exception {
		
		ArrayList cods = gr.getTheMemoryDriver().getCodes();
		int len = cods.size();
		String item = null;
		for (int i=0; i<len; i++) {
			item = (String) cods.get(i);
			if (item.compareToIgnoreCase(code) == 0) {
				select(gr,i);
				return;
			}
		}
		throw new Exception("Code not found: " + code);
	}

	private void select(MapSheetGrid gr, int i) {
		FBitSet fbs = new FBitSet();
		fbs.set(i);
		gr.getSelectionSupport().setSelection(fbs);
	}

}
