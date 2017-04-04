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
package org.apache.airavata.xbaya.modifier;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.airavata.workflow.model.component.system.InputComponent;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.messaging.EventData;
import org.apache.airavata.xbaya.messaging.EventDataRepository;
import org.apache.airavata.xbaya.messaging.MonitorException;
import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler.NodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowModifier {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowModifier.class);

    private Workflow modifiedWorkflow;

    private EventDataRepository eventData;

    /**
     * Constructs a WorkflowModifier.
     * 
     * @param modifiedWorkflow
     * @param eventData
     */
    public WorkflowModifier(Workflow modifiedWorkflow, EventDataRepository eventData) {
        this.modifiedWorkflow = modifiedWorkflow;
        this.eventData = eventData;
    }

    /**
     * @return The workflow that needs to be executed.
     * @throws GraphException
     * @throws MonitorException
     */
    public Workflow createDifference() throws GraphException, MonitorException {
        WSGraph originalGraph = this.modifiedWorkflow.getGraph();
        Workflow workflow = this.modifiedWorkflow.clone();
        String name = workflow.getName();
        name += " (diff)";
        workflow.setName(name);
        WSGraph graph = workflow.getGraph();

        // Remove the finished node.
        removeFinishedNodes(originalGraph, graph);

        Set<WSPort> originalFromPorts = getFinalOutputPorts(originalGraph, graph);

        // Create input nodes for unconnected input ports.
        createInputNodes(graph, originalFromPorts);

        // Set default values.
        for (WSPort originalFromPort : originalFromPorts) {
            // TODO handle the case that node is not WSNode.
            Node originalFromNode = originalFromPort.getNode();
            String fromNodeID = originalFromNode.getID();
            String output;
            if (originalFromNode instanceof InputNode) {
                // notification that includes the input of the workflow.
                output = getWorkflowInput(fromNodeID);
            } else if (originalFromNode instanceof WSNode) {
                // Retrieve input value from notification.
                WSComponent component = ((WSNode) originalFromNode).getComponent();
                String messageName = component.getOutputTypeName();
                String parameterName = originalFromPort.getComponentPort().getName();
                output = getOutput(fromNodeID, messageName, parameterName);
            } else {
                // This should not happen.
                throw new WorkflowRuntimeException(originalFromNode.getClass().getName());
            }
            Port originalToPort = originalFromPort.getToPorts().get(0);
            PortImpl toPort = graph.getPort(originalToPort.getID());
            InputNode inputNode = (InputNode) toPort.getFromNode();
            inputNode.setDefaultValue(output);
        }

        return workflow;
    }

    /**
     * @param originalGraph
     * @param graph
     * @throws GraphException
     */
    private void removeFinishedNodes(WSGraph originalGraph, WSGraph graph) throws GraphException {
        ArrayList<Node> finishedNodes = new ArrayList<Node>();
        for (Node node : originalGraph.getNodes()) {
            Color color = NodeController.getGUI(node).getBodyColor();
            if (NodeState.FINISHED.color.equals(color)) {
                finishedNodes.add(node);
            }
        }
        for (Node finishedNode : finishedNodes) {
            Node node = graph.getNode(finishedNode.getID());
            graph.removeNode(node);
        }
    }

    /**
     * @param originalGraph
     * @param graph
     * @return The final output ports.
     */
    private Set<WSPort> getFinalOutputPorts(WSGraph originalGraph, WSGraph graph) {
        Collection<Port> inputPorts = GraphUtil.getPorts(graph, Port.Kind.DATA_IN);
        Set<WSPort> originalFromPorts = new HashSet<WSPort>();
        for (Port inputPort : inputPorts) {
            Port fromPort = inputPort.getFromPort();
            if (fromPort == null) {
                // This input port is not connected.
                String inputPortID = inputPort.getID();
                logger.debug("id: " + inputPortID);
                Port originalInputPort = originalGraph.getPort(inputPortID);
                // No duplicate in set.
                Port originalFromPort = originalInputPort.getFromPort();
                originalFromPorts.add((WSPort) originalFromPort);
            }
        }
        return originalFromPorts;
    }

    /**
     * @param graph
     * @param originalFromPorts
     * @throws GraphException
     */
    private void createInputNodes(WSGraph graph, Set<WSPort> originalFromPorts) throws GraphException {
        InputComponent inputComponent = new InputComponent();
        for (WSPort originalFromPort : originalFromPorts) {
            InputNode inputNode = inputComponent.createNode(graph);
            List<Port> originalToPorts = originalFromPort.getToPorts();
            boolean first = true;
            for (Port originalToPort : originalToPorts) {
                String toPortID = originalToPort.getID();
                Port toPort = graph.getPort(toPortID);
                graph.addEdge(inputNode.getPort(), toPort);
                if (first) {
                    first = false;
                    Point position = NodeController.getGUI(originalToPort).getPosition();
                    Point inputNodePosition = new Point(0, position.y);
                    inputNode.setPosition(inputNodePosition);
                }
            }
        }
    }

    private String getWorkflowInput(String nodeID) throws MonitorException {
        logger.debug("Node:" + nodeID);
        List<EventData> events = this.eventData.getEvents();
        for (EventData event : events) {
//            EventType type = event.getType();
//            // TODO change this to read from the notification from GPEL.
//            if (type != EventType.INVOKING_SERVICE) {
//                continue;
//            }
//            String id = event.getNodeID();
//            if (!"".equals(id)) {
//                continue;
//            }
//            // TODO null check
//            XmlElement eventElement = event.getEvent();
//            XmlElement result = eventElement.element(MonitorUtil.REQUEST);
//            XmlElement body = result.element(MonitorUtil.BODY);
//            XmlElement soapBody = body.element(XmlConstants.S_BODY);
//            WsdlPortTypeOperation wsdlPortTypeOperation;
//            try {
//                wsdlPortTypeOperation = WSDLUtil.getFirstOperation(this.modifiedWorkflow.getWorkflowWSDL());
//            } catch (UtilsException e) {
//                throw new MonitorException(e);
//            }
//            XmlElement part = soapBody.element(wsdlPortTypeOperation.getName());
//            XmlElement parameter = part.element(nodeID);
//            // TODO support complex type.
//            String value = parameter.requiredText();
            return event.getMessage();
        }
        // TODO
        String message = "Couldn't find a notification of about the input with nodeID, " + nodeID;
        throw new MonitorException(message);
    }

    private String getOutput(String nodeID, String messageName, String parameterName) throws MonitorException {
        List<EventData> events = this.eventData.getEvents();
        for (EventData event : events) {
//            // We need to find the notification that contains the output of the
//            // service invocation.
//            EventType type = event.getType();
//            if (!(type == EventType.SENDING_RESULT || type == EventType.RECEIVED_RESULT)) {
//                continue;
//            }
//            String id = event.getNodeID();
//            if (!nodeID.equals(id)) {
//                continue;
//            }
//            // TODO null check
//            XmlElement eventElement = event.getEvent();
//            XmlElement result = eventElement.element(MonitorUtil.RESULT);
//            XmlElement body = result.element(MonitorUtil.BODY);
//            XmlElement soapBody = body.element(XmlConstants.S_BODY);
//            XmlElement part = soapBody.element(messageName);
//            XmlElement parameter = part.element(parameterName);
//            // TODO support complex type.
//            String value = parameter.requiredText();
            return event.getMessage();
        }
        // TODO
        String message = "Couldn't find a notification of the output from the service with nodeID, " + nodeID;
        throw new MonitorException(message);
    }

}