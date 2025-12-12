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
package org.apache.airavata.service.sharing;

import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.sharing.models.Entity;

/**
 * Service interface for managing sharing registry operations.
 */
public interface SharingManager {
    /**
     * Creates a sharing entity for an experiment and shares it with admin gateway groups.
     * Returns the created entity ID.
     */
    String createExperimentEntity(String experimentId, ExperimentModel experiment) throws AiravataSystemException;
    
    /**
     * Creates a sharing entity for a project.
     * Returns the created entity ID.
     */
    String createProjectEntity(String projectId, Project project) throws AiravataSystemException;
    
    /**
     * Updates sharing entity metadata for an experiment.
     */
    void updateExperimentEntity(String experimentId, ExperimentModel experiment) throws AiravataSystemException;
    
    /**
     * Deletes a sharing entity.
     */
    void deleteEntity(String entityId) throws AiravataSystemException;
}
