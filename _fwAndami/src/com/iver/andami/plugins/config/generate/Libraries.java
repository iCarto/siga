/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package com.iver.andami.plugins.config.generate;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class Libraries.
 * 
 * @version $Revision$ $Date$
 */
public class Libraries implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _libraryDir
     */
    private java.lang.String _libraryDir;


      //----------------/
     //- Constructors -/
    //----------------/

    public Libraries() {
        super();
    } //-- com.iver.andami.plugins.config.generate.Libraries()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'libraryDir'.
     * 
     * @return the value of field 'libraryDir'.
     */
    public java.lang.String getLibraryDir()
    {
        return this._libraryDir;
    } //-- java.lang.String getLibraryDir() 

    /**
     * Method isValid
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'libraryDir'.
     * 
     * @param libraryDir the value of field 'libraryDir'.
     */
    public void setLibraryDir(java.lang.String libraryDir)
    {
        this._libraryDir = libraryDir;
    } //-- void setLibraryDir(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (com.iver.andami.plugins.config.generate.Libraries) Unmarshaller.unmarshal(com.iver.andami.plugins.config.generate.Libraries.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
