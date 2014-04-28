/*
 * Copyright 2014, Augmented Technologies Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.augtech.geoapi.context.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.opengis.context.Author;
import org.opengis.context.Context;
import org.opengis.context.ContextDocumentReader;
import org.opengis.context.ContextFilter;
import org.opengis.context.ContextURI;
import org.opengis.context.CreatorApplication;
import org.opengis.context.CreatorDisplay;
import org.opengis.context.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLFilterImpl;

import com.augtech.geoapi.context.AuthorImpl;
import com.augtech.geoapi.context.ContextImpl;
import com.augtech.geoapi.context.ContextURIImpl;
import com.augtech.geoapi.context.ContextValueImpl;
import com.augtech.geoapi.context.CreatorApplicationImpl;
import com.augtech.geoapi.context.CreatorDisplayImpl;
import com.vividsolutions.jts.geom.Geometry;

/** A SAX Parser implementation for processing a Context document in
 * to the {@link Context} API classes.
 * A secondary SAX Parser implementation is created for 
 * processing 'WHERE' geometries in to valid JTS Geometry objects - 
 * the creation of this parser class must be implemented on the parent 
 * {@link ContextDocumentReader}
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContextFilterImpl extends XMLFilterImpl implements ContextFilter {
	ContextDocumentReader parent = null;
	Context contextDoc = null;
	EntryFilter entry = null;
	private StringBuffer characters = new StringBuffer();
	Map<String, Object> feedValues = new HashMap<String, Object>();
	String currDataType = "";
	boolean insideEntry, insideFeed, collectTags, insideWhere, insideDisplay;
	
	String srsName, lower, upper = "";
	int srsDimension = 2;
	Map<String, String> currentAttrs = null;
	List<Resource> resources = new ArrayList<Resource>();
	private Map<String, String> collectedValues = new HashMap<String, String>();
	private List<ContextURI> keywords = new ArrayList<ContextURI>();
	ContentBuffer contentBuffer = null;
	InputSource inputSource = null;
	ParserAdapter contextAdapter = null;
	ParserAdapter gmAdapter = null;
	private Map<String, Object> nextWhereGeom = null;
	private String charEncoding = "UTF-8";
	
	private Map<String, Map<String, String>> extensions = new HashMap<String, Map<String, String>>();
	/** Create a new SAX parser for processing the Context document.<p>
	 * Note that in order for WHERE Geometries to be created a valid SAX
	 * parser for GML must be implemented on the {@link ContextDocumentReader}
	 * 
	 * @param reader
	 * @param inStream
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public ContextFilterImpl(ContextDocumentReader reader) throws SAXException, ParserConfigurationException {
		parent = reader;
		entry = new EntryFilter(this);
		contentBuffer = new ContentBuffer( reader.getInputStream() );
		
		// Set-up the SAX parser and source
        SAXParser contextParser = SAXParserFactory.newInstance().newSAXParser();
        contextAdapter = new ParserAdapter(contextParser.getParser());
        contextAdapter.setContentHandler( this );
        inputSource = new InputSource( new BufferedReader(new InputStreamReader(contentBuffer)) );
        charEncoding = inputSource.getEncoding();
        
        // Geometry parser
        SAXParser gmlParser = SAXParserFactory.newInstance().newSAXParser();
        gmAdapter = new ParserAdapter(gmlParser.getParser());
        gmAdapter.setContentHandler( parent.getGeometryFilter() );
	}
	/** Process the document passed in the constructor. The {@link Context}
	 * document is passed back to the reader via 
	 * {@link ContextDocumentReader#setDocument(Context)
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	public void parse() throws IOException, SAXException {
		contextAdapter.parse(inputSource);
	}
	@Override
	public void parse(InputSource inSource) throws IOException, SAXException {
		parse();
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
			characters.append(ch, start, length);
	}
	
	/** New Geometry received from Geometry XML parser handler
	 * 
	 * @param geom
	 */
	public void setGeometry(Geometry geom) {
		if (geom!=null && nextWhereGeom!=null) nextWhereGeom.put("where", geom);
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public void buildWhereGeometry(Map<String, Object> valueMap) throws SAXException {
		nextWhereGeom = valueMap;
		// Create a new source to parse from the String
		StringBuffer sb = new StringBuffer();
		sb.append("<feed xmlns:gml=\"http://www.opengis.net/gml\">");
		sb.append( getContentBuffer("georss:where") );
		sb.append("</feed>");
		
		InputSource gSource = new InputSource( new StringReader( sb.toString() ) );
		// The new geometry will be received through #geometry()
		try {
			gmAdapter.parse( gSource );
		} catch (IOException e) {
			throw new SAXException(e);
		}

	}
	/** Get a set of namespaces that defines any extension elements within the
	 * context document to be populated on to relevant classes.
	 * 
	 * @return
	 */
	@Override
	public Set<String> getExtensionNameSpaces() {
		return parent.getExtensionNameSpaces();
	}
	@Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {

		if (insideEntry) {
			entry.startElement(namespaceURI, localName, qName, atts);
			return;
		}
		
		buildAttrs(atts);
		
		setExtensionAttrs(namespaceURI);
		
    	if(localName.equals(Context.TAG)) {
    		
    		feedValues = new HashMap<String, Object>();
    		insideFeed = true;
    		String lang = currentAttrs.get("lang");
    		if (lang!=null)
    			feedValues.put("language", new ContextValueImpl(lang, "text", String.class) );

    	} else if(localName.equals(Resource.TAG)) {
    		insideEntry = true;
    		// Pass straight to entry handler
    		entry.startElement(namespaceURI, localName, qName, atts);
    		
    	} else if(localName.equalsIgnoreCase("where")) {
    		
    		insideWhere = true;
    		contentBuffer.resetContentBuffer(qName);
    	} else {
    		
    		// The current data type
    		currDataType = currentAttrs.get("type")==null ? "text" : currentAttrs.get("type");
    			
			// Set flags for nested elements that require multiple values
    		if (localName.equals(Author.TAG) || localName.equals(CreatorDisplay.TAG)) {
    			collectTags = true;
    			collectedValues = new HashMap<String, String>();
    			insideDisplay = true;
    		}

    	}
    	
    }
	/** Add a new entry resource from the EntryFilter
	 * 
	 * @param entry
	 */
	@Override
	public void addEntry(Resource entry) {
		resources.add( entry );
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName)
        	throws SAXException {

		String eleValue = null;
		
		if (insideEntry) {
			entry.endElement(namespaceURI, localName, qName);
			if (localName.equals(Resource.TAG)) insideEntry = false;
			return;
		} else {
			eleValue = getElementValue();
			setExtension(namespaceURI, localName, eleValue);
		}

		
		// Now do specific tags
		if (localName.equals(Context.TAG)) {
			insideFeed = false;
			contextDoc = new ContextImpl(
					charEncoding, 
					feedValues, 
					resources, 
					keywords, 
					extensions.get(localName));
			// Document should now end!!
		} else if (localName.equals("where")) {
			insideWhere = false;
			buildWhereGeometry(feedValues);
		} else if (localName.equals("id")) {
			try {
				feedValues.put(localName, new URI(eleValue) );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else if (localName.equals("link")) {
			String rel = currentAttrs.get("rel");
			String len = currentAttrs.get("length");
			ContextURI curi = new ContextURIImpl(
					currentAttrs.get("href"),
					currentAttrs.get("title"), 
					currentAttrs.get("type"), 
					Integer.parseInt( len = len==null ? "-1" : len )
					);
			if (rel.equals("via")) {
				// This is meta-data
				feedValues.put("metaData", curi );
				
			} else if (rel.equals("profile")) {
				feedValues.put("specReference", curi );
			}
		} else if (localName.equals("category")) {
			// non-standard names
			keywords.add( new ContextURIImpl(
					currentAttrs.get("scheme"),
					currentAttrs.get("label"),
					currentAttrs.get("term"),
					-1) );
		} else if (localName.equals(Author.TAG)) {
			collectTags = false;
			insideDisplay = false;
			feedValues.put(localName, new AuthorImpl(collectedValues) );
		} else if (localName.equals(CreatorDisplay.TAG)) {
			collectTags = false;
			insideDisplay = false;
			feedValues.put(localName, new CreatorDisplayImpl(collectedValues, extensions.get(localName) ) );
			resetExtensions(localName);
		} else if (localName.equals(CreatorApplication.TAG)) {
			feedValues.put(localName, new CreatorApplicationImpl(
					currentAttrs.get("uri"),
					currentAttrs.get("title"),
					currentAttrs.get("version"))
					);
		} else {
			
			if (insideWhere || eleValue.equals("")) return;
			
			/* Do we need to collect sub-elements in order to build
			 * an entire object? */
			if (collectTags) {
				collectedValues.put(localName, eleValue);
			} else { // All other standard feed values
				// Try to decode at least String or number classes for the feed value
				Class<?> clazz = String.class;
			    try {
			    	Integer.valueOf(eleValue);
			    	clazz = Integer.class;
			    } catch (NumberFormatException e1) {
			        try {
			        	Double.valueOf(eleValue);
			        	clazz = Double.class;
			        } catch (NumberFormatException e2) {/* Will use default of String */ }

			    }
			    feedValues.put(localName, new ContextValueImpl(eleValue, currDataType, clazz) );

			}
		}
		
    }
	@Override
	public void resetContentBuffer(String qName) {
		contentBuffer.resetContentBuffer(qName);
	}
	@Override
	public boolean hasContentInBuffer() {
		return contentBuffer.hasContent();
	}
	/** Get all buffered content up to and including the supplied tag.<p>
	 * Note that content will be reset (cleared) up to the same tag name.
	 * To get content from the buffer correctly, ensure it is reset 
	 * on a starting tag.
	 * 
	 * @param qName The qualified tag name
	 * @return
	 */
	@Override
	public String getContentBuffer(String qName) {
		String ret = "";
		if (contentBuffer.hasContent()) ret = contentBuffer.getBuffer( qName );
		return ret;
	}
	/** Get the trimmed String associated with the last element
	 * and reset the characters buffer
	 * 
	 * @return
	 */
	@Override
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
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		parent.setDocument(contextDoc);
	}
	/**
	 * 
	 * @param localName
	 */
	private void resetExtensions(String localName) {
		if (extensions.get(localName)==null) return;
		extensions.put(localName, new HashMap<String, String>() );
	}
	
	private String getExtType() {
		String type = null;
		if (insideEntry) {
			type = null;
		} else if (insideDisplay) {
			type = CreatorDisplay.TAG;
		} else {
			type = Context.TAG;
		}
		return type;
	}
	private boolean isExtension(String namespaceURI) {
		return 	parent.getExtensionNameSpaces()!=null && 
				parent.getExtensionNameSpaces().contains(namespaceURI);
	}
	/**
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param data
	 */
	private void setExtension(String namespaceURI, String localName, String data) {
		if (!isExtension(namespaceURI)) return;
		
		String type = getExtType();
		if (type==null) return;
		
		if (extensions.get(type)==null) extensions.put(type, new HashMap<String, String>() );
		extensions.get(type).put(localName, data);

	}
	/**
	 * 
	 * @param namespaceURI
	 */
	private void setExtensionAttrs(String namespaceURI) {
		if (!isExtension(namespaceURI)) return;
		
		String type = getExtType();
		if (type==null) return;
		
		if (extensions.get(type)==null) extensions.put(type, new HashMap<String, String>() );
		if (type!=null) extensions.get(type).putAll( currentAttrs );
		
	}
	
}
