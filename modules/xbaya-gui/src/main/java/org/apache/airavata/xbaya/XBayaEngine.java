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
package org.apache.airavata.xbaya;

import java.util.List;

import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.amazon.AmazonComponentRegistry;
import org.apache.airavata.workflow.model.component.local.LocalComponentRegistry;
import org.apache.airavata.workflow.model.component.system.SystemComponentRegistry;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.component.registry.ComponentController;
import org.apache.airavata.xbaya.messaging.Monitor;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.monitor.MonitorStarter;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelector;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class XBayaEngine {

    private static final Logger logger = LoggerFactory.getLogger(XBayaEngine.class);

    private XBayaConfiguration configuration;

    private XBayaGUI gui;

    private Monitor monitor;

    private boolean exitOnClose = true;

    private ComponentTreeNode systemComponentTree;

    private SystemComponentRegistry componentRegistry;

    private ComponentSelector componentTreeViewer; 

    /**
     * Constructs a ApplicationClient.
     *
     * @param configuration
     */
    public XBayaEngine(XBayaConfiguration configuration) {
        this.configuration = configuration;
        // initiate monitor to monitor the events
        this.monitor = new Monitor();

        // Set up the GUI.
        XBayaEngine.this.gui = new XBayaGUI(XBayaEngine.this);

        // Arguments errors.
        for (Throwable e : this.configuration.getErrors()) {
            getGUI().getErrorWindow().error(e.getMessage(), e);
        }

        // Initialization after the GUI setup.
        initAfterGUI();
    }

    /**
     * Returns the configuration.
     *
     * @return The configuration
     */
    public XBayaConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the XwfGui. The XwfGui is either XwfAppletGui in case of the applet, or XwfApplicationGui in case of the
     * application.
     *
     * @return the XwfGui
     */
    public XBayaGUI getGUI() {
        return this.gui;
    }

    /**
     * Returns the monitor.
     *
     * @return The monitor
     */
    public Monitor getMonitor() {
        return this.monitor;
    }

    /**
     * Disposes on exit.
     *
     * @throws WorkflowException
     */
    public void dispose() throws WorkflowException {
        this.monitor.stop();
    }

    /**
     * Initialization process. This method is called after the GUI is initialized.
     */
    private void initAfterGUI() {

        initRegistry();

        initMonitor();

    }

    private void addApplicationRegistry(){

    }
    /**
     * Initializes registris.
     */
    private void initRegistry() {

        componentTreeViewer = this.gui.getComponentSelector();
        try {
            this.componentRegistry = new SystemComponentRegistry();
            // This does not take time, so we can do it in the same thread.
            this.systemComponentTree = ComponentController.getComponentTree(this.componentRegistry);
            componentTreeViewer.addComponentTree(0, this.systemComponentTree);

            componentTreeViewer.addComponentTree(1, ComponentController.getComponentTree(new AmazonComponentRegistry()));

        } catch (RuntimeException e) {
            // This should not happen
            logger.error(e.getMessage(), e);
            getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (ComponentRegistryException e) {
            logger.error(e.getMessage(), e);
            getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
		}

        List<String> localRegistryPaths = this.configuration.getLocalRegistry();
        for (String path : localRegistryPaths) {
            try {
                LocalComponentRegistry registry = new LocalComponentRegistry(path);
                // XXX This might take time, so it's better to move to another
                // thread.
                ComponentTreeNode componentTree = ComponentController.getComponentTree(registry);
                componentTreeViewer.addComponentTree(componentTree);
            } catch (ComponentRegistryException e) {
                getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
            } catch (RuntimeException e) {
                getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            }
        }
    }

    /**
     * Initializes monitor.
     */
    private void initMonitor() {
        try {
            MonitorStarter starter = new MonitorStarter(this);
            starter.start();
        } catch (RuntimeException e) {
            getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
        } catch (Error e) {
            getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }
    
    public void reloadRegistry(){
    	componentTreeViewer.refresh();
    }
}