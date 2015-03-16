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
package com.augtech.geoapi.geometry;

import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
/** Extends the JTS {@link Envelope} to include a spatial reference name,
 * z coordinate and projection methods.<br>
 * <b>Note</b> that if the projection methods {@link #toBounds(CoordinateReferenceSystem)}, 
 * {@link #toSRS(int)} or {@link #toCRS(String)} are used, the native libraries
 * <i>MUST</i> be copied to a readable directory and the directory path set 
 * on {@link OSMUtils#PROJECTION_LIB_PATH} before calling, otherwise an 
 * UnsatisfiedLinkError will be thrown<br>
 * The libraries are contained within the GeoAPI assets directory ready for copying<p>
 * Implements the GeoAPI {@link BoundingBox} interface
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class BoundingBoxImpl extends Envelope implements BoundingBox {
	private static final long serialVersionUID = 1L;
	/**
     * The coordinate reference system, or {@code null}.
     */
    CoordinateReferenceSystem CRS = null;
    private double minz;
    private double maxz;
    /**
     * Creates a Null envelope with a default coordinate reference system
     * of WGS84 (EPSG:4326)
     */
    public BoundingBoxImpl(String crsName) {
        this( new CoordinateReferenceSystemImpl(crsName) );
    }

    /** Creates a null envelope with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system EPSG code.
     */
    public BoundingBoxImpl(CoordinateReferenceSystem crs) {
    	this(0,-1,0,-1,crs);
    	super.setToNull();
    }
    /**
     * Creates an envelope for a region defined by maximum and minimum values,
     * with a default CRS of EPSG:4326
     *
     * @param x1  The min/ first x-value.
     * @param x2  The max/ second x-value.
     * @param y1  The min/ first y-value.
     * @param y2  The max/ second y-value.
     *
     */
    public BoundingBoxImpl(final double x1, final double x2, final double y1, final double y2) {
            this(x1, x2, y1, y2, new CoordinateReferenceSystemImpl("") );
            minz = Double.NaN; maxz = Double.NaN;
        }
    /**
     * Creates an envelope for a region defined by maximum and minimum values.
     *
     * @param x1  The min/ first x-value.
     * @param x2  The max/ second x-value.
     * @param y1  The min/ first y-value.
     * @param y2  The max/ second y-value.
     * @param crs The coordinate reference system EPSG code.
     *
     */
    public BoundingBoxImpl(final double x1, final double x2, final double y1, final double y2,
        final CoordinateReferenceSystem crs) {
    	this(x1, x2, y1, y2, Double.NaN, Double.NaN, crs);
    }
    /**
     * 
     * @param x1  The min/ first x-value.
     * @param x2  The max/ second x-value.
     * @param y1  The min/ first y-value.
     * @param y2  The max/ second y-value.
     * @param z1  The min/ first z-value.
     * @param z2  The max/ second z-value.
     * @param crs The coordinate reference system EPSG code.
     */
    public BoundingBoxImpl(final double x1, final double x2, final double y1, final double y2,
            double z1, double z2, final CoordinateReferenceSystem crs) {
    	super(x1,x2,y1,y2);
    	if (z1 < z2) {
	    	minz = z1;
	    	maxz = z2;
    	} else {
	    	minz = z2;
	    	maxz = z1;
    	}
    	setCRS(crs);
    }
    public double getMinZ() {
    	return this.minz;
    }
    public double getMaxZ() {
    	return this.maxz;
    }
    /** Return this BoundingBox as a closed polygon geometry.
     * 
     * @return
     */
    public Geometry toPolygon() {
    	GeometryFactory gf = new GeometryFactory();
    	Coordinate[] lr = new Coordinate[]{
    			new Coordinate(getMinX(), getMinY(), 0),
    			new Coordinate(getMinX(), getMaxY(), 0),
    			new Coordinate(getMaxX(), getMaxY(), 0),
    			new Coordinate(getMaxX(), getMinY(), 0),
    			new Coordinate(getMinX(), getMinY(), 0)
    	};
    	Geometry g = gf.createPolygon(gf.createLinearRing(lr), null);
    	g.setSRID(Integer.valueOf( this.CRS.getName().getCode()) );
    	
    	return g;
    }
    /**
     * Creates a new envelope from an existing envelope.
     *
     * @param envelope The envelope to initialize from
     * @param The srsName to use
     */
    public BoundingBoxImpl(Envelope envelope, String srsName) {
    	super(envelope);
    	setCRS(srsName);
        minz = 0; maxz = 0;
    }
    /**
     * 
     * @param c1
     * @param c2
     * @param srsName
     */
    public BoundingBoxImpl(final Coordinate c1, final Coordinate c2, final String srsName) {
        this( c1.x, c2.x, c1.y, c2.y, new CoordinateReferenceSystemImpl(srsName) );
        if (c1.z < c2.z) {
            minz = c1.z ;
            maxz = c2.z ;
          }
          else {
            minz = c2.z;
            maxz = c1.z;
          }
    }
    /** Create a new ReferencedEnvelope from a {@link BoundingBox}
     * 
     * @param bbox
     */
    public BoundingBoxImpl(BoundingBox bbox) {
    	super(new Envelope(
    			bbox.getMinX(),bbox.getMaxX(),
    			bbox.getMinY(),bbox.getMaxY()
    			));
    }

	/**
     * Returns the native coordinate reference system associated with 
     * this envelope. (An EPSG:xxxx code)
     */
    public String getCRSName() {
        return CRS.getName().getCodeSpace()+":"+CRS.getName().getCode();
    }
    /** Set the {@link CoordinateReferenceSystem}
     * 
     * @param crs A constructed CoordinateReferenceSystem
     */
    public void setCRS(CoordinateReferenceSystem crs) {
    	this.CRS = crs;
    }
    /** Set the EPSG code (SRS)
     * 
     * @param srsName The EPSG code to use. Method adds 'epsg:' if required
     */
    public void setCRS(String srsName) {
    	this.CRS = new CoordinateReferenceSystemImpl(srsName);
    }
    /** Get the extents of the envelope as two coordinates
     * 
     * @return	A coordinate array of bottom left/ top right (2 coords)
     */
    public Coordinate[] getExtents() {
    	Coordinate ret[] = new Coordinate[2];
    	ret[0] = new Coordinate(getMinX(), getMinY(), getMinZ());
    	ret[1] = new Coordinate(getMaxX(), getMaxY(), getMaxZ());
    	return ret;
    }

	@Override
	public boolean contains(BoundingBox bbox) {
		if (bbox==null) return false;
		return super.contains(new BoundingBoxImpl(bbox));
	}

	@Override
	public void include(BoundingBox bbox) {
		if (bbox==null) return;
		super.expandToInclude( new BoundingBoxImpl(bbox) );
	}
	@Override
	public boolean intersects(BoundingBox bbox) {
		if (bbox==null) return false;
		return super.intersects(new BoundingBoxImpl(bbox));
	}
	public boolean intersects(BoundingBoxImpl re) {
		return super.intersects(re);
	}
	public boolean contains(BoundingBoxImpl re) {
		return super.contains(re);
	}

	/** Get a centre value. 0=X, Y=1, Z=2;
	 * 
	 * @return The relevant coordinate or isNaN if isNull
	 * 
	 */
	public double getCenter(int XYZ) throws IndexOutOfBoundsException {
    	if (isNull()) return Double.NaN;
		Coordinate c = new Coordinate( centre() );
		switch (XYZ) {
			case 0:
				return c.x;
			case 1:
				return c.y;
			case 2:
				return (getMinZ() + getMaxZ()) /2.0;
		}
		return Double.NaN;
	}
	@Override
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return this.CRS;
	}

	@Override
	/** Always returns 3 */
	public int getDimension() {
		return 3;
	}


	@Override
	public double getMaximum(int XorY) throws IndexOutOfBoundsException {
		switch (XorY) {
		case 0:
			return getMaxX();
		case 1:
			return getMaxX();
		case 2:
			return getMaxZ();
		}
		return 0;
	}
	@Override
	public double getMinimum(int XorY) throws IndexOutOfBoundsException {
		switch (XorY) {
		case 0:
			return getMinX();
		case 1:
			return getMinX();
		case 2:
			return getMinZ();
		}
		return 0;
	}
	@Override
	public double getMedian(int arg0) throws IndexOutOfBoundsException {
		return getMedian(arg0);
	}
	@Override
	public DirectPosition getLowerCorner() {return null;}

	@Override
	public double getSpan(int arg0) throws IndexOutOfBoundsException {
		return getSpan(arg0);
	}

	@Override
	public DirectPosition getUpperCorner() {return null;}

	@Override
	public boolean contains(DirectPosition arg0) {
		return super.contains(
				new Coordinate(arg0.getCoordinate()[0],arg0.getCoordinate()[1] )
				);
	}

	@Override
	public void include(double x, double y) {
		expandToInclude(x, y);
	}

	@Override
	public boolean isEmpty() {
		return isNull();
	}

	@Override
	/** Not implemented - Create a new instance instead */
	public void setBounds(BoundingBox bbox) {}

	@Override
	public BoundingBox toBounds(CoordinateReferenceSystem crs)
			throws TransformException {
		/*TODO: Implement mechanism for checking ProjectionUtils class exists?
		 * This method is not used by GeoPackage or Context */
		try {
			//return ProjectionUtils.reproject(this, crs.getName().getCodeSpace() + ":" + crs.getName().getCode() );
		} catch (Exception e) {
			throw new TransformException(e.getLocalizedMessage());
		}
		
		return null;
	}


	@Override
	public String toString() {
		return super.toString();
	}



	
}
