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
package org.apache.airavata.service.experiment;

import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;

import java.util.List;

/**
 * Service interface for experiment management operations.
 */
public interface ExperimentService {
    String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException;
    
    ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException;
    
    void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws AiravataSystemException;
    
    boolean deleteExperiment(String experimentId) throws AiravataSystemException;
    
    String cloneExperiment(String existingExperimentId, String newExperimentName) throws AiravataSystemException;
    
    List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset) throws AiravataSystemException;
    
    List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset) throws AiravataSystemException;
    
    ExperimentStatistics getExperimentStatistics(String gatewayId, String userName, long fromTime, long toTime) throws AiravataSystemException;
    
    List<ExperimentSummaryModel> searchExperiments(String gatewayId, String userName, ExperimentSearchFields searchFields, int limit, int offset) throws AiravataSystemException;
}
