/**
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
package org.apache.airavata.service.domain.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.domain.ExperimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of ExperimentService.
 */
@Service
public class ExperimentServiceImpl implements ExperimentService {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentServiceImpl.class);
    
    private final RegistryService registryService;
    private Publisher statusPublisher;
    
    public ExperimentServiceImpl(RegistryService registryService) {
        this.registryService = registryService;
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
        } catch (AiravataException e) {
            logger.warn("StatusPublisher unavailable: " + e.getMessage());
        }
    }
    
    private AiravataSystemException airavataSystemException(AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(errorType, message, cause);
    }
    
    @Override
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException {
        try {
            var experimentId = registryService.createExperiment(gatewayId, experiment);
            if (statusPublisher != null) {
                var event = new ExperimentStatusChangeEvent(ExperimentState.CREATED, experimentId, gatewayId);
                var messageId = AiravataUtils.getId("EXPERIMENT");
                var messageContext = new MessageContext(event, org.apache.airavata.model.messaging.event.MessageType.EXPERIMENT, messageId, gatewayId);
                messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                statusPublisher.publish(messageContext);
            }
            return experimentId;
        } catch (RegistryServiceException | AiravataException e) {
            String msg = "Error while creating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws AiravataSystemException {
        try {
            registryService.updateExperiment(airavataExperimentId, experiment);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public boolean deleteExperiment(String experimentId) throws AiravataSystemException {
        try {
            return registryService.deleteExperiment(experimentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public String cloneExperiment(String existingExperimentId, String newExperimentName) throws AiravataSystemException {
        // This is a simplified version - full implementation would need more context
        throw new UnsupportedOperationException("Clone experiment requires additional context - use AiravataService.cloneExperiment");
    }
    
    @Override
    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset) throws AiravataSystemException {
        try {
            return registryService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (RegistryServiceException e) {
            String msg = "Error occurred while getting user experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset) throws AiravataSystemException {
        try {
            return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving the experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public ExperimentStatistics getExperimentStatistics(String gatewayId, String userName, long fromTime, long toTime) throws AiravataSystemException {
        try {
            return registryService.getExperimentStatistics(
                    gatewayId, fromTime, toTime, userName, null, null, List.of(), 0, 0);
        } catch (RegistryServiceException e) {
            String msg = "Error while getting experiment statistics: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public List<ExperimentSummaryModel> searchExperiments(String gatewayId, String userName, ExperimentSearchFields searchFields, int limit, int offset) throws AiravataSystemException {
        try {
            Map<ExperimentSearchFields, String> filters = searchFields != null ? Map.of(searchFields, "") : Map.of();
            return registryService.searchExperiments(gatewayId, userName, List.of(), filters, limit, offset);
        } catch (RegistryServiceException e) {
            String msg = "Error while searching experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
