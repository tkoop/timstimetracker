package com.timkoop.timetracker;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class Data {
	private ArrayList<DataRow> data = new ArrayList<DataRow>();
	
	public Data(ResultSet results) throws DBException {
		try {
			ResultSetMetaData columns = results.getMetaData();
			
			while(results.next()) {
				DataRow row = new DataRow();
				
				for(int c=0; c<columns.getColumnCount(); c++) {
					String column = columns.getColumnLabel(c+1);
					String value = results.getString(c+1);
					
					if (value != null) row.setData(column, value);
				}
				
				data.add(row);
			}
		} catch (SQLException e) {
			throw new DBException("Can't convert ResultSet to Data", e);
		}
		
	}
	
	public int length() {
		return data.size();
	}
	
	public DataRow[] asArray() {
		return data.toArray(new DataRow[0]);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for(DataRow row : asArray()) {
			str.append(row).append("\n");
		}
		
		return str.toString();
	}
}
