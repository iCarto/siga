/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.andami.ui.mdiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.Messages;
import com.iver.andami.plugins.ExtensionDecorator;
import com.iver.andami.plugins.PluginClassLoader;
import com.iver.andami.plugins.config.generate.PluginConfig;
import com.iver.andami.plugins.config.generate.SkinExtension;
import com.iver.andami.ui.MDIManagerLoadException;
import com.iver.utiles.XMLEntity;

/**
 * Se encarga de la creación de la clase Skin.
 *
 * @author Fernando González Cortés
 */
public class MDIManagerFactory {
    private static SkinExtension skinExtension = null;
    private static PluginClassLoader loader = null;
    private static MDIManager mdiManager = null;
    private static Logger logger = Logger.getLogger(MDIManagerFactory.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param extension
     *            DOCUMENT ME!
     * @param loader
     *            DOCUMENT ME!
     */
    public static void setSkinExtension(SkinExtension extension, PluginClassLoader loader) {
        MDIManagerFactory.loader = loader;
        MDIManagerFactory.skinExtension = extension;
    }

    /**
     * Obtiene una referencia al Skin cargado. El skin cargado es un singleton, se
     * devuelve la misma instancia a todas las invocaciones de éste método
     *
     * @return referencia al skin cargado
     */
    public static MDIManager createManager() {
        if (mdiManager != null) {
            return mdiManager;
        }

        if (skinExtension == null) {
            throw new NoSkinExtensionException(
                    Messages.getString("MDIManagerFactory.No_skin_extension_in_the_plugins"));
        } else {
            try {
                mdiManager = (MDIManager) loader.loadClass(skinExtension.getClassName()).newInstance();
                return mdiManager;
            } catch (InstantiationException e) {
                logger.error(Messages.getString("Launcher.Error_instanciando_la_extension"), e);
            } catch (IllegalAccessException e) {
                logger.error(Messages.getString("Launcher.Error_instanciando_la_extension"), e);
            } catch (ClassNotFoundException e) {
                logger.error(Messages.getString("Launcher.No_se_encontro_la_clase_de_la_extension"), e);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the skinExtension.
     */
    public static SkinExtension getSkinExtension() {
        return skinExtension;
    }

    public static void skinPlugin(XMLEntity entity, String defaultSkin) throws MDIManagerLoadException {
        HashMap<String, PluginConfig> pluginsConfig = Launcher.getPluginConfig();
        SkinExtension skinExtension = null;
        PluginClassLoader pluginClassLoader = null;
        List<SkinExtension> skinExtensions = new ArrayList<SkinExtension>();

        for (String name : pluginsConfig.keySet()) {
            PluginConfig pc = pluginsConfig.get(name);
            PluginServices ps = Launcher.getPluginServices(name);

            if (pc.getExtensions().getSkinExtension() != null) {

                SkinExtension[] se = pc.getExtensions().getSkinExtension();
                for (int numExten = 0; numExten < se.length; numExten++) {
                    skinExtensions.add(se[numExten]);
                }
                for (int j = 0; j < se.length; j++) {
                    String configuredSkin = configureSkin(entity, defaultSkin);
                    if (configuredSkin != null && configuredSkin.equals(se[j].getClassName())) {
                        skinExtension = se[j];
                        pluginClassLoader = ps.getClassLoader();
                    }
                }
            }

        }

        if ((skinExtension != null) && (pluginClassLoader != null)) {
            // configured skin was found
            fixSkin(skinExtension, pluginClassLoader);
        } else {
            if (skinExtensions.contains(defaultSkin)) {
                skinPlugin(entity, defaultSkin);
            } else if (skinExtensions.size() > 0) {
                // try to load the first skin found
                SkinExtension se = skinExtensions.get(0);
                skinPlugin(entity, se.getClassName());
            } else {
                throw new MDIManagerLoadException("No Skin-Extension installed");
            }
        }
    }

    private static String configureSkin(XMLEntity xml, String defaultSkin) {
        if (defaultSkin == null) {
            for (int i = 0; i < xml.getChildrenCount(); i++) {
                if (xml.getChild(i).contains("Skin-Selected")) {
                    String className = xml.getChild(i).getStringProperty("Skin-Selected");
                    return className;
                }
            }
        }
        return defaultSkin;
    }

    private static void fixSkin(SkinExtension skinExtension, PluginClassLoader pluginClassLoader)
            throws MDIManagerLoadException {
        // now insert the skin selected.
        MDIManagerFactory.setSkinExtension(skinExtension, pluginClassLoader);

        try {
            Class skinClass = pluginClassLoader.loadClass(skinExtension.getClassName());

            com.iver.andami.plugins.IExtension skinInstance = (com.iver.andami.plugins.IExtension) skinClass
                    .newInstance();
            ExtensionDecorator newExtensionDecorator = new ExtensionDecorator(skinInstance,
                    ExtensionDecorator.INACTIVE);
            Launcher.getClassesExtensions().put(skinClass, newExtensionDecorator);
        } catch (ClassNotFoundException e) {
            logger.error(Messages.getString("Launcher.No_se_encontro_la_clase_mdi_manager"), e);
            throw new MDIManagerLoadException(e);
        } catch (InstantiationException e) {
            logger.error(Messages.getString("Launcher.No_se_pudo_instanciar_la_clase_mdi_manager"), e);
            throw new MDIManagerLoadException(e);
        } catch (IllegalAccessException e) {
            logger.error(Messages.getString("Launcher.No_se_pudo_acceder_a_la_clase_mdi_manager"), e);
            throw new MDIManagerLoadException(e);
        }

    }
}
