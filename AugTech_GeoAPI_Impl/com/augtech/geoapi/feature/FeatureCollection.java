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
import com.augtech.geoapi.geometry.OSMTile;
import com.vividsolutions.jts.geom.Geometry;

/** The base collection for storing all of awila's geographically
 * based features. Uses a {@link CopyOnWriteArrayList} to ensure their is no
 * clash with the rendering engined when items are added or removed from the collection.<p>
 * Use {@link #addAll(Collection)} to maintain speed where possible.
 * 
 * @author Augmented Technologies Ltd
 *
 */
public class FeatureCollection extends ArrayList<SimpleFeature> {
	private static final long serialVersionUID = -926972509429116720L;
	
	/** All features in this collection by their FeatureType name */
	protected Map<Name, Set<SimpleFeature>> typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
	protected Map<String, SimpleFeature> idIndex = new HashMap<String, SimpleFeature>();
	protected BoundingBox bounds;
	private Map<OSMTile, Set<SimpleFeature>> tileIndex = new HashMap<OSMTile, Set<SimpleFeature>>();
	
	
	/** Create a new feature collection, with the specified types available to
	 * it.
	 * 
	 * @param featureTypes The featureTypes permissible on this collection, or
	 * Null for a temporary collection.
	 */
	public FeatureCollection() {
//		if (featureTypes!=null) {
//			availableFeatureTypes = featureTypes;
//		}
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

    /** Get all the features in this collection that intersect or are contained
     * by the passed {@link BoundingBox}.
     * 
     * @param extent The bounding box to test against. This must be in the same
     * CRS as the features in order to return the correct features.
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
    /** Get a list of all SimpleFeatureType's from the SimpleFeature's currently
     * stored.
     * 
     * @return
     */
    public List<SimpleFeatureType> getCurrentTypes() {
    	List<SimpleFeatureType> ret = new ArrayList<SimpleFeatureType>();
    	for (Set<SimpleFeature> s : typeNameIndex.values()) {
    		if (s.size()>0) {
    			ret.add( s.iterator().next().getFeatureType() );
    		}
    	}
    	return ret;
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

    /** Get a list of FeatureType Names from the features currently loaded into this collection.
     * 
     * @param sort	Sort the list by Name. This is a bit slower, so don't use it if not required
     * 
     * @return	FeatureType Names
     */
    public final List<Name> getNames(boolean sort) {
    	List<Name> ret = new ArrayList<Name>();

    	for (Name tmp : typeNameIndex.keySet()) ret.add(tmp);


    	if (sort) {
    		java.util.Collections.sort(ret, new Comparator<Name>() {
    			@Override
    			public int compare(Name n1, Name n2) {
    				return n1.getLocalPart().compareTo(n2.getLocalPart());
    			} 
    		});
    	}
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
	public final List<String> mergeAll(Map<Name, SimpleFeatureType> definedTypes, List<SimpleFeature> from) {
		if (from==null) return null;
		ArrayList<SimpleFeature> tmpColl = new ArrayList<SimpleFeature>();
		List<String> featsMerged = new ArrayList<String>();

		for (SimpleFeature newFeature : from) {
			if (this.idIndex.containsKey(newFeature.getID())) continue;
			
			Name newFeatName = newFeature.getType().getName();
			SimpleFeatureType sft = definedTypes.get( newFeatName );

			// Try harder..find by local name?
			if (sft==null) {
				for (Entry<Name, SimpleFeatureType> aft : definedTypes.entrySet()) {
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
			
			featsMerged.add( newFeature.getID() );

		}
		
		this.addAll( tmpColl );

		return featsMerged;
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
		// ID Index
		if (idIndex==null) idIndex = new HashMap<String, SimpleFeature>();
		idIndex.put(feature.getID(), feature);
		
		// Type name index
		if (typeNameIndex==null) typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
		Name type = feature.getType().getName();
		Set<SimpleFeature> prev = typeNameIndex.get(type);
		if (prev==null) prev = new HashSet<SimpleFeature>();
		prev.add(feature);
		typeNameIndex.put(type, prev);
		
		// Tile Index
		// Image features are separated by - and last parts are xref-yref-zoom
		if (feature.getID().matches(".*-[0-9]+-[0-9]+-[0-9]+")) {
			OSMTile tile = new OSMTile(feature.getID());
			prev = tileIndex.get( tile );
			if (prev==null) prev = new HashSet<SimpleFeature>();
			prev.add(feature);
			tileIndex.put(tile, prev);
			
		} else {
			/* TODO Something for features..?
			 * get geometry bounds, calculate tile and insert into relevant index positions?
			 * This would then also work for removal...	 */
		}
		
	}
	/** Get all features associated with an OSM tile reference.
	 * 
	 * @param tileIdx The tile reference as zoom, XRef, YRef
	 * @return
	 */
	public Set<SimpleFeature> getFeaturesForTile(OSMTile tileIdx) {
		synchronized (tileIndex) {
			return tileIndex.get( tileIdx );
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
		Set<SimpleFeature> ret = new HashSet<SimpleFeature>();
		synchronized (tileIndex) {
			for (Entry<OSMTile, Set<SimpleFeature>>  val : tileIndex.entrySet()) {
				if (val.getKey().getZoom()==zoomLevel) ret.addAll( val.getValue() );
			}
		}
		return ret;
	}
	/** Helper function to maintain indexes - Must be called when removing a feature
	 * 
	 * @param feature
	 */
	protected void removeFromIndex(SimpleFeature feature) {
		if (feature==null) return;
		
		// ID Index
		idIndex.remove( feature.getID() );
		
		// Type name index
		Name type = feature.getType().getName();
		Set<SimpleFeature> prev = typeNameIndex.get(type);
		if ( prev.remove( feature ) ) {
			if (prev.size()>0) {
				typeNameIndex.put(type,prev);
			} else {
				typeNameIndex.remove(type);
			}
		}
		
		// Tile Index
		if (feature.getID().matches(".*-[0-9]+-[0-9]+-[0-9]+")) {
			OSMTile tile = new OSMTile(feature.getID());
			prev = tileIndex.get( tile );
			if ( prev.remove( feature ) ) {
				if (prev.size()>0) {
					tileIndex.put(tile, prev);
				} else {
					tileIndex.remove(type);
				}
			}
		} else {
			// Whatever for features
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
		} else if (object instanceof SimpleFeature) {
			f = (SimpleFeature)object;
		} else if (object instanceof Integer) {
			f = this.get(Integer.valueOf(String.valueOf(object)));
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
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		for (Object o : collection) {
			if (o instanceof SimpleFeature) {
				removeFromIndex( (SimpleFeature)o );
			} else if (o instanceof String) {
				removeFromIndex( this.get( String.valueOf( o ) ) );
			}
		}
		return super.removeAll(collection);
	}

	/** Clear the whole container and resets all the indexes
	 */
	@Override
	public void clear() {
		super.clear();
		idIndex = new HashMap<String, SimpleFeature>();
		typeNameIndex = new HashMap<Name, Set<SimpleFeature>>();
		tileIndex = new HashMap<OSMTile, Set<SimpleFeature>>();
	}

}