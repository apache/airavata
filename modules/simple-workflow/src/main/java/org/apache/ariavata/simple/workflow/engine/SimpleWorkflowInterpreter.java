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

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.ExecutionUnit;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.ApplicationNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.NodeState;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowOutputNode;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleWorkflowInterpreter implements Runnable{


    private List<WorkflowInputNode> workflowInputNodes;

    private Experiment experiment;

    private String credentialToken;

    private List<WorkflowNode> readList = new ArrayList<WorkflowNode>();
    private List<WorkflowNode> waitingList = new ArrayList<WorkflowNode>();
    private Map<String,WfNodeContainer> processingQueue = new HashMap<String, WfNodeContainer>();
    private List<WorkflowNode> completeList = new ArrayList<WorkflowNode>();
    private Registry registry;

    public SimpleWorkflowInterpreter(Experiment experiment, String credentialStoreToken) {
        // read the workflow file and build the topology to a DAG. Then execute that dag
        // get workflowInputNode list and start processing
        // next() will return ready task and block the thread if no task in ready state.
        this.experiment = experiment;
        this.credentialToken = credentialStoreToken;
    }


    public void launchWorkflow() throws Exception {
        // process workflow input nodes
        processWorkflowInputNodes(getWorkflowInputNodes());
        processReadyList();
        // process workflow application nodes
        // process workflow output nodes
    }

    // try to remove synchronization tag
    private synchronized void processReadyList() {
        for (WorkflowNode readyNode : readList) {
            try {
                WorkflowNodeDetails workflowNodeDetails = createWorkflowNodeDetails(readyNode);
                TaskDetails process = getProcess(workflowNodeDetails);
                processingQueue.put(process.getTaskID(), new WfNodeContainer(readyNode, workflowNodeDetails));
                publishToProcessQueue(process);
            } catch (RegistryException e) {
                // FIXME : handle this exception
            }
        }
    }

    private void publishToProcessQueue(TaskDetails process) {
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
            if (applicationNode.isSatisfy()) {
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
            if (wfInputNode.isSatisfy()) {

//                for (Edge edge : wfInputNode.getOutputLinks()) {
//                    WorkflowUtil.copyValues(wfInputNode.getInputObject(), edge.getToPort().getInputObject());
//                    tempNodeSet.add(edge.getToPort().getNode());
//                }
            }
        }
        for (WorkflowNode workflowNode : tempNodeSet) {
            if (workflowNode.isSatisfy()) {
                readList.add(workflowNode);
            } else {
                waitingList.add(workflowNode);
            }
        }
    }


    public List<WorkflowInputNode> getWorkflowInputNodes() throws Exception {
        if (workflowInputNodes == null) {
            // read workflow description from registry and parse it
            WorkflowFactoryImpl wfFactory = WorkflowFactoryImpl.getInstance();
            List<WorkflowInputNode> wfInputNodes = wfFactory.getWorkflowParser(experiment.getExperimentID(),
                    credentialToken).parse();
            setWorkflowInputNodes(wfInputNodes);
        }
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
        WfNodeContainer wfNodeContainer = processingQueue.get(taskId);
        Set<WorkflowNode> tempWfNodeSet = new HashSet<WorkflowNode>();
        if (wfNodeContainer != null) {
            WorkflowNode workflowNode = wfNodeContainer.getWorkflowNode();
            if (workflowNode instanceof ApplicationNode) {
                ApplicationNode applicationNode = (ApplicationNode) workflowNode;
                // Workflow node can have one to many output ports and each output port can have one to many links
                for (OutPort outPort : applicationNode.getOutputPorts()) {
//                    for (Edge edge : outPort.getOutputLinks()) {
//                        WorkflowUtil.copyValues(outPort.getOutputObject(), edge.getToPort().getInputObject());
//                        tempWfNodeSet.add(edge.getToPort().getNode());
//                    }
                }

                for (WorkflowNode node : tempWfNodeSet) {
                    if (node.isSatisfy()) {
                        waitingList.remove(node);
                        readList.add(node);
                    }
                }
            }
            processingQueue.remove(taskId);
            processReadyList();
        }

    }

    @Subscribe
    public void taskStatusChanged(TaskStatusChangeEvent taskStatus){
        String taskId = taskStatus.getTaskIdentity().getTaskId();
        WfNodeContainer wfNodeContainer = processingQueue.get(taskId);
        if (wfNodeContainer != null) {
            WorkflowNodeState wfNodeState = WorkflowNodeState.UNKNOWN;
            switch (taskStatus.getState()) {
                case WAITING:
                    break;
                case STARTED:
                    break;
                case PRE_PROCESSING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.PRE_PROCESSING);
                    break;
                case INPUT_DATA_STAGING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.PRE_PROCESSING);
                    break;
                case OUTPUT_DATA_STAGING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.POST_PROCESSING);
                    break;
                case EXECUTING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.EXECUTING);
                    break;
                case POST_PROCESSING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.POST_PROCESSING);
                    break;
                case COMPLETED:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.EXECUTED);
                    break;
                case FAILED:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.FAILED);
                    break;
                case UNKNOWN:
                    break;
                case CONFIGURING_WORKSPACE:
                    break;
                case CANCELED:
                case CANCELING:
                    wfNodeContainer.getWorkflowNode().setNodeState(NodeState.FAILED);
                    break;
                default:
                    break;
            }
            if (wfNodeState != WorkflowNodeState.UNKNOWN) {
                try {
                    updateWorkflowNodeStatus(wfNodeContainer.getWfNodeDetails(), wfNodeState);
                } catch (RegistryException e) {
                    // TODO: handle this.
                }
            }
        }

    }

    @Override
    public void run() {
        // TODO: Auto generated method body.
    }
}
