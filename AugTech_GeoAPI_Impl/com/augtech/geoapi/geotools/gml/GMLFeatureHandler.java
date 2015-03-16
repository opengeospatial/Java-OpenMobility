package com.augtech.geoapi.geotools.gml;

import java.util.ArrayList;

import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.helpers.XMLFilterImpl;



public class GMLFeatureHandler extends XMLFilterImpl implements IFeatureHandler {
    public ArrayList<SimpleFeature> featureContainer;

    /**
     * Creates a new instance of a Feature Receiver
     *
     * @param container The feature container to store the new features
     */
    public GMLFeatureHandler(ArrayList<SimpleFeature> container) {
        this.featureContainer = container;
    }
	@Override
	public void feature(SimpleFeature feature) {
		featureContainer.add(feature);
		
	}
}
