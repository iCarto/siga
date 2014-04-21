
package org.gvsig.remoteClient.wms.wms_1_1_1;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.TreeMap;

import org.gvsig.remoteClient.utils.CapabilitiesTags;
import org.gvsig.remoteClient.utils.EncodingXMLParser;
import org.gvsig.remoteClient.utils.ExceptionTags;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * <p>
 * Describes the handler to comunicate to a WMS 1.1.1
 * </p>
 */
public class WMSProtocolHandler1_1_1 extends org.gvsig.remoteClient.wms.WMSProtocolHandler {
	private WMSLayer1_1_1 fakeRootLayer;

	public WMSProtocolHandler1_1_1()
	{
		this.version = "1.1.1";
		this.name = "WMS1.1.1";
		this.serviceInfo = new ServiceInformation();
		this.layers = new TreeMap();
	}

//------------------------------------------------------------------------------
// Parsing methods....
//------------------------------------------------------------------------------
/**
 * <p>Parse the xml data retrieved from the WMS, it will parse the WMS Capabilities</p>
 *
 */
    public void parse(File f)
    {
    	rootLayer = null;
    	rootLayer = null;
    	int tag;
    	EncodingXMLParser kxmlParser = null;
    	kxmlParser = new EncodingXMLParser();
    	try
    	{
//    		FileReader reader = new FileReader(f);
//    		BufferedReader br = new BufferedReader(reader);
//
//    		 // patch for ArcIMS + WMS connector > 9.0 bug
//    		char[] buffer = new char[(int) f.length()];
//    		br.read(buffer);
//    		String string = new String(buffer);
//    		int a = string.toLowerCase().indexOf("<?xml");
//            if (a !=-1) {
//            	string = string.substring(a, string.length());
//            	kxmlParser.setInput(string);
//            } else
            	// end patch
            	kxmlParser.setInput(f);

			kxmlParser.nextTag();
    		if ( kxmlParser.getEventType() != KXmlParser.END_DOCUMENT )
    		{
    			kxmlParser.require(KXmlParser.START_TAG, null, CapabilitiesTags.CAPABILITIES_ROOT1_1_1);
    			tag = kxmlParser.nextTag();
				 while(tag != KXmlParser.END_DOCUMENT)
				 {
                     switch(tag)
					 {

						case KXmlParser.START_TAG:
							if (kxmlParser.getName().compareTo(CapabilitiesTags.SERVICE )==0)
							{
								parseServiceTag(kxmlParser);
							}
							else if (kxmlParser.getName().compareTo(CapabilitiesTags.CAPABILITY)==0)
							{
								parseCapabilityTag(kxmlParser);
							}
							break;
						case KXmlParser.END_TAG:
							break;
						case KXmlParser.TEXT:
							//System.out.println("[TEXT]["+kxmlParser.getText()+"]");
						break;
					 }
    				 tag = kxmlParser.next();
    			 }
    			kxmlParser.require(KXmlParser.END_DOCUMENT, null, null);
    		}
    	}
    	catch(XmlPullParserException parser_ex){
    		parser_ex.printStackTrace();
    	}
   		catch (IOException ioe) {
   			ioe.printStackTrace();
		} finally {

        }
   		// In the parsing process the layer has been filled
    }

    /**
     * <p>Parses the Service Information </p>
     */
    private void parseServiceTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.SERVICE);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.NAME)==0)
					{
						serviceInfo.name = parser.nextText();
					}
					else if (parser.getName().compareTo(CapabilitiesTags.TITLE)==0)
					{
						serviceInfo.title = parser.nextText();
					}
					else if (parser.getName().compareTo(CapabilitiesTags.ABSTRACT)==0)
					{
						serviceInfo.abstr = parser.nextText();
					}
					else if (parser.getName().compareTo(CapabilitiesTags.ONLINERESOURCE)==0)
					{
				    	String value = new String();
				        value = parser.getAttributeValue("", CapabilitiesTags.XLINK_HREF);
				        if (value != null){
				        	serviceInfo.online_resource = value;
				        }
					}
					else if ((parser.getName().compareTo(CapabilitiesTags.KEYWORDLIST)==0) ||
							(parser.getName().compareTo(CapabilitiesTags.CONTACTINFORMATION)==0))
					{
						parser.skipSubTree();
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.SERVICE) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
             if (!end)
                 currentTag = parser.next();
    	}
    	parser.require(KXmlParser.END_TAG, null, CapabilitiesTags.SERVICE);
    }
    /**
     * <p>Parses the Capability Tag </p>
     */
    private void parseCapabilityTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.CAPABILITY);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.REQUEST)==0)
					{
						parseRequestTag(parser);
					}
					else if (parser.getName().compareTo(CapabilitiesTags.EXCEPTION)==0)
					{
						//TODO:
						//Add to serviceInfo the supported formats for the exceptions????
					}
					else if (parser.getName().compareTo(CapabilitiesTags.LAYER)==0)
					{
						WMSLayer1_1_1 lyr = new WMSLayer1_1_1();
						lyr.parse(parser, layers);

                        if (rootLayer == null)
                            rootLayer = lyr;
                        else {
                            // Handles when there is no general root layer, will use
                            // a fake non-queryable one.
                            if (!rootLayer.equals(getFakeRootLayer())){
                                WMSLayer1_1_1 aux = (WMSLayer1_1_1) rootLayer;
                                rootLayer  = getFakeRootLayer();
                                rootLayer.getChildren().add(aux);
                            }
                            rootLayer.getChildren().add(lyr);
                        }

                        if (lyr.getName()!=null)
						    layers.put(lyr.getName(), lyr);

//                        Collection layerCollection = layers.values();
//                        Iterator iter = layerCollection.iterator();
//                        while (iter.hasNext())
//                        {
//                        	WMSLayer1_1_1 layer = (WMSLayer1_1_1)iter.next();
//    						//Add all the SRS that the layer supports to the WMSProtocolHandler if they dont exist already
//    						for (i=0;i<layer.getAllSrs().size();i++)
//    						{
////    						    if (!layer.srs.contains(layer.getAllSrs().elementAt(i)))
////    						    {
////    						        this.srs.add(layer.getAllSrs().elementAt(i));
////    						    }
//    						}
//                        }
					}
					else if ((parser.getName().compareTo(CapabilitiesTags.VENDORSPECIFICCAPABILITIES)==0) ||
							(parser.getName().compareTo(CapabilitiesTags.USERDEFINEDSYMBOLIZATION )==0))

					{
						parser.skipSubTree();
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.CAPABILITY) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
			 if (!end)
				 currentTag = parser.next();
    	}
    	//parser.require(KXmlParser.END_TAG, null, CapabilitiesTags.CAPABILITY);
    }

    /**
     * <p>Parses the Request tag </p>
     */
    private void parseRequestTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.REQUEST);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.GETCAPABILITIES)==0)
					{
						serviceInfo.operations.put(CapabilitiesTags.GETCAPABILITIES, null);
					}
					else if (parser.getName().compareTo(CapabilitiesTags.GETMAP)==0)
					{
						// put a null to this key?
						// or leave it like it was with a CODE in a vector specifying this operation.
						// WMSProtocolHandler.GETMAP_OPERATION
						serviceInfo.operations.put(CapabilitiesTags.GETMAP, null);
						parseGetMapTag(parser);
					}
					else if (parser.getName().compareTo(CapabilitiesTags.GETFEATUREINFO)==0)
					{
						//serviceInfo.operations.put(WMSProtocolHandler.GETFEATUREINFO_OPERATION)
						serviceInfo.operations.put(CapabilitiesTags.GETFEATUREINFO, null);
						parseGetFeatureInfoTag(parser);
					}
					else if (parser.getName().compareTo(CapabilitiesTags.DESCRIBELAYER)==0)
					{
						//serviceInfo.operations.put(WMSProtocolHandler.DESCRIBELAYER_OPERATION)
						serviceInfo.operations.put(CapabilitiesTags.DESCRIBELAYER, null);
					}
					else if (parser.getName().compareTo(CapabilitiesTags.GETLEGENDGRAPHIC)==0)
					{
						serviceInfo.operations.put(CapabilitiesTags.GETLEGENDGRAPHIC, null);
						parseGetLegendGraphicTag(parser);
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.REQUEST) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
			 if(!end)
				 currentTag = parser.next();
    	}
    	// TODO: does not get such a tag when arrives here!!!!!!
    	//parser.require(KXmlParser.END_TAG, null, CapabilitiesTags.REQUEST);
    }

    /**
     * <p>Parses the GetMap tag </p>
     */
    private void parseGetMapTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.GETMAP);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.FORMAT)==0)
					{
						serviceInfo.formats.add(parser.nextText());
					}
					else if (parser.getName().compareTo(CapabilitiesTags.DCPTYPE)==0)
					{
						currentTag = parser.nextTag();
						if(parser.getName().compareTo(CapabilitiesTags.HTTP)==0)
						{
							currentTag = parser.nextTag();
							if(parser.getName().compareTo(CapabilitiesTags.GET)==0)
							{
								currentTag = parser.nextTag();
								if (parser.getName().compareTo(CapabilitiesTags.ONLINERESOURCE)==0)
								{
									String value = new String();
									value = parser.getAttributeValue("", CapabilitiesTags.XLINK_HREF);
									if (value != null){
										serviceInfo.operations.put(CapabilitiesTags.GETMAP, value);
									}
								}
							}
						}
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.GETMAP) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
			 if(!end)
				 currentTag = parser.next();
    	}
    }

    /**
     * <p>Parses the GetFeatureInfoTag tag </p>
     */
    private void parseGetFeatureInfoTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.GETFEATUREINFO);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.FORMAT)==0)
					{
						// add the supported formats by the GetFeatureInfo request
						serviceInfo.infoFormats.add(parser.nextText());
					}
					else if (parser.getName().compareTo(CapabilitiesTags.DCPTYPE)==0)
					{
						currentTag = parser.nextTag();
						if(parser.getName().compareTo(CapabilitiesTags.HTTP)==0)
						{
							currentTag = parser.nextTag();
							if(parser.getName().compareTo(CapabilitiesTags.GET)==0)
							{
								currentTag = parser.nextTag();
								if (parser.getName().compareTo(CapabilitiesTags.ONLINERESOURCE)==0)
								{
									String value = new String();
									value = parser.getAttributeValue("", CapabilitiesTags.XLINK_HREF);
									if (value != null){
										serviceInfo.operations.put(CapabilitiesTags.GETFEATUREINFO, value);
									}
								}
							}
						}
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.GETFEATUREINFO) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
			 if(!end)
				 currentTag = parser.next();
    	}
    }

    /**
     * <p>Parses the GetFeatureInfoTag tag </p>
     */
    private void parseGetLegendGraphicTag(KXmlParser parser) throws IOException, XmlPullParserException
    {
    	int currentTag;
    	boolean end = false;

    	parser.require(KXmlParser.START_TAG, null, CapabilitiesTags.GETLEGENDGRAPHIC);
    	currentTag = parser.next();

    	while (!end)
    	{
			 switch(currentTag)
			 {
				case KXmlParser.START_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.FORMAT)==0)
					{
						//TODO:
						// add the supported formats by the GetLegendGraphic request
						//serviceInfo.formats.add(parser.nextText());
					}
					else if (parser.getName().compareTo(CapabilitiesTags.DCPTYPE)==0)
					{
						currentTag = parser.nextTag();
						if(parser.getName().compareTo(CapabilitiesTags.HTTP)==0)
						{
							currentTag = parser.nextTag();
							if(parser.getName().compareTo(CapabilitiesTags.GET)==0)
							{
								currentTag = parser.nextTag();
								if (parser.getName().compareTo(CapabilitiesTags.ONLINERESOURCE)==0)
								{
									String value = new String();
									value = parser.getAttributeValue("", CapabilitiesTags.XLINK_HREF);
									if (value != null){
										serviceInfo.operations.put(CapabilitiesTags.GETLEGENDGRAPHIC, value);
									}
								}
							}
						}
					}
					break;
				case KXmlParser.END_TAG:
					if (parser.getName().compareTo(CapabilitiesTags.GETLEGENDGRAPHIC) == 0)
						end = true;
					break;
				case KXmlParser.TEXT:
				break;
			 }
			 if(!end)
				 currentTag = parser.next();
    	}
    }


    private WMSLayer1_1_1 getFakeRootLayer(){
        if (fakeRootLayer == null){
            fakeRootLayer = new WMSLayer1_1_1();
            fakeRootLayer.setTitle(serviceInfo.title);
            fakeRootLayer.setQueryable(false);
            fakeRootLayer.setName(null);
        }
        return fakeRootLayer;
    }
    /* (non-Javadoc)
     * @see org.gvsig.remoteClient.wms.WMSProtocolHandler#parseException(byte[])
     */
    protected String parseException(byte[] data) {
        ArrayList errors = new ArrayList();
        KXmlParser kxmlParser = new KXmlParser();
        try
        {
            kxmlParser.setInput(new ByteArrayInputStream(data), encoding);
            int tag;

            boolean end = false;
            tag = kxmlParser.nextTag();

            //Comentar temporalmente para subsanar el hecho de que SimonCit me devuelve las capabilities en un GetLegendGraphic!!!
            kxmlParser.require(KXmlParser.START_TAG, null, ExceptionTags.EXCEPTION_ROOT);

            while (!end)
            {
                switch(tag)
                {
                    case KXmlParser.START_TAG:
                            if (kxmlParser.getName().compareTo(ExceptionTags.SERVICE_EXCEPTION)==0){
                                String errorCode = kxmlParser.getAttributeValue("", ExceptionTags.CODE);
                                errorCode = (errorCode != null) ? "["+errorCode+"] " : "";
                                String errorMessage = kxmlParser.nextText();
                                errors.add(errorCode+errorMessage);
                            }
                            break;
                    case KXmlParser.END_TAG:
                            if (kxmlParser.getName().compareTo(ExceptionTags.EXCEPTION_ROOT) == 0)
                                end = true;
                            break;
                     }
                	if (!end)
                	{
                		tag = kxmlParser.nextTag();
                	}
                 }
        	}
        catch(XmlPullParserException parser_ex){
            System.out.println(parser_ex.getMessage());
            parser_ex.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        String message = errors.size()>0? "" : null;
        for (int i = 0; i < errors.size(); i++) {
            message += (String) errors.get(i)+"\n";
        }
        return message;
    }

  }
