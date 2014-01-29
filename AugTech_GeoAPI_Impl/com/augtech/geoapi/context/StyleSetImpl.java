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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.opengis.context.Content;
import org.opengis.context.StyleSet;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class StyleSetImpl implements StyleSet {
	Content content, sAbstract = null;
	Map<String, String> styleValues = new HashMap<String, String>();
	Map<String, String> extensions = new HashMap<String, String>();
	/**
	 * 
	 * @param styleValues
	 */
	public StyleSetImpl(Map<String, String> styleValues) {
		this(styleValues, null);
	}
	/**
	 * 
	 * @param styleValues
	 * @param extensions
	 */
	public StyleSetImpl(Map<String, String> styleValues, Map<String, String> extensions) {
		if (styleValues.containsKey("href")) {
			URI uri;
			try {
				uri = new URI( styleValues.get("href") );
				content = new ContentImpl(uri, styleValues.get("contentType") );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			content = new ContentImpl(styleValues.get("content"), styleValues.get("contentType") );
		}
		sAbstract = new ContentImpl(styleValues.get("abstract"), styleValues.get("abstractType"));
		
		styleValues.remove("href");
		styleValues.remove("content");
		styleValues.remove("contentType");
		styleValues.remove("abstract");
		styleValues.remove("abstractType");
		
		this.styleValues = styleValues;
		
		if (extensions!=null) this.extensions = extensions;
	}
	@Override
	public String getName() {
		return styleValues.get("name");
	}

	@Override
	public String getTitle() {
		return styleValues.get("title");
	}

	@Override
	public Content getAbstract() {
		return sAbstract;
	}

	@Override
	public boolean getDefault() {
		return Boolean.valueOf( styleValues.get("default") );
	}

	@Override
	public URL getLegendUrl() {

		try {
			return styleValues.get("legendURL")==null ? null : new URL( styleValues.get("legendURL") );
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		return String.format("StyleSetImpl [name=%s, title=%s, content=%s]", getName(), 
				getTitle(), content);
	}

}
