package com.augtech.geoapi.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Coordinate;

public class OSMTile {
	/** Standard pixel size for a 256*256 raster at zoom level 18 for a Spherical Mercator
	 * project - i.e. EPSG:3857 Google, OSM, Bing tile */
	public static final double PIXEL_256_LENGTH_AT_ZOOM18 = 0.597165;
	
	/** The world extents for the EPSG:3857 tile matrix set */
	public static final BoundingBox OSM_TMS_EXTENT = new BoundingBoxImpl(
			-20037508.34, 20037508.34, -20037508.34, 20037508.34,
			new CoordinateReferenceSystemImpl("3857"));
	
	public static final double WGS84_EQUATOR = 6378137.0;
	private static final double WGS84_CONV = 20037508.34;
	private static final double OSMOriginShift = 2 * Math.PI * WGS84_EQUATOR / 2.0;
	
	int xRef;
	int yRef;
	int zoom;
	
	/** Create a new OSMTile
	 * 
	 * @param xRef
	 * @param yRef
	 * @param zoom
	 */
	public OSMTile(int xRef, int yRef, int zoom) {
		this.xRef = xRef;
		this.yRef = yRef;
		this.zoom = zoom;
	}
	/** Create a new OSMTile from a string.
	 * Only the last three parts of the string are used.
	 * 
	 * @param tileKey [xRef-yRef-zoom]
	 */
	public OSMTile(String tileKey) {
		if (!tileKey.matches(".*[0-9]+-[0-9]+-[0-9]+")) return;
		
		String[] id = tileKey.split("-");
		xRef = Integer.valueOf(id[id.length-3]);
		yRef = Integer.valueOf(id[id.length-2]);
		zoom = Integer.valueOf(id[id.length-1]);
	}
	/** Get the zoom level of this tile
	 * 
	 * @return
	 */
	public int getZoom() {
		return this.zoom;
	}
	/**
	 * 
	 * @return
	 */
	public int getX() {
		return this.xRef;
	}
	/**
	 * 
	 * @return
	 */
	public int getY() {
		return this.yRef;
	}
	/** Gets 9 tiles centred on this tile.
	 * 
	 * @return A Set of OSMTile's
	 */
	public Set<OSMTile> getAdjacentTiles() {
		Set<OSMTile> res = new HashSet<OSMTile>();
		
		res.add( new OSMTile(xRef,yRef+1,zoom) );
		res.add( new OSMTile(xRef,yRef-1,zoom) );
		res.add( new OSMTile(xRef+1,yRef+1,zoom) );
		res.add( new OSMTile(xRef+1,yRef-1,zoom) );
		res.add( new OSMTile(xRef-1,yRef+1,zoom) );
		res.add( new OSMTile(xRef-1,yRef-1,zoom) );
		res.add( new OSMTile(xRef+1,yRef,zoom) );
		res.add( new OSMTile(xRef-1,yRef,zoom) );
		res.add( new OSMTile(xRef,yRef,zoom) );
		
		return res;
	}
	/** Get a centre point and diameter (in metres) for this tile.
	 * This method does not take pixel size into account, therefore assumes 1m per pixel.
	 * 
	 * @return float[] as X (lon), Y (lat), Diameter (m). Diameter rounded to nearest 1/10th metre
	 */
	public float[] getAsRadius() {
		BoundingBoxImpl tileBounds = (BoundingBoxImpl) getLatLongBounds();
		double diam = Math.cos(tileBounds.getMaxY() * Math.PI / 180f) * 2 * Math.PI * WGS84_EQUATOR / Math.pow(2, zoom);
		return new float[]{
				(float) tileBounds.getCenter(0),
				(float) tileBounds.getCenter(1),
				(float) (Math.floor((diam * 10)+0.5))/10 };// Round to 1dp
	}
	/** Get the four sub-tiles (the next smallest tiles)
	 * Order is top-left to top-right, bottom-left, bottom-right
	 * @return int[4][] containing the sub-tile references
	 */
	public OSMTile[] getOSMSubTiles() {
		OSMTile[] res = new OSMTile[4];

		res[0] = new OSMTile(2*xRef, 2*yRef, zoom+1);
		res[1] = new OSMTile((2*xRef)+1, 2*yRef, zoom+1);
		res[2] = new OSMTile(2*xRef, (2*yRef)+1, zoom+1);
		res[3] = new OSMTile((2*xRef)+1, (2*yRef)+1, zoom+1);

		return res;
	}
	/**
	 * 
	 * @param tiles
	 * @return
	 */
	public static List<String> getSortedList(OSMTile[] tiles) {
		List<String> tileIDs = new ArrayList<String>();
		for (OSMTile o : tiles) tileIDs.add(o.toString());
		// Swap so sort works in our texture order
		for (int t=0; t<tileIDs.size(); t++) tileIDs.set(t, swapXY(tileIDs.get(t)) );
		Collections.sort(tileIDs);
		for (int t=0; t<tileIDs.size(); t++) tileIDs.set(t, swapXY(tileIDs.get(t)) );
		return tileIDs;
	}
	private static String swapXY(String val) {
		String[] s = val.split("-");
		return String.format("%s-%s-%s",s[1], s[0], s[2]);
	}
	/** Get the parent (next biggest) tile to this one
	 * 
	 * @return An OSMTile
	 */
	public OSMTile getOSMParentTile() {
		return new OSMTile(xRef/2, yRef/2, zoom-1);
	}
	/** Gets the bounding coordinates for this tile in the
	 * native CRS of EPSG:3857
	 * 
	 * @param pixelSize The number of pixels in width/ height
	 * @return
	 */
	public BoundingBox getNativeBounds(int pixelSize) {

		double[] ll = osmTileToCoords(xRef, yRef, zoom, pixelSize);
		double[] ur = osmTileToCoords(xRef+1, yRef+1, zoom, pixelSize);

		return new BoundingBoxImpl(ll[0], ur[0], ll[1], ur[1], new CoordinateReferenceSystemImpl("3857") );
		
	}
	/** Get the bounding coordinates for this tiles in WGS84 latitude, longitude
	 * with a pixel size of 256
	 * 
	 * @return
	 */
	public BoundingBox getLatLongBounds() {
		return getLatLongBounds(256);
	}
	/** Get the bounding coordinates for this tiles in WGS84 latitude, longitude
	 * 
	 * @param pixelSize The number of pixels in width/ height
	 * @return
	 */
	public BoundingBox getLatLongBounds(int pixelSize) {
		BoundingBox nb = getNativeBounds( pixelSize );

		return new BoundingBoxImpl(
				osmToWGS84(nb.getMinX(), nb.getMinY()),
				osmToWGS84(nb.getMaxX(), nb.getMaxY()),
			    "epsg:4326"
				);
	}
	/** Get an OSMTile for the supplied WGS84 latitude/ longitude at the
	 * specified zoom level.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param zoom
	 * @return
	 */
	public static OSMTile getTile(double latitude, double longitude, int zoom) {
		return new OSMTile(
				getOSMXTile(longitude, zoom),
				getOSMYTile(latitude, zoom),
				zoom);
	}
	/** Get the X tile reference for an OSM tile
	 * 
	 * @param lon Longitude position
	 * @param zoom The required zoom level (typically 1-18)
	 * @return The X tile number
	 */
	public static int getOSMXTile(double lon, int zoom) {
		return (int)Math.floor( (lon + 180d) / 360d * (1<<zoom) );
	}
	/** Get the Y tile reference for an OSM tile
	 * 
	 * @param lat latitude position
	 * @param zoom The required zoom level (typically 1-18)
	 * @return The Y tile number
	 */
	public static int getOSMYTile(double lat, int zoom) {
		return (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
	}
	/** Get a lon/ lat Coordinate from native coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private static Coordinate osmToWGS84(double x, double y) {
		  double lon = (x / WGS84_CONV) * 180d;
		  double lat = (y / WGS84_CONV) * 180d;
			
		  lat = 180d/Math.PI * (2d * Math.atan(Math.exp(lat * Math.PI / 180d)) - Math.PI / 2d);

		  return new Coordinate(lon, lat,0);
	}
	/** Calculate the native 3857 coordinates from a tile reference. 
	 * 
	 * @param tileX
	 * @param tileY
	 * @param zoom
	 * @param tileSize
	 * @return
	 */
	private double[] osmTileToCoords(int tileX, int tileY, int zoom, int tileSize) {
		double tileRes = (2d * Math.PI * WGS84_EQUATOR) / (tileSize * Math.pow(2d,zoom));
		
		int tpx = tileX*tileSize+1;
		int tpy = tileY*tileSize+1;

        double mx = tpx * tileRes - OSMOriginShift;
        double my = tpy * tileRes - OSMOriginShift;
        return new double[]{mx, -my};
	}
	/** Get all the OSM tiles at the supplied zoom that are required
	 * to cover the supplied BoundingBox.
	 * 
	 * @param re The BoundingBox to test against
	 * @param zoom The zoom level to build the tiles for
	 * @return An array of tiles
	 */
	public static Set<OSMTile> getOSMTilesForArea(BoundingBox re, int zoom) {
		Set<OSMTile> allTiles = new HashSet<OSMTile>();
		
		OSMTile origin = getTile(re.getMinY(), re.getMinX(), zoom);
		OSMTile currPos = null;
		
		boolean doneX = false;
		boolean doneY = false;
		int x = origin.xRef;
		int y = origin.yRef;
		BoundingBox curr = null;
		
		while(!doneY) {
			while (!doneX) {
				currPos = new OSMTile(x, y, zoom);
				curr = currPos.getLatLongBounds(256);
				if (re.contains(curr) || re.intersects(curr)) {
					allTiles.add( currPos );
				} else {
					doneX = true;
				}
				x+=1;
			}
			// Increment Y and reset X
			
			y-=1;
			x = origin.xRef;
			doneX = false;
			
			// Check Y movement still in range
			curr = new OSMTile(x, y, zoom).getLatLongBounds(256);
			if (!re.contains(curr) && !re.intersects(curr)) doneY = true;
			
		}
		
		return allTiles;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s-%s-%s", xRef, yRef, zoom);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xRef;
		result = prime * result + yRef;
		result = prime * result + zoom;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OSMTile))
			return false;
		OSMTile other = (OSMTile) obj;
		if (xRef != other.xRef)
			return false;
		if (yRef != other.yRef)
			return false;
		if (zoom != other.zoom)
			return false;
		return true;
	}
	
	
	
}
