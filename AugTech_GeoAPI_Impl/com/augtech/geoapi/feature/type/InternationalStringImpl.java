/*
  * Copyright 2013, Augmented Technologies Ltd.
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
package com.augtech.geoapi.feature.type;

import java.util.Locale;

import org.opengis.util.InternationalString;

public class InternationalStringImpl implements org.opengis.util.InternationalString {
	private String string;
	
	public InternationalStringImpl(String string) {
		this.string = string;
	}
	
	public char charAt(int arg0) {
		return string.charAt(arg0);
	}

	public int length() {
		return string.length();
	}

	public CharSequence subSequence(int arg0, int arg1) {
		return string.subSequence(arg0, arg1);
	}

	public int compareTo(InternationalString arg0) {
		return string.compareTo(arg0.toString());
	}

	public String toString(Locale arg0) {
		return string;
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InternationalStringImpl))
			return false;
		InternationalStringImpl other = (InternationalStringImpl) obj;
		if (string == null) {
			if (other.string != null)
				return false;
		} else if (!string.equals(other.string))
			return false;
		return true;
	}

}
