package com.timkoop.timetracker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	public static final String[] COLOURS = new String[] {"blue", "cyan", "green", "grey", "magenta", 
		"red", "yellow", "darkblue", "darkcyan", "darkmagenta", "darkred", "lightblue", 
		"lightgreen", "lightmagenta", "lightred"};
	public static final String[] COLOUR_NAMES = new String[] {"Blue", "Cyan", "Green", "Grey", "Magenta", 
		"Red", "Yellow", "Dark Blue", "Dark Cyan", "Dark Magenta", "Dark Red", "Light Blue", 
		"Light Green", "Light Magenta", "Light Red"};
	
	public static String secondsToFormat(int seconds) {
		int sec = seconds % 60;
		int min = seconds / 60 % 60;
		int hou = seconds / 60 / 60;
		
		return hou + ":" + (min<10 ? "0":"") + min + ":" + (sec<10 ? "0":"") + sec;		
	}
	
	
	public static String formatDate(String date) {
		DateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat formatOut = new SimpleDateFormat("E, MMM d, yyyy");
		
		Date d;
		try {
			d = formatIn.parse(date);
		} catch (ParseException e) {
			return "Can't parse " + date;
		}
		
		return formatOut.format(d);
	}
	
	
	public static void reorderProjects() {
		DataRow[] projects = DB.getData("select project_id, the_order from project order by the_order");
		
		for(int i=0; i<projects.length; i++) {
			DB.perform("update project set the_order = ? where project_id = ?", i*10, projects[i].getString("project_id"));
		}
	}
	
}
