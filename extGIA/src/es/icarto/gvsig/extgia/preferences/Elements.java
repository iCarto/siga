package es.icarto.gvsig.extgia.preferences;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.batch.elements.BatchBarreraRigidaTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchIsletasTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchSenhalizacionVerticalTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchTaludesTrabajos;
import es.icarto.gvsig.extgia.forms.areas_descanso.AreasDescansoForm;
import es.icarto.gvsig.extgia.forms.areas_mantenimiento.AreasMantenimientoForm;
import es.icarto.gvsig.extgia.forms.areas_peaje.AreasPeajeForm;
import es.icarto.gvsig.extgia.forms.areas_servicio.AreasServicioForm;
import es.icarto.gvsig.extgia.forms.bajantes.BajantesForm;
import es.icarto.gvsig.extgia.forms.barrera_rigida.BarreraRigidaForm;
import es.icarto.gvsig.extgia.forms.competencias.CompetenciasForm;
import es.icarto.gvsig.extgia.forms.comunicaciones.ComunicacionesForm;
import es.icarto.gvsig.extgia.forms.dren_caz.DrenCazForm;
import es.icarto.gvsig.extgia.forms.enlaces.EnlacesForm;
import es.icarto.gvsig.extgia.forms.firme.FirmeForm;
import es.icarto.gvsig.extgia.forms.isletas.IsletasForm;
import es.icarto.gvsig.extgia.forms.juntas.JuntasForm;
import es.icarto.gvsig.extgia.forms.lecho_frenado.LechoFrenadoForm;
import es.icarto.gvsig.extgia.forms.lineas_suministro.LineasSuministroForm;
import es.icarto.gvsig.extgia.forms.muros.MurosForm;
import es.icarto.gvsig.extgia.forms.obras_desague.ObrasDesagueForm;
import es.icarto.gvsig.extgia.forms.obras_paso.ObrasPasoForm;
import es.icarto.gvsig.extgia.forms.pasos_mediana.PasosMedianaForm;
import es.icarto.gvsig.extgia.forms.ramales.RamalesForm;
import es.icarto.gvsig.extgia.forms.senhalizacion_variable.SenhalizacionVariableForm;
import es.icarto.gvsig.extgia.forms.senhalizacion_vertical.SenhalizacionVerticalForm;
import es.icarto.gvsig.extgia.forms.taludes.TaludesForm;
import es.icarto.gvsig.extgia.forms.transformadores.TransformadoresForm;
import es.icarto.gvsig.extgia.forms.tuneles.TunelesForm;
import es.icarto.gvsig.extgia.forms.valla_cierre.VallaCierreForm;
import es.icarto.gvsig.navtableforms.AbstractForm;

public enum Elements {
    Areas_Descanso(
	    "id_area_descanso",
	    AreasDescansoForm.class,
	    BatchAbstractSubForm.class,
	    "areas_descanso_trabajos",
	    "areas_descanso_trabajos",
	    "areas_descanso_imagenes",
	    "areas_descanso_reconocimientos"),
    Areas_Mantenimiento(
	    "id_area_mantenimiento",
	    AreasMantenimientoForm.class,
	    null,
	    null,
	    null,
	    "areas_mantenimiento_imagenes",
	    null),
    Areas_Peaje(
	    "id_area_peaje",
	    AreasPeajeForm.class,
	    BatchAbstractSubForm.class,
	    "areas_peaje_trabajos",
	    "areas_peaje_trabajos",
	    "areas_peaje_imagenes",
	    "areas_peaje_reconocimientos"),
    Areas_Servicio(
	    "id_area_servicio",
	    AreasServicioForm.class,
	    BatchAbstractSubForm.class,
	    "areas_servicio_trabajos",
	    "areas_servicio_trabajos",
	    "areas_servicio_imagenes",
	    "areas_servicio_reconocimientos"),
    Barrera_Rigida(
	    "id_barrera_rigida",
	    BarreraRigidaForm.class,
	    BatchBarreraRigidaTrabajos.class,
	    "batch_barrera_rigida_trabajos",
	    "barrera_rigida_trabajos",
	    "barrera_rigida_imagenes",
	    "barrera_rigida_reconocimientos"),
    Enlaces("id_enlace", EnlacesForm.class, null, null, null, "enlaces_imagenes", "enlaces_reconocimientos"),
    Firme(
	    "id_firme",
	    FirmeForm.class,
	    BatchAbstractSubForm.class,
	    "firme_trabajos",
	    "firme_trabajos",
	    "firme_imagenes",
	    "firme_reconocimientos"),
    Isletas(
	    "id_isleta",
	    IsletasForm.class,
	    BatchIsletasTrabajos.class,
	    "batch_isletas_trabajos",
	    "isletas_trabajos",
	    "isletas_imagenes",
	    "isletas_reconocimientos"),
    Juntas(
	    "id_junta",
	    JuntasForm.class,
	    BatchAbstractSubForm.class,
	    "juntas_trabajos",
	    "juntas_trabajos",
	    "juntas_imagenes",
	    "juntas_reconocimientos"),
    Lecho_Frenado(
	    "id_lecho_frenado",
	    LechoFrenadoForm.class,
	    BatchAbstractSubForm.class,
	    "lecho_frenado_trabajos",
	    "lecho_frenado_trabajos",
	    "lecho_frenado_imagenes",
	    "lecho_frenado_reconocimientos"),
    Lineas_Suministro(
	    "id_linea_suministro",
	    LineasSuministroForm.class,
	    BatchAbstractSubForm.class,
	    "lineas_suministro_trabajos",
	    "lineas_suministro_trabajos",
	    "lineas_suministro_imagenes",
	    "lineas_suministro_reconocimientos"),
    Muros(
	    "id_muro",
	    MurosForm.class,
	    BatchAbstractSubForm.class,
	    "muros_trabajos",
	    "muros_trabajos",
	    "muros_imagenes",
	    "muros_reconocimientos"),
    Obras_Desague(
	    "id_obra_desague",
	    ObrasDesagueForm.class,
	    BatchAbstractSubForm.class,
	    "obras_desague_trabajos",
	    "obras_desague_trabajos",
	    "obras_desague_imagenes",
	    null),
    Obras_Paso(
	    "id_obra_paso",
	    ObrasPasoForm.class,
	    BatchAbstractSubForm.class,
	    "obras_paso_trabajos",
	    "obras_paso_trabajos",
	    "obras_paso_imagenes",
	    null),
    Pasos_Mediana(
	    "id_paso_mediana",
	    PasosMedianaForm.class,
	    BatchAbstractSubForm.class,
	    "pasos_mediana_trabajos",
	    "pasos_mediana_trabajos",
	    "pasos_mediana_imagenes",
	    "pasos_mediana_reconocimientos"),
    Senhalizacion_Variable(
	    "id_senhal_variable",
	    SenhalizacionVariableForm.class,
	    BatchAbstractSubForm.class,
	    "senhalizacion_variable_trabajos",
	    "senhalizacion_variable_trabajos",
	    "senhalizacion_variable_imagenes",
	    "senhalizacion_variable_reconocimientos"),
    Senhalizacion_Vertical(
	    "id_elemento_senhalizacion",
	    SenhalizacionVerticalForm.class,
	    BatchSenhalizacionVerticalTrabajos.class,
	    "batch_senhalizacion_vertical_trabajos",
	    "senhalizacion_vertical_trabajos",
	    "senhalizacion_vertical_imagenes",
	    "senhalizacion_vertical_reconocimientos"),
    Taludes(
	    "id_talud",
	    TaludesForm.class,
	    BatchTaludesTrabajos.class,
	    "batch_taludes_trabajos",
	    "taludes_trabajos",
	    "taludes_imagenes",
	    "taludes_reconocimientos"),
    Transformadores(
	    "id_transformador",
	    TransformadoresForm.class,
	    BatchAbstractSubForm.class,
	    "transformadores_trabajos",
	    "transformadores_trabajos",
	    "transformadores_imagenes",
	    "transformadores_reconocimientos"),
	Tuneles(
	    "id_tunel",
	    TunelesForm.class,
	    null,
	    null,
	    null,
	    "tuneles_imagenes",
	    null),
    Valla_Cierre(
	    "id_valla",
	    VallaCierreForm.class,
	    BatchAbstractSubForm.class,
	    "valla_cierre_trabajos",
	    "valla_cierre_trabajos",
	    "valla_cierre_imagenes",
	    "valla_cierre_reconocimientos"),
    Ramales("gid", RamalesForm.class, null, null, null, "ramales_imagenes", null),
    Competencias("gid", CompetenciasForm.class, null, null, null, null, null),
    Comunicaciones("gid", ComunicacionesForm.class, null, null, null, "comunicaciones_imagenes", null),
    Dren_Caz("gid", DrenCazForm.class, null, null, null, "dren_caz", null),
    Bajantes("gid", BajantesForm.class, null, null, null, "bajantes", null);

    public final String pk;
    public final Class<? extends AbstractForm> form;
    public final Class<? extends BatchAbstractSubForm> batchForm;
    public final String layerName;
    public final String batchTrabajosBasicName;
    public final String trabajosTableName;
    public final String imagenesTableName;
    public final String batchReconocimientosBasicName;
    
    private Elements(String pk, Class<? extends AbstractForm> form,
	    Class<? extends BatchAbstractSubForm> batchForm,
	    String batchTrabajosBasicName, String dbTableName, String imagenesTablename, String batchReconocimientosBasicName) {
	this.pk = pk;
	this.form = form;
	this.batchForm = batchForm;
	this.layerName = this.toString();
	this.batchTrabajosBasicName = batchTrabajosBasicName;
	this.trabajosTableName = dbTableName;
	this.imagenesTableName = imagenesTablename;
	this.batchReconocimientosBasicName = batchReconocimientosBasicName;
    }
}