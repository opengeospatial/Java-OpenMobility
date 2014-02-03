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

import org.opengis.context.ContextValue;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContextValueImpl implements ContextValue {
	String value = "";
	String encoding = "text";
	Class<?> clazz = String.class;
	
	/**
	 * 
	 * @param value
	 * @param encoding
	 * @param clazz
	 */
	public ContextValueImpl(String value, String encoding, Class<?> clazz) {
		this.value = value==null ? "" : value;
		this.encoding = encoding==null ? "text" : encoding;
		this.clazz = clazz==null ? String.class : clazz;
	}

	@Override
	public String getString() {
		return value;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public String getType() {
		return clazz.getSimpleName();
	}
	@Override
	public String toString() {
		return String.format("ContextValueImpl [value=%s, encoding=%s]", value,	encoding);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((encoding == null) ? 0 : encoding.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof ContextValueImpl))
			return false;
		ContextValueImpl other = (ContextValueImpl) obj;
		if (encoding == null) {
			if (other.encoding != null)
				return false;
		} else if (!encoding.equals(other.encoding))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
