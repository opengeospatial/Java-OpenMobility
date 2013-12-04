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
package com.augtech.geoapi.geopackage;

import java.util.ArrayList;
import java.util.Map;


public class GpkgRecords extends ArrayList<Map<String, GpkgField>>{

	/** GpkgRecords is an extension of the standard Java ArrayList
	 * which stores none, one or more records from a GeoPackage table.<p>
	 * Each record (entry) within the list is a HashMap of {@link GpkgField}'s
	 * returned from the interaction operation, such as 'query'. This
	 * class provides convienience methods for quickly getting a single field 
	 * from a specific record.
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/** Get a records field value as an Integer
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public int getFieldInt(int record, String field) {
		if (this.size()==0 || this.size()<record) return -1;
		GpkgField f = this.get(record).get(field);
		if (f==null || f.getValue()==null) return -1;

		return Integer.valueOf( f.getValue().toString() );
	}
	/** Get a records field value as a Float
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public float getFieldFloat(int record, String field) {
		if (this.size()==0 || this.size()<record) return -1f;
		GpkgField f = this.get(record).get(field);
		if (f==null || f.getValue()==null) return -1f;
		
		return Float.valueOf( f.getValue().toString() );
	}
	/** Get a records field value as a Double
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public double getFieldDouble(int record, String field) {
		if (this.size()==0 || this.size()<record) return -1d;
		GpkgField f = this.get(record).get(field);
		if (f==null || f.getValue()==null) return -1d;
		
		return Double.valueOf( f.getValue().toString() );
	}
	/** Get a records field value as a String
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or an empty string if no record or field with the supplied name exists
	 */
	public String getField(int record, String field) {
		if (this.size()==0 || this.size()<record) return "";
		GpkgField f = this.get(record).get(field);
		if (f==null || f.getValue()==null) return "";
		
		return f.getValue().toString();
	}
	/** Get a records field value as an boolean
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or false if no record or field with the supplied name exists
	 */
	public boolean getFieldBool(int record, String field) {
		if (this.size()==0 || this.size()<record) return false;
		GpkgField f = this.get(record).get(field);
		if (f==null || f.getValue()==null) return false;
		
		return Integer.valueOf( f.getValue().toString() )==1 || Boolean.getBoolean( f.getValue().toString() )==true;
	}

}
