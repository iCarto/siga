package org.gvsig.mapsheets.print.series.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.TestCase;

import org.cresques.cts.IProjection;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.MapSheetSelectionDialog;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.layout.MapSheetsProjectMap;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.layout.Attributes;
import com.iver.cit.gvsig.project.documents.layout.ProjectMapFactory;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;

public class LayoutTest extends TestCase {
	
	public static MapSheetGrid grid = null;
	public static FLyrVect layerv = null;
	
	public void test() {
		
		AllTests.waitSome();
		
		ProjectView pv = null;
		try {
			// ProjectDocument.NUMS.put(ProjectViewFactory.registerName, 0);
			// ProjectViewFactory pvf = new ProjectViewFactory();
			pv = createView("Japan view", layerv.getProjection());
			// (ProjectView) pvf.create((Project) null);
			// pv = ProjectFactory.createView("Japan view");
			// pv.getMapContext().setProjection(layerv.getProjection());
			pv.getMapContext().getLayers().addLayer(layerv);
			pv.getMapContext().getViewPort().setImageSize(new Dimension(800,600));
			pv.getMapContext().getViewPort().setExtent(layerv.getFullExtent());
		} catch (Exception ex) {
			fail("While creating view. " + ex.getMessage());
		}
		
		MapContext clo_mc = pv.getMapContext();
		MapContext clo_omc = pv.getMapOverViewContext();

		 ProjectView cloned_pv = new ProjectView();
		 
		 cloned_pv.setName("");
		 cloned_pv.setProjectDocumentFactory(new ProjectViewFactory());
		 cloned_pv.setMapContext(clo_mc);
		 cloned_pv.setMapOverViewContext(clo_omc);
		 
		 // FLayers ll = cloned_pv.getMapContext().getLayers();
		 // MapSheetsUtils.setGridsToVisible(ll, false);
		 
		 double wh_ratio = 1;
		 
		 try {
			wh_ratio = MapSheetsUtils.getWHRatio(grid);
		} catch (Exception e) {
			NotificationManager.addError(e);
		}
		 
		
		 MapSheetsUtils.initViewPort(cloned_pv, wh_ratio);

		 MapSheetSelectionDialog dlg = new MapSheetSelectionDialog(clo_mc, null, new Layout());
		 Object[] objarr = dlg.getSelectedAndAuxLayout();
		 Layout auxlayout = (Layout) objarr[1];
		 
		 MapSheetsLayoutTemplate mslt = 
			 new MapSheetsLayoutTemplate(grid, cloned_pv, auxlayout, null);
		 // mslt.setGrid(msg);
		 // ProjectMap _pmap = ProjectFactory.createMap("Sheets layout");
		 
		 MapSheetsProjectMap mspm = new MapSheetsProjectMap();
		 mspm.setName("Layout Template " + MapSheetsLayoutTemplate.nextId());
		 mspm.setProjectDocumentFactory(new ProjectMapFactory());
		 
		 
		 mspm.setModel(mslt);
		 mslt.setProjectMap(mspm);
		 
		 mslt.init(
				 new ArrayList(),
				 new ArrayList(),
				 new ArrayList(),
				 1.5, 1.5,
				 true);		
		 
		 PrintTest.template = mslt;
	}

	private ProjectView createView(String name, IProjection projection) {
    	ProjectView v = new ProjectView();
		MapContext viewMapContext = new MapContext(new ViewPort(projection));
		ViewPort vp = viewMapContext.getViewPort();
		vp.setBackColor(Color.WHITE);
		
		int mpos = MapContext.getDistancePosition("Metros");
		
		vp.setDistanceUnits(mpos);
		vp.setDistanceArea(mpos);
		
		mpos = MapContext.getDistancePosition("Grados");
		vp.setMapUnits(mpos);

		v.setMapContext(viewMapContext);
		v.setMapOverViewContext(new MapContext(null));

		v.setName(name);
		v.setCreationDate(DateFormat.getInstance().format(new Date()));
        return v;
        
	}

	private static Image img = new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);
	private static File icon_f = new File(
			AllTests.TEST_DATA_FOLDER_URL.getAbsolutePath() + File.separator + "grid.png");
	private static URL icon_u = null;
	
	static {
		try {
			icon_u = icon_f.toURL();
		} catch (Exception ex) { }
		 
	}

	private void addIcon(String str) {
		PluginServices.getIconTheme().registerDefault(str, icon_u);
	}
	
	public void setUp() {
		
		Attributes.setDefaultGridGap(100,100);
		
		addIcon("document-map-icon");
		addIcon("document-map-icon");
		addIcon("document-map-icon-sel");
		addIcon("neresize-icon");
		addIcon("eresize-icon");
		addIcon("nresize-icon");
		addIcon("move-icon");
		addIcon("sereresize-icon");
		addIcon("symboltag-icon");
		addIcon("move-icon");
		addIcon("numero-icon");
		addIcon("barra1-icon");
		addIcon("barra2-icon");
		addIcon("barra3-icon");
		addIcon("text-left-icon");
		addIcon("text-center-v-icon");
		addIcon("text-right-icon");
		addIcon("left-rotation-icon");
		addIcon("text-up-icon");
		addIcon("text-center-h-icon");
		addIcon("text-down-icon");
		addIcon("text-distup-icon");
		addIcon("text-distcenterh-icon");
		addIcon("text-distdown-icon");
		addIcon("text-distleft-icon");
		addIcon("text-distcenterv-icon");
		addIcon("text-distright-icon");
		addIcon("text-size-width-icon");
		addIcon("text-size-height-icon");
		addIcon("text-size-other-icon");
		addIcon("text-space-right-icon");
		addIcon("text-inlayout-icon");
		addIcon("rect-select-cursor");
		addIcon("circle-cursor");
		addIcon("line-cursor");
		addIcon("point-cursor");
		addIcon("poligon-cursor");
		addIcon("rectangle-cursor");
		addIcon("crux-cursor");
		addIcon("layout-hand-icon");
		addIcon("zoom-in-cursor");
		addIcon("hand-icon");
		addIcon("zoom-out-cursor");
		addIcon("layout-zoom-in-cursor");
		addIcon("layout-zoom-out-cursor");
		addIcon("right-rotation-icon");
		addIcon("point-select-cursor");
	}
}
