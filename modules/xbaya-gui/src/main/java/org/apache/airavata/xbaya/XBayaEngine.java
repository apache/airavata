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

package org.apache.airavata.xbaya;

import java.net.URI;
import java.util.List;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataManager;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.amazon.AmazonComponentRegistry;
import org.apache.airavata.workflow.model.component.local.LocalComponentRegistry;
import org.apache.airavata.workflow.model.component.system.SystemComponentRegistry;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.component.registry.ComponentController;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.monitor.MonitorStarter;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelector;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentTreeNode;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class XBayaEngine {

    private static final Logger logger = LoggerFactory.getLogger(XBayaEngine.class);

    private XBayaConfiguration configuration;

    private XBayaGUI gui;

    private WorkflowClient workflowClient;

    private Monitor monitor;

    private boolean exitOnClose = true;

    private ComponentTreeNode systemComponentTree;

    private SystemComponentRegistry componentRegistry;

    private WorkflowInterpreter workflowInterpreter;

    private AiravataAPI airavataAPI;

    /**
     * Constructs a ApplicationClient.
     *
     * @param configuration
     */
    public XBayaEngine(XBayaConfiguration configuration) {
        this.configuration = configuration;

        // Creates some essential objects.

        MonitorConfiguration monitorConfiguration = new MonitorConfiguration(configuration.getBrokerURL(),
                configuration.getTopic(), configuration.isPullMode(), configuration.getMessageBoxURL());
        this.monitor = new Monitor(monitorConfiguration);

        // MyProxy
        // this.myProxyClient = new MyProxyClient(this.configuration.getMyProxyServer(),
        // this.configuration.getMyProxyPort(), this.configuration.getMyProxyUsername(),
        // this.configuration.getMyProxyPassphrase(), this.configuration.getMyProxyLifetime());
        //
        // // These have to be before the GUI setup.
        // this.workflowClient = WorkflowEngineManager.getWorkflowClient();
        // this.workflowClient.setXBayaEngine(this);


        // Set up the GUI.
        updateXBayaConfigurationServiceURLs();
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
     * Returns the Workflow Client.
     *
     * @return the Workflow Client
     */
    public WorkflowClient getWorkflowClient() {
        return this.workflowClient;
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

    /**
     * Initializes registris.
     */
    private void initRegistry() {

        final ComponentSelector componentTreeViewer = this.gui.getComponentSelector();
        try {
            this.componentRegistry = new SystemComponentRegistry();
            // This does not take time, so we can do it in the same thread.
            this.systemComponentTree = ComponentController.getComponentTree(this.componentRegistry);
            componentTreeViewer.addComponentTree(0, this.systemComponentTree);

            componentTreeViewer.addComponentTree(1, ComponentController.getComponentTree(new AmazonComponentRegistry()));

        } catch (RuntimeException e) {
            // This should not happen
            e.printStackTrace();
            getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (ComponentRegistryException e) {
        	e.printStackTrace();
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
            if (this.configuration.isStartMonitor()) {
                MonitorStarter starter = new MonitorStarter(this);
                starter.start();
            }
        } catch (RuntimeException e) {
            getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
        } catch (Error e) {
            getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }

    public void resetWorkflowInterpreter() {
		this.workflowInterpreter = null;
	}
    
    
	public WorkflowInterpreter getWorkflowInterpreter() {
		return workflowInterpreter;
	}

	public void registerWorkflowInterpreter(WorkflowInterpreter workflowInterpreter) {
		if (getWorkflowInterpreter()!=null){
			throw new WorkflowRuntimeException("Critical Error!!! Workflow interpretter already running. Cleanup first");
		}
		this.workflowInterpreter = workflowInterpreter;
	}

	
	public void updateXBayaConfigurationServiceURLs() {
		try {
			if (this.getConfiguration().getAiravataAPI()!=null && this.getConfiguration().getAiravataAPI()!=null){
                AiravataAPI airavataAPI = getConfiguration().getAiravataAPI();
                AiravataManager airavataManager = airavataAPI.getAiravataManager();
//                AiravataRegistry2 registry=this.getConfiguration().getJcrComponentRegistry().getRegistry();
	        	URI eventingServiceURL = airavataManager.getEventingServiceURL();
				if (eventingServiceURL!=null) {
					this.getConfiguration().setBrokerURL(eventingServiceURL);
					this.getMonitor()
							.getConfiguration()
							.setBrokerURL(eventingServiceURL);
				}
				URI messageBoxServiceURL = airavataManager.getMessageBoxServiceURL();
				if (messageBoxServiceURL!=null) {
					this.getConfiguration()
					.setMessageBoxURL(messageBoxServiceURL);
					this.getMonitor()
							.getConfiguration()
							.setMessageBoxURL(messageBoxServiceURL);
				}
				List<URI> interpreterServiceURLList = airavataManager.getWorkflowInterpreterServiceURLs();
				if (interpreterServiceURLList.size()>0) {
					this.getConfiguration()
							.setWorkflowInterpreterURL(interpreterServiceURLList.get(0));
				}
				List<URI> gfacURLList = airavataManager.getGFaCURLs();
				if (gfacURLList.size()>0) {
					this.getConfiguration().setGFacURL(gfacURLList.get(0));
				}
			}
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
}