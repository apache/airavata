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
package org.apache.airavata.orchestrator.server;

import java.text.MessageFormat;
import java.util.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.logging.MDCUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.process.scheduling.api.ProcessSchedulerImpl;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.schedule.HostScheduler;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.orchestrator.util.OrchestratorUtils;
import org.apache.airavata.service.OrchestratorRegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
    private SimpleOrchestratorImpl orchestrator = null;
    private String airavataUserName;
    private String gatewayName;
    private Publisher publisher;
    private final Subscriber statusSubscribe;
    private final Subscriber experimentSubscriber;

    private CuratorFramework curatorClient;
    private OrchestratorRegistryService orchestratorRegistryService = new OrchestratorRegistryService();
    private org.apache.airavata.service.OrchestratorService orchestratorService;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getAPIVersion() throws TException {
        return null;
    }

    public OrchestratorServerHandler() throws OrchestratorException, TException {
        // orchestrator init
        try {
            // first constructing the monitorManager and orchestrator, then fill
            // the required properties
            setAiravataUserName(ServerSettings.getDefaultUser());
            orchestrator = new SimpleOrchestratorImpl();

            publisher = MessagingFactory.getPublisher(Type.STATUS);
            orchestrator.initialize();
            orchestrator.getOrchestratorContext().setPublisher(publisher);
            statusSubscribe = getStatusSubscriber();
            experimentSubscriber = getExperimentSubscriber();
            startCurator();
            orchestratorService = new org.apache.airavata.service.OrchestratorService(orchestratorRegistryService, orchestrator, curatorClient, publisher);
        } catch (OrchestratorException | AiravataException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
        }
    }

    private Subscriber getStatusSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        //			routingKeys.add("*"); // listen for gateway level messages
        //			routingKeys.add("*.*"); // listen for gateway/experiment level messages
        routingKeys.add("*.*.*"); // listen for gateway/experiment/process level messages
        return MessagingFactory.getSubscriber(new ProcessStatusHandler(), routingKeys, Type.STATUS);
    }

    private Subscriber getExperimentSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqExperimentLaunchQueueName());
        return MessagingFactory.getSubscriber(new ExperimentHandler(), routingKeys, Type.EXPERIMENT_LAUNCH);
    }

    /**
     * * After creating the experiment Data user have the * experimentID as the
     * handler to the experiment, during the launchProcess * We just have to
     * give the experimentID * * @param experimentID * @return sucess/failure *
     * *
     *
     * @param experimentId
     */
    public boolean launchExperiment(String experimentId, String gatewayId) throws TException {
        try {
            return orchestratorService.launchExperimentWithErrorHandling(
                    experimentId, gatewayId, OrchestratorServerThreadPoolExecutor.getCachedThreadPool());
        } catch (TException e) {
            throw e;
        } catch (Exception e) {
            throw new TException("Experiment '" + experimentId + "' launch failed.", e);
        }
    }

    /**
     * This method will validate the experiment before launching, if is failed
     * we do not run the launch in airavata thrift service (only if validation
     * is enabled
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean validateExperiment(String experimentId) throws TException, LaunchValidationException {
        try {
            return orchestratorService.validateExperiment(experimentId);
        } catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating experiment", e);
            throw new TException(e);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            log.error(experimentId, "Error while retrieving experiment for validation", e);
            throw new TException(e);
        }
    }

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, TException {
        try {
            return orchestratorService.validateProcess(experimentId, processes);
        } catch (LaunchValidationException lve) {
            // If a process failed to validate, also add an error message at the experiment level
            ErrorModel details = new ErrorModel();
            details.setActualErrorMessage(lve.getErrorMessage());
            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
            try {
                orchestratorService.addProcessValidationErrors(experimentId, details);
            } catch (org.apache.airavata.registry.cpi.RegistryException e) {
                log.error(experimentId, "Error while adding errors to experiment", e);
            }
            throw lve;
        } catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating process", e);
            throw new TException(e);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            log.error(experimentId, "Error while retrieving experiment for process validation", e);
            throw new TException(e);
        }
    }

    /**
     * This can be used to cancel a running experiment and store the status to
     * terminated in registry
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean terminateExperiment(String experimentId, String gatewayId) throws TException {
        log.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        try {
            return orchestratorService.terminateExperiment(experimentId, gatewayId);
        } catch (Exception e) {
            log.error("expId : " + experimentId + " :- Error while cancelling experiment", e);
            return false;
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws TException {
        try {
            orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
        } catch (Exception e) {
            log.error("expId : " + experimentId + " :- Error while fetching intermediate", e);
        }
    }


    private String getAiravataUserName() {
        return airavataUserName;
    }

    private String getGatewayName() {
        return gatewayName;
    }

    public void setAiravataUserName(String airavataUserName) {
        this.airavataUserName = airavataUserName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId) throws TException {
        try {
            return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            log.error(processId, "Error while launching process ", e);
            throw new TException(e);
        } catch (Exception e) {
            log.error(processId, "Error while launching process ", e);
            throw new TException(e);
        }
    }



    private class ProcessStatusHandler implements MessageHandler {
        /**
         * This method only handle MessageType.PROCESS type messages.
         *
         * @param message
         */
        @Override
        public void onMessage(MessageContext message) {
            if (message.getType().equals(MessageType.PROCESS)) {
                try {
                    ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent();
                    TBase event = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(event);
                    ThriftUtils.createThriftFromBytes(bytes, processStatusChangeEvent);
                    ProcessIdentifier processIdentity = processStatusChangeEvent.getProcessIdentity();
                    log.info(
                            "expId: {}, processId: {} :- Process status changed event received for status {}",
                            processIdentity.getExperimentId(),
                            processIdentity.getProcessId(),
                            processStatusChangeEvent.getState().name());
                    orchestratorService.handleProcessStatusChange(processStatusChangeEvent, processIdentity);
                } catch (TException e) {
                    log.error("Message Id : " + message.getMessageId() + ", Message type : " + message.getType()
                            + "Error" + " while prcessing process status change event");
                    throw new RuntimeException("Error while updating experiment status", e);
                } catch (Exception e) {
                    log.error("Message Id : " + message.getMessageId() + ", Message type : " + message.getType()
                            + "Error" + " while prcessing process status change event", e);
                    throw new RuntimeException("Error while updating experiment status", e);
                }
            } else {
                System.out.println("Message Recieved with message id " + message.getMessageId() + " and with message "
                        + "type " + message.getType().name());
            }
        }
    }

    private class ExperimentHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            MDC.put(MDCConstants.GATEWAY_ID, messageContext.getGatewayId());
            switch (messageContext.getType()) {
                case EXPERIMENT:
                    launchExperiment(messageContext);
                    break;
                case EXPERIMENT_CANCEL:
                    cancelExperiment(messageContext);
                    break;
                case INTERMEDIATE_OUTPUTS:
                    handleIntermediateOutputsEvent(messageContext);
                    break;
                default:
                    experimentSubscriber.sendAck(messageContext.getDeliveryTag());
                    log.error("Orchestrator got un-support message type : " + messageContext.getType());
                    break;
            }
            MDC.clear();
        }

        private void cancelExperiment(MessageContext messageContext) {
            try {
                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
                ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
                ThriftUtils.createThriftFromBytes(bytes, expEvent);
                log.info(
                        "Cancelling experiment with experimentId: {} gateway Id: {}",
                        expEvent.getExperimentId(),
                        expEvent.getGatewayId());
                orchestratorService.handleCancelExperiment(expEvent);
            } catch (TException e) {
                log.error("Error while cancelling experiment", e);
                throw new RuntimeException("Error while cancelling experiment", e);
            } catch (Exception e) {
                log.error("Error while cancelling experiment", e);
                throw new RuntimeException("Error while cancelling experiment", e);
            } finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            }
        }

        private void handleIntermediateOutputsEvent(MessageContext messageContext) {
            try {
                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
                ExperimentIntermediateOutputsEvent event = new ExperimentIntermediateOutputsEvent();
                ThriftUtils.createThriftFromBytes(bytes, event);
                log.info(
                        "INTERMEDIATE_OUTPUTS event for experimentId: {} gateway Id: {} outputs: {}",
                        event.getExperimentId(),
                        event.getGatewayId(),
                        event.getOutputNames());
                orchestratorService.handleIntermediateOutputsEvent(event);
            } catch (TException e) {
                log.error("Error while fetching intermediate outputs", e);
                throw new RuntimeException("Error while fetching intermediate outputs", e);
            } catch (Exception e) {
                log.error("Error while fetching intermediate outputs", e);
                throw new RuntimeException("Error while fetching intermediate outputs", e);
            } finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            }
        }
    }

    private void launchExperiment(MessageContext messageContext) {
        try {
            orchestratorService.handleLaunchExperimentFromMessage(messageContext);
        } catch (TException e) {
            log.error("Experiment launch failed due to Thrift conversion error", e);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            log.error("Experiment launch failed due to registry error", e);
        } catch (Exception e) {
            log.error("An unknown issue while launching experiment", e);
        } finally {
            experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            MDC.clear();
        }
    }


    private void startCurator() throws ApplicationSettingsException {
        String connectionSting = ServerSettings.getZookeeperConnection();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
        curatorClient.start();
    }

    public String getExperimentNodePath(String experimentId) {
        return ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
    }

}
