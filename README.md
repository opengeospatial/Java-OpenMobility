JavaGeoPackage
==============

A Java GeoPackage client developed under the OGC OWS-10 test-bed.

The 'library' code enables the creation, insertion, query and update of records in the SQLite database either directly or via SimpleFeature's and SimpleFeatureType's interfaces the <a href="www.geoapi.org/">GeoAPI</a>. This client is suitable for (and was developed on) Android, but should work equally as well in desktop applications. Two interfaces to an underlying SQLite implementation are provided to hopefully make it SQLite implementation agnostic.

This project includes an Android application for testing and demonstrational purposes. The test layer utilises functions from our own implementation of the GeoAPI interfaces for loading and processing GML and tiles in to SimpleFeature's.(<a href="http://www.awila.co.uk/dev_doc/geoapi_doc/index.html">GeoAPI Impl. Javadoc</a>)<p>

The GeoPackage source cannot be used stand-alone; You will require the <a href="http://www.vividsolutions.com/jts/JTSHome.htm">Java Topology Suite</a> v1.8 and <a href="http://maven.geotoolkit.org/org/opengis/geoapi-pending/">GeoAPI Pending</a> version 3.0 or greater.<p>

Note that the Android project includes a re-compiled version of JTS as we found issues running the original .jar on Android - no other changes have been made. The geoapi-pending-3.1-M04.jar is also included within the /libs directory.
<p>
Note that this is still a work in progress and subject to quite a bit of change!

Some classes are directly copied from and/ or based upon <a href="http://geotools.org">GeoTools</a>. We neither take or imply any credit for their excellent work.