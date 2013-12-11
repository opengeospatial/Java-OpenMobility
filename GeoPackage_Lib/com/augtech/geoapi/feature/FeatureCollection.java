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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.vividsolutions.jts.geom.Geometry;

/** The base collection for storing all of awila's geographically
 * based features. Uses a {@link CopyOnWriteArrayList} to ensure their is no
 * clash with the rendering engined when items are added or removed from the collection.<p>
 * Use {@link #addAll(Collection)} to maintain speed where possible.
 * 
 * @author Augmented Technologies Ltd
 *
 */
public class FeatureCollection extends CopyOnWriteArrayList<SimpleFeature> {
	private static final long serialVersionUID = -926972509429116720L;
	
	/** All features in this collection by their FeatureType name */
	protected Map<Name, Set<SimpleFeature>> typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
	protected Map<String, SimpleFeature> idIndex = new HashMap<String, SimpleFeature>();
	protected BoundingBox bounds;
	/** All feature types that are permissible, by their name */
	protected Map<Name, SimpleFeatureType> availableFeatureTypes = new HashMap<Name, SimpleFeatureType>();
	private Map<Integer, Set<SimpleFeature>> tileIndex = new HashMap<Integer, Set<SimpleFeature>>();
	
	
	/** Create a new feature collection, with the specified types available to
	 * it.
	 * 
	 * @param featureTypes The featureTypes permissible on this collection, or
	 * Null for a temporary collection.
	 */
	public FeatureCollection(Map<Name, SimpleFeatureType> featureTypes) {
		if (featureTypes!=null) {
			availableFeatureTypes = featureTypes;
		}
	}

	/** Get a specific feature
	 * 
	 * @param featureID
	 * @return The SimpleFeature
	 */
	public SimpleFeature get(String featureID) {
		return idIndex.get(featureID);
	}
	/** Does this collection have this feature
	 * 
	 * @param featureID
	 * @return True if feature exists
	 */
	public boolean hasFeature(String featureID) {
		return idIndex.containsKey(featureID);
	}
	/** Gets the Map of predefined {@link SimpleFeatureType} .
	 * 
	 * @return Map<Name, SimpleFeatureType>
	 */
	public Map<Name, SimpleFeatureType> getDefinedTypes() {
		return this.availableFeatureTypes;
	}

    /** Get all the features in this collection that intersect or are contained
     * by the passed extents.
     * 
     * @param extent
     * @return A new ArrayList<SimpleFeature> containing the relevant features
     */
    public ArrayList<SimpleFeature> getFeatures(BoundingBox extent) {
    	ArrayList<SimpleFeature> tmp = new ArrayList<SimpleFeature>();
    	
    	Iterator<SimpleFeature> iterator = iterator();
    	while (iterator.hasNext()) {
    		SimpleFeature f = iterator.next();
    		if (extent.contains( f.getBounds() ) || 
    			extent.intersects( f.getBounds() )) {
    			tmp.add(f);
    		}
    	}
    	return tmp;
    }
    /** Get a feature iterator for the whole collection
     * 
     * @return Iterator<SimpleFeature>
     */
    public Iterator<SimpleFeature> getFeatures() {
    	return this.iterator();
    }
	/** Gets all the features for a specific type name. 
	 * 
	 * @param name The Name to look for
	 * @return A Set of Feature's
	 * @throws Exception If the NameImpl is not found in the index.
	 */
	public Set<SimpleFeature> getFeatures(Name name) throws Exception {
		if (typeNameIndex.get(name)==null) throw new Exception("Type not found");
		return typeNameIndex.get(name);
	}

//    /** Create a spatial index of all the features within this collection based
//     * on tile names. <p>
//     * The index is cached, so as long as no features are added or removed, and 
//     * the zoom level is the same as before, the index will not be rebuilt.
//     * 
//     * @param zoom	The tile zoom level for the tiles
//     * @return A new Map<String, ArrayList<SimpleFeature>> containing the features
//     * @throws Exception 
//     */
//    public Map<String, ArrayList<SimpleFeature>> getOSMTileIndex(int zoom) throws Exception {
//    	if (tileIndexLevel==zoom && tileIndexDirty==false) return tileIndex;
//    	
//		tileIndex = new HashMap<String, ArrayList<SimpleFeature>>();
//		ArrayList<SimpleFeature> curr = null;
//		String sTile = "";
//		
//		for (SimpleFeature sf : this) {
//			Object geom = sf.getDefaultGeometry();
//			Coordinate[] coords = null;
//			if (geom instanceof Geometry) {
//				Geometry g = (Geometry)sf.getDefaultGeometry();
//				coords = g.getCoordinates();
//
//			} else if ( geom instanceof BoundingBoxImpl) {
//				BoundingBoxImpl re = (BoundingBoxImpl)geom;
//				coords = new Coordinate[] {
//						new Coordinate(re.getMinX(), re.getMinY(), re.getMinZ()),
//						new Coordinate(re.getMaxX(), re.getMaxY(), re.getMaxZ())
//						};
//			} else {
//				throw new Exception("Unknown Geometry whilst building index");
//			}
//			
//
//			
//			for (Coordinate c : coords) {
//				sTile = OSMUtils.getOSMTileReference(c.y, c.x, zoom);
//				curr = tileIndex.get(sTile);
//				if (curr==null) curr = new ArrayList<SimpleFeature>();
//				
//				curr.add(sf);
//				tileIndex.put(sTile,curr);
//			}
//
//		}
//		
//		tileIndexDirty = false;
//		tileIndexLevel = zoom;
//		
//		return tileIndex;
//    }

    /** Get a list of FeatureType Names from this feature collection
     * 
     * @param allNames	True for all available FeatureTypes, False for loaded ones only
     * @param sort	Sort the list by Name. This is a bit slower, so don't use it if not required
     * 
     * @return	FeatureType Names
     */
    public final ArrayList<Name> getNames(boolean allNames, boolean sort) {
    	ArrayList<Name> ret = new ArrayList<Name>();
    	
    	if (allNames) {
    		if(sort) {
    			ret = sortNames( availableFeatureTypes.keySet() );
    		} else {
    			for (Name tmp : availableFeatureTypes.keySet()) {
    				ret.add(tmp);
    			}
    		}
    	} else {
    		if (sort) {
    			ret = sortNames( typeNameIndex.keySet() );
    		} else {
    			for (Name tmp : typeNameIndex.keySet()) {
    				ret.add(tmp);
    			}
    		}
    	}
    	return ret;
    }
    /** Sort a key set
     * @param toSort
     * 
     * @return ArrayList<Name>
     */
    private ArrayList<Name> sortNames(Set<Name> toSort) {
		ArrayList<Name> ret = new ArrayList<Name>();
		for (Name tmp : toSort) {
			ret.add((NameImpl)tmp);
		}
		ret.trimToSize();
		
		java.util.Collections.sort(ret, new NameComparator() );
		
		return ret;
    }
	/** Copy all new features from the passed collection into this one if their 
	 * feature ID does not already exist. The feature type(s) from the inbound loader
	 * are discarded in favour of the available types on this collection<p>
	 * Any new {@link AttributeTypeImpl} found on the inbound {@link SimpleFeatureTypeImpl} 
	 * are appended to the current attribute type list, with the existing values and 
	 * overall attribute order being maintained. The {@link Name} is used to find the
	 * matching defined type.<p>
	 * 
	 * @param from The {@link FeatureLoader} containing the source data
	 * @return The number of features copied to this collection
	 */
	public final int mergeAll(List<SimpleFeature> from) {
		if (from==null) return -1;
		ArrayList<SimpleFeature> tmpColl = new ArrayList<SimpleFeature>();

		int i=0;
		for (SimpleFeature newFeature : from) {
			if (this.idIndex.containsKey(newFeature.getID())) continue;
			
			Name newFeatName = newFeature.getType().getName();
			SimpleFeatureType sft = availableFeatureTypes.get( newFeatName );

			// Try harder..find by local name?
			if (sft==null) {
				for (Entry<Name, SimpleFeatureType> aft : availableFeatureTypes.entrySet()) {
					if (aft.getKey().getLocalPart().equals(newFeatName.getLocalPart())) {
						sft = aft.getValue();
						break;
					}
				}
			}
			// If we failed to get stored type, skip to next feature
			if (sft==null) continue;
			if ((sft instanceof SimpleFeatureTypeImpl)==false) continue;
			SimpleFeatureTypeImpl definedType = (SimpleFeatureTypeImpl)sft;
			
			ArrayList<Object> newValues = new ArrayList<Object>();
			
			/* Copy new attribute types over to existing type set, 
			 * maintaining existing definition and order of default attribute 
			 * implementations.*/
			List<AttributeType> newFeatAttrTypes = newFeature.getFeatureType().getTypes();
			List<AttributeType> useATypes = new ArrayList<AttributeType>();
			
			for (int n=0; n < newFeatAttrTypes.size(); n++) {
				AttributeType newAT = null;
			
				if (definedType.getAttributeCount()>0) {
					/* The name defined as default attribute may not be the same
					 * as the qualified name coming from the data, therefore just
					 * match on the local name */
					newAT = definedType.getType(newFeatAttrTypes.get(n).getName().getLocalPart());
				}
				/* The newly defined type doesn't exist yet, so get the definition
				 * and add to our list */
				if (newAT==null) newAT = newFeatAttrTypes.get(n);
				
				useATypes.add(newAT);
			}
			
			// update our dataset defined type and re-store
			definedType.addAttributeTypes(useATypes);//The new type set
			//availableFeatureTypes.put(updateKey, definedType);
			
			/* Now get the attribute value from the new feature based on 
			 * the new type list */
			for (int n=0;n<definedType.getAttributeCount(); n++) {
				newValues.add( newFeature.getAttribute( definedType.getType(n).getName().getLocalPart() )  );
			}
			
			newValues.trimToSize();
			newFeature.setAttributes(newValues);
			((SimpleFeatureImpl)newFeature).setType(definedType);
			tmpColl.add( newFeature );
			
			i++;

		}
		
		this.addAll( tmpColl );

		return i;
	}
    /** Does the collection contain this feature?
     * 
     * @param feature
     * @return True if it does
     */
	public boolean hasFeature(SimpleFeature feature) {
		return idIndex.containsKey(feature.getID());
	}
	/** Helper function to maintain indexes - Must be called when adding a feature
	 * 
	 */
	private void addToIndex(SimpleFeature feature) {
		if (idIndex==null) idIndex = new HashMap<String, SimpleFeature>();
		idIndex.put(feature.getID(), feature);
		
		if (typeNameIndex==null) typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
		Name type = feature.getType().getName();
		Set<SimpleFeature> prev = typeNameIndex.get(type);
		if (prev==null) prev = new HashSet<SimpleFeature>();
		prev.add(feature);
		typeNameIndex.put(type, prev);
		
		// Image features are separated by - and last parts are zoom-xref-yref
		if (feature.getID().matches(".*-[0-9]+-[0-9]+-[0-9]+")) {
			
			String[] id = feature.getID().split("-");
			int zoom = Integer.valueOf(id[id.length-3]);
			prev = tileIndex.get(zoom);
			if (prev==null) prev = new HashSet<SimpleFeature>();
			prev.add(feature);
			tileIndex.put(zoom, prev);
			
		} else {
			//TODO Something for features..?
		}
		
	}
	/** Get all features associated with a specific zoom level.
	 * In the case of vector features this will be everything in the collection
	 * as they are not bound by zoom scales
	 * 
	 * @param zoomLevel The level (generally 0-18)
	 * @return
	 */
	public Set<SimpleFeature> getFeaturesForZoom(int zoomLevel) {
		return tileIndex.get(zoomLevel);
	}
	/** Helper function to maintain indexes - Must be called when removing a feature
	 * 
	 * @param feature
	 */
	void removeFromIndex(SimpleFeature feature) {
		if (feature==null) return;
		idIndex.remove( feature.getID() );
		Name type = feature.getType().getName();
		Set<SimpleFeature> prev = typeNameIndex.get(type);
		boolean rev = false;
		if (prev!=null) rev = prev.remove( feature );
		if (rev) {
			if (prev.size()>0) {
				typeNameIndex.put(type,prev);
			} else {
				typeNameIndex.remove(type);
			}
		}
	}


	/** Get the bounding envelope for all features within this collection.
	 * If the bounds are not already set, this will build the spatialIndex and set the bounds
	 * from that.<p>
	 * Its assumed that all features in the collection have the same CoordinateReferenceSystem,
	 * therefore the BoundingBox has its SRSID set to the first feature's SRSID
	 * 
	 * @return ReferencedEnvelope
	 */
	public final BoundingBox getBounds() {
		if (bounds==null) {
			int srsID =( (Geometry)this.get(0).getDefaultGeometry()).getSRID();
			BoundingBox re = new BoundingBoxImpl(""+srsID);
			for (SimpleFeature f : this) {
				re.include(f.getBounds());
			}
			bounds = re;
		}
		return bounds;
	}

	//---------------- Standard ArrayList methods -----------------------//
	
	@Override
	public boolean addAll(Collection<? extends SimpleFeature> collection) {
		boolean ret = super.addAll(collection);
		for (SimpleFeature f : collection) {
			addToIndex(f);
		}
		return ret;
	}
	@Override
	public boolean addAll(int index, Collection<? extends SimpleFeature> collection) {
		boolean ret = super.addAll(index, collection);
		for (SimpleFeature f : collection) {
			addToIndex(f);
		}
		return ret;
	}
	@Override
	public boolean add(SimpleFeature feature) {
		super.add(feature);
		addToIndex(feature);
		return true;
	}
	@Override
	public void add(int idx, SimpleFeature feature) {
		super.add(idx, feature);
		addToIndex(feature);
	}
	@Override
	public SimpleFeature set(int index, SimpleFeature feature) {
		addToIndex(feature);
		return super.set(index, feature);
	}

	@Override
	public boolean remove(Object object) {
		SimpleFeature f = null;
		if (object instanceof String) {
			f = this.get(String.valueOf(object));
		} else {
			try {
				f = (SimpleFeature)object;
			} catch (Exception e) {
				return false;
			}
		}
		if (f==null) return false;
		
		removeFromIndex(f);
		return super.remove(object);
	}
	@Override
	public SimpleFeature remove(int idx) {
		SimpleFeature f = get(idx);
		removeFromIndex(f);
		return super.remove(idx);
	}
	
	/** Clear the whole container and resets all the indexes
	 */
	@Override
	public void clear() {
		super.clear();
		idIndex = new HashMap<String, SimpleFeature>();
		typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
		//tileIndex = new HashMap<String, ArrayList<SimpleFeature>>();
	}

	/** A comparator used to sort {@link Name} implementations.
	 *
	 */
	class NameComparator  implements Comparator<Name> {

		@Override
		public int compare(Name n1, Name n2) {
			return n1.getLocalPart().compareTo(n2.getLocalPart());
		}

	}
}