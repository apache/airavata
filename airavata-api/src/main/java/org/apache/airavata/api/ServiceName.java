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
