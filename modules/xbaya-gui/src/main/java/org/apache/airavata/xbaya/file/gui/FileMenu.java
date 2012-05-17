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

package org.apache.airavata.xbaya.file.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.ode.ODEDeploymentDescriptor;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.wf.gui.WorkflowMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMenu {

    private static final Logger logger = LoggerFactory.getLogger(FileMenu.class);

    private JMenu fileMenu;

    private WorkflowFiler graphFiler;

    private JythonFiler jythonFiler;

    private ImageFiler imageFiler;

    private BPELFiler bpelFiler;

    private ScuflFiler scuflFiler;

    private ODEDeploymentDescriptor odeDeploymentDescription;

    private JMenuItem openWorkflowItem;

    private JMenuItem saveWorkflowItem;

    private JMenuItem exportJythonItem;

    private JMenuItem exportBpelItem;

    private JMenuItem saveImageItem;

    private JMenuItem importWorkflowItem;

    private JMenuItem exportODEScriptsItem;

    private JMenuItem exitItem;

    private XBayaEngine engine;

    /**
     * Constructs a FileMenu.
     * 
     * @param engine
     * 
     */
    public FileMenu(XBayaEngine engine) {
        this.engine = engine;
        this.graphFiler = new WorkflowFiler(engine);
        this.jythonFiler = new JythonFiler(engine);
        this.imageFiler = new ImageFiler(engine);
        this.bpelFiler = new BPELFiler(engine);
        this.scuflFiler = new ScuflFiler(engine);
        this.odeDeploymentDescription = new ODEDeploymentDescriptor();

        this.exitItem = createExitItem();

        createFileMenu();
    }

    private void createFileMenu() {

        createOpenWorkflowMenuItem();
        createSaveWorkflowItem();
        createImportWorkflowItem();
        createExportJythonScriptItem();
        createExportBpelScriptItem();
        createSaveWorkflowImageItem();
        // createExportScuflScriptItem();
        createExportODEScriptsItem();

        this.fileMenu = new JMenu("File");
        this.fileMenu.setMnemonic(KeyEvent.VK_F);

        this.fileMenu.add(this.openWorkflowItem);
        this.fileMenu.add(this.saveWorkflowItem);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.importWorkflowItem);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.exportJythonItem);
        this.fileMenu.add(this.exportBpelItem);
        this.fileMenu.add(this.exportODEScriptsItem);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.saveImageItem);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.exitItem);
    }

    /**
     * @return The file menu.
     */
    public JMenu getMenu() {
        return this.fileMenu;
    }

    private void createOpenWorkflowMenuItem() {
        this.openWorkflowItem = new JMenuItem("Open Workflow");
        this.openWorkflowItem.setMnemonic(KeyEvent.VK_O);
        this.openWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                FileMenu.this.graphFiler.openWorkflow();
            }
        });
    }

    private void createSaveWorkflowItem() {
        this.saveWorkflowItem = new JMenuItem("Save Workflow");
        this.saveWorkflowItem.setMnemonic(KeyEvent.VK_S);
        this.saveWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                FileMenu.this.graphFiler.saveWorkflow();
            }
        });
    }

    private void createImportWorkflowItem() {
        this.importWorkflowItem = new JMenuItem("Import Workflow");
        this.importWorkflowItem.setMnemonic(KeyEvent.VK_I);
        this.importWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                FileMenu.this.graphFiler.importWorkflow();
            }
        });
    }

    private void createExportJythonScriptItem() {
        this.exportJythonItem = new JMenuItem("Save as WS Jython Script");
        this.exportJythonItem.setMnemonic(KeyEvent.VK_J);
        this.exportJythonItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                FileMenu.this.jythonFiler.exportJythonScript();
            }
        });
    }

    private void createExportBpelScriptItem() {
        this.exportBpelItem = new JMenuItem("Save as BPEL2 Script");
        this.exportBpelItem.setMnemonic(KeyEvent.VK_B);
        this.exportBpelItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                FileMenu.this.bpelFiler.exportBPEL();
            }
        });
    }

    private void createSaveWorkflowImageItem() {
        this.saveImageItem = new JMenuItem("Save Workflow Image");
        this.saveImageItem.setMnemonic(KeyEvent.VK_I);
        this.saveImageItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                FileMenu.this.imageFiler.saveWorkflowImage();
            }
        });
    }

    // private void createExportScuflScriptItem(){
    // this.exportScuflItem = new JMenuItem("Export Taverna Scufl");
    // this.exportScuflItem.setMnemonic(KeyEvent.VK_T);
    // this.exportScuflItem.addActionListener(new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // FileMenu.this.scuflFiler.exportScuflScript();
    // }
    // });
    // }

    private void createExportODEScriptsItem() {
        this.exportODEScriptsItem = new JMenuItem("Save as ODE Scripts");
        this.exportODEScriptsItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ODEScriptFiler(FileMenu.this.engine).save();

            }
        });
    }

    private JMenuItem createExitItem() {
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    FileMenu.this.engine.dispose();
                } catch (WorkflowException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    FileMenu.this.engine.getGUI().getFrame().dispose();
                }
            }
        });
        return menuItem;
    }
}