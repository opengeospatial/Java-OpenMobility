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
package com.augtech.geoapi.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opengis.context.Content;
import org.opengis.context.Offering;
import org.opengis.context.Operation;
import org.opengis.context.StyleSet;

/** An OWS Context Offering
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class OfferingImpl implements Offering {
	URI code;
	List<Operation> operations;
	List<Content> contents;
	StyleSet styleSet;
	Map<String, String> extensions = new HashMap<String, String>();
	
	/**
	 * 
	 * @param code
	 * @param operations
	 */
	public OfferingImpl(String code, List<Operation> operations) {
		this(code, operations, null, null, null);
	}
	/**
	 * 
	 * @param uri
	 * @param operations
	 * @param contents
	 * @param styleSet
	 */
	public OfferingImpl(String uri, List<Operation> operations, List<Content> contents, StyleSet styleSet) {
		this(uri, operations, contents, styleSet, null);
	}
	/**
	 * 
	 * @param uri
	 * @param operations
	 * @param contents
	 * @param styleSet
	 * @param extensions
	 */
	public OfferingImpl(String uri, List<Operation> operations, List<Content> contents, StyleSet styleSet,
			Map<String, String> extensions) {
		try {
			this.code = uri==null ? null : new URI(uri);
		} catch (URISyntaxException e) {}
		this.operations = operations;
		this.styleSet = styleSet;
		this.contents = contents;
		
		if(extensions!=null) this.extensions = extensions;
	}
	@Override
	public int getID() {

		if ( isService() ) {
			// Shouldn't happen with these services
			if (operations==null) return -1;

			// Use the hash code of the first GetCapabilities request URL
			for (Operation o : operations) {
				if (o.getCode().equals("GetCapabilities")) {
					return o.getURI().hashCode();
				}
			}
			
		} else {
			/* GML, KML, GEOTIFF, GMLJP2 and GMLCOV have 
			 * Inline content or Local reference */
			if (contents.size()>0) {
				return contents.get(0).hashCode();	
			}
		}
		
		return UUID.randomUUID().toString().hashCode();
	}
	@Override
	public boolean isService() {
		String code = getCode().toString();
		code = code.substring(code.lastIndexOf("/")+1).toUpperCase();
		
		return 	code.equals("WFS") || code.equals("WMS") || code.equals("WMTS") ||
				code.equals("CSW") || code.equals("WPS");
	}
	@Override
	public URI getCode() {
		return code;
	}

	@Override
	public List<Operation> getOperations() {
		return operations;
	}

	@Override
	public List<Content> getContents() {
		return contents;
	}

	@Override
	public StyleSet getStyleset() {
		return styleSet;
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}
	
	@Override
	public String toString() {
		return String.format("OfferingImpl [code=%s]", code);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((contents == null) ? 0 : contents.hashCode());
		result = prime * result
				+ ((operations == null) ? 0 : operations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OfferingImpl))
			return false;
		OfferingImpl other = (OfferingImpl) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (contents == null) {
			if (other.contents != null)
				return false;
		} else if (!contents.equals(other.contents))
			return false;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}
	@Override
	public int getOperationCount() {
		return operations==null ? 0 : operations.size();
	}
	@Override
	public int getContentsCount() {
		return contents==null ? 0 : contents.size();
	}


}
