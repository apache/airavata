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

package org.apache.airavata.registry.services;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;

import javax.ws.rs.core.Response;
import java.util.Date;

public interface ProjectsRegistryService {
    // ------------Project management
    public Response isWorkspaceProjectExists(String projectName) throws RegistryException;

    public Response isWorkspaceProjectExists(String projectName, String createIfNotExists) throws RegistryException;

    public Response addWorkspaceProject(String projectName) throws WorkspaceProjectAlreadyExistsException,
            RegistryException;

    public Response updateWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException,
            RegistryException;

    public Response deleteWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException,
            RegistryException;

    public Response getWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException,
            RegistryException;

    public Response getWorkspaceProjects() throws RegistryException;

    // ------------Experiment management
    public Response addExperiment(String projectName, AiravataExperiment experiment)
            throws WorkspaceProjectDoesNotExistsException, ExperimentDoesNotExistsException, RegistryException;

    public Response removeExperiment(String experimentId) throws ExperimentDoesNotExistsException;

    public Response getExperiments() throws RegistryException;

    public Response getExperimentsByProject(String projectName) throws RegistryException;

    public Response getExperimentsByDate(Date from, Date to) throws RegistryException;

    public Response getExperimentsByProjectDate(String projectName, Date from, Date to) throws RegistryException;
}
