package com.augtech.geoapi.geopackage;

import java.sql.ResultSet;
import java.sql.SQLException;

/** An implementation of the the ICursor for use with the {@link JSqlLiteDatabase}
 * implementation.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class JSqlLiteCursor implements com.augtech.geoapi.geopackage.ICursor {
	ResultSet results = null;
	private int colOffset = 0;
	
	public JSqlLiteCursor(ResultSet rs) {
		results = rs;
		colOffset = JSqlLiteDatabase.ONE_BASED ? 1 : 0;
	}
	public JSqlLiteCursor() {
		colOffset = JSqlLiteDatabase.ONE_BASED ? 1 : 0;
	}
	
	@Override
	public boolean moveToFirst() {
		if (results==null) return false;
		try {
//			if (results.isClosed()) {
//				return false;
			if (results.isBeforeFirst()) {
				return results.next();
			} else if (results.isFirst()) {
				return true;
			} else {
				// move here to first?
			}
		} catch (SQLException e) {
			return false;
		}
		
		return false;
	}

	@Override
	public boolean moveToNext() {
		if (results==null) return false;
		try {
			return results.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean moveToLast() {
		if (results==null) return false;
		try {
			return results.last();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void close() {
		if (results==null) return;
		try {
			results.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getString(int columnIndex) {
		if (results==null) return null;
		try {
			return results.getString(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new String();
	}

	@Override
	public int getInt(int columnIndex) {
		if (results==null) return 0;
		try {
			return results.getInt(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getColumnCount() {
		if (results==null) return -1;
		try {
			return results.getMetaData().getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public double getDouble(int columnIndex) {
		if (results==null) return Double.NaN;
		try {
			return results.getDouble(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Double.NaN;
	}

	@Override
	public boolean getBoolean(int columnIndex) {
		if (results==null) return false;
		try {
			return results.getBoolean(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public float getFloat(int columnIndex) {
		if (results==null) return 0;
		try {
			results.getFloat(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		if (results==null) return null;
		try {
			/* getBlob not implemented by driver
			Blob b = results.getBlob(columnIndex + colOffset);
			return b.getBytes(0, (int) b.length());
			*/
			
			return results.getBytes(columnIndex + colOffset);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getColumnIndex(String columnName) {
		if (results==null) return -1;
		try {
			return results.findColumn(columnName) - colOffset;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (results==null) return null;
		try {
			results.getMetaData().getColumnName(columnIndex + colOffset);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String[] getColumnNames() {
		if (results==null) return new String[]{};
		try {
			String[] names = new String[results.getMetaData().getColumnCount()];
			for (int i=0; i<results.getMetaData().getColumnCount(); i++) 
				names[i] = results.getMetaData().getColumnName( i + colOffset );
			return names;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
