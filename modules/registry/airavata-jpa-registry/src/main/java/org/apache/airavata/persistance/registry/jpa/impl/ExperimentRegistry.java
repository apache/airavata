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

package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.experiment.BasicMetadata;
import org.apache.airavata.model.experiment.ConfigurationData;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentConfigDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentMetadataResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.registry.cpi.DependentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ExperimentRegistry {
    private GatewayRegistry gatewayRegistry;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRegistry.class);

    public void add(BasicMetadata basicMetadata) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getGateway();
            ExperimentMetadataResource exBasicData = gateway.createBasicMetada(getExperimentID(basicMetadata.getExperimentName()));
            exBasicData.setExperimentName(basicMetadata.getExperimentName());
            exBasicData.setDescription(basicMetadata.getExperimentDescription());
            exBasicData.setExecutionUser(basicMetadata.getUserName());
            exBasicData.setSubmittedDate(getCurrentTimestamp());
            exBasicData.setShareExp(basicMetadata.isSetShareExperimentPublicly());
            exBasicData.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void add(ConfigurationData configurationData, String experimentID) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource)gateway.get(ResourceType.EXPERIMENT_METADATA, experimentID);
            ExperimentConfigDataResource exConfigData = (ExperimentConfigDataResource)exBasicData.create(ResourceType.EXPERIMENT_CONFIG_DATA);
            BasicMetadata updatedBasicMetadata = configurationData.getBasicMetadata();
            if (updatedBasicMetadata != null){
                if (updatedBasicMetadata.getExperimentName() != null && !updatedBasicMetadata.getExperimentName().equals("")){
                    exBasicData.setExperimentName(updatedBasicMetadata.getExperimentName());
                }
                if (updatedBasicMetadata.getExperimentDescription() != null && !updatedBasicMetadata.getExperimentDescription().equals("")){
                    exBasicData.setDescription(updatedBasicMetadata.getExperimentDescription());
                }
                if (updatedBasicMetadata.getUserName() != null && !updatedBasicMetadata.getUserName().equals("")){
                    exBasicData.setExecutionUser(updatedBasicMetadata.getUserName());
                }
                exBasicData.setShareExp(updatedBasicMetadata.isSetShareExperimentPublicly());
                exBasicData.save();
            }
            exConfigData.setExMetadata(exBasicData);
            exConfigData.setApplicationID(configurationData.getApplicationId());
            exConfigData.setApplicationVersion(configurationData.getApplicationVersion());
            exConfigData.setWorkflowTemplateId(configurationData.getWorkflowTemplateId());
            exConfigData.setWorkflowTemplateVersion(configurationData.getWorklfowTemplateVersion());

            exConfigData.setCpuCount(configurationData.getComputationalResourceScheduling().getTotalCPUCount());
            exConfigData.setAiravataAutoSchedule(configurationData.getComputationalResourceScheduling().isAiravataAutoSchedule());
            exConfigData.setOverrideManualSchedule(configurationData.getComputationalResourceScheduling().isOverrideManualScheduledParams());
            exConfigData.setResourceHostID(configurationData.getComputationalResourceScheduling().getResourceHostId());

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String getExperimentID (String experimentName){
        return experimentName + "_" + UUID.randomUUID();
    }

    public void update(DependentDataType dataType, Object newObjectToUpdate) {

    }

    public void update(DependentDataType dataType, Object identifier, Object field, Object value) {

    }

    public List<Object> get(DependentDataType dataType, Object filteredBy, Object value) {
        return null;
    }

    public Object getValue(DependentDataType dataType, Object identifier, Object field) {
        return null;
    }

    public void remove(DependentDataType dataType, Object identifier) {

    }

    public boolean isExist(DependentDataType dataType, Object identifier) {
        return false;
    }

    public Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        return new Timestamp(d.getTime());
    }
}
