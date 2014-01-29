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
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;

/** A special extension of an {@link AttributeTypeImpl} used to define feature geometries
 * <p>
 * Typically used for creating a new {@link SimpleFeatureImpl} as<p>
 * <code>
 * 		CoordinateReferenceSystem crs = new CoordinateReferenceSystemImpl("EPSG:27700");<br>
		GeometryType gType = new GeometryTypeImpl(new NameImpl("MultiPoint"), Geometry.class, crs);<br>
		GeometryDescriptor defGeom = new GeometryDescriptorImpl(gType, new NameImpl("the_geom") );<br>
 * </code>
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GeometryTypeImpl extends AttributeTypeImpl implements GeometryType {

	protected CoordinateReferenceSystem CRS;

	/** Simple constructor for default values.<p>
	 * This constructor creates a default CoordinateReferenceSystemImpl of 'EPSG:4326'
	 * 
	 * @param name The type of Geometry, such as MultiLineString, Point etc as a {@link Name}
	 * @param binding The class that will be used to hold the geometry, for example {@link Geometry}.class
	 */
	public GeometryTypeImpl(Name name, Class<?> binding) {
		this(name, binding, new CoordinateReferenceSystemImpl("EPSG:4326"), false, null, null, new InternationalStringImpl(name.getLocalPart()));
	}
	
	/** Simple constructor for default values used to create a CoordinateReferenceSystem with a CRS other
	 * than EPSG:4326 (WGS84 lat/lon)
	 * 
	 * @param name The type of Geometry, such as MultiLineString, Point etc as a {@link Name}
	 * @param binding The class that will be used to hold the geometry, for example {@link Geometry}.class
	 * @param crs The {@link CoordinateReferenceSystem} that applies to all geometries of this type
	 */
	public GeometryTypeImpl(Name name, Class<?> binding, CoordinateReferenceSystem crs) {
		this(name, binding, crs, false, null, null, new InternationalStringImpl(name.getLocalPart()));
	}
	/** Advanced constructor
	 * 
	 * @param name The type of Geometry, such as MultiLineString, Point etc as a {@link Name}
	 * @param binding The class that will be used to hold the geometry, for example {@link Geometry}.class
	 * @param crs The {@link CoordinateReferenceSystem} that applies to all geometries of this type
	 * @param isAbstract
	 * @param restrictions
	 * @param superType
	 * @param description
	 */
	public GeometryTypeImpl(Name name, Class<?> binding, CoordinateReferenceSystem crs, 
			boolean isAbstract, List<Filter> restrictions, 
			AttributeType superType, InternationalString description) {
		super(name, binding, false, isAbstract, restrictions, superType, description);
		CRS = crs;
	}
	/** Get the associated CRS for this GeometryType
	 * 
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return CRS;
	}

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GeometryType)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        GeometryType o = (GeometryType) other;
        if (CRS == null) {
            return o.getCoordinateReferenceSystem() == null;
        }
        if (o.getCoordinateReferenceSystem() == null) {
            return false;
        }
        return true;
    }

}
