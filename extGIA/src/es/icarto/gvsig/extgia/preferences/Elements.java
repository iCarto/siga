package es.icarto.gvsig.extgia.preferences;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.batch.elements.BatchBarreraRigidaTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchIsletasTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchSenhalizacionVerticalTrabajos;
import es.icarto.gvsig.extgia.batch.elements.BatchTaludesTrabajos;
import es.icarto.gvsig.extgia.forms.areas_descanso.AreasDescansoForm;
import es.icarto.gvsig.extgia.forms.areas_mantenimiento.AreasMantenimientoForm;
import es.icarto.gvsig.extgia.forms.areas_peaje.AreasPeajeForm;
import es.icarto.gvsig.extgia.forms.areas_servicio.AreasServicioForm;
import es.icarto.gvsig.extgia.forms.arquetas.ArquetasForm;
import es.icarto.gvsig.extgia.forms.bajantes.BajantesForm;
import es.icarto.gvsig.extgia.forms.barrera_metalica.BarreraMetalicaForm;
import es.icarto.gvsig.extgia.forms.barrera_rigida.BarreraRigidaForm;
import es.icarto.gvsig.extgia.forms.centros_mando.CentrosMandoForm;
import es.icarto.gvsig.extgia.forms.competencias.CompetenciasForm;
import es.icarto.gvsig.extgia.forms.comunicaciones.ComunicacionesForm;
import es.icarto.gvsig.extgia.forms.cunetas.CunetasForm;
import es.icarto.gvsig.extgia.forms.dren_caz.DrenCazForm;
import es.icarto.gvsig.extgia.forms.enlaces.EnlacesForm;
import es.icarto.gvsig.extgia.forms.estructuras.EstructurasForm;
import es.icarto.gvsig.extgia.forms.farolas.FarolasForm;
import es.icarto.gvsig.extgia.forms.fibra_optica_derivaciones.FibraOpticaDerivacionesForm;
import es.icarto.gvsig.extgia.forms.firme.FirmeForm;
import es.icarto.gvsig.extgia.forms.isletas.IsletasForm;
import es.icarto.gvsig.extgia.forms.juntas.JuntasForm;
import es.icarto.gvsig.extgia.forms.lecho_frenado.LechoFrenadoForm;
import es.icarto.gvsig.extgia.forms.lineas_distribucion_ufd.LineasDistribucionUFDForm;
import es.icarto.gvsig.extgia.forms.lineas_suministro.LineasSuministroForm;
import es.icarto.gvsig.extgia.forms.muros.MurosForm;
import es.icarto.gvsig.extgia.forms.obras_drenaje.ObrasDrenajeForm;
import es.icarto.gvsig.extgia.forms.pasos_mediana.PasosMedianaForm;
import es.icarto.gvsig.extgia.forms.pozos.PozosForm;
import es.icarto.gvsig.extgia.forms.pretiles.PretilesForm;
import es.icarto.gvsig.extgia.forms.ramales.RamalesForm;
import es.icarto.gvsig.extgia.forms.senhalizacion_variable.SenhalizacionVariableForm;
import es.icarto.gvsig.extgia.forms.senhalizacion_vertical.SenhalizacionVerticalForm;
import es.icarto.gvsig.extgia.forms.taludes.TaludesForm;
import es.icarto.gvsig.extgia.forms.transformadores.TransformadoresForm;
import es.icarto.gvsig.extgia.forms.tuneles.TunelesForm;
import es.icarto.gvsig.extgia.forms.valla_cierre.VallaCierreForm;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.udc.cartolab.gvsig.users.utils.DBSession;

final class Help {
    public static final List<String> ignoreGeom = Arrays.asList(new String[] { "the_geom", "geom" });
    public static final List<String> ignoredGidGeom = Arrays.asList(new String[] { "gid", "the_geom", "geom" });
}

public enum Elements {

    Areas_Descanso(
            "id_area_descanso",
            AreasDescansoForm.class,
            BatchAbstractSubForm.class,
            "areas_descanso_trabajos",
            "areas_descanso_trabajos",
            "areas_descanso_imagenes",
            "areas_descanso_reconocimientos",
            Help.ignoredGidGeom,
            "nombre",
            true),
    Areas_Mantenimiento(
                    "id_area_mantenimiento",
                    AreasMantenimientoForm.class,
                    null,
                    null,
                    null,
                    "areas_mantenimiento_imagenes",
                    null,
                    Help.ignoredGidGeom,
                    "nombre",
                    true),
    Areas_Peaje(
                            "id_area_peaje",
                            AreasPeajeForm.class,
                            BatchAbstractSubForm.class,
                            "areas_peaje_trabajos",
                            "areas_peaje_trabajos",
                            "areas_peaje_imagenes",
                            "areas_peaje_reconocimientos",
                            Help.ignoredGidGeom,
                            "nombre",
                            true),
    Areas_Servicio(
                                    "id_area_servicio",
                                    AreasServicioForm.class,
                                    BatchAbstractSubForm.class,
                                    "areas_servicio_trabajos",
                                    "areas_servicio_trabajos",
                                    "areas_servicio_imagenes",
                                    "areas_servicio_reconocimientos",
                                    Help.ignoredGidGeom,
                                    "nombre",
                                    true),
    Arquetas("gid", ArquetasForm.class, null, null, null, "arquetas_imagenes", null, Help.ignoreGeom, null, true),
    Bajantes("gid", BajantesForm.class, null, null, null, "bajantes_imagenes", null, Help.ignoreGeom, null, true),
    Barrera_Rigida(
                                            "id_barrera_rigida",
                                            BarreraRigidaForm.class,
                                            BatchBarreraRigidaTrabajos.class,
                                            "batch_barrera_rigida_trabajos",
                                            "barrera_rigida_trabajos",
                                            "barrera_rigida_imagenes",
                                            "barrera_rigida_reconocimientos",
                                            Help.ignoredGidGeom,
                                            "codigo",
                                            true),
    Barrera_Metalica(
                                                    "gid",
                                                    BarreraMetalicaForm.class,
                                                    null,
                                                    null,
                                                    null,
                                                    "barrera_metalica_imagenes",
                                                    null,
                                                    Help.ignoreGeom,
                                                    null,
                                                    true),
    Centros_Mando(
                                                            "gid",
                                                            CentrosMandoForm.class,
                                                            null,
                                                            null,
                                                            null,
                                                            "centros_mando_imagenes",
                                                            null,
                                                            Help.ignoreGeom,
                                                            null,
                                                            true),
    Competencias("gid", CompetenciasForm.class, null, null, null, null, null, Help.ignoreGeom, null, true),
    Comunicaciones(
                                                                    "gid",
                                                                    ComunicacionesForm.class,
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    "comunicaciones_imagenes",
                                                                    null,
                                                                    Help.ignoreGeom,
                                                                    null,
                                                                    false),
    Cunetas("gid", CunetasForm.class, null, null, null, "cunetas_imagenes", null, Help.ignoreGeom, null, true),
    Dren_Caz("gid", DrenCazForm.class, null, null, null, "dren_caz_imagenes", null, Help.ignoreGeom, null, true),
    Enlaces(
                                                                            "id_enlace",
                                                                            EnlacesForm.class,
                                                                            null,
                                                                            null,
                                                                            null,
                                                                            "enlaces_imagenes",
                                                                            "enlaces_reconocimientos",
                                                                            Help.ignoredGidGeom,
                                                                            "nombre",
                                                                            true),
   Estructuras(
                                                                                    "id_estructura",
                                                                                    EstructurasForm.class,
                                                                                    BatchAbstractSubForm.class,
                                                                                    "estructuras_trabajos",
                                                                                    "estructuras_trabajos",
                                                                                    "estructuras_imagenes",
                                                                                    "estructuras_reconocimientos",
                                                                                    Help.ignoredGidGeom,
                                                                                    "nombre",
                                                                                    true),
    Farolas("gid", FarolasForm.class, null, null, null, "null", null, Help.ignoreGeom, null, false),                                                                                
    Fibra_Optica_Derivaciones(
                                                                                            "gid",
                                                                                            FibraOpticaDerivacionesForm.class,
                                                                                            null,
                                                                                            null,
                                                                                            null,
                                                                                            "fibra_optica_derivaciones_imagenes",
                                                                                            null,
                                                                                            Help.ignoreGeom,
                                                                                            null,
                                                                                            false),
    Firme(
                                                                                                    "id_firme",
                                                                                                    FirmeForm.class,
                                                                                                    BatchAbstractSubForm.class,
                                                                                                    "firme_trabajos",
                                                                                                    "firme_trabajos",
                                                                                                    "firme_imagenes",
                                                                                                    "firme_reconocimientos",
                                                                                                    Help.ignoredGidGeom,
                                                                                                    "unidad_constructiva",
                                                                                                    false),
    Isletas(
                                                                                                            "id_isleta",
                                                                                                            IsletasForm.class,
                                                                                                            BatchIsletasTrabajos.class,
                                                                                                            "batch_isletas_trabajos",
                                                                                                            "isletas_trabajos",
                                                                                                            "isletas_imagenes",
                                                                                                            "isletas_reconocimientos",
                                                                                                            Help.ignoredGidGeom,
                                                                                                            null,
                                                                                                            true),
    Juntas(
                                                                                                                    "id_junta",
                                                                                                                    JuntasForm.class,
                                                                                                                    BatchAbstractSubForm.class,
                                                                                                                    "juntas_trabajos",
                                                                                                                    "juntas_trabajos",
                                                                                                                    "juntas_imagenes",
                                                                                                                    "juntas_reconocimientos",
                                                                                                                    Help.ignoredGidGeom,
                                                                                                                    null,
                                                                                                                    true),
    Lecho_Frenado(
                                                                                                                            "id_lecho_frenado",
                                                                                                                            LechoFrenadoForm.class,
                                                                                                                            BatchAbstractSubForm.class,
                                                                                                                            "lecho_frenado_trabajos",
                                                                                                                            "lecho_frenado_trabajos",
                                                                                                                            "lecho_frenado_imagenes",
                                                                                                                            "lecho_frenado_reconocimientos",
                                                                                                                            Help.ignoredGidGeom,
                                                                                                                            null,
                                                                                                                            true),
    Lineas_Distribucion_UFD(
                                                                                                                                    "gid",
                                                                                                                                    LineasDistribucionUFDForm.class,
                                                                                                                                    BatchAbstractSubForm.class,
                                                                                                                                    null,
                                                                                                                                    null,
                                                                                                                                    "lineas_distribucion_ufd_imagenes",
                                                                                                                                    null,
                                                                                                                                    Help.ignoreGeom,
                                                                                                                                    null,
                                                                                                                                    true),

    Lineas_Suministro(
                                                                                                                                            "id_linea_suministro",
                                                                                                                                            LineasSuministroForm.class,
                                                                                                                                            BatchAbstractSubForm.class,
                                                                                                                                            "lineas_suministro_trabajos",
                                                                                                                                            "lineas_suministro_trabajos",
                                                                                                                                            "lineas_suministro_imagenes",
                                                                                                                                            "lineas_suministro_reconocimientos",
                                                                                                                                            Help.ignoredGidGeom,
                                                                                                                                            "denominacion",
                                                                                                                                            true),
    Muros(
                                                                                                                                                    "id_muro",
                                                                                                                                                    MurosForm.class,
                                                                                                                                                    BatchAbstractSubForm.class,
                                                                                                                                                    "muros_trabajos",
                                                                                                                                                    "muros_trabajos",
                                                                                                                                                    "muros_imagenes",
                                                                                                                                                    "muros_reconocimientos",
                                                                                                                                                    Help.ignoredGidGeom,
                                                                                                                                                    null,
                                                                                                                                                    true),
    Obras_Drenaje(
                                                                                                                                                            "id_obra_drenaje",
                                                                                                                                                            ObrasDrenajeForm.class,
                                                                                                                                                            BatchAbstractSubForm.class,
                                                                                                                                                            "obras_drenaje_trabajos",
                                                                                                                                                            "obras_drenaje_trabajos",
                                                                                                                                                            "obras_drenaje_imagenes",
                                                                                                                                                            null,
                                                                                                                                                            Help.ignoredGidGeom,
                                                                                                                                                            null,
                                                                                                                                                            true),

                                                                                                                                                            Pasos_Mediana(
                                                                                                                                                                    "id_paso_mediana",
                                                                                                                                                                    PasosMedianaForm.class,
                                                                                                                                                                    BatchAbstractSubForm.class,
                                                                                                                                                                    "pasos_mediana_trabajos",
                                                                                                                                                                    "pasos_mediana_trabajos",
                                                                                                                                                                    "pasos_mediana_imagenes",
                                                                                                                                                                    "pasos_mediana_reconocimientos",
                                                                                                                                                                    Help.ignoredGidGeom,
                                                                                                                                                                    null,
                                                                                                                                                                    true),
    Pozos("gid", PozosForm.class, null, null, null, "pozos_imagenes", null, Help.ignoreGeom, "denominacion", true),
    Pretiles("gid", PretilesForm.class, null, null, null, "pretiles_imagenes", null, Help.ignoreGeom, null, true),
    Ramales("gid", RamalesForm.class, null, null, null, "ramales_imagenes", null, Help.ignoreGeom, null, true),
    Senhalizacion_Variable(
                                                                                                                                                                            "id_senhal_variable",
                                                                                                                                                                            SenhalizacionVariableForm.class,
                                                                                                                                                                            BatchAbstractSubForm.class,
                                                                                                                                                                            "senhalizacion_variable_trabajos",
                                                                                                                                                                            "senhalizacion_variable_trabajos",
                                                                                                                                                                            "senhalizacion_variable_imagenes",
                                                                                                                                                                            "senhalizacion_variable_reconocimientos",
                                                                                                                                                                            Help.ignoredGidGeom,
                                                                                                                                                                            null,
                                                                                                                                                                            true),
    Senhalizacion_Vertical(
                                                                                                                                                                                    "id_elemento_senhalizacion",
                                                                                                                                                                                    SenhalizacionVerticalForm.class,
                                                                                                                                                                                    BatchSenhalizacionVerticalTrabajos.class,
                                                                                                                                                                                    "batch_senhalizacion_vertical_trabajos",
                                                                                                                                                                                    "senhalizacion_vertical_trabajos",
                                                                                                                                                                                    "senhalizacion_vertical_imagenes",
                                                                                                                                                                                    null,
                                                                                                                                                                                    Help.ignoredGidGeom,
                                                                                                                                                                                    null,
                                                                                                                                                                                    true),
    Taludes(
                                                                                                                                                                                            "id_talud",
                                                                                                                                                                                            TaludesForm.class,
                                                                                                                                                                                            BatchTaludesTrabajos.class,
                                                                                                                                                                                            "batch_taludes_trabajos",
                                                                                                                                                                                            "taludes_trabajos",
                                                                                                                                                                                            "taludes_imagenes",
                                                                                                                                                                                            "taludes_reconocimientos",
                                                                                                                                                                                            Help.ignoredGidGeom,
                                                                                                                                                                                            null,
                                                                                                                                                                                            true),
    Transformadores(
                                                                                                                                                                                                    "id_transformador",
                                                                                                                                                                                                    TransformadoresForm.class,
                                                                                                                                                                                                    BatchAbstractSubForm.class,
                                                                                                                                                                                                    "transformadores_trabajos",
                                                                                                                                                                                                    "transformadores_trabajos",
                                                                                                                                                                                                    "transformadores_imagenes",
                                                                                                                                                                                                    "transformadores_reconocimientos",
                                                                                                                                                                                                    Help.ignoredGidGeom,
                                                                                                                                                                                                    "denominacion",
                                                                                                                                                                                                    true),
                                                                                                                                                                                                    Tuneles("gid", TunelesForm.class, null, null, null, "tuneles_imagenes", null, Help.ignoreGeom, "nombre", true),
    Valla_Cierre(
                                                                                                                                                                                                            "id_valla",
                                                                                                                                                                                                            VallaCierreForm.class,
                                                                                                                                                                                                            BatchAbstractSubForm.class,
                                                                                                                                                                                                            "valla_cierre_trabajos",
                                                                                                                                                                                                            "valla_cierre_trabajos",
                                                                                                                                                                                                            "valla_cierre_imagenes",
                                                                                                                                                                                                            "valla_cierre_reconocimientos",
                                                                                                                                                                                                            Help.ignoredGidGeom,
                                                                                                                                                                                                            null,
                                                                                                                                                                                                            true);

    private static final Logger logger = Logger.getLogger(Elements.class);

    private static Map<String, String> NAMES;

    public final String pk;
    public final Class<? extends AbstractForm> form;
    public final Class<? extends BatchAbstractSubForm> batchForm;
    public final String layerName;
    public final String descriptiveField;
    public final String batchTrabajosBasicName;
    public final String trabajosTableName;
    public final String imagenesTableName;
    public final String batchReconocimientosBasicName;
    public final Boolean hasViaInfo;
    public final List<String> ignoredColumnsInReports;

    private Elements(String pk, Class<? extends AbstractForm> form, Class<? extends BatchAbstractSubForm> batchForm,
            String batchTrabajosBasicName, String dbTableName, String imagenesTablename,
            String batchReconocimientosBasicName, List<String> ignoredColumnsInReportrs, String descriptiveField,
            boolean hasViaInfo) {
        this.pk = pk;
        this.form = form;
        this.batchForm = batchForm;
        this.layerName = this.toString();
        this.descriptiveField = descriptiveField;
        this.hasViaInfo = hasViaInfo;
        this.batchTrabajosBasicName = batchTrabajosBasicName;
        this.trabajosTableName = dbTableName;
        this.imagenesTableName = imagenesTablename;
        this.batchReconocimientosBasicName = batchReconocimientosBasicName;
        this.ignoredColumnsInReports = ignoredColumnsInReportrs;
    }

    private static void initializeNames() {
        String query = "SELECT DISTINCT id, item FROM audasa_extgia_dominios.elemento;";

        try {
            ResultSet rs = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query).executeQuery();
            NAMES = new HashMap<String, String>();
            while (rs.next()) {
                String id = rs.getString(1);
                String name = rs.getString(2);
                if (id != null && name != null && !NAMES.containsKey(id)) {
                    NAMES.put(id, name);
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public String getStylizedName() {
        if (NAMES == null) {
            initializeNames();
        }
        return (NAMES.containsKey(this.name())) ? NAMES.get(this.name()) : this.name();
    }
}