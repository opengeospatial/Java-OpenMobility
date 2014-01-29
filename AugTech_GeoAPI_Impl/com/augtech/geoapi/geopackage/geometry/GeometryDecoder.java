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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;

/** An abstract class for processing byte[] data from a GeoPacakge
 * GEOMETRY field into JTS Geometry.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public abstract class GeometryDecoder {
	protected byte[] geomData = null;
	protected int gpkgVersion = 0;
	protected Envelope envelope = new Envelope();
	protected boolean isEmpty = false;
	protected boolean extendedGeom = false;
	protected int srsID = -1;
	protected int byteOrder = 1;
	protected boolean gpkgVaild = true;
	
	/** Set the field data on this decoder. This must be done prior
	 * to calling {@link #getGeometry()}.<p> The header information is decoded immediately
	 * with remaining geometry byte data stored on {@link #geomData} for further
	 * processing.
	 * 
	 * @param inputGeom The input byte[]
	 * @return This decoder
	 * @throws IOException
	 */
	public GeometryDecoder setGeometryData(byte[] inputGeom) throws IOException {
		
		this.clear();
		
		if (inputGeom==null) throw new IllegalArgumentException("Geometry value is null");
		
		ByteArrayInputStream bais = new ByteArrayInputStream( inputGeom );
		
		// 'Magic' and version
		byte[] buffer = new byte[2];
		bais.read(buffer);
		String magic = new String(buffer);
		gpkgVaild = magic.equals("GP");

		gpkgVersion = bais.read();
		
		if (gpkgVersion>GeoPackage.MAX_GPKG_VERSION)
			throw new IllegalArgumentException("Geometry version is greater than supported version");
		
		// Decode header flags
		int flags = bais.read();
		byteOrder = (flags & 1);
		
		// Envelope
		int envBytes = 0;
		if ( (flags >> 1 & 1)==1) {
			envBytes = 32; // XY
		} else if ( (flags >> 2 & 1)==1 ) {
			envBytes = 48; // XYZ
		} else if ( (flags >> 3 & 1)==1 ) {
			envBytes = 48; // XYM
		} else if ( (flags >> 4 & 1)==1 ) {
			envBytes = 64; // XYZM
		}
		isEmpty = (flags & (1 << 4))==1;
		extendedGeom = (flags & (1 << 5))==1;
		// Bits 7 and 8 are reserved and currently 0

		int jtsByteOrder = byteOrder==0 ? ByteOrderValues.BIG_ENDIAN : ByteOrderValues.LITTLE_ENDIAN;
		
		// SRID
		buffer = new byte[4];
		bais.read( buffer );
		srsID = ByteOrderValues.getInt(buffer, jtsByteOrder);
		
		// Envelope
		int numCoord = envBytes/8;
		buffer = new byte[ envBytes/4 ];
		double[] vals = envBytes > 0 ? new double[numCoord] : null;
		for(int i=0; i < numCoord; i++) {
			bais.read(buffer);
			vals[i] = ByteOrderValues.getDouble(buffer, jtsByteOrder);
		}
		
		// Construct the header defined envelope
		if (vals!=null) {
			envelope.init(vals[0], vals[1], vals[2], vals[3]);
		}
		
		int headerLength = 8+envBytes;
		geomData = new byte[inputGeom.length-headerLength];
		bais.read( geomData );

		bais.close();
		
		return this;
	}

	/** Clear all byte data and reset to default values.
	 * 
	 */
	public void clear() {
		geomData = null;
		gpkgVersion = 0;
		envelope.setToNull();
		isEmpty = false;
		extendedGeom = false;
		srsID = -1;
		byteOrder = 1;
	}
	/** Get the Envelope as defined in the Geometry header. Note this has no SRID 
	 * set on it for performance reasons.
	 * 
	 * @return
	 */
	public Envelope getEnvelope() {
		return envelope;
	}
	/** Get the GeoPacakge version as defined in the Geometry header
	 * 
	 * @return
	 */
	public int getVersion() {
		return this.gpkgVersion;
	}
	/** Decode the binary data into a {@link Geometry} object.
	 * 
	 * @return
	 */
	public abstract Geometry getGeometry();

	public boolean isEmpty() {
		return this.isEmpty;
	}
	/** Get the SRS ID as defined in the Geometry header
	 * 
	 * @return
	 */
	public int getSRSID() {
		return this.srsID;
	}
	/** Is this an extended geometry field, as defined in the Geometry header
	 * 
	 * @return
	 */
	public boolean isExtended() {
		return this.extendedGeom;
	}

	public boolean isEmptyGeom() {
		return this.isEmpty;
	}
}
