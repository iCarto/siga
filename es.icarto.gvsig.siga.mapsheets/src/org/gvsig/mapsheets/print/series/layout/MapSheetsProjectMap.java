package org.gvsig.mapsheets.print.series.layout;

import org.gvsig.mapsheets.print.series.utils.IMapSheetsIdentified;

import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.ProjectDocumentFactory;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.ProjectMap;
import com.iver.cit.gvsig.project.documents.layout.ProjectMapFactory;
import com.iver.utiles.XMLEntity;

/**
 * This class is the gvsig document which holds the layout template as a field.
 * 
 * @author jldominguez
 *
 */
public class MapSheetsProjectMap extends ProjectMap implements IMapSheetsIdentified {
	
	
	public MapSheetsProjectMap() {
		setId(System.currentTimeMillis());
	}
	

	public void setXMLEntity(XMLEntity xml) throws OpenException {
		try {
			// super.setXMLEntity(xml);
			int numMaps=xml.getIntProperty("numMaps");
			ProjectDocument.NUMS.put(ProjectMapFactory.registerName,new Integer(numMaps));
			
			XMLEntity child = xml.getChild(0);
			MapSheetsLayoutTemplate mslt = new MapSheetsLayoutTemplate();
			mslt.setXMLEntity(child,getProject());
			setModel(mslt);
			setName(xml.getStringProperty("name"));

			
			/*
			for (int i=0; i<xml.getChildrenCount(); i++)
			{
				if (child.contains("className")
						&& (child.getStringProperty("className").equals("org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate")
								|| child.getStringProperty("className").equals(MapSheetsLayoutTemplate.class.getName()))
								) {
					
					MapSheetsLayoutTemplate mslt = new MapSheetsLayoutTemplate();
					mslt.setXMLEntity(child,getProject());
					setModel(mslt);
				}
			}
			*/
			this.getModel().setProjectMap(this);
		} catch (Exception e) {
			throw new OpenException(e,this.getClass().getName());
		}
	}
	
	
	public XMLEntity getXMLEntity() throws SaveException   {
		
		XMLEntity xml = new XMLEntity();
		try{
			xml.putProperty("name", getName());
			xml.putProperty("className", getProjectDocumentFactory().getRegisterName());
			int numMaps=((Integer)ProjectDocument.NUMS.get(MapSheetsProjectMapFactory.regName)).intValue();
			xml.putProperty("numMaps", numMaps);
			xml.addChild(getModel().getXMLEntity());
		} catch (Exception e) {
			throw new SaveException(e,this.getClass().getName());
		}
		return xml;
	}

	/**
	 * helps in persistency 
	 */
	private long msid = -1;
	public long getId() {
		return msid;
	}

	public void setId(long id) {
		msid = id;
		try { Thread.sleep(40); } catch (Exception e) {}
	}
	
	public ProjectDocumentFactory getProjectDocumentFactory() {
		return MapSheetsProjectMapFactory.instance;
	}
	
	

}
