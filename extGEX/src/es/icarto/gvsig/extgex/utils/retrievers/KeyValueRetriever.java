package es.icarto.gvsig.extgex.utils.retrievers;

import java.util.ArrayList;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.parser.ParseException;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class KeyValueRetriever {

    private FLyrVect layer = null;
    private String alias = null;
    private String value = null;
    private ArrayList<KeyValue> foreignKeys = null;
    private String orderByField = null;
    
    protected KeyValueRetriever() {
        // Don't use this. Just a hack for SlopesAndCurvesKeyValueRetriever
    }

    public KeyValueRetriever(FLyrVect layer,
	    String alias,
	    String value) {

	this(layer, alias, value, null);
    }

    public KeyValueRetriever(FLyrVect layer,
	    String alias,
	    String value,
	    ArrayList<KeyValue> foreignKeys) {

	this.layer = layer;
	this.alias = alias;
	this.value = value;
	this.foreignKeys = foreignKeys;
    }

    public void setOrderBy(String field) {
	this.orderByField = field;
    }

    public ArrayList<KeyValue> getValues() {
	ArrayList<KeyValue> values = new ArrayList<KeyValue>();
	try {
	    SelectableDataSource sds = getFilteredRecordset();
	    int indexOfAlias = sds.getFieldIndexByName(alias);
	    int indexOfValue = sds.getFieldIndexByName(value);
	    for(int i=0; i<sds.getRowCount(); i++) {
		//TODO: retrieve value correctly, not from toString method
		KeyValue kv = new KeyValue(
			sds.getFieldValue(i, indexOfAlias).toString(),
			sds.getFieldValue(i, indexOfValue).toString());
		values.add(kv);
	    }
	    return values;
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return values;

	}
    }

    private SelectableDataSource getFilteredRecordset()
	    throws ReadDriverException {
	if((foreignKeys != null)) {
	    String sqlQuery = "select * from " + layer.getRecordset().getName() +
		    buildSQLWhereSentence() + ";";
	    return doFilterRecordset(sqlQuery);
	} else if (orderByField != null) {
	    String sqlQuery = "select * from " + layer.getRecordset().getName() +
		    " where 1 = 1 order by " + orderByField + ";";
	    return doFilterRecordset(sqlQuery);
	} else {
	    return layer.getSource().getRecordset();
	}
    }

    private SelectableDataSource doFilterRecordset(String sqlQuery) {
	DataSourceFactory dsf;
	try {
	    dsf = layer.getRecordset().getDataSourceFactory();
	    System.out.println("SQLQUERY= " + sqlQuery);
	    DataSource ds = dsf.executeSQL(sqlQuery, EditionEvent.ALPHANUMERIC);
	    ds.setDataSourceFactory(dsf);
	    SelectableDataSource sds = new SelectableDataSource(ds);
	    EditableAdapter ea = new EditableAdapter();
	    ea.setOriginalDataSource(sds);
	    return ea.getRecordset();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return null;
	} catch (DriverLoadException e) {
	    e.printStackTrace();
	    return null;
	} catch (ParseException e) {
	    e.printStackTrace();
	    return null;
	} catch (SemanticException e) {
	    e.printStackTrace();
	    return null;
	} catch (EvaluationException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private String buildSQLWhereSentence() {
	String where = " where ";
	ArrayList<String> whereValues = new ArrayList<String>();
	for (KeyValue fk : foreignKeys) {
	    //TODO: build the SQL depending on the type of field
	    whereValues.add(fk.getKey() + " = " + fk.getValue() + ""); //numeric values
	    //whereValues.add(fk.getKey() + " = '" + fk.getValue() + "'");//string values
	}
	for (int i=0; i<whereValues.size()-1; i++) {
	    where = where + whereValues.get(i) + " and ";
	}
	where = where + whereValues.get(whereValues.size()-1);
	if(orderByField != null) {
	    where = where + " order by " + orderByField;
	}
	return where;
    }
}
