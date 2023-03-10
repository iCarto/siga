/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package com.iver.andami.config.generate;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Andami.
 * 
 * @version $Revision$ $Date$
 */
public class Andami implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _update
     */
    private boolean _update;

    /**
     * keeps track of state for field: _update
     */
    private boolean _has_update;


      //----------------/
     //- Constructors -/
    //----------------/

    public Andami() {
        super();
    } //-- com.iver.andami.config.generate.Andami()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteUpdate
     */
    public void deleteUpdate()
    {
        this._has_update= false;
    } //-- void deleteUpdate() 

    /**
     * Returns the value of field 'update'.
     * 
     * @return the value of field 'update'.
     */
    public boolean getUpdate()
    {
        return this._update;
    } //-- boolean getUpdate() 

    /**
     * Method hasUpdate
     */
    public boolean hasUpdate()
    {
        return this._has_update;
    } //-- boolean hasUpdate() 

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
     * Sets the value of field 'update'.
     * 
     * @param update the value of field 'update'.
     */
    public void setUpdate(boolean update)
    {
        this._update = update;
        this._has_update = true;
    } //-- void setUpdate(boolean) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (com.iver.andami.config.generate.Andami) Unmarshaller.unmarshal(com.iver.andami.config.generate.Andami.class, reader);
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
