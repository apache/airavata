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

package org.apache.airavata.xbaya.ui.component;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.SubWorkflowComponent;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.component.registry.LocalComponentRegistry;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.ui.ErrorMessages;
import org.apache.airavata.xbaya.ui.file.WorkflowFiler;
import org.apache.airavata.xbaya.util.RegistryConstants;

public class ComponentMenu {

    private XBayaEngine engine;

    private JMenu componentMenu;

    private JMenuItem fileRegistryItem;

    private JMenuItem webItem;

    private JMenuItem removeItem;

    private JMenuItem refreshSelectedItem;

    private JMenuItem refreshAllItem;

    private JMenuItem workflowItem;


    /**
     * Constructs a ComponentMenu.
     * 
     * @param engine
     * 
     */
    public ComponentMenu(XBayaEngine engine) {
        this.engine = engine;
        createComponentMenu();
    }

    /**
     * @return The component menu.
     */
    public JMenu getMenu() {
        return this.componentMenu;
    }

    private void createComponentMenu() {
        this.fileRegistryItem = createFileRegistryMenuItem();
        this.webItem = createWebRegistryItem();
        this.workflowItem = createWorkflowItem();
        this.removeItem = createRemoveRegistryItem();
        this.refreshSelectedItem = createRefreshSelectedRegistryItem();
        this.refreshAllItem = createRefreshAllRegistriesItem();
        this.componentMenu = new JMenu("Component");
        this.componentMenu.setMnemonic(KeyEvent.VK_C);
        this.componentMenu.add(this.fileRegistryItem);
        this.componentMenu.add(this.webItem);
        this.componentMenu.add(this.workflowItem);
        this.componentMenu.addSeparator();
        this.componentMenu.add(this.removeItem);
        this.componentMenu.addSeparator();
        this.componentMenu.add(this.refreshSelectedItem);
        this.componentMenu.add(this.refreshAllItem);
        this.componentMenu.addSeparator();
    }

    /**
     * @return
     */
    private JMenuItem createWorkflowItem() {
        JMenuItem item = new JMenuItem("Load Workflow Component");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        Workflow workflow = new WorkflowFiler(ComponentMenu.this.engine).getWorkflow();
                        // //to normalize and build the scripts
                        try {
                            ComponentMenu.this.engine.addWorkflowComponent(workflow.getName(),
                                    SubWorkflowComponent.getInstance(workflow));

                        } catch (ComponentException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (RuntimeException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (Error e) {
                            ComponentMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        }
                    }
                }.start();
            }
        });
        return item;
    }

   

    private JMenuItem createRefreshSelectedRegistryItem() {
        JMenuItem item = new JMenuItem("Refresh Selected Registry");
        // this.refreshItem.setMnemonic(KeyEvent.VK_R);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ComponentMenu.this.engine.getGUI().getComponentSelector().updateSelectedRegistry();
                        } catch (ComponentRegistryException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (RuntimeException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (Error e) {
                            ComponentMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        }
                    }
                }.start();
            }
        });
        return item;
    }

    private JMenuItem createRefreshAllRegistriesItem() {
        JMenuItem item = new JMenuItem("Refresh All Registries");
        // this.refreshItem.setMnemonic(KeyEvent.VK_E);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ComponentMenu.this.engine.getGUI().getComponentSelector().update();
                        } catch (ComponentRegistryException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (RuntimeException e) {
                            ComponentMenu.this.engine.getErrorWindow()
                                    .error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (Error e) {
                            ComponentMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        }
                    }
                }.start();
            }
        });
        return item;
    }

    private JMenuItem createRemoveRegistryItem() {
        JMenuItem item = new JMenuItem("Remove Selected Registry");
        item.setMnemonic(KeyEvent.VK_R);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    ComponentMenu.this.engine.getGUI().getComponentSelector().removeSelectedRegistry();
                } catch (RuntimeException e) {
                    ComponentMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (Error e) {
                    ComponentMenu.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        });
        return item;
    }

    private JMenuItem createWebRegistryItem() {
        JMenuItem item = new JMenuItem("Add Web Registry");
        item.setMnemonic(KeyEvent.VK_W);
        item.addActionListener(new AbstractAction() {
            private WebResigtoryWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new WebResigtoryWindow(ComponentMenu.this.engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createFileRegistryMenuItem() {
        JMenuItem item = new JMenuItem("Add Local Directory");
        item.setMnemonic(KeyEvent.VK_L);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                registerComponentLocalDirectory();
            }
        });
        return item;
    }

    private void registerComponentLocalDirectory() {
        JFileChooser fileChooser = new JFileChooser(XBayaPathConstants.WSDL_DIRECTORY);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(this.engine.getGUI().getFrame().getContentPane());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            LocalComponentRegistry registry = new LocalComponentRegistry(directory);
            // move to another thread using loader.
            ComponentRegistryLoader loader = ComponentRegistryLoader.getLoader(this.engine, RegistryConstants.REGISTRY_TYPE_LOCAL);
            loader.load(registry);
        }
    }
}