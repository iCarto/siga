package es.icarto.gvsig.extgex.queries;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.icarto.gvsig.commons.queries.QueryFiltersI;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.commons.utils.StrUtils;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class QueryFilters implements QueryFiltersI {

    
    private static final String DEFAULT_FILTER = " - ";

    
    private List<Field> fields;
    private KeyValue tramo;
    private KeyValue uc;
    private KeyValue ayuntamiento;
    private KeyValue parroquia;

    public QueryFilters(KeyValue tramo, KeyValue uc, KeyValue ayuntamiento, KeyValue parroquia) {
        this.tramo = tramo;
        this.uc = uc;
        this.ayuntamiento = ayuntamiento;
        this.parroquia = parroquia;
    }

    @Override
    public Collection<Field> getLocation() {
        // Maybe we will ensure here that setValue is never null and always have the desired default value
        List<Field> location = new ArrayList<Field>();
        Field tramoF = new Field(DBNames.FIELD_TRAMO_FINCAS, "Tramo: ");
        tramoF.setValue(ensureNotNullValue(tramo));
        tramoF.addForeignKey(tramo.getKey());
        Field ucF = new Field(DBNames.FIELD_UC_FINCAS, "UC: ");
        ucF.setValue(ensureNotNullValue(uc));
        ucF.addForeignKey(uc.getKey());
        Field ayuntamientoF = new Field(DBNames.FIELD_AYUNTAMIENTO_FINCAS, "Ayuntamiento: ");
        ayuntamientoF.setValue(ensureNotNullValue(ayuntamiento));
        ayuntamientoF.addForeignKey(ayuntamiento.getKey());
        Field parroquiaF = new Field(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS, "Parroquia/Subtramo: ");
        parroquiaF.setValue(ensureNotNullValue(parroquia));
        parroquiaF.addForeignKey(parroquia.getKey());

        location.add(tramoF);
        location.add(ucF);
        location.add(ayuntamientoF);
        location.add(parroquiaF);
        return location;
    }
    
    private String ensureNotNullValue(KeyValue kv) {
        return StrUtils.isEmptyString(kv.getValue()) ? DEFAULT_FILTER : kv.getValue();
    }
        

    @Override
    public List<Field> getFields() {
	return fields;
    }

    public void setQueryType(String string) {
    }

    public void setFields(List<Field> fields) {
	this.fields = fields;
    }
    
    public String getWhereClauseByLocationWidgets(boolean hasWhere) throws SQLException {
        String whereC;
        if (!hasWhere) {
            whereC = " WHERE ";
        } else {
            whereC = " AND ";
        }
        
        for (Field f : getLocation()) {
            if (! f.getValue().toString().equalsIgnoreCase(DEFAULT_FILTER)) {
                whereC = whereC + f.getKey() + " = '" + f.getForeignKeys().get(0) + "' AND ";                
            }
        }
        
        if (whereC.equalsIgnoreCase(" WHERE ")) {
            whereC = " "; // has no combobox selected
        }
        if (whereC.endsWith(" AND ")) {
            whereC = whereC.substring(0, whereC.length() - " AND ".length());
        }
         
        return whereC;
    }

    @Override
    public boolean getSeleccionados() {
    // TODO Auto-generated method stub
    return false;
    }

}
