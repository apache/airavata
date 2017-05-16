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
package org.apache.airavata.workflow.model.component.ws;

import java.net.URI;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.ws.WorkflowNode;
import org.apache.airavata.workflow.model.wf.Workflow;
//import org.gpel.GpelConstants;
//import org.xmlpull.infoset.XmlNamespace;

//import xsul5.wsdl.WsdlDefinitions;
//import xsul5.wsdl.WsdlPortType;

public class WorkflowComponent extends WSComponent {

    /**
     * GPEL_NAMESPACE
     */
//    public static final XmlNamespace GPEL_NAMESPACE = GpelConstants.GPEL_NS;

    /**
     * workflowTemplateID
     */
    public static final String WORKFLOW_TEMPLATE_ID_TAG = "workflowTemplateID";

    protected Workflow workflow;

    private URI templateID;

    /**
     * Constructs a WorkflowComponent.
     * 
     * This method is used when loading a workflow component from GPEL.
     * 
     * @param workflow
     * @throws ComponentException
     */
    public WorkflowComponent(Workflow workflow) throws ComponentException {
        this.workflow = workflow;
        this.templateID = this.workflow.getGPELTemplateID();

//        // Add template ID to WSDL
//        for (WsdlPortType portType : this.wsdl.portTypes()) {
//            XmlElement templateIDElement = portType.xml().addElement(GPEL_NAMESPACE, WORKFLOW_TEMPLATE_ID_TAG);
//            templateIDElement.setText(this.templateID.toString());
//        }
    }

    /**
     * Returns the templateID.
     * 
     * @return The templateID
     */
    public URI getTemplateID() {
        return this.templateID;
    }

//    /**
//     * Constructs a WorkflowComponent.
//     * 
//     * This method is used when loading a workflow component from an xwf file.
//     * 
//     * @param wsdl
//     * @param portTypeQName
//     * @param operationName
//     * @throws ComponentException
//     */
//    public WorkflowComponent(WsdlDefinitions wsdl, QName portTypeQName, String operationName) throws ComponentException {
////        super(wsdl, portTypeQName, operationName);
////        try {
////            // Get template ID from WSDL
////            WsdlPortType portType = this.wsdl.getPortType(portTypeQName.getLocalPart());
////            XmlElement templateIDElement = portType.xml().element(GPEL_NAMESPACE, WORKFLOW_TEMPLATE_ID_TAG);
////            String templateIDString = templateIDElement.requiredText();
////            this.templateID = new URI(templateIDString);
////        } catch (URISyntaxException e) {
////            throw new ComponentException(e);
////        }
//    }

    /**
     * @param workflowClient
     * @return The workflow
     * @throws ComponentException
     * @throws WorkflowEngineException
     * @throws GraphException
     */
    public Workflow getWorkflow() throws GraphException, ComponentException {
        return this.workflow;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ws.WSComponent#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public WorkflowNode createNode(Graph graph) {
        WorkflowNode node = new WorkflowNode(graph);

        // Copy some infomation from the component

        node.setName(getName());
        node.setComponent(this);
        // node.setWSDLQName(this.wsdlQName);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creat ports
        createPorts(node);

        return node;
    }

//    /**
//     * @param definitions
//     * @return workflow template ID if the specified WSDL definition is for a workflow; null otherwise.
//     * @throws ComponentException
//     */
//    public static URI getWorkflowTemplateID(WsdlDefinitions definitions) throws ComponentException {
//        try {
//            // Get template ID from WSDL
//            WsdlPortType portType = WSDLUtil.getFirstPortType(definitions);
//            XmlElement templateIDElement = portType.xml().element(GPEL_NAMESPACE, WORKFLOW_TEMPLATE_ID_TAG);
//            if (templateIDElement == null) {
//                // Not a workflow
//                return null;
//            } else {
//                String templateIDString = templateIDElement.requiredText();
//                URI templateID = new URI(templateIDString);
//                return templateID;
//            }
//        } catch (URISyntaxException e) {
//            throw new ComponentException(e);
//        } catch (UtilsException e) {
//            throw new ComponentException(e);
//        }
//    }
}