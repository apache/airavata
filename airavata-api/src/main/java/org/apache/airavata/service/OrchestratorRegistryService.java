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
package org.apache.airavata.service;

import java.util.List;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorRegistryService.class);
    
    private org.apache.airavata.service.RegistryService registryService = new org.apache.airavata.service.RegistryService();

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryException {
        return registryService.getExperiment(airavataExperimentId);
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws AppCatalogException {
        return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws AppCatalogException {
        return registryService.getGroupResourceProfile(groupResourceProfileId);
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryException {
        return registryService.getDataProduct(productUri);
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        registryService.updateProcess(processModel, processId);
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryException {
        registryService.addErrors(errorType, errorModel, id);
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryException {
        return registryService.getExperimentStatus(airavataExperimentId);
    }

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        return registryService.addProcess(processModel, experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        return registryService.getProcess(processId);
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        return registryService.getProcessList(experimentId);
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        return registryService.getProcessStatus(processId);
    }

    public List<String> getProcessIds(String experimentId) throws RegistryException {
        return registryService.getProcessIds(experimentId);
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        registryService.addProcessStatus(processStatus, processId);
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AppCatalogException {
        return registryService.getApplicationOutputs(appInterfaceId);
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AppCatalogException {
        return registryService.getApplicationInterface(appInterfaceId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws AppCatalogException {
        return registryService.getApplicationDeployments(appModuleId);
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws AppCatalogException {
        return registryService.getComputeResource(computeResourceId);
    }

    public void deleteTasks(String processId) throws RegistryException {
        registryService.deleteTasks(processId);
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryException {
        registryService.registerQueueStatuses(queueStatuses);
    }
}

