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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;

/** An implementation of the GeoAPI {@link SimpleFeatureType}.<p>
 * A FeatureType describes the basic requirements for a set of Features such 
 * as attributes and properties that apply to all Features of the same type.<br>
 * This implementation also includes additional methods for simplifying the 
 * retrieval of user data and addition of attribute types.<p>
 * 
 * <b>Description from {@link SimpleFeatureType}</b>
 * The type of a SimpleFeature.
 * <p>
 * The definition of a "simple feature" can be summed up as the following:
 * <ul>
 * <li>made up of only non-complex attributes, no associations
 * <li>attributes are of multiplicity 1
 * <li>attributes are ordered
 * <li>attribute names are unqualified (namespaceURI == null)
 * </ul>
 * </p>
 * <p>
 * <h3>Attribute Indexing</h3>
 * The attributes which compose a simple feature type are ordered. For this
 * reason attributes are available via a simple index. Given the following type
 * definition:
 *
 * <pre>
 *   &lt;complexType name=&quot;mySimpleType&quot;/&gt;
 *     &lt;sequence&gt;
 *        &lt;element name=&quot;foo&quot; type=&quot;xs:string&quot;/&gt;
 *        &lt;element name=&quot;bar&quot; type=&quot;xs:integer&quot;/&gt;
 *     &lt;/sequence&gt;
 *   &lt;/complexType&gt;
 * </pre>
 *
 * <br>
 * The attribute descriptor are addressable via index:
 *
 * <pre>
 *   SimpleFeatureType type = ...;
 *
 *   AttributeDescriptor foo = type.getAttribute( 0 );
 *   AttributeDescriptor bar-= type.getAttribute( 1 );
 * </pre>
 *
 * <p>
 * <h3>Attribute Multiplicity</h3>
 * With simple feature types, the multiplicity of attributes is always assumed
 * to be 1, ie, <code>getMinOccurs() == 1</code> and
 * <code>getMaxOccurs() == 1</code>. A consequence of this is that attributes
 * from a simple feature always line up 1 to 1 with the descriptors from the
 * type:
 *
 * <pre>
 *   SimpleFeature feature = ...;
 *   SimpleFeatureType type = feature.getType();
 *
 *   type.getAttribute( 0 ).getDescriptor() == type.getAttribute( 0 );
 *   type.getAttribute( 1 ).getDescriptor() == type.getAttribute( 1 );
 * </pre>
 *
 * </p>
  * <p>
 * <h3>Attribute Naming</h3>
 * The names of attributes in a simple feature type may not be namespace
 * qualified. For this reason there should be no difference between accessing an
 * attribute with {@link #getDescriptor(String)} and {@link #getDescriptor(Name)}.
 * </p>
 * 
 */
public class SimpleFeatureTypeImpl implements SimpleFeatureType {

	private List<AttributeType> attributeTypes;
	private int id;
	private Name name;
	protected Map<Object, Object> userdata = new HashMap<Object, Object>();
	private GeometryDescriptor defaultGeometry;
	
	/** A specific value stored in this type's 'user-data' */
	public enum USER_DATA {
		/** Is this type for image/ raster features */
		IMAGE_TILE,
		/** A description of this type */
		DESCRIPTION,
		/** The X (width) real-world size per pixel */
		X_PIXEL_DIST_AT_MAX_ZOOM,
		/** The Y (height) real-world size per pixel */
		Y_PIXEL_DIST_AT_MAX_ZOOM,
		/** RGBColor. Make a specified colour within the raster image transparent? 
		 * Default is False */
		COLOR_TRANSPARENT,
		/** Boolean. Is this FeatureType currently visible. Default is False.
		 * Will get set when this {@link ARFeatureType} is toggled on and off. */
		VISIBLE,
		/** Boolean. Can the user select this object for information display?.
		 * Default is True */
		SELECTABLE,
		/** Boolean. Was the feature downloaded from the Internet. 
		 * This value will get set when the feature is downloaded */
		DOWNLOADED,
		/** Boolean. An exclusive OR toggle meaning only one {@link ARFeatureType}
		 *  can be visible at a time. Default is False */
		BILLBOARD,
		/** Boolean. An exclusive OR toggle meaning only one {@link ARFeatureType}
		 *  can be visible at a time. Default is False */
		EXCLUSIVE_TOGGLE,
		/** Boolean. Allow additional information query? Default is True */
		ADDITIONAL_INFO,
		/** The name of the first attribute whose value should be use in applying a style to the feature.
		 * Only applies if the value matches a defined style and the value is not null etc 
		 * i.e it matches. */
		STYLE_ATTRIBUTE_NAME_1,
		/** The name of the second attribute whose value should be use in applying a style to the feature.
		 * Only applies if the value matches a defined style and the value is not null etc 
		 * i.e it matches. */
		STYLE_ATTRIBUTE_NAME_2,
		/** Any additional server url (specific to this feature type) to append to 
		 * the root server url */
		ADD_URL,
		/** A datasource specifically for this feature type which overrides that on a Dataset */
		FEATURE_DATASOURCE,
		/** Should this type be switched on (visible) by default? */
		DEFAULT_ON,
		/** Don't show when zoomed out beyond.. (The minimum scale) */
		MIN_SCALE,
		/** Don't show when zoomed in beyond.. (The maximum scale) */
		MAX_SCALE
	};
	/** No ID constructor
	 * 
	 * @param name The name of the FeatureType
	 * @param attributeTypes A list of AttributeType's to retrieve from the server/ load
	 * by default. If null then only the geometry attribute is created (as is required by all features).
	 * If not null, the geometry attribute <i>must be</i> in the attribute list provided. 
	 * @param defaultGeom Descriptor of the Geometry field
	 */
	public SimpleFeatureTypeImpl(Name name, List<AttributeType> attributeTypes, GeometryDescriptor defaultGeom) {
		this(-1, name, attributeTypes, defaultGeom);
	}
	/** Creates default userData values.<p>
	 * The Geometry field <b>is not</b> added by default even when a defaultGeom is provided, 
	 * therefore ensure it is in the attributeTypes list prior to constructing. defaultGeom
	 * is used to find the Geometry attribute within setDefaultGeom()
	 * 
	 * @param id The FeatureType id
	 * @param name The name implementation to be used for finding and displaying this type
	 * @param attributeTypes A list of AttributeType's to retrieve from the server/ load
	 * by default. If null then only the geometry attribute is created (as is required by all features).
	 * If not null, the geometry attribute <i>must be</i> in the attribute list provided.
	 * @param defaultGeom Descriptor of the Geometry field
	 */
	public SimpleFeatureTypeImpl(int id, Name name, List<AttributeType> attributeTypes, GeometryDescriptor defaultGeom) {
		this.id = id;
		this.name = (NameImpl)name;
		this.defaultGeometry = defaultGeom;

		// Ensure we have a geometry definition in the attribute list

		/* 17/09/13 - Changed if empty list don't add default geom (for new GML2/3 parsing),
		 * but if null initialise with an empty list and add the geometry definition */
		if (attributeTypes==null) {
			attributeTypes = new ArrayList<AttributeType>();
			attributeTypes.add( defaultGeometry.getType() );
		}
		this.attributeTypes = attributeTypes;

	}
	/** Advanced constructor to allow for the manual creation of a FeatureType.<p>
	 * 
	 * The Geometry field <b>is not</b> added by default even when a defaultGeom is provided, 
	 * therefore ensure it is in the attributeTypes list prior to constructing. defaultGeom
	 * is used to find the Geometry attribute within setDefaultGeom()
	 * 
	 * @param id The FeatureType id. A unique ID for this type (within this type)
	 * @param name The name implementation to be used for finding and displaying this type
	 * @param attributeTypes A list of AttributeType's to retrieve from the server/ load
	 * by default. If null then only the geometry attribute is created (as is required by all features)
	 * @param defaultGeom Descriptor of the Geometry field
	 * @param userData A set of USER_xxx settings to override the default values
	 */
	public SimpleFeatureTypeImpl(int id, Name name, List<AttributeType> attributeTypes,
			GeometryDescriptor defaultGeom, Map<Object, Object> userData) {
		this(id, name, attributeTypes, defaultGeom);
		if (userData!=null) {
			this.userdata.putAll(userData);
		}
	}
	/**
    * Walks up the type hierarchy of the feature returning all super types of the specified feature
    * type. The search terminates when a non-FeatureType or null is found. The original featureType
    * is not included as an ancestor, only its strict ancestors.
    */
   public static List<FeatureType> getAncestors(FeatureType featureType) {
       List<FeatureType> ancestors = new ArrayList<FeatureType>();
       while (featureType.getSuper() instanceof FeatureType) {
           FeatureType superType = (FeatureType) featureType.getSuper();
           ancestors.add(superType);
           featureType = superType;
       }
       return ancestors;
   }

	@Override
	public GeometryDescriptor getGeometryDescriptor() {
	    if (defaultGeometry == null) {
            for (AttributeType at : attributeTypes) {
                if (at instanceof GeometryDescriptor ) {
                    defaultGeometry = (GeometryDescriptor) at; 
                    break;
                }
            }
        }
        return defaultGeometry;
	}
	/** Build a GeometryDescriptor from a JTS {@link Geometry} with the name of 'the_geom'.<p>
	 * This method will attempt to get a valid SRID from the Geometry. If it can't
	 * get one then WGS84 (EPSG:4326) will be set on the GeometryDescriptor, therefore it is
	 * always best to set it first using {@linkplain Geometry#setSRID(int)}
	 * 
	 * @param geometry
	 * @return A new {@link GeometryDescriptor}
	 */
	public static GeometryDescriptor getDefaultGeometryDescriptor(Geometry geometry) {
		
		Name gName = new NameImpl(geometry.getGeometryType());
		GeometryType gType = null;
		
		// Does the geometry contain an SRID we can use?
		int srid = geometry.getSRID();
		if (srid>0) {
			CoordinateReferenceSystem crs = new CoordinateReferenceSystemImpl("EPSG:"+srid);
			gType = new GeometryTypeImpl(gName, Geometry.class, crs);
		} else {
			// Create assuming EPSG:4326
			gType = new GeometryTypeImpl(gName, Geometry.class);	
		}
    	
    	
    	return new GeometryDescriptorImpl(gType, new NameImpl("the_geom"));
	}
	/** Get the ID for this FeatureType
	 * 
	 * @return The id
	 */
	public int getId() {
		return this.id;
	}
	/** Always returns True */
	@Override
	public boolean isIdentified() {
		return true;
	}
	/** Set a description for this type (i.e. To use in dialogs etc
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		userdata.put(USER_DATA.DESCRIPTION, description);
	}
	/** Returns an InternationalString made from the UserData 'Description' value
	 */
	@Override
	public InternationalString getDescription() {
		String d = String.valueOf( userdata.get(USER_DATA.DESCRIPTION) );
		return new InternationalStringImpl( d.equals("") ? name.getLocalPart() : d );
	}
	@Override
	public Name getName() {
		return this.name;
	}
	@Override
	public Map<Object, Object> getUserData() {
		return this.userdata;
	}
	@Override
	/** Always returns false
	 * 
	 */
	public boolean isAbstract() {
		return false;
	}
	/** Add an additional set of {@link AttributeType} to this feature
	 * type, preserving the existing set in order.<p>
	 * Used, for example, when a getObjectInformation() request returns
	 * additional attributes to those set as default.
	 * 
	 * @param addTypes Additional AttributeTypeImpl's
	 */
	public void addAttributeTypes(List<AttributeType> addTypes) {
		addAttributeTypes(addTypes, false);
	}
	/** Add an additional set of {@link AttributeType} to this feature
	 * type, preserving the existing set in order.<p>
	 * Used, for example, when a getObjectInformation() request returns
	 * additional attributes to those set as default.
	 * 
	 * @param newTypes The new AttributeTypes to add
	 * @param overwrite If False then the existing set of values is preserved,
	 * otherwise they are replaced with the new set
	 */
	public void addAttributeTypes(List<AttributeType> newTypes, boolean overwrite) {
		if (newTypes==null) {
			int p=0;
			p++;
		}
		// If our list is blank, copy them all
		if (attributeTypes==null || overwrite==true) {
			attributeTypes = newTypes;
			return;
		} else if (attributeTypes.size()==0) {
			attributeTypes = newTypes;
			return;
		}
		
		// Add only the new values
		for (AttributeType nt : newTypes) {
			if ( getType(nt.getName().getLocalPart())==null ) {
				attributeTypes.add(nt);
			}
		}
		
	}

	/** Add a specific user data setting
	 * 
	 * @param setting The {@linkplain USER_DATA} enum setting
	 * @param value The value
	 */
	public void setUserData(Enum<USER_DATA> setting, Object value) {
		userdata.put(setting, value);
	}
	/** Get the current attribute count.
	 * 
	 * @return 0 if null
	 */
	@Override
	public int getAttributeCount() {
		if (attributeTypes==null) return 0;
		return attributeTypes.size();
	}
	/** Returns the AttributeType with the name matching that passed, or null
	 * if not found. 
	 * 
	 * @param name The string to search for
	 * @return Null if not found
	 */
	@Override
	public AttributeType getType(String name) {
		if (attributeTypes==null) return null;
		for (AttributeType at : this.attributeTypes) {
			if (at.getName().getLocalPart().equals(name)) return at;
		}
		return null;
	}

	@Override
	public AttributeType getType(Name name) {
		if (this.attributeTypes==null) return null;
		for (AttributeType at : this.attributeTypes) {
			if (at.getName().equals(name)) return at;
		}
		return null;
	}
	/** Get a specific AttributeType
	 * 
	 */
	@Override
	public AttributeType getType(int idx) throws IndexOutOfBoundsException {
		return this.attributeTypes.get(idx);
	}
	/** Get the feature type's local name
	 * 
	 */
	@Override
	public String getTypeName() {
		return this.name.getLocalPart();
	}
	/** Get all the defined AttributeTypes
	 * 
	 */
	@Override
	public List<AttributeType> getTypes() {
		return attributeTypes;
	}
	/** Get the index of an AttributeType based on its local name.
	 * 
	 * @param The local part name to search for in the attributeType list
	 */
	@Override
	public int indexOf(String name) {
		if (attributeTypes==null) return -1;
		try {
			for (int i=0;i<attributeTypes.size(); i++) {
				if (attributeTypes.get(i).getName().getLocalPart().equals(name))
					return i;
			}
		} catch (Exception e) {}
		return -1;
	}

	/** Get the index of an AttributeType based on its name
	 * 
	 * @param The {@link Name} to search for in the attributeType list
	 */
	@Override
	public int indexOf(Name name) {
		if (attributeTypes==null) return -1;
		int idx = -1;
		for (int i=0;i<attributeTypes.size(); i++) {
			if (attributeTypes.get(i).getName().equals(name)) idx = i;
		}
		
		// Second attempt to match on local part only
		if (idx==-1) {
			return indexOf(name.getLocalPart());
		} else {
			return idx;
		}
	}

	@Override
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return defaultGeometry.getCoordinateReferenceSystem();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeTypes == null) ? 0 : attributeTypes.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SimpleFeatureTypeImpl))
			return false;
		
		SimpleFeatureTypeImpl other = (SimpleFeatureTypeImpl) obj;

		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		if (attributeTypes == null) {
			if (other.attributeTypes != null)
				return false;
		} else if (!attributeTypes.equals(other.attributeTypes))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return name.getLocalPart()+": Desc:"+String.valueOf(userdata.get("Description"));
	}
	//--------- USER DATA HELPER FUNCTIONS -----------
	/** Get a string value from the userdata
	 * 
	 * @param setting Which setting to check.
	 * @return zero length string if not found
	 */
	public String getUserString(Enum<USER_DATA> setting) {
		Object ret = userdata.get(setting);
		if (ret==null) return "";
		if (ret instanceof String) {
			return (String)ret;
		} else {
			return "";
		}
	}
	/** Get a integer value from the userdata
	 * 
	 * @param setting Which setting to check.
	 * @return 0 if not found
	 */
	public Integer getUserInt(Enum<USER_DATA> setting) {
		Object ret = userdata.get(setting);
		if (ret==null) return 0;
		if (ret instanceof Integer) {
			return (Integer)ret;
		} else {
			return 0;
		}
	}
	/** Get a boolean value from the userdata
	 * 
	 * @param setting Which setting to check.
	 * @return False if false or not found
	 */
	public boolean getUserBool(Enum<USER_DATA> setting) {
		Object ret = userdata.get(setting);
		if (ret==null) return false;
		if (ret instanceof Boolean) {
			return (Boolean)ret;
		} else {
			return false;
		}
	}
	/** Get a double value from the userdata
	 * 
	 * @param setting Which setting to check.
	 * @return Double.NaN if not found
	 */
	public double getUserDouble(Enum<USER_DATA> setting) {
		Object ret = userdata.get(setting);
		if (ret==null) return Double.NaN;
		if (ret instanceof Double) {
			return (Double)ret;
		} else {
			return Double.NaN;
		}
	}
	/** Get a float value from the userdata
	 * 
	 * @param setting Which setting to check.
	 * @return 0 if not found
	 */
	public float getUserFloat(int setting) {
		Object ret = userdata.get(setting);
		if (ret==null) return 0;
		if (ret instanceof Float) {
			return (Float)ret;
		} else {
			return 0;
		}
	}
	//--------- GeoAPI unimplemented functions ------------- 
	


	/** Not implemented. Use attributes */
	@Override
	public Class<Collection<Property>> getBinding() {
		return null;
	}
	/** Not implemented. Use attributes */
	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return null;
	}
	/** Not implemented. Use attributes */
	@Override
	public boolean isInline() {	return false;}
	/** Not implemented.*/
	@Override
	public AttributeType getSuper() {return null;}
	/** Not implemented. Use attributes */
	@Override
	public List<Filter> getRestrictions() {return null;}
	/** Not implemented. */
	@Override
	public List<AttributeDescriptor> getAttributeDescriptors() {return null;}
	/** Not implemented. */
	@Override
	public AttributeDescriptor getDescriptor(String arg0) {	return null;}
	/** Not implemented. */
	@Override
	public AttributeDescriptor getDescriptor(Name arg0) {return null;}
	/** Not implemented. */
	@Override
	public AttributeDescriptor getDescriptor(int arg0) throws IndexOutOfBoundsException {return null;}

}
