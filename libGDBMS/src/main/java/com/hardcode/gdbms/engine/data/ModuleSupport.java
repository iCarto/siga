package com.hardcode.gdbms.engine.data;

import java.util.HashMap;


/**
 * Clase de gesti�n de modulos. Un m�dulo en GDBMS es cualquier objeto que deba
 * ser accedido desde distintas partes del sistema no comunicadas entre s�.
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class ModuleSupport {
    private static HashMap nameModule = new HashMap();

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param instance DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public void registerModule(String name, Object instance) {
        if (nameModule.get(name) != null) {
            throw new RuntimeException(
                "A module with the same name already registered");
        }

        nameModule.put(name, instance);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getModule(String name) {
        return nameModule.get(name);
    }
}
