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
package org.apache.airavata.helix.impl.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.workflow.WorkflowOperator;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.ServiceFactory;
import org.apache.airavata.service.ServiceFactoryException;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient.RealmMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;
    private List<WorkflowOperator> workflowOperators = new ArrayList<>();
    private RegistryService registryService;
    private String workflowManagerName;
    private ZKHelixAdmin zkHelixAdmin;
    private boolean loadBalanceClusters;

    private int currentOperator = 0;

    public WorkflowManager(String workflowManagerName, boolean loadBalanceClusters) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
    }

    protected void initComponents() throws Exception {
        initRegistryService();
        initHelixAdmin();
        initWorkflowOperators();
        initStatusPublisher();
    }

    private void initWorkflowOperators() throws Exception {

        if (!loadBalanceClusters) {
            logger.info("Using default cluster " + ServerSettings.getSetting("helix.cluster.name")
                    + " to submit workflows");
            workflowOperators.add(new WorkflowOperator(
                    ServerSettings.getSetting("helix.cluster.name"),
                    workflowManagerName,
                    ServerSettings.getZookeeperConnection()));
        } else {
            logger.info("Load balancing workflows among existing clusters");
            List<String> clusters = zkHelixAdmin.getClusters();
            logger.info("Total available clusters " + clusters.size());
            for (String cluster : clusters) {
                workflowOperators.add(
                        new WorkflowOperator(cluster, workflowManagerName, ServerSettings.getZookeeperConnection()));
            }
        }
    }

    private void initStatusPublisher() throws AiravataException {
        this.statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
    }

    private void initHelixAdmin() throws ApplicationSettingsException {
        this.zkHelixAdmin = new ZKHelixAdmin.Builder()
                .setRealmMode(RealmMode.SINGLE_REALM)
                .setZkAddress(ServerSettings.getZookeeperConnection())
                .build();
    }

    private void initRegistryService()
            throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        // Try to get from Spring context first, fallback to ServiceFactory for backward compatibility
        try {
            this.registryService = org.apache.airavata.config.RegistryServiceProvider.getInstance();
        } catch (Exception e) {
            // Fallback to ServiceFactory if Spring context not available
            try {
                this.registryService = ServiceFactory.getInstance().getRegistryService();
            } catch (ServiceFactoryException ex) {
                throw new IllegalStateException("Failed to get RegistryService", ex);
            }
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
}
