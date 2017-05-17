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
package org.apache.airavata.xbaya.ui.menues;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.core.generators.BPELFiler;
import org.apache.airavata.xbaya.core.generators.ImageFiler;
import org.apache.airavata.xbaya.core.generators.JythonFiler;
import org.apache.airavata.xbaya.core.generators.ODEScriptFiler;
import org.apache.airavata.xbaya.core.generators.WorkflowFiler;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.ui.dialogs.component.URLRegistryWindow;
import org.apache.airavata.xbaya.ui.dialogs.workflow.WorkflowImportWindow;
import org.apache.airavata.xbaya.ui.experiment.RegistryWorkflowPublisherWindow;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.ToolbarButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaMenuItem implements XBayaExecutionModeListener {

    private static final Logger logger = LoggerFactory.getLogger(XBayaMenuItem.class);

    private JMenu xbayaMenuItem;

    private WorkflowFiler graphFiler;

    private JythonFiler jythonFiler;

    private ImageFiler imageFiler;

    private BPELFiler bpelFiler;

//    private ScuflFiler scuflFiler;
    
    private JMenuItem urlItem;

//    private ODEDeploymentDescriptor odeDeploymentDescription;

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

	private XBayaToolBar toolBar;

	private ToolbarButton toolbarButtonSave;

	private ToolbarButton toolbarButtonOpen;

	private ToolbarButton toolbarButtonNew;
	
	private static final String FILE_ACTIONS="file";
	
    /**
     * Constructs a FileMenu.
     * 
     * @param engine
     * 
     */
    public XBayaMenuItem(XBayaEngine engine, XBayaToolBar toolBar) {
        this.engine = engine;
        this.toolBar=toolBar;

        this.graphFiler = new WorkflowFiler(engine);
        this.jythonFiler = new JythonFiler(engine);
        this.imageFiler = new ImageFiler(engine);
        this.bpelFiler = new BPELFiler(engine);
//        this.scuflFiler = new ScuflFiler(engine);
//        this.odeDeploymentDescription = new ODEDeploymentDescriptor();

        this.exitItem = createExitItem();

        createFileMenu();
        engine.getConfiguration().registerExecutionModeChangeListener(this);
        XBayaToolBar.setGroupOrder(FILE_ACTIONS, 1);
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
        urlItem = createURLRegistryItem();
        
//        createRegisterHostDesc();
//        createRegisterServiceDesc();
//        createRegisterApplicationDesc();
        
        xbayaMenuItem = new JMenu("XBaya");
        xbayaMenuItem.setMnemonic(KeyEvent.VK_X);
//        JMenu newMenu = new JMenu("New");
//	        newMenu.add(newWorkflowTabItem);
//	        newMenu.addSeparator();
//	        
//	        newMenu.add(this.registerApplicationDesc);
//	        newMenu.addSeparator();
//	        JMenu regAddSubMenuItem = new JMenu("Registry additions");
//	        newMenu.add(regAddSubMenuItem);
//	        regAddSubMenuItem.add(this.registerHostDesc);
//	        regAddSubMenuItem.add(this.registerServiceDesc);
//	        
//        xbayaMenuItem.add(newMenu);
        xbayaMenuItem.add(newWorkflowTabItem);
//        xbayaMenuItem.add(registerHostDesc);
//        xbayaMenuItem.add(this.registerServiceDesc);
//        xbayaMenuItem.add(registerApplicationDesc);
        
        xbayaMenuItem.add(importWorkflowItemFromRegistry);
        xbayaMenuItem.add(saveWorkflowtoRegistryItem);

        xbayaMenuItem.addSeparator();
        
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
        
//        JMenu importMenu = new JMenu("Import");
//        	importMenu.add(importWorkflowItemFromFileSystem);
//        	importMenu.add(importWorkflowItemFromRegistry);
//        	importMenu.addSeparator();
//        	importMenu.add(urlItem);	
        	
//        JMenu exportMenu = new JMenu("Export");
//        	exportMenu.add(saveWorkflowtoRegistryItem);
//        	exportMenu.addSeparator();
//	        exportMenu.add(exportJythonItem);
//	        exportMenu.add(exportBpelItem);
//	        exportMenu.add(exportODEScriptsItem);
//	        exportMenu.addSeparator();
//	        exportMenu.add(saveImageItem);
//        
//        xbayaMenuItem.add(importMenu);
//        xbayaMenuItem.add(exportMenu);
        
        xbayaMenuItem.addSeparator();
        
        xbayaMenuItem.add(exitItem);
        
        xbayaMenuItem.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				GraphCanvas graphCanvas = engine.getGUI().getGraphCanvas();
				saveAsWorkflowItem.setEnabled(isWorkflowTabPresent() && graphCanvas.getWorkflowFile()!=null);
				saveWorkflowItem.setEnabled(isSaveShouldBeActive());
				saveAllWorkflowItem.setEnabled(engine.getGUI().getGraphCanvases().size()>0);
				saveWorkflowtoRegistryItem.setEnabled(isWorkflowTabPresent());
				exportJythonItem.setEnabled(isWorkflowTabPresent());
				exportBpelItem.setEnabled(isWorkflowTabPresent());
				exportODEScriptsItem.setEnabled(isWorkflowTabPresent());
				saveImageItem.setEnabled(isWorkflowTabPresent());
			}
			@Override
			public void menuDeselected(MenuEvent e) {}
			@Override
			public void menuCanceled(MenuEvent e) {}
		});
        executionModeChanged(engine.getConfiguration());
    }

    /**
     * @return The file menu.
     */
    public JMenu getMenu() {
        return this.xbayaMenuItem;
    }

    private void createSaveWorkflowtoRegistryItem() {
        this.saveWorkflowtoRegistryItem = new JMenuItem("Register workflow...");
        this.saveWorkflowtoRegistryItem.setMnemonic(KeyEvent.VK_C);
        this.saveWorkflowtoRegistryItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
            	//FIXME
            	RegistryWorkflowPublisherWindow window = new RegistryWorkflowPublisherWindow(engine);
            	window.show();
//                if (registryAccesser.saveWorkflow()){
//                	if (engine.getGUI().getGraphCanvas().getWorkflowFile()==null){
//                		engine.getGUI().getGraphCanvas().workflowSaved();
//                	}
//                }
            }
        });
    }
    
    private JMenuItem createURLRegistryItem() {
        JMenuItem item = new JMenuItem("WSDL From URL...");
        item.setMnemonic(KeyEvent.VK_U);
        item.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            private URLRegistryWindow window;
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new URLRegistryWindow(engine);
                }
                this.window.show();
            }
        });
        return item;
    }
    
//    private void createRegisterServiceDesc() {
//        this.registerServiceDesc = new JMenuItem("Register Application...");
//
//        this.registerServiceDesc.addActionListener(new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (XBayaUtil.acquireJCRRegistry(engine)) {
//                    try {
//                        DeploymentDescriptionDialog serviceDescriptionDialog = new DeploymentDescriptionDialog(XBayaMenuItem.this.engine.getGUI().getFrame(), engine.getConfiguration()
//                                        .getAiravataAPI());
//                    	serviceDescriptionDialog.open();
////                        ServiceDescriptionDialog serviceDescriptionDialog = new ServiceDescriptionDialog(
////                                engine.getConfiguration().getJcrComponentRegistry()
////                                        .getExperimentCatalog());
////                        serviceDescriptionDialog.open();
//                    	if (serviceDescriptionDialog.isServiceCreated()){
////                    		engine.reloadRegistry();
//                    		ComponentRegistryLoader loader = ComponentRegistryLoader.getLoader(engine, RegistryConstants.REGISTRY_TYPE_JCR);
//                    		loader.load(engine.getConfiguration().getJcrComponentRegistry());
//                    	}
//                    } catch (Exception e1) {
//                        engine.getGUI().getErrorWindow().error(e1);
//                    }
//                }
//            }
//        });
//
//    }

//    private void createRegisterApplicationDesc() {
//        this.registerApplicationDesc = new JMenuItem("Register Application...");
//
//        this.registerApplicationDesc.addActionListener(new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (XBayaUtil.acquireJCRRegistry(engine)) {
//                    try {
//                        ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(
//                                engine);
//        	    		applicationDescriptionDialog.setLocationRelativeTo(engine.getGUI().getFrame());
//                        applicationDescriptionDialog.open();
//                    } catch (Exception e1) {
//                        engine.getGUI().getErrorWindow().error(e1);
//                    }
//                }
//            }
//        });
//
//    }

//    private void createRegisterHostDesc() {
//        this.registerHostDesc = new JMenuItem("Add Host...");
//
//        this.registerHostDesc.addActionListener(new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (XBayaUtil.acquireJCRRegistry(engine)) {
//                    try {
//                        HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(
//                        		engine.getConfiguration().getAiravataAPI(),XBayaMenuItem.this.engine.getGUI().getFrame() );
//                        hostDescriptionDialog.open();
//                    } catch (Exception e1) {
//                        engine.getGUI().getErrorWindow().error(e1);
//                    }
//                }
//            }
//        });
//
//    }
    private JMenuItem createClearWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Clear Workflow");
        menuItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
                engine.getGUI().getGraphCanvas().newWorkflow();
            }
        });
        return menuItem;
    }

    private JMenuItem createNewWorkflowTabMenuItem() {
        
		JMenuItem menuItem = new JMenuItem("New Workflow", MenuIcons.NEW_ICON);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        AbstractAction action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().newGraphCanvas(true, true);
            }
        };
		menuItem.addActionListener(action);
		toolbarButtonNew=getToolBar().addToolbarButton(FILE_ACTIONS,menuItem.getText(), MenuIcons.NEW_ICON, "Create new workflow", action,1);
        return menuItem;
    }

    private JMenuItem createCloseWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Close Tab");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().closeGraphCanvas();
            }
        });
        return menuItem;
    }

    private JMenuItem createCloseAllWorkflowTabItem() {
        JMenuItem menuItem = new JMenuItem("Close All Tabs");
        menuItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
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
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                engine.getGUI().selectNextGraphCanvas();
            }
        });
        return menuItem;
    }
    
    private void createOpenWorkflowMenuItem() {
		this.openWorkflowItem = new JMenuItem("Open Workflow...", MenuIcons.OPEN_ICON);
        this.openWorkflowItem.setMnemonic(KeyEvent.VK_O);
        openWorkflowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        AbstractAction action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
                XBayaMenuItem.this.graphFiler.openWorkflow();
            }
        };
		this.openWorkflowItem.addActionListener(action);
		toolbarButtonOpen=getToolBar().addToolbarButton(FILE_ACTIONS,openWorkflowItem.getText(), MenuIcons.OPEN_ICON, "Open workflow", action,2);
    }

    private void createSaveWorkflowItem() {
		saveWorkflowItem = new JMenuItem("Save", MenuIcons.SAVE_ICON);
        saveWorkflowItem.setMnemonic(KeyEvent.VK_S);
        saveWorkflowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        AbstractAction action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveWorkflow();
                toolbarButtonSave.setEnabled(isSaveShouldBeActive());
            }
        };
		saveWorkflowItem.addActionListener(action);
        toolbarButtonSave = getToolBar().addToolbarButton(FILE_ACTIONS,saveWorkflowItem.getText(), MenuIcons.SAVE_ICON, "Save workflow", action,3);
        toolbarButtonSave.setEnabled(false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	while(engine.getGUI()==null){
            		Thread.yield();
            	}
                engine.getGUI().addWorkflowTabChangeListener(new ChangeListener(){
					@Override
					public void stateChanged(ChangeEvent event) {
						toolbarButtonSave.setEnabled(isSaveShouldBeActive());						
					}
                });
            }
        });
    }
    
    private void createSaveAsWorkflowItem() {
        saveAsWorkflowItem = new JMenuItem("Save as...");
        saveAsWorkflowItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveAsWorkflow();
            }
        });
    }
    
    private void createSaveAllWorkflowItem() {
        saveAllWorkflowItem = new JMenuItem("Save all");
        saveAllWorkflowItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.saveAllWorkflows();
            }
        });
        saveAllWorkflowItem.setEnabled(false);
    }

    private void createImportWorkflowItemFromFileSystem() {
        importWorkflowItemFromFileSystem = new JMenuItem("Workflow From File System...");
        importWorkflowItemFromFileSystem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.graphFiler.importWorkflow();
            }
        });
    }
    
    private void createImportWorkflowItemFromRegistry() {
        importWorkflowItemFromRegistry = new JMenuItem("Import workflow...");
        importWorkflowItemFromRegistry.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
            	try {
					WorkflowImportWindow window = new WorkflowImportWindow(engine);
					window.show();
				} catch (Exception e1) {
                	engine.getGUI().getErrorWindow().error(e1);
				}
            }
        });
    }
    
    private void createExportJythonScriptItem() {
        this.exportJythonItem = new JMenuItem("Workflow To Jython Script...");
        this.exportJythonItem.setMnemonic(KeyEvent.VK_J);
        this.exportJythonItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.jythonFiler.exportJythonScript();
            }
        });
    }

    private void createExportBpelScriptItem() {
        this.exportBpelItem = new JMenuItem("Workflow To BPEL2 Script...");
        this.exportBpelItem.setMnemonic(KeyEvent.VK_B);
        this.exportBpelItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.bpelFiler.exportBPEL();
            }
        });
    }

    private void createSaveWorkflowImageItem() {
        this.saveImageItem = new JMenuItem("Workflow To Image...");
        this.saveImageItem.setMnemonic(KeyEvent.VK_I);
        this.saveImageItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                XBayaMenuItem.this.imageFiler.saveWorkflowImage();
            }
        });
    }

    private void createExportODEScriptsItem() {
        this.exportODEScriptsItem = new JMenuItem("Workflow To ODE Scripts...");
        this.exportODEScriptsItem.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
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
			private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
            	JFrame frame = XBayaMenuItem.this.engine.getGUI().getFrame();
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); 
            }
        });
        return menuItem;
    }

	public XBayaToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(XBayaToolBar toolBar) {
		this.toolBar = toolBar;
	}

	private boolean isSaveShouldBeActive() {
		GraphCanvas graphCanvas = engine.getGUI().getGraphCanvas();
		return isWorkflowTabPresent() && (graphCanvas.getWorkflowFile()==null || graphCanvas.isWorkflowChanged());
	}

	private boolean isWorkflowTabPresent() {
		return engine.getGUI().getGraphCanvas() !=null;
	}

	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		toolbarButtonNew.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
		toolbarButtonSave.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
		toolbarButtonOpen.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
	}
}