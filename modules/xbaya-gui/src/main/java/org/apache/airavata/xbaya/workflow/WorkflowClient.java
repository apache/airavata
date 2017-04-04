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
package org.apache.airavata.xbaya.workflow;

import java.net.URI;
import java.util.Map;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.messaging.event.EventProducer;
import org.gpel.client.GcInstance;
import org.gpel.client.GcSearchList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul5.wsdl.WsdlDefinitions;

public abstract class WorkflowClient extends EventProducer {
    /**
     * Either workflow template or workflow instance
     */
    public enum WorkflowType {
        /**
         * Workflow template
         */
        TEMPLATE,

        /**
         * Workflow instance
         */
        INSTANCE
    }

    protected static final String PROCESS_WSDL_TYTLE = "process.wsdl";

    protected static final String PROCESS_GPEL_TITLE = "process.gpel";

    protected static final String PNG_MIME_TYPE = "image/png";

    protected static final String GRAPH_MIME_TYPE = "application/x-xbaya+xml";

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowClient.class);

    protected URI engineURL;

    // ===========================================================================
    // Concrete Methods
    // ===========================================================================

    /**
     * @param engineURL
     * @throws WorkflowEngineException
     */
    public void setEngineURL(URI engineURL) throws WorkflowEngineException {
        this.engineURL = engineURL;
        connect();
    }

    /**
     * @return The URL of the GPEL Engine.
     */
    public URI getEngineURL() {
        return this.engineURL;
    }

    /**
     * Deploys a workflow to the GPEL Engine.
     * 
     * @param workflow
     * @param redeploy
     * @return The workflow template ID.
     * @throws GraphException
     * @throws WorkflowEngineException
     */
    public URI createScriptAndDeploy(Workflow workflow, boolean redeploy) throws GraphException,
            WorkflowEngineException {
        logger.debug("Entering: " + workflow.toString());

        // Generate a BPEL process.
        workflow.createScript();

        return deploy(workflow, redeploy);
    }

    /**
     * Loads a workflow with s specified workflow template ID.
     * 
     * @param templateID
     *            The workflow template ID.
     * @return The workflow loaded
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws org.apache.airavata.workflow.model.component.ComponentException
     * 
     */
    public Workflow load(URI templateID) throws GraphException, WorkflowEngineException, ComponentException {
        // Don't delete this method because the portal uses it.
        return load(templateID, WorkflowType.TEMPLATE);
    }

    /**
     * Returns the List of GcSearchResult.
     * <p/>
     * This method returns the first 100 matches.
     * 
     * @return The List of GcSearchResult.
     * @throws WorkflowEngineException
     */
    public GcSearchList list() throws WorkflowEngineException {
        return list(100, WorkflowType.TEMPLATE);
    }

    /**
     * @param workflow
     * @param dscURL
     * @return The instance of workflow
     * @throws WorkflowEngineException
     * @throws ComponentException
     * @throws GraphException
     */
    public GcInstance instantiate(Workflow workflow, URI dscURL) throws WorkflowEngineException, ComponentException,
            GraphException {
        return instantiate(workflow, dscURL, null);
    }

    /**
     * @return True if the connection is secure; false otherwise.
     */
    public boolean isSecure() {
        return true;
        // return SecurityUtil.isSecureService(this.engineURL);
    }

    /**
     * Checks if the client is connected to the BPEL engine.
     * 
     * @return true if the client is connected to the BPEL engine; false otherwise.
     */
    protected synchronized boolean isConnected() {
        throw new WorkflowRuntimeException("Critical Error: Called a unsupported API");
    }

    // ===========================================================================
    // Abstract Methods
    // ===========================================================================

    /**
     * @param workflow
     * @param redeploy
     * @return The workflow template ID.
     * @throws WorkflowEngineException
     */
    public abstract URI deploy(Workflow workflow, boolean redeploy) throws WorkflowEngineException;

    /**
     * @param id
     * @param workflowType
     * @return The workflow loaded
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws ComponentException
     */
    public abstract Workflow load(URI id, WorkflowType workflowType) throws GraphException, WorkflowEngineException,
            ComponentException;

    /**
     * Returns the List of GcSearchResult.
     * 
     * @param maxNum
     *            The maximum number of results
     * @param type
     * @return The List of GcSearchResult.
     * @throws WorkflowEngineException
     */
    @SuppressWarnings("boxing")
    public abstract GcSearchList list(int maxNum, WorkflowType type) throws WorkflowEngineException;

    /**
     * @param workflow
     *            The workflow to instantiate.
     * @param dscURL
     *            The URL of the DSC.
     * @param name
     *            The name that becomes a part of the workflow instance name.
     * @return The instance of workflow
     * @throws WorkflowEngineException
     * @throws ComponentException
     * @throws GraphException
     */
    public abstract GcInstance instantiate(Workflow workflow, URI dscURL, String name) throws WorkflowEngineException,
            ComponentException, GraphException;

    /**
     * Instantiate a specified workflow.
     * <p/>
     * The workflow must have been dployed.
     * 
     * @param workflow
     * @param wsdlMap
     *            Map<partnerLinkName, CWSDL>
     * @return The workflow instance.
     * @throws WorkflowEngineException
     * @Deprecated This one doesn't support hierarchical workflows. Use instantiate(workflow, dscURL) instead.
     */
    @Deprecated
    public abstract GcInstance instantiate(Workflow workflow, Map<String, WsdlDefinitions> wsdlMap)
            throws WorkflowEngineException;

    /**
     * Starts the workflow instance.
     * <p/>
     * The AWSDLs in workflow must have been modified to CWSDLs.
     * 
     * @param instance
     * @return The WSDL of the workflow.
     * @throws WorkflowEngineException
     */
    public abstract WsdlDefinitions start(final GcInstance instance) throws WorkflowEngineException;

    public abstract void connect() throws WorkflowEngineException;

    public abstract void setXBayaEngine(XBayaEngine xBayaEngine);

}
