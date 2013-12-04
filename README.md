JavaGeoPackage
==============

A Java GeoPackage client developed under the OGC OWS-10 test-bed.

The client code enables the creation, insertion, query and update of records in the SQLite database either directly or via SimpleFeature's and SimpleFeatureType's from the GeoAPI. This client is suitable for (and was developed on) Android devices as well as desktop Java applications.

This project includes an Android application for testing and demonstrational purposes.

The source cannot be used stand-alone; You will require the Java Topology Suite 1.8 and GeoAPI (core and pending) version 3.0 or greater.
This project includes a re-compiled version of JTS as we found issues running the downloadable .jar on Android - no other changes have been made. The geoapi-pending-3.1-M04.jar is also included.

Note that this is still work in progress!
