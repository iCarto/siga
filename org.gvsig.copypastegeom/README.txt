Introduction
===============

This document covers the description of:

* Version information.

* Build Instructions.

* Packaging Instructions.

* Notes on internationalization.


Version information
============================

    The version number is specified in the file "install / intall.xml"
    in the tag: <appversion> 0.1.0 </ appVersion>
    
    The build number is specified in the file "build.number" and
    can be changed manually or by running the proper tag "build.xml"
    which by default runs and an increase in the number of build.

Build Instructions
===============================

    The code is compatible with the 1.6 JVM.
    Previously set a workspace with version 1.10 of gvSIG and in this workspace include this project.
     To build a workspace you can see:
    https://gvsig.org/web/docdev/docs/svn_article/construir-gvsig-desde-el-repositorio-svn
    https://gvsig.org/web/docdev/docs/desarrollo/comos/desarrollo-contra-binarios/como-desarrollar-contra-unos-binarios-de-gvsig-1.9

Packaging Instructions
===============================

    To be run from the workspace can be launched to generate build.xml
    packaging required within _fwAndami also running this "build.xml" we increased the number by one
    File "build.number"
    
    To generate an installer packaged with this plugin we
    ant to execute the file "install / buildExt.xml." Running does not increase the number of build or anything.
    
    After generating a new installer will have to get on the SVN all the changes we have in the project.
    
    To establish a new version tag the ant build does nothing about it, we will have to make a Team / tags ..
    and specify the build number as the tag of the project.

Internationalization Notes
===================================

* Where are the translation strings.

    The translation strings within the files have
        "Text *. properties" in the "config"
        The languages available are:
        - Spanish
        - English
        - German

* What should I do to include a new language

  To add a new language, create a file with the keys
        of any that exists now with the appropriate name to
        language, for example in java the Locator for German is "de", thus
        the name will be "text_de.properties"