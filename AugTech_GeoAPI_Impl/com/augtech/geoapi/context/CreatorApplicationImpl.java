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

import org.opengis.context.CreatorApplication;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class CreatorApplicationImpl implements CreatorApplication {
	String title;
	String version;
	URI uri;
	
	/**
	 * 
	 * @param uri
	 * @param title
	 * @param version
	 */
	public CreatorApplicationImpl(String uri, String title, String version ) {
		try {
			this.uri = uri==null ? null : new URI(uri);
		} catch (URISyntaxException e) {}
		
		this.title = title;
		this.version = version;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public String getVersion() {
		return version;
	}


	@Override
	public String toString() {
		return String.format(
				"CreatorApplicationImpl [title=%s, version=%s, uri=%s]", title,
				version, uri);
	}

}
