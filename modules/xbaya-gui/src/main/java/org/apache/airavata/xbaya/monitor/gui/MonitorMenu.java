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

package org.apache.airavata.xbaya.monitor.gui;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.XBayaGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMenu {

    private static final Logger logger = LoggerFactory.getLogger(MonitorMenu.class);

    private XBayaEngine engine;

    private XBayaGUI gui;

    private JMenu monitorMenu;


    /**
     * Constructs a MonitorMenu.
     * 
     * @param engine
     */
    public MonitorMenu(XBayaEngine engine) {
        this.engine = engine;
        this.gui = engine.getGUI();
        this.monitorMenu = createMonitorMenu();

    }

    /**
     * @return The JMenu.
     */
    public JMenu getMenu() {
        return this.monitorMenu;
    }

    private JMenu createMonitorMenu() {
        JMenu menu = new JMenu("Monitoring");
        menu.setMnemonic(KeyEvent.VK_M);



        menu.addSeparator();
        menu.addSeparator();
        return menu;
    }

  
}