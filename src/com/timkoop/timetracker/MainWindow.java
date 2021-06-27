package com.timkoop.timetracker;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.timkoop.timetracker.tabs.*;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1635049736963266532L;


	/**
	 * Create the frame.
	 */
	public MainWindow() {
		Events.registerEventListener(new EventListener() {
			@Override
			public void fire(EventType eventType, DataRow data) {
				if (eventType == EventType.CLOSE) {
					MainWindow.this.dispatchEvent(new WindowEvent(MainWindow.this, WindowEvent.WINDOW_CLOSING));
				}
				if (eventType == EventType.SHOW) {
					MainWindow.this.setVisible(true);
					MainWindow.this.setState(java.awt.Frame.NORMAL);
				}
			}
		});


		setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/images/white.png")));
		setTitle("Tim's Time Tracker 2.0");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {
				MainWindow.this.setVisible(false);
			}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				Events.fireEvent(EventType.CLOSE);				
			}
			public void windowActivated(WindowEvent e) {}
		});
		
		setBounds(100, 100, 550, 376);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		
		Tabbable[] tabs = new Tabbable[] {
			new ProjectsTab(),
			new ReportsTab(),
			new SQLTab(),
//			new BillingTab(),
//			new TimeEntriesTab()
		};
		
		for(Tabbable tab : tabs) {
			tabbedPane.addTab(tab.getTabText(), tab.getComponent());
		}
		
	}

}
