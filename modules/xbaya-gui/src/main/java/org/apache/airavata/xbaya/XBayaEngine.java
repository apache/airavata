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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.SubWorkflowComponent;
import org.apache.airavata.xbaya.component.gui.ComponentSelector;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.registry.AmazonComponentRegistry;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.registry.LocalComponentRegistry;
import org.apache.airavata.xbaya.component.registry.SystemComponentReference;
import org.apache.airavata.xbaya.component.registry.SystemComponentRegistry;
import org.apache.airavata.xbaya.gpel.component.SubWorkflowUpdater;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.ErrorWindow;
import org.apache.airavata.xbaya.gui.XBayaGUI;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.gui.MonitorStarter;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.streaming.StreamTableModel;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.wf.gui.WorkflowPropertyWindow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaEngine {

    private static final Logger logger = LoggerFactory.getLogger(XBayaEngine.class);

    private XBayaConfiguration configuration;

    private XBayaGUI gui;

    private WorkflowClient workflowClient;

    private SubWorkflowUpdater subWorkflowUpdater;

    private Monitor monitor;

    private MyProxyClient myProxyClient;

    private WorkflowPropertyWindow workflowPropertiesWindow;

    private boolean exitOnClose = true;

    private ComponentTreeNode systemComponentTree;

    private SystemComponentRegistry componentRegistry;

    private static XBayaEngine engine;

    /**
     * Constructs a ApplicationClient.
     * 
     * @param configuration
     */
    public XBayaEngine(XBayaConfiguration configuration) {
        this.configuration = configuration;
        this.engine = this;

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

        this.subWorkflowUpdater = new SubWorkflowUpdater(this);

        // Set up the GUI.
        XBayaEngine.this.gui = new XBayaGUI(XBayaEngine.this);

        // Arguments errors.
        for (Throwable e : this.configuration.getErrors()) {
            getErrorWindow().error(e.getMessage(), e);
        }

        // Initialization after the GUI setup.
        initAfterGUI();
    }

    /**
     * Static get instance method
     * 
     * @return
     */
    public static XBayaEngine getInstance() {
        return engine;
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
     * Returns the subWorkflowUpdater.
     * 
     * @return The subWorkflowUpdater
     */
    public SubWorkflowUpdater getSubWorkflowUpdater() {
        return this.subWorkflowUpdater;
    }

    /**
     * Sets the workflow.
     * 
     * @param workflow
     *            The workflow
     */
    public void setWorkflow(Workflow workflow) {
        this.gui.getGraphCanvas().setWorkflow(workflow);
    }

    /**
     * Return the current workflow.
     * 
     * @return The current workflow
     */
    public Workflow getWorkflow() {
        return this.gui.getGraphCanvas().getWorkflowWithImage();
    }

    /**
     * Returns the ErrorWindow. The ErrorWindow is used to show error messages.
     * 
     * @return the ErrorWindow
     */
    public ErrorWindow getErrorWindow() {
        return this.gui.getErrorWindow();
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
     * @return The MyProxyClient.
     */
    public MyProxyClient getMyProxyClient() {
        return this.myProxyClient;
    }

    /**
     * Disposes on exit.
     * 
     * @throws XBayaException
     */
    public void dispose() throws XBayaException {
        this.monitor.stop();
    }

    /**
     * Initialization process. This method is called after the GUI is initialized.
     */
    private void initAfterGUI() {

        // Initialize security at the beginning.
        initSecurity();

        // load myProxy before loading components from registries.
        // loadMyProxy();

        initRegistry();

        // TODO May be we need to load a default workflow from Xregistry.
        // initGPEL();

        // This has to be after gpel initialization.
        // loadDefaultGraph();

        // This has to be after loading a graph.
        initMonitor();

    }

    private void initSecurity() {
        // Initializes XSUL invokers with SSL without client authentication.
        try {
            XBayaSecurity.init();
        } catch (RuntimeException e) {
            getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }

    // private void loadyProxy() {
    // if (this.configuration.isLoadMyProxy()) {
    // if (this.configuration.getMyProxyUsername() == null) {
    // this.getErrorWindow().error("Trying to load the proxy, but the myproxy usernameis not set.");
    // } else if (this.configuration.getXRegistryURL() == null) {
    // this.getErrorWindow().error(
    // "Trying to load the XRegistry default services, but Xregistry url is not set");
    // } else {
    // MyProxyDialog dialog = new MyProxyDialog(this);
    // dialog.show(true); // blocking
    // }
    //
    // }
    // }

    /**
     * Initializes registris.
     */
    private void initRegistry() {

        final ComponentSelector componentTreeViewer = this.gui.getComponentSelector();
        try {
            this.componentRegistry = new SystemComponentRegistry();
            // This does not take time, so we can do it in the same thread.
            this.systemComponentTree = this.componentRegistry.getComponentTree();
            componentTreeViewer.addComponentTree(0, this.systemComponentTree);

            componentTreeViewer.addComponentTree(1, new AmazonComponentRegistry().getComponentTree());

            // this.wsComponnetRegistry = new WSComponentRegistry();
            // this.wsComponentTree = wsComponnetRegistry.getComponentTree();
            // componentTreeViewer.addComponentTree(wsComponentTree);
        } catch (RuntimeException e) {
            // This should not happen
            e.printStackTrace();
            getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }

        List<String> localRegistryPaths = this.configuration.getLocalRegistry();
        for (String path : localRegistryPaths) {
            try {
                LocalComponentRegistry registry = new LocalComponentRegistry(path);
                // XXX This might take time, so it's better to move to another
                // thread.
                ComponentTreeNode componentTree = registry.getComponentTree();
                componentTreeViewer.addComponentTree(componentTree);
            } catch (ComponentRegistryException e) {
                getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
            } catch (RuntimeException e) {
                getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            }
        }

        // load xRegistry only when myProxy is loaded and xRegistryURL is presented
        // new Thread() {
        // @Override
        // public void run() {
        // try {
        // XRegistryComponent client = new XRegistryComponent(
        // XBayaEngine.this.configuration.getXRegistryURL(), XRegistryComponent.Type.ABSTRACT,
        // XBayaEngine.this.myProxyClient.getProxy());
        // XBayaEngine.this.setXRegistryURL(XBayaEngine.this.configuration.getXRegistryURL());
        // ComponentTreeNode componentTree = client.getComponentTree();
        // componentTreeViewer.addComponentTree(componentTree);
        //
        // } catch (ComponentRegistryException e) {
        // getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
        // } catch (RuntimeException e) {
        // getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        // }
        // }
        // }.start();
    }

    // public void setXRegistryURL(URI xregistryURL) {
    // this.configuration.setXRegistryURL(xregistryURL);
    // }
    //
    // public URI getXRegistryClient() {
    // return this.configuration.getXRegistryURL();
    // }

    /**
     * Loads a default graph if needed either from myLead or the server.
     */
    // private void loadDefaultGraph() {
    // this.configuration.getGPELTemplateID();
    // String localWorkflow = this.configuration.getWorkflow();
    // if (null != localWorkflow && !"".equals(localWorkflow)) {
    // XRegistryAccesser xregistryAccesser = new XRegistryAccesser(this.configuration.getMyProxyUsername(),
    // this.configuration.getMyProxyPassphrase(), this.configuration.getMyProxyServer(),
    // this.configuration.getXRegistryURL());
    // Workflow workflow = xregistryAccesser.getWorkflow(localWorkflow);
    // this.setWorkflow(workflow);
    // }
    //
    // }

    private void loadLocalGraph(String graphFilePath) {
        try {
            Workflow workflow = new Workflow(XMLUtil.loadXML(new File(graphFilePath)));
            setWorkflow(workflow);
        } catch (GraphException e) {
            getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (ComponentException e) {
            getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (IOException e) {
            getErrorWindow().error(ErrorMessages.OPEN_FILE_ERROR, e);
        } catch (RuntimeException e) {
            getErrorWindow().error(ErrorMessages.GRAPH_LOAD_ERROR, e);
        } catch (Error e) {
            getErrorWindow().error(ErrorMessages.GRAPH_LOAD_ERROR, e);
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
            getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
        } catch (Error e) {
            getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }

    /**
     * @return
     */
    public WorkflowPropertyWindow getWorkflowPropertyWindow() {
        if (this.workflowPropertiesWindow == null) {
            this.workflowPropertiesWindow = new WorkflowPropertyWindow(this.engine);
        }
        return this.workflowPropertiesWindow;
    }

    /**
     * @param workflowComponent
     */
    public void addWorkflowComponent(String name, SubWorkflowComponent workflowComponent) {
        this.componentRegistry.addComponent(name, workflowComponent);
        SystemComponentReference componentReference = new SystemComponentReference(name, workflowComponent);
        this.systemComponentTree.add(new ComponentTreeNode(componentReference));
        systemComponentTree.getPath();
        ComponentSelector swingComponent = this.gui.getComponentSelector();
        swingComponent.getSwingComponent().updateUI();

    }

    /**
     * @param newStreamName
     * @return
     */
    public String getStreamRate(String newStreamName) {
        return this.getGUI().getStreamRate(newStreamName);
    }

    /**
     * @return
     */
    public StreamTableModel getStreamModel() {
        return this.getGUI().getStreamModel();
    }

}