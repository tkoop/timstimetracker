package com.timkoop.timetracker.tabs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.timkoop.timetracker.DB;
import com.timkoop.timetracker.DataRow;
import com.timkoop.timetracker.EventListener;
import com.timkoop.timetracker.EventType;
import com.timkoop.timetracker.Events;
import com.timkoop.timetracker.MainWindow;
import com.timkoop.timetracker.Tabbable;
import com.timkoop.timetracker.Utils;

public class ProjectsTab extends JPanel implements Tabbable {

	private static final long serialVersionUID = -7812985265081304456L;

	private JPanel pnlProjects;
	
	private ArrayList<ProjectRow> projectRows = new ArrayList<ProjectRow>();
	
	private String getUnusedColour() {
		for(String colour : Utils.COLOURS) {
			String pid = DB.getFirst("select project_id from project where colour = ?", colour);
			if (pid == null) return colour;
		}
		return Utils.COLOURS[0];
	}
	
	private void setProjects() {
		pnlProjects.removeAll();
		
		projectRows.clear();
		
		for(DataRow project : DB.getData("select * from project order by the_order;")) {
			int id = project.getInt("project_id");
			String colour = project.getString("colour");
			String name = project.getString("name");
			ProjectRow panel = new ProjectRow(id, colour, name);
			projectRows.add(panel);
			pnlProjects.add(panel);
		}
		
		JPanel newButtonHolder = new JPanel(new BorderLayout());
		JButton newProject = new JButton("Add Project");
		newButtonHolder.setBorder(new EmptyBorder(6, 27, 0, 0));
		newButtonHolder.add(newProject, BorderLayout.WEST);
		newProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int projectId = DB.getFirstInt("select max(project_id) from project;") + 1;
				String colour = getUnusedColour();
				DB.perform("insert into project (name, project_id, colour, the_order) values "
						+ "(?, ?, ?, ?)", "Project " + projectId, projectId, colour, 1000000);
				Utils.reorderProjects();
				ProjectsTab.this.reset();
			}
		});
		pnlProjects.add(newButtonHolder);

	}
	
	private void reset() {
		ProjectsTab.this.setProjects();
		Container con = this;
		while (con != null) {
			if (con.getClass().getName().equals("javax.swing.JTabbedPane")) {
				con.repaint();
				break;
			}
			con = con.getParent();
		}
	}
	
	public ProjectsTab() {
		this.setLayout(new BorderLayout(0, 0));
		
		pnlProjects = new JPanel();
		this.add(pnlProjects, BorderLayout.NORTH);
		pnlProjects.setLayout(new GridLayout(0, 1, 0, 0));
		
		setProjects();
		
		
		Events.registerEventListener(new EventListener() {
			@Override
			public void fire(EventType eventType, DataRow eventData) {
				if (eventType == EventType.START || eventType == EventType.STOP || eventType == EventType.UPDATE) {
					ProjectsTab.this.reset();
				}
				if (eventType == EventType.TICK) {
					for(ProjectRow row : projectRows) {
						if (row.getProjectId() == eventData.getInt("projectId")) {
							row.updateTime();
						}
					}
				}
			}
		});
	}
	

	@Override
	public JScrollPane getComponent() {
		JScrollPane scrollPane = new JScrollPane(this);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	@Override
	public String getTabText() {
		return "Projects";
	}
	
	

}
