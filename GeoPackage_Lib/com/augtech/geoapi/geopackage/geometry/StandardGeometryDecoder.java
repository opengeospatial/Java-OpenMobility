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
package com.augtech.geoapi.geopackage.geometry;

import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

/** A standard geometry type decoder - i.e. Not for use on Extended Geometry
 * fields.
 * 
 */
public class StandardGeometryDecoder extends GeometryDecoder {
	boolean geomDirty = true;
	Geometry theGeom = null;
	
	
	@Override
	public Geometry getGeometry() {
		if (geomData==null) return null;
		
		if (theGeom!=null && geomDirty==false) return theGeom;
		
		Geometry geom = null;
		try {
			geom = new OGCWKBReader().read( this.geomData );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (geom==null || geom.isEmpty()) {
			return new GeometryFactory().createPoint(new Coordinate(Double.NaN, Double.NaN, Double.NaN));
		}
		
		geom.setSRID( super.srsID );
		
		geomDirty = false;
		
		return geom;
	}

	@Override
	public GeometryDecoder setGeometryData(byte[] inputGeom) throws IOException {
		geomDirty = true;
		return super.setGeometryData(inputGeom);
		
	}

}
