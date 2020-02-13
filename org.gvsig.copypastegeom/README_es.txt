Introducción
===============

Este documento cubre la descripción de:

* Información de versión.

* Instrucciones de compilación.

* Instrucciones de empaquetado.

* Notas sobre internacionalización.


Información sobre versión
============================

    El número de versión está especificado en el fichero "install/intall.xml" 
    en el tag: <appversion>0.1.0</appversion>
    
    El número de build está especificado en el fichero "build.number" y se 
    puede cambiar manualmente o ejecutando el tag adecuado del "build.xml", 
    el cual por defecto se ejecuta y aumenta en uno el número de build.

Instrucciones de compilación
===============================

    El código es compatible con la 1.6 JVM.
    Configurar previamente un workspace con la versión 1.10 de gvSIG y en este workspace incorporar este proyecto.
    Para construir un workspace se puede ver estos enlaces:
    https://gvsig.org/web/docdev/docs/svn_article/construir-gvsig-desde-el-repositorio-svn
    https://gvsig.org/web/docdev/docs/desarrollo/comos/desarrollo-contra-binarios/como-desarrollar-contra-unos-binarios-de-gvsig-1.9
    
Instrucciones de empaquetado
===============================

    Para su ejecución desde el workspace se puede lanzar build.xml para generar 
    el empaquetado necesario dentro de _fwAndami, además al ejecutar este "build.xml" aumentamos en uno el número 
    del fichero "build.number"
    
    Para generar un empaquetado con el instalador de este plugin podemos 
    ejecutar el fichero ant "install/buildExt.xml". Al ejecutar esto no aumentamos el número de build ni nada.
    
    Después de generar un nuevo instalador tendremos que subir al SVN todos los cambios que tengamos en el proyecto.
    
    Para establecer un nuevo tag de versión del build el ant no hace nada al respecto, tendremos que hacer un Team/tags.. 
    y especificar el número de build como tag del proyecto.

Notas sobre internacionalización
===================================

* Donde se encuentran las cadenas de traducción.

    Las cadenas de traducción las tenemos dentro de los ficheros 
        "text*.properties" en el directorio "config"
        Los idiomas disponibles son:
        - Castellano
        - Inglés
        - Alemán 

* Que hay que hacer para incluir un nuevo idioma

  Para añadir un nuevo idioma, hay que crear un fichero con las claves 
        de cualquiera de los que existe ahora mismo con el nombre adecuado al 
        idioma, por ejemplo en java el Locator para alemán es "de", por tanto,
        el nombre será: "text_de.properties"

