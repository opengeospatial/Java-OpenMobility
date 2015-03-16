/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package com.augtech.geoapi.geotools.gml;

/**
 * Creates the appropriate SubHandler element for a given OGC simple geometry
 * type.
 *
 * @author Rob Hranac, Vision for New York
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/gml/SubHandlerFactory.java $
 * @version $Id: SubHandlerFactory.java 30648 2008-06-12 19:22:35Z acuster $
 */
public class SubHandlerFactory {

    /** List of all valid OGC multi geometry types. */
    private static final java.util.Collection BASE_GEOMETRY_TYPES = new java.util.Vector(java.util.Arrays
            .asList(new String[] { "MultiPoint", "MultiLineString", "MultiPolygon" }));

    /**
     * Empty constructor.
     */
    public SubHandlerFactory() {
    }

    /**
     * Creates a new SubHandler, based on the appropriate OGC simple geometry
     * type.  Note that some types are aggregated into a generic 'multi' type.
     *
     * @param type Type of SubHandler to return.
     *
     * @return DOCUMENT ME!
     *
     */
    public SubHandler create(String type) {

        SubHandler returnValue = null;

        if (type.equals("Point")) {
            returnValue = new SubHandlerPoint();
        } else if (type.equals("LineString")) {
            returnValue = new SubHandlerLineString();
        } else if (type.equals("LinearRing")) {
            returnValue = new SubHandlerLinearRing();
        } else if (type.equals("Polygon")) {
            returnValue = new SubHandlerPolygon();
        } else if (type.equals("Box") || type.equals("Envelope")) {
            returnValue = new SubHandlerBox();
        } else if (BASE_GEOMETRY_TYPES.contains(type)) {
            returnValue = new SubHandlerMulti();
        } else {
            returnValue = null; // should be throwing an exception here!
        }

        return returnValue;
    }
}
