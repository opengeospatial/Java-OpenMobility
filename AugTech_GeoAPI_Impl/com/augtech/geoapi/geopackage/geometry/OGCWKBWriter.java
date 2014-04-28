/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 * <p>
 * Amended by Augmented Technologies Ltd to accept both OGC/ISO WKB encoding as well
 * as PostGIS EWKB
 * <p>
 */
package com.augtech.geoapi.geopackage.geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.OutStream;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBConstants;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.util.Assert;

/**
 * Writes a {@link Geometry} into ISO/OGC Well-Known Binary format.
 * Supports use of an {@link OutStream}, which allows easy use
 * with arbitrary byte stream sinks.
 * <p>
 * This is a direct copy of the JTS {@link WKBWriter} with amendments in 
 * {@link #writeGeometryType(int, OutStream)} to write the binary in ISO/OGC WKB format 
 * (compared to the original 'EWKB' format) and support Z/M encoding. The only other
 * change is encoding <code>Double.NaN</code> ZM values as 0<p>
 * Note that because the rest of JTS does not support 4 dimensional data (Z and M),
 * neither does this writer.
 */
public class OGCWKBWriter {


	public static String bytesToHex(byte[] bytes)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			buf.append(toHexDigit((b >> 4) & 0x0F));
			buf.append(toHexDigit(b & 0x0F));
		}
		return buf.toString();
	}

	private static char toHexDigit(int n)
	{
		if (n < 0 || n > 15)
			throw new IllegalArgumentException("Nibble value out of range: " + n);
		if (n <= 9)
			return (char) ('0' + n);
		return (char) ('A' + (n - 10));
	}

	private int outputDimension = 2;
	private int byteOrder;
	private boolean dim3IsMeasure = false;
	private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
	private OutStream byteArrayOutStream = new OutputStreamOutStream(byteArrayOS);
	// holds output data values
	private byte[] buf = new byte[8];

	/**
	 * Creates a writer that writes {@link Geometry}s with
	 * output dimension = 2 , BIG_ENDIAN byte order
	 * and the third dimension as Z (height)
	 */
	public OGCWKBWriter() {
		this(2, false, ByteOrderValues.BIG_ENDIAN);
	}

	/**
	 * Creates a writer that writes {@link Geometry}s with
	 * the given output dimension (2, 3 or 4), BIG_ENDIAN byte order
	 * and the third dimension as Z (height)
	 *
	 * @param outputDimension the dimension to output (2 or 3)
	 */
	public OGCWKBWriter(int outputDimension) {
		this(outputDimension, false, ByteOrderValues.BIG_ENDIAN);
	}

	/**
	 * Creates a writer that writes {@link Geometry}s with
	 * the given output dimension (2 or 3) and byte order
	 *
	 * @param outputDimension the dimension to output (2 or 3)
	 * @param dim3IsMeasure The third coordinate value is Measure (M), not Z (height)
	 * @param byteOrder the byte ordering to use
	 */
	public OGCWKBWriter(int outputDimension, boolean dim3IsMeasure, int byteOrder) {
		this.outputDimension = outputDimension;
		this.byteOrder = byteOrder;
		this.dim3IsMeasure = dim3IsMeasure;
		
		if (outputDimension < 2 || outputDimension > 3)
			throw new IllegalArgumentException("Output dimension must be 2 or 3");

		if (outputDimension==2 && dim3IsMeasure && GeoPackage.MODE_STRICT)
			throw new IllegalArgumentException("Output dimension = 2 and Measure specified");
			
	}

	/**
	 * Writes a {@link Geometry} into a byte array.
	 *
	 * @param geom the geometry to write
	 * @return the byte array containing the WKB
	 */
	public byte[] write(Geometry geom) {
		try {
			byteArrayOS.reset();
			write(geom, byteArrayOutStream);
		}
		catch (IOException ex) {
			throw new RuntimeException("Unexpected IO exception: " + ex.getMessage());
		}
		return byteArrayOS.toByteArray();
	}

	/**
	 * Writes a {@link Geometry} to an {@link OutStream}.
	 *
	 * @param geom the geometry to write
	 * @param os the out stream to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void write(Geometry geom, OutStream os) throws IOException {
		if (geom instanceof Point)
			writePoint((Point) geom, os);
		// LinearRings will be written as LineStrings
		else if (geom instanceof LineString)
			writeLineString((LineString) geom, os);
		else if (geom instanceof Polygon)
			writePolygon((Polygon) geom, os);
		else if (geom instanceof MultiPoint)
			writeGeometryCollection(WKBConstants.wkbMultiPoint, (MultiPoint) geom, os);
		else if (geom instanceof MultiLineString)
			writeGeometryCollection(WKBConstants.wkbMultiLineString,
					(MultiLineString) geom, os);
		else if (geom instanceof MultiPolygon)
			writeGeometryCollection(WKBConstants.wkbMultiPolygon,
					(MultiPolygon) geom, os);
		else if (geom instanceof GeometryCollection)
			writeGeometryCollection(WKBConstants.wkbGeometryCollection,
					(GeometryCollection) geom, os);
		else {
			Assert.shouldNeverReachHere("Unknown Geometry type");
		}
	}

	private void writePoint(Point pt, OutStream os) throws IOException {
		if (pt.getCoordinateSequence().size() == 0)
			throw new IllegalArgumentException("Empty Points cannot be represented in WKB");
		writeByteOrder(os);
		writeGeometryType(WKBConstants.wkbPoint, os);
		writeCoordinateSequence(pt.getCoordinateSequence(), false, os);
	}

	private void writeLineString(LineString line, OutStream os)
			throws IOException {
			
		writeByteOrder(os);
		writeGeometryType(WKBConstants.wkbLineString, os);
		writeCoordinateSequence(line.getCoordinateSequence(), true, os);
	}

	private void writePolygon(Polygon poly, OutStream os) throws IOException {
		writeByteOrder(os);
		writeGeometryType(WKBConstants.wkbPolygon, os);
		writeInt(poly.getNumInteriorRing() + 1, os);
		writeCoordinateSequence(poly.getExteriorRing().getCoordinateSequence(), true, os);
		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			writeCoordinateSequence(poly.getInteriorRingN(i).getCoordinateSequence(), true,
					os);
		}
	}

	private void writeGeometryCollection(int geometryType, GeometryCollection gc,
			OutStream os) throws IOException {
		writeByteOrder(os);
		writeGeometryType(geometryType, os);
		writeInt(gc.getNumGeometries(), os);
		for (int i = 0; i < gc.getNumGeometries(); i++) {
			write(gc.getGeometryN(i), os);
		}
	}

	private void writeByteOrder(OutStream os) throws IOException {
		if (byteOrder == ByteOrderValues.LITTLE_ENDIAN)
			buf[0] = WKBConstants.wkbNDR;
		else
			buf[0] = WKBConstants.wkbXDR;
		os.write(buf, 1);
	}

	private void writeGeometryType(int geometryType, OutStream os) throws IOException {
		
		/*      	EWKB           			 OGC
				Z     base | 0x80000000    base + 1000
				M     base | 0x40000000    base + 2000
				ZM    base | 0xC0000000    base + 3000
		 */
		//int flag3D = (outputDimension == 3) ? 0x80000000 : 0;
		//int typeInt = geometryType | flag3D;

		int dimFlag = 0; //2D
		if (outputDimension == 3) {
			dimFlag = dim3IsMeasure ? 2000 : 1000; // Z or M
		}
		if (outputDimension == 4) dimFlag = 4000; // ZM (Not allowed, but added for reference)
		
		writeInt(dimFlag + geometryType, os);
	}

	private void writeInt(int intValue, OutStream os) throws IOException {
		ByteOrderValues.putInt(intValue, buf, byteOrder);
		os.write(buf, 4);
	}

	private void writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, OutStream os)
			throws IOException {
		if (writeSize)
			writeInt(seq.size(), os);

		boolean output3D = false;
		if (seq.getDimension() >= 3 && outputDimension >= 3) output3D = true;

		for (int i = 0; i < seq.size(); i++) {
			writeCoordinate(seq, i, output3D, os);
		}
	}

	private void writeCoordinate(CoordinateSequence seq, int index, boolean output3D, OutStream os)
			throws IOException {
		ByteOrderValues.putDouble(seq.getX(index), buf, byteOrder);
		os.write(buf, 8);
		ByteOrderValues.putDouble(seq.getY(index), buf, byteOrder);
		os.write(buf, 8);
		
		if (output3D) {
			// Set NaN values to 0
			double zm = seq.getOrdinate(index, 2);
			ByteOrderValues.putDouble(Double.isNaN(zm) ? 0d : zm, buf, byteOrder);
			os.write(buf, 8);
		}

	}
	
}
