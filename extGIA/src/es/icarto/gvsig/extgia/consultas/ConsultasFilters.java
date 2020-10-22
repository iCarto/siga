package es.icarto.gvsig.extgia.consultas;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import es.icarto.gvsig.commons.queries.QueryFiltersI;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;

public class ConsultasFilters<E> implements QueryFiltersI {

    private KeyValue area;
    private KeyValue baseContratista;
    private KeyValue tramo;
    private Date fechaInicio;
    private Date fechaFin;
    private boolean seleccionados;
    private boolean ultimos;
    private ArrayList<String> selectedRecords;

    private final DateFormat dateFormat = DateFormatNT.getDateFormat();
    private String queryType = "";
    private List<E> fields;
    private List<E> orderBy;

    public ConsultasFilters(KeyValue area, KeyValue baseContratista,
	    KeyValue tramo, Date fechaInicio, Date fechaFin, boolean seleccionados, 
	    ArrayList<String> selectedRecords, boolean ultimos) {
	this.area = area;
	this.baseContratista = baseContratista;
	this.tramo = tramo;
	this.fechaInicio = fechaInicio;
	this.fechaFin = fechaFin;
	this.seleccionados = seleccionados;
	this.selectedRecords = selectedRecords;
	this.ultimos = ultimos;
    }

    public KeyValue getArea() {
	return area;
    }

    public void setArea(KeyValue area) {
	this.area = area;
    }

    public KeyValue getBaseContratista() {
	return baseContratista;
    }

    public void setBaseContratista(KeyValue baseContratista) {
	this.baseContratista = baseContratista;
    }

    public KeyValue getTramo() {
	return tramo;
    }

    public void setTramo(KeyValue tramo) {
	this.tramo = tramo;
    }
    
    public boolean getSeleccionados() {
    return this.seleccionados;
    }
    
    public boolean getUltimos() {
    return this.ultimos;
    }

    public Date getFechaInicio() {
	return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
	this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
	return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
	this.fechaFin = fechaFin;
    }

    public String getWhereClauseFiltersForAgregados() {
	
    	String query = " ";
	if (area != null) {
	    query += " AND el.area_mantenimiento =  '" + area.getKey() + "'";
	}
	if (baseContratista != null) {
	    query += " AND el.base_contratista =  '" + baseContratista.getKey() + "'";
	}
	if (tramo != null) {
	    query += " AND el.tramo =  '" + tramo.getKey() + "'";
	}

	if (fechaInicio != null && fechaFin != null) {
	    query += " AND sub.fecha BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "'";
	}

	return query;
    }

    public String getWhereClauseByLocationWidgets() {
	String query = "";
	if (this.seleccionados) {
	    return query;
	}
	if (area != null) {
	    query = " WHERE area_mantenimiento =  '" + area.getKey() + "'";
	}
	if (baseContratista != null) {
	    if (!query.isEmpty()) {
		query = query + " AND base_contratista =  '"
			+ baseContratista.getKey() + "'";
	    } else {
		query = " WHERE base_contratista =  '"
			+ baseContratista.getKey() + "'";
	    }
	}
	if (tramo != null) {
	    if (!query.isEmpty()) {
		query = query + " AND tramo =  '" + tramo.getKey() + "'";
	    } else {
		query = " WHERE tramo =  '" + tramo.getKey() + "'";
	    }
	}
	if (!this.seleccionados && this.ultimos) {
	    return query + ")";
	}
	return query;
    }

    public String getWhereClauseByDates(String dateField) {
	String query = "";
	if (this.seleccionados) {
	    query = " AND " + dateField + " BETWEEN '" + fechaInicio
	            + "' AND '" + fechaFin + "'";
	} else if (!getWhereClauseByLocationWidgets().isEmpty()) {
	    query = " ) AND " + dateField + " BETWEEN '" + fechaInicio
		    + "' AND '" + fechaFin + "'";
	} else {
	    query = " WHERE " + dateField + " BETWEEN '" + fechaInicio
		    + "' AND '" + fechaFin + "'";
	}
	return query;
    }
    
    public String getWhereClauseBySelectedRecordsOnly(String prefix, String idField) {
    String query = "";
    if (!this.seleccionados) {
        return query;
    }
    if (this.ultimos) {
        query = " AND ";
    } else {
        query = "WHERE ";
    }
    query = query + prefix + "." + idField + " IN (";
    for (int i = 0; i < this.selectedRecords.size(); i++) {
        query = query + "'" + this.selectedRecords.get(i) + "', ";
    }
    query = query.substring(0, query.length() - 2);
    if (this.seleccionados && !this.ultimos) {
        return query + ")";
    }
    //return query + ")";
    return query;
    }

    public String getFechaInicioFormatted() {
	return dateFormat.format(fechaInicio);
    }

    public String getFechaFinFormatted() {
	return dateFormat.format(fechaFin);
    }

    public void setQueryType(String queryType) {
	this.queryType = queryType;
    }

    public String getQueryType() {
	return this.queryType;
    }

    public void setFields(List<E> fields) {
	this.fields = fields;
    }

    @Override
    public List<Field> getFields() {
	return (List<Field>) this.fields;
    }

    public void setOrderBy(List<E> orderBy) {
	this.orderBy = orderBy;
    }

    public List<E> getOrderBy() {
	return this.orderBy;
    }

    @Override
    public Collection<Field> getLocation() {
	List<Field> location = new ArrayList<Field>();
	Field areaMantenimientoField = new Field("", "Área Mantenimiento");
	areaMantenimientoField.setValue(area == null ? "" : area.getValue());
	Field baseContratistaField = new Field("", "Base contratista");
	baseContratistaField.setValue(baseContratista == null ? ""
		: baseContratista.getValue());
	Field tramoField = new Field("", "Tramo");
	tramoField.setValue(tramo == null ? "" : tramo.getValue());

	location.add(areaMantenimientoField);
	location.add(baseContratistaField);
	location.add(tramoField);
	return location;
    }
}
