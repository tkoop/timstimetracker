package com.timkoop.timetracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {
	private static DB instance = null;
	private Connection connection = null;
	private static boolean log = false;
	private final String DB_PATH = "data";

	public static void main(String[] args) {
		try {
			System.out.println("Tables = " + DB.getDB().query("show tables;"));
			System.out.println("Project Table = " + DB.getDB().query("show columns from project;"));
			System.out.println("Work Table = " + DB.getDB().query("show columns from work;"));
			System.out.println("Projects: = " + DB.getDB().query("select * from project;"));
			System.out.println("Work entries: = " + DB.getDB().query("select count(*) from work;"));
			DB.getDB().close();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private DB() {
	}
	
	public static DataRow[] getData(String sql, Object... values) {
		try {
			return getDB().query(sql, values).asArray();
		} catch (DBException e) {
			e.printStackTrace();
			return new DataRow[0];
		}
	}
	
	public static DataRow getFirstRow(String sql, Object... values) {
		DataRow[] data = getData(sql, values);
		if (data == null) return null;
		if (data.length == 0) return null;
		return data[0];
	}
	
	public static String getFirst(String sql, Object... values) {
		DataRow[] data = getData(sql, values);
		if (data == null) return null;
		if (data.length == 0) return null;
		return data[0].getFirstString();
	}

	public static Integer getFirstInt(String sql, Object... values) {
		String first = getFirst(sql, values);
		if (first == null) return null;
		return Integer.parseInt(first);
	}

	
	public static DB getDB() {
		if (instance == null) instance = new DB();
		return instance;
	}
		
	public static int perform(String sql, Object... values) {
		try {
			return getDB().performInstance(sql, values);
		} catch (DBException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private String valuesToString(Object[] values) {
		StringBuilder str = new StringBuilder();
		
		for(Object v : values) {
			if (str.length() > 0) str.append(", ");
			str.append(v.toString());
		}
		
		return str.toString();
	}
	
	public int performInstance(String sql, Object... values) throws DBException {
		connect();
		
		try {
			if (log) System.out.println("performing: " + sql + ", values: " + valuesToString(values));
			PreparedStatement statement = connection.prepareStatement(sql);
			
			for(int i=0; i<values.length; i++) {
				Object value = values[i];
				statement.setObject(i+1, value);
			}
			
			return statement.executeUpdate();
		} catch (SQLException e) {
			throw new DBException("Error in sql: " + sql, e);
		}
		
	}
	
	public Data query(String sql, Object... values) throws DBException {
		connect();
		
		try {			
			PreparedStatement statement = connection.prepareStatement(sql);
			
			if (log) System.out.println("DB query: " + sql + " " + valuesToString(values));
			
			for(int i=0; i<values.length; i++) {
				Object value = values[i];
				statement.setObject(i+1, value);
			}
			
			ResultSet results = statement.executeQuery();

			return new Data(results);
			
		} catch (SQLException e) {
			throw new DBException("Error with sql: " + sql, e);
		}

	}
	
	public void close() throws DBException {
		return;
		// H2 will close itself on system exit
		
		/*
		try {
			connection.close();
		} catch (SQLException e) {
			throw new DBException("Can't close database", e);
		}
		*/
	}
	
	private void createDB() throws DBException {
		DB.perform("create table project(project_id integer, name varchar(128), colour varchar(32), the_order integer);");
		DB.perform("create table work(project_id integer, start_time timestamp, end_time timestamp, the_date date, billed tinyint default 0, total integer);");
		
		DB.perform("insert into project (project_id, name, colour, the_order) values (1, 'Project 1', 'blue', 1)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (2, 'Project 2', 'red', 2)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (3, 'Project 3', 'green', 3)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (4, 'Project 4', 'magenta', 4)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (5, 'Project 5', 'darkred', 5)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (6, 'Project 6', 'cyan', 6)");
		DB.perform("insert into project (project_id, name, colour, the_order) values (7, 'Project 7', 'yellow', 7)");
	}
	
	public boolean isAlreadyOpen() {
		if (connection != null) return false;

		try {
			connect();
		} catch (DBException e) {
			return true;
		}
		
		return false;
	}
	
	private synchronized void connect() throws DBException {
		if (log) System.out.println("Calling connect.  connection is: " + connection);
		try {
			if (connection != null && !connection.isClosed()) {
				if (log) System.out.println("  returning");
				return;
			}
		} catch (SQLException e2) {
			throw new DBException("Error while trying to check of connection is open", e2);
		}
		
		try {
			Class.forName("org.h2.Driver").newInstance();	// Register the H2 JDBC Driver
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new DBException("Can't register com.mckoi.JDBCDriver", e);
		}

		String url = "jdbc:h2:"+this.DB_PATH+";ALIAS_COLUMN_NAME=TRUE";

		try {
			// Make a connection with the local database.
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			throw new DBException("Can't connect to database", e);
		}
			
		
		if (DB.getFirstRow("show tables;") == null) {
			createDB();
		}

	}

}
