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

package org.apache.airavata.xbaya.ui.pegasus;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;

public class PegasusMenu {

    // private JMenu pegasusMenu;
    //
    // protected XRegistryAccesser xregistry;
    //
    // private JMenuItem configureXRegistryItem;
    //
    // private JMenuItem saveWorkflowtoXRegistryItem;
    //
    // private JMenuItem loadWorkflowfromXRegistryItem;
    //
    // private JMenuItem launchPegasusWorkflowItem;
    //
    // private XBayaEngine engine;
    //
    // /**
    // * Constructs a FileMenu.
    // *
    // * @param engine
    // *
    // */
    // public PegasusMenu(XBayaEngine engine) {
    // this.engine = engine;
    // this.xregistry = new XRegistryAccesser(engine);
    //
    // createExperimentMenu();
    // }
    //
    // private void createExperimentMenu() {
    //
    // createConfigureXRegistryItem();
    // createSaveWorkflowtoXRegistryItem();
    // createLoadWorkflowfromXRegistryItem();
    // createLaunchPegasusWorkflowItem();
    //
    // this.pegasusMenu = new JMenu("Pegasus");
    // this.pegasusMenu.setMnemonic(KeyEvent.VK_F);
    //
    // this.pegasusMenu.add(this.configureXRegistryItem);
    // this.pegasusMenu.addSeparator();
    // this.pegasusMenu.add(this.saveWorkflowtoXRegistryItem);
    // this.pegasusMenu.add(this.loadWorkflowfromXRegistryItem);
    // this.pegasusMenu.addSeparator();
    // this.pegasusMenu.add(this.launchPegasusWorkflowItem);
    // }
    //
    // /**
    // * @return The Experiment menu.
    // */
    // public JMenu getMenu() {
    // return this.pegasusMenu;
    // }
    //
    // private void createConfigureXRegistryItem() {
    // this.configureXRegistryItem = new JMenuItem("Configure XRegistry");
    // this.configureXRegistryItem.setMnemonic(KeyEvent.VK_C);
    // this.configureXRegistryItem.addActionListener(new AbstractAction() {
    // private XRegistryConfigurationWindow window;
    //
    // public void actionPerformed(ActionEvent e) {
    // if (this.window == null) {
    // this.window = new XRegistryConfigurationWindow(PegasusMenu.this.engine);
    // }
    // this.window.show();
    // }
    // });
    // }
    //
    // private void createSaveWorkflowtoXRegistryItem() {
    // this.saveWorkflowtoXRegistryItem = new JMenuItem("Save Workflow to XRegistry");
    // this.saveWorkflowtoXRegistryItem.addActionListener(new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // PegasusMenu.this.xregistry.saveWorkflow();
    // }
    // });
    // }
    //
    // private void createLoadWorkflowfromXRegistryItem() {
    // this.loadWorkflowfromXRegistryItem = new JMenuItem("Load Workflow from XRegistry");
    // this.loadWorkflowfromXRegistryItem.addActionListener(new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // new OGCEXRegistryLoaderWindow(PegasusMenu.this.engine).show();
    // }
    // });
    // }
    //
    // private void createLaunchPegasusWorkflowItem() {
    // this.launchPegasusWorkflowItem = new JMenuItem("Launch Workflow to Pegasys Engine");
    // this.launchPegasusWorkflowItem.addActionListener(new AbstractAction() {
    // private PegasusInvokerWindow window;
    //
    // public void actionPerformed(ActionEvent e) {
    // if (this.window == null) {
    // this.window = new PegasusInvokerWindow(PegasusMenu.this.engine);
    // }
    // try {
    // this.window.show();
    // } catch (Exception e1) {
    // PegasusMenu.this.engine.getErrorWindow().error(e1);
    // }
    //
    // }
    // });
    //
    // }

}