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
package org.apache.airavata.xbaya.ui.experiment;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.util.XBayaUtil;

public class ExperimentMenu {

    private JMenu experimentMenu;


    private JMenuItem configureRegistryItem;

    private JMenuItem deleteWorkflowfromRegistryItem;

    private XBayaEngine engine;

    /**
     * Constructs a FileMenu.
     * 
     * @param engine
     * 
     */
    public ExperimentMenu(XBayaEngine engine) {
        this.engine = engine;
        createExperimentMenu();
    }

    private void createExperimentMenu() {

        createConfigureRegistryItem();
//        createDeleteWorkflowtoRegistryItem();

        this.experimentMenu = new JMenu("Experiment");
        this.experimentMenu.setMnemonic(KeyEvent.VK_F);

//        this.experimentMenu.add(this.configureRegistryItem);
//        this.experimentMenu.addSeparator();
//        this.experimentMenu.add(this.deleteWorkflowfromRegistryItem);
//        this.experimentMenu.addSeparator();
//        this.experimentMenu.addSeparator();
    }

    /**
     * @return The Experiment menu.
     */
    public JMenu getMenu() {
        return this.experimentMenu;
    }

    private void createConfigureRegistryItem() {
        this.configureRegistryItem = new JMenuItem("Configure Registry");
        configureRegistryItem.setMnemonic(KeyEvent.VK_C);
        configureRegistryItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaEngine xbayaEngine = ExperimentMenu.this.engine;
                XBayaUtil.updateJCRRegistryInfo(xbayaEngine);
            }
        });
    }

//    private void createDeleteWorkflowtoRegistryItem() {
//        this.deleteWorkflowfromRegistryItem = new JMenuItem("Delete Workflows in Registry");
//        this.deleteWorkflowfromRegistryItem.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    ExperimentMenu.this.registryAccesser.deleteOGCEWorkflow(ExperimentMenu.this.engine.getGUI().getWorkflow()
//                            .getQname());
//                } catch (AiravataAPIInvocationException e1) {
//                    throw new WorkflowRuntimeException(e1);
//                }
//            }
//        });
//    }

}