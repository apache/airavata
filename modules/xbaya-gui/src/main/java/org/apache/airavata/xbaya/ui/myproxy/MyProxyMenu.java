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

package org.apache.airavata.xbaya.ui.myproxy;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;

public class MyProxyMenu {

    private XBayaEngine engine;

    private JMenu myProxyMenu;

    private JMenuItem loadMenuItem;

    /**
     * Constructs a MyProxyMenu.
     * 
     * @param engine
     */
    public MyProxyMenu(XBayaEngine engine) {
        this.engine = engine;
        createMonitorMenu();
    }

    /**
     * @return The JMenu.
     */
    public JMenu getMenu() {
        return this.myProxyMenu;
    }

    private void createMonitorMenu() {
        this.myProxyMenu = new JMenu("Security");
        this.myProxyMenu.setMnemonic(KeyEvent.VK_P);

        createConfigItem();

        this.myProxyMenu.add(this.loadMenuItem);
    }

    private void createConfigItem() {
        this.loadMenuItem = new JMenuItem("Load GSI Credentials from MyProxy");
        this.loadMenuItem.setMnemonic(KeyEvent.VK_L);
        this.loadMenuItem.addActionListener(new AbstractAction() {
            private MyProxyDialog dialog;

            public void actionPerformed(ActionEvent e) {
                if (this.dialog == null) {
                    this.dialog = new MyProxyDialog(MyProxyMenu.this.engine);
                }
                this.dialog.show();
            }
        });
    }

}