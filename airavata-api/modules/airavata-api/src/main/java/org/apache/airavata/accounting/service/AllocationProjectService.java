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
package org.apache.airavata.accounting.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.accounting.model.AllocationProject;

public interface AllocationProjectService {

    AllocationProject getAllocationProject(String allocationProjectId);

    List<AllocationProject> getAllocationProjects(String gatewayId);

    List<AllocationProject> getAllocationProjectsByResource(String resourceId);

    AllocationProject findByProjectCodeAndResource(String projectCode, String resourceId);

    String createAllocationProject(AllocationProject project);

    void deleteAllocationProject(String allocationProjectId);

    void syncFromBinding(
            String bindingId, String resourceId, String gatewayId, String credentialId, Map<String, Object> metadata);

    void cleanupForBinding(String bindingId);

    List<String> getProjectCredentials(String allocationProjectId);

    /**
     * Alias for {@link #getProjectCredentials(String)} matching the REST controller method name.
     */
    default List<String> getProjectMembers(String allocationProjectId) {
        return getProjectCredentials(allocationProjectId);
    }
}
