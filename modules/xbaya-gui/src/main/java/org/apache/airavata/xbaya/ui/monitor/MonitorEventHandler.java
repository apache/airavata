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
package org.apache.airavata.xbaya.ui.monitor;

import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.experiment.WorkflowNodeState;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.messaging.EventData;
import org.apache.airavata.xbaya.messaging.EventDataRepository;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MonitorEventHandler implements ChangeListener {

    /**
     * The state of a node
     */
    public static enum NodeState {

        /**
         * FINISHED
         */
        FINISHED(Color.GRAY),

        /**
         * EXECUTING
         */
        EXECUTING(Color.GREEN),

        /**
         * FAILED
         */
        FAILED(Color.RED),

        /**
         * DEFAULT COLOR
         */
        DEFAULT(NodeGUI.DEFAULT_BODY_COLOR);


        /**
         * color
         */
        final public Color color;

        private NodeState(Color color) {
            this.color = color;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(MonitorEventHandler.class);

    private XBayaGUI xbayaGUI;

    private int sliderValue;

    private Collection<URI> incorrectWorkflowIDs;

    private Collection<URI> triedWorkflowIDs;

    private Map<Node, LinkedList<ResourcePaintable>> resourcePaintableMap;

//    private WorkflowStatusUpdater workflowStatusUpdater;

//    private WorkflowNodeStatusUpdater workflowNodeStatusUpdater;

    /**
     * Model MonitorEventHandler
     *
     * @param xbayaGUI
     */
    public MonitorEventHandler(XBayaGUI xbayaGUI) {
        this.xbayaGUI = xbayaGUI;
        this.incorrectWorkflowIDs = Collections.synchronizedSet(new HashSet<URI>());
        this.triedWorkflowIDs = Collections.synchronizedSet(new HashSet<URI>());
        this.resourcePaintableMap = new HashMap<Node, LinkedList<ResourcePaintable>>();
//        try {
//            if (this.getClass().getClassLoader().getResource("airavata-server.properties") != null) {
//                this.workflowNodeStatusUpdater =
//                        new WorkflowNodeStatusUpdater(XBayaUtil.getExperimentCatalog(this.getClass().getClassLoader().getResource("airavata-server.properties")));
//                this.workflowStatusUpdater =
//                        new WorkflowStatusUpdater(XBayaUtil.getExperimentCatalog(this.getClass().getClassLoader().getResource("airavata-server.properties")));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (RepositoryException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (URISyntaxException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent event) {
        try {
            Object source = event.getSource();
            if (source instanceof EventDataRepository) {
                handleChange((EventDataRepository) source);
            }else if (source instanceof EventData) {
                EventData eventData = (EventData) source;
                if (eventData.getType() == MessageType.WORKFLOWNODE && eventData.getMessageId().startsWith("NODE")) {
                   handleEvent(eventData, true);
                }
            }else if (source instanceof String) {
                handleNewWorkflowStartEvent((String) source);

            }
        } catch (RuntimeException e) {
            // Don't want to pop up an error dialog every time XBaya received an
            // ill-formatted notification.
            logger.error(e.getMessage(), e);
        }
    }

    private void handleNewWorkflowStartEvent(String workflowName) {
        Workflow workflow;
        for (GraphCanvas graphCanvas : this.xbayaGUI.getGraphCanvases()) {
            workflow = graphCanvas.getWorkflow();
            if (workflow.getName().equals(workflowName)) {
                resetAllNode(workflow);
            }
        }

    }

    private void resetAllNode(Workflow workflow) {
        Graph graph = workflow.getGraph();
        for (Node node : graph.getNodes()) {
            resetNode(node);
        }
    }

    /**
     * @param model
     */
    private void handleChange(EventDataRepository model) {
        int newValue = model.getValue();

        if (model.getEventSize() == 0) {
            // The monitor was reset.
            resetAll();
        } else if (newValue > this.sliderValue) {
            // 3 -> 5
            // 3, 4
            for (int i = this.sliderValue; i < newValue; i++) {
                handleEvent(model.getEvent(i), true);
            }
        } else if (newValue < this.sliderValue) {
            // 5 -> 3
            // 4, 3
            for (int i = this.sliderValue - 1; i >= newValue; i--) {
                handleEvent(model.getEvent(i), false);
            }
        }
        this.sliderValue = newValue;

        // Repaints only the active canvas.
        this.xbayaGUI.getGraphCanvas().repaint();
    }

    private void handleEvent(EventData event, boolean forward) {
//        EventType type = event.getType();
        //todo currrently we do not set the workflowID properly its just node ID
//        URI workflowID = event.getWorkflowID();
        List<GraphCanvas> graphCanvases = this.xbayaGUI.getGraphCanvases();
        for (GraphCanvas graphCanvas : graphCanvases) {
            Workflow workflow = graphCanvas.getWorkflow();
//            URI instanceID = workflow.getGPELInstanceID();
//            if (instanceID == null) {
            // If the workflow doesn't have an instance ID, it's a template.
            // We handle it so that users can use a workflow template to
            // monitor a workflow too.
            // This is also needed in the case of jython workflow.
            handleEvent(event, forward, workflow.getGraph());
            graphCanvas.repaint();
//            } else if (instanceID.equals(workflowID)) {
//                This is the regular case.
//                found = true;
//                handleEvent(event, forward, workflow.getGraph());
//            } else if (null != workflowID
//                    && -1 != WSDLUtil.findWorkflowName(workflowID).indexOf(WSDLUtil.findWorkflowName(instanceID))) {
//                handleEvent(event, WSDLUtil.findWorkflowName(workflowID), workflow.getGraph());
//            }
        }

//        // Load a sub-workflow.
//        if (type == MonitorUtil.EventType.WORKFLOW_INITIALIZED) {
//            if (forward) {
//                // Check if the workflow instance is already open.
//                for (GraphCanvas graphCanvas : graphCanvases) {
//                    Workflow workflow = graphCanvas.getWorkflow();
//                    URI instanceID = workflow.getGPELInstanceID();
//                    if (workflowID.equals(instanceID)) {
//                        // The workflow instance is already loaded.
//                        return;
//                    }
//                }
//                loadWorkflow(workflowID);
//            } else {
//                // Don't need to close the workflow when it's opened.
//            }
//        }
//
//        if (found == false && workflowID != null) {
//            // Loads the workflow instance ID in case a user started to monitor
//            // in the middle.
//            loadWorkflow(workflowID);
//        }
//
//        /*
//         * Handle resource mapping message which contains resource from Amazon EC2 Since workflowID (workflowName) from
//         * message and instanceID do not equal, so we have to handle it explicitly
//         */
//        if (type == EventType.RESOURCE_MAPPING && event.getMessage().contains("i-")) {
//            String nodeID = event.getNodeID();
//            for (GraphCanvas graphCanvas : graphCanvases) {
//                Node node = graphCanvas.getWorkflow().getGraph().getNode(nodeID);
//                if (node != null) {
//                    ControlPort control = node.getControlInPort();
//                    if (control != null) {
//                        Node fromNode = control.getFromNode();
//                        if (fromNode instanceof InstanceNode) {
//                            InstanceNode ec2Node = (InstanceNode) fromNode;
//
//                            /*
//                             * parse message and set output to InstanceNode
//                             */
//                            int start = event.getMessage().indexOf("i-");
//                            String instanceId = event.getMessage().substring(start, start + 10);
//                            ec2Node.setOutputInstanceId(instanceId);
//
//                            // make this node to not start a new instance
//                            ec2Node.setStartNewInstance(false);
//                            ec2Node.setInstanceId(instanceId);
//                            ec2Node.setAmiId(null);
//                        }
//                    }
//                }
//            }
//        }

        // TODO There is a possibility that XBaya misses to handle some
        // notification while a workflow is being loaded. Create a thread for
        // each workflow ID to handle notifications.
    }

    /**
     * @param graph
     * @return
     */
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

    /**
     * @param event
     * @param forward
     * @param graph
     */
    private void handleEvent(EventData event, boolean forward, Graph graph) {
//        EventType type = event.getType();
        String nodeID = event.getWorkflowNodeId();
        Node node = graph.getNode(nodeID);
        if (node != null){
            if (event.getStatus().equals(WorkflowNodeState.INVOKED.toString())) {
                invokeNode(node);
//            workflowStarted(graph, forward);
//            workflowStatusUpdater.workflowStarted(event.getExperimentID());
            } else if (event.getStatus().equals(WorkflowNodeState.COMPLETED.toString())) {
                nodeFinished(node, true);
//            workflowFinished(graph, forward);
//            workflowStatusUpdater.workflowFinished(event.getExperimentID());
            } else if (event.getStatus().equals(WorkflowNodeState.EXECUTING.toString())) {
                nodeStarted(node, forward);
//                workflowNodeStatusUpdater.workflowStarted(event.getExperimentID(), event.getNodeID());
            } else {
                // Ignore the rest.
            }
        }

    }

    /**
     * @param workflowInstanceID
     */
    private void loadWorkflow(final URI workflowInstanceID) {
        // To avoid to load a same workflow twice.
        if (this.triedWorkflowIDs.contains(workflowInstanceID)) {
            return;
        }
        this.triedWorkflowIDs.add(workflowInstanceID);

        new Thread() {
            @Override
            public void run() {
                loadWorkflowInThread(workflowInstanceID);
            }
        }.start();
    }

    private void loadWorkflowInThread(URI workflowInstanceID) {
        try {
            if (this.incorrectWorkflowIDs.contains(workflowInstanceID)) {
                // Do not try to load a workflow that failed before.
                return;
            }
            //There is not workflow client assigned in the engine. thus the following code is commented
//            WorkflowClient client = this.engine.getWorkflowClient();
//            Workflow loadedWorkflow = client.load(workflowInstanceID, WorkflowType.INSTANCE);
//            GraphCanvas canvas = this.xbayaGUI.newGraphCanvas(true);
//            canvas.setWorkflow(loadedWorkflow);
//        } catch (GraphException e) {
//            this.incorrectWorkflowIDs.add(workflowInstanceID);
//            logger.error(e.getMessage(), e);
//        } catch (WorkflowEngineException e) {
//            this.incorrectWorkflowIDs.add(workflowInstanceID);
//            logger.error(e.getMessage(), e);
//        } catch (ComponentException e) {
//            this.incorrectWorkflowIDs.add(workflowInstanceID);
//            logger.error(e.getMessage(), e);
        } catch (RuntimeException e) {
            this.incorrectWorkflowIDs.add(workflowInstanceID);
            logger.error(e.getMessage(), e);
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

    private void nodeStarted(Node node, boolean forward) {
        if (forward) {
            if (node.getState() != NodeExecutionState.FINISHED) {
                executeNode(node);
                finishPredecessorNodes(node);
            }
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

    private void resetAll() {
        List<GraphCanvas> graphCanvases = this.xbayaGUI.getGraphCanvases();
        for (GraphCanvas graphCanvas : graphCanvases) {
            Graph graph = graphCanvas.getGraph();
            for (Node node : graph.getNodes()) {
                resetNode(node);
            }
        }
    }

    private void executeNode(Node node) {
        node.setState(NodeExecutionState.EXECUTING);
    }

    private void invokeNode(Node node) {
        node.setState(NodeExecutionState.WAITING);
    }

    private void finishNode(Node node) {
        if (!NodeExecutionState.FAILED.equals(node.getState())) {
            node.setState(NodeExecutionState.FINISHED);
        }
    }

    private void failNode(Node node) {
        node.setState(NodeExecutionState.FAILED);
    }

    private void resetNode(Node node) {
        node.setState(NodeExecutionState.WAITING);
        NodeController.getGUI(node).resetTokens();
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
