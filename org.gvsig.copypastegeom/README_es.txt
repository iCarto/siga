Introducci�n
===============

Este documento cubre la descripci�n de:

* Informaci�n de versi�n.

* Instrucciones de compilaci�n.

* Instrucciones de empaquetado.

* Notas sobre internacionalizaci�n.


Informaci�n sobre versi�n
============================

    El n�mero de versi�n est� especificado en el fichero "install/intall.xml" 
    en el tag: <appversion>0.1.0</appversion>
    
    El n�mero de build est� especificado en el fichero "build.number" y se 
    puede cambiar manualmente o ejecutando el tag adecuado del "build.xml", 
    el cual por defecto se ejecuta y aumenta en uno el n�mero de build.

Instrucciones de compilaci�n
===============================

    El c�digo es compatible con la 1.6 JVM.
    Configurar previamente un workspace con la versi�n 1.10 de gvSIG y en este workspace incorporar este proyecto.
    Para construir un workspace se puede ver estos enlaces:
    https://gvsig.org/web/docdev/docs/svn_article/construir-gvsig-desde-el-repositorio-svn
    https://gvsig.org/web/docdev/docs/desarrollo/comos/desarrollo-contra-binarios/como-desarrollar-contra-unos-binarios-de-gvsig-1.9
    
Instrucciones de empaquetado
===============================

    Para su ejecuci�n desde el workspace se puede lanzar build.xml para generar 
    el empaquetado necesario dentro de _fwAndami, adem�s al ejecutar este "build.xml" aumentamos en uno el n�mero 
    del fichero "build.number"
    
    Para generar un empaquetado con el instalador de este plugin podemos 
    ejecutar el fichero ant "install/buildExt.xml". Al ejecutar esto no aumentamos el n�mero de build ni nada.
    
    Despu�s de generar un nuevo instalador tendremos que subir al SVN todos los cambios que tengamos en el proyecto.
    
    Para establecer un nuevo tag de versi�n del build el ant no hace nada al respecto, tendremos que hacer un Team/tags.. 
    y especificar el n�mero de build como tag del proyecto.

Notas sobre internacionalizaci�n
===================================

* Donde se encuentran las cadenas de traducci�n.

    Las cadenas de traducci�n las tenemos dentro de los ficheros 
        "text*.properties" en el directorio "config"
        Los idiomas disponibles son:
        - Castellano
        - Ingl�s
        - Alem�n 

* Que hay que hacer para incluir un nuevo idioma

  Para a�adir un nuevo idioma, hay que crear un fichero con las claves 
        de cualquiera de los que existe ahora mismo con el nombre adecuado al 
        idioma, por ejemplo en java el Locator para alem�n es "de", por tanto,
        el nombre ser�: "text_de.properties"

