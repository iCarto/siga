package org.gvsig.mapsheets.print.series.gui.utils;

import com.iver.cit.gvsig.project.documents.view.ProjectView;

/**
 * Utility class to keep info about a view in a combo box item
 * 
 * @author jldominguez
 *
 */
public class ProjectViewComboItem {

	private ProjectView pv = null;
	
	public ProjectViewComboItem(ProjectView _pv) {
		pv = _pv;
	}
	
	public String toString() {
		if (pv != null) {
			return pv.getName();
		} else {
			return "-";
		}
	}
	
	public ProjectView getProjectView() {
		return pv;
	}
	
}
