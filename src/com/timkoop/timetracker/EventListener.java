package com.timkoop.timetracker;


public interface EventListener {
	
	public void fire(EventType eventType, DataRow data);
	
}
