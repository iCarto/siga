package org.gvsig.mapsheets.print.series.layout;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.layout.ProjectMapFactory;

/**
 * Every gvsig document needs to have an associated factory. This is the 
 * factory for {@link MapSheetsProjectMap}
 * 
 * @author jldominguez
 *
 */
public class MapSheetsProjectMapFactory extends ProjectMapFactory {
	
	public static String regName = "MapSheetsProjectMap";
	
    public String getRegisterName() {
        return regName;
    }
    
    public ProjectDocument create(Project project) {
    	String mapName = "";
    	String aux = "Map Sheets map";
    	int numMaps=((Integer)ProjectDocument.NUMS.get(regName)).intValue();
    	mapName = aux + " - " + numMaps++;
    	if (project != null) {
            while (existName(project, mapName)) {
                mapName = aux + " - " + numMaps++;
            }
        }
    	ProjectDocument.NUMS.put(regName,new Integer(numMaps));
        MapSheetsProjectMap map = new MapSheetsProjectMap();
        map.setName(mapName);
        // map.setModel(new Layout());
        // map.getModel().setProjectMap(map);
        map.setProject(project, 0);
        map.setProjectDocumentFactory(this);

        return map;
    }
    
    public static void register() {
    	register(
    			regName,
    			new MapSheetsProjectMapFactory(),
    			"org.gvsig.mapsheets.print.series.layout.MapSheetsProjectMap");
    	
    	// add icons to theme
    	PluginServices.getIconTheme().register(
    			"document-mapsh-icon",
    			MapSheetsProjectMapFactory.class.getClassLoader().getResource(
    					"images" + File.separator + "document-mapsh-icon.png"));
    	PluginServices.getIconTheme().register(
    			"document-mapsh-icon-sel",
    			MapSheetsProjectMapFactory.class.getClassLoader().getResource(
    					"images" + File.separator + "document-mapsh-icon-sel.png"));
    }
    
    public static MapSheetsProjectMapFactory instance = 
    	new MapSheetsProjectMapFactory();
    
    public ProjectDocument createFromGUI(Project project) {
    	
    	JOptionPane.showMessageDialog(
    			null,
    			PluginServices.getText(this, "You_cannot_create_a_layout_here"),
    			PluginServices.getText(this, "Create_layout_template"),
    			JOptionPane.WARNING_MESSAGE);
        return null;
    }
    
    public String getNameType() {
        return PluginServices.getText(this, "Mapa") + " (Map Sheets)";
    }
    
    
    public ImageIcon getButtonIcon() {
      return PluginServices.getIconTheme().get("document-mapsh-icon");
  }

  public ImageIcon getSelectedButtonIcon() {
      return PluginServices.getIconTheme().get("document-mapsh-icon-sel");
  }


}
