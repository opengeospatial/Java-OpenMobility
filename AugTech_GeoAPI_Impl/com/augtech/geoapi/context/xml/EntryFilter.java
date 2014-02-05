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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.context.Author;
import org.opengis.context.Content;
import org.opengis.context.ContextFilter;
import org.opengis.context.ContextURI;
import org.opengis.context.Folder;
import org.opengis.context.Offering;
import org.opengis.context.Operation;
import org.opengis.context.Resource;
import org.opengis.context.StyleSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.augtech.geoapi.context.AuthorImpl;
import com.augtech.geoapi.context.ContentImpl;
import com.augtech.geoapi.context.ContextURIImpl;
import com.augtech.geoapi.context.ContextValueImpl;
import com.augtech.geoapi.context.FolderImpl;
import com.augtech.geoapi.context.OfferingImpl;
import com.augtech.geoapi.context.OperationImpl;
import com.augtech.geoapi.context.ResourceImpl;
import com.augtech.geoapi.context.StyleSetImpl;
/** A SAX XMLFilterImpl specifically for Context Document <Entry>'s
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class EntryFilter extends XMLFilterImpl {
	ContextFilter parent = null;
	String rawAttribute = "";
	Map<String, Object> entryValues = new HashMap<String, Object>();
	private List<ContextURI> keywords = new ArrayList<ContextURI>();
	private List<Content> offerContents = new ArrayList<Content>();
	private List<Offering> offerings  = new ArrayList<Offering>();
	private List<Operation> operations = new ArrayList<Operation>();
	private Map<String, Object> operationAttrs = new HashMap<String, Object>();
	Map<String, String> currentAttrs = null;
	private Map<String, String> collected = new HashMap<String, String>();
	private String offeringCode = "";
	private StyleSet currentStyle;
	boolean insideAuthor, insideWhere, insideOffering, defaultStyle;
	boolean insideOperation, insideStyle, insideContent, insideEntry;
	boolean insideCreator, insideCreatorDisplay;
	private String contentType, currentType;
	
	private Map<String, Map<String, String>> extensions = new HashMap<String, Map<String, String>>();

	/** A XML filter specifically for Context Document <Entry>'s
	 * 
	 * @param filterDoc The parent document (Feed)
	 */
	public EntryFilter(ContextFilter filterDoc) {
		this.parent = filterDoc;
	}

	
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		String eleValue = parent.getElementValue();
		setExtension(namespaceURI, localName, eleValue);
		
		
		if (localName.equals(Resource.TAG)) {
			insideEntry = false;
			parent.addEntry( new ResourceImpl(
					entryValues, 
					offerings, 
					keywords, 
					extensions.get(Resource.TAG)) 
					);
			resetExtensions(localName);
			return;
		}
		
		// Now do specific tags
		if (localName.equals(Author.TAG)) {
			insideAuthor = false;
			entryValues.put(localName, new AuthorImpl(collected) );
		} else if (localName.equals("id")) {
			try {
				entryValues.put(localName, new URI(eleValue) );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else if (localName.equals("where")) {
			insideWhere = false;
			parent.buildWhereGeometry(entryValues);
		} else if (localName.equalsIgnoreCase("category")) {
			
			String scheme = currentAttrs.get("scheme");
			
			if (scheme.toLowerCase().endsWith("active")) {
				
				entryValues.put("active", Boolean.parseBoolean(currentAttrs.get("term")) );
			
			} else if (scheme.endsWith(Folder.TAG)) {
				// Ambiguous in current spec, so try both
				String t = currentAttrs.get("label");
				if (t==null) t = currentAttrs.get("title");
				entryValues.put("folder", new FolderImpl(currentAttrs.get("term"), t) );
				
			} else if (scheme.toLowerCase().endsWith("category")) {
				
				// non-standard names
				keywords.add( new ContextURIImpl(
						currentAttrs.get("uri"),
						currentAttrs.get("label"),
						currentAttrs.get("term"),
						-1) );
			}
		} else if (localName.equals(StyleSet.TAG)) {
			insideStyle = false;
			currentStyle = new StyleSetImpl(collected, extensions.get(StyleSet.TAG) );
			resetExtensions(localName);
		} else if (localName.equals(Offering.TAG)) {
			insideOffering = false;
			offerings.add( new OfferingImpl(
					offeringCode, 
					operations, 
					offerContents, 
					currentStyle,
					extensions.get(Offering.TAG))
					);
			resetExtensions(localName);
		} else if (localName.equals(Operation.TAG)) {
			insideOperation = false;
			operations.add( new OperationImpl(operationAttrs, extensions.get(Operation.TAG)) );
			resetExtensions(localName);
		} else if (localName.equals("request") || localName.equals("result")) {
			if ( parent.hasContentInBuffer() ) {
				operationAttrs.put(localName, new ContentImpl(parent.getContentBuffer(qName), currentType) );
			} else {
				try {
					String uri = currentAttrs.get("uri");
					if (uri!=null) {
						operationAttrs.put(localName, new ContentImpl(new URI( uri ), currentType) );
					}
				} catch (URISyntaxException e) {}
			}

		} else if (localName.equals(Content.TAG)) {
			insideContent = false;
			Content content = null; 
			// Strip <content> tags if we have data between tags
			if (!eleValue.equals("") || currentAttrs.get("href")==null) {
				String sCon = parent.getContentBuffer(qName);
				sCon = sCon.substring(sCon.indexOf(">")+1);
				int idx = sCon.lastIndexOf("</") > -1 ? sCon.lastIndexOf("</") : sCon.length();
				sCon = sCon.substring(0, idx);
				content = new ContentImpl(sCon, contentType, extensions.get(Content.TAG));
			} else {
				//Otherwise has to be by reference using href
				String uri = currentAttrs.get("href");
				try {
					content = new ContentImpl(new URI(uri), contentType);
				} catch (URISyntaxException e) {}
				parent.getContentBuffer("????");// We need to clear beyond the previously reset 'content' tag 
			}

			resetExtensions(localName);
			
			if ( insideOffering ) {
				offerContents.add( content );
			} else {
				// Need to do as the content elseif will stop storage happening further down
				if (!eleValue.equals("")) entryValues.put(localName,  content );
			}
		} else if (localName.equals("link")) {
			
			String type = currentAttrs.get("rel");
			
			if (type.equals("enclosure")) {
				// ContentByRef
				entryValues.put("contentByRef", buildContextURI() );
			} else if (type.equals("icon")) {
				// Preview
				entryValues.put("preview", buildContextURI() );
			} else if (type.equals("via")) {
				// resourceMetadata
				entryValues.put("metaData", buildContextURI() );
			} else if (type.equals("alternate")) {
				// Description
				entryValues.put("description", buildContextURI() );
			}
			
		} else {
			// All other 'standard' elements
			collectOrStoreValues(localName, qName, eleValue);
		}
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName,
			Attributes atts) throws SAXException {

		buildAttrs(atts);
		
		setExtensionAttrs(namespaceURI);
		
		currentType = currentAttrs.get("type");
		
		if (localName.equals(Resource.TAG)) {
			entryValues = new HashMap<String, Object>();
			offerings = new ArrayList<Offering>();
			keywords = new ArrayList<ContextURI>();
			insideEntry = true;
		} else if (localName.equals(Content.TAG)) {
			insideContent = true;
			contentType = currentType;
			parent.resetContentBuffer(qName);
		} else if (localName.equals(Author.TAG)) {
			insideAuthor = true;
			collected = new HashMap<String, String>();
		} else if (localName.equals(Offering.TAG)) {
			insideOffering = true;
			offeringCode = currentAttrs.get("code");
			operations = new ArrayList<Operation>();
			offerContents = new ArrayList<Content>();
			currentStyle = null;
		} else if (localName.equals(Operation.TAG)) {
			operationAttrs = new HashMap<String, Object>();
			operationAttrs.putAll(currentAttrs);
			insideOperation = true;
			parent.resetContentBuffer(qName);
		} else if (localName.equals(StyleSet.TAG)) {
			insideStyle = true;
			collected = new HashMap<String, String>();
			String s = currentAttrs.get("default");
			defaultStyle = s!=null ? Boolean.valueOf(s) : false;
			parent.resetContentBuffer(qName);
		} else if (localName.equalsIgnoreCase("where")) {
			insideWhere = true;
			parent.resetContentBuffer(qName);
		}
		
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
	/** Either store the current element data on the entry values map or
	 * collect sub elements for object creation during {@link #endElement(String, String, String)}
	 * 
	 * @param localName
	 * @param qName
	 * @param rawData
	 */
	private void collectOrStoreValues(String localName, String qName, String rawData) {
		
		if (insideWhere || insideContent || insideOperation) return;// Data taken from buffer
		
		if (insideAuthor) {
			// URI will go in as String
			collected.put(localName, rawData);
			
		} else if (insideStyle) {
			if (localName.equalsIgnoreCase("abstract") ) {
				collected.put("abstractType", currentType );
				collected.put(localName, rawData);
			} else if (localName.equals(Content.TAG) ) {
				collected.put("contentType", currentType );
				if (currentAttrs.get("href")!=null) {
					collected.put("href", currentAttrs.get("href") );
				} else {
					// inline
					collected.put(localName, parent.getContentBuffer(qName) );
				}
			} else {
				collected.put(localName, rawData);
			}
			
		} else {
			// All other standard feed values
			Class<?> clazz = String.class;
		    try {
		    	Integer.valueOf(rawData);
		    	clazz = Integer.class;
		    } catch (NumberFormatException e1) {
		        try {
		        	Double.valueOf(rawData);
		        	clazz = Double.class;
		        } catch (NumberFormatException e2) {/* Will use default of String */ }

		    }
			entryValues.put(localName, new ContextValueImpl(rawAttribute, currentType, clazz) );

		}
	}

	/**
	 * 
	 * @return
	 */
	private ContextURI buildContextURI() {
		String len = currentAttrs.get("length");
		len = len==null ? "-1" : len;
		return new ContextURIImpl(
				currentAttrs.get("href"),
				currentAttrs.get("title"), 
				currentAttrs.get("type"), 
				Integer.parseInt( len )
				);
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
	private String getExtType() {
		String type = null;
		// Have to be reverse order of possible nesting
		if (insideContent) {
			type = Content.TAG;
		} else if (insideOperation) {
			type = Operation.TAG;
		} else if (insideOffering) {
			type = Offering.TAG;
		} else if (insideStyle) {
			type = StyleSet.TAG;
		} else if (insideEntry) {
			type = Resource.TAG;
		}
		return type;
	}
	private boolean isExtension(String namespaceURI) {
		return 	parent.getExtensionNameSpaces()!=null && 
				parent.getExtensionNameSpaces().contains(namespaceURI);
	}
	/**
	 * 
	 * @param localName
	 */
	private void resetExtensions(String localName) {
		if (extensions.get(localName)==null) return;
		extensions.put(localName, new HashMap<String, String>() );
	}
}
