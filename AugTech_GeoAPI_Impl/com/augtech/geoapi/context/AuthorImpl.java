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
import java.util.Map;

import org.opengis.context.Author;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class AuthorImpl implements Author {
	Map<String, String> authorValues = new HashMap<String, String>();

	/**
	 * 
	 * @param authorValues
	 */
	public AuthorImpl(Map<String, String> authorValues) {
		this.authorValues = authorValues;
	}
	/**
	 * 
	 * @param name
	 * @param email
	 * @param uri
	 */
	public AuthorImpl(String name, String email, String uri) {
		authorValues.put("name", name);
		authorValues.put("email", email);
		authorValues.put("uri", uri);
	}

	@Override
	public String getName() {
		return authorValues.get("name");
	}

	@Override
	public String getEmail() {
		return authorValues.get("email");
	}

	@Override
	public URI getURI() {
		String uri = authorValues.get("uri");
		try {
			return uri==null ? null : new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format("AuthorImpl [name=%s, email=%s]", getName(), getEmail() );
	}

}
