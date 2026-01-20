/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.workflow;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.dapr.messaging.DaprMessagingFactory;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.Publisher;
import org.apache.airavata.dapr.messaging.Type;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.statemachine.ProcessStateValidator;
import org.apache.airavata.statemachine.StateTransitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowManager extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;

    protected final RegistryService registryService;
    private final DaprMessagingFactory messagingFactory;

    public WorkflowManager(RegistryService registryService, DaprMessagingFactory messagingFactory) {
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
    }

    protected String workflowManagerName;
    protected boolean loadBalanceClusters;

    public WorkflowManager(
            String workflowManagerName,
            boolean loadBalanceClusters,
            RegistryService registryService,
            DaprMessagingFactory messagingFactory) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
    }

    protected void initComponents() throws Exception {
        initStatusPublisher();
    }

    private void initStatusPublisher() throws AiravataException {
        this.statusPublisher = messagingFactory.getPublisher(Type.STATUS);
    }

    public Publisher getStatusPublisher() {
        return statusPublisher;
    }

    public void publishProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {
        try {
            ProcessStatus currentStatus = registryService.getProcessStatus(processId);
            ProcessState currentState = currentStatus != null ? currentStatus.getState() : null;

            if (!StateTransitionService.validateAndLog(
                    ProcessStateValidator.INSTANCE, currentState, state, processId, "process")) {
                throw new AiravataException(String.format(
                        "Invalid process state transition rejected: processId=%s, %s -> %s",
                        processId, currentState != null ? currentState.name() : "(initial)", state.name()));
            }

            ProcessStatus status = new ProcessStatus();
            status.setState(state);
            status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());

            registryService.updateProcessStatus(status, processId);

            ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
            ProcessStatusChangeEvent processStatusChangeEvent =
                    new ProcessStatusChangeEvent(status.getState(), identifier);
            MessageContext msgCtx = new MessageContext(
                    processStatusChangeEvent,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    gatewayId);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);
        } catch (RegistryException e) {
            logger.error("Failed to update process status " + processId, e);
            throw new AiravataException("Failed to update process status " + processId, e);
        }
    }

    public String normalizeTaskId(String taskId) {
        return taskId.replace(":", "-").replace(",", "-");
    }

    protected void registerWorkflowForProcess(String processId, String workflowName, String workflowType) {
        try {
            ProcessWorkflow processWorkflow = new ProcessWorkflow();
            processWorkflow.setProcessId(processId);
            processWorkflow.setWorkflowId(workflowName);
            processWorkflow.setType(workflowType);
            processWorkflow.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
            registryService.addProcessWorkflow(processWorkflow);
        } catch (RegistryException e) {
            logger.error(
                    "Failed to save workflow " + workflowName + " of process " + processId
                            + ". This will affect cancellation tasks",
                    e);
        }
    }

    protected RegistryService getRegistryService() {
        return registryService;
    }

    // Abstract methods from ServerLifecycle that subclasses must implement
    public abstract String getServerName();

    public abstract String getServerVersion();

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    // Default phase implementation - subclasses can override
    @Override
    public int getPhase() {
        return 0;
    }
}
