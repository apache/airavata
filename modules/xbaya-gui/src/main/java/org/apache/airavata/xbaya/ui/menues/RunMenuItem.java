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

package org.apache.airavata.xbaya.ui.menues;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.event.Event.Type;
import org.apache.airavata.xbaya.event.EventListener;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.ui.dialogs.GridChemRunnerWindow;
import org.apache.airavata.xbaya.ui.dialogs.JythonRunnerWindow;
import org.apache.airavata.xbaya.ui.dialogs.graph.dynamic.DynamicWorkflowRunnerWindow;
import org.apache.airavata.xbaya.ui.dialogs.monitor.MonitorConfigurationWindow;
import org.apache.airavata.xbaya.ui.experiment.WorkflowInterpreterLaunchWindow;
import org.apache.airavata.xbaya.ui.monitor.MonitorStarter;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.ToolbarButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunMenuItem  implements EventListener, XBayaExecutionModeListener{

    private XBayaEngine engine;

    private JMenu runMenu;
    
    private JMenuItem launchDynamicWorkflowItem;

    private JMenuItem launchGridChemWorkflowItem;

    private JMenuItem launchXBayaInterpreterItem;

    private JMenuItem launchAndSaveInGridChemWorkflowItem;

    private JMenuItem configMonitorItem;

    private JMenuItem resumeMonitoringItem;

    private JMenuItem pauseMonitoringItem;

    private JMenuItem resetMonitoringItem;

    private static final Logger logger = LoggerFactory.getLogger(RunMenuItem.class);

	private static final String EXECUTE_ACTIONS = "run_actions";
	private static final String EXECUTE_MONITOR_ACTIONS = "monitor_actions";

    private XBayaToolBar toolBar;

	private ToolbarButton toolbarButtonRunWorkflow;

	private ToolbarButton toolbarButtonPauseMonitor;

	private ToolbarButton toolbarButtonResumeMonitor;

	private JMenuItem stopWorkflowItem;

	private ToolbarButton toolbarButtonStopWorkflow;
    
    /**
     * Constructs a WorkflowMenu.
     * 
     * @param engine
     */
    public RunMenuItem(XBayaEngine engine, XBayaToolBar toolBar) {
        this.engine = engine;
        setToolBar(toolBar);
        createWorkflowMenu();
        Monitor monitor = this.engine.getMonitor();
        monitor.addEventListener(this);
        monitor.getConfiguration().addEventListener(this);
        engine.getConfiguration().registerExecutionModeChangeListener(this);
        XBayaToolBar.setGroupOrder(EXECUTE_ACTIONS, 5);
        XBayaToolBar.setGroupOrder(EXECUTE_MONITOR_ACTIONS, 6);
    }

    /**
     * @return The workflow menu.
     */
    public JMenu getMenu() {
        return this.runMenu;
    }

    /**
     * Creates workflow menu.
     */
    private void createWorkflowMenu() {
        this.launchDynamicWorkflowItem = createLaunchDynamicWorkflowItem();
        this.launchGridChemWorkflowItem = createLaunchGridChemWorkflowItem();
        createLaunchXBayaInterpreterItem();
        createLaunchAndSaveGridChemWorkflowItem();
        this.configMonitorItem = createConfigMonitoring();
        this.resumeMonitoringItem = createResumeMonitoring();
        this.pauseMonitoringItem = createPauseMonitoring();
        this.resetMonitoringItem = createResetMonitoring();
        stopWorkflowItem = createStopWorkflow();
        
        runMenu = new JMenu("Run");
        runMenu.setMnemonic(KeyEvent.VK_R);

        runMenu.add(launchDynamicWorkflowItem);
        runMenu.add(launchXBayaInterpreterItem);
        
        runMenu.addSeparator();
        runMenu.add(stopWorkflowItem);
        
        runMenu.addSeparator();
        runMenu.add(launchGridChemWorkflowItem);
        runMenu.add(launchAndSaveInGridChemWorkflowItem);

        runMenu.addSeparator();
        
        runMenu.add(this.resumeMonitoringItem);
        runMenu.add(this.pauseMonitoringItem);
        runMenu.add(this.resetMonitoringItem);
        runMenu.add(this.configMonitorItem);
        
        runMenu.addMenuListener(new MenuListener(){
			@Override
			public void menuCanceled(MenuEvent e) {}
			@Override
			public void menuDeselected(MenuEvent e) {}
			@Override
			public void menuSelected(MenuEvent e) {
				stopWorkflowItem.setEnabled(isWorkflowRunning());
			}
        });
        
        setupMonitors();
        startStopButtonStateUpdater();
        executionModeChanged(engine.getConfiguration());
    }

	private void setupMonitors() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	while(engine.getGUI()==null){
            		Thread.yield();
            	}
                engine.getGUI().addWorkflowTabChangeListener(new ChangeListener(){
					@Override
					public void stateChanged(ChangeEvent event) {
						boolean runShouldBeActive = isRunShouldBeActive();
						toolbarButtonRunWorkflow.setEnabled(runShouldBeActive);	
						launchDynamicWorkflowItem.setEnabled(runShouldBeActive);
						launchXBayaInterpreterItem.setEnabled(runShouldBeActive);
                        launchGridChemWorkflowItem.setEnabled(false);
                        launchAndSaveInGridChemWorkflowItem.setEnabled(false);
					}
                });
            }
        });
	}
    
    private JMenuItem createConfigMonitoring() {
        JMenuItem item = new JMenuItem("Configure Monitoring...");
        item.setMnemonic(KeyEvent.VK_C);
        item.addActionListener(new AbstractAction() {
            private MonitorConfigurationWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new MonitorConfigurationWindow(engine);
                }
                this.window.show();
            }
        });
        return item;
    }

    private JMenuItem createResumeMonitoring() {
        JMenuItem item = new JMenuItem("Resume monitoring",MenuIcons.MONITOR_RESUME_ICON);
        item.setMnemonic(KeyEvent.VK_S);
        AbstractAction action = new AbstractAction() {
            private MonitorStarter starter;

            public void actionPerformed(ActionEvent event) {
                if (this.starter == null) {
                    this.starter = new MonitorStarter(engine);
                }
                this.starter.start();
            }
        };
		item.addActionListener(action);
        boolean valid = this.engine.getMonitor().getConfiguration().isValid();
        item.setVisible(valid);
        toolbarButtonResumeMonitor = getToolBar().addToolbarButton(EXECUTE_MONITOR_ACTIONS,item.getText(), MenuIcons.MONITOR_RESUME_ICON, "Resume monitoring", action,4);
        toolbarButtonResumeMonitor.setEnabled(false);
        return item;
    }

    private JMenuItem createPauseMonitoring() {
        JMenuItem item = new JMenuItem("Pause monitoring", MenuIcons.MONITOR_PAUSE_ICON);
        item.setMnemonic(KeyEvent.VK_T);
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    engine.getMonitor().asynchronousStop();
                } catch (RuntimeException e) {
                    engine.getGUI().getErrorWindow().error(ErrorMessages.MONITOR_ERROR, e);
                } catch (Error e) {
                    engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        };
		item.addActionListener(action);
        item.setVisible(false);
        toolbarButtonPauseMonitor = getToolBar().addToolbarButton(EXECUTE_MONITOR_ACTIONS,item.getText(), MenuIcons.MONITOR_PAUSE_ICON, "Pause monitoring", action,3);
        toolbarButtonPauseMonitor.setEnabled(false);
        return item;
    }

    private JMenuItem createResetMonitoring() {
        JMenuItem item = new JMenuItem("Reset monitoring");
        item.setMnemonic(KeyEvent.VK_R);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                engine.getMonitor().reset();
            }
        });
        item.setVisible(false);
        return item;
    }

    private JMenuItem createStopWorkflow() {
        JMenuItem item = new JMenuItem("Stop running workflow",MenuIcons.STOP_ICON);
        item.setMnemonic(KeyEvent.VK_S);
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                cleanup();
            }
        };
		item.addActionListener(action);
        item.setEnabled(false);
        toolbarButtonStopWorkflow = getToolBar().addToolbarButton(EXECUTE_ACTIONS,item.getText(), MenuIcons.STOP_ICON, "Stop workflow", action,2);
        toolbarButtonStopWorkflow.setEnabled(item.isEnabled());
        return item;
    }
    
    private JMenuItem createRunJythonWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Run workflow as Jython...");
        menuItem.setMnemonic(KeyEvent.VK_J);
        menuItem.addActionListener(new AbstractAction() {
            private JythonRunnerWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new JythonRunnerWindow(engine);
                }
                this.window.show();
            }
        });
        menuItem.setEnabled(false);
        return menuItem;
    }

    private JMenuItem createLaunchDynamicWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Run workflow...", MenuIcons.RUN_ICON);
        menuItem.setMnemonic(KeyEvent.VK_D);
        AbstractAction action = new AbstractAction() {
            private DynamicWorkflowRunnerWindow window;

            public void actionPerformed(ActionEvent event) {
            	if (isWorkflowRunning()){
            		if (JOptionPane.showConfirmDialog(null, "A previous workflow excution data needs to be cleared before launching another workflow. Do you wish to continue?", "Run Dynamic Workflow", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            			cleanup();
                    }else{
            			return;
            		}
            	}
                if (this.window == null) {
                    int count = 0;
                    //there is a possibility the ealier run is not yet cleanedup yet.. so wait until it finishes
                    // and sets the execution state to NONE as the last task of scheduleDynamically
                    while(engine.getGUI().getWorkflow().getExecutionState() != WorkflowExecutionState.NONE){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        count++;
                        if(count > 20){
                            throw new WorkflowRuntimeException("Error stopping previous workflow Execution");
                        }
                    }
                    
                }
                this.window = new DynamicWorkflowRunnerWindow(engine);
                this.window.show();
            }
        };
		menuItem.addActionListener(action);
		menuItem.setEnabled(false);
        return menuItem;
    }
    
    private boolean isRunShouldBeActive() {
		return engine.getGUI().getGraphCanvas() !=null;
	}
    
    private JMenuItem createLaunchGridChemWorkflowItem() {
        JMenuItem menuItem = new JMenuItem("Run as GridChem Workflow...");
        menuItem.addActionListener(new AbstractAction() {
            private GridChemRunnerWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new GridChemRunnerWindow(engine);
                }
                this.window.show();
            }
        });
        menuItem.setEnabled(false);
        return menuItem;
    }
    
    private void createLaunchXBayaInterpreterItem() {
        this.launchXBayaInterpreterItem = new JMenuItem("Run on Interpreter Server...");
        AbstractAction action = new AbstractAction() {
            private WorkflowInterpreterLaunchWindow window;
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new WorkflowInterpreterLaunchWindow(engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    engine.getGUI().getErrorWindow().error(e1);
                }
            }
        };
        launchXBayaInterpreterItem.addActionListener(action);
        toolbarButtonRunWorkflow = getToolBar().addToolbarButton(EXECUTE_ACTIONS, launchXBayaInterpreterItem.getText(), MenuIcons.RUN_ICON, "Run workflow", action,1);
        toolbarButtonRunWorkflow.setEnabled(launchXBayaInterpreterItem.isEnabled());
        launchXBayaInterpreterItem.setEnabled(false);
    }

    /**
	 * 
	 */
    private void createLaunchAndSaveGridChemWorkflowItem() {
        launchAndSaveInGridChemWorkflowItem = new JMenuItem("Run & Register in GridChem...	");
        // TODO Add the following operations
        // First Call OGCE-GridChem-Bridge Service to register an experiment
        // Set lead context header with all the required notifier context
        // call launch workflow
        launchAndSaveInGridChemWorkflowItem.setEnabled(false);
    }
    
    /**
     * @see org.apache.airavata.xbaya.event.EventListener#eventReceived(org.apache.airavata.xbaya.event.Event)
     */
    public void eventReceived(Event event) {
        Type type = event.getType();
        if (type.equals(Type.MONITOR_CONFIGURATION_CHANGED)) {
            MonitorConfiguration configuration = this.engine.getMonitor().getConfiguration();
            boolean valid = configuration.isValid();
            resumeMonitoringItem.setVisible(valid);
            pauseMonitoringItem.setVisible(false);
            resetMonitoringItem.setVisible(false);
        } else if (type.equals(Type.MONITOR_STARTED)) {
            resumeMonitoringItem.setVisible(false);
            pauseMonitoringItem.setVisible(true);
            resetMonitoringItem.setVisible(true);
        } else if (type.equals(Type.MONITOR_STOPED)) {
            resumeMonitoringItem.setVisible(true);
            pauseMonitoringItem.setVisible(false);
            resetMonitoringItem.setVisible(false);
        }
        toolbarButtonPauseMonitor.setEnabled(pauseMonitoringItem.isVisible());
        toolbarButtonResumeMonitor.setEnabled(resumeMonitoringItem.isVisible());
    }

	public XBayaToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(XBayaToolBar toolBar) {
		this.toolBar = toolBar;
	}
	
	private void cleanup() {
		try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            engine.getWorkflowInterpreter().cleanup();
		} catch (MonitorException e) {
			this.engine.getGUI().getErrorWindow().error(e);
		}
	}

	private boolean isWorkflowRunning() {
		return engine.getWorkflowInterpreter()!=null;
	}

	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		toolbarButtonRunWorkflow.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
		toolbarButtonStopWorkflow.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
	}
	
	private void startStopButtonStateUpdater() {
		Thread t=new Thread(){
			@Override
			public void run() {
				while(true){
					toolbarButtonStopWorkflow.setEnabled(isWorkflowRunning());
//					if (!isWorkflowRunning()){
//						pauseMonitorButton.setEnabled(false);
//						resumeMonitorButton.setEnabled(false);
//					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		t.start();
	}
}