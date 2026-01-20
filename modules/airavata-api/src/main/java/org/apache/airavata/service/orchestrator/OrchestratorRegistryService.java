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
package org.apache.airavata.service.orchestrator;

import java.util.List;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(org.apache.airavata.service.registry.RegistryService.class)
public class OrchestratorRegistryService {

    private final org.apache.airavata.service.registry.RegistryService registryService;

    public OrchestratorRegistryService(org.apache.airavata.service.registry.RegistryService registryService) {
        this.registryService = registryService;
    }

    private RegistryService getRegistryService() {
        return registryService;
    }

    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryException {
        return getRegistryService().getExperiment(airavataExperimentId);
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryException {
        return getRegistryService().getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryException {
        return getRegistryService().getGroupResourceProfile(groupResourceProfileId);
    }

    public DataProductModel getDataProduct(String productUri) throws RegistryException {
        return getRegistryService().getDataProduct(productUri);
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        getRegistryService().updateProcess(processModel, processId);
    }

    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryException {
        getRegistryService().addErrors(errorType, errorModel, id);
    }

    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryException {
        return getRegistryService().getExperimentStatus(airavataExperimentId);
    }

    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryException {
        getRegistryService().updateExperimentStatus(experimentStatus, experimentId);
    }

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        return getRegistryService().addProcess(processModel, experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        return getRegistryService().getProcess(processId);
    }

    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        return getRegistryService().getProcessList(experimentId);
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        return getRegistryService().getProcessStatus(processId);
    }

    public List<String> getProcessIds(String experimentId) throws RegistryException {
        return getRegistryService().getProcessIds(experimentId);
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        getRegistryService().addProcessStatus(processStatus, processId);
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryException {
        return getRegistryService().getApplicationOutputs(appInterfaceId);
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws RegistryException {
        return getRegistryService().getApplicationInterface(appInterfaceId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryException {
        return getRegistryService().getApplicationDeployments(appModuleId);
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryException {
        return getRegistryService().getComputeResource(computeResourceId);
    }

    public void deleteTasks(String processId) throws RegistryException {
        getRegistryService().deleteTasks(processId);
    }

    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryException {
        getRegistryService().registerQueueStatuses(queueStatuses);
    }
}
