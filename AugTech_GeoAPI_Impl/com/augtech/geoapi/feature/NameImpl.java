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
package com.augtech.geoapi.feature;

import org.opengis.feature.type.Name;

/** A NameImpl is an implementation of an org.opengis.feature.type.Name<p>
 * This NameImpl provides the additional ability to set a DisplayName for 
 * use in dialogs etc.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class NameImpl implements Name, Cloneable {
	private String localName = null;
	private String nameSpace = null;
	private String seperator = "";

	/** Just sets the localName<p>
	 * User NameImpl.getNameImpl() if you do not know if the name
	 * is qualified with a namespace
	 * 
	 * @param localName How the name is referenced internally.
	 */
	public NameImpl(String localName) {
		this("", localName);
	}
	/** Build a name from a javax.xml.namespace.QName
	 * 
	 * @param qName
	 */
	public NameImpl(javax.xml.namespace.QName qName) {
		this( qName.getNamespaceURI(), qName.getLocalPart() );
	}
	/** A new Name implementation<p>
	 * 
	 * User NameImpl.getNameImpl() if you do not know if the name
	 * is qualified with a namespace
	 * 
	 * @param namespace Can be null
	 * @param localName How the name is referenced internally.
	 */
	public NameImpl(String namespace, String localName) {
		this.nameSpace = namespace;
		this.localName = localName;
		
		if (nameSpace==null) nameSpace = "";
		if (nameSpace.equals("")) {
			seperator = "";
		} else {
			seperator = ":";
		}
	}

	@Override
	public String getLocalPart() {
		return this.localName;
	}
	/** Same as getURI()
	 * 
	 */
	@Override
	public String toString() {
		return getURI();
	}
	/** Create a new NameImpl from the supplied String, which can either be qualified
	 * with a namespace or not.<p>
	 * Alternatively use the constructor NameImpl(NameSpace, LocalName)
	 * 
	 * @param qName
	 * @return A new NameImpl
	 */
	public static NameImpl getNameImpl(String qName) {
        if (qName.contains(":")) {
        	return new NameImpl(
        			qName.substring(0,qName.indexOf(":")),
        			qName.substring(qName.indexOf(":")+1) );
        } else {
        	return new NameImpl(qName);
        }
	}
	/** Gets the first part of the URI, or the local part if null
	 * 
	 */
	@Override
	public String getNamespaceURI() {
		return nameSpace==null ? "" : nameSpace;
	}

	@Override
	public String getSeparator() {
		return this.seperator;
	}

	/**
	 * Returns the full qualified name 'bob:bobs_widget'
	 */
	@Override
    public String getURI() {
        return nameSpace + seperator + localName;
    }

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {}
		return null;
	}
	@Override
	public boolean isGlobal() {
		return getNamespaceURI() == null;
	}
    public int compareTo(NameImpl other) {
        if( other == null ){
            return 1; // we are greater than null!
        }
        return getURI().compareTo(other.getURI());
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((seperator == null) ? 0 : seperator.hashCode());
		result = prime * result
				+ ((localName == null) ? 0 : localName.hashCode());
		result = prime * result
				+ ((nameSpace == null) ? 0 : nameSpace.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NameImpl))
			return false;
		NameImpl other = (NameImpl) obj;
		if (localName == null) {
			if (other.localName != null)
				return false;
		} else if (!localName.equals(other.localName))
			return false;
		if (nameSpace == null) {
			if (other.nameSpace != null)
				return false;
		} else if (!nameSpace.equals(other.nameSpace))
			return false;
		if (seperator == null) {
			if (other.seperator != null)
				return false;
		} else if (!seperator.equals(other.seperator))
			return false;
		return true;
	}

}
