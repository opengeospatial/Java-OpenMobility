JavaGeoPackage
==============

A Java GeoPackage client developed under the OGC OWS-10 test-bed.

The 'library' code enables the creation, insertion, query and update of records in the SQLite database either directly or via SimpleFeature's and SimpleFeatureType's interfaces the GeoAPI. This client is suitable for (and was developed on) Android, but should work equally as well in desktop applications. Two interfaces to an underlying SQLite implementation are provided to hopefully make it SQLite implementation agnostic.

This project includes an Android application for testing and demonstrational purposes. The test layer utilises functions from our own implementation of the GeoAPI interfaces for loading and processing GML in to SimpleFeature's.

The GeoPackage source cannot be used stand-alone; You will require the Java Topology Suite 1.8 and GeoAPI (pending) version 3.0 or greater.
Note that the Android project includes a re-compiled version of JTS as we found issues running the original .jar on Android - no other changes have been made. The geoapi-pending-3.1-M04.jar is also included within the /libs directory.

JTS:				http://www.vividsolutions.com/jts/JTSHome.htm
GeoAPI Pending:			http://maven.geotoolkit.org/org/opengis/geoapi-pending/
AT GeoAPI Impl. Javadoc:	http://www.awila.co.uk/dev_doc/geoapi_doc/index.html

Note that this is still a work in progress!
