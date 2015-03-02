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

package org.apache.airavata.simple.workflow.engine;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQProcessPublisher;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.ExecutionUnit;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.airavata.simple.workflow.engine.dag.nodes.ApplicationNode;
import org.apache.airavata.simple.workflow.engine.dag.nodes.NodeState;
import org.apache.airavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.airavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.airavata.simple.workflow.engine.dag.nodes.WorkflowOutputNode;
import org.apache.airavata.simple.workflow.engine.dag.port.InPort;
import org.apache.airavata.simple.workflow.engine.dag.port.OutPort;
import org.apache.airavata.simple.workflow.engine.parser.AiravataWorkflowParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleWorkflowInterpreter implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(SimpleWorkflowInterpreter.class);
    private List<WorkflowInputNode> workflowInputNodes;

    private Experiment experiment;

    private String credentialToken;

    private String gatewayName;

    private Map<String, WorkflowNode> readList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, WorkflowNode> waitingList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, ProcessContext> processingQueue = new ConcurrentHashMap<String, ProcessContext>();
    private Map<String, ProcessContext> completeList = new HashMap<String, ProcessContext>();
    private Registry registry;
    private List<WorkflowOutputNode> completeWorkflowOutputs = new ArrayList<WorkflowOutputNode>();
    private RabbitMQProcessPublisher publisher;
    private RabbitMQStatusConsumer statusConsumer;
    private String consumerId;

    public SimpleWorkflowInterpreter(String experimentId, String credentialToken, String gatewayName, RabbitMQProcessPublisher publisher) throws RegistryException {
        this.gatewayName = gatewayName;
        setExperiment(experimentId);
        this.credentialToken = credentialToken;
        this.publisher = publisher;
    }

    public SimpleWorkflowInterpreter(Experiment experiment, String credentialStoreToken, String gatewayName, RabbitMQProcessPublisher publisher) {
        this.gatewayName = gatewayName;
        this.experiment = experiment;
        this.credentialToken = credentialStoreToken;
        this.publisher = publisher;
    }


    public void launchWorkflow() throws Exception {
        WorkflowFactoryImpl wfFactory = WorkflowFactoryImpl.getInstance();
        WorkflowParser workflowParser = wfFactory.getWorkflowParser(experiment.getExperimentID(), credentialToken);
        log.debug("Initialized workflow parser");
        setWorkflowInputNodes(workflowParser.parse());
        log.debug("Parsed the workflow and got the workflow input nodes");
        // process workflow input nodes
        processWorkflowInputNodes(getWorkflowInputNodes());


        statusConsumer = new RabbitMQStatusConsumer();
        consumerId = statusConsumer.listen(new TaskMessageHandler());
    }

    // try to remove synchronization tag
    private synchronized void processReadyList() {
        for (WorkflowNode readyNode : readList.values()) {
            try {
                if (readyNode instanceof WorkflowOutputNode) {
                    WorkflowOutputNode wfOutputNode = (WorkflowOutputNode) readyNode;
                    wfOutputNode.getOutputObject().setValue(wfOutputNode.getInPort().getInputObject().getValue());
                    addToCompleteOutputNodeList(wfOutputNode);
                    continue;
                }
                WorkflowNodeDetails workflowNodeDetails = createWorkflowNodeDetails(readyNode);
                TaskDetails process = getProcess(workflowNodeDetails);
                ProcessContext processContext = new ProcessContext(readyNode, workflowNodeDetails, process);
                addToProcessingQueue(processContext);
                publishToProcessQueue(process);
//                publishToProcessQueue(processPack);
            } catch (RegistryException e) {
                // FIXME : handle this exception
            } catch (AiravataException e) {
                log.error("Error while publishing process to the process queue");
            }
        }
    }


    private void publishToProcessQueue(TaskDetails process) throws AiravataException {
        ProcessSubmitEvent processSubmitEvent = new ProcessSubmitEvent();
        processSubmitEvent.setCredentialToken(credentialToken);
        processSubmitEvent.setTaskId(process.getTaskID());
        MessageContext messageContext = new MessageContext(processSubmitEvent, MessageType.TASK, process.getTaskID(), null);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        publisher.publish(messageContext);


//        Thread thread = new Thread(new TempPublisher(process, eventBus));
//        thread.start();
        //TODO: publish to process queue.
    }

    private TaskDetails getProcess(WorkflowNodeDetails wfNodeDetails) throws RegistryException {
        // create workflow taskDetails from workflowNodeDetails
        TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromWorkflowNodeDetails(getExperiment(), wfNodeDetails);
        taskDetails.setTaskID(getRegistry()
                .add(ChildDataType.TASK_DETAIL, taskDetails, wfNodeDetails.getNodeInstanceId()).toString());
        return taskDetails;
    }

    private WorkflowNodeDetails createWorkflowNodeDetails(WorkflowNode readyNode) throws RegistryException {
        WorkflowNodeDetails wfNodeDetails = ExperimentModelUtil.createWorkflowNode(readyNode.getId(), null);
        ExecutionUnit executionUnit = ExecutionUnit.APPLICATION;
        String executionData = null;
        if (readyNode instanceof ApplicationNode) {
            executionUnit = ExecutionUnit.APPLICATION;
            executionData = ((ApplicationNode) readyNode).getApplicationId();
        } else if (readyNode instanceof WorkflowInputNode) {
            executionUnit = ExecutionUnit.INPUT;
        } else if (readyNode instanceof WorkflowOutputNode) {
            executionUnit = ExecutionUnit.OUTPUT;
        }
        wfNodeDetails.setExecutionUnit(executionUnit);
        wfNodeDetails.setExecutionUnitData(executionData);
        setupNodeDetailsInput(readyNode, wfNodeDetails);
        wfNodeDetails.setNodeInstanceId((String) getRegistry()
                .add(ChildDataType.WORKFLOW_NODE_DETAIL, wfNodeDetails, getExperiment().getExperimentID()));
//        nodeInstanceList.put(node, wfNodeDetails);
        return wfNodeDetails;
    }

    private void setupNodeDetailsInput(WorkflowNode readyNode, WorkflowNodeDetails wfNodeDetails) {
        if (readyNode instanceof ApplicationNode) {
            ApplicationNode applicationNode = (ApplicationNode) readyNode;
            if (applicationNode.isReady()) {
                for (InPort inPort : applicationNode.getInputPorts()) {
                    wfNodeDetails.addToNodeInputs(inPort.getInputObject());
                }
            } else {
                // TODO: handle this scenario properly.
            }
        } else {
            // TODO: do we support for other type of workflow nodes ?
        }
    }


    private void processWorkflowInputNodes(List<WorkflowInputNode> wfInputNodes) {
        Set<WorkflowNode> tempNodeSet = new HashSet<WorkflowNode>();
        for (WorkflowInputNode wfInputNode : wfInputNodes) {
            if (wfInputNode.isReady()) {
                log.debug("Workflow node : " + wfInputNode.getId() + " is ready to execute");
                for (Edge edge : wfInputNode.getOutPort().getOutEdges()) {
                    edge.getToPort().getInputObject().setValue(wfInputNode.getInputObject().getValue());
                    if (edge.getToPort().getNode().isReady()) {
                        addToReadyQueue(edge.getToPort().getNode());
                        log.debug("Added workflow node : " + edge.getToPort().getNode().getId() + " to the readyQueue");
                    } else {
                        addToWaitingQueue(edge.getToPort().getNode());
                        log.debug("Added workflow node " + edge.getToPort().getNode().getId() + " to the waitingQueue");

                    }
                }
            }
        }
    }


    public List<WorkflowInputNode> getWorkflowInputNodes() throws Exception {
        return workflowInputNodes;
    }

    public void setWorkflowInputNodes(List<WorkflowInputNode> workflowInputNodes) {
        this.workflowInputNodes = workflowInputNodes;
    }

    private Registry getRegistry() throws RegistryException {
        if (registry==null){
            registry = RegistryFactory.getDefaultRegistry();
        }
        return registry;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    private void updateWorkflowNodeStatus(WorkflowNodeDetails wfNodeDetails, WorkflowNodeState state) throws RegistryException{
        WorkflowNodeStatus status = ExperimentModelUtil.createWorkflowNodeStatus(state);
        wfNodeDetails.setWorkflowNodeStatus(status);
        getRegistry().update(RegistryModelType.WORKFLOW_NODE_STATUS, status, wfNodeDetails.getNodeInstanceId());
    }

    /**
     * Remove the workflow node from waiting queue and add it to the ready queue.
     * @param workflowNode - Workflow Node
     */
    private synchronized void addToReadyQueue(WorkflowNode workflowNode) {
        waitingList.remove(workflowNode.getId());
        readList.put(workflowNode.getId(), workflowNode);
    }

    private void addToWaitingQueue(WorkflowNode workflowNode) {
        waitingList.put(workflowNode.getId(), workflowNode);
    }

    /**
     * First remove the node from ready list and then add the WfNodeContainer to the process queue.
     * Note that underline data structure of the process queue is a Map.
     * @param processContext - has both workflow and correspond workflowNodeDetails and TaskDetails
     */
    private synchronized void addToProcessingQueue(ProcessContext processContext) {
        readList.remove(processContext.getWorkflowNode().getId());
        processingQueue.put(processContext.getTaskDetails().getTaskID(), processContext);
    }

    private synchronized void addToCompleteQueue(ProcessContext processContext) {
        processingQueue.remove(processContext.getTaskDetails().getTaskID());
        completeList.put(processContext.getTaskDetails().getTaskID(), processContext);
    }


    private void addToCompleteOutputNodeList(WorkflowOutputNode wfOutputNode) {
        completeWorkflowOutputs.add(wfOutputNode);
        readList.remove(wfOutputNode.getId());
    }

    @Override
    public void run() {
        try {
            log.debug("Launching workflow");
            launchWorkflow();
            while (!(waitingList.isEmpty() && readList.isEmpty())) {
                processReadyList();
                Thread.sleep(1000);
            }
            log.info("Successfully launched workflow for experiment : " + getExperiment().getExperimentID());
            statusConsumer.stopListen(consumerId);
            log.info("Successfully un-bind status consumer for experiment " + getExperiment().getExperimentID());
        } catch (Exception e) {
            log.error("Error launching workflow", e);
        }
    }

    private void setExperiment(String experimentId) throws RegistryException {
        experiment = (Experiment) getRegistry().get(RegistryModelType.EXPERIMENT, experimentId);
        log.debug("Retrieve Experiment for experiment id : " + experimentId);
    }

    class TaskMessageHandler implements MessageHandler{

        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            String gatewayId = "*";
            String experimentId = getExperiment().getExperimentID();
            List<String> routingKeys = new ArrayList<String>();
//            routingKeys.add(gatewayName+ "." + getExperiment().getExperimentID() + ".*");
            routingKeys.add(gatewayId);
            routingKeys.add(gatewayId + "." + experimentId);
            routingKeys.add(gatewayId + "." + experimentId+ ".*");
            routingKeys.add(gatewayId + "." + experimentId+ ".*.*");
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
            return props;
        }

        @Override
        public void onMessage(MessageContext msgCtx) {
            String message;
            if (msgCtx.getType() == MessageType.TASK) {
                TaskStatusChangeEvent event = (TaskStatusChangeEvent) msgCtx.getEvent();
                TaskIdentifier taskIdentifier = event.getTaskIdentity();
                handleTaskStatusChangeEvent(event);
                message = "Received task output change event , expId : " + taskIdentifier.getExperimentId() + ", taskId : " + taskIdentifier.getTaskId() + ", workflow node Id : " + taskIdentifier.getWorkflowNodeId();
                log.debug(message);
            }else if (msgCtx.getType() == MessageType.TASKOUTPUT) {
                TaskOutputChangeEvent event = (TaskOutputChangeEvent) msgCtx.getEvent();
                TaskIdentifier taskIdentifier = event.getTaskIdentity();
                handleTaskOutputChangeEvent(event);
                message = "Received task output change event , expId : " + taskIdentifier.getExperimentId() + ", taskId : " + taskIdentifier.getTaskId() + ", workflow node Id : " + taskIdentifier.getWorkflowNodeId();
                log.debug(message);
            } else {
                // not interesting, ignores
            }
        }

        private void handleTaskOutputChangeEvent(TaskOutputChangeEvent taskOutputChangeEvent) {

            String taskId = taskOutputChangeEvent.getTaskIdentity().getTaskId();
            log.debug("Task Output changed event received for workflow node : " +
                    taskOutputChangeEvent.getTaskIdentity().getWorkflowNodeId() + ", task : " + taskId);
            ProcessContext processContext = processingQueue.get(taskId);
            Set<WorkflowNode> tempWfNodeSet = new HashSet<WorkflowNode>();
            if (processContext != null) {
                WorkflowNode workflowNode = processContext.getWorkflowNode();
                if (workflowNode instanceof ApplicationNode) {
                    ApplicationNode applicationNode = (ApplicationNode) workflowNode;
                    // Workflow node can have one to many output ports and each output port can have one to many links
                    for (OutPort outPort : applicationNode.getOutputPorts()) {
                        for (OutputDataObjectType outputDataObjectType : taskOutputChangeEvent.getOutput()) {
                            if (outPort.getOutputObject().getName().equals(outputDataObjectType.getName())) {
                                outPort.getOutputObject().setValue(outputDataObjectType.getValue());
                                break;
                            }
                        }
                        for (Edge edge : outPort.getOutEdges()) {
                            edge.getToPort().getInputObject().setValue(outPort.getOutputObject().getValue());
                            if (edge.getToPort().getNode().isReady()) {
                                addToReadyQueue(edge.getToPort().getNode());
                            }
                        }
                    }
                }
                addToCompleteQueue(processContext);
                log.debug("removed task from processing queue : " + taskId);
            }
        }

        private void handleTaskStatusChangeEvent(TaskStatusChangeEvent taskStatusChangeEvent) {
            TaskState taskState = taskStatusChangeEvent.getState();
            TaskIdentifier taskIdentity = taskStatusChangeEvent.getTaskIdentity();
            String taskId = taskIdentity.getTaskId();
            ProcessContext processContext = processingQueue.get(taskId);
            if (processContext != null) {
                WorkflowNodeState wfNodeState = WorkflowNodeState.UNKNOWN;
                switch (taskState) {
                    case WAITING:
                        break;
                    case STARTED:
                        break;
                    case PRE_PROCESSING:
                        processContext.getWorkflowNode().setState(NodeState.PRE_PROCESSING);
                        break;
                    case INPUT_DATA_STAGING:
                        processContext.getWorkflowNode().setState(NodeState.PRE_PROCESSING);
                        break;
                    case EXECUTING:
                        processContext.getWorkflowNode().setState(NodeState.EXECUTING);
                        break;
                    case OUTPUT_DATA_STAGING:
                        processContext.getWorkflowNode().setState(NodeState.POST_PROCESSING);
                        break;
                    case POST_PROCESSING:
                        processContext.getWorkflowNode().setState(NodeState.POST_PROCESSING);
                        break;
                    case COMPLETED:
                        processContext.getWorkflowNode().setState(NodeState.EXECUTED);
                        break;
                    case FAILED:
                        processContext.getWorkflowNode().setState(NodeState.FAILED);
                        break;
                    case UNKNOWN:
                        break;
                    case CONFIGURING_WORKSPACE:
                        break;
                    case CANCELED:
                    case CANCELING:
                        processContext.getWorkflowNode().setState(NodeState.FAILED);
                        break;
                    default:
                        break;
                }
                if (wfNodeState != WorkflowNodeState.UNKNOWN) {
                    try {
                        updateWorkflowNodeStatus(processContext.getWfNodeDetails(), wfNodeState);
                    } catch (RegistryException e) {
                        // TODO: handle this.
                    }
                }
            }

        }
    }

}
