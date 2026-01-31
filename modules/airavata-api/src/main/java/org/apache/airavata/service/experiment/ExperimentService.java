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
package org.apache.airavata.service.experiment;

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.CoreExceptions.AiravataErrorType;
import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.apache.airavata.common.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentSearchFields;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatistics;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.orchestrator.config.OrchestratorConfig;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts.MessageContext;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts.Publisher;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts.Type;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Service for experiment management operations.
 */
@Service("experimentServiceFacade")
@ConditionalOnBean(org.apache.airavata.service.registry.RegistryService.class)
public class ExperimentService {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    private final RegistryService registryService;
    private Publisher statusPublisher;

    public ExperimentService(
            RegistryService registryService,
            MessagingFactory messagingFactory,
            @Value("${" + OrchestratorConfig.ENABLED + ":false}") boolean daprEnabled,
            @Autowired(required = false) Environment environment) {
        this.registryService = registryService;
        if (daprEnabled) {
            try {
                if (messagingFactory.isAvailable()) {
                    statusPublisher = messagingFactory.getPublisher(Type.STATUS);
                } else {
                    boolean isTestProfile = environment != null
                            && java.util.Arrays.asList(environment.getActiveProfiles())
                                    .contains("test");
                    if (isTestProfile) {
                        logger.debug("StatusPublisher unavailable: Dapr not fully configured (test environment)");
                    } else {
                        logger.warn(
                                "StatusPublisher unavailable: Dapr is not configured. Ensure airavata.dapr.enabled=true and Dapr sidecar is running.");
                    }
                }
            } catch (Exception e) {
                boolean isTestProfile = environment != null
                        && java.util.Arrays.asList(environment.getActiveProfiles())
                                .contains("test");
                if (isTestProfile) {
                    logger.debug("StatusPublisher unavailable: " + e.getMessage() + " (test environment)");
                } else {
                    logger.warn("StatusPublisher unavailable: " + e.getMessage());
                }
            }
        }
    }

    private AiravataSystemException airavataSystemException(
            AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ValidationExceptions.ExceptionHandlerUtil.wrapAsAiravataException(
                errorType, message, cause);
    }

    public String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException {
        try {
            var experimentId = registryService.createExperiment(gatewayId, experiment);
            if (statusPublisher != null) {
                var event = new ExperimentStatusChangeEvent();
                event.setState(ExperimentState.CREATED);
                event.setExperimentId(experimentId);
                event.setGatewayId(gatewayId);
                var messageId = AiravataUtils.getId("EXPERIMENT");
                var messageContext = new MessageContext(
                        event, org.apache.airavata.common.model.MessageType.EXPERIMENT, messageId, gatewayId);
                messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                statusPublisher.publish(messageContext);
            }
            return experimentId;
        } catch (RegistryException | AiravataException e) {
            String msg = "Error while creating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (RegistryException e) {
            String msg = "Error while retrieving experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws AiravataSystemException {
        try {
            registryService.updateExperiment(airavataExperimentId, experiment);
        } catch (RegistryException e) {
            String msg = "Error while updating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteExperiment(String experimentId) throws AiravataSystemException {
        try {
            return registryService.deleteExperiment(experimentId);
        } catch (RegistryException e) {
            String msg = "Error while deleting experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    /**
     * Clone an existing experiment with a new name.
     *
     * <p>This method is not implemented in ExperimentService as it requires additional
     * context (user permissions, gateway configuration, etc.) that is better handled
     * by the full AiravataService implementation.
     *
     * @param existingExperimentId the ID of the experiment to clone
     * @param newExperimentName the name for the cloned experiment
     * @return the ID of the cloned experiment
     * @throws AiravataSystemException if the operation fails
     * @throws UnsupportedOperationException always - use AiravataService.cloneExperiment() instead
     */
    public String cloneExperiment(String existingExperimentId, String newExperimentName)
            throws AiravataSystemException {
        throw new UnsupportedOperationException(
                "Clone experiment requires additional context - use AiravataService.cloneExperiment() instead");
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException {
        try {
            return registryService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (RegistryException e) {
            String msg = "Error occurred while getting user experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws AiravataSystemException {
        try {
            return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (RegistryException e) {
            String msg = "Error while retrieving the experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ExperimentStatistics getExperimentStatistics(String gatewayId, String userName, long fromTime, long toTime)
            throws AiravataSystemException {
        try {
            return registryService.getExperimentStatistics(
                    gatewayId, fromTime, toTime, userName, null, null, List.of(), 0, 0);
        } catch (RegistryException e) {
            String msg = "Error while getting experiment statistics: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId, String userName, ExperimentSearchFields searchFields, int limit, int offset)
            throws AiravataSystemException {
        try {
            Map<ExperimentSearchFields, String> filters = searchFields != null ? Map.of(searchFields, "") : Map.of();
            return registryService.searchExperiments(gatewayId, userName, List.of(), filters, limit, offset);
        } catch (RegistryException e) {
            String msg = "Error while searching experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
