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
package org.apache.airavata.execution.orchestration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.status.model.ErrorModel;

/**
 * Experiment launch entry point and process coordination.
 *
 * <p>Owns the top-level launch pipeline for single-application and workflow experiments.
 * Status transitions are delegated to {@link ExperimentStatusManager}; credential and deployment
 * resolution is delegated to {@link ProcessResourceResolver}.
 */
public interface OrchestratorService {

    boolean validateExperiment(String experimentId)
            throws LaunchValidationException, RegistryException, OrchestratorException;

    boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, RegistryException, OrchestratorException;

    boolean terminateExperiment(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException;

    void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException;

    boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException;

    void createAndValidateTasks(ExperimentModel experiment, boolean recreateTaskDag)
            throws OrchestratorException, RegistryException, LaunchValidationException;

    void addProcessValidationErrors(String processId, ErrorModel details) throws RegistryException;

    boolean launchSingleAppExperimentInternal(
            String experimentId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException;

    void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException;

    StatusModel<ExperimentState> getExperimentStatus(String experimentId) throws RegistryException;

    ProcessModel getProcess(String processId) throws RegistryException;

    boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException;
}
