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
import org.apache.airavata.core.service.CrudService;
import org.apache.airavata.research.experiment.model.Project;

/**
 * Domain service for managing experiment projects within a gateway.
 *
 * <p>Extends {@link CrudService} for the standard create/get/update/delete/listByGateway
 * contract. Domain-specific methods with differing signatures are declared here.
 */
public interface ProjectService extends CrudService<Project> {

    /**
     * Create a new project, setting the gateway scope from the supplied {@code gatewayId}.
     *
     * @param gatewayId the owning gateway
     * @param project   the project to create
     * @return the generated project id
     */
    String createProject(String gatewayId, Project project);

    /** Return the project with the given id, or {@code null} if not found. */
    default Project getProject(String projectId) {
        return get(projectId);
    }

    /** Update the project identified by {@code projectId}. */
    default void updateProject(String projectId, Project updatedProject) {
        update(projectId, updatedProject);
    }

    /**
     * Delete the project identified by {@code projectId}.
     *
     * @return {@code true} if the project existed and was deleted, {@code false} if not found
     */
    boolean deleteProject(String projectId);

    /**
     * Search for projects within a gateway, optionally filtered by user and accessible ids.
     *
     * @param gatewayId    the owning gateway
     * @param userName     the requesting user
     * @param searchFields optional list of accessible project ids for sharing-enabled gateways
     * @param limit        maximum number of results
     * @param offset       pagination offset
     * @return matching projects ordered by creation date descending
     */
    List<Project> searchProjects(String gatewayId, String userName, Object searchFields, int limit, int offset);
}
