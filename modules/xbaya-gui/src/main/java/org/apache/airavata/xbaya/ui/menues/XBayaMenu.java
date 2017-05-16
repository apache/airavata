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

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
//import org.apache.airavata.xbaya.menues.tools.ToolsMenuItem;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.AboutWindow;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaToolBar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class XBayaMenu implements XBayaComponent,XBayaExecutionModeListener{

    private XBayaEngine engine;

    private JMenuBar menuBar;

	private XBayaMenuItem xBayaMenuItem;

	private EditMenuItem editMenuItem;

//	private ViewMenuItem viewMenuItem;

	private RunMenuItem runMenuItem;

	private RegistryMenuItem registryMenuItem;

//    private AmazonEC2MenuItem amazonEC2MenuItem;
    
//    private ApplicationMenuItem applicationMenuItem;

//	private ToolsMenuItem toolsMenuItem;

	private XBayaGUI gui;

	private XBayaToolBar toolBar;
	
    /**
     * Constructs an XwfMenu.
     * 
     * @param engine
     */
    public XBayaMenu(XBayaEngine engine, XBayaToolBar toolBar) {
        this.setEngine(engine);
        setToolBar(toolBar);
        initMenu();
		engine.getConfiguration().registerExecutionModeChangeListener(this);
    }
    
	private void initMenu() {
		xBayaMenuItem = new XBayaMenuItem(getEngine(),getToolBar());
		editMenuItem = new EditMenuItem(getEngine());
//        amazonEC2MenuItem = new AmazonEC2MenuItem(getEngine());
//		viewMenuItem = new ViewMenuItem(getEngine());
		runMenuItem = new RunMenuItem(getEngine(), getToolBar());
		registryMenuItem = new RegistryMenuItem(getEngine(),getToolBar());
//		toolsMenuItem = new ToolsMenuItem(getEngine());
//		applicationMenuItem = new ApplicationMenuItem(getEngine());
		createMenuBar();
		executionModeChanged(getEngine().getConfiguration());
	}

    /**
     * Returns the menu bar.
     * 
     * @return The menu bar.
     */
    public JMenuBar getSwingComponent() {
        return this.menuBar;
    }

    /**
     * Creates the menu bar.
     */
    private void createMenuBar() {
        this.menuBar = new JMenuBar();
        menuBar.add(xBayaMenuItem.getMenu());
        menuBar.add(editMenuItem.getMenu());
//        menuBar.add(viewMenuItem.getMenu());
        menuBar.add(runMenuItem.getMenu());
//        menuBar.add(toolsMenuItem.getMenu());
        menuBar.add(registryMenuItem.getMenu());
//        menuBar.add(amazonEC2MenuItem.getMenu());
//        menuBar.add(applicationMenuItem.getMenu());
        // Space before Help
        this.menuBar.add(Box.createHorizontalGlue());

        this.menuBar.add(createHelpMenu());
    }

    private JMenu createHelpMenu() {
        // Help
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About " + XBayaConstants.APPLICATION_SHORT_NAME);
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.addActionListener(new AbstractAction() {
            private AboutWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new AboutWindow(XBayaMenu.this.getEngine());
                }
                this.window.show();
            }
        });
        helpMenu.add(aboutItem);

        return helpMenu;
    }

	public XBayaEngine getEngine() {
		return engine;
	}

	public void setEngine(XBayaEngine engine) {
		this.engine = engine;
	}

	public XBayaToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(XBayaToolBar toolBar) {
		this.toolBar = toolBar;
	}

	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		this.menuBar.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);	
	}

}