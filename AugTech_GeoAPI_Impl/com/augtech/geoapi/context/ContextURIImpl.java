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

import org.opengis.context.ContextURI;
/** An OWS Context specific URI holding mime-type and title information
 * as well as the URI. If an element in the document cannot contain these
 * additional elements then a standard URI is used.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContextURIImpl implements ContextURI {

	URI uri;
	String mimeType;
	String title;
	int length;
	
	/**
	 * 
	 * @param uri
	 * @param title
	 */
	public ContextURIImpl(String uri, String title) {
		this(uri, title, null, -1);
	}
	/**
	 * 
	 * @param uri
	 * @param title
	 * @param mimeType
	 * @param length
	 */
	public ContextURIImpl(String uri, String title, String mimeType, int length) {
		try {
			this.uri = uri==null ? null : new URI(uri);
		} catch (URISyntaxException e) {}
		this.title = title;
		this.mimeType = mimeType;
		this.length = length==0 ? -1 : 0;
	}
	/**
	 * 
	 * @param uri
	 * @param title
	 */
	public ContextURIImpl(URI uri, String title) {
		this(uri, title, null, -1);
	}
	/**
	 * 
	 * @param uri
	 * @param title
	 * @param mimeType
	 * @param length
	 */
	public ContextURIImpl(URI uri, String title, String mimeType, int length) {
		this.title = title;
		this.mimeType = mimeType;
		this.uri = uri;
		this.length = length==0 ? -1 : length;
	}
	@Override
	public String getKeyword() {
		return mimeType;
	}
	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getLength() {
		return length;
	}
	

	@Override
	public String toString() {
		return String.format("ContextURIImpl [title=%s, type=%s, uri=%s]", title, mimeType, uri);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ContextURIImpl))
			return false;
		ContextURIImpl other = (ContextURIImpl) obj;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}


	
}
