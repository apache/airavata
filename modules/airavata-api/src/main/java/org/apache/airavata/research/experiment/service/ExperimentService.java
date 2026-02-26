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
package org.apache.airavata.research.experiment.service;

import java.util.List;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.CoreExceptions.InvalidRequestException;
import org.apache.airavata.iam.exception.AuthExceptions.AuthorizationException;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ProjectNotFoundException;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.UserConfigurationDataModel;
import org.apache.airavata.research.project.model.Project;

public interface ExperimentService {

    String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException;

    ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException;

    void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws AiravataSystemException;

    boolean deleteExperiment(String experimentId) throws AiravataSystemException;

    String cloneExperiment(String existingExperimentId, String newExperimentName) throws AiravataSystemException;

    void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AiravataSystemException;

    List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException;

    List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws AiravataSystemException;

    // Lifecycle (from ExperimentOperationsService)

    ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException;

    void launchExperiment(AuthzToken authzToken, String gatewayId, String airavataExperimentId)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, ProjectNotFoundException, SharingRegistryException;

    String cloneExperiment(
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, AuthorizationException,
                    AiravataSystemException, InvalidRequestException;

    void terminateExperiment(String airavataExperimentId, String gatewayId)
            throws ExperimentNotFoundException, AiravataSystemException;

    // Project ops (from ExperimentOperationsService)

    String createProject(String gatewayId, Project project) throws AiravataSystemException;

    List<Project> getUserProjects(AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException;
}
