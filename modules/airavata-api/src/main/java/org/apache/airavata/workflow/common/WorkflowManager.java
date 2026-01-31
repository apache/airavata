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
package org.apache.airavata.workflow.common;

import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowManager extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    protected final RegistryService registryService;
    private final MessagingFactory messagingFactory;
    private final ProcessStatusUpdater statusUpdateHelper;

    public WorkflowManager(
            RegistryService registryService,
            MessagingFactory messagingFactory,
            ProcessStatusUpdater statusUpdateHelper) {
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
        this.statusUpdateHelper = statusUpdateHelper;
    }

    protected String workflowManagerName;
    protected boolean loadBalanceClusters;

    public WorkflowManager(
            String workflowManagerName,
            boolean loadBalanceClusters,
            RegistryService registryService,
            MessagingFactory messagingFactory,
            ProcessStatusUpdater statusUpdateHelper) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
        this.statusUpdateHelper = statusUpdateHelper;
    }

    protected void initComponents() throws Exception {
        // No pub-sub; DB-only updates.
    }

    /**
     * Updates process status in registry only. Delegates to ProcessStatusUpdater.
     */
    public void updateProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {
        statusUpdateHelper.updateProcessStatus(processId, experimentId, gatewayId, state);
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
