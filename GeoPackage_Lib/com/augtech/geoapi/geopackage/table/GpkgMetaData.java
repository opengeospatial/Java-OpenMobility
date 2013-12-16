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
package com.augtech.geoapi.geopackage.table;

import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgTable;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgMetaData extends GpkgTable {
	public static String TABLE_NAME = "gpkg_metadata";
	
	public static final String SCOPE_UNDEFINED  = "undefined";
	public static final String SCOPE_FIELDSESSION  = "fieldSession";
	public static final String SCOPE_COLLECTIONSESSION  = "collectionSession";
	public static final String SCOPE_SERIES  = "series";
	public static final String SCOPE_DATASET  = "dataset";
	public static final String SCOPE_FEATURETYPE  = "featureType";
	public static final String SCOPE_FEATURE  = "feature";
	public static final String SCOPE_ATTRIBUTETYPE  = "attributeType";
	public static final String SCOPE_ATTRIBUTE  = "attribute";
	public static final String SCOPE_TILE  = "tile";
	public static final String SCOPE_MODEL  = "model";
	public static final String SCOPE_CATALOG  = "catalog";
	public static final String SCOPE_SCHEMA  = "schema";
	public static final String SCOPE_TAXONOMY  = "taxonomy";
	public static final String SCOPE_SOFTWARE  = "software";
	public static final String SCOPE_SERVICE  = "service";
	public static final String SCOPE_COLLECTIONHARDWARE  = "collectionHardware";
	public static final String SCOPE_NONGEOGRAPHICDATASET  = "nonGeographicDataset";
	public static final String SCOPE_DIMENSIONGROUP  = "dimensionGroup";

	/**
	 * 
	 */
	public GpkgMetaData() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("id", "INTEGER", "CONSTRAINT m_pk PRIMARY KEY ASC AUTOINCREMENT NOT NULL UNIQUE"),
					new GpkgField("md_scope", "TEXT", "NOT NULL", "'dataset'"),
					new GpkgField("md_standard_URI", "TEXT", "NOT NULL", "'http://schemas.opengis.net/iso/19139/'"),
					new GpkgField("mime_type", "TEXT", "NOT NULL", "'text/xml'"),
					new GpkgField("metadata", "TEXT", "NOT NULL", "('')")
				},
				null
			);

	}
}
//public static final String CREATE_TABLE_GPKG_METADATA = "CREATE TABLE gpkg_metadata ("+
//		"id INTEGER CONSTRAINT m_pk PRIMARY KEY ASC AUTOINCREMENT NOT NULL UNIQUE,"+
//		"md_scope TEXT NOT NULL DEFAULT 'dataset', "+
//		"metadata_standard_URI TEXT NOT NULL DEFAULT 'http://schemas.opengis.net/iso/19139/',"+
//		"mime_type TEXT NOT NULL DEFAULT 'text/xml', "+
//		"metadata TEXT NOT NULL DEFAULT ('') )";