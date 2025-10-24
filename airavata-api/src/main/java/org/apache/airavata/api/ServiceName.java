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
package org.apache.airavata.api;

import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;

public enum ServiceName {
    AIRAVATA_API("AiravataAPI"),
    REGISTRY("RegistryAPI"),
    CREDENTIAL_STORE("CredentialStore"),
    SHARING_REGISTRY("SharingRegistry"),
    ORCHESTRATOR("Orchestrator"),
    USER_PROFILE(profile_user_cpiConstants.USER_PROFILE_CPI_NAME),
    TENANT_PROFILE(profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME),
    IAM_ADMIN_SERVICES(iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME),
    GROUP_MANAGER(group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME);

    private final String serviceName;

    ServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
