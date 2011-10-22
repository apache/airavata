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

package org.apache.airavata.xbaya.gpel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.airavata.common.exception.UtilsException;
import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WorkflowComponent;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.graph.ws.WSGraphFactory;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.gpel.GpelConstants;
import org.gpel.client.GcBinaryWebResource;
import org.gpel.client.GcInstance;
import org.gpel.client.GcProcessResource;
import org.gpel.client.GcProvidesWsdlResource;
import org.gpel.client.GcResourceNotFoundException;
import org.gpel.client.GcSearchList;
import org.gpel.client.GcSearchRequest;
import org.gpel.client.GcTemplate;
import org.gpel.client.GcUsesWsdlResource;
import org.gpel.client.GcWebResource;
import org.gpel.client.GcWsdlResource;
import org.gpel.client.GcXmlWebResource;
import org.gpel.client.GpelClient;
import org.gpel.client.GpelUserCredentials;
import org.gpel.client.http.apache_http_client.Transport;
import org.gpel.client.security.GpelUserX509Credential;
import org.gpel.model.GpelProcess;

import xsul5.wsdl.WsdlDefinitions;

public class GPELClient extends WorkflowClient {
    /**
     * Constructs a GPELClient.
     */
    public GPELClient() {
        // Nothing
    }

    /**
     * Constructs a GPELClient.
     * 
     * @param engineURL
     * @param gpelUserX509Credential
     * @throws WorkflowEngineException
     */
    public GPELClient(URI engineURL, GpelUserX509Credential gpelUserX509Credential) throws WorkflowEngineException {
        this.engineURL = engineURL;
        this.gpelUserX509Credential = gpelUserX509Credential;
        connect();
    }

    /**
     * @param userX509Credential
     * @throws WorkflowEngineException
     */
    public void setUserX509Credential(UserX509Credential userX509Credential) throws WorkflowEngineException {
        this.gpelUserX509Credential = userX509Credential;
        connect();
    }

    /**
     * @param workflow
     * @param redeploy
     * @return The workflow template ID.
     * @throws WorkflowEngineException
     */
    public synchronized URI deploy(Workflow workflow, boolean redeploy) throws WorkflowEngineException {
        if (workflow.getGpelProcess() == null) {
            throw new IllegalStateException("BPEL script has to have been generated.");
        }
        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not been configured.");
        }

        GcTemplate template = null;
        List<GcWebResource> links;
        GcProcessResource processResource = null;
        GcXmlWebResource graphResource = null;
        GcBinaryWebResource imageResource = null;
        GcWsdlResource workflowWSDLresource = null;
        Map<String, GcWsdlResource> wsdlResourceMap = new HashMap<String, GcWsdlResource>();

        if (redeploy) {
            URI templateID = workflow.getGPELTemplateID();
            if (templateID != null) {
                try {
                    template = this.client.retrieveTemplate(templateID);
                } catch (GcResourceNotFoundException e) {
                    // The workflow was not found in the engine.
                    logger.error(e.getMessage(), e);
                    template = null;
                } catch (RuntimeException e) {
                    throw new WorkflowEngineException(ErrorMessages.GPEL_ERROR, e);
                }
            }
        }

        String title = workflow.getName();
        if (template == null) {
            // This case is either it is a new deploy or the tempate does not
            // exist in the GPEL Engine.

            template = this.client.createTemplate(title);
            links = new ArrayList<GcWebResource>();
            template.setLinks(links);
        } else {
            // Redeploy

            template.setTitle(title);
            links = template.getLinks();

            // Look for each resource.
            for (GcWebResource link : links) {
                String rel = link.getRel();
                logger.info("rel: " + rel);
                if (GpelConstants.REL_PROCESS.equals(rel)) {
                    processResource = (GcProcessResource) link;
                } else if (GPELLinksFilter.REL_XWF.equals(rel)) {
                    graphResource = (GcXmlWebResource) link;
                } else if (GPELLinksFilter.REL_IMAGE.equals(rel)) {
                    imageResource = (GcBinaryWebResource) link;
                } else if (GpelConstants.REL_WSDL.equals(rel)) {
                    GcWsdlResource wsdlResouece = (GcWsdlResource) link;
                    String wsdlTitle = wsdlResouece.getTitle();
                    logger.info("wsdlTitle: " + wsdlTitle);
                    // XXX Can we rely on the title to do the matching?
                    wsdlResourceMap.put(wsdlTitle, wsdlResouece);
                }
            }
        }

        // BPEL process
        GpelProcess process = workflow.getGpelProcess();
        if (processResource == null) {
            processResource = new GcProcessResource(PROCESS_GPEL_TITLE, process);
            links.add(processResource);
        } else {
            processResource.setXmlContent(process.xml());
        }

        // WSDL for the process
        workflowWSDLresource = wsdlResourceMap.remove(PROCESS_WSDL_TYTLE);
        WsdlDefinitions workflowWSDL = workflow.getWorkflowWSDL();
        logger.info(workflowWSDL.xmlString());
        if (workflowWSDLresource == null) {
            workflowWSDLresource = new GcWsdlResource(PROCESS_WSDL_TYTLE, workflowWSDL);
            links.add(workflowWSDLresource);
        } else {
            workflowWSDLresource.setXmlContent(workflowWSDL.xml());
        }

        Map<String, WsdlDefinitions> wsdlMap = workflow.getWSDLs();
        for (String id : wsdlMap.keySet()) {
            WsdlDefinitions wsdl = wsdlMap.get(id);
            GcWsdlResource wsdlResource = wsdlResourceMap.remove(id);
            if (wsdlResource == null) {
                wsdlResource = new GcWsdlResource(id, wsdl);
                links.add(wsdlResource);
            } else {
                wsdlResource.setXmlContent(wsdl.xml());
            }
        }

        // Remove the rest of unused WSDL from the links.
        for (GcWsdlResource resource : wsdlResourceMap.values()) {
            links.remove(resource);
        }

        // Graph
        Graph graph = workflow.getGraph();
        if (graphResource == null) {
            logger.info("Creating a new graphResource");
            graphResource = new GcXmlWebResource("process.xgr", graph.toXML(), GRAPH_MIME_TYPE);
            graphResource.setRel(GPELLinksFilter.REL_XWF);
            links.add(graphResource);
        } else {
            logger.info("Updating the graphResource");
            graphResource.setXmlContent(graph.toXML());
        }

        // Image
        try {
            BufferedImage image = workflow.getImage();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ImageIO.write(image, XBayaConstants.PNG_FORMAT_NAME, outStream);
            byte[] bytes = outStream.toByteArray();

            if (imageResource == null) {
                imageResource = new GcBinaryWebResource("image.png", bytes, PNG_MIME_TYPE);
                imageResource.setRel(GPELLinksFilter.REL_IMAGE);
                links.add(imageResource);
            } else {
                imageResource.setBinaryContent(bytes);
            }
        } catch (IOException e) {
            // It's OK to not save the image for now.
            logger.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // This happens only from junit tests.
            logger.error(e.getMessage(), e);
        }

        try {
            this.client.deployTemplate(template);

            // this needs to be kept and reused
            URI templateID = template.getTemplateId();
            workflow.setGPELTemplateID(templateID);

            return templateID;
        } catch (RuntimeException e) {
            throw new WorkflowEngineException(ErrorMessages.GPEL_ERROR, e);
        }
    }

    /**
     * @param id
     * @param workflowType
     * @return The workflow loaded
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws ComponentException
     */
    public synchronized Workflow load(URI id, WorkflowType workflowType) throws GraphException,
            WorkflowEngineException, ComponentException {
        logger.debug("ID: " + id.toString() + " Type:" + workflowType);

        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not configured.");
        }

        Workflow workflow = new Workflow();
        switch (workflowType) {
        case TEMPLATE:
            workflow.setGPELTemplateID(id);
            break;
        case INSTANCE:
            workflow.setGPELInstanceID(id);
            break;
        }

        // This is either a workflow template or a workflow instance.
        GcTemplate workflowTemplate;
        GcInstance workflowInstance = null;
        try {
            switch (workflowType) {
            case TEMPLATE:
                workflowTemplate = this.client.retrieveTemplate(id);
                break;
            case INSTANCE:
                // TODO change this when we start modifying the instance.
                workflowInstance = this.client.retrieveInstance(id);
                GcWebResource templateResource = workflowInstance.getLinkWithRel(GpelConstants.REL_TEMPLATE);
                URI templateID = templateResource.getId();
                workflow.setGPELTemplateID(templateID);
                workflowTemplate = this.client.retrieveTemplate(templateID);
                break;
            default:
                // This doesn't happen
                throw new XBayaRuntimeException();
            }
        } catch (GcResourceNotFoundException e) {
            // The workflow was not found in the engine.
            throw new WorkflowEngineException(ErrorMessages.GPEL_WORKFLOW_NOT_FOUND_ERROR, e);
        } catch (RuntimeException e) {
            throw new WorkflowEngineException(ErrorMessages.GPEL_ERROR, e);
        }

        GcProcessResource processResource = null;
        if (workflowInstance != null) {
            processResource = (GcProcessResource) workflowInstance.getLinkWithRel(GpelConstants.REL_PROCESS);
        }
        if (processResource == null) {
            processResource = (GcProcessResource) workflowTemplate.getLinkWithRel(GpelConstants.REL_PROCESS);
        }

        GcXmlWebResource graphResource = null;
        if (workflowInstance != null) {
            graphResource = (GcXmlWebResource) workflowInstance.getLinkWithRel(GPELLinksFilter.REL_XWF);
        }
        if (graphResource == null) {
            graphResource = (GcXmlWebResource) workflowTemplate.getLinkWithRel(GPELLinksFilter.REL_XWF);
        }

        GcBinaryWebResource imageResource = null;
        if (workflowInstance != null) {
            imageResource = (GcBinaryWebResource) workflowInstance.getLinkWithRel(GPELLinksFilter.REL_IMAGE);
        }
        if (imageResource == null) {
            imageResource = (GcBinaryWebResource) workflowTemplate.getLinkWithRel(GPELLinksFilter.REL_IMAGE);
        }

        // Look for wsdl resource.
        GcWsdlResource workflowWSDLresource = null;
        Map<String, GcWsdlResource> wsdlResourceMap = new HashMap<String, GcWsdlResource>();
        List<GcWebResource> links = workflowTemplate.getLinks();
        for (GcWebResource link : links) {
            String rel = link.getRel();
            logger.info("rel: " + rel);
            if (GpelConstants.REL_WSDL.equals(rel)) {
                GcWsdlResource wsdlResouece = (GcWsdlResource) link;
                String wsdlTitle = wsdlResouece.getTitle();
                logger.info("wsdlTitle: " + wsdlTitle);
                // Use use title to do matching
                wsdlResourceMap.put(wsdlTitle, wsdlResouece);
            }
        }

        // BPEL process
        GpelProcess process = new GpelProcess(processResource.getXmlContent());
        workflow.setGpelProcess(process);

        // WSDL for the process
        workflowWSDLresource = wsdlResourceMap.remove(PROCESS_WSDL_TYTLE);
        WsdlDefinitions workflowWSDL = new WsdlDefinitions(workflowWSDLresource.getXmlContent());
        workflow.setWorkflowWSDL(workflowWSDL);

        // WSDLs for services.
        for (String wsdlID : wsdlResourceMap.keySet()) {
            GcWsdlResource wsdlResource = wsdlResourceMap.get(wsdlID);
            WsdlDefinitions definition = new WsdlDefinitions(wsdlResource.getXmlContent());
            workflow.addWSDL(wsdlID, definition);
        }

        // Graph
        WSGraph graph = WSGraphFactory.createGraph(graphResource.getXmlContent());
        workflow.setGraph(graph);

        workflow.bindComponents();

        // Set the name of the workflow.
        // This has to be after parsing the graph so that workflow instance name
        // overwrites the name in the graph.
        String name = null;
        if (workflowInstance != null) {
            name = workflowInstance.getTitle();
            logger.info("name from the instance: " + name);
        }
        if (name == null) {
            name = workflowTemplate.getTitle();
            logger.info("name from the template: " + name);
        }
        workflow.setName(name);

        // Image
        if (imageResource != null) {
            byte[] imageBytes = imageResource.getBinaryContent();
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                workflow.setImage(image);
            } catch (IOException e) {
                // Not crucial
                logger.error(e.getMessage(), e);
            }
        }

        return workflow;
    }

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
    public synchronized GcSearchList list(int maxNum, WorkflowType type) throws WorkflowEngineException {
        logger.debug("Maxnum: " + maxNum + " Type: " + type);
        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not configured.");
        }

        try {
            // TODO return the ones only related to the user.
            GcSearchList results;
            switch (type) {
            case INSTANCE:
                // TODO no findInstances now.
                throw new UnsupportedOperationException();
            case TEMPLATE:
                results = this.client.findTemplates(maxNum, GcSearchRequest.SearchType.GPEL_TEMPLATE);
                break;
            default:
                throw new XBayaRuntimeException();
            }

            return results;
        } catch (RuntimeException e) {
            throw new WorkflowEngineException(e);
        }
    }

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
    public synchronized GcInstance instantiate(Workflow workflow, URI dscURL, String name)
            throws WorkflowEngineException, ComponentException, GraphException {
        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not been configured.");
        }

        URI templateID = workflow.getGPELTemplateID();
        if (templateID == null) {
            throw new IllegalStateException("The workflow has not been deployed.");
        }

        // declare instanceID here to use in the exception handling.
        URI instanceID = null;
        try {
            GcTemplate template = this.client.retrieveTemplate(templateID);
            GcInstance instance = template.createInstance();

            // Set the instance ID to the workflow so that the monitor doesn't
            // load the same workflow.
            instanceID = instance.getInstanceId();
            logger.info("instanceID: " + instanceID);
            workflow.setGPELInstanceID(instanceID);

            // Change the name
            if (name != null) {
                String title = instance.getTitle();
                title = title + " (" + name + ")";
                logger.info("new title: " + title);
                instance.setTitle(title);
            }

            Graph graph = workflow.getGraph();
            for (WSNode node : GraphUtil.getNodes(graph, WSNode.class)) {
                String partnerLinkName = BPELScript.createPartnerLinkName(node.getID());
                logger.info("partnerLinkName: " + partnerLinkName);
                WsdlDefinitions wsdl = node.getComponent().getWSDL();
                logger.info("WSDL QName: " + WSDLUtil.getWSDLQName(wsdl));

                if (WSDLUtil.isAWSDL(wsdl)) {
                    URI subWorkflowTemplateID = WorkflowComponent.getWorkflowTemplateID(wsdl);
                    if (subWorkflowTemplateID == null) {
                        // It's a service AWSDL. Create a CWSDL that uses DSC.
                        logger.info("service");
                        wsdl = DSCUtil.convertToCWSDL(WSDLUtil.deepClone(wsdl), dscURL);
                    } else {
                        // It's a workflow WSDL.
                        logger.info("workflow");
                        // recursively instantiate and start sub-workflows.
                        Workflow subWorkflow = load(subWorkflowTemplateID, WorkflowType.TEMPLATE);
                        GcInstance subInstance = instantiate(subWorkflow, dscURL, name);
                        // XXX Need to start to get a sub-workflow CWSDL.
                        // (Not so clean)
                        wsdl = start(subInstance);
                    }
                }

                GcUsesWsdlResource wsdlResource = new GcUsesWsdlResource(partnerLinkName, wsdl);
                instance.getLinks().add(wsdlResource);

                instance.store();
            }
            return instance;
        } catch (UtilsException e) {
            String message = "Error while creating a workflow instance. template ID: " + templateID + ", instanceID: "
                    + instanceID;
            throw new WorkflowEngineException(message, e);
        }
    }

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
    public synchronized GcInstance instantiate(Workflow workflow, Map<String, WsdlDefinitions> wsdlMap)
            throws WorkflowEngineException {
        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not configured.");
        }

        URI templateID = workflow.getGPELTemplateID();
        if (templateID == null) {
            throw new IllegalStateException("The workflow has not been deployed.");
        }

        try {
            GcTemplate template = this.client.retrieveTemplate(templateID);
            GcInstance instance = template.createInstance();

            // Set the instance ID to the workflow so that the monitor doesn't
            // load the same workflow.
            URI instanceID = instance.getInstanceId();
            logger.info("instanceID: " + instanceID);
            workflow.setGPELInstanceID(instanceID);

            for (String partnerLinkName : wsdlMap.keySet()) {
                WsdlDefinitions wsdl = wsdlMap.get(partnerLinkName);
                logger.info("partnerLinkName: " + partnerLinkName);
                logger.info("WSDL QName: " + WSDLUtil.getWSDLQName(wsdl));

                GcUsesWsdlResource wsdlResource = new GcUsesWsdlResource(partnerLinkName, wsdl);
                instance.getLinks().add(wsdlResource);

                instance.store();
            }
            return instance;
        } catch (RuntimeException e) {
            throw new WorkflowEngineException(e);
        }
    }

    /**
     * Starts the workflow instance.
     * <p/>
     * The AWSDLs in workflow must have been modified to CWSDLs.
     * 
     * @param instance
     * @return The WSDL of the workflow.
     * @throws WorkflowEngineException
     */
    public synchronized WsdlDefinitions start(final GcInstance instance) throws WorkflowEngineException {
        if (!isConnected()) {
            throw new IllegalStateException("The BPEL Engine has not configured.");
        }

        URI instanceID = null;
        try {
            instanceID = instance.getInstanceId();
            instance.start();

            // access public WSDL for created workflow (contains actual location
            // as SOAP endpoint
            String workflowPartnerLinkName = BPELScript.WORKFLOW_PARTNER_LINK;
            GcProvidesWsdlResource workflowPublicWsdl = (GcProvidesWsdlResource) instance.getLinkWithTitleAndRel(
                    workflowPartnerLinkName, GcProvidesWsdlResource.REL);

            WsdlDefinitions workflowCWSDL = new WsdlDefinitions(workflowPublicWsdl.getXmlContent());
            return workflowCWSDL;
        } catch (RuntimeException e) {
            String message = "Error while starting a workflow instance. instance ID: " + instanceID;
            throw new WorkflowEngineException(message, e);
        }
    }

    public synchronized void connect() throws WorkflowEngineException {
        if (this.engineURL == null) {
            if (this.client != null) {
                this.client = null;
                sendSafeEvent(new Event(Event.Type.GPEL_ENGINE_DISCONNECTED));
            }
            return;
        }

        logger.info("Connecting a GPEL Engine at " + this.engineURL);
        try {
            Transport transport;
            if (isSecure()) {
                if (this.gpelUserX509Credential == null) {
                    logger.info("Using ssl without any credential.");
                    this.gpelUserX509Credential = new GpelUserX509Credential(null,
                            XBayaSecurity.getTrustedCertificates());
                }
                transport = new Transport(this.gpelUserX509Credential);
            } else {
                // This one is phasing out.
                GpelUserCredentials credentials = new GpelUserCredentials("user", "password");
                transport = new Transport(credentials);
            }
            this.client = new GpelClient(this.engineURL, transport);
            this.client.setFilter(this.linksFilter);
            sendSafeEvent(new Event(Event.Type.GPEL_ENGINE_CONNECTED));
        } catch (RuntimeException e) {
            this.client = null;
            sendSafeEvent(new Event(Event.Type.GPEL_ENGINE_DISCONNECTED));
            throw new WorkflowEngineException(ErrorMessages.GPEL_CONNECTION_ERROR, e);
        }
    }

    public void setXRegistryUrl(URI xRegistryURL) {
        // nothing to be done here as we don't need Xregistry info for GPEL client
    }

    public void setXBayaEngine(XBayaEngine xBayaEngine) {
        // nothing to be done here as we don't need XBayaEngine info for GPEL client
    }

}