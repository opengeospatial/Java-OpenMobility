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
package com.augtech.geoapi.referncing;

import java.util.Collection;
import java.util.Set;

import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

import com.augtech.geoapi.feature.type.GeometryTypeImpl;

public class CoordinateReferenceSystemImpl implements CoordinateReferenceSystem {
	ReferenceIdentifier identifier = null;
	
	/** Construct a new CoordinateReferenceSystem with a ReferenceIdentifier<p>
	 * Note this implementation is only really a place holder to allow for codes to 
	 * be specified when building {@linkplain GeometryTypeImpl} - Only the {@link #getName()}
	 * method returns other than {@code null}
	 * 
	 * @param identifier If {@code null} then a new ReferenceIdentifier set to
	 * EPSG:4326 is created.
	 */
	public CoordinateReferenceSystemImpl(ReferenceIdentifier identifier) {
		if (identifier==null) {
			identifier = new ReferenceIdentifierImpl("4326", "EPSG", "1");
		}
		this.identifier = identifier;
	}
	/** Construct a new CoordinateReferenceSystemImpl with a CRS code (such as epsg:4326)<p>
	 * Note this implementation is only really a place holder to allow for codes to 
	 * be specified when building {@linkplain GeometryTypeImpl} - Only the {@link #getName()}
	 * method returns other than {@code null}
	 * 
	 * @param crsCode The authority code, such as epsg:4326
	 */
	public CoordinateReferenceSystemImpl(String crsCode) {
		if (crsCode==null || crsCode.equals("")) crsCode = "EPSG:4326";
		crsCode = crsCode.contains(":") ? crsCode : ":"+crsCode;
		
		String[] split = crsCode.split(":");
		identifier = new ReferenceIdentifierImpl(split[1], split[0], "1");
	}
	@Override
	public Extent getDomainOfValidity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InternationalString getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReferenceIdentifier getName() {
		return identifier;
	}

	@Override
	public Collection<GenericName> getAlias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ReferenceIdentifier> getIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InternationalString getRemarks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toWKT() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CoordinateSystem getCoordinateSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
