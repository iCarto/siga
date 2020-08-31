package es.icarto.gvsig.extgia.forms.farolas;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import es.icarto.gvsig.extgia.forms.centros_mando.CentrosMandoForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FarolasForm extends BasicAbstractForm {
    
    public static final String TABLENAME = "farolas";
    public static final String BUFFER_RADIO = "2500";

    public FarolasForm(FLyrVect layer) {
    super(layer);
    if (layer.isEditing()) {
    long pos;
    try {
        pos = layer.getSource().getRecordset().getRowCount()-1;
        calculateCentroMandoIDs(layer, pos);
    } catch (ReadDriverException e) {
        e.printStackTrace();
    }
    }
    
    }

    protected void fillSpecificValues() {
    super.fillSpecificValues();
    calculateCentroMandoIDs(layer, getPosition());
    try {
        Value[] rowValues = layer.getSource().getRecordset().getRow(getPosition());
        for (int j = 0; j < getCentrosMandoCB().getItemCount(); j++) {
            String value = (String) getCentrosMandoCB().getItemAt(j);
            if ((value != null)
                && (value.compareTo(rowValues[1].toString()) == 0)) {
                getCentrosMandoCB().setSelectedIndex(j);
                break;
            }
        }
    } catch (ReadDriverException e) {
        e.printStackTrace();
    }
    }
    
    private void calculateCentroMandoIDs(FLyrVect layer, long pos) {
    ReadableVectorial source = layer.getSource();
    try {    
        IGeometry g = null;
        source.start();
        g = source.getShape(Long.valueOf(pos).intValue());
        source.stop();

        Geometry geom = g.toJTSGeometry();
        Coordinate[] coords = geom.getCoordinates();
        getCentrosMandoCB().removeAllItems();
        getCentrosMandoCB().addItem(" ");
        
        PreparedStatement statement;
        String query = "SELECT gid FROM " 
                + DBFieldNames.GIA_SCHEMA + "." + CentrosMandoForm.TABLENAME 
                + " WHERE ST_Within(the_geom, ST_Buffer(ST_SetSRID(ST_MakePoint("
                + coords[0].x + ", " + coords[0].y +")," + DBFieldNames.GIA_SRID + ")," 
                + BUFFER_RADIO + "))";
        
       statement = DBSession.getCurrentSession().getJavaConnection()
                .prepareStatement(query);
       statement.execute();
       ResultSet rs = statement.getResultSet();
       while (rs.next()) {
           getCentrosMandoCB().addItem(rs.getString(1));
       }
   } catch (ExpansionFileReadException e) {
        e.printStackTrace();
   } catch (ReadDriverException e) {
        e.printStackTrace();
   } catch (SQLException e) {
        e.printStackTrace();
   }
    
   }

    private JComboBox getCentrosMandoCB() {
    Map<String, JComponent> widgets = getWidgets();
    JComboBox centrosMandoCB = (JComboBox) widgets.get(DBFieldNames.CENTRO_MANDO);
    return centrosMandoCB;
    }

    @Override
    protected String getBasicName() {
    return TABLENAME;
    }

    @Override
    protected String getSchema() {
    return DBFieldNames.GIA_SCHEMA; 
    }

}
