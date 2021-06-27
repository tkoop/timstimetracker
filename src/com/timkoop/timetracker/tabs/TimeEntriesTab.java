package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.DataRow;
import com.timkoop.timetracker.Tabbable;

public class TimeEntriesTab extends JPanel implements Tabbable {
	
	public TimeEntriesTab() {
		JTextArea tarAll = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(tarAll);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				DataRow[] data = DB.getData("select * from work w inner join project p on w.project_id = p.project_id order by start_time desc;");
				tarAll.setText("");
				StringBuilder str = new StringBuilder();
				for(DataRow row : data) {
					str.append(row.getString("name")).append(" (").append(row.getString("project_id")).append(") ").append(row.getString("start_time")).append(" to ").append(row.getString("end_time")).append("\n");
				}
				tarAll.setText(str.toString());
				tarAll.setSelectionStart(0);
				tarAll.setSelectionEnd(0);
			}
		});
		this.setLayout(new BorderLayout(0, 0));
		this.add(scrollPane, BorderLayout.CENTER);
	
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 4677544953921523125L;

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public String getTabText() {
		return "Time Entries";
	}
	
	

}
