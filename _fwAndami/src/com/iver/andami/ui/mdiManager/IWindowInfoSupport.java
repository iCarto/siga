package com.iver.andami.ui.mdiManager;


public interface IWindowInfoSupport {

    /**
     * Devuelve la vista cuyo identificador es el parametro
     *
     * @param id Identificador de la vista que se quiere obtener
     *
     * @return La vista o null si no hay ninguna vista con ese identificador
     */
    public abstract IWindow getWindowById(int id);

    public abstract WindowInfo getWindowInfo(IWindow w);

}