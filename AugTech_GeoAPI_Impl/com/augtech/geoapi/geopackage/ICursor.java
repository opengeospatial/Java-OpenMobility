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

/** An interface to access cursor methods which are used by this
 * Java GeoPackage implementation. Additional methods will be available from
 * the underlying SQLite implementation but are not required by this GeoPackage
 * implementation.<p>
 * Implementors should implement this class to ensure GeoPackaging can work on 
 * different SQLite implementations (such as Android, SpatialLite etc).
 * 
 *
 */
public interface ICursor {

	/**Move the cursor to the first row. 
	 * This method should return false if the cursor is empty.
	 * 
	 * @return
	 */
	public boolean moveToFirst();
	/**Move the cursor to the next row. 
	 * This method should return false if the cursor is already past the last entry in the result set.
	 * 
	 * @return
	 */
	public boolean moveToNext();
	/** Move the cursor to the last row. 
	 * This method will return false if the cursor is empty.
	 * 
	 * @return
	 */
	public boolean moveToLast();
	/** Closes the Cursor, releasing all of its resources and making it completely invalid
	 * 
	 */
	public void close();
	/** Returns the value of the requested column as a String. 
	 * The result and whether this method throws an exception when the column value is null or the column type
	 * is not a string type is implementation-defined.
	 * columnIndex  the zero-based index of the target column. 
	 * 
	 * @param columnIndex the zero-based index of the target column. 
	 * @return the value of that column as a String. 
	 */
	public String getString(int columnIndex);
	/** Returns the value of the requested column as an int. 
	 * The result and whether this method throws an exception when the column value is null, 
	 * the column type is not an integral type, or the integer value is outside the 
	 * range [Integer.MIN_VALUE, Integer.MAX_VALUE] is implementation-defined.
	 * 
	 * @param columnIndex the zero-based index of the target column.
	 * @return the value of that column as an int.
	 */
	public int getInt(int columnIndex);

	/** Get the number of columns in this cursor
	 * 
	 * @return the numbers of columns in the cursor or -1
	 */
	public int getColumnCount();
	/** Returns the value of the requested column as a double. 
	 * The result and whether this method throws an exception when the column value
	 * is null, the column type is not a floating-point type, or the floating-point 
	 * value is not representable as a double value is implementation-defined.
	 * 
	 * @param columnIndex
	 * @return the value of that column as a double. 
	 */
	public double getDouble(int columnIndex);
	/**Returns the value of the requested column as a boolean. 
	 * If the field value is stored as an Integer then returns True if the
	 * value is 1
	 * 
	 * @param columnIndex
	 * @return True of the column is True or 1
	 */
	public boolean getBoolean(int columnIndex);
	/** Returns the value of the requested column as a float.
	 * The result and whether this method throws an exception when the 
	 * column value is null, the column type is not a floating-point type, 
	 * or the floating-point value is not representable as a float value is implementation-defined.
	 * 
	 * @param columnIndex
	 * @return
	 */
	public float getFloat(int columnIndex);
	/** Returns the value of the requested column as a byte array. 
	 * The result and whether this method throws an exception when the column 
	 * value is null or the column type is not a blob type is implementation-defined.
	 * 
	 * @param columnIndex
	 * @return he requested column as a byte array
	 */
	public byte[] getBlob(int columnIndex);
	/** Returns the zero-based index for the given column name, or -1 if the column doesn't exist. 
	 * 
	 * @param columnName
	 * @return
	 */
	public int getColumnIndex(String columnName);
	/** Returns the column name at the given zero-based column index.
	 * 
	 * @param columnIndex
	 * @return
	 */
	public String getColumnName(int columnIndex);
	/** Returns a string array holding the names of all of the columns in the result set in 
	 * the order in which they were listed in the result.
	 * 
	 * @return the names of the columns returned in this query. 
	 */
	public String[] getColumnNames();

}
