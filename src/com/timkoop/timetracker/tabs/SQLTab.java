package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.DBException;
import com.timkoop.timetracker.Data;
import com.timkoop.timetracker.DataRow;
import com.timkoop.timetracker.Tabbable;

import java.awt.FlowLayout;
import javax.swing.ImageIcon;

public class SQLTab extends JPanel implements Tabbable {
	public SQLTab() {
		this.setLayout(new BorderLayout(0, 0));
		
		JTextArea tarResult = new JTextArea();
		
		JScrollPane scrollPane_2 = new JScrollPane(tarResult);
		this.add(scrollPane_2, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(6, 6, 6, 6));
		this.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(6, 0));
		
		JTextArea tarSQL = new JTextArea();
		panel.add(tarSQL);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(6);
		panel.add(panel_1, BorderLayout.EAST);
		
		JButton btnQuery = new JButton("Query");
		btnQuery.setIcon(new ImageIcon(SQLTab.class.getResource("/icons/play.png")));
		panel_1.add(btnQuery);
		
		JButton btnPerform = new JButton("Perform");
		btnPerform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performSQL(tarSQL.getText(), tarResult);
			}
		});
		panel_1.add(btnPerform);
		btnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				querySQL(tarSQL.getText(), tarResult);
			}
		});
	
	}
	
	
	private void querySQL(String sql, JTextArea tar) {
		try {
			Data data = DB.getDB().query(sql);
			StringBuilder str = new StringBuilder();
			
			for(DataRow d : data.asArray()) {
				str.append(d).append("\n");
			}
			
			tar.setText(str.toString());
		} catch (DBException e) {
			tar.setText("Error: " + e.getMessage() + "\nCaused by: " + e.getCause().getMessage());
		}
	}
	

	private void performSQL(String sql, JTextArea tar) {
		try {
			int result = DB.getDB().performInstance(sql);
			StringBuilder str = new StringBuilder();
			
			str.append("Result: " + result);
			
			tar.setText(str.toString());
		} catch (DBException e) {
			tar.setText("Error: " + e.getMessage() + "\nCaused by: " + e.getCause().getMessage());
		}
	}
	

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public String getTabText() {
		return "SQL";
	}

}
