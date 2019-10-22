package es.icarto.gvsig.extgia.forms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchAreasDescansoReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchAreasPeajeReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchAreasServicioReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchBarreraRigidaReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchEnlacesReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchFirmeReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchIsletasReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchJuntasReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchLechoFrenadoReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchLineasSuministroReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchMurosReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchPasosMedianaReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchSenhalizacionVariableReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchTaludesReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchTransformadoresReconocimientos;
import es.icarto.gvsig.extgia.batch.reconocimientos.BatchVallaCierreReconocimientos;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.tables.handler.BaseTableHandler;
import es.icarto.gvsig.navtableforms.gui.tables.model.AlphanumericTableModel;
import es.icarto.gvsig.navtableforms.gui.tables.model.TableModelFactory;
import es.icarto.gvsig.navtableforms.utils.FormFactory;

public class LaunchGIAForms {

    private static final Logger logger = Logger.getLogger(LaunchGIAForms.class);

    public static AbstractForm getFormDependingOfLayer(FLyrVect layer) {
	AbstractForm form = null;

	final String layerName = layer.getName();
	if (!isGIALayerName(layerName)) {
	    return form;
	}
	Class<? extends AbstractForm> formClass = Elements.valueOf(layerName).form;
	try {
	    Constructor<? extends AbstractForm> constructor = formClass
		    .getConstructor(FLyrVect.class);
	    form = constructor.newInstance(layer);
	} catch (NoSuchMethodException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (InstantiationException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (IllegalAccessException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (InvocationTargetException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return form;
    }

    public static AbstractForm getExistingFormDependingOfLayer(FLyrVect layer) {
        final String layerName = layer.getName();
        if (isGIALayerName(layerName)) {
            Class<? extends AbstractForm> formClass = Elements
                    .valueOf(layerName).form;
            for (IWindow window : PluginServices.getMDIManager()
                    .getAllWindows()) {
                try {
                    return formClass.cast(window);
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }
        return null;
    }

    public static AbstractForm getExistingOrNewFormDependingOfLayer(
            FLyrVect layer) {
        AbstractForm form = getExistingFormDependingOfLayer(layer);
        return form != null ? form : getFormDependingOfLayer(layer);
    }

    public static boolean callFormDependingOfLayer(FLyrVect layer,
	    boolean editing) {
	AbstractForm form = getFormDependingOfLayer(layer);
	if (form != null) {
	    if (form.init()) {
		PluginServices.getMDIManager().addWindow(form);
	    }
	    if (editing) {
		form.last();
	    }
	}
	return true;
    }

    private static boolean isGIALayerName(String layerName) {
	boolean isGIALayerName = false;
	for (int i = 0; i < Elements.values().length; i++) {
	    if (Elements.values()[i].toString().equals(layerName)) {
		isGIALayerName = true;
	    }
	}
	return isGIALayerName;
    }

    public static void callBatchTrabajosSubFormDependingOfElement(
	    String elementStr, String dbTableName,
	    BaseTableHandler trabajosTableHandler) {
	final Elements element = Elements.valueOf(elementStr);
	BatchAbstractSubForm batchForm = getBatchTrabajosSubFormDependingOfElement(element);
	if (batchForm == null) {
	    logger.error(String.format("This sould not happen: %s %s %s",
		    elementStr, dbTableName, trabajosTableHandler));
	}

	FormFactory.checkAndLoadTableRegistered(element.trabajosTableName);
	AlphanumericTableModel model = TableModelFactory.createFromTable(
		dbTableName, null, null);

	batchForm.setModel(model);
	batchForm.setTrabajoTableHandler(trabajosTableHandler);
	batchForm.actionCreateRecord();
    }

    private static BatchAbstractSubForm getBatchTrabajosSubFormDependingOfElement(
	    Elements element) {
	BatchAbstractSubForm form = null;

	Class<? extends BatchAbstractSubForm> formClass = element.batchForm;
	if (formClass == null) {
	    return form;
	}

	try {
	    Constructor<? extends BatchAbstractSubForm> constructor = formClass
		    .getConstructor(Elements.class);
	    form = constructor.newInstance(element);
	} catch (NoSuchMethodException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (InstantiationException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (IllegalAccessException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (InvocationTargetException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return form;
    }

    public static void callBatchReconocimientosSubFormDependingOfElement(
	    String element, String formFileName, String dbTableName) {
	BatchAbstractSubForm subform;
	switch (Elements.valueOf(element)) {
	case Areas_Peaje:
	    subform = new BatchAreasPeajeReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Areas_Descanso:
	    subform = new BatchAreasDescansoReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Areas_Servicio:
	    subform = new BatchAreasServicioReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Barrera_Rigida:
	    subform = new BatchBarreraRigidaReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Enlaces:
	    subform = new BatchEnlacesReconocimientos(formFileName, dbTableName);
	    break;
	case Firme:
	    subform = new BatchFirmeReconocimientos(formFileName, dbTableName);
	    break;
	case Isletas:
	    subform = new BatchIsletasReconocimientos(formFileName, dbTableName);
	    break;
	case Juntas:
	    subform = new BatchJuntasReconocimientos(formFileName, dbTableName);
	    break;
	case Lecho_Frenado:
	    subform = new BatchLechoFrenadoReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Lineas_Suministro:
	    subform = new BatchLineasSuministroReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Muros:
	    subform = new BatchMurosReconocimientos(formFileName, dbTableName);
	    break;
	case Pasos_Mediana:
	    subform = new BatchPasosMedianaReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Senhalizacion_Variable:
	    subform = new BatchSenhalizacionVariableReconocimientos(
		    formFileName, dbTableName);
	    break;
	case Taludes:
	    subform = new BatchTaludesReconocimientos(formFileName, dbTableName);
	    break;
	case Transformadores:
	    subform = new BatchTransformadoresReconocimientos(formFileName,
		    dbTableName);
	    break;
	case Valla_Cierre:
	    subform = new BatchVallaCierreReconocimientos(formFileName,
		    dbTableName);
	    break;
	default:
	    subform = null;
	}
	FormFactory.checkAndLoadTableRegistered(dbTableName);
	AlphanumericTableModel model = TableModelFactory.createFromTable(
		dbTableName, null, null);
	subform.setModel(model);
	subform.actionCreateRecord();
    }
}
