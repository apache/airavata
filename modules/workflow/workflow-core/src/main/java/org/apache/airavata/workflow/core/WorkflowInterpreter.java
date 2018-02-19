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
package org.apache.airavata.workflow.core;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.ComponentState;
import org.apache.airavata.model.ComponentStatus;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.WorkflowCatalog;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.parser.WorkflowParser;
import org.apache.thrift.TException;
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
class WorkflowInterpreter {

    private static final Logger log = LoggerFactory.getLogger(WorkflowInterpreter.class);
    private List<InputNode> inputNodes;

    private ExperimentModel experiment;

    private String credentialToken;

    private String gatewayName;

    private String workflowString;
    private Map<String, WorkflowNode> readyList = new ConcurrentHashMap<>();
    private Map<String, WorkflowNode> waitingList = new ConcurrentHashMap<>();
    private Map<String, WorkflowNode> processingQueue = new ConcurrentHashMap<>();
    private Map<String, WorkflowNode> completeList = new HashMap<>();
    private Registry registry;
    private List<OutputNode> completeWorkflowOutputs = new ArrayList<>();
    private Publisher publisher;
    private String consumerId;
    private boolean continueWorkflow = true;

    public WorkflowInterpreter(String experimentId, String credentialToken, String gatewayName, Publisher publisher) throws RegistryException, TException, ApplicationSettingsException {
        this.gatewayName = gatewayName;
        setExperiment(experimentId);
        this.credentialToken = credentialToken;
        this.publisher = publisher;
    }

    public WorkflowInterpreter(ExperimentModel experiment, String credentialStoreToken, String gatewayName, Publisher publisher) {
        this.gatewayName = gatewayName;
        this.experiment = experiment;
        this.credentialToken = credentialStoreToken;
        this.publisher = publisher;
    }

    /**
     * Package-Private method.
     *
     * @throws Exception
     */
    void launchWorkflow() throws Exception {
//        WorkflowBuilder workflowBuilder = WorkflowFactory.getWorkflowBuilder(experiment.getExperimentId(), credentialToken, null);
        workflowString = getWorkflow();
        WorkflowParser workflowParser = WorkflowFactory.getWorkflowParser(workflowString);
        log.debug("Initialized workflow parser");
        workflowParser.parse();
        setInputNodes(workflowParser.getInputNodes());
        log.debug("Parsed the workflow and got the workflow input nodes");
        // process workflow input nodes
        processWorkflowInputNodes(getInputNodes());
        if (readyList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (InputNode inputNode : inputNodes) {
                sb.append(", ");
                sb.append(inputNode.getInputObject().getName());
                sb.append("=");
                sb.append(inputNode.getInputObject().getValue());
            }
            throw new AiravataException("No workflow application node is in ready state to run with experiment inputs" + sb.toString());
        }
        processReadyList();
    }

    private String getWorkflow() throws AppCatalogException, WorkflowCatalogException {
        WorkflowCatalog workflowCatalog = RegistryFactory.getAppCatalog().getWorkflowCatalog();
        //FIXME: parse workflowTemplateId or experimentId
//        workflowCatalog.getWorkflow("");
        return "";
    }

    // try to remove synchronization tag

    /**
     * Package-Private method.
     *
     * @throws RegistryException
     * @throws AiravataException
     */
    void processReadyList() throws RegistryException, AiravataException {
        if (readyList.isEmpty() && processingQueue.isEmpty() && !waitingList.isEmpty()) {
            throw new AiravataException("No workflow application node is in ready state to run");
        }
        for (WorkflowNode readyNode : readyList.values()) {
            if (readyNode instanceof OutputNode) {
                OutputNode outputNode = (OutputNode) readyNode;
                outputNode.getOutputObject().setValue(outputNode.getInPort().getInputObject().getValue());
                addToCompleteOutputNodeList(outputNode);
            } else if (readyNode instanceof InputNode) {
                // FIXME: set input object of applications and add applications to ready List.
            } else if (readyNode instanceof ApplicationNode) {
                // FIXME:  call orchestrator to create process for the application
            } else {
                throw new RuntimeException("Unsupported workflow node type");
            }
        }

        if (processingQueue.isEmpty() && waitingList.isEmpty()) {
            try {
                saveWorkflowOutputs();
            } catch (AppCatalogException e) {
                throw new AiravataException("Error while updating completed workflow outputs to registry", e);
            }
        }
    }

    private void saveWorkflowOutputs() throws AppCatalogException {
        List<OutputDataObjectType> outputDataObjects = new ArrayList<>();
        for (OutputNode completeWorkflowOutput : completeWorkflowOutputs) {
            outputDataObjects.add(completeWorkflowOutput.getOutputObject());
        }
        // FIXME: save workflow output to registry.
//        RegistryFactory.getAppCatalog().getWorkflowCatalog()
//                .updateWorkflowOutputs(experiment.getApplicationId(), outputDataObjects);
    }

    private void processWorkflowInputNodes(List<InputNode> inputNodes) {
        Set<WorkflowNode> tempNodeSet = new HashSet<>();
        for (InputNode inputNode : inputNodes) {
            if (inputNode.isReady()) {
                log.debug("Workflow node : " + inputNode.getId() + " is ready to execute");
                for (Edge edge : inputNode.getOutPort().getEdges()) {
                    edge.getToPort().getInputObject().setValue(inputNode.getInputObject().getValue());
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


    public List<InputNode> getInputNodes() throws Exception {
        return inputNodes;
    }

    public void setInputNodes(List<InputNode> inputNodes) {
        this.inputNodes = inputNodes;
    }

    /**
     * Package-Private method.
     * Remove the workflow node from waiting queue and add it to the ready queue.
     *
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
     *
     * @param applicationNode - has both workflow and correspond workflowNodeDetails and TaskDetails
     */
    private synchronized void addToProcessingQueue(ApplicationNode applicationNode) {
        readyList.remove(applicationNode.getId());
        processingQueue.put(applicationNode.getId(), applicationNode);
    }

    private synchronized void addToCompleteQueue(ApplicationNode applicationNode) {
        processingQueue.remove(applicationNode.getId());
        completeList.put(applicationNode.getId(), applicationNode);
    }


    private void addToCompleteOutputNodeList(OutputNode wfOutputNode) {
        completeWorkflowOutputs.add(wfOutputNode);
        readyList.remove(wfOutputNode.getId());
    }

    boolean isAllDone() {
        return !continueWorkflow || (waitingList.isEmpty() && readyList.isEmpty() && processingQueue.isEmpty());
    }

    private void setExperiment(String experimentId) throws RegistryException, TException, ApplicationSettingsException {
        experiment = getRegistryServiceClient().getExperiment(experimentId);
        log.debug("Retrieve Experiment for experiment id : " + experimentId);
    }

/*    synchronized void handleTaskOutputChangeEvent(ProcessStatusChangeEvent taskOutputChangeEvent) {

        String taskId = taskOutputChangeEvent.getTaskIdentity().getTaskId();
        log.debug("Task Output changed event received for workflow node : " +
                taskOutputChangeEvent.getTaskIdentity().getWorkflowNodeId() + ", task : " + taskId);
        WorkflowNode workflowNode = processingQueue.get(taskId);
        Set<WorkflowNode> tempWfNodeSet = new HashSet<>();
        if (workflowNode != null) {
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
                    for (Edge edge : outPort.getEdges()) {
                        edge.getToPort().getInputObject().setValue(outPort.getOutputObject().getValue());
                        if (edge.getToPort().getNode().isReady()) {
                            addToReadyQueue(edge.getToPort().getNode());
                        }
                    }
                }
                addToCompleteQueue(applicationNode);
                log.debug("removed task from processing queue : " + taskId);
            }
            try {
                processReadyList();
            } catch (Exception e) {
                log.error("Error while processing ready workflow nodes", e);
                continueWorkflow = false;
            }
        }
    }*/

    void handleProcessStatusChangeEvent(ProcessStatusChangeEvent processStatusChangeEvent) {
        ProcessState processState = processStatusChangeEvent.getState();
        ProcessIdentifier processIdentity = processStatusChangeEvent.getProcessIdentity();
        String processId = processIdentity.getProcessId();
        ApplicationNode applicationNode = (ApplicationNode) processingQueue.get(processId);
        if (applicationNode != null) {
            ComponentState state = applicationNode.getState();
            switch (processState) {
                case CREATED:
                case VALIDATED:
                case STARTED:
                    break;
                case CONFIGURING_WORKSPACE:
                case PRE_PROCESSING:
                case INPUT_DATA_STAGING:
                case EXECUTING:
                case OUTPUT_DATA_STAGING:
                case POST_PROCESSING:
                    state = ComponentState.RUNNING;
                    break;
                case COMPLETED:
                    state = ComponentState.COMPLETED;
                    // FIXME: read output form registry and set it to node outputport then continue to next application.
                    break;
                case FAILED:
                    state = ComponentState.FAILED;
                    // FIXME: fail workflow.
                    break;
                case CANCELED:
                case CANCELLING:
                    state = ComponentState.CANCELED;
                    // FIXME: cancel workflow.
                    break;
                default:
                    break;
            }
            if (state != applicationNode.getState()) {
                try {
                    updateWorkflowNodeStatus(applicationNode, new ComponentStatus(state));
                } catch (RegistryException e) {
                    log.error("Error! Couldn't update new application state to registry. nodeInstanceId : {} "
                            + applicationNode.getId() + " status to: " + applicationNode.getState().toString(), e);
                }
            }
        }

    }

    private void updateWorkflowNodeStatus(ApplicationNode applicationNode, ComponentStatus componentStatus) throws RegistryException {
        // FIXME: save new workflow node status to registry.
    }

    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }

}
