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
package com.augtech.geoapi.geopackage.views;
/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class STSpatialRefSys extends GpkgView {
	/** The internal DB View name */
	public static String VIEW_NAME = "st_spatial_ref_sys";
	/**
	 * 
	 */
	public STSpatialRefSys() {
		super(	VIEW_NAME, 
				"gpkg_spatial_ref_sys", 
				new String[]{
					"srs_name",
					"srs_id",
					"organization",
					"organization_coordsys_id",
					"definition",
					"description"
				}
		);

	}

}
//public static final String CREATE_VIEW_ST_SPATIAL_REF_SYS = "CREATE VIEW st_spatial_ref_sys AS SELECT "+
//		"srs_name, "+
//		"srs_id, "+
//		"organization, "+
//		"organization_coordsys_id, "+
//		"definition, "+
//		"description FROM gpkg_spatial_ref_sys;