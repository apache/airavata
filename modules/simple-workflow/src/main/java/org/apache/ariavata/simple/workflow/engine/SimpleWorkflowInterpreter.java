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

package org.apache.ariavata.simple.workflow.engine;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
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
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.ApplicationNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.NodeState;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowOutputNode;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;
import org.apache.ariavata.simple.workflow.engine.parser.AiravataDefaultParser;
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

    private Map<String, WorkflowNode> readList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, WorkflowNode> waitingList = new ConcurrentHashMap<String, WorkflowNode>();
    private Map<String, ProcessPack> processingQueue = new ConcurrentHashMap<String, ProcessPack>();
    private Map<String, ProcessPack> completeList = new HashMap<String, ProcessPack>();
    private Registry registry;
    private EventBus eventBus = new EventBus();
    private List<WorkflowOutputNode> completeWorkflowOutputs = new ArrayList<WorkflowOutputNode>();

    public SimpleWorkflowInterpreter(String experimentId, String credentialToken) throws RegistryException {
        setExperiment(experimentId);
        this.credentialToken = credentialToken;
    }

    public SimpleWorkflowInterpreter(Experiment experiment, String credentialStoreToken) {
        // read the workflow file and build the topology to a DAG. Then execute that dag
        // get workflowInputNode list and start processing
        // next() will return ready task and block the thread if no task in ready state.
        this.experiment = experiment;
        this.credentialToken = credentialStoreToken;
    }


    public void launchWorkflow() throws Exception {
        // process workflow input nodes
//        WorkflowFactoryImpl wfFactory = WorkflowFactoryImpl.getInstance();
//        WorkflowParser workflowParser = wfFactory.getWorkflowParser(experiment.getExperimentID(), credentialToken);
        WorkflowParser workflowParser = new AiravataDefaultParser(experiment, credentialToken);
        log.debug("Initialized workflow parser");
        setWorkflowInputNodes(workflowParser.parse());
        log.debug("Parsed the workflow and got the workflow input nodes");
        processWorkflowInputNodes(getWorkflowInputNodes());
//        processReadyList();
        // process workflow application nodes
        // process workflow output nodes
    }

    // try to remove synchronization tag
    private synchronized void processReadyList() {
        for (WorkflowNode readyNode : readList.values()) {
            try {
                if (readyNode instanceof WorkflowOutputNode) {
                    WorkflowOutputNode wfOutputNode = (WorkflowOutputNode) readyNode;
                    completeWorkflowOutputs.add(wfOutputNode);
                    continue;
                }
                WorkflowNodeDetails workflowNodeDetails = createWorkflowNodeDetails(readyNode);
                TaskDetails process = getProcess(workflowNodeDetails);
                ProcessPack processPack = new ProcessPack(readyNode, workflowNodeDetails, process);
                addToProcessingQueue(processPack);
//                publishToProcessQueue(process);
                publishToProcessQueue(processPack);
            } catch (RegistryException e) {
                // FIXME : handle this exception
            }
        }
    }

    private void publishToProcessQueue(TaskDetails process) {
        Thread thread = new Thread(new TempPublisher(process, eventBus));
        thread.start();
        //TODO: publish to process queue.
    }

    // TODO : remove this test method
    private void publishToProcessQueue(ProcessPack process) {
        WorkflowNode workflowNode = process.getWorkflowNode();
        if (workflowNode instanceof ApplicationNode) {
            ApplicationNode applicationNode = (ApplicationNode) workflowNode;
            List<InPort> inputPorts = applicationNode.getInputPorts();
            if (applicationNode.getNodeName().equals("Add")) {
                applicationNode.getOutputPorts().get(0).getOutputObject().setValue(String.valueOf(
                        Integer.parseInt(inputPorts.get(0).getInputObject().getValue()) + Integer.parseInt(inputPorts.get(1).getInputObject().getValue())));
            } else if (applicationNode.getNodeName().equals("Multiply")) {
                applicationNode.getOutputPorts().get(0).getOutputObject().setValue(String.valueOf(
                        Integer.parseInt(inputPorts.get(0).getInputObject().getValue()) * Integer.parseInt(inputPorts.get(1).getInputObject().getValue())));
            } else if (applicationNode.getNodeName().equals("Subtract")) {
                applicationNode.getOutputPorts().get(0).getOutputObject().setValue(String.valueOf(
                        Integer.parseInt(inputPorts.get(0).getInputObject().getValue()) - Integer.parseInt(inputPorts.get(1).getInputObject().getValue())));
            } else {
                throw new RuntimeException("Invalid Application name");
            }

            for (Edge edge : applicationNode.getOutputPorts().get(0).getOutEdges()) {
                WorkflowUtil.copyValues(applicationNode.getOutputPorts().get(0).getOutputObject(), edge.getToPort().getInputObject());
                if (edge.getToPort().getNode().isReady()) {
                    addToReadyQueue(edge.getToPort().getNode());
                } else {
                    addToWaitingQueue(edge.getToPort().getNode());
                }
            }
        } else if (workflowNode instanceof WorkflowOutputNode) {
            WorkflowOutputNode wfOutputNode = (WorkflowOutputNode) workflowNode;
            throw new RuntimeException("Workflow output node in processing queue");
        }

        processingQueue.remove(process.getTaskDetails().getTaskID());
    }

    private TaskDetails getProcess(WorkflowNodeDetails wfNodeDetails) throws RegistryException {
        // create workflow taskDetails from workflowNodeDetails
        TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromWorkflowNodeDetails(getExperiment(), wfNodeDetails);
        taskDetails.setTaskID(getRegistry()
                .add(ChildDataType.TASK_DETAIL, taskDetails, wfNodeDetails.getNodeInstanceId()).toString());
        return taskDetails;
    }

    private WorkflowNodeDetails createWorkflowNodeDetails(WorkflowNode readyNode) throws RegistryException {
        WorkflowNodeDetails wfNodeDetails = ExperimentModelUtil.createWorkflowNode(readyNode.getNodeId(), null);
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
                log.debug("Workflow node : " + wfInputNode.getNodeId() + " is ready to execute");
                for (Edge edge : wfInputNode.getOutPort().getOutEdges()) {
                    edge.getToPort().getInputObject().setValue(wfInputNode.getInputObject().getValue());
                    if (edge.getToPort().getNode().isReady()) {
                        addToReadyQueue(edge.getToPort().getNode());
                        log.debug("Added workflow node : " + edge.getToPort().getNode().getNodeId() + " to the readyQueue");
                    } else {
                        addToWaitingQueue(edge.getToPort().getNode());
                        log.debug("Added workflow node " + edge.getToPort().getNode().getNodeId() + " to the waitingQueue");

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


    private List<WorkflowInputNode> parseWorkflowDescription(){
        return null;
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

    @Subscribe
    public void taskOutputChanged(TaskOutputChangeEvent taskOutputEvent){
        String taskId = taskOutputEvent.getTaskIdentity().getTaskId();
        log.debug("Task Output changed event received for workflow node : " +
                taskOutputEvent.getTaskIdentity().getWorkflowNodeId() + ", task : " + taskId);
        ProcessPack processPack = processingQueue.get(taskId);
        Set<WorkflowNode> tempWfNodeSet = new HashSet<WorkflowNode>();
        if (processPack != null) {
            WorkflowNode workflowNode = processPack.getWorkflowNode();
            if (workflowNode instanceof ApplicationNode) {
                ApplicationNode applicationNode = (ApplicationNode) workflowNode;
                // Workflow node can have one to many output ports and each output port can have one to many links
                for (OutPort outPort : applicationNode.getOutputPorts()) {
                    for (OutputDataObjectType outputDataObjectType : taskOutputEvent.getOutput()) {
                        if (outPort.getOutputObject().getName().equals(outputDataObjectType.getName())) {
                            outPort.getOutputObject().setValue(outputDataObjectType.getValue());
                            break;
                        }
                    }
                    for (Edge edge : outPort.getOutEdges()) {
                        WorkflowUtil.copyValues(outPort.getOutputObject(), edge.getToPort().getInputObject());
                        if (edge.getToPort().getNode().isReady()) {
                            addToReadyQueue(edge.getToPort().getNode());
                        }
                    }
                }
            }
            processingQueue.remove(taskId);
            log.debug("removed task from processing queue : " + taskId);
        }

    }

    @Subscribe
    public void taskStatusChanged(TaskStatusChangeEvent taskStatus){
        String taskId = taskStatus.getTaskIdentity().getTaskId();
        ProcessPack processPack = processingQueue.get(taskId);
        if (processPack != null) {
            WorkflowNodeState wfNodeState = WorkflowNodeState.UNKNOWN;
            switch (taskStatus.getState()) {
                case WAITING:
                    break;
                case STARTED:
                    break;
                case PRE_PROCESSING:
                    processPack.getWorkflowNode().setNodeState(NodeState.PRE_PROCESSING);
                    break;
                case INPUT_DATA_STAGING:
                    processPack.getWorkflowNode().setNodeState(NodeState.PRE_PROCESSING);
                    break;
                case EXECUTING:
                    processPack.getWorkflowNode().setNodeState(NodeState.EXECUTING);
                    break;
                case OUTPUT_DATA_STAGING:
                    processPack.getWorkflowNode().setNodeState(NodeState.POST_PROCESSING);
                    break;
                case POST_PROCESSING:
                    processPack.getWorkflowNode().setNodeState(NodeState.POST_PROCESSING);
                    break;
                case COMPLETED:
                    processPack.getWorkflowNode().setNodeState(NodeState.EXECUTED);
                    break;
                case FAILED:
                    processPack.getWorkflowNode().setNodeState(NodeState.FAILED);
                    break;
                case UNKNOWN:
                    break;
                case CONFIGURING_WORKSPACE:
                    break;
                case CANCELED:
                case CANCELING:
                    processPack.getWorkflowNode().setNodeState(NodeState.FAILED);
                    break;
                default:
                    break;
            }
            if (wfNodeState != WorkflowNodeState.UNKNOWN) {
                try {
                    updateWorkflowNodeStatus(processPack.getWfNodeDetails(), wfNodeState);
                } catch (RegistryException e) {
                    // TODO: handle this.
                }
            }
        }

    }

    /**
     * Remove the workflow node from waiting queue and add it to the ready queue.
     * @param workflowNode - Workflow Node
     */
    private synchronized void addToReadyQueue(WorkflowNode workflowNode) {
        waitingList.remove(workflowNode.getNodeId());
        readList.put(workflowNode.getNodeId(), workflowNode);
    }

    private void addToWaitingQueue(WorkflowNode workflowNode) {
        waitingList.put(workflowNode.getNodeId(), workflowNode);
    }

    /**
     * First remove the node from ready list and then add the WfNodeContainer to the process queue.
     * Note that underline data structure of the process queue is a Map.
     * @param processPack - has both workflow and correspond workflowNodeDetails and TaskDetails
     */
    private synchronized void addToProcessingQueue(ProcessPack processPack) {
        readList.remove(processPack.getWorkflowNode().getNodeId());
        processingQueue.put(processPack.getTaskDetails().getTaskID(), processPack);
    }

    private synchronized void addToCompleteQueue(ProcessPack processPack) {
        processingQueue.remove(processPack.getTaskDetails().getTaskID());
        completeList.put(processPack.getTaskDetails().getTaskID(), processPack);
    }


    @Override
    public void run() {
        // TODO: Auto generated method body.
        try {
            log.debug("Launching workflow");
            launchWorkflow();
            while (!(waitingList.isEmpty() && readList.isEmpty())) {
                processReadyList();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setExperiment(String experimentId) throws RegistryException {
        experiment = (Experiment) getRegistry().get(RegistryModelType.EXPERIMENT, experimentId);
        log.debug("Retrieve Experiment for experiment id : " + experimentId);
    }


    class TempPublisher implements Runnable {
        private TaskDetails tempTaskDetails;
        private EventBus tempEventBus;

        public TempPublisher(TaskDetails tempTaskDetails, EventBus tempEventBus) {
            this.tempTaskDetails = tempTaskDetails;
            this.tempEventBus = tempEventBus;
        }

        @Override
        public void run() {
            try {
                TaskIdentifier identifier = new TaskIdentifier(tempTaskDetails.getTaskID(), null, null, null);
                TaskStatusChangeEvent statusChangeEvent = new TaskStatusChangeEvent(TaskState.PRE_PROCESSING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.WAITING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.INPUT_DATA_STAGING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.STARTED, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.EXECUTING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.POST_PROCESSING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.OUTPUT_DATA_STAGING, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);
                statusChangeEvent = new TaskStatusChangeEvent(TaskState.COMPLETED, identifier);
                tempEventBus.post(statusChangeEvent);
                Thread.sleep(1000);

                List<InputDataObjectType> applicationInputs = tempTaskDetails.getApplicationInputs();
                List<OutputDataObjectType> applicationOutputs = tempTaskDetails.getApplicationOutputs();
                log.info("**************   Task output change event fired for application id :" + tempTaskDetails.getApplicationId());
                if (tempTaskDetails.getApplicationId().equals("Add") || tempTaskDetails.getApplicationId().equals("Add_2")) {
                    applicationOutputs.get(0).setValue((Integer.parseInt(applicationInputs.get(0).getValue()) +
                            Integer.parseInt(applicationInputs.get(1).getValue())) + "");
                } else if (tempTaskDetails.getApplicationId().equals("Subtract")) {
                    applicationOutputs.get(0).setValue((Integer.parseInt(applicationInputs.get(0).getValue()) -
                            Integer.parseInt(applicationInputs.get(1).getValue())) + "");
                } else if (tempTaskDetails.getApplicationId().equals("Multiply")) {
                    applicationOutputs.get(0).setValue((Integer.parseInt(applicationInputs.get(0).getValue()) *
                            Integer.parseInt(applicationInputs.get(1).getValue())) + "");
                }
                TaskOutputChangeEvent taskOutputChangeEvent = new TaskOutputChangeEvent(applicationOutputs, identifier);
                eventBus.post(taskOutputChangeEvent);

            } catch (InterruptedException e) {
                log.error("Thread was interrupted while sleeping");
            }

        }
    }
}
