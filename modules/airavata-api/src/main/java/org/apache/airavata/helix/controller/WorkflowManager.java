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
package org.apache.airavata.helix.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.helix.task.TaskUtil;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.Publisher;
import org.apache.airavata.messaging.Type;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient.RealmMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowManager extends ServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;
    private List<WorkflowOperator> workflowOperators = new ArrayList<>();

    protected final RegistryService registryService;
    private final AiravataServerProperties properties;
    private final MessagingFactory messagingFactory;
    private final TaskUtil taskUtil;

    public WorkflowManager(
            RegistryService registryService,
            AiravataServerProperties properties,
            MessagingFactory messagingFactory,
            TaskUtil taskUtil) {
        this.registryService = registryService;
        this.properties = properties;
        this.messagingFactory = messagingFactory;
        this.taskUtil = taskUtil;
    }

    protected String workflowManagerName;
    private ZKHelixAdmin zkHelixAdmin;
    protected boolean loadBalanceClusters;

    private int currentOperator = 0;

    public WorkflowManager(
            String workflowManagerName,
            boolean loadBalanceClusters,
            RegistryService registryService,
            AiravataServerProperties properties,
            MessagingFactory messagingFactory,
            TaskUtil taskUtil) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
        this.registryService = registryService;
        this.properties = properties;
        this.messagingFactory = messagingFactory;
        this.taskUtil = taskUtil;
    }

    protected void initComponents() throws Exception {
        initHelixAdmin();
        initWorkflowOperators();
        initStatusPublisher();
    }

    private void initWorkflowOperators() throws Exception {
        if (zkHelixAdmin == null) {
            logger.warn("Helix Admin not initialized (Zookeeper unavailable). Workflow operators will not be created.");
            return;
        }

        try {
            if (!loadBalanceClusters) {
                String clusterName = properties.services.helix.clusterName;
                logger.info("Using default cluster " + clusterName + " to submit workflows");
                workflowOperators.add(new WorkflowOperator(
                        clusterName, workflowManagerName, properties.zookeeper.serverConnection, taskUtil));
            } else {
                logger.info("Load balancing workflows among existing clusters");
                List<String> clusters = zkHelixAdmin.getClusters();
                logger.info("Total available clusters " + clusters.size());
                for (String cluster : clusters) {
                    workflowOperators.add(new WorkflowOperator(
                            cluster, workflowManagerName, properties.zookeeper.serverConnection, taskUtil));
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Failed to initialize workflow operators (Zookeeper may not be available): {}. "
                            + "Workflow management features will be unavailable until Zookeeper is accessible.",
                    e.getMessage());
            // Allow server to start even if workflow operators can't be created
        }
    }

    private void initStatusPublisher() throws AiravataException {
        this.statusPublisher = messagingFactory.getPublisher(Type.STATUS);
    }

    private void initHelixAdmin() {
        try {
            this.zkHelixAdmin = new ZKHelixAdmin.Builder()
                    .setRealmMode(RealmMode.SINGLE_REALM)
                    .setZkAddress(properties.zookeeper.serverConnection)
                    .build();
        } catch (Exception e) {
            logger.warn(
                    "Failed to initialize Helix Admin (Zookeeper may not be available): {}. "
                            + "Workflow management features will be unavailable until Zookeeper is accessible.",
                    e.getMessage());
            // Allow server to start even if Zookeeper is unavailable
            this.zkHelixAdmin = null;
        }
    }

    public Publisher getStatusPublisher() {
        return statusPublisher;
    }

    public WorkflowOperator getWorkflowOperator() {
        currentOperator++;
        if (workflowOperators.size() <= currentOperator) {
            currentOperator = 0;
        }
        return workflowOperators.get(currentOperator);
    }

    public void publishProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {

        ProcessStatus status = new ProcessStatus();
        status.setState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());

        try {
            registryService.updateProcessStatus(status, processId);
        } catch (RegistryServiceException e) {
            logger.error("Failed to update process status " + processId, e);
            throw new AiravataException("Failed to update process status " + processId, e);
        }

        ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(status.getState(), identifier);
        MessageContext msgCtx = new MessageContext(
                processStatusChangeEvent,
                MessageType.PROCESS,
                AiravataUtils.getId(MessageType.PROCESS.name()),
                gatewayId);
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        getStatusPublisher().publish(msgCtx);
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
            processWorkflow.setCreationTime(System.currentTimeMillis());
            registryService.addProcessWorkflow(processWorkflow);
        } catch (RegistryServiceException e) {
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
