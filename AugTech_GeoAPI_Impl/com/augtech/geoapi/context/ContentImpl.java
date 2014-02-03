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
import java.util.Map;

import org.opengis.context.Content;

/** Content is either in-line content or a URI reference to on-line
 * content that has a specific mime-type.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContentImpl implements Content {
	String type;
	URI uri;
	String content;
	Map<String, String> extensions;
	/**
	 * 
	 * @param uri
	 * @param type
	 */
	public ContentImpl(URI uri, String type ) {
		this.uri = uri;
		this.type = type;

	}
	/**
	 * 
	 * @param content
	 * @param type
	 */
	public ContentImpl(String content, String type) {
		this(content, type, null);
	}
			
	/**
	 * 
	 * @param content
	 * @param type
	 * @param extensions
	 */
	public ContentImpl(String content, String type, Map<String, String> extensions) {
		this.content = content;
		this.type = type==null ? "text" : type;
		if (extensions!=null) this.extensions = extensions;
	}
	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public Map<String, String> getExtensions() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("ContentImpl [type=%s, uri=%s, content=%s]", type,	uri, content);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ContentImpl))
			return false;
		ContentImpl other = (ContentImpl) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
