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
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorRegistryService {

    @Autowired
    private RegistryService registryService;

    private RegistryService getRegistryService() {
        return registryService;
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryServiceException {
        return getRegistryService().getExperiment(airavataExperimentId);
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryServiceException {
        return getRegistryService().getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        return getRegistryService().getGroupResourceProfile(groupResourceProfileId);
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException {
        return getRegistryService().getDataProduct(productUri);
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException {
        getRegistryService().updateProcess(processModel, processId);
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryServiceException {
        getRegistryService().addErrors(errorType, errorModel, id);
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryServiceException {
        return getRegistryService().getExperimentStatus(airavataExperimentId);
    }

    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryServiceException {
        getRegistryService().updateExperimentStatus(experimentStatus, experimentId);
    }

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryServiceException {
        return getRegistryService().addProcess(processModel, experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryServiceException {
        return getRegistryService().getProcess(processId);
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException {
        return getRegistryService().getProcessList(experimentId);
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException {
        return getRegistryService().getProcessStatus(processId);
    }

    public List<String> getProcessIds(String experimentId) throws RegistryServiceException {
        return getRegistryService().getProcessIds(experimentId);
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        getRegistryService().addProcessStatus(processStatus, processId);
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryServiceException {
        return getRegistryService().getApplicationOutputs(appInterfaceId);
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws RegistryServiceException {
        return getRegistryService().getApplicationInterface(appInterfaceId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryServiceException {
        return getRegistryService().getApplicationDeployments(appModuleId);
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryServiceException {
        return getRegistryService().getComputeResource(computeResourceId);
    }

    public void deleteTasks(String processId) throws RegistryServiceException {
        getRegistryService().deleteTasks(processId);
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryServiceException {
        getRegistryService().registerQueueStatuses(queueStatuses);
    }
}
