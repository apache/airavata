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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.datadriven.WorkflowHarvester;
import org.apache.airavata.xbaya.graph.dynamic.gui.DynamicWorkflowRunnerWindow;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.gridchem.gui.GridChemRunnerWindow;
import org.apache.airavata.xbaya.jython.gui.JythonRunnerWindow;
import org.apache.airavata.xbaya.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowMenu {

    private XBayaEngine engine;

    private JMenu workflowMenu;

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
        // this.launchInTavernaItem = createLaunchInTavernaItem();

        this.workflowMenu = new JMenu("Workflow");
        this.workflowMenu.setMnemonic(KeyEvent.VK_W);

        this.workflowMenu.addSeparator();
        this.workflowMenu.addSeparator();
        // this.workflowMenu.add(this.launchInTavernaItem);
        this.workflowMenu.addSeparator();
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