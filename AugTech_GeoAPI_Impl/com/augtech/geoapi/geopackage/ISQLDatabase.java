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

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ISQLDatabase {

	/** Get the file associated with the SQLite database
	 * 
	 * @return The File
	 */
	public File getDatabaseFile();
	/** Create a new database file on disk. This does not need to insert any
	 * records or alter the file header
	 * 
	 * @return The new ISQLDatabase
	 */
	public ISQLDatabase createDatabase();
	/** Get a connection to the ISQLDatabase
	 * 
	 * @param writeable If True, the database is opened in read/write mode, 
	 * otherwise it is in read-only mode
	 * 
	 * @return The ISQLDatabase with an open connection
	 */
	public ISQLDatabase getDatabase(boolean writeable);
    /** Query a specfic database table for a selection of columns.
     * 
     * @param table The table to query
     * @param columns The columns to select. Passing Null will request all columns.
     * @param where A filtering where clause or Null for all records.
     * @return A ICursor object
     */
	public ICursor doQuery(String table, String[] columns, String strWhere);
	/** Execute a raw SQL statement that returns a result
	 * 
	 * @param sql The SQL statement to execute
	 * @return A ICursor of the results
	 */
	public ICursor doRawQuery(String sql);
	/** Execute a raw SQL statement that does not return a result
	 * 
	 * @param sql The SQL statement to execute
	 */
	public void execSQL(String sql);
	/** Execute a set of SQL statements as a batch
	 * 
	 * @param statements An array of SQL statements to execute as a batch.
	 * @return True if successful, False if all statements could not be executed
	 */
	public boolean execSQLWithRollback(String[] statements);
	
	   /** Update a database table
     * 
     * @param table The table to update
     * @param values The column names and associated values to update
     * @param strWhere A filering where clause. If Null, the update will not happen
     * @return The number of rows affected 
     */
	public int doUpdate(String table, Map<String, Object> values, String strWhere);
	   /** Delete a record from a table
     * 
     * @param table The table to remove a record from
     * @param strWhere The where clause without 'where'. Set to NULL to clear all records. 
     * @return The number of rows affected if a where clause is passed in, 0 otherwise
     */
	public int doDelete(String table, String strWhere);
	/** Insert multiple records into a table as a batch commit<p>
	 * This method assumes that the value set for every record is the same.
	 * 
	 * @param table The table to insert to
	 * @param values A Collection of a map of the column names and associated values to insert
	 * 
	 * @return The total amount of rows inserted
	 */
	public long doInsert(String table, List<Map<String, Object>> values);
	/** Insert a record into a table
	 * 
	 * @param table The table to insert to
	 * @param values A map of the column names and associated values to insert
	 * 
	 * @return The row ID of the newly inserted row
	 */
	public long doInsert(String table, Map<String, Object> values);
	
	/** Does the underlying SQLite implementation have R*Tree indexing
	 * available? 
	 * 
	 * @return
	 * @see http://www.sqlite.org/rtree.html
	 */
	public boolean hasRTreeEnabled();
	/** Is the database connection currently open?
	 * 
	 * @return True if it is
	 */
	public boolean isOpen();
	/** Releases a reference to the object, closing the object if the last reference was released
	 * 
	 */
	public void close();
}
