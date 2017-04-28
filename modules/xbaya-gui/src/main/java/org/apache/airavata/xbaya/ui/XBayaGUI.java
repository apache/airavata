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
package org.apache.airavata.xbaya.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.core.generators.WorkflowFiler;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.messaging.MonitorException;
import org.apache.airavata.xbaya.messaging.event.Event;
import org.apache.airavata.xbaya.messaging.event.EventListener;
import org.apache.airavata.xbaya.ui.dialogs.ErrorWindow;
import org.apache.airavata.xbaya.ui.dialogs.registry.RegistryWindow;
import org.apache.airavata.xbaya.ui.dialogs.workflow.WorkflowPropertyWindow;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent.GraphCanvasEventType;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasListener;
import org.apache.airavata.xbaya.ui.menues.XBayaMenu;
import org.apache.airavata.xbaya.ui.views.ComponentViewer;
import org.apache.airavata.xbaya.ui.views.MonitorPanel;
import org.apache.airavata.xbaya.ui.views.PortViewer;
import org.apache.airavata.xbaya.ui.widgets.ScrollPanel;
import org.apache.airavata.xbaya.ui.widgets.TabLabelButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaToolBar;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaGUI implements EventListener, XBayaExecutionModeListener {

    private static final Logger logger = LoggerFactory.getLogger(XBayaGUI.class);

    private static final int STATIC_MENU_ITEMS = 4;

    private XBayaEngine engine;

    private JFrame frame;

    private XBayaMenu menu;

    private List<GraphCanvas> graphCanvases = new LinkedList<GraphCanvas>();

    private PortViewer portViewer;

    private ComponentViewer componentViewer;

    private ComponentSelector componentSelector;

    private MonitorPanel monitorPane;

    private XBayaToolBar toolbar;

    private ErrorWindow errorWindow;

    private JTabbedPane rightBottomTabbedPane;

    private JTabbedPane graphTabbedPane;

    private boolean graphPanelMaximized;

    private int previousMainDividerLocation;

    private int previousRightDividerLocation;

    private JSplitPane mainSplitPane;

    private JSplitPane leftSplitPane;

    private JSplitPane rightSplitPane;

    private JTabbedPane componentTabbedPane;

    private ScrollPanel compTreeXBayapanel;

	private WorkflowFiler graphFiler;

    private WorkflowPropertyWindow workflowPropertiesWindow;

    /**
     * Constructs an XBayaEngine.
     * 
     * @param engine
     */
    public XBayaGUI(XBayaEngine engine) {
        this.engine = engine;
        this.engine.getMonitor().addEventListener(this);
        graphFiler = new WorkflowFiler(engine);
        engine.getConfiguration().registerExecutionModeChangeListener(this);

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    init();
                }
            });
        } catch (InterruptedException e) {
            // Shouldn't happen.
            throw new WorkflowRuntimeException(e);
        } catch (InvocationTargetException e) {
            // Shouldn't happen.
        	//It happened
        	/* exception occurs when xbaya is opened twice from the jvm
        	 * org.apache.airavata.xbaya.XBayaRuntimeException: java.lang.reflect.InvocationTargetException
				at org.apache.airavata.xbaya.gui.XBayaGUI.<init>(XBayaGUI.java:148)
				at org.apache.airavata.xbaya.XBayaEngine.<init>(XBayaEngine.java:106)
				at org.apache.airavata.xbaya.XBaya.<init>(XBaya.java:51)
				at org.ogce.paramchem.XBayaLauncher.run(XBayaLauncher.java:44)
				at java.lang.Thread.run(Thread.java:662)
			Caused by: java.lang.reflect.InvocationTargetException
				at java.awt.EventQueue.invokeAndWait(EventQueue.java:1042)
				at javax.swing.SwingUtilities.invokeAndWait(SwingUtilities.java:1326)
				at org.apache.airavata.xbaya.gui.XBayaGUI.<init>(XBayaGUI.java:138)
				... 4 more
        	 */
            throw new WorkflowRuntimeException(e);
        }
        
        // Following suppsed to jump in the middle to save unsaved workflows when exiting xbaya
        // but its not working because the UI is already disposed it seems :(
//        Runtime.getRuntime().addShutdownHook(new Thread(){
//        	@Override
//        	public void run() {
//        		while (getGraphCanvases().size()>0){
//        			removeGraphCanvasFromIndex(0);
//        		}
//        	}
//        });
    }

    /**
     * Returns the notificationPane.
     * 
     * @return The notificationPane
     */
    public MonitorPanel getMonitorPane() {
        return this.monitorPane;
    }

    /**
     * Returns the ComponentTreeViewer.
     * 
     * @return The ComponentTreeViewer
     */
    public ComponentSelector getComponentSelector() {
        return this.componentSelector;
    }

    /**
     * Returns the ErrorWindow.
     * 
     * @return the errorWindow
     */
    public ErrorWindow getErrorWindow() {
        return this.errorWindow;
    }

    /**
     * Returns the Frame.
     * 
     * @return the Frame
     */
    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * @return The list of GraphCanvases.
     */
    public List<GraphCanvas> getGraphCanvases() {
        return this.graphCanvases;
    }

    /**
     * Return the active GraphPanel.
     * 
     * @return The GraphPanel
     */
    public GraphCanvas getGraphCanvas() {
        int index = this.graphTabbedPane.getSelectedIndex();
        if (index!=-1) {
			return this.graphCanvases.get(index);
		}else{
			return null;
		}
    }

    /**
     * Returns the toolbar.
     * 
     * @return The toolbar
     */
    public XBayaToolBar getToolbar() {
    	if (toolbar==null){
    		this.toolbar = new XBayaToolBar(this.engine);
    	}
        return this.toolbar;
    }

    public GraphCanvas newGraphCanvas(boolean focus) {
    	return newGraphCanvas(focus, false);
    }

    /**
     * Creates a new graph tab.
     *
     * This method needs to be called by Swing event thread.
     *
     * @param focus
     *
     * @return The graph canvas created
     */
    public GraphCanvas newGraphCanvas(boolean focus, boolean newFreshWorkflow) {
        if (newFreshWorkflow) {
            getWorkflowPropertyWindow().show();
            return null;
        } else {
            GraphCanvas graphCanvas = getNewGraphCanvas(null, null);
            if (focus) {
                setFocus(graphCanvas);
            }
            return graphCanvas;
        }
    }

    public GraphCanvas getNewGraphCanvas(String wfName, String wfDescription) {
        GraphCanvas newGraphCanvas = new GraphCanvas(this.engine, wfName);
        newGraphCanvas.setDescription(wfDescription);
        this.graphCanvases.add(newGraphCanvas);
        this.graphTabbedPane.addTab(newGraphCanvas.getWorkflow().getName(), newGraphCanvas.getSwingComponent());
        final int index = graphTabbedPane.getTabCount() - 1;
        TabLabelButton tabLabelButton = new TabLabelButton(graphTabbedPane, "Close this workflow");
        graphTabbedPane.setTabComponentAt(index, tabLabelButton);
        tabLabelButton.setCloseButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeGraphCanvasFromIndex(index);
            }
        });
        graphTabbedPane.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent event) {
            }

            @Override
            public void componentRemoved(ContainerEvent event) {
                List<GraphCanvas> graphCanvases = engine.getGUI().getGraphCanvases();
                for (GraphCanvas graphCanvas : graphCanvases) {
                    if (graphCanvas.getSwingComponent() == event.getComponent()) {
                        if (graphCanvas.isWorkflowChanged()) {
                            setFocus(graphCanvas);
                            if (JOptionPane.showConfirmDialog(null, "The workflow '" + graphCanvas.getWorkflow().getName() + "' has been modified. Save changes?", "Save Workflow", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                graphFiler.saveWorkflow(graphCanvas);
                            }
                        }
                        break;
                    }
                }
            }

        });

        newGraphCanvas.addGraphCanvasListener(this.componentViewer);
        newGraphCanvas.addGraphCanvasListener(this.portViewer);
        newGraphCanvas.addGraphCanvasListener(new GraphCanvasListener() {

            public void graphCanvasChanged(GraphCanvasEvent event) {
                GraphCanvasEventType type = event.getType();
                final GraphCanvas graphCanvas = event.getGraphCanvas();
                final Workflow workflow = event.getWorkflow();
                switch (type) {
                    case GRAPH_LOADED:
                    case NAME_CHANGED:
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String name = workflow.getName();

                                // Change the name of the tab.
                                updateTabTitle(graphCanvas, workflow);

                                // Change the name of the frame.
                                setFrameName(name);
                            }
                        });
                        break;
                    case NODE_SELECTED:
                    case INPUT_PORT_SELECTED:
                    case OUTPUT_PORT_SELECTED:
                        // Do nothing
                    case WORKFLOW_CHANGED:
                        updateTabTitle(graphCanvas, graphCanvas.getWorkflow());
                        setFrameName(workflow.getName());
                        for (ChangeListener listener : tabChangeListeners) {
                            try {
                                listener.stateChanged(null);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                }
            }

            private void updateTabTitle(
                    final GraphCanvas graphCanvas,
                    final Workflow workflow) {
                int index = XBayaGUI.this.graphTabbedPane.indexOfComponent(graphCanvas.getSwingComponent());
                String newTitle = workflow.getName();
                if (graphCanvas.isWorkflowChanged()) {
                    newTitle = "*" + newTitle;
                }
                XBayaGUI.this.graphTabbedPane.setTitleAt(index, newTitle);
            }
        });
        return newGraphCanvas;
    }


    /**
     * @param graphCanvas
     */
    public void setFocus(GraphCanvas graphCanvas) {
        this.graphTabbedPane.setSelectedComponent(graphCanvas.getSwingComponent());
    }

    /**
     * Selects a canvas with a specified workflow if any; otherwise create one.
     * 
     * This method needs to be called by Swing event thread.
     * 
     * @param workflow
     */
    public void selectOrCreateGraphCanvas(Workflow workflow) {
        GraphCanvas graphCanvas = null;
        for (GraphCanvas canvas : this.graphCanvases) {
            if (workflow == canvas.getWorkflow()) {
                graphCanvas = canvas;
            }
        }
        if (graphCanvas == null) {
            graphCanvas = newGraphCanvas(true);
            graphCanvas.setWorkflow(workflow);
        } else {
            setFocus(graphCanvas);
        }
    }
    
    private List<ChangeListener> tabChangeListeners=new ArrayList<ChangeListener>();

//	private JCRBrowserPanel jcrBrowserPanel;
    
    public void addWorkflowTabChangeListener(ChangeListener listener){
		graphTabbedPane.addChangeListener(listener);
		tabChangeListeners.add(listener);
    }
    
    public void removeWorkflowTabChangeListener(ChangeListener listener){
		graphTabbedPane.removeChangeListener(listener);
		tabChangeListeners.remove(listener);
    }
    /**
     * Closes the selected graph canvas.
     * 
     * This method needs to be called by Swing event thread.
     */
    public void closeGraphCanvas() {
        removeGraphCanvasFromIndex(this.graphTabbedPane.getSelectedIndex());
    	//I dont know why but aparently you have to have atleast one tab present
//    	newGraphCanvas(true);
    }

    public boolean closeAllGraphCanvas(){
    	while (graphTabbedPane.getTabCount()>0){
    		if (!removeGraphCanvasFromIndex(0)){
    			return false;
    		}
    	}
		return true;
    	//I dont know why but aparently you have to have atleast one tab present
//    	newGraphCanvas(true);
    }
    
	private boolean removeGraphCanvasFromIndex(int index) {
		boolean actionSuccess=true;
		if ((graphTabbedPane.getTabCount()>0) && (index<this.graphTabbedPane.getTabCount())){
			GraphCanvas graphCanvas = graphCanvases.get(index);
			if (graphCanvas.isWorkflowChanged()){
				int result = JOptionPane.showConfirmDialog(frame, "'"+graphCanvas.getWorkflow().getName()+"' has been modified. Save changes?", "Save Workflow", JOptionPane.YES_NO_CANCEL_OPTION);
				try {
					if (result==JOptionPane.YES_OPTION){
						graphFiler.saveWorkflow(graphCanvas);
						if (graphCanvas.isWorkflowChanged()){
							//if cancelled while trying to save
							actionSuccess=false;
						}
					}else if (result==JOptionPane.CANCEL_OPTION){
						actionSuccess=false;
					}
						
				} catch (Exception e) {
                    logger.error(e.getMessage(), e);
				}
			}
			if (actionSuccess) {
				graphCanvases.remove(index);
				graphTabbedPane.removeTabAt(index);
				activeTabChanged();
			}
		}
		return actionSuccess;
	}
    
    /**
     * Selects the next graph canvas.
     * 
     * This method needs to be called by Swing event thread.
     */
    public void selectNextGraphCanvas() {
        int count = this.graphTabbedPane.getTabCount();
        int index = this.graphTabbedPane.getSelectedIndex();
        index = (index + 1) % count;
        this.graphTabbedPane.setSelectedIndex(index);
    }

    /**
     * Toggles the maximization of the Graph Panel.
     */
    public void toggleMaximizeGraphPanel() {
        if (XBayaGUI.this.graphPanelMaximized) {
            unmaximizeGraphPanel();
        } else {
            maximizeGraphPanel();
        }
    }

    /**
     * Maximizes the Graph Panel.
     */
    public void maximizeGraphPanel() {
        if (!XBayaGUI.this.graphPanelMaximized) {
            XBayaGUI.this.graphPanelMaximized = true;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    XBayaGUI.this.previousMainDividerLocation = XBayaGUI.this.mainSplitPane.getDividerLocation();
                    XBayaGUI.this.previousRightDividerLocation = XBayaGUI.this.rightSplitPane.getDividerLocation();
                    XBayaGUI.this.mainSplitPane.setDividerLocation(0.0);
                    XBayaGUI.this.rightSplitPane.setDividerLocation(1.0);
                }
            });
        }
    }

    /**
     * Set the size of the graph panel to the original.
     */
    public void unmaximizeGraphPanel() {
        if (XBayaGUI.this.graphPanelMaximized) {
            XBayaGUI.this.graphPanelMaximized = false;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    XBayaGUI.this.mainSplitPane.setDividerLocation(XBayaGUI.this.previousMainDividerLocation);
                    XBayaGUI.this.rightSplitPane.setDividerLocation(XBayaGUI.this.previousRightDividerLocation);
                }
            });
        }
    }

    /**
     * Adds a selected component as a node at random position.
     */
    public void addNode() {
        getGraphCanvas().addNode(this.componentSelector.getSelectedComponent());
    }

    /**
     * @see EventListener#eventReceived(Event)
     */
    @Override
    public void eventReceived(Event event) {
        Event.Type type = event.getType();
        if (type == Event.Type.MONITOR_STARTED || type == Event.Type.KARMA_STARTED) {
            // Show the monitor panel.
            this.rightBottomTabbedPane.setSelectedComponent(this.monitorPane.getSwingComponent());
        }
    }

    /**
     * Initializes
     */
    private void init() {
        createFrame();

        this.menu = new XBayaMenu(this.engine, getToolbar());
        this.frame.setJMenuBar(this.menu.getSwingComponent());

        initPane();

        // Create an empty graph canvas.
//        newGraphCanvas(true);

        this.frame.setVisible(true);
    	loadDefaultGraph();

        executionModeChanged(this.engine.getConfiguration());
    }

    /**
     * Initializes the GUI.
     */
    private void initPane() {
        Container contentPane = this.frame.getContentPane();

        // Error window
        this.errorWindow = new ErrorWindow(contentPane);

        contentPane.add(getToolbar().getSwingComponent(), BorderLayout.PAGE_START);

        this.portViewer = new PortViewer();
        this.componentViewer = new ComponentViewer();
        this.componentSelector = new ComponentSelector(this.engine);
        this.componentSelector.addComponentSelectorListener(this.componentViewer);
        this.monitorPane = new MonitorPanel(this,this.engine.getMonitor());

        compTreeXBayapanel = new ScrollPanel(this.componentSelector, ComponentSelector.TITLE);
        ScrollPanel compViewXBayaPanel = new ScrollPanel(this.componentViewer, ComponentViewer.TITLE);

        this.rightBottomTabbedPane = new JTabbedPane();
        this.rightBottomTabbedPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.rightBottomTabbedPane.setPreferredSize(new Dimension(0, 200));
        this.rightBottomTabbedPane.addTab(PortViewer.TITLE, this.portViewer.getSwingComponent());
        this.rightBottomTabbedPane.addTab(MonitorPanel.TITLE, this.monitorPane.getSwingComponent());

        this.graphTabbedPane = new JTabbedPane();
        this.graphTabbedPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.graphTabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    toggleMaximizeGraphPanel();
                }
            }
        });
        this.graphTabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                // Called when the active tab changed.
                // Note that this is not called when a tab is removed.
                logger.debug(event.toString());
                XBayaGUI.this.activeTabChanged();
            }
        });

        this.leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        this.rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, this.leftSplitPane, this.rightSplitPane);
        contentPane.add(this.mainSplitPane, BorderLayout.CENTER);

        this.leftSplitPane.setOneTouchExpandable(true);
        this.rightSplitPane.setOneTouchExpandable(true);
        this.mainSplitPane.setOneTouchExpandable(true);

        // this.leftSplitPane.setTopComponent(compTreeXBayapanel.getSwingComponent());
        // this.leftSplitPane.setTopComponent(new JCRBrowserPanel(engine));

        this.componentTabbedPane = new JTabbedPane();
        this.componentTabbedPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.leftSplitPane.setTopComponent(this.componentTabbedPane);
        this.componentTabbedPane.add(this.compTreeXBayapanel.getSwingComponent());
        this.componentTabbedPane.setTitleAt(0, "Component");

        this.leftSplitPane.setBottomComponent(compViewXBayaPanel.getSwingComponent());
        this.rightSplitPane.setTopComponent(this.graphTabbedPane);
        this.rightSplitPane.setBottomComponent(this.rightBottomTabbedPane);

        this.leftSplitPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.rightSplitPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);

        //
        // Adjust sizes
        //

        // Need to pack the frame first to get the size of each component.
        this.frame.pack();

        final int leftPanelWidth = 250;
        final int portViewHight = 200;

        this.mainSplitPane.setDividerLocation(leftPanelWidth);
        this.leftSplitPane.setDividerLocation(0.5);
        this.leftSplitPane.setResizeWeight(0.5);

        this.rightSplitPane.setDividerLocation(this.rightSplitPane.getSize().height - portViewHight);
        // The bottom component to stay the same size
        this.rightSplitPane.setResizeWeight(1.0);

    }

//    public void viewJCRBrowserPanel(){
//    	if (jcrBrowserPanel!=null){
//    		jcrBrowserPanel=componentTabbedPane.indexOfComponent(jcrBrowserPanel)==-1? null:jcrBrowserPanel;
//    	}
//    	if (jcrBrowserPanel==null) {
//			jcrBrowserPanel = new JCRBrowserPanel(engine);
//			this.componentTabbedPane.add(jcrBrowserPanel);
//			int index=this.componentTabbedPane.getTabCount()-1;
//			this.componentTabbedPane.setTitleAt(1, "Airavata Registry");
//			TabLabelButton tabLabelButton = new TabLabelButton(componentTabbedPane, "Close JCR Browser");
//			tabLabelButton.setCloseButtonListener(new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					componentTabbedPane.remove(jcrBrowserPanel);
//				}
//				
//			});
//			this.componentTabbedPane.setTabComponentAt(index, tabLabelButton);
//		}
//		componentTabbedPane.setSelectedComponent(jcrBrowserPanel);
//    }
    
    public void viewComponentTree(){
    	componentTabbedPane.setSelectedComponent(compTreeXBayapanel.getSwingComponent());
    }
    
  
    /**
     * Creates a frame.
     */
    private void createFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // OK. The default will be used.
            logger.error(e.getMessage(), e);
        }

        JFrame.setDefaultLookAndFeelDecorated(false);
        this.frame = new JFrame();

        // Adjust the size
        XBayaConfiguration config = this.engine.getConfiguration();
        int width = config.getWidth();
        int height = config.getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int inset = 50;
        this.frame.setLocation(inset, inset);
        Dimension size = new Dimension(screenSize.width - inset * 2, screenSize.height - inset * 2);
        if (width != 0) {
            size.width = width;
        }
        if (height != 0) {
            size.height = height;
        }

        // This controls the size when you open in a huge screen
        if(size.width > 1280 && size.height > 800){
            size.width = 1280;
            size.height = 800;
        }
        this.frame.setPreferredSize(size);

        this.frame.setTitle(XBayaConstants.APPLICATION_NAME);

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
            	int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit XBaya", JOptionPane.YES_NO_OPTION);
				if (result==JOptionPane.NO_OPTION || (!closeAllGraphCanvas())){
					return;
				}
                logger.debug(event.toString());
                XBayaGUI.this.frame.setVisible(false);
                try {
                    XBayaGUI.this.engine.dispose();
                } catch (WorkflowException e) {
                    // Ignore the error.
                    logger.error(e.getMessage(), e);
                } catch (RuntimeException e) {
                    // Ignore the error.
                    logger.error(e.getMessage(), e);
                }
                if (XBayaGUI.this.engine.getConfiguration().isCloseOnExit()) {
                    System.exit(0);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                logger.debug(e.toString());

                try {
                    XBayaGUI.this.engine.getMonitor().stop();
                } catch (MonitorException e1) {
                    logger.error(e1.getMessage(), e1);
                }
                // Make sure to kill all threads.
                // Dispose only when it can be disposed to prevent infinite loop
                if (XBayaGUI.this.frame.isDisplayable()) {
                    XBayaGUI.this.frame.dispose();
                }
            }
        });
        this.frame.setIconImage(SwingUtil.createImage("airavata-2.png"));
    }

    private void activeTabChanged() {
        GraphCanvas graphPanel = getGraphCanvas();

        if (graphPanel!=null) {
			// Reset the port viewers.
			Port inputPort = graphPanel.getSelectedInputPort();
			Port outputPort = graphPanel.getSelectedOutputPort();
			this.portViewer.setInputPort(inputPort);
			this.portViewer.setOutputPort(outputPort);
			// Reset component viewer.
			Node node = graphPanel.getSelectedNode();
			Component component;
			if (node != null) {
				component = node.getComponent();
			} else {
				component = this.componentSelector.getSelectedComponent();
			}
			this.componentViewer.setComponent(component);
			String name = graphPanel.getWorkflow().getName();
			setFrameName(name);
		}else{
			//TODO what to do when no tabs are present???
		}
    }

    public ComponentViewer getComponentVIewer() {
        return this.componentViewer;
    }

    private void setFrameName(String workflowName) {
        String title = this.engine.getConfiguration().getTitle();
        this.frame.setTitle(workflowName + " - " + title);
    }

	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		this.leftSplitPane.setVisible(config.getXbayaExecutionMode()==XBayaExecutionMode.IDE);
	}


    /**
     * @return
     */
    public WorkflowPropertyWindow getWorkflowPropertyWindow() {
        if (this.workflowPropertiesWindow == null) {
            this.workflowPropertiesWindow = new WorkflowPropertyWindow(this);
        }
        return this.workflowPropertiesWindow;
    }
    
    /**
     * Sets the workflow.
     *
     * @param workflow
     *            The workflow
     */
    public void setWorkflow(Workflow workflow) {
        this.getGraphCanvas().setWorkflow(workflow);
    }

    /**
     * Return the current workflow.
     *
     * @return The current workflow
     */
    public Workflow getWorkflow() {
        return this.getGraphCanvas().getWorkflowWithImage();
    }
    
    private void loadDefaultGraph() {
        if (this.engine.getConfiguration().getWorkflow() != null) {
//            this.newGraphCanvas(true, false);
//            try {
//            	String xml = this.engine.getConfiguration().getAiravataAPI().getWorkflowManager().getWorkflowAsString(this.engine.getConfiguration().getWorkflow());
//                XmlElement xwf = XMLUtil.stringToXmlElement(xml);
//                Workflow workflow = new Workflow(xwf);
//                setWorkflow(workflow);
//            } catch (GraphException e) {
//                getErrorWindow().error(ErrorMessages.WORKFLOW_IS_WRONG, e);
//            } catch (ComponentException e) {
//                getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
//            }
        }
    }

	public XBayaConfiguration getConfiguration() {
		return engine.getConfiguration();
	}
	
	public boolean setupThriftClientData(ThriftServiceType type){
		return setupThriftClientData(type, false); 
	}
	
	public boolean setupThriftClientData(ThriftServiceType type, boolean force){
		if (force || !engine.getConfiguration().isThriftServiceDataExist(type)){
			RegistryWindow window = new RegistryWindow(engine, type);
	        window.show();
		}
		return engine.getConfiguration().isThriftServiceDataExist(type);
	}
}