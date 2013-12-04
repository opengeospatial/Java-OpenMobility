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

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.ICursor;

/** An abstract class for creating and interacting with one of the standard
 * Views within the GeoPackage
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public abstract class GpkgView {
	String viewName = "";
	String sourceName = "";
	String[] columns = null;
	String whereClause = "";
	private StringBuffer sb = new StringBuffer();
	
	public GpkgView(String viewName, String sourceName, String[] columns) {
		this(viewName, sourceName, columns, null);
	}
	public GpkgView(String viewName, String sourceName, String[] columns, String where) {
		this.viewName = viewName;
		this.columns = columns;
		this.sourceName = sourceName;
		this.whereClause = where==null ? "" : where;
	}
	/** Create this view in the GeoPackage
	 * 
	 * @return True if created. False if already exists.
	 */
	public boolean create(GeoPackage geoPackage) {
		if (isViewInDB(geoPackage)) return false;
		sb.setLength(0);
		
		sb.append("CREATE VIEW ").append(viewName).append(" AS SELECT ");
		for (String s : columns) sb.append(s).append(",");
		sb.setLength(sb.length()-1);
		sb.append(" FROM ").append(sourceName);
		if (!whereClause.equals("")) sb.append(" WHERE ").append(whereClause);
		
		geoPackage.getDatabase().execSQL( sb.toString() );
		
		return true;
	}
	/** Check that this View exists in the GeoPackage database.
	 * 
	 * @param geoPackage The GeoPackage to look in
	 * @return True if the table is in SQLITE_MASTER
	 */
	public boolean isViewInDB(GeoPackage geoPackage) {
		// Does the table already exist?
		boolean tExists = false;
		ICursor c = geoPackage.getDatabase().doQuery("SQLITE_MASTER", new String[]{"tbl_name"},"tbl_name='"+viewName+"'");
		if( c.moveToFirst() ) tExists = c.getString(0).equals(viewName);
		c.close();
		return tExists;
	}

}
