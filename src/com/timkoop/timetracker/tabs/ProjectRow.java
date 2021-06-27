package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.DataRow;
import com.timkoop.timetracker.EventType;
import com.timkoop.timetracker.Events;
import com.timkoop.timetracker.MainWindow;
import com.timkoop.timetracker.Utils;

public class ProjectRow extends JPanel {
	private static final long serialVersionUID = 8803259388597449668L;

	private JLabel timeLabel = new JLabel("0:00");
	private int projectId = 0; 
	
	private AdjustTime adjustTimeWindow = new AdjustTime();
	
	public int getProjectId() {
		return projectId;
	}
	
	public void updateTime() {
		Integer total = DB.getFirstInt("select coalesce(sum(datediff('SECOND', start_time, coalesce(end_time, current_timestamp))), 0) as total "
				+ "from work where project_id = ? and the_date = current_date", projectId);
		String formatted = Utils.secondsToFormat(total == null ? 0 : total);
		timeLabel.setText(formatted);
	}

	public ProjectRow(int projectId, String colour, String name) {
		this.projectId = projectId;
		this.setBorder(new EmptyBorder(6, 6, 0, 6));
		this.setLayout(new BorderLayout(6, 6));
		
		JLabel lblNewLabel = new JLabel("");
		this.add(lblNewLabel, BorderLayout.WEST);
		lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("/images/"+colour+".png")));
		
		JTextField textField = new JTextField();
		this.add(textField, BorderLayout.CENTER);
		textField.setText(name);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			
			private void update() {
				Events.fireEvent(EventType.NAME_CHANGED, new DataRow("id", projectId, "name", textField.getText()));
			}
		});
		
		
		String runningId = DB.getFirst("select project_id from work where end_time is null;");
		
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BorderLayout(0, 0));

		timeLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
		eastPanel.add(timeLabel, BorderLayout.WEST);
		

		JButton optionsButton = new JButton();
		eastPanel.add(optionsButton, BorderLayout.EAST);
		optionsButton.setIcon(new ImageIcon(ProjectsTab.class.getResource("/icons/options.png")));
		optionsButton.setBorder(new EmptyBorder(3, 7, 3, 7));
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popUpOptions(projectId, optionsButton);
			}
		});
		
		this.add(eastPanel, BorderLayout.EAST);
		
		if ((""+projectId).equals(runningId)) {
			textField.setFont(textField.getFont().deriveFont(Font.BOLD));
			JButton btnStartButton = new JButton();
			btnStartButton.setIcon(new ImageIcon(ProjectsTab.class.getResource("/icons/stop.png")));
			btnStartButton.setBorder(new EmptyBorder(3, 12, 3, 12));
			btnStartButton.setSelected(true);

			btnStartButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Events.fireEvent(EventType.STOP, new DataRow("id", ""+projectId));
				}
			});
			eastPanel.add(btnStartButton, BorderLayout.CENTER);
		} else {
			JButton btnStartButton = new JButton();
			btnStartButton.setIcon(new ImageIcon(ProjectsTab.class.getResource("/icons/play.png")));
			btnStartButton.setBorder(new EmptyBorder(3, 12, 3, 12));

			btnStartButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Events.fireEvent(EventType.START, new DataRow("id", ""+projectId));
				}
			});
			eastPanel.add(btnStartButton, BorderLayout.CENTER);
		}
		
		updateTime();
	}

	private void popUpOptions(int projectId, JButton button) {
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem time = new JMenuItem("Adjust Time");
		time.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Window parentWindow = SwingUtilities.windowForComponent(button);
	            JDialog dialog = new JDialog(parentWindow, "Adjust Time");
	            dialog.setLocationRelativeTo(button);
	            dialog.setModal(true);
	            adjustTimeWindow.setProjectId(projectId);
	            adjustTimeWindow.setJDialog(dialog);
	            dialog.add(adjustTimeWindow);
	            dialog.pack();
	            dialog.setVisible(true);
			}
		});
		menu.add(time);
	
		JMenuItem moveUp = new JMenuItem("Move Up");
		moveUp.setIcon(new ImageIcon(ProjectsTab.class.getResource("/icons/up.png")));
		moveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DB.perform("update project set the_order = the_order - 15 where project_id = ?", projectId);
				Utils.reorderProjects();
				Events.fireEvent(EventType.UPDATE);
			}
		});
		menu.add(moveUp);
	
		JMenuItem moveDown = new JMenuItem("Move Down");
		moveDown.setIcon(new ImageIcon(ProjectsTab.class.getResource("/icons/down.png")));
		moveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DB.perform("update project set the_order = the_order + 15 where project_id = ?", projectId);
				Utils.reorderProjects();
				Events.fireEvent(EventType.UPDATE);
			}
		});
		menu.add(moveDown);
	
		JMenu changeColour = new JMenu("Change Colour");
		for(int i=0; i<Utils.COLOURS.length; i++) {
			final int c = i;
			JMenuItem colourItem = new JMenuItem(Utils.COLOUR_NAMES[c]);
			colourItem.setIcon(new ImageIcon(ProjectsTab.class.getResource("/images/"+Utils.COLOURS[c]+".png")));
			colourItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DB.perform("update project set colour = ? where project_id = ?", Utils.COLOURS[c], projectId);
					Events.fireEvent(EventType.UPDATE);
				}
			});
			changeColour.add(colourItem);
		}
		menu.add(changeColour);
		
	
		menu.addSeparator();
		
		JMenuItem delete = new JMenuItem("Delete Projct");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String projectName = DB.getFirst("select name from project where project_id = ?",  projectId);
				int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to permanently delete \""+projectName+"\"?", "Warning", JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.YES_OPTION){
					DB.perform("delete from work where project_id = ?", projectId);
					DB.perform("delete from project where project_id = ?", projectId);
					Utils.reorderProjects();
					Events.fireEvent(EventType.UPDATE);
				}
			}
		});
		menu.add(delete);
		
		menu.show(button, 0, button.getHeight());
	}
}
