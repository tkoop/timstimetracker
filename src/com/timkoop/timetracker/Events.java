package com.timkoop.timetracker;

import java.util.ArrayList;

public class Events {
	
	private static ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	private static boolean log = false;

	public static void registerEventListener(EventListener listener) {
		listeners.add(listener);
	}
	
	
	/**
	 * Valid events: start, stop, close, tick, update, nameChanged
	 * @param eventName
	 * @param data
	 * @param exceptThisOne
	 */
	public static void fireEvent(EventType type, DataRow data, EventListener exceptThisOne) {
		for(EventListener listener : listeners) {
			if (listener != exceptThisOne) listener.fire(type, data);
			if (log) System.out.println("Firing " + type);
		}
	}
	
	public static void fireEvent(EventType type, DataRow data) {
		fireEvent(type, data, null);
	}
	
	public static void fireEvent(EventType type) {
		fireEvent(type, new DataRow(), null);
	}
}
