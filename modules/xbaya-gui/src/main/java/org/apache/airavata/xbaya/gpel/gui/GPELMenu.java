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

package org.apache.airavata.xbaya.gpel.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.event.EventListener;
import org.apache.airavata.xbaya.gpel.component.gui.GPELRegistryWindow;
import org.apache.airavata.xbaya.modifier.gui.WorkflowModifierGUI;
import org.apache.airavata.xbaya.ode.ODEInvokerWindow;
import org.apache.airavata.xbaya.ode.gui.ODEDeploymentWindow;

import xsul5.MLogger;

public class GPELMenu implements EventListener {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private JMenu bpelMenu;

    private JMenuItem configurationItem;

    private JMenuItem deployItem;

    private JMenuItem loadItem;

    private JMenuItem invokeItem;

    private JMenuItem invokePrecompiledItem;

    private JMenuItem invokeModifiedWorkflowItem;

    private JMenuItem subWorkflowItem;

    private JMenuItem odeDeploymentItem;

    private JMenuItem odeInvokeItem;

    /**
     * Constructs a GPELMenu.
     * 
     * @param engine
     */
    public GPELMenu(XBayaEngine engine) {
        this.engine = engine;
        createBPELMenu();
        this.engine.getWorkflowClient().addEventListener(this);
    }

    /**
     * @return The JMenu.
     */
    public JMenu getMenu() {
        return this.bpelMenu;
    }

    /**
     * @see org.apache.airavata.xbaya.event.EventListener#eventReceived(org.apache.airavata.xbaya.event.Event)
     */
    public void eventReceived(Event event) {
        logger.entering();

        boolean connected;
        if (event.getType() == Event.Type.GPEL_ENGINE_CONNECTED) {
            connected = true;
        } else if (event.getType() == Event.Type.GPEL_ENGINE_DISCONNECTED) {
            connected = false;
        } else {
            return;
        }
        this.deployItem.setEnabled(connected);
        this.loadItem.setEnabled(connected);
        this.invokeItem.setEnabled(connected);
        this.invokePrecompiledItem.setEnabled(connected);
        this.invokeModifiedWorkflowItem.setEnabled(connected);
        this.subWorkflowItem.setEnabled(connected);
    }

    private void createBPELMenu() {
        this.configurationItem = createConfigurationItem();
        this.deployItem = createDeployItem();
        this.loadItem = createLoadItem();
        this.invokeItem = createInvokeItem();
        this.invokePrecompiledItem = createInvokePrecompiledItem();
        this.invokeModifiedWorkflowItem = createModifyItem();
        this.subWorkflowItem = createSubWorkflowItem();
        this.odeDeploymentItem = createOdeDeploymentItem();
        this.odeInvokeItem = createOdeInvokeItem();

        this.bpelMenu = new JMenu("GPEL");
        this.bpelMenu.setMnemonic(KeyEvent.VK_B);

        this.bpelMenu.add(this.configurationItem);
        this.bpelMenu.addSeparator();
        this.bpelMenu.add(this.deployItem);
        this.bpelMenu.add(this.loadItem);
        this.bpelMenu.addSeparator();
        this.bpelMenu.add(this.invokeItem);
        this.bpelMenu.add(this.invokePrecompiledItem);
        this.bpelMenu.add(this.invokeModifiedWorkflowItem);

        this.bpelMenu.addSeparator();
        this.bpelMenu.add(this.subWorkflowItem);

        this.bpelMenu.addSeparator();
        this.bpelMenu.add(this.odeDeploymentItem);
        this.bpelMenu.add(this.odeInvokeItem);

    }

    /**
     * @return
     */
    private JMenuItem createOdeInvokeItem() {
        JMenuItem item = new JMenuItem("Invoke workflow in ODE");
        item.addActionListener(new AbstractAction() {
            private ODEInvokerWindow window;

            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {

                if (this.window == null) {
                    this.window = new ODEInvokerWindow(GPELMenu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    GPELMenu.this.engine.getErrorWindow().error(e1);
                }

            }
        });
        return item;
    }

    /**
     * @return
     */
    private JMenuItem createOdeDeploymentItem() {
        JMenuItem item = new JMenuItem("Deploy to ODE");
        item.addActionListener(new AbstractAction() {
            private ODEDeploymentWindow window;

            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {

                if (this.window == null) {
                    this.window = new ODEDeploymentWindow(GPELMenu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    GPELMenu.this.engine.getErrorWindow().error(e1);
                }

            }
        });
        return item;
    }

    private JMenuItem createConfigurationItem() {
        JMenuItem item = new JMenuItem("Configure GPEL Setting");
        item.setMnemonic(KeyEvent.VK_C);
        item.addActionListener(new AbstractAction() {
            private GPELConfigurationWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new GPELConfigurationWindow(GPELMenu.this.engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createDeployItem() {
        JMenuItem item = new JMenuItem("Deploy Workflow to GPEL Engine");
        item.setMnemonic(KeyEvent.VK_D);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private GPELDeployWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GPELDeployWindow(GPELMenu.this.engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createLoadItem() {
        JMenuItem item = new JMenuItem("Load Workflow from GPEL Engine");
        item.setMnemonic(KeyEvent.VK_L);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private GPELLoadWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GPELLoadWindow(GPELMenu.this.engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createInvokeItem() {
        JMenuItem item = new JMenuItem("Invoke Workflow");
        item.setMnemonic(KeyEvent.VK_I);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private GPELInvokeWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GPELInvokeWindow(GPELMenu.this.engine);
                }
                this.window.show(true);
            }
        });
        return item;
    }

    private JMenuItem createInvokePrecompiledItem() {
        JMenuItem item = new JMenuItem("Invoke Precompiled Workflow");
        item.setMnemonic(KeyEvent.VK_P);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private GPELInvokeWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GPELInvokeWindow(GPELMenu.this.engine);
                }
                this.window.show(false);
            }
        });
        return item;
    }

    private JMenuItem createSubWorkflowItem() {
        JMenuItem item = new JMenuItem("Add Sub-workflow Components");
        item.setMnemonic(KeyEvent.VK_S);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private GPELRegistryWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new GPELRegistryWindow(GPELMenu.this.engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createModifyItem() {
        JMenuItem item = new JMenuItem("Invoke Modified Workflow");
        item.setMnemonic(KeyEvent.VK_M);
        item.setEnabled(false);
        item.addActionListener(new AbstractAction() {
            private WorkflowModifierGUI modifierGUI;

            public void actionPerformed(ActionEvent event) {
                if (this.modifierGUI == null) {
                    this.modifierGUI = new WorkflowModifierGUI(GPELMenu.this.engine);
                }
                // Errors are handled inside.
                this.modifierGUI.invokeModifiedWorkflow();
            }
        });
        return item;
    }
}