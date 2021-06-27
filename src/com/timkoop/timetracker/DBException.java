package com.timkoop.timetracker;

public class DBException extends Exception {

	public DBException(Throwable cause) {
		super(cause);
	}
	
	public DBException(String message, Throwable cause) {
		super(message, cause);
	}
}
