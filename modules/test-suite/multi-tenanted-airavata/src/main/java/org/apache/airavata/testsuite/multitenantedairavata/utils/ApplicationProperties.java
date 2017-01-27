package org.apache.airavata.testsuite.multitenantedairavata.utils;

/**
 * Created by Ajinkya on 12/14/16.
 */
public class ApplicationProperties {

    private String applicationModuleId;
    private String applicationInterfaceId;
    private String applicationDeployId;

    public ApplicationProperties(String applicationModuleId, String applicationInterfaceId, String applicationDeployId) {
        this.applicationModuleId = applicationModuleId;
        this.applicationInterfaceId = applicationInterfaceId;
        this.applicationDeployId = applicationDeployId;
    }

    public String getApplicationModuleId() {
        return applicationModuleId;
    }

    public String getApplicationDeployId() {
        return applicationDeployId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationProperties{");
        sb.append("applicationModuleId='").append(applicationModuleId).append('\'');
        sb.append(", applicationInterfaceId='").append(applicationInterfaceId).append('\'');
        sb.append(", applicationDeployId='").append(applicationDeployId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
