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

package org.apache.airavata.xbaya.mylead.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.event.EventListener;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.mylead.MyLeadConfiguration;
import org.ietf.jgss.GSSException;

public class MyLeadMenu implements EventListener {

    private XBayaEngine engine;

    private JMenu myLeadMenu;

    private JMenuItem myLeadConfigurationItem;

    private JMenuItem myLeadLoadSampleWorkflowItem;

    private JMenuItem myLeadLoadWorkflowItem;

    private JMenuItem myLeadSaveWorkflowItem;

    /**
     * Constructs a MyLeadMenu.
     * 
     * @param engine
     * 
     */
    public MyLeadMenu(XBayaEngine engine) {
        this.engine = engine;
        createMyLeadMenu();
        this.engine.getMyLead().getConfiguration().addEventListener(this);
    }

    /**
     * @return The MyLead menu.
     */
    public JMenu getMenu() {
        return this.myLeadMenu;
    }

    /**
     * @see org.apache.airavata.xbaya.event.EventListener#eventReceived(org.apache.airavata.xbaya.event.Event)
     */
    public void eventReceived(Event event) {
        if (event.getType() == Event.Type.MYLEAD_CONFIGURATION_CHANGED) {
            boolean configured = this.engine.getMyLead().getConfiguration().isValid();
            this.myLeadSaveWorkflowItem.setEnabled(configured);
            this.myLeadLoadWorkflowItem.setEnabled(configured);
        }
    }

    /**
     * Creates the myLead menu.
     */
    private void createMyLeadMenu() {
        createConfigurationItem();
        createLoadSampleItem();
        createLoadItem();
        createSaveItem();

        this.myLeadMenu = new JMenu("MyLead");
        this.myLeadMenu.setMnemonic(KeyEvent.VK_L);

        this.myLeadMenu.add(this.myLeadConfigurationItem);
        this.myLeadMenu.addSeparator();
        this.myLeadMenu.add(this.myLeadLoadSampleWorkflowItem);
        this.myLeadMenu.addSeparator();
        this.myLeadMenu.add(this.myLeadLoadWorkflowItem);
        this.myLeadMenu.add(this.myLeadSaveWorkflowItem);

    }

    private void createConfigurationItem() {
        this.myLeadConfigurationItem = new JMenuItem("Configure MyLead Setting");
        this.myLeadConfigurationItem.setMnemonic(KeyEvent.VK_C);
        this.myLeadConfigurationItem.addActionListener(new AbstractAction() {
            private MyLeadConfigurationWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new MyLeadConfigurationWindow(MyLeadMenu.this.engine);
                }
                try {
                    this.window.show();
                } catch (GSSException e1) {
                    MyLeadMenu.this.engine.getErrorWindow().error(e1);
                }
            }
        });
    }

    private void createLoadSampleItem() {
        // XXX Temporally solution to load sample workflows.
        this.myLeadLoadSampleWorkflowItem = new JMenuItem("Load Sample Workflow from MyLead");
        this.myLeadLoadSampleWorkflowItem.setMnemonic(KeyEvent.VK_S);

        XBayaConfiguration config = this.engine.getConfiguration();
        URI url = config.getMyLeadAgentURL();
        String user = config.getMyLeadSampleUser();
        String project = config.getMyLeadSampleProject();
        final MyLeadConfiguration configuration = new MyLeadConfiguration(url, user, project);
        boolean valid = configuration.isValid();
        this.myLeadLoadSampleWorkflowItem.setEnabled(valid);
        this.myLeadLoadSampleWorkflowItem.addActionListener(new AbstractAction() {
            private MyLeadLoadWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    try {
                        MyLead myLead = new MyLead();
                        myLead.getConfiguration().set(configuration);
                        this.window = new MyLeadLoadWindow(MyLeadMenu.this.engine, myLead);
                    } catch (RuntimeException e) {
                        MyLeadMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        return;
                    } catch (Error e) {
                        MyLeadMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        return;
                    }
                }
                this.window.show();
            }
        });
    }

    private void createLoadItem() {
        this.myLeadLoadWorkflowItem = new JMenuItem("Load Workflow from MyLead");
        this.myLeadLoadWorkflowItem.setMnemonic(KeyEvent.VK_L);
        this.myLeadLoadWorkflowItem.addActionListener(new AbstractAction() {
            private MyLeadLoadWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new MyLeadLoadWindow(MyLeadMenu.this.engine);
                }
                this.window.show();
            }
        });
        this.myLeadLoadWorkflowItem.setEnabled(false);
    }

    private void createSaveItem() {
        this.myLeadSaveWorkflowItem = new JMenuItem("Save Workflow to MyLead");
        this.myLeadSaveWorkflowItem.setMnemonic(KeyEvent.VK_S);
        this.myLeadSaveWorkflowItem.addActionListener(new AbstractAction() {
            private MyLeadSaveWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new MyLeadSaveWindow(MyLeadMenu.this.engine);
                }
                this.window.show();
            }
        });
        this.myLeadSaveWorkflowItem.setEnabled(false);
    }
}