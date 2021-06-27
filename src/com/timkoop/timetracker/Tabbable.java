package com.timkoop.timetracker;

import javax.swing.JComponent;
import javax.swing.JPanel;

public interface Tabbable {
	JComponent getComponent();
	
	String getTabText();
}
