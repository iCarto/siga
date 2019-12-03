package es.icarto.gvsig.extgex.wms;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Hashtable;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrWMS;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.elle.db.DBStructure;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LoadWMS {

    private final String wmsName;
    private String wmsHost = null;
    
    public LoadWMS(String wmsName) {
    this.wmsName = wmsName;
    }
    
    public LoadWMS(String wmsName, String wmsHost) {
    this.wmsName =wmsName;
    this.wmsHost = wmsHost;
    }
    
    public void Load() {

	DBSession dbs = DBSession.getCurrentSession();
	String whereC = DBNames.FIELD_LAYER_WMS + "=" + "'" + wmsName + "'";
	try {
	    // columns in order from wms table: id_wms[0], layer_name[1],
	    // layer[2], srs[3], format[4], host[5]
	    String[][] wmsValues = null;
	    wmsValues = dbs.getTable(DBNames.TABLE_WMS, DBStructure.getSchema(), whereC);

	    String name = wmsValues[0][1];
	    String sLayer = wmsValues[0][2];
	    String srs = wmsValues[0][3];
	    String format = wmsValues[0][4];
	    if (wmsHost == null) {
	    this.wmsHost = wmsValues[0][5];
	    }
	    
	    URL url = new URL(wmsHost);
	    FLyrWMS layer = new FLyrWMS();

	    layer.setHost(url);
	    layer.setFullExtent(new Rectangle2D.Float(430819, 4623297, 286459, 232149));
	    layer.setFormat(format);
	    layer.setLayerQuery(sLayer);
	    layer.setInfoLayerQuery(sLayer);
	    layer.setSRS(srs);
	    ((FLayer) layer).setName(name);
	    layer.setWmsTransparency(true);

	    // "gvSIG Raster Driver";
	    //	    FMapWMSDriver driver;
	    //	    driver = FMapWMSDriverFactory.getFMapDriverForURL(url);
	    //	    if (driver != null) {
	    //	    	layer.setDriver(driver);
	    //	    }

	    Hashtable online_resources = new Hashtable();
	    online_resources.put("GetFeatureInfo", this.wmsHost);
	    online_resources.put("GetMap", this.wmsHost);                         
	    layer.setOnlineResources(online_resources);
	    layer.setFixedSize(new Dimension(-1, -1));
	    layer.setQueryable(false);
	    ((FLayer) layer).setVisible(true);

	    View v = (View) PluginServices.getMDIManager().getActiveWindow();
	    MapContext mapContext = v.getModel().getMapContext();

	    mapContext.getLayers().addLayer(0, layer);

	} catch (SQLException e) {
	    e.printStackTrace();
	    return;
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return;
	} catch (IOException e) {
	    e.printStackTrace();
	    return;
	}
    }
}
