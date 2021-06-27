package com.timkoop.timetracker;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	private MainWindow window = null; 
	private int todaysDate = (new GregorianCalendar()).get(GregorianCalendar.DATE); 

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
	
	private void setUpWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new MainWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	private void setUpEvents() {
		Timer timer = new Timer();

		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				String projectId = DB.getFirst("select project_id from work where end_time is null;");
				if (projectId != null) {
					Events.fireEvent(EventType.TICK, new DataRow("projectId", projectId));
				}
				
				Calendar now = new GregorianCalendar();
				if (now.get(GregorianCalendar.DATE) != todaysDate) {
					todaysDate = now.get(GregorianCalendar.DATE);
					Events.fireEvent(EventType.RESET);
				}
			}
		}, 1000, 1000);

		
		Events.registerEventListener(new EventListener() {
			
			@Override
			public void fire(EventType eventType, DataRow data) {
				if (eventType == EventType.CLOSE) {
					timer.cancel();
					try {
						DB.getDB().close();
					} catch (DBException e1) {
						System.err.println("Can't even close the DB");
						e1.printStackTrace();
					}
					System.exit(0);
				}
				
				// this nameChanged listener must run before other ones, since other ones look at the database
				if (eventType == EventType.NAME_CHANGED) {
					DB.perform("update project set name = ? where project_id = ?", data.getString("name"), data.getInt("id"));
				}
				
				if (eventType == EventType.START) {
					// first stop the running one
					DB.perform("update work set end_time = current_timestamp where end_time is null");
					DB.perform("insert into work (project_id, start_time, the_date) values (?, current_timestamp, current_date);", data.getString("id"));
				}

				if (eventType == EventType.STOP) {
					DB.perform("update work set end_time = current_timestamp where end_time is null");
					DB.perform("update work set total = datediff('SECOND', start_time, end_time) where total is null;");
				}
			}
		});
		
	}
	
	private void setUpSystemTray() {
		SystemTrayManager tray = new SystemTrayManager();
		tray.run();
	}
	
	private void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (DB.getDB().isAlreadyOpen()) {
			JOptionPane.showMessageDialog(null, "It looks like Tim's Time Tracker is already open.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		setUpEvents();
		setUpWindow();
		setUpSystemTray();
	}

}
