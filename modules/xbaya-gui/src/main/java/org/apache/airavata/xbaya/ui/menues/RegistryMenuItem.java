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

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.widgets.ToolbarButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaToolBar;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryMenuItem implements XBayaExecutionModeListener {

	private static final String REGISTRY_ACTIONS = "registry_actions";

    private XBayaEngine engine;

    private JMenu registryMenu;

    private JMenuItem jcrRegistryItem;

    private static final Logger logger = LoggerFactory.getLogger(RegistryMenuItem.class);

    private XBayaToolBar toolBar;

	private ToolbarButton toolbarButtonJCR;
    
    /**
     * Constructs a WorkflowMenu.
     * 
     * @param engine
     */
    public RegistryMenuItem(XBayaEngine engine, XBayaToolBar toolBar) {
        this.engine = engine;
        setToolBar(toolBar);
        createWorkflowMenu();
		engine.getConfiguration().registerExecutionModeChangeListener(this);
    }

    /**
     * @return The workflow menu.
     */
    public JMenu getMenu() {
        return this.registryMenu;
    }

    /**
     * Creates workflow menu.
     */
    private void createWorkflowMenu() {
        this.jcrRegistryItem = createJCRRegistryItem();

        registryMenu = new JMenu("Registry");
        registryMenu.setMnemonic(KeyEvent.VK_G);

        registryMenu.add(this.jcrRegistryItem);
        executionModeChanged(engine.getConfiguration());

    }
    
    private JMenuItem createJCRRegistryItem() {
        JMenuItem item = new JMenuItem("Setup Airavata Registry...",MenuIcons.JCR_ICON);
        item.setMnemonic(KeyEvent.VK_J);
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                XBayaEngine xbayaEngine = engine;
                XBayaUtil.updateJCRRegistryInfo(xbayaEngine);
            }

        };
		item.addActionListener(action);
		toolbarButtonJCR = getToolBar().addToolbarButton(REGISTRY_ACTIONS, item.getText(), MenuIcons.JCR_ICON, item.getText(), action, 1);
        return item;
    }

	public XBayaToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(XBayaToolBar toolBar) {
		this.toolBar = toolBar;
	}
	
	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		toolbarButtonJCR.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
	}
}