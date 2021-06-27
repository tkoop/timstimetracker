package com.timkoop.timetracker;

import java.util.Properties;

public class DataRow {
	private Properties data = new Properties();
	
	public DataRow(Object... nameValuePairs) {
		for(int i=0; i<nameValuePairs.length/2; i++) {
			String name = nameValuePairs[i*2].toString();
			String value = nameValuePairs[i*2+1].toString();
			setData(name.toUpperCase(), value);
		}
	}
	
	public void setData(String column, String value) {
		data.setProperty(column.toUpperCase(), value);
	}
	
	public String getString(String column) {
		return data.getProperty(column.toUpperCase());
	}
	
	public String getFirstString() {
		for (Object key : data.keySet()) {
			return getString(key.toString().toUpperCase());
		}
		return null;
	}
	
	public int getInt(String column) {
		return Integer.parseInt(getString(column.toUpperCase()));
	}
	
	public int getInt(String column, int defaultValue) {
		if (hasColumn(column)) return Integer.parseInt(getString(column.toUpperCase()));
		return defaultValue;
	}
	
	public float getFloat(String column) {
		return Float.parseFloat(getString(column.toUpperCase()));
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for(Object key : data.keySet()) {
			if (str.length() > 0) str.append(", ");
			str.append(key).append("=").append(getString(key.toString()));
		}
		
		return str.toString();
	}
	
	public boolean hasColumn(String column) {
		return data.containsKey(column.toUpperCase());
	}
}
