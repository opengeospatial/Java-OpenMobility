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

import java.util.List;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

import com.augtech.geoapi.feature.NameImpl;

/** Used to define an attribute, including it's name and the 
 * class/ type of data it is.
 * 
 * @author Augmented Technologies Ltd
 *
 */
public class AttributeTypeImpl extends PropertyTypeImpl implements AttributeType {
	final protected boolean identified;
	/** A default NameImpl for storing a single image; 'the_image' */
	public static final NameImpl IMAGE_NAME = new NameImpl("the_image");
	
	/** Boolean. Store whether this attribute type displays in information screens.
	 * Default is True. */
	public static final int USER_DISPLAY_ATTRIBUTE = 1;
	/** User data setting for esri info */
	public static final int USER_DOMAIN = 100;
	/** User data setting for esri info */
	public static final int USER_REQUIRED = 101;
	/** User data setting for esri info */
	public static final int USER_LENGTH = 102;
	
	//private String type;

	/** Construct a new AttributeType.
	 * Description is taken from the display name.
	 * 
	 * @param name The name of the type
	 * @param binding The class binding
	 */
	public AttributeTypeImpl(Name name, Class<?> binding) {
		super(name, binding, false, null, null, new InternationalStringImpl(name.getLocalPart() ) );

		identified = false;
		//this.type = type;
		//userdata = new HashMap<Object, Object>();
		userData.put(USER_DISPLAY_ATTRIBUTE, true);
	}
	public AttributeTypeImpl(
			Name name, Class<?> binding, boolean identified, boolean isAbstract,
			List<Filter> restrictions, AttributeType superType, InternationalString description
		) {
			super(name, binding, isAbstract, restrictions, superType, description);
			
			this.identified = identified;
	}

	/** Create a default implementation of typeNone
	 * 
	 * @param name
	 */
	public AttributeTypeImpl(Name name) {
		this(name, Object.class);
	}
	/** Get the qualified name
	 * 
	 */
	public Name getName() {
		return this.name;
	}
	/** Get the attribute's type, for example String.
	 * 
	 * @return A String describing the type
	 */
	public String getType() {
		return this.superType.toString();//Return actual type??
	}
	/** Always returns false
	 * 
	 */
	public boolean isIdentified() {
		return identified;
	}
	/** Get attributes super type
	 * 
	 */
	public AttributeType getSuper() {
	    return (AttributeType) super.getSuper();
	}
	/** Set user data settings.
	 * 
	 * @param setting See USER_xxx flags
	 * @param data The data to store
	 */
	public void setUserData(int setting, Object data) {
		this.userData.put(setting, data);
	}
	
	@Override
	public boolean equals(Object other) {
	    if(this == other) return true;
	    
		if (!(other instanceof AttributeType)) return false;

		if (!super.equals(other)) return false;
		
		AttributeType att = (AttributeType) other;
		
		if (identified != att.isIdentified()) return false;
	
		return true;
	}
	

	@Override
	public String toString() {
		return name.toString()+" "+getBinding().getSimpleName();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
