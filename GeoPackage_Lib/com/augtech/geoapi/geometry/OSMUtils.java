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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opengis.geometry.BoundingBox;
import com.vividsolutions.jts.geom.Coordinate;

/** A collection of static functions used for calculating to/ from
 * WGS84 Latitude/ Longitude and the Slippy tile format used by Open Street Map,
 * Google Maps and others.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class OSMUtils {
	public static final double PIXEL_LENGTH_AT_ZOOM18 = 0.597165;
	private static final double OSMOriginShift = 2 * Math.PI * 6378137 / 2.0;
	public static final double WGS84_EQUATOR = 6378137.0;
	/** Gets 9 tiles in OSM/ Google format centred on the xy location passed.
	 * 
	 * @param xy
	 * @param zoom
	 * @return An int array of tiles, with each tile specified as an array of 
	 * X, Y, Zoom
	 */
	public static int[][] getOSMAdjacentTiles(int[] xy, int zoom) {
		int x = xy[0];
		int y = xy[1];
		int res[][] = new int[9][];
		res[0] = new int[]{x,y+1,zoom};
		res[1] = new int[]{x,y-1,zoom};
		res[2] = new int[]{x+1,y+1,zoom};
		res[3] = new int[]{x+1,y-1,zoom};
		res[4] = new int[]{x-1,y+1,zoom};
		res[5] = new int[]{x-1,y-1,zoom};
		res[6] = new int[]{x+1,y,zoom};
		res[7] = new int[]{x-1,y,zoom};
		res[8] = new int[]{x,y,zoom};
		
		return res;
	}
	/** Get the four sub-tiles (the next smallest tiles) for the passed tile
	 * reference
	 * 
	 * @param zoom
	 * @param x
	 * @param y
	 * @return int[4][] containing the sub-tile references
	 */
	public static List<int[]> getOSMSubTiles(int zoom, int x, int y) {
		List<int[]> ret = new ArrayList<int[]>();
		ret.add( new int[]{zoom+1, 2*x, 2*y} );
		ret.add( new int[]{zoom+1, (2*x)+1, 2*y} );
		ret.add( new int[]{zoom+1, 2*x, (2*y)+1} );
		ret.add( new int[]{zoom+1, (2*x)+1, (2*y)+1} );
		
		return ret;
	}

	/** Get the parent (next biggest) tile for the passed tile
	 * 
	 * @param zoom
	 * @param x Tile reference
	 * @param y Tile reference
	 * @return An int array of Zoom, X and Y
	 */
	public static int[] getOSMParentTile(int zoom, int x, int y) {
		return new int[]{zoom-1, x/2, y/2};
	}
	/** Get all surrounding tiles for the passed OSM tile array.
	 * The returned array will NOT include any of the original tiles 
	 * 
	 * @param allTiles
	 * @return An int array of tiles, with each tile specified as an array of 
	 * X, Y, Zoom
	 */
	public static int[][] getOSMSurroundingTiles(int[][] allTiles) {
		Map<String, Boolean> central = new HashMap<String, Boolean>();
		Map<String, int[]> tiles = new HashMap<String, int[]>();
		for (int j=0;j<allTiles.length;j++) {
			central.put(
					String.format("%s,%s,%s", allTiles[j][0], allTiles[j][1], allTiles[j][2]),
					true);
			
		}
		
		// Ger all adjacent tiles
		for (int j=0;j<allTiles.length;j++) {
			int[][] tmp = OSMUtils.getOSMAdjacentTiles(allTiles[j], allTiles[j][2]);
			
			for (int k=0;k<tmp.length;k++) {
				String key = String.format("%s,%s,%s", tmp[k][0], tmp[k][1], tmp[k][2]);
				// Only add to new set if not in original set
				if (tiles.get(key)==null && central.get(key)==null) {
					tiles.put(key, tmp[k]);
				}
			}
		}

		// Transfer the total set back to 'allTiles'
		int[][] surrTiles = new int[tiles.size()][];
		int p = 0;
		for (Entry<String, int[]> temp : tiles.entrySet()) {
			surrTiles[p] = temp.getValue();
			p++;
		}
		return surrTiles;
	}

	/** Get all the OSM tiles at the supplied zoom that are required
	 * to cover the supplied BoundingBox.
	 * 
	 * @param re The BoundingBox to test against
	 * @param zoom The zoom level to build the tiles for
	 * @return An int array of tiles, with each tile specified as an array of 
	 * X, Y, Zoom
	 */
	public static int[][] getOSMTiles(BoundingBox re, int zoom) {
		Map<String, int[]> allTiles = new HashMap<String, int[]>();
		
		int[] pos = new int[]{
				getOSMXTile(re.getMinX(), zoom), 
				getOSMYTile(re.getMinY(), zoom),
				zoom};
		
		boolean doneX = false;
		boolean doneY = false;
		int x = pos[0];
		int y = pos[1];

		while(!doneY) {
			while (!doneX) {
				BoundingBoxImpl curr = getOSMBounds(x, y, zoom);
				if (re.contains(curr) || re.intersects(curr)) {
					allTiles.put(String.format("%s/%s/%s", zoom, x, y), new int[]{x,y,zoom});
				} else {
					doneX = true;
				}
				x+=1;
			}
			// Increment Y and reset X
			y-=1;
			x = pos[0];
			doneX = false;
			
			// Check Y movement still in range
			BoundingBoxImpl curr = getOSMBounds(x, y, zoom);
			if (!re.contains(curr) && !re.intersects(curr)) doneY = true;
			
		}
		
		// Convert the list back to an integer array
		int[][] ret = new int[allTiles.size()][];
		int i = 0;
		for (int[] m : allTiles.values() ) {
			ret[i] = m;
			i++;
		}
		
		return ret;
		
	}

	/** Gets a lat/long envelope for all tiles around an original xy location
	 * 
	 * @param xy An array of x reference, y reference
	 * @param zoom The zoom level
	 * 
	 * @return A new ReferencedEnvelope in WGS84
	 */
	public static BoundingBoxImpl getOSMSurroundingBounds(int[] xy, int zoom) {
		int[][] allTiles = OSMUtils.getOSMAdjacentTiles(xy, zoom);
		BoundingBoxImpl ret = new BoundingBoxImpl("EPSG:4326");
		for (int j=0;j<allTiles.length;j++) {
			ret.expandToInclude(
					getOSMBounds(allTiles[j][0], allTiles[j][1], allTiles[j][2])
					);
		}
		return ret;
	}
	/** Get an OSM file reference based on lat/long 
	 * <p>From <A href="http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">Slippy Map tiles</A>
	 * 
	 * @param lat
	 * @param lon
	 * @param zoom
	 * @return A string of the format zoom/X tile/Y tile
	 */
	public static String getOSMTileReference(final double lat, final double lon, final int zoom) {
		int xtile = getOSMXTile(lon, zoom);
		int ytile = getOSMYTile(lat, zoom);
		return String.format("%s/%s/%s", zoom, xtile, ytile);
	}
	/** Get the lat/ long bounds from of an OSM tile reference
	 * <p>From <A href="http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">Slippy Map tiles</A>
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return A new ReferencedEnvelope in WGS84
	 */
	public static BoundingBoxImpl getOSMBounds(final int x, final int y, final int zoom) {
		double[] ll = osmTileToCoords(x, y, zoom);
		double[] ur = osmTileToCoords(x+1, y+1, zoom);

		BoundingBoxImpl ret = new BoundingBoxImpl(
				GoogleBingtoWGS84Mercator(ll[0], ll[1]),
				GoogleBingtoWGS84Mercator(ur[0], ur[1]),
			    "epsg:4326"
				);
		return ret;
	}
	/** Get a centre point and diameter (in metres) for a tile.
	 * This method does not take pixel size into account, therefore assumes 1m per pixel.
	 * 
	 * @param xTile
	 * @param yTile
	 * @param zoom
	 * @return float[] as X (lon), Y (lat), Diameter (m). Diameter rounded to nearest 1/10th metre
	 */
	public static float[] getOSMTileAsRadius(final int xTile, final int yTile, final int zoom) {
		BoundingBoxImpl tileBounds = getOSMBounds(xTile, yTile, zoom);
		double diam = Math.cos(tileBounds.getMaxY() * Math.PI / 180) * 2 * Math.PI * WGS84_EQUATOR / Math.pow(2, zoom);
		return new float[]{
				(float) tileBounds.getCenter(0),
				(float) tileBounds.getCenter(1),
				(float) (Math.floor((diam * 10)+0.5))/10 };// Round to 1dp
	}

	private static double[] osmTileToCoords(int tileX, int tileY, int zoom) {
		int tileSize = 256;
		double tileRes = (2 * Math.PI * 6378137) / (tileSize * Math.pow(2,zoom));
		
		int tpx = tileX*tileSize+1;
		int tpy = tileY*tileSize+1;

        double mx = tpx * tileRes - OSMOriginShift;
        double my = tpy * tileRes - OSMOriginShift;
        return new double[]{mx, -my};
	}
	/** Get the X tile reference for an OSM tile
	 * 
	 * @param lon Longitude position
	 * @param zoom The required zoom level (typically 1-18)
	 * @return The X tile number
	 */
	public static int getOSMXTile(double lon, int zoom) {
		return (int)Math.floor( (lon + 180) / 360 * (1<<zoom) );
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

	private static Coordinate GoogleBingtoWGS84Mercator (double x, double y) {
		  double lon = (x / 20037508.34) * 180;
		  double lat = (y / 20037508.34) * 180;
			
		  lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

		  return new Coordinate(lon, lat,0);
	}

}
