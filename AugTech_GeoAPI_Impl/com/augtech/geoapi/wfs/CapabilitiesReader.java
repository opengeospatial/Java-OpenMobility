package com.augtech.geoapi.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLFilterImpl;

/** Very simple XML document reader that extracts the available SimpleFeatureType
 * names from a WFS getCapabilties document.
 * 
 * @author Augmented Technologies Ltd. 2015
 *
 */
public class CapabilitiesReader extends XMLFilterImpl {

	ParserAdapter contextAdapter = null;
	private String charEncoding = "UTF-8";
	boolean insideTypeList = false, insideType = false;
	private StringBuffer characters = new StringBuffer();
	Map<String, String> currentAttrs = null;
	List<String> typeNames = new ArrayList<String>();
	Map<String, String> featureType = new HashMap<String, String>();
	
	/** Construct a new instance of the reader and immediately parse the supplied inputStream.
	 * 
	 * Use {@link #getTypeNames()} to retrieve the output.
	 * 
	 * @param inputStream
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public CapabilitiesReader(InputStream inputStream)  throws SAXException, IOException, ParserConfigurationException {
		InputStream dStream = decompressStream( inputStream );
		//InputSource source = new InputSource( dStream );
		
		// Set-up the SAX parser and source
        SAXParser contextParser = SAXParserFactory.newInstance().newSAXParser();
        contextAdapter = new ParserAdapter(contextParser.getParser());
        contextAdapter.setContentHandler( this );
        contextAdapter.parse( new InputSource( dStream ) );
	}
	@Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		
		buildAttrs(atts);
		
		if (localName.equals("FeatureTypeList")) {
			
			insideTypeList = true;
			return;
			
		} else if (insideTypeList && localName.equals("FeatureType")) {
			
			insideType = true;
			featureType = new HashMap<String, String>();
			return;
			
		}
		
	}
	@Override
    public void endElement(String namespaceURI, String localName, String qName)
        	throws SAXException {
		
		String eleValue = getElementValue();
		
		if (localName.equals("FeatureTypeList")) {
			
			insideTypeList = false;
			return;
			
		} else if (localName.equals("FeatureType")) {
			
			insideType = false;
			// Create something additional?
			typeNames.add( featureType.get("Name") );
			
		} else if (insideType) {
			
			featureType.put(localName, eleValue);

		}
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
			characters.append(ch, start, length);
	}
	/** Get the trimmed String associated with the last element
	 * and reset the characters buffer
	 * 
	 * @return
	 */
	public String getElementValue() {
		if(characters.length() == 0) return "";
		String rawData = characters.toString().trim();
		characters.setLength(0);
		return rawData;
	}
	/** Store all attributes for the current element
	 * 
	 * @param atts
	 */
	private void buildAttrs(Attributes atts) {
		currentAttrs = new HashMap<String, String>();
		
		for (int i=0; i<atts.getLength(); i++) {
			String s = atts.getLocalName(i);
			if (s!=null) currentAttrs.put(s, atts.getValue(i));
		}
	}
	/**
	 * 
	 * @return
	 */
	public List<String> getTypeNames() {
		return this.typeNames;
	}
	/** Checks whether the InputStream is zipped, and if so returns the a 
	 * decompressed stream.
	 * 
	 * @param input
	 * @return A decompressed InputStream as required
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	private static InputStream decompressStream(InputStream input) throws IOException  {
		PushbackInputStream pb = new PushbackInputStream( input, 2 ); //we need a pushbackstream to look ahead
		byte [] signature = new byte[2];
		pb.read( signature ); //read the signature
		pb.unread( signature ); //push back the signature to the stream

		//check if matches standard gzip maguc number
		if( signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b ) {
			return new GZIPInputStream( pb );
		} else { 
			return pb;
		}
	}
}
