package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.hardcode.gdbms.engine.values.ValueWriter;

/**
 * @author Fernando Gonz�lez Cort�s
 * 
 *         Imported from libGDBMS in order to not change the API, as its
 *         visibility is package by default.
 */
public class ValueWriterImpl implements ValueWriter {

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(long)
     */
    public String getStatementString(long i) {
	return Long.toString(i);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(int, int)
     */
    public String getStatementString(int i, int sqlType) {
	return Integer.toString(i);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(double, int)
     */
    public String getStatementString(double d, int sqlType) {
	return Double.toString(d);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(java.lang.String, int)
     */
    public String getStatementString(String str, int sqlType) {
	return "'" + escapeString(str) + "'";
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(java.sql.Date)
     */
    public String getStatementString(Date d) {
	return "'" + d.toString() + "'";
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(java.sql.Time)
     */
    public String getStatementString(Time t) {
	return "'" + timeFormat.format(t) + "'";
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(java.sql.Timestamp)
     */
    public String getStatementString(Timestamp ts) {
	return "'" + ts.toString() + "'";
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(byte[])
     */
    public String getStatementString(byte[] binary) {
	StringBuffer sb = new StringBuffer("'");
	for (int i = 0; i < binary.length; i++) {
	    int byte_ = binary[i];
	    if (byte_ < 0) {
		byte_ = byte_ + 256;
	    }
	    String b = Integer.toHexString(byte_);
	    if (b.length() == 1) {
		sb.append("0").append(b);
	    } else {
		sb.append(b);
	    }

	}
	sb.append("'");

	return sb.toString();
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getStatementString(boolean)
     */
    public String getStatementString(boolean b) {
	return Boolean.toString(b);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getNullStatementString()
     */
    public String getNullStatementString() {
	return "null";
    }

    static String escapeString(String string){
	return string.replaceAll("\\Q'\\E", "''");
    }

}