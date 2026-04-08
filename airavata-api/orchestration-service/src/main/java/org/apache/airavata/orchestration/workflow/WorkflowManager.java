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
package org.apache.airavata.orchestration.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.messaging.event.proto.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.proto.ProcessStatusChangeEvent;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.airavata.util.AiravataUtils;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient.RealmMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;
    private List<WorkflowOperator> workflowOperators = new ArrayList<>();
    private RegistryHandler registryHandler;
    private String workflowManagerName;
    private ZKHelixAdmin zkHelixAdmin;
    private boolean loadBalanceClusters;

    private int currentOperator = 0;

    public WorkflowManager(String workflowManagerName, boolean loadBalanceClusters) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
    }

    public void initComponents() throws Exception {
        initRegistryHandler();
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

    private void initRegistryHandler() {
        this.registryHandler = SchedulerUtils.getRegistryHandler();
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

    public RegistryHandler getRegistryHandler() {
        return registryHandler;
    }

    public void publishProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {

        ProcessStatus status = ProcessStatus.newBuilder()
                .setState(state)
                .setTimeOfStateChange(Calendar.getInstance().getTimeInMillis())
                .build();

        try {
            registryHandler.updateProcessStatus(status, processId);
        } catch (Exception e) {
            logger.error("Failed to update process status " + processId, e);
        }

        ProcessIdentifier identifier = ProcessIdentifier.newBuilder()
                .setProcessId(processId)
                .setExperimentId(experimentId)
                .setGatewayId(gatewayId)
                .build();
        ProcessStatusChangeEvent processStatusChangeEvent = ProcessStatusChangeEvent.newBuilder()
                .setState(status.getState())
                .setProcessIdentity(identifier)
                .build();
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

    public void registerWorkflowForProcess(String processId, String workflowName, String workflowType) {
        try {
            ProcessWorkflow processWorkflow = ProcessWorkflow.newBuilder()
                    .setProcessId(processId)
                    .setWorkflowId(workflowName)
                    .setType(workflowType)
                    .setCreationTime(System.currentTimeMillis())
                    .build();
            registryHandler.addProcessWorkflow(processWorkflow);
        } catch (Exception e) {
            logger.error(
                    "Failed to save workflow " + workflowName + " of process " + processId
                            + ". This will affect cancellation tasks",
                    e);
        }
    }
}
