package org.apache.airavata.xbaya.ui.menues;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.experiment.LaunchApplicationWindow;

public class ApplicationMenuItem {
	
	private JMenu applicationMenu;

    private JMenuItem executeApplicationItem;
    
	private XBayaEngine engine;

	public ApplicationMenuItem(XBayaEngine engine) {
		this.engine = engine;

        createApplicationMenu();
	}

	private void createApplicationMenu() {
		createExecuteApplicationItem();
		this.applicationMenu = new JMenu("Run Applications");
		this.applicationMenu.add(this.executeApplicationItem);
		this.applicationMenu.addSeparator();
	}

	private void createExecuteApplicationItem() {
		this.executeApplicationItem = new JMenuItem("Execute Application");
		this.executeApplicationItem.addActionListener(new AbstractAction() {
			private LaunchApplicationWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new LaunchApplicationWindow(ApplicationMenuItem.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    ApplicationMenuItem.this.engine.getGUI().getErrorWindow().error(e1);
                }
            }
        });
		
	}

	public JMenu getMenu() {
		return this.applicationMenu;
	}

	

}
