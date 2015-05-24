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

package org.apache.airavata.workflow.core;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
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
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.NodeState;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowInputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowOutputNode;
import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.OutPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Package-Private class
 */
class SimpleWorkflowInterpreter{

    private static final Logger log = LoggerFactory.getLogger(SimpleWorkflowInterpreter.class);
    private List<WorkflowInputNode> workflowInputNodes;

    private Experiment experiment;

    private String credentialToken;

    private String gatewayName;

    private Map<String, WorkflowNode> readyList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, WorkflowNode> waitingList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, ProcessContext> processingQueue = new ConcurrentHashMap<String, ProcessContext>();
    private Map<String, ProcessContext> completeList = new HashMap<String, ProcessContext>();
    private Registry registry;
    private List<WorkflowOutputNode> completeWorkflowOutputs = new ArrayList<WorkflowOutputNode>();
    private RabbitMQProcessPublisher publisher;
    private RabbitMQStatusConsumer statusConsumer;
    private String consumerId;
    private boolean continueWorkflow = true;

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

    /**
     * Package-Private method.
     * @throws Exception
     */
    void launchWorkflow() throws Exception {
        WorkflowFactoryImpl wfFactory = WorkflowFactoryImpl.getInstance();
        WorkflowParser workflowParser = wfFactory.getWorkflowParser(experiment.getExperimentId(), credentialToken);
        log.debug("Initialized workflow parser");
        setWorkflowInputNodes(workflowParser.parse());
        log.debug("Parsed the workflow and got the workflow input nodes");
        // process workflow input nodes
        processWorkflowInputNodes(getWorkflowInputNodes());
        if (readyList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (WorkflowInputNode workflowInputNode : workflowInputNodes) {
                sb.append(", ");
                sb.append(workflowInputNode.getInputObject().getName());
                sb.append("=");
                sb.append(workflowInputNode.getInputObject().getValue());
            }
            throw new AiravataException("No workflow application node is in ready state to run with experiment inputs" + sb.toString());
        }
        processReadyList();
    }

    // try to remove synchronization tag
    /**
     * Package-Private method.
     * @throws RegistryException
     * @throws AiravataException
     */
    void processReadyList() throws RegistryException, AiravataException {
        if (readyList.isEmpty() && processingQueue.isEmpty() && !waitingList.isEmpty()) {
            throw new AiravataException("No workflow application node is in ready state to run");
        }
        for (WorkflowNode readyNode : readyList.values()) {
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
        }
        if (processingQueue.isEmpty()) {
            try {
                saveWorkflowOutputs();
            } catch (AppCatalogException e) {
                throw new AiravataException("Error while updating completed workflow outputs to registry", e);
            }
        }
    }

    private void saveWorkflowOutputs() throws AppCatalogException {
        List<OutputDataObjectType> outputDataObjects = new ArrayList<OutputDataObjectType>();
        for (WorkflowOutputNode completeWorkflowOutput : completeWorkflowOutputs) {
            outputDataObjects.add(completeWorkflowOutput.getOutputObject());
        }
        AppCatalogFactory.getAppCatalog().getWorkflowCatalog()
                .updateWorkflowOutputs(experiment.getApplicationId(), outputDataObjects);
    }


    private void publishToProcessQueue(TaskDetails process) throws AiravataException {
        ProcessSubmitEvent processSubmitEvent = new ProcessSubmitEvent();
        processSubmitEvent.setCredentialToken(credentialToken);
        processSubmitEvent.setTaskId(process.getTaskId());
        MessageContext messageContext = new MessageContext(processSubmitEvent, MessageType.TASK, process.getTaskId(), null);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        publisher.publish(messageContext);
    }

    private TaskDetails getProcess(WorkflowNodeDetails wfNodeDetails) throws RegistryException {
        // create workflow taskDetails from workflowNodeDetails
        TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromWorkflowNodeDetails(getExperiment(), wfNodeDetails);
        taskDetails.setTaskId(getRegistry()
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
            setupNodeDetailsInput(((ApplicationNode) readyNode), wfNodeDetails);
        } else if (readyNode instanceof WorkflowInputNode) {
            executionUnit = ExecutionUnit.INPUT;
        } else if (readyNode instanceof WorkflowOutputNode) {
            executionUnit = ExecutionUnit.OUTPUT;
        }
        wfNodeDetails.setExecutionUnit(executionUnit);
        wfNodeDetails.setExecutionUnitData(executionData);
        wfNodeDetails.setNodeInstanceId((String) getRegistry()
                .add(ChildDataType.WORKFLOW_NODE_DETAIL, wfNodeDetails, getExperiment().getExperimentId()));
        return wfNodeDetails;
    }

    private void setupNodeDetailsInput(ApplicationNode readyAppNode, WorkflowNodeDetails wfNodeDetails) {
        if (readyAppNode.isReady()) {
            for (InPort inPort : readyAppNode.getInputPorts()) {
                wfNodeDetails.addToNodeInputs(inPort.getInputObject());
            }
        } else {
            throw new IllegalArgumentException("Application node should be in ready state to set inputs to the " +
                    "workflow node details, nodeId = " + readyAppNode.getId());
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
     * Package-Private method.
     * Remove the workflow node from waiting queue and add it to the ready queue.
     * @param workflowNode - Workflow Node
     */
    synchronized void addToReadyQueue(WorkflowNode workflowNode) {
        waitingList.remove(workflowNode.getId());
        readyList.put(workflowNode.getId(), workflowNode);
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
        readyList.remove(processContext.getWorkflowNode().getId());
        processingQueue.put(processContext.getTaskDetails().getTaskId(), processContext);
    }

    private synchronized void addToCompleteQueue(ProcessContext processContext) {
        processingQueue.remove(processContext.getTaskDetails().getTaskId());
        completeList.put(processContext.getTaskDetails().getTaskId(), processContext);
    }


    private void addToCompleteOutputNodeList(WorkflowOutputNode wfOutputNode) {
        completeWorkflowOutputs.add(wfOutputNode);
        readyList.remove(wfOutputNode.getId());
    }

    boolean isAllDone() {
        return !continueWorkflow || (waitingList.isEmpty() && readyList.isEmpty() && processingQueue.isEmpty());
    }

    private void setExperiment(String experimentId) throws RegistryException {
        experiment = (Experiment) getRegistry().get(RegistryModelType.EXPERIMENT, experimentId);
        log.debug("Retrieve Experiment for experiment id : " + experimentId);
    }

    synchronized void handleTaskOutputChangeEvent(TaskOutputChangeEvent taskOutputChangeEvent) {

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
            try {
                processReadyList();
            } catch (Exception e) {
                log.error("Error while processing ready workflow nodes", e);
                continueWorkflow = false;
            }
        }
    }

    void handleTaskStatusChangeEvent(TaskStatusChangeEvent taskStatusChangeEvent) {
        TaskState taskState = taskStatusChangeEvent.getState();
        TaskIdentifier taskIdentity = taskStatusChangeEvent.getTaskIdentity();
        String taskId = taskIdentity.getTaskId();
        ProcessContext processContext = processingQueue.get(taskId);
        if (processContext != null) {
            WorkflowNodeState wfNodeState = WorkflowNodeState.INVOKED;
            switch (taskState) {
                case WAITING:
                    break;
                case STARTED:
                    break;
                case PRE_PROCESSING:
                    wfNodeState = WorkflowNodeState.INVOKED;
                    processContext.getWorkflowNode().setState(NodeState.PRE_PROCESSING);
                    break;
                case INPUT_DATA_STAGING:
                    wfNodeState = WorkflowNodeState.INVOKED;
                    processContext.getWorkflowNode().setState(NodeState.PRE_PROCESSING);
                    break;
                case EXECUTING:
                    wfNodeState = WorkflowNodeState.EXECUTING;
                    processContext.getWorkflowNode().setState(NodeState.EXECUTING);
                    break;
                case OUTPUT_DATA_STAGING:
                    wfNodeState = WorkflowNodeState.COMPLETED;
                    processContext.getWorkflowNode().setState(NodeState.POST_PROCESSING);
                    break;
                case POST_PROCESSING:
                    wfNodeState = WorkflowNodeState.COMPLETED;
                    processContext.getWorkflowNode().setState(NodeState.POST_PROCESSING);
                    break;
                case COMPLETED:
                    wfNodeState = WorkflowNodeState.COMPLETED;
                    processContext.getWorkflowNode().setState(NodeState.EXECUTED);
                    break;
                case FAILED:
                    wfNodeState = WorkflowNodeState.FAILED;
                    processContext.getWorkflowNode().setState(NodeState.FAILED);
                    break;
                case UNKNOWN:
                    wfNodeState = WorkflowNodeState.UNKNOWN;
                    break;
                case CONFIGURING_WORKSPACE:
                    wfNodeState = WorkflowNodeState.COMPLETED;
                    break;
                case CANCELED:
                case CANCELING:
                    wfNodeState = WorkflowNodeState.CANCELED;
                    processContext.getWorkflowNode().setState(NodeState.FAILED);
                    break;
                default:
                    break;
            }
            if (wfNodeState != WorkflowNodeState.UNKNOWN) {
                try {
                    updateWorkflowNodeStatus(processContext.getWfNodeDetails(), wfNodeState);
                } catch (RegistryException e) {
                    log.error("Error while updating workflow node status update to the registry. nodeInstanceId :"
                            + processContext.getWfNodeDetails().getNodeInstanceId() + " status to: "
                            + processContext.getWfNodeDetails().getWorkflowNodeStatus().toString() , e);
                }
            }
        }

    }
}
