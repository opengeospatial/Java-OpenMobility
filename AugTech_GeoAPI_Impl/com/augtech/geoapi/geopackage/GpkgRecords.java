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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GpkgRecords is an extension of the standard Java ArrayList
 * which stores none, one or more records from a GeoPackage table.<p>
 * Each record (entry) within the list is an ordered list of object values
 * returned from the database. This class provides indexes for fast access
 * to the record data.
 * 
 */
public class GpkgRecords extends ArrayList<List<Object>> {
	Map<String, Integer> fieldIndex = new HashMap<String, Integer>();
	List<GpkgField> fields = new ArrayList<GpkgField>();
	
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param fieldIdx
	 * @param fieldList
	 */
	public GpkgRecords(Map<String, Integer> fieldIdx, List<GpkgField> fieldList) {
		this.fieldIndex = fieldIdx;
		this.fields = fieldList;
	}
	/**
	 * 
	 * @param record
	 * @param fieldName
	 * @return
	 */
	private Object getValue(int record, String fieldName) {
		if (this.size()==0 || record > this.size()) return null;

		return this.get(record).get( getFieldIdx(fieldName) );
	}
	/** Get the index of a specific field. 
	 * 
	 * @param fieldName The field name
	 * @return The index or -1 if it doesn't exist
	 */
	public int getFieldIdx(String fieldName) {
		return fieldIndex.get(fieldName)==null ? -1 : fieldIndex.get(fieldName);
	}
	/** Get a records field value as an Integer
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public int getFieldInt(int record, String fieldName) {
		Object obj = getValue(record, fieldName);
		if (obj==null) return -1;

		return Integer.valueOf( String.valueOf(obj) );
	}
	/** Get a records blob field (as byte[])
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return A byte[] or null if no record or field with the supplied name exists
	 */
	public byte[] getFieldBlob(int record, String fieldName) {
		Object obj = getValue(record, fieldName);
		if (obj==null) return null;
		
		return (byte[]) obj;
	}
	/** Get a records field value as a Float
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public float getFieldFloat(int record, String fieldName) {
		Object obj = getValue(record, fieldName);
		if (obj==null) return -1f;
		
		return Float.valueOf( String.valueOf(obj) );
	}
	/** Get a records field value as a Double
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return The value or -1 if no record or field with the supplied name exists
	 */
	public double getFieldDouble(int record, String fieldName) {
		Object obj = getValue(record, fieldName);
		if (obj==null) return -1d;
		
		return Double.valueOf( String.valueOf(obj) );
	}
	
	/** Get a records field value as a String
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return The value or an empty string if no record or field with the supplied name exists
	 */
	public String getFieldString(int record, String fieldName) {
		Object obj = getValue(record, fieldName);
		if (obj==null) return "";
		
		return String.valueOf(obj);
	}
	/** Get a records field value as an boolean
	 * 
	 * @param record The record ID
	 * @param field The field name
	 * @return The value or false if no record or field with the supplied name exists
	 */
	public boolean getFieldBool(int record, String fieldName) {
		
		String b = getFieldString(record, fieldName);
		
		return b.equals("1") || Boolean.parseBoolean( b );
	}
	/** Get a GpkgField field
	 * 
	 * @param record The record ID
	 * @param fieldName The field name
	 * @return The GpkgField or <code>Null</code> if no record or field with the supplied name exists
	 */
	public GpkgField getField(int record, String fieldName) {
		if (this.size()==0 || record > this.size() ) return null;
		int idx = getFieldIdx(fieldName);
		
		GpkgField gf = fields.get( idx ).clone();
		if (gf==null) return null;
		gf.value = this.get( record ).get( idx );

		return gf;
	}
	/** Get all GpkgField's for the given record index
	 * 
	 * @param record The index of the record in this list
	 * @return All GpkgField's or a blank list if the record does not exist
	 */
	public List<GpkgField> getFields(int record) {
		List<GpkgField> allFields = new ArrayList<GpkgField>();
		if (this.size()==0 || record>this.size()) return allFields;
		
		for (GpkgField field : fields) {
			allFields.add( getField(record, field.getFieldName() ) );
		}
		
		return allFields;
	}
}
