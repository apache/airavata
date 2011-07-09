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

package org.apache.airavata.xbaya.interpretor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.graph.EPRPort;
import org.apache.airavata.xbaya.graph.Edge;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.monitor.MonitorEvent;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.monitor.MonitorUtil;
import org.apache.airavata.xbaya.monitor.MonitorUtil.EventType;
import org.apache.airavata.xbaya.monitor.WsmgClient;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.infoset.XmlElement;

import wsmg.NotificationHandler;
import wsmg.WseClientAPI;
import wsmg.XmlConsumer;
import wsmg.pull.MessagePuller;
import wsmg.util.WsmgUtil;
import xsul.ws_addressing.WsaEndpointReference;
import xsul5.MLogger;

public class WorkflowInterpretorEventListener implements NotificationHandler {

    private Workflow workflow;
    private WsmgClient wsmgClient;
    private boolean pullMode;
    private WseClientAPI wseClient;
    private URI brokerURL;
    private String topic;
    private URI messageBoxURL;
    private String subscriptionID;
    private XmlConsumer xmlConsumer;
    private MessagePuller messagePuller;

    private static MLogger logger = MLogger.getLogger();

    public WorkflowInterpretorEventListener(Workflow workflow, XBayaConfiguration configuration) {
        this.workflow = workflow;
        this.brokerURL = configuration.getBrokerURL();
        this.topic = configuration.getTopic();
        this.pullMode = true;
        this.messageBoxURL = configuration.getMessageBoxURL();

        this.wseClient = new WseClientAPI();
    }

    public void start() throws MonitorException {

        subscribe();
    }

    public void stop() throws MonitorException {
        unsubscribe();
    }

    private synchronized void subscribe() throws MonitorException {
        if (this.subscriptionID != null) {
            throw new IllegalStateException();
        }
        try {
            if (this.pullMode) {
                WsaEndpointReference messageBoxEPR = this.wseClient.createPullMsgBox(this.messageBoxURL.toString());
                URI address = messageBoxEPR.getAddress();
                this.subscriptionID = this.wseClient.subscribe(this.brokerURL.toString(), address.toString(),
                        this.topic);
                this.messagePuller = this.wseClient.startPullingEventsFromMsgBox(messageBoxEPR, this, 1000L);
            } else {
                this.xmlConsumer = new XmlConsumer(0, this);
                this.xmlConsumer.start();
                URL consumerUrl = new URL(this.xmlConsumer.getServer().getLocation());
                this.subscriptionID = this.wseClient.subscribe(this.brokerURL.toString(), consumerUrl.getHost() + ":"
                        + consumerUrl.getPort(), this.topic);
            }
        } catch (IOException e) {
            throw new MonitorException("Failed to subscribe.", e);
        } catch (RuntimeException e) {
            throw new MonitorException("Failed to subscribe.", e);
        }
    }

    /**
     * Unsubscribes from the notification.
     * 
     * @throws MonitorException
     */
    private synchronized void unsubscribe() throws MonitorException {
        // This method needs to be synchronized along with subscribe() because
        // unsubscribe() might be called while subscribe() is being executed.
        if (this.subscriptionID == null) {
            throw new IllegalStateException();
        }
        try {
            if (this.pullMode) {
                this.messagePuller.stopPulling();
            } else {
                this.xmlConsumer.shutdown();
            }
            this.wseClient.unSubscribe(this.brokerURL.toString(), this.subscriptionID);
        } catch (RuntimeException e) {
            throw new MonitorException("Failed to unsubscribe.", e);
        }

    }

    /**
     * @see wsmg.NotificationHandler#handleNotification(java.lang.String)
     */
    public void handleNotification(String message) {
        try {
            String soapBody = WsmgUtil.getSoapBodyContent(message);
            XmlElement event = XMLUtil.stringToXmlElement(soapBody);
            handleEvent(new MonitorEvent(event), true, this.workflow.getGraph());

        } catch (XmlBuilderException e) {
            // Just log them because they can be unrelated messages sent to
            // this topic by accident.
            logger.warning("Could not parse received notification: " + message, e);
        } catch (RuntimeException e) {
            logger.warning("Failed to process notification: " + message, e);
        }
    }

    private void handleEvent(MonitorEvent event, boolean forward, Graph graph) {
        EventType type = event.getType();
        String nodeID = event.getNodeID();
        Node node = graph.getNode(nodeID);

        System.out.println(nodeID);

        // logger.finest("type: " + type);
        if (type == MonitorUtil.EventType.WORKFLOW_INVOKED) {
            workflowStarted(graph, forward);
        } else if (type == MonitorUtil.EventType.WORKFLOW_TERMINATED) {
            workflowFinished(graph, forward);
        } else if (type == EventType.INVOKING_SERVICE
        // TODO this should be removed when GPEL sends all notification
        // correctly.
                || type == EventType.SERVICE_INVOKED) {
            if (node == null) {
                logger.warning("There is no node that has ID, " + nodeID);
            } else {
                nodeStarted(node, forward);
            }
        } else if (type == MonitorUtil.EventType.RECEIVED_RESULT
        // TODO this should be removed when GPEL sends all notification
        // correctly.
                || type == EventType.SENDING_RESULT) {
            if (node == null) {
                logger.warning("There is no node that has ID, " + nodeID);
            } else {
                nodeFinished(node, forward);
            }
        } else if (type == EventType.INVOKING_SERVICE_FAILED || type == EventType.RECEIVED_FAULT
        // TODO
                || type == EventType.SENDING_FAULT || type == EventType.SENDING_RESPONSE_FAILED) {
            if (node == null) {
                logger.warning("There is no node that has ID, " + nodeID);
            } else {
                nodeFailed(node, forward);
            }
        } else if (type == MonitorUtil.EventType.RESOURCE_MAPPING) {
            if (node == null) {
                logger.warning("There is no node that has ID, " + nodeID);
            } else {
                // nodeResourceMapped(node, event.getEvent(), forward);
            }
        } else {
            // Ignore the rest.
        }
    }

    private void workflowStarted(Graph graph, boolean forward) {
        for (InputNode node : GraphUtil.getInputNodes(graph)) {
            if (forward) {
                finishNode(node);
            } else {
                resetNode(node);
            }
        }
    }

    private void workflowFinished(Graph graph, boolean forward) {
        for (OutputNode node : GraphUtil.getOutputNodes(graph)) {
            if (forward) {
                finishNode(node);
                finishPredecessorNodes(node);
            } else {
                resetNode(node);
            }
        }
    }

    private LinkedList<InputNode> getInputNodes(WSGraph graph) {
        List<NodeImpl> nodes = graph.getNodes();
        LinkedList<InputNode> inputNodes = new LinkedList<InputNode>();
        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof InputNode) {
                inputNodes.add((InputNode) nodeImpl);
            }
        }
        return inputNodes;
    }

    private LinkedList<OutputNode> getOutputNodes(WSGraph graph) {
        List<NodeImpl> nodes = graph.getNodes();
        LinkedList<OutputNode> outputNodes = new LinkedList<OutputNode>();
        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof OutputNode) {
                outputNodes.add((OutputNode) nodeImpl);
            }
        }
        return outputNodes;
    }

    private void nodeStarted(Node node, boolean forward) {
        if (forward) {
            executeNode(node);
            finishPredecessorNodes(node);
        } else {
            resetNode(node);
        }
    }

    private void nodeFinished(Node node, boolean forward) {
        if (forward) {
            finishNode(node);
            finishPredecessorNodes(node);
        } else {
            executeNode(node);
        }
    }

    private void nodeFailed(Node node, boolean forward) {
        if (forward) {
            failNode(node);
            finishPredecessorNodes(node);
        } else {
            executeNode(node);
        }
    }

    private void executeNode(Node node) {
        node.getGUI().setBodyColor(NodeState.EXECUTING.color);
    }

    private void finishNode(Node node) {
        node.getGUI().setBodyColor(NodeState.FINISHED.color);
    }

    private void failNode(Node node) {
        node.getGUI().setBodyColor(NodeState.FAILED.color);
    }

    private void resetNode(Node node) {
        node.getGUI().setBodyColor(NodeGUI.DEFAULT_BODY_COLOR);
        node.getGUI().resetTokens();
    }

    /**
     * Make preceding nodes done. This helps the monitoring GUI when a user subscribes from the middle of the workflow
     * execution.
     * 
     * @param node
     */
    private void finishPredecessorNodes(Node node) {
        for (Port inputPort : node.getInputPorts()) {
            for (Edge edge : inputPort.getEdges()) {
                Port fromPort = edge.getFromPort();
                if (!(fromPort instanceof EPRPort)) {
                    Node fromNode = fromPort.getNode();
                    finishNode(fromNode);
                    finishPredecessorNodes(fromNode);
                }
            }
        }
        Port controlInPort = node.getControlInPort();
        if (controlInPort != null) {
            for (Node fromNode : controlInPort.getFromNodes()) {
                finishNode(fromNode);
                finishPredecessorNodes(fromNode);
            }
        }
    }
}