package es.icarto.gvsig.extgex.queries;

/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruña)
 * Copyright (c) 2014 iCarto
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extEIELForms.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.List;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.queries.QueryFiltersI;

@SuppressWarnings("serial")
public class ResultTableModel extends DefaultTableModel {

    private final String code;
    private final String description;
    private final String title;
    private final String subtitle;

    private List<String> tables;
    private QueryFiltersI queryFilters;

    public ResultTableModel(String code, String description, String title,
	    String subtitle) {
	super();
	this.code = code;
	this.description = description;
	this.title = title;
	this.subtitle = subtitle;
    }

    public final String getCode() {
	return code;
    }

    public final String getDescription() {
	return description;
    }

    public final String getTitle() {
	return title;
    }

    public final String getSubtitle() {
	return subtitle;
    }

    public void setQueryTables(List<String> tables) {
	this.tables = tables;
    }

    public String getQueryTables() {
	String t = "";
	if (tables != null && tables.size() > 0) {
	    for (String table : tables) {
		t = t.concat(table).concat(", ");
	    }
	    t = t.substring(0, t.length() - 2).concat(".");
	}
	return t;
    }

    public String getHTML() {

	int columnCount = getColumnCount();

	String html = "<table border=\"1\"><tr>";

	for (int i = 0; i < columnCount; i++) {
	    html = html + "<td>" + "<h4>" + getColumnName(i) + "</h4>"
		    + "</td>";
	}
	html = html + "</tr>";

	for (int row = 0; row < getRowCount(); row++) {
	    html = html + "<tr>";
	    for (int column = 0; column < columnCount; column++) {
		html = html + "<td>" + getValueAt(row, column) + "</td>";
	    }
	    html = html + "</tr>";
	}

	html = html + "</table>";

	return html;
    }

    public void setQueryFilters(QueryFiltersI queryFilters) {
	this.queryFilters = queryFilters;
    }

    public QueryFiltersI getQueryFilters() {
	return queryFilters;
    }
}
