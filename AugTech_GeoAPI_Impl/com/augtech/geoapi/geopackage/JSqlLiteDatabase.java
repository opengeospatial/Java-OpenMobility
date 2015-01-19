package com.augtech.geoapi.geopackage;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.augtech.geoapi.geopackage.ICursor;
import com.augtech.geoapi.geopackage.ISQLDatabase;

/** An implementation of ISQLDatabase for the org.sqlite.JDBC driver.
 * The sqlite-jdbc-3.7.2 library must be added to the end project which can be 
 * downloaded from <a href=https://bitbucket.org/xerial/sqlite-jdbc">BitBucket</a>
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class JSqlLiteDatabase implements com.augtech.geoapi.geopackage.ISQLDatabase {
	File dbFile = null;
	Connection connection = null;
	static final boolean ONE_BASED = true;
	/**
	 * 
	 * @param dbFile
	 * @throws ClassNotFoundException
	 */
	public JSqlLiteDatabase(File dbFile) {
		
		this.dbFile = dbFile;
	    
	    try {
	    	// load the sqlite-JDBC driver using the current class loader
			Class.forName("org.sqlite.JDBC");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to load SQLite JDBC driver");
		}
	    
	}

	@Override
	public File getDatabaseFile() {
		return this.dbFile;

	}

	@Override
	public ISQLDatabase createDatabase() {
		return getDatabase(true);
	}

	@Override
	public ISQLDatabase getDatabase(boolean writeable) {
		// create a database connection
		try {
			if (connection==null || connection.isClosed() ) {
				connection = DriverManager.getConnection("jdbc:sqlite:"+dbFile.toString());
			}
			if (connection.isReadOnly() && writeable) connection.setReadOnly(false);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public ICursor doQuery(String table, String[] columns, String strWhere) {
		getDatabase(true);

		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			// Build the statement from supplied info
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT ");
			
			if (columns==null) {
				sql.append("*");
			} else {
				for (String s : columns) sql.append("[").append(s).append("],");
				sql.deleteCharAt(sql.length()-1);
			}
			
			sql.append(" FROM ").append(table).append(" ");

			addWhereClause(sql, strWhere);
			
			return new JSqlLiteCursor( statement.executeQuery( sql.toString() ) );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public ICursor doRawQuery(String sql) {
		getDatabase(true);
		try {
			
			Statement statement = connection.createStatement();
			return new JSqlLiteCursor( statement.executeQuery( sql ) );
			
		} catch (SQLException e) {
			/* If no results were returned then "query does not return ResultSet"
			 * is thrown, therefore just return a new blank cursor */
		}
		
		return new JSqlLiteCursor( );
	}

	@Override
	public void execSQL(String sql) {
		getDatabase(true);
		
		try {

			Statement statement = connection.createStatement();
			statement.execute(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean execSQLWithRollback(String[] statements) {
		getDatabase(true);
		
		try {
			
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			statement.clearBatch();
			for (String sql : statements) statement.addBatch(sql);
			statement.executeBatch();
			connection.setAutoCommit(true);
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
				return false;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		return false;
	}
	/** Add (or not) the supplied where clause to the StringBuffer
	 * if required.
	 * 
	 * @param sb
	 * @param strWhere
	 */
	private void addWhereClause(StringBuffer sb, String strWhere) {
		if (strWhere!=null && !strWhere.equals("")) {
			if (strWhere.toLowerCase().startsWith("where ")) strWhere = strWhere.replace("where ", "");
			sb.append("WHERE ").append(strWhere);
		}
	}
	/**
	 * 
	 * @param prep
	 * @param objs
	 * @throws NumberFormatException
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void encodeValues(PreparedStatement prep, Collection<Object> objs) 
			throws NumberFormatException, SQLException, IOException {

		int c = ONE_BASED ? 1 : 0;
		String name = "";
		for (Object o : objs) {
			name = o==null ? "string" : o.getClass().getSimpleName().toLowerCase();

			if (name.equals("integer") || name.equals("int")) {
				prep.setInt(c, Integer.valueOf(String.valueOf(o)));
			} else if (name.equals("string")) {
				prep.setString(c, String.valueOf(o));
			} else if (name.equals("boolean") || name.equals("byte")) {
				prep.setBoolean(c, Boolean.valueOf(String.valueOf(o)));
			} else if (name.equals("double") || name.equals("float")) {
				prep.setDouble(c, Double.valueOf(String.valueOf(o)));
			} else if (name.equals("long")) {
				prep.setLong(c, Long.valueOf(String.valueOf(o)));
			} else if (name.equals("geometry") || name.equals("byte[]")) {
				 prep.setBytes(c, (byte[])o );
			}
			
			c++;
		}
		
	}
	@Override
	public int doUpdate(String table, Map<String, Object> values, String strWhere) {
		getDatabase(true);
		int result = -1;
		
		try {
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ").append(table).append(" SET ");
			for (Map.Entry<String, Object> e : values.entrySet()) {
				sql.append("[").append(e.getKey()).append("]=?,");
			}
			// Trim last comma
			sql.deleteCharAt(sql.length()-1);
			
			addWhereClause(sql, strWhere);
			
			PreparedStatement preparedStatement = connection.prepareStatement( sql.toString() );
			
			try {
				encodeValues(preparedStatement, values.values() );
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			result = preparedStatement.executeUpdate();
			preparedStatement.clearParameters();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public int doDelete(String table, String strWhere) {
		getDatabase(true);

		try {
			
			Statement statement = connection.createStatement();
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE * FROM ").append(table).append(" ");

			addWhereClause(sql, strWhere);
			
			return statement.executeUpdate( sql.toString() );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private String buildInsertStmt(String table, Set<String> fields) {
		StringBuffer tmpSql = new StringBuffer();
		
		tmpSql.append("INSERT INTO ").append(table).append(" (");
		
		StringBuffer vs = new StringBuffer();
		vs.append("(");
		
		for (String k : fields) {
			tmpSql.append("[").append(k).append("],");
			vs.append("?,");
		}
		// Trim last comma
		tmpSql.deleteCharAt(tmpSql.length()-1).append(")");
		vs.deleteCharAt(vs.length()-1).append(")");
		
		tmpSql.append(" VALUES ").append(vs);
		
		return tmpSql.toString();
	}
	@Override
	public long doInsert(String table, List<Map<String, Object>> values) {
		getDatabase(true);
		
		if (values==null || values.size()<1) return 0;
		
		Set<String> fields = values.get(0).keySet();
		
		String sql = buildInsertStmt(table, fields);
		int res = -1;
		try {
			
			connection.setAutoCommit(false);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.clearBatch();
			
			// For each set of fields in the list of records..
			for (Map<String, Object> map : values) {
				try {
					
					encodeValues(preparedStatement, map.values() );
				
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				preparedStatement.addBatch();
			}
			res = preparedStatement.executeBatch().length;
			connection.setAutoCommit(true);
			preparedStatement.clearBatch();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	@Override
	public long doInsert(String table, Map<String, Object> values) {
		getDatabase(true);

		try {
			
			//Build the statement
			String sql = buildInsertStmt(table, values.keySet());
			
			PreparedStatement preparedStatement = connection.prepareStatement( sql );
			
			try {
				encodeValues(preparedStatement, values.values() );
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			return preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	@Override
	public boolean hasRTreeEnabled() {
		return false;
	}

	@Override
	public boolean isOpen() {
		try {
			return !connection.isClosed();
			//return connection.isValid(100);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


}
