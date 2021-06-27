package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.DataRow;
import com.timkoop.timetracker.EventListener;
import com.timkoop.timetracker.EventType;
import com.timkoop.timetracker.Events;
import com.timkoop.timetracker.Tabbable;
import com.timkoop.timetracker.Utils;

public class ReportsTab extends JPanel implements Tabbable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -499268150068529009L;
	private JTextArea tarReports;
	
	public ReportsTab() {
		tarReports = new JTextArea();
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				runReport();
			}
		});
		this.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_1 = new JScrollPane(tarReports);
		this.add(scrollPane_1, BorderLayout.CENTER);
		
		Events.registerEventListener(new EventListener() {
			
			@Override
			public void fire(EventType eventType, DataRow data) {
				if (eventType == EventType.TICK || eventType == EventType.START || eventType == EventType.STOP || eventType == EventType.UPDATE) {
					if (ReportsTab.this.isVisible()) {
						runReport();
					}
				}
			}
		});
	}
	

	private String report(String sql) {
		DataRow[] data = DB.getData(sql);
		int total = 0;

		StringBuilder str = new StringBuilder();
		for(DataRow row : data) {
			if (row.hasColumn("the_date")) str.append(Utils.formatDate(row.getString("the_date")) + " ");
			str.append(row.getString("name") + " " + Utils.secondsToFormat(row.getInt("total")) + "\n");
			total += row.getInt("total");
		}
		
		str.append("Total: " + Utils.secondsToFormat(total) + "\n");
		
		return str.toString();
	}
	
	
	private DataRow[] runReport() {
		String today = "select w.the_date, p.name, "
				+ "sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))) as total, "
				+ "p.project_id, p.the_order from project p "
				+ "inner join work w "
				+ "on p.project_id = w.project_id "
				+ "where w.the_date = current_date "
				+ "group by p.project_id, p.name, p.the_order, w.the_date "
				+ "order by w.the_date desc, p.the_order";

		String unbilled = "select w.the_date, p.name, "
				+ "sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))) as total, "
				+ "p.project_id, p.the_order from project p "
				+ "inner join work w "
				+ "on p.project_id = w.project_id "
				+ "where w.billed = 0 "
				+ "group by p.project_id, p.name, p.the_order, w.the_date "
				+ "order by w.the_date desc, p.the_order";


		String totals = "select p.name, "
				+ "sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))) as total, "
				+ "p.project_id, p.the_order from project p "
				+ "inner join work w "
				+ "on p.project_id = w.project_id "
				+ "where w.billed = 0 "
				+ "group by p.project_id, p.name, p.the_order "
				+ "order by p.the_order";

		String all = "select w.the_date, p.name, "
				+ "sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))) as total, "
				+ "p.project_id, p.the_order from project p "
				+ "inner join work w "
				+ "on p.project_id = w.project_id "
				+ "group by p.project_id, p.name, p.the_order, w.the_date "
				+ "order by w.the_date desc, p.the_order";

		
		StringBuilder str = new StringBuilder();

		str.append("From Today\n");
		str.append(report(today));

		/*
		str.append("\nUnbilled Hours\n");
		str.append(report(unbilled));
		*/

		str.append("\nProject Totals\n");
		str.append(report(totals));

		str.append("\nAll Days\n");
		str.append(report(all));

		
		tarReports.setText(str.toString());
		tarReports.setSelectionStart(0);
		tarReports.setSelectionEnd(0);

		
		return null;
	}
	


	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public String getTabText() {
		return "Reports";
	}

}
