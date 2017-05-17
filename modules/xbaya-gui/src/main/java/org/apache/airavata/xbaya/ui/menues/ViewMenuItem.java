/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.xbaya.ui.menues;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewMenuItem {

	private XBayaEngine engine;

	private JMenu viewMenu;

	private JMenuItem jcrRegistryView;

	private JMenuItem componentsView;

	private JMenuItem monitoringView;

	private JMenuItem parametersView;

	private static final Logger logger = LoggerFactory
			.getLogger(ViewMenuItem.class);

	/**
	 * Constructs a WorkflowMenu.
	 * 
	 * @param engine
	 */
	public ViewMenuItem(XBayaEngine engine) {
		this.engine = engine;
		createWorkflowMenu();
	}

	/**
	 * @return The workflow menu.
	 */
	public JMenu getMenu() {
		return this.viewMenu;
	}

	/**
	 * Creates workflow menu.
	 */
	private void createWorkflowMenu() {
//		this.jcrRegistryView = createShpwJCRRegistryView();
		this.componentsView = createShowComponentsView();

		monitoringView = createShowMonitoringView();
		parametersView = createShowParameterView();

		viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);

//		viewMenu.add(this.jcrRegistryView);

		viewMenu.addSeparator();

		viewMenu.add(this.componentsView);

		viewMenu.addSeparator();

		viewMenu.add(monitoringView);
		viewMenu.add(parametersView);
	}

//	private JMenuItem createShpwJCRRegistryView() {
//		JMenuItem menuItem = new JMenuItem("Airavata Registry");
//		menuItem.addActionListener(new AbstractAction() {
//			public void actionPerformed(ActionEvent e) {
//				engine.getGUI().viewJCRBrowserPanel();
//			}
//		});
//		return menuItem;
//	}

	private JMenuItem createShowComponentsView() {
		JMenuItem menuItem = new JMenuItem("Components");
		menuItem.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				engine.getGUI().viewComponentTree();
			}
		});
		return menuItem;
	}

	private JMenuItem createShowMonitoringView() {
		JMenuItem menuItem = new JMenuItem("Monitoring");
		menuItem.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		// FIXME remove this once save all functionality is fixed
		menuItem.setEnabled(false);
		return menuItem;
	}

	private JMenuItem createShowParameterView() {
		JMenuItem menuItem = new JMenuItem("Parameters");
		menuItem.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		// FIXME remove this once save all functionality is fixed
		menuItem.setEnabled(false);
		return menuItem;
	}

}