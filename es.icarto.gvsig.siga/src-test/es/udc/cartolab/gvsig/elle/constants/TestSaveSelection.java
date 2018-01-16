package es.udc.cartolab.gvsig.elle.constants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.fmap.layers.VectorialAdapter;
import com.iver.cit.gvsig.fmap.layers.VectorialDefaultAdapter;

import es.icarto.gvsig.commons.datasources.FieldDescriptionFactory;
import es.icarto.gvsig.commons.testutils.FLyrVectDBStub;
import es.icarto.gvsig.commons.testutils.FLyrVectStub;
import es.icarto.gvsig.commons.testutils.VectorialDBDriverStub;

public class TestSaveSelection {

    private DBLayerDefinition layerDefinition() {
	FieldDescriptionFactory fdf = new FieldDescriptionFactory();
	fdf.addInteger("gid");
	fdf.addInteger("org_row_number");
	DBLayerDefinition lyrDef = new DBLayerDefinition();
	lyrDef.setFieldID("gid");
	lyrDef.setFieldsDesc(fdf.getFields());
	return lyrDef;
    }

    private List<IFeature> featureList() {
	List<IFeature> featList = new ArrayList<IFeature>();
	final IGeometry geom = ShapeFactory.createPoint2D(0, 0);
	Integer id = 100;
	for (int i = 0; i < 10; i++) {

	    Value[] atts = new Value[2];
	    atts[0] = ValueFactory.createValue(id.intValue());
	    atts[1] = ValueFactory.createValue(i);

	    featList.add(new DefaultFeature(geom, atts, id.toString()));

	    id += 100;
	}
	return featList;
    }

    private List<IFeature> newFeatureList() {
	List<IFeature> featList = new ArrayList<IFeature>();
	final IGeometry geom = ShapeFactory.createPoint2D(0, 0);
	Integer id = 200;
	for (int i = 0; i < 11; i++) {

	    Value[] atts = new Value[2];
	    atts[0] = ValueFactory.createValue(id.intValue());
	    atts[1] = ValueFactory.createValue(i);

	    featList.add(new DefaultFeature(geom, atts, id.toString()));

	    id += 100;
	}
	return featList;
    }

    @Test
    public void silently_fails_if_layer_is_not_from_db() {
	FLyrVectStub layer = new FLyrVectStub("test");
	VectorialAdapter source = new VectorialDefaultAdapter();
	VectorialDriver driver = new VectorialDBDriverStub("foo",
		featureList(), layerDefinition());
	source.setDriver(driver);
	layer.setSource(source);
	SaveSelection saveSelection = new SaveSelection(layer);
	assertTrue(saveSelection.getError());
    }

    @Test
    public void test() {
	FLyrVectDBStub layer = new FLyrVectDBStub("test");

	layer.setData(featureList(), layerDefinition());

	layer.getSelectionSupport().getSelection().set(0); // gid = 100
	layer.getSelectionSupport().getSelection().set(4); // gid = 500

	SaveSelection saveSelection = new SaveSelection(layer);

	layer.setData(newFeatureList(), layerDefinition());

	saveSelection.restoreSelection();

	SelectionSupport selection = layer.getSelectionSupport();
	for (int i = 0; i < 11; i++) {
	    switch (i) {
	    case 3:
		assertTrue("i: " + i, selection.isSelected(i));
		break;

	    default:
		assertFalse("i: " + i, selection.isSelected(i));
		break;
	    }

	}

    }

}
