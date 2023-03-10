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
 * Class ActionTool.
 * 
 * @version $Revision$ $Date$
 */
public class ActionTool implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _text
     */
    private java.lang.String _text;

    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _actionCommand
     */
    private java.lang.String _actionCommand;

    /**
     * Field _icon
     */
    private java.lang.String _icon;

    /**
     * Field _last
     */
    private boolean _last;

    /**
     * keeps track of state for field: _last
     */
    private boolean _has_last;

    /**
     * Field _tooltip
     */
    private java.lang.String _tooltip;

    /**
     * Field _enableText
     */
    private java.lang.String _enableText;

    /**
     * Field _position
     */
    private int _position;

    /**
     * keeps track of state for field: _position
     */
    private boolean _has_position;


      //----------------/
     //- Constructors -/
    //----------------/

    public ActionTool() {
        super();
    } //-- com.iver.andami.plugins.config.generate.ActionTool()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteLast
     */
    public void deleteLast()
    {
        this._has_last= false;
    } //-- void deleteLast() 

    /**
     * Method deletePosition
     */
    public void deletePosition()
    {
        this._has_position= false;
    } //-- void deletePosition() 

    /**
     * Returns the value of field 'actionCommand'.
     * 
     * @return the value of field 'actionCommand'.
     */
    public java.lang.String getActionCommand()
    {
        return this._actionCommand;
    } //-- java.lang.String getActionCommand() 

    /**
     * Returns the value of field 'enableText'.
     * 
     * @return the value of field 'enableText'.
     */
    public java.lang.String getEnableText()
    {
        return this._enableText;
    } //-- java.lang.String getEnableText() 

    /**
     * Returns the value of field 'icon'.
     * 
     * @return the value of field 'icon'.
     */
    public java.lang.String getIcon()
    {
        return this._icon;
    } //-- java.lang.String getIcon() 

    /**
     * Returns the value of field 'last'.
     * 
     * @return the value of field 'last'.
     */
    public boolean getLast()
    {
        return this._last;
    } //-- boolean getLast() 

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'name'.
     */
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
     * Returns the value of field 'position'.
     * 
     * @return the value of field 'position'.
     */
    public int getPosition()
    {
        return this._position;
    } //-- int getPosition() 

    /**
     * Returns the value of field 'text'.
     * 
     * @return the value of field 'text'.
     */
    public java.lang.String getText()
    {
        return this._text;
    } //-- java.lang.String getText() 

    /**
     * Returns the value of field 'tooltip'.
     * 
     * @return the value of field 'tooltip'.
     */
    public java.lang.String getTooltip()
    {
        return this._tooltip;
    } //-- java.lang.String getTooltip() 

    /**
     * Method hasLast
     */
    public boolean hasLast()
    {
        return this._has_last;
    } //-- boolean hasLast() 

    /**
     * Method hasPosition
     */
    public boolean hasPosition()
    {
        return this._has_position;
    } //-- boolean hasPosition() 

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
     * Sets the value of field 'actionCommand'.
     * 
     * @param actionCommand the value of field 'actionCommand'.
     */
    public void setActionCommand(java.lang.String actionCommand)
    {
        this._actionCommand = actionCommand;
    } //-- void setActionCommand(java.lang.String) 

    /**
     * Sets the value of field 'enableText'.
     * 
     * @param enableText the value of field 'enableText'.
     */
    public void setEnableText(java.lang.String enableText)
    {
        this._enableText = enableText;
    } //-- void setEnableText(java.lang.String) 

    /**
     * Sets the value of field 'icon'.
     * 
     * @param icon the value of field 'icon'.
     */
    public void setIcon(java.lang.String icon)
    {
        this._icon = icon;
    } //-- void setIcon(java.lang.String) 

    /**
     * Sets the value of field 'last'.
     * 
     * @param last the value of field 'last'.
     */
    public void setLast(boolean last)
    {
        this._last = last;
        this._has_last = true;
    } //-- void setLast(boolean) 

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * Sets the value of field 'position'.
     * 
     * @param position the value of field 'position'.
     */
    public void setPosition(int position)
    {
        this._position = position;
        this._has_position = true;
    } //-- void setPosition(int) 

    /**
     * Sets the value of field 'text'.
     * 
     * @param text the value of field 'text'.
     */
    public void setText(java.lang.String text)
    {
        this._text = text;
    } //-- void setText(java.lang.String) 

    /**
     * Sets the value of field 'tooltip'.
     * 
     * @param tooltip the value of field 'tooltip'.
     */
    public void setTooltip(java.lang.String tooltip)
    {
        this._tooltip = tooltip;
    } //-- void setTooltip(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (com.iver.andami.plugins.config.generate.ActionTool) Unmarshaller.unmarshal(com.iver.andami.plugins.config.generate.ActionTool.class, reader);
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
