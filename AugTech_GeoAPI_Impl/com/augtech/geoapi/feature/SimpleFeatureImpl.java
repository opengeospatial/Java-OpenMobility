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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/** A general implementation of the OGC SimpleFeature which holds attribution,
 *  geometry and user data.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class SimpleFeatureImpl implements SimpleFeature {
	private String featureID;
	private List<Object> attrValues;
	protected Map<Object, Object> userdata = new HashMap<Object, Object>();
	protected SimpleFeatureType featureType;
	protected boolean allAttrsPresent = false;
	protected BoundingBox bounds = null;

	/** Create a new SimpleFeature implementation
	 * 
	 * @param fid The unique feature ID
	 * @param attrValues The list of attribute values for this feature. Can be Null
	 * if the values list is to be added afterwards.
	 * @param fType The {@link SimpleFeatureType} this feature belongs to
	 */
	public SimpleFeatureImpl(String fid, List<Object> attrValues, SimpleFeatureType fType) {
		featureID = fid;
		featureType = fType;
		if (attrValues!=null) this.attrValues = attrValues;

	}

	/** Tell the feature that we have now got all attributes on it
	 * 
	 * @param allAttrsPresent
	 */
	public final void setAllAttrsPresent(boolean allAttrsPresent) {
		this.allAttrsPresent = allAttrsPresent;
	}
	/** Do we have all available attributes on this feature?
	 * 
	 * @return False if there are <i>possibly</i> more attributes available
	 * from the server
	 */
	public final boolean isAllAttrsPresent() {
		return allAttrsPresent;
	}

	/** Get Bounding box of this feature's coordinates
	 * 
	 * @return An implementation of {@link BoundingBox}
	 */
	public final BoundingBox getBounds() {
		if (bounds!=null) return bounds;
		
		Name geomName = featureType.getGeometryDescriptor().getName();
		Object geom = getAttribute( geomName );
		
		if (geom==null) return new BoundingBoxImpl("4326");
				
		if (geom instanceof Geometry) {
			
			Geometry g = (Geometry) geom;
			int srsID = g.getSRID();
			this.bounds = new BoundingBoxImpl(g.getEnvelopeInternal(), ""+srsID);
			
		} else if (geom instanceof BoundingBox) {
			
			this.bounds = (BoundingBox)geom;
			
		} else if (geom instanceof Envelope) {

			try {
				
				this.bounds = (BoundingBox)geom;

			} catch (Exception e) {
				e.printStackTrace();
				String srsID = featureType.getGeometryDescriptor().getCoordinateReferenceSystem().getName().getCode();
				this.bounds = new BoundingBoxImpl((Envelope)geom, srsID);
			}
			
		}
		
		return this.bounds;
	}
	/** Get the feature's ID as a {@link NameImpl}
	 * 
	 */
	@Override
	public final Name getName() {
		return new NameImpl(featureID);
	}
	/** Get any user data associated for this feature
	 * 
	 */
	@Override
	public final Map<Object, Object> getUserData() {
		return this.userdata;
	}
	
	/** Returns false */
	@Override
	public final boolean isNillable() {
		return false;
	}
	/** Get an attribute value based on the local part name. This is 
	 * case sensitive.
	 * 
	 * @param name The name String to search for.
	 * @return The value as an Object, or Null if not found
	 */
	@Override
	public Object getAttribute(String name) {
		if (name==null) return null;
		int idx = featureType.indexOf(name);
		if (attrValues!=null && idx>-1) {
			try {
				return attrValues.get( idx );
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	/** Get an attribute value based on its {@link Name}
	 * 
	 * @param name The Name to search for.
	 * @return The value as an Object, or Null if not found
	 */
	@Override
	public final Object getAttribute(Name name) {
		if (name==null || attrValues==null) return null;
		int idx = featureType.indexOf(name);
		if (idx>-1) {
			try {
				return attrValues.get( idx );
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	/** Get an attribute value based on its index in the value list
	 * 
	 */
	@Override
	public final Object getAttribute(int idx) throws IndexOutOfBoundsException {
		return attrValues.get( idx );
	}
	/** Get the count of attribute values on this feature. This may not
	 * match the FeatureType attributeType list 
	 * 
	 */
	@Override
	public final int getAttributeCount() {
		return attrValues.size();
	}
	/** Get the attribute value list for this feature
	 * 
	 */
	@Override
	public final List<Object> getAttributes() {
		return attrValues;
	}
	/** All features must have geometry. This adds geometry at the correct
	 * place in the attribute value list. Nothing happens if geometry is not
	 * defined for the FeatureType.<p>
	 * If the value list is smaller than the index of the Geometry attribute
	 * on the FeatureType, then the value list is expanded accordingly.
	 * The value list <i>cannot</i> be Null for this method to work,
	 * therefore always initialise the feature with <i>at least</i> an
	 * empty list.
	 * 
	 * @param geom The Geometry to add.
	 * @see {@link GeometryFactory} to build the geometry
	 */
	@Override
	public final void setDefaultGeometry(Object geom) {
		int idx = featureType.indexOf( featureType.getGeometryDescriptor().getName() );
		if (idx==-1) {
			Logger.getAnonymousLogger().log(Level.INFO, "Used Geom TYPE name");
			idx = featureType.indexOf( featureType.getGeometryDescriptor().getType().getName() );
		}
		if (idx>-1 && attrValues!=null) {
			if (attrValues.size()>idx) {
				attrValues.set(idx, geom);
			} else {
				for (int i=attrValues.size(); i<idx+1; i++) {// Is this supposed to be attrTypes??
					attrValues.add(null);
				}
				attrValues.set(idx, geom);
			}
		}
	}
	@Override
	public final Geometry getDefaultGeometry() {
		if (attrValues==null) return null;
		int idx = featureType.indexOf( featureType.getGeometryDescriptor().getName() );
		if (idx==-1) {
			idx = featureType.indexOf( featureType.getGeometryDescriptor().getType().getName() );
		}
		return (Geometry) attrValues.get( idx );
	}

	@Override
	public final SimpleFeatureType getFeatureType() {
		return this.featureType;
	}
	/** The ID of this feature. The ID is only unique within the featureCollection
	 * 
	 */
	@Override
	public final String getID() {
		return featureID;
	}
	/** Set the features parent {@link SimpleFeatureType}
	 * 
	 * @param fType
	 */
	public final void setType(SimpleFeatureType fType) {
		this.featureType = fType;
	}
	@Override
	public final SimpleFeatureType getType() {
		return this.featureType;
	}
	/** Set the named attribute to the passed value.
	 * If the passed name does not exist on the FeatureType, it is not added.
	 * The attribute value list is expanded in size automatically.
	 * 
	 * @param name The {@link AttributeType} name to search for.
	 * @param value The new value. Existing values will be overwritten.
	 */
	@Override
	public final void setAttribute(String name, Object value) {
		int idx = featureType.indexOf(name);
		if (idx<0) return;
		if (idx < attrValues.size()) {
			attrValues.set(idx, value);
		} else {
			attrValues.add(value);
		}
	}
	/** Set the named attribute to the passed value.
	 * If the passed name does not exist on the FeatureType, it is not added.
	 * The attribute value list is expanded in size automatically.
	 * 
	 * @param name The {@link AttributeType} {@link Name} to search for.
	 * @param value The new value. Existing values will be overwritten.
	 */
	@Override
	public final void setAttribute(Name name, Object value) {
		int idx = featureType.indexOf(name);
		if (attrValues==null || idx<0) return;
		if (idx < attrValues.size()) {
			attrValues.set(idx, value);
		} else {
			attrValues.add(value);
		}
	}
	/** Set an attribute value based on its index. 
	 * Adds to the end of the attrValue list automatically.
	 * 
	 *@param idx The index of the {@link AttributeType} in {@link FeatureType}
	 *@param value The value to add 
	 */
	@Override
	public final void setAttribute(int idx, Object value)
			throws IndexOutOfBoundsException {
		if (attrValues==null || idx<0) return;
		if (idx < attrValues.size()) {
			attrValues.set(idx, value);
		} else {
			attrValues.add(value);
		}
	}

	/** Overwrites the current list, therefore ensure they are in the correct
	 * order (from the FeatureType) before passing.
	 * 
	 * @param values The list of attribute values to overwrite
	 */
	@Override
	public final void setAttributes(List<Object> values) {
		this.attrValues = values;
	}
	/** Overwrites the current list, therefore ensure they are in the correct
	 * order (from the FeatureType) before passing.
	 * 
	 * @param values The array of attribute values to overwrite
	 */
	@Override
	public final void setAttributes(Object[] values) {
		this.attrValues = new ArrayList<Object>();
		for (Object o : values) {
			attrValues.add(o);
		}
	}
	/** Add new attribute values and types to this feature.
	 * This method ensures that the value order matches the type order, 
	 * even if previous types existed.<p>
	 * 
	 * @param values The list of attribute values
	 * @param types The attribute types
	 */
	public final void setAttributes(List<Object> values, List<AttributeType> types) {
		((SimpleFeatureTypeImpl)featureType).addAttributeTypes(types);
		this.attrValues = new ArrayList<Object>();
		
		for (int i=0; i<featureType.getAttributeCount()+1; i++) {
			attrValues.add("");
		}
		
		for (int i=0; i<types.size(); i++) {
			int idx = featureType.indexOf(types.get(i).getName().getLocalPart());
	
			if (idx==-1) continue;
			attrValues.set(idx, values.get(i) );
		}
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attrValues == null) ? 0 : attrValues.hashCode());
		result = prime * result
				+ ((featureID == null) ? 0 : featureID.hashCode());
		result = prime * result
				+ ((featureType == null) ? 0 : featureType.hashCode());
		return result;
	}
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SimpleFeatureImpl))
			return false;
		SimpleFeatureImpl other = (SimpleFeatureImpl) obj;
		if (attrValues == null) {
			if (other.attrValues != null)
				return false;
		} else if (!attrValues.equals(other.attrValues))
			return false;
		if (featureID == null) {
			if (other.featureID != null)
				return false;
		} else if (!featureID.equals(other.featureID))
			return false;
		if (featureType == null) {
			if (other.featureType != null)
				return false;
		} else if (!featureType.equals(other.featureType))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return featureID;
	}

	/** Not Implemented. Use attributes */
	public Collection<Property> getProperties() {return null;}
	/** Not Implemented. Use attributes */
	public Collection<Property> getProperties(Name arg0) {return null;}
	/** Not Implemented. Use attributes */
	public Collection<Property> getProperties(String arg0) {return null;}
	/** Not Implemented. Use attributes */
	public Property getProperty(Name arg0) {return null;}
	/** Not Implemented. Use attributes */
	public Property getProperty(String arg0) {return null;}
	/** Not Implemented. Use Attributes */
	public GeometryAttribute getDefaultGeometryProperty() {return null;}
	/** Not Implemented. Use Attributes */
	public void setDefaultGeometryProperty(GeometryAttribute arg0) {}
	/** Not Implemented. Use attributes */
	public Collection<? extends Property> getValue() {return null;}
	/** Not Implemented. Use attributes */
	public void setValue(Collection<Property> arg0) {}
	/** Not Implemented */
	public void validate() throws IllegalAttributeException {}
	/** Not Implemented */
	public AttributeDescriptor getDescriptor() {return null;}
	/** Not Implemented Use getID() */
	public FeatureId getIdentifier() {return null;}
	/** Not implemented. Use Attributes */
	public void setValue(Object arg0) {}

}
