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

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
/** A simple implementation of a Geometry Descriptor used to define the type of Geometry
 * that a {@link SimpleFeatureImpl} would require.
 *  <p>
 * <code>
 * 		CoordinateReferenceSystem crs = new CoordinateReferenceSystemImpl("EPSG:27700");<br>
		GeometryType gType = new GeometryTypeImpl(new NameImpl("MultiPoint"), Geometry.class, crs);<br>
		GeometryDescriptor defGeom = new GeometryDescriptorImpl(gType, new NameImpl("the_geom") );<br>
 * </code>
 *
 * @author Augmented Technologies Ltd.
 */
public class GeometryDescriptorImpl extends AttributeDescriptorImpl implements GeometryDescriptor {

	/** Simple constructor.<p>
	 * Creates with default values of; Min=0, Max=1, Nillable = false, DefaultValue = Null.
	 * 
	 * @param type A {@link GeometryType} that defines the CRS and 
	 * type of geometry, such as LineString, Point, MulitPolygon etc
	 * @param name The name of the geometry field, such as 'the_geom'.
	 */
	public GeometryDescriptorImpl(GeometryType type, Name name) {
		this(type, name, 0, 1, false, null);
	}
	/** Advanced constructor
	 * 
	 * @param type A {@link GeometryType} that defines the CRS and 
	 * type of geometry, such as LineString, Point, MulitPolygon etc
	 * @param name The name of the geometry field, such as 'the_geom'.
	 * @param min
	 * @param max
	 * @param isNillable
	 * @param defaultValue
	 */
    public GeometryDescriptorImpl(GeometryType type, Name name, int min,
            int max, boolean isNillable, Object defaultValue) {
        super(type, name, min, max, isNillable, defaultValue);
        
    }
    @Override
    public GeometryType getType() {
        return (GeometryType) super.getType();
    }
    @Override
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return getType().getCoordinateReferenceSystem();
	}
	@Override
	public String getLocalName() {
		return getName().getLocalPart();
	}
}
