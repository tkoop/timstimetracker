package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.timkoop.timetracker.Tabbable;

public class BillingTab extends JPanel implements Tabbable {
	private static final long serialVersionUID = -7822366747749215750L;

	public BillingTab() {
		this.setLayout(new BorderLayout(0, 0));
		
		JTextArea tarResult = new JTextArea();
		
		JScrollPane scrollPane_2 = new JScrollPane(tarResult);
		this.add(scrollPane_2, BorderLayout.CENTER);

	}

	@Override
	public JPanel getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public String getTabText() {
		return "Billing";
	}

}
