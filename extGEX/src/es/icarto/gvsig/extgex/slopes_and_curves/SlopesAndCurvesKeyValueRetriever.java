package es.icarto.gvsig.extgex.slopes_and_curves;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SlopesAndCurvesKeyValueRetriever extends KeyValueRetriever {
    private static final Logger logger = Logger.getLogger(SlopesAndCurvesKeyValueRetriever.class);

    private final String fieldname;
    private String orderByField = null;

    public SlopesAndCurvesKeyValueRetriever(String fieldname) {
        this.fieldname = fieldname;
    }

    public SlopesAndCurvesKeyValueRetriever(String alias, String value) {
        throw new AssertionError("Not implemented");

    }

    public SlopesAndCurvesKeyValueRetriever(String alias, String value, ArrayList<KeyValue> foreignKeys) {
        throw new AssertionError("Not implemented");
    }

    @Override
    public void setOrderBy(String field) {
        this.orderByField = field;
        if (!field.equals(fieldname)) {
            throw new AssertionError("Not supported");
        }
    }

    @Override
    public ArrayList<KeyValue> getValues() {
        boolean sorted = this.orderByField != null;
        ArrayList<KeyValue> values = new ArrayList<KeyValue>();

        try {
            String[] distinctValues = DBSession.getCurrentSession().getDistinctValues("slopes_and_curveS",
                    "audasa_aplicaciones", fieldname, sorted, true, null);
            for (String v : distinctValues) {
                values.add(new KeyValue(v, v));
            }
        } catch (SQLException e1) {
            logger.error(e1.getStackTrace(), e1);
        }
        return values;
    }

}
