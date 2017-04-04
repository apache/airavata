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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.interpretor;
//
//import java.io.IOException;
//import java.net.URI;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.common.utils.XMLUtil;
//import org.apache.airavata.registry.api.workflow.NodeExecutionError;
//import org.apache.airavata.workflow.model.graph.EPRPort;
//import org.apache.airavata.workflow.model.graph.Edge;
//import org.apache.airavata.workflow.model.graph.Graph;
//import org.apache.airavata.workflow.model.graph.Node;
//import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
//import org.apache.airavata.workflow.model.graph.Port;
//import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.graph.util.GraphUtil;
//import org.apache.airavata.workflow.model.graph.ws.WSGraph;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.ws.monitor.EventData;
//import org.apache.airavata.ws.monitor.MonitorException;
//import org.apache.airavata.ws.monitor.MonitorUtil;
//import org.apache.airavata.ws.monitor.MonitorUtil.EventType;
//import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
//import org.apache.airavata.wsmg.client.MsgBrokerClientException;
//import org.apache.airavata.wsmg.client.NotificationHandler;
//import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
//import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.apache.airavata.xbaya.graph.controller.NodeController;
//import org.apache.airavata.xbaya.provenance.WorkflowNodeStatusUpdater;
//import org.apache.airavata.xbaya.provenance.WorkflowStatusUpdater;
//import org.apache.axiom.soap.SOAPEnvelope;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.addressing.EndpointReference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.infoset.XmlElement;
//
//public class WorkflowInterpretorEventListener implements NotificationHandler, ConsumerNotificationHandler {
//
//    private Workflow workflow;
//    private boolean pullMode;
//    private WseMsgBrokerClient wseClient;
//    private URI brokerURL;
//    private String topic;
//    private URI messageBoxURL;
//    private String subscriptionID;
//    private MessagePuller messagePuller;
//    private WorkflowStatusUpdater workflowStatusUpdater;
//    private WorkflowNodeStatusUpdater workflowNodeStatusUpdater;
//    private WorkflowInterpreterConfiguration workflowInterpreterConfiguration;
//    private String lastSubscriptionId;
//
//    private static Logger logger = LoggerFactory.getLogger(WorkflowInterpretorEventListener.class);
//
//    public WorkflowInterpretorEventListener(Workflow workflow, XBayaConfiguration configuration) {
//        this.workflow = workflow;
//        this.brokerURL = configuration.getBrokerURL();
//        this.topic = configuration.getTopic();
//        this.pullMode = true;
//        this.messageBoxURL = configuration.getMessageBoxURL();
//        this.wseClient = new WseMsgBrokerClient();
//        this.wseClient.init(this.brokerURL.toString());
//        this.workflowInterpreterConfiguration = WorkflowInterpreter.getWorkflowInterpreterConfiguration();
//        this.workflowNodeStatusUpdater = new WorkflowNodeStatusUpdater(this.workflowInterpreterConfiguration.getAiravataAPI());
//        this.workflowStatusUpdater = new WorkflowStatusUpdater(this.workflowInterpreterConfiguration.getAiravataAPI());
//    }
//
//    public void start() throws MonitorException {
//
//        subscribe();
//    }
//
//    public void stop() throws MonitorException {
//        unsubscribe();
//    }
//
//    private synchronized void subscribe() throws MonitorException {
//        if (this.subscriptionID != null) {
//            throw new IllegalStateException();
//        }
//        try {
//            if (this.pullMode) {
//                EndpointReference messageBoxEPR = this.wseClient.createPullMsgBox(this.messageBoxURL.toString(),20000L);
//                this.subscriptionID = this.wseClient.subscribe(messageBoxEPR.getAddress(), this.topic, null);
//                this.messagePuller = this.wseClient.startPullingEventsFromMsgBox(messageBoxEPR, this, 1000L, 20000L);
//            } else {
//                String[] endpoints = this.wseClient.startConsumerService(2222, this);
//                this.subscriptionID = this.wseClient.subscribe(endpoints[0], this.topic, null);
//            }
//        } catch (IOException e) {
//            throw new MonitorException("Failed to subscribe.", e);
//        } catch (RuntimeException e) {
//            throw new MonitorException("Failed to subscribe.", e);
//        }
//    }
//
//    /**
//     * Unsubscribes from the notification.
//     * 
//     * @throws MonitorException
//     */
//    private synchronized void unsubscribe() throws MonitorException {
//        // This method needs to be synchronized along with subscribe() because
//        // unsubscribe() might be called while subscribe() is being executed.
//        if (this.subscriptionID == null) {
//            throw new IllegalStateException();
//        }
//        try {
//            if (this.pullMode) {
//                this.messagePuller.stopPulling();
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    throw new MonitorException("Error during stop message puller", e);
//                }
////            } else {
////                this.wseClient.unSubscribe(this.subscriptionID);
//            }
//            this.wseClient.unSubscribe(this.subscriptionID);
//
//        } catch (MsgBrokerClientException e) {
//            throw new MonitorException("Failed to unsubscribe.", e);
//        }
//
//    }
//
//    /**
//     * @see org.apache.airavata.wsmg.client.NotificationHandler#handleNotification(java.lang.String)
//     */
//    public void handleNotification(String message) {
//        try {
//            // String soapBody = WorkFlowUtils.getSoapBodyContent(message);
//            XmlElement event = XMLUtil.stringToXmlElement(message);
//            handleEvent(new EventData(event), true, this.workflow.getGraph());
//
//            // } catch (XMLStreamException e) {
//            // // Just log them because they can be unrelated messages sent to
//            // // this topic by accident.
//            // logger.warn("Could not parse received notification: " + message,
//            // e);
//            // }
//        } catch (RuntimeException e) {
//            logger.warn("Failed to process notification: " + message, e);
//        } catch (AiravataAPIInvocationException e) {
//            logger.error("Error occured during Exception saving to the Registry");
//        }
//    }
//
//    private void handleEvent(EventData event, boolean forward, Graph graph) throws AiravataAPIInvocationException {
//        EventType type = event.getType();
//        String nodeID = event.getNodeID();
//        Node node = graph.getNode(nodeID);
//
//        if (type == MonitorUtil.EventType.WORKFLOW_INVOKED) {
//            workflowStarted(graph, forward);
//            //todo ideally experimentID and workflowInstanceID has to be different
//            workflowStatusUpdater.saveWorkflowData(event.getExperimentID(), event.getExperimentID(),
//                    this.workflowInterpreterConfiguration.getWorkflow().getName());
//            workflowStatusUpdater.workflowStarted(event.getExperimentID());
//        } else if (type == MonitorUtil.EventType.WORKFLOW_TERMINATED) {
//            workflowFinished(graph, forward);
//            workflowStatusUpdater.workflowFinished(event.getExperimentID());
//            try {
//                this.unsubscribe();
//            } catch (MonitorException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        } else if (type == EventType.INVOKING_SERVICE || type == EventType.SERVICE_INVOKED) {
//            if (node == null) {
//                if (nodeID!=null && !nodeID.equals("")) {
//					logger.warn("There is no node that has ID, " + nodeID);
//				}
//            } else {
//                nodeStarted(node, forward);
//                workflowNodeStatusUpdater.workflowNodeStarted(event.getExperimentID(), event.getNodeID()
//                        , event.getMessage(), event.getWorkflowID().toASCIIString());
//            }
//        } else if (type == MonitorUtil.EventType.RECEIVED_RESULT
//        // TODO this should be removed when GPEL sends all notification
//        // correctly.
//                || type == EventType.SENDING_RESULT) {
//            if (node == null) {
//            	if (nodeID!=null && !nodeID.equals("")) {
//					logger.warn("There is no node that has ID, " + nodeID);
//				}
//        	} else {
//                nodeFinished(node, forward);
//                workflowNodeStatusUpdater.workflowNodeFinished(event.getExperimentID(), event.getNodeID(), event.getMessage(),
//                        event.getWorkflowID().toASCIIString());
//            }
//        } else if (type == EventType.RECEIVED_FAULT
//                || type == EventType.SENDING_FAULT || type == EventType.SENDING_RESPONSE_FAILED) {
//            //Constructing NodeExecutionError with required data...
//            logger.error(event.getMessage());
//            NodeExecutionError nodeExecutionError = new NodeExecutionError();
//            nodeExecutionError.setExperimentId(event.getExperimentID());
//            nodeExecutionError.setNodeId(event.getNodeID());
//            nodeExecutionError.setWorkflowInstanceId(event.getExperimentID());
//            nodeExecutionError.setErrorMessage(event.getMessage());
//            nodeExecutionError.setErrorDescription(event.getMessage());
//            nodeExecutionError.setErrorTime(event.getTimestamp());
//            this.workflowInterpreterConfiguration.getAiravataAPI().getExecutionManager().addNodeExecutionError(nodeExecutionError);
//            if (node == null) {
//            	if (nodeID!=null && !nodeID.equals("")) {
//					logger.warn("There is no node that has ID, " + nodeID);
//				}
//            } else {
//                nodeFailed(node, forward);
//                workflowNodeStatusUpdater.workflowNodeFailed(event.getExperimentID(), event.getNodeID());
//            }
//            try {
//                this.unsubscribe();
//            } catch (MonitorException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        } else if (type == MonitorUtil.EventType.RESOURCE_MAPPING) {
//            if (node == null) {
//            	if (nodeID!=null && !nodeID.equals("")) {
//					logger.warn("There is no node that has ID, " + nodeID);
//				}
//            } else {
//                // nodeResourceMapped(node, event.getEvent(), forward);
//                workflowNodeStatusUpdater.workflowNodeRunning(event.getExperimentID(), event.getNodeID());
//            }
//        } else if(type == MonitorUtil.EventType.LOG_INFO){
//            // This is not very gram specific, if these data is required in other provider they have to send
//            // the notification in info mode with ending these text, DONE,PENDING and ACTIVE
//            if(event.getMessage().endsWith("DONE")) {
//                workflowNodeStatusUpdater.workflowNodeStatusDone(event.getExperimentID(), event.getNodeID());
//            } else if(event.getMessage().endsWith("PENDING")){
//                workflowNodeStatusUpdater.workflowNodeStatusPending(event.getExperimentID(), event.getNodeID());
//            } else if(event.getMessage().endsWith("ACTIVE")){
//                workflowNodeStatusUpdater.workflowNodeStatusActive(event.getExperimentID(), event.getNodeID());
//            }
//        } else {
//            // Ignore the rest.
//        }
//    }
//
//    private void workflowStarted(Graph graph, boolean forward) {
//        for (InputNode node : GraphUtil.getInputNodes(graph)) {
//            if (forward) {
//                finishNode(node);
//            } else {
//                resetNode(node);
//            }
//        }
//    }
//
//    private void workflowFinished(Graph graph, boolean forward) {
//        for (OutputNode node : GraphUtil.getOutputNodes(graph)) {
//            if (forward) {
//                finishNode(node);
//                finishPredecessorNodes(node);
//            } else {
//                resetNode(node);
//            }
//        }
//    }
//
//    private LinkedList<InputNode> getInputNodes(WSGraph graph) {
//        List<NodeImpl> nodes = graph.getNodes();
//        LinkedList<InputNode> inputNodes = new LinkedList<InputNode>();
//        for (NodeImpl nodeImpl : nodes) {
//            if (nodeImpl instanceof InputNode) {
//                inputNodes.add((InputNode) nodeImpl);
//            }
//        }
//        return inputNodes;
//    }
//
//    private LinkedList<OutputNode> getOutputNodes(WSGraph graph) {
//        List<NodeImpl> nodes = graph.getNodes();
//        LinkedList<OutputNode> outputNodes = new LinkedList<OutputNode>();
//        for (NodeImpl nodeImpl : nodes) {
//            if (nodeImpl instanceof OutputNode) {
//                outputNodes.add((OutputNode) nodeImpl);
//            }
//        }
//        return outputNodes;
//    }
//
//    private void nodeStarted(Node node, boolean forward) {
//        if (forward) {
//            executeNode(node);
//            finishPredecessorNodes(node);
//        } else {
//            resetNode(node);
//        }
//    }
//
//    private void nodeFinished(Node node, boolean forward) {
//        if (forward) {
//            finishNode(node);
//            finishPredecessorNodes(node);
//        } else {
//            executeNode(node);
//        }
//    }
//
//    private void nodeFailed(Node node, boolean forward) {
//        if (forward) {
//            failNode(node);
//            finishPredecessorNodes(node);
//        } else {
//            executeNode(node);
//        }
//    }
//
//    private void executeNode(Node node) {
//        node.setState(NodeExecutionState.EXECUTING);
//    }
//
//    private void finishNode(Node node) {
//        node.setState(NodeExecutionState.FINISHED);
//    }
//
//    private void failNode(Node node) {
//        node.setState(NodeExecutionState.FAILED);
//    }
//
//    private void resetNode(Node node) {
//        node.setState(NodeExecutionState.WAITING);
//        NodeController.getGUI(node).resetTokens();
//    }
//
//    /**
//     * Make preceding nodes done. This helps the monitoring GUI when a user subscribes from the middle of the workflow
//     * execution.
//     * 
//     * @param node
//     */
//    private void finishPredecessorNodes(Node node) {
//        for (Port inputPort : node.getInputPorts()) {
//            for (Edge edge : inputPort.getEdges()) {
//                Port fromPort = edge.getFromPort();
//                if (!(fromPort instanceof EPRPort)) {
//                    Node fromNode = fromPort.getNode();
//                    finishNode(fromNode);
//                    finishPredecessorNodes(fromNode);
//                }
//            }
//        }
//        Port controlInPort = node.getControlInPort();
//        if (controlInPort != null) {
//            for (Node fromNode : controlInPort.getFromNodes()) {
//                finishNode(fromNode);
//                finishPredecessorNodes(fromNode);
//            }
//        }
//    }
//
//    /**
//     * @see org.apache.airavata.wsmg.client.NotificationHandler#handleNotification(java.lang.String)
//     */
//    public void handleNotification(SOAPEnvelope message) {
//        String soapBody = message.getBody().toString();
//        this.handleNotification(soapBody);
//    }
//
//}