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

import java.util.HashMap;
import java.util.Map;

import org.opengis.context.Content;
import org.opengis.context.Operation;

/** An implementation of the {@link Operation} class for 
 * storing offering operations.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class OperationImpl implements Operation {
	String uri;
	Map<String, Object> values = new HashMap<String, Object>();
	Map<String, String> extensions = new HashMap<String, String>();
	/**
	 * 
	 * @param values
	 */
	public OperationImpl(Map<String, Object> values, Map<String, String> extensions) {
		this.values = values;
		uri = String.valueOf(values.get("href"));

		if (extensions!=null) this.extensions = extensions;
	}
	
	@Override
	public String getCode() {
		return String.valueOf(values.get("code"));
	}

	@Override
	public String getMethod() {
		return String.valueOf(values.get("method"));
	}

	@Override
	public String getType() {
		return String.valueOf(values.get("type"));
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public Content getRequest() {
		return (Content) values.get("request");
	}

	@Override
	public Content getResult() {
		return (Content) values.get("result");
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		return String.format("OperationImpl [code=%s, uri=%s]", getCode(), uri  );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OperationImpl))
			return false;
		OperationImpl other = (OperationImpl) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}



}
