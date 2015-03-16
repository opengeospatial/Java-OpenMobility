package com.augtech.geoapi.geotools.gml;

import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.ContentHandler;


public interface IFeatureHandler extends ContentHandler {
    /**
     * Receives simple feature from child.
     */
    void feature(SimpleFeature feature);
   
}
