/*
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
 *
 */

package org.apache.airavata.xbaya.menues.registry;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.gui.ComponentMenu;
import org.apache.airavata.xbaya.component.gui.JCRRegistryWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryMenuItem {

    private XBayaEngine engine;

    private JMenu registryMenu;

    private JMenuItem jcrRegistryItem;

    private static final Logger logger = LoggerFactory.getLogger(RegistryMenuItem.class);

    /**
     * Constructs a WorkflowMenu.
     * 
     * @param engine
     */
    public RegistryMenuItem(XBayaEngine engine) {
        this.engine = engine;
        createWorkflowMenu();
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
        registryMenu.setMnemonic(KeyEvent.VK_R);

        registryMenu.add(this.jcrRegistryItem);

    }
    

    private JMenuItem createJCRRegistryItem() {
        JMenuItem item = new JMenuItem("Setup JCR Registry...");
        item.setMnemonic(KeyEvent.VK_J);
        item.addActionListener(new AbstractAction() {
            private JCRRegistryWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new JCRRegistryWindow(engine);
                }
                this.window.show();
            }
        });
        return item;
    }
}