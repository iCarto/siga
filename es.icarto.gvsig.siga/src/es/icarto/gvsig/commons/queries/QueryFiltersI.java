package es.icarto.gvsig.commons.queries;

import java.util.Collection;
import java.util.List;

import es.icarto.gvsig.commons.utils.Field;

public interface QueryFiltersI {

    public Collection<Field> getLocation();

    public List<Field> getFields();
    
    public boolean getSeleccionados();

}
