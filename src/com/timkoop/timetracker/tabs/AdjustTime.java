package com.timkoop.timetracker.tabs;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.SpinnerNumberModel;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.EventType;
import com.timkoop.timetracker.Events;

public class AdjustTime extends JPanel {
	private int projectId = -1;
	private JDialog dialog = null;
	
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	
	public void setJDialog(JDialog dialog) {
		this.dialog = dialog;
	}
	
	public AdjustTime() {
		setBorder(new EmptyBorder(6, 6, 6, 6));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
		setLayout(gridBagLayout);
		
		JLabel lblAddMinutes = new JLabel("Add minutes:");
		GridBagConstraints gbc_lblAddMinutes = new GridBagConstraints();
		gbc_lblAddMinutes.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddMinutes.fill = GridBagConstraints.BOTH;
		gbc_lblAddMinutes.gridx = 0;
		gbc_lblAddMinutes.gridy = 0;
		add(lblAddMinutes, gbc_lblAddMinutes);
		
		JSpinner spnAdd = new JSpinner();
		spnAdd.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		GridBagConstraints gbc_spnAdd = new GridBagConstraints();
		gbc_spnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_spnAdd.gridx = 1;
		gbc_spnAdd.gridy = 0;
		add(spnAdd, gbc_spnAdd);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int minutes = (Integer)spnAdd.getValue();
				String startTime = DB.getFirst("select max(start_time) from work where project_id = ? and the_date = current_date", projectId);
				if (startTime == null) {
					// project hasn't started today yet
					DB.perform("insert into work (project_id, start_time, end_time, total, the_date) values "
							+ "(?, dateadd('MINUTE', ?, current_timestamp), current_timestamp, ?, current_date);", 
							projectId, -1*minutes, minutes*60);
					Events.fireEvent(EventType.UPDATE);
				} else {
					DB.perform("update work set start_time = dateadd('MINUTE', ?, start_time) where project_id = ? and start_time = ?", minutes*-1, projectId, startTime);
					Events.fireEvent(EventType.UPDATE);
				}
				dialog.setVisible(false);
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 0;
		add(btnAdd, gbc_btnAdd);
		
		JLabel lblSubtractMinutes = new JLabel("Subtract minutes:");
		GridBagConstraints gbc_lblSubtractMinutes = new GridBagConstraints();
		gbc_lblSubtractMinutes.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubtractMinutes.gridx = 0;
		gbc_lblSubtractMinutes.gridy = 1;
		add(lblSubtractMinutes, gbc_lblSubtractMinutes);
		
		JSpinner spnSubtract = new JSpinner();
		spnSubtract.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		GridBagConstraints gbc_spnSubtract = new GridBagConstraints();
		gbc_spnSubtract.insets = new Insets(0, 0, 5, 5);
		gbc_spnSubtract.gridx = 1;
		gbc_spnSubtract.gridy = 1;
		add(spnSubtract, gbc_spnSubtract);
		
		JButton btnSubtract = new JButton("Subtract");
		btnSubtract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int minutes = (Integer)spnSubtract.getValue();
				String startTime = DB.getFirst("select max(start_time) from work where project_id = ? and the_date = current_date", projectId);
				Integer lastDuration = DB.getFirstInt("select datediff('SECOND', start_time, coalesce(end_time, current_timestamp)) from work "
						+ "where project_id = ? and start_time = ?;", projectId, startTime);
				
				if (startTime == null) {
					JOptionPane.showMessageDialog(null, "You haven't done anything today yet in that project.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (lastDuration < minutes*60) {
					JOptionPane.showMessageDialog(null, "You don't have that many minutes to take off.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					if (DB.getFirst("select end_time from work where project_id = ? and start_time = ?", projectId, startTime) == null) {
						// still timing right now
						DB.perform("update work set end_time = dateadd('MINUTE', ?, current_timestamp) where project_id = ? and start_time = ?", minutes*-1, projectId, startTime);
						Events.fireEvent(EventType.STOP);
						DB.perform("insert into work (project_id, start_time, the_date) values "
								+ "(?, current_timestamp, current_date);", projectId);
						Events.fireEvent(EventType.UPDATE);
					} else {
						DB.perform("update work set end_time = dateadd('MINUTE', ?, end_time) where project_id = ? and start_time = ?", minutes*-1, projectId, startTime);
						Events.fireEvent(EventType.UPDATE);
					}
				}
				dialog.setVisible(false);
			}
		});
		GridBagConstraints gbc_btnSubtract = new GridBagConstraints();
		gbc_btnSubtract.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSubtract.insets = new Insets(0, 0, 5, 0);
		gbc_btnSubtract.gridx = 2;
		gbc_btnSubtract.gridy = 1;
		add(btnSubtract, gbc_btnSubtract);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 2;
		add(btnCancel, gbc_btnCancel);
	}

}
