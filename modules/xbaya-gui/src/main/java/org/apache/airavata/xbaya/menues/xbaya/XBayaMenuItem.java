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

package org.apache.airavata.xbaya.menues.xbaya;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.appwrapper.ApplicationDescriptionDialog;
import org.apache.airavata.xbaya.appwrapper.HostDescriptionDialog;
import org.apache.airavata.xbaya.appwrapper.ServiceDescriptionDialog;
import org.apache.airavata.xbaya.experiment.gui.RegistryLoaderWindow;
import org.apache.airavata.xbaya.ode.ODEDeploymentDescriptor;
import org.apache.airavata.xbaya.registry.RegistryAccesser;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaMenuItem {

    private static final Logger logger = LoggerFactory.getLogger(XBayaMenuItem.class);

    private JMenu xbayaMenuItem;

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

    private JMenuItem importWorkflowItemFromFileSystem;

    private JMenuItem exportODEScriptsItem;
    
    private JMenuItem clearWorkflowItem;

    private JMenuItem newWorkflowTabItem;

    private JMenuItem closeWorkflowItem;

    private JMenuItem nextWorkflowTabItem;
    
    private JMenuItem exitItem;

    private XBayaEngine engine;
    
    private JMenuItem registerServiceDesc;

    private JMenuItem registerApplicationDesc;

    private JMenuItem registerHostDesc;

	private JMenuItem closeAllWorkflowItem;

	private JMenuItem saveAsWorkflowItem;

	private JMenuItem saveAllWorkflowItem;

    private JMenuItem saveWorkflowtoRegistryItem;

	private JMenuItem importWorkflowItemFromRegistry;

	private RegistryAccesser registryAccesser;

    /**
     * Constructs a FileMenu.
     * 
     * @param engine
     * 
     */
    public XBayaMenuItem(XBayaEngine engine) {
        this.engine = engine;
        this.registryAccesser = new RegistryAccesser(engine);

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
        createSaveAsWorkflowItem();
        createSaveAllWorkflowItem();
        createSaveWorkflowtoRegistryItem();

        createImportWorkflowItemFromFileSystem();
        createImportWorkflowItemFromRegistry();
        createExportJythonScriptItem();
        createExportBpelScriptItem();
        createSaveWorkflowImageItem();
        createExportODEScriptsItem();
        
        clearWorkflowItem = createClearWorkflowItem();
        newWorkflowTabItem = createNewWorkflowTabMenuItem();
        closeWorkflowItem = createCloseWorkflowTabItem();
        closeAllWorkflowItem = createCloseAllWorkflowTabItem();
        nextWorkflowTabItem = createNextWorkflowTabItem();
        
        createRegisterHostDesc();
        createRegisterServiceDesc();
        createRegisterApplicationDesc();
        
        xbayaMenuItem = new JMenu("XBaya");
        xbayaMenuItem.setMnemonic(KeyEvent.VK_F);
        
        JMenu newMenu = new JMenu("New");
	        newMenu.add(newWorkflowTabItem);
	        
	        newMenu.addSeparator();
	        
	        newMenu.add(this.registerHostDesc);
	        newMenu.add(this.registerServiceDesc);
	        newMenu.add(this.registerApplicationDesc);
	        
        xbayaMenuItem.add(newMenu);
        xbayaMenuItem.add(this.openWorkflowItem);
        
        xbayaMenuItem.addSeparator();
        
        xbayaMenuItem.add(clearWorkflowItem);
        xbayaMenuItem.add(closeWorkflowItem);
        xbayaMenuItem.add(closeAllWorkflowItem);
        
        //This menu item did not seem useful at all
//        xbayaMenuItem.add(this.nextWorkflowTabItem);

        xbayaMenuItem.addSeparator();

        xbayaMenuItem.add(this.saveWorkflowItem);

        xbayaMenuItem.add(this.saveAsWorkflowItem);
        xbayaMenuItem.add(this.saveAllWorkflowItem);
        
        xbayaMenuItem.addSeparator();
        JMenu importMenu = new JMenu("Import");
        	importMenu.add(importWorkflowItemFromFileSystem);
        	importMenu.add(importWorkflowItemFromRegistry);
        	
        JMenu exportMenu = new JMenu("Export");
        	exportMenu.add(saveWorkflowtoRegistryItem);
        	exportMenu.addSeparator();
	        exportMenu.add(exportJythonItem);
	        exportMenu.add(exportBpelItem);
	        exportMenu.add(exportODEScriptsItem);
	        exportMenu.addSeparator();
	        exportMenu.add(saveImageItem);
        
        xbayaMenuItem.add(importMenu);
        xbayaMenuItem.add(exportMenu);
        
        xbayaMenuItem.addSeparator();
        
        xbayaMenuItem.add(exitItem);
    }

    /**
     * @return The file menu.
     */
    public JMenu getMenu() {
        return this.xbayaMenuItem;
    }

    private void createSaveWorkflowtoRegistryItem() {
        this.saveWorkflowtoRegistryItem = new JMenuItem("To Registry...");
        this.saveWorkflowtoRegistryItem.setMnemonic(KeyEvent.VK_C);
        this.saveWorkflowtoRegistryItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                registryAccesser.saveWorkflow();
            }
        });
    }
    
    private void createRegisterServiceDesc() {
        this.registerServiceDesc = new JMenuItem("Service Description...");

        this.registerServiceDesc.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (XBayaUtil.acquireJCRRegistry(engine)) {
                    try {
                        ServiceDescriptionDialog serviceDescriptionDialog = new ServiceDescriptionDialog(
                                engine.getConfiguration().getJcrComponentRegistry()
                                        .getRegistry());
                        serviceDescriptionDialog.open();
                    } catch (Exception e1) {
                        engine.getErrorWindow().error(e1);
                    }
                }
            }
        });

    }

    private void createRegisterApplicationDesc() {
        this.registerApplicationDesc = new JMenuItem("Application Description...");

        this.registerApplicationDesc.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (XBayaUtil.acquireJCRRegistry(engine)) {
                    try {
                        ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(
                                engine);
                        applicationDescriptionDialog.open();
                    } catch (Exception e1) {
                        engine.getErrorWindow().error(e1);
                    }
                }
            }
        });

    }

    private void createRegisterHostDesc() {
        this.registerHostDesc = new JMenuItem("Host Description...");

        this.registerHostDesc.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (XBayaUtil.acquireJCRRegistry(engine)) {
                    try {
                        HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(
                                engine);
                        // TODO : should remove this
                        //hostDescriptionDialog.open();
                        hostDescriptionDialog.show();
                    } catch (Exception e1) {
                        engine.getErrorWindow().error(e1);
                    }
                }
            }
        });

    }
    private JMenuItem createClearWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Clear Workflow");
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().getGraphCanvas().newWorkflow();
            }
        });
        return menuItem;
    }

    private JMenuItem createNewWorkflowTabMenuItem() {
        JMenuItem menuItem = new JMenuItem("Workflow");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().newGraphCanvas(true);
            }
        });
        return menuItem;
    }

    private JMenuItem createCloseWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Close");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().closeGraphCanvas();
            }
        });
        return menuItem;
    }

    private JMenuItem createCloseAllWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Close all");
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().closeAllGraphCanvas();
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
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().selectNextGraphCanvas();
            }
        });
        return menuItem;
    }
    
    private void createOpenWorkflowMenuItem() {
        this.openWorkflowItem = new JMenuItem("Open...");
        this.openWorkflowItem.setMnemonic(KeyEvent.VK_O);
        this.openWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                XBayaMenuItem.this.graphFiler.openWorkflow();
            }
        });
    }

    private void createSaveWorkflowItem() {
        saveWorkflowItem = new JMenuItem("Save");
        saveWorkflowItem.setMnemonic(KeyEvent.VK_S);
        saveWorkflowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveWorkflow();
            }
        });
    }
    
    private void createSaveAsWorkflowItem() {
        saveAsWorkflowItem = new JMenuItem("Save as...");
        saveAsWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveAsWorkflow();
            }
        });
    }
    
    private void createSaveAllWorkflowItem() {
        saveAllWorkflowItem = new JMenuItem("Save all");
        saveAllWorkflowItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveAllWorkflows();
            }
        });
    }

    private void createImportWorkflowItemFromFileSystem() {
        importWorkflowItemFromFileSystem = new JMenuItem("From file system...");
        importWorkflowItemFromFileSystem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.importWorkflow();

            }
        });
    }
    
    private void createImportWorkflowItemFromRegistry() {
        importWorkflowItemFromRegistry = new JMenuItem("From registry...");
        importWorkflowItemFromRegistry.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new RegistryLoaderWindow(engine).show();
            }
        });
    }
    
    private void createExportJythonScriptItem() {
        this.exportJythonItem = new JMenuItem("Jython Script...");
        this.exportJythonItem.setMnemonic(KeyEvent.VK_J);
        this.exportJythonItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.jythonFiler.exportJythonScript();
            }
        });
    }

    private void createExportBpelScriptItem() {
        this.exportBpelItem = new JMenuItem("BPEL2 Script...");
        this.exportBpelItem.setMnemonic(KeyEvent.VK_B);
        this.exportBpelItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.bpelFiler.exportBPEL();
            }
        });
    }

    private void createSaveWorkflowImageItem() {
        this.saveImageItem = new JMenuItem("Image...");
        this.saveImageItem.setMnemonic(KeyEvent.VK_I);
        this.saveImageItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.imageFiler.saveWorkflowImage();
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
        this.exportODEScriptsItem = new JMenuItem("ODE Scripts...");
        this.exportODEScriptsItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ODEScriptFiler(XBayaMenuItem.this.engine).save();

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
                    XBayaMenuItem.this.engine.dispose();
                } catch (XBayaException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    XBayaMenuItem.this.engine.getGUI().getFrame().dispose();
                }
            }
        });
        return menuItem;
    }
}