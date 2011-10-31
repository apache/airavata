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

package org.apache.airavata.xbaya.wf.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.dynamic.gui.DynamicWorkflowRunnerWindow;
import org.apache.airavata.xbaya.gridchem.gui.GridChemRunnerWindow;
import org.apache.airavata.xbaya.jython.gui.JythonRunnerWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowMenu {

    private XBayaEngine engine;

    private JMenu workflowMenu;

    private JMenuItem newWorkflowItem;

    private JMenuItem newWorkflowTabItem;

    private JMenuItem closeWorkflowItem;

    private JMenuItem nextWorkflowTabItem;

    private JMenuItem workflowDescriptionItem;

    private JMenuItem parameterReorderingItem;

    private JMenuItem runJythonWorkflowItem;

    private JMenuItem launchDynamicWorkflowItem;

    private JMenuItem launchGridChemWorkflowItem;

    private static final Logger logger = LoggerFactory.getLogger(WorkflowMenu.class);

    /**
     * Constructs a WorkflowMenu.
     * 
     * @param engine
     */
    public WorkflowMenu(XBayaEngine engine) {
        this.engine = engine;
        createWorkflowMenu();
    }

    /**
     * @return The workflow menu.
     */
    public JMenu getMenu() {
        return this.workflowMenu;
    }

    /**
     * Creates workflow menu.
     */
    private void createWorkflowMenu() {
        this.newWorkflowItem = createNewWorkflowItem();
        this.newWorkflowTabItem = createNewWorkflowTabMenuItem();
        this.closeWorkflowItem = createCloseWorkflowTabItem();
        this.nextWorkflowTabItem = createNextWorkflowTabItem();
        this.workflowDescriptionItem = createWorkflowDescriptionItem();
        this.parameterReorderingItem = createParameterReorderingItem();
        this.runJythonWorkflowItem = createRunJythonWorkflowItem();
        // this.launchInTavernaItem = createLaunchInTavernaItem();
        this.launchDynamicWorkflowItem = createLaunchDynamicWorkflowItem();
        this.launchGridChemWorkflowItem = createLaunchGridChemWorkflowItem();

        this.workflowMenu = new JMenu("Workflow");
        this.workflowMenu.setMnemonic(KeyEvent.VK_W);

        this.workflowMenu.add(this.newWorkflowItem);
        this.workflowMenu.addSeparator();
        this.workflowMenu.add(this.newWorkflowTabItem);
        this.workflowMenu.add(this.closeWorkflowItem);
        this.workflowMenu.add(this.nextWorkflowTabItem);
        this.workflowMenu.addSeparator();
        this.workflowMenu.add(this.workflowDescriptionItem);
        this.workflowMenu.add(this.parameterReorderingItem);
        this.workflowMenu.addSeparator();
        this.workflowMenu.add(this.runJythonWorkflowItem);
        this.workflowMenu.addSeparator();
        // this.workflowMenu.add(this.launchInTavernaItem);
        this.workflowMenu.add(this.launchDynamicWorkflowItem);
        this.workflowMenu.addSeparator();
        this.workflowMenu.add(this.launchGridChemWorkflowItem);
    }

    private JMenuItem createNewWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("New Workflow");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkflowMenu.this.engine.getGUI().getGraphCanvas().newWorkflow();
            }
        });
        return menuItem;
    }

    private JMenuItem createNewWorkflowTabMenuItem() {
        JMenuItem menuItem = new JMenuItem("New Workflow Tab");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkflowMenu.this.engine.getGUI().newGraphCanvas(true);
            }
        });
        return menuItem;
    }

    private JMenuItem createCloseWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Close Workflow Tab");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkflowMenu.this.engine.getGUI().closeGraphCanvas();
            }
        });
        return menuItem;
    }

    private JMenuItem createNextWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Select Next Workflow Tab");
        menuItem.setMnemonic(KeyEvent.VK_S);
        // XXX VK_TAB doesn't work...
        // menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
        // ActionEvent.CTRL_MASK));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkflowMenu.this.engine.getGUI().selectNextGraphCanvas();
            }
        });
        return menuItem;
    }

    private JMenuItem createWorkflowDescriptionItem() {
        JMenuItem menuItem = new JMenuItem("Workflow Properties");
        menuItem.setMnemonic(KeyEvent.VK_W);
        menuItem.addActionListener(new AbstractAction() {
            private WorkflowPropertyWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = WorkflowMenu.this.engine.getWorkflowPropertyWindow();
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    private JMenuItem createParameterReorderingItem() {
        JMenuItem menuItem = new JMenuItem("Parameter Properties");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.addActionListener(new AbstractAction() {
            private ParameterPropertyWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new ParameterPropertyWindow(WorkflowMenu.this.engine);
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    private JMenuItem createRunJythonWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Run Workflow (Jython)");
        menuItem.setMnemonic(KeyEvent.VK_J);
        menuItem.addActionListener(new AbstractAction() {
            private JythonRunnerWindow window;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new JythonRunnerWindow(WorkflowMenu.this.engine);
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    private JMenuItem createLaunchDynamicWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Launch Dynamic Workflow");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(new AbstractAction() {
            private DynamicWorkflowRunnerWindow window;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new DynamicWorkflowRunnerWindow(WorkflowMenu.this.engine);
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    private JMenuItem createLaunchGridChemWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Launch GridChem Workflow");
        menuItem.addActionListener(new AbstractAction() {
            private GridChemRunnerWindow window;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GridChemRunnerWindow(WorkflowMenu.this.engine);
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    // private JMenuItem createLaunchInTavernaItem() {
    // JMenuItem item = new JMenuItem("Launch in Dynamic Mode");
    // item.setMnemonic(KeyEvent.VK_T);
    // //TODO Chathuraa this should be made to false and enabled only if the taverna location is set
    // item.setEnabled(true);
    // item.addActionListener(new AbstractAction() {
    // private TavernaRunnerWindow window;
    //
    // public void actionPerformed(ActionEvent e) {
    // if (this.window == null) {
    // this.window = new TavernaRunnerWindow(WorkflowMenu.this.engine);
    // }
    // this.window.show();
    // }
    // });
    // return item;
    // }

}