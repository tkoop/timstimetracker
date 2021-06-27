package com.timkoop.timetracker;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class SystemTrayManager {
	
	private TrayIcon trayIcon = null;
	private SystemTray tray = null;

	public static void main(String[] args) {
		SystemTrayManager m = new SystemTrayManager();
		m.run();
	}
	
	private JMenuItem createMenuItem(int id, String colour, String name) {
		URL imageURL = ClassLoader.getSystemResource("images/" + colour + ".png");
		String runningId = DB.getFirst("select project_id from work where end_time is null;");
		
		JMenuItem item = new JMenuItem("Test", new ImageIcon(imageURL, colour));
		item.setText(name);
		
		if ((""+id).equals(runningId)) {
			item.setFont(item.getFont().deriveFont(Font.BOLD));
		}
		
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String currentId = DB.getFirst("select project_id from work where end_time is null;");
				if ((""+id).equals(currentId)) {
					// user clicked a project that is running.  Let's just stop it.
					Events.fireEvent(EventType.STOP, new DataRow("id", ""+id));
				} else {
					Events.fireEvent(EventType.START, new DataRow("id", ""+id));
				}
			}
		});
		return item;
	}
	
	private JPopupMenu makePopupMenu() {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem stopItem = new JMenuItem("Nothing");
		stopItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Events.fireEvent(EventType.STOP);
			}
		});
		menu.add(stopItem);
		
		menu.addSeparator();
		
		for(DataRow project : DB.getData("select pr.*, wo.* from ( "
				+ "select colour, name, project_id, the_order from project "
				+ ") pr left outer join ( "
				+ "select project_id, "
				+ "coalesce(sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))), 0) as total "
				+ "from work where the_date = current_date group by project_id "
				+ ") wo on pr.project_id = wo.project_id order by the_order;")) {
			int id = project.getInt("project_id");
			String colour = project.getString("colour");
			String name = project.getString("name") + " (" + Utils.secondsToFormat(project.getInt("total", 0)) + ")";

			menu.add(createMenuItem(id, colour, name)); 
		}
		
		menu.addSeparator();
		
		JMenuItem showItem = new JMenuItem("Show Window");
		showItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Events.fireEvent(EventType.SHOW);
			}
		});
		menu.add(showItem);

		
		JMenuItem closeItem = new JMenuItem("Exit");
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Events.fireEvent(EventType.CLOSE);
			}
		});
		menu.add(closeItem);
				
		return menu;
	}
	
	
	private void updateIcon() {
		DataRow project = DB.getFirstRow("select work.project_id, project.colour, project.name from work inner join project on work.project_id = project.project_id where work.end_time is null;");
		
		if (project == null) {
			trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("images/white.png")));
			trayIcon.setToolTip("Tim's Time Tracker");
		} else {
			String colour = project.getString("colour");
			Integer total = DB.getFirstInt("select sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))) as total "
					+ "from work where the_date = current_date and project_id = ?", project.getString("project_id"));
			if (total == null) total = 0;
			trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("images/"+colour+".png")));
			trayIcon.setToolTip(project.getString("name") + " (" + Utils.secondsToFormat(total) + ")");
		}
	}
	
	
	private void setup() {
		String colour = "white";
		String runningId = DB.getFirst("select project_id from work where end_time is null;");
		if (runningId != null) {
			colour = DB.getFirst("select colour from project where project_id = " + runningId);
		}
		Image image = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("images/"+colour+".png"));
		

		trayIcon = new TrayIcon(image, "Tim's Time Tracker", null);

		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
//				if (e.isPopupTrigger()) {
					JPopupMenu popup = makePopupMenu();
					popup.setLocation(e.getX(), e.getY());
					popup.setInvoker(popup);
					popup.setVisible(true);
					
					popup.addFocusListener(new FocusListener() {
						
						@Override
						public void focusLost(FocusEvent e) {
							System.out.println("focus lost");
						}
						
						@Override
						public void focusGained(FocusEvent e) {
							System.out.println("focus gained");
						}
					});
					
					popup.setFocusable(true);
					popup.requestFocus();

//				}
			}
		});
	
		tray = SystemTray.getSystemTray();
		
		try {
			tray.add(trayIcon);
			updateIcon();
		} catch (AWTException e) {
			System.err.println(e);
		}
	}
	

	
	public void run() {
		if (!SystemTray.isSupported()) {
			return;
		}

		setup();

		Events.registerEventListener(new EventListener() {
			
			@Override
			public void fire(EventType eventType, DataRow data) {
				if (eventType == EventType.CLOSE) {
					tray.remove(trayIcon);
				}
				if (eventType == EventType.NAME_CHANGED || eventType == EventType.STOP || eventType == EventType.UPDATE || eventType == EventType.START || eventType == EventType.TICK) {
					updateIcon();
				}
			}
		});	
		

	}

}
