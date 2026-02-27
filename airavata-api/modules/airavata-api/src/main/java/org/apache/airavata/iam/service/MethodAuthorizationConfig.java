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
package org.apache.airavata.iam.service;

import java.util.HashMap;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "airavata.security.iam", name = "enabled", havingValue = "true")
public class MethodAuthorizationConfig {

    // Methods that users use to manage their user resource profile
    private static final String USER_RESOURCE_PROFILE_USER_METHODS =
            "/airavata/registerUserResourceProfile|/airavata/getUserResourceProfile"
                    + "|/airavata/updateUserResourceProfile|/airavata/deleteUserResourceProfile|/airavata/addUserComputeResourcePreference"
                    + "|/airavata/addUserStoragePreference|/airavata/getUserComputeResourcePreference|/airavata/getUserStoragePreference"
                    + "|/airavata/getAllUserComputeResourcePreferences|/airavata/getAllUserStoragePreferences"
                    + "|/airavata/updateUserComputeResourcePreference|/airavata/updateUserStoragePreference"
                    + "|/airavata/deleteUserComputeResourcePreference|/airavata/deleteUserStoragePreference"
                    + "|/airavata/generateAndRegisterSSHKeys|/airavata/getAllCredentialSummaryForUsersInGateway"
                    + "|/airavata/deleteSSHPubKey|/airavata/isUserResourceProfileExists";
    private static final String SHARING_RESOURCE_METHODS =
            "/airavata/shareResourceWithUsers|/airavata/revokeSharingOfResourceFromUsers"
                    + "|/airavata/shareResourceWithGroups|/airavata/revokeSharingOfResourceFromGroups|/airavata/getAllAccessibleUsers"
                    + "|/airavata/getAllAccessibleGroups|/airavata/userHasAccess|/airavata/getAllDirectlyAccessibleUsers"
                    + "|/airavata/getAllDirectlyAccessibleGroups";
    private static final String SSH_ACCOUNT_PROVISIONER_METHODS =
            "/airavata/getSSHAccountProvisioners|/airavata/doesUserHaveSSHAccount|/airavata"
                    + "/setupUserComputeResourcePreferencesForSSH|"
                    +
                    // getGatewayResourceProfile is needed to look up whether ssh account provisioning is
                    // configured for a gateway's compute resource preference
                    "/airavata/getGatewayResourceProfile";
    // These methods are protected by sharing registry authorization
    private static final String GROUP_RESOURCE_PROFILE_METHODS =
            "/airavata/createGroupResourceProfile|/airavata/updateGroupResourceProfile|/airavata/getGroupResourceProfile"
                    + "|/airavata/removeGroupResourceProfile|/airavata/getGroupResourceList|/airavata/removeGroupComputePrefs"
                    + "|/airavata/removeGroupComputeResourcePolicy|/airavata/removeGroupBatchQueueResourcePolicy"
                    + "|/airavata/getGroupComputeResourcePreference|/airavata/getGroupComputeResourcePolicy"
                    + "|/airavata/getBatchQueueResourcePolicy|/airavata/getGroupComputeResourcePrefList"
                    + "|/airavata/getGroupBatchQueueResourcePolicyList|/airavata/getGroupComputeResourcePolicyList";
    // These methods are protected by sharing registry authorization
    private static final String APPLICATION_DEPLOYMENT_METHODS =
            "/airavata/registerApplicationDeployment|/airavata/getApplicationDeployment|/airavata/updateApplicationDeployment"
                    + "|/airavata/deleteApplicationDeployment|/airavata/getAllApplicationDeployments|/airavata/getAccessibleApplicationDeployments"
                    + "|/airavata/getApplicationDeploymentsForAppModuleAndGroupResourceProfile";
    private static final String APPLICATION_MODULE_METHODS = "/airavata/getAccessibleAppModules";
    private static final String CREDENTIAL_TOKEN_METHODS =
            "/airavata/getCredentialSummary|/airavata/getAllCredentialSummaries|/airavata/generateAndRegisterSSHKeys|/airavata/registerPwdCredential|/airavata/deleteSSHPubKey|/airavata/deletePWDCredential";
    // Misc. other methods needed for group based authorization
    private static final String GROUP_BASED_AUTH_METHODS = "/airavata/getGatewayGroups";
    private static final String INTERMEDIATE_OUTPUTS_METHODS =
            "/airavata/fetchIntermediateOutputs|/airavata/getIntermediateOutputProcessStatus";

    private final HashMap<String, String> rolePermissionConfig = new HashMap<>();

    public MethodAuthorizationConfig() {
        rolePermissionConfig.put("admin", "/airavata/.*");
        rolePermissionConfig.put("gateway-provider", "/airavata/.*");
        rolePermissionConfig.put(
                "admin-read-only",
                "/airavata/getSSHPubKey|/airavata/getAllGatewaySSHPubKeys"
                        + "|/airavata/getAllGatewayPWDCredentials|/airavata/getApplicationModule|/airavata/getAllAppModules"
                        + "|/airavata/getApplicationDeployment|/airavata/getAllApplicationDeployments"
                        + "|/airavata/getStorageResource|/airavata/getAllStorageResourceNames|/airavata/getSCPDataMovement"
                        + "|/airavata/getUnicoreDataMovement|/airavata/getGridFTPDataMovement|/airavata/getResourceJobManager"
                        + "|/airavata/deleteResourceJobManager|/airavata/getGatewayResourceProfile|/airavata/getGatewayComputeResourcePreference"
                        + "|/airavata/getGatewayStoragePreference|/airavata/getAllGatewayComputeResourcePreferences"
                        + "|/airavata/getAllGatewayStoragePreferences|/airavata/getAllGatewayResourceProfiles|/airavata/getAPIVersion"
                        + "|/airavata/getNotification|/airavata/getAllNotifications|/airavata/createProject|/airavata/updateProject"
                        + "|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|/airavata/searchProjectsByProjectName"
                        + "|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|/airavata/searchExperimentsByDesc"
                        + "|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|/airavata/searchExperimentsByCreationTime"
                        + "|/airavata/searchExperiments|/airavata/getExperimentStatistics|/airavata/getExperimentsInProject"
                        + "|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|/airavata/getExperiment"
                        + "|/airavata/updateExperiment|/airavata/updateExperimentConfiguration"
                        + "|/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment"
                        + "|/airavata/getExperimentStatus|/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs"
                        + "|/airavata/getJobStatuses|/airavata/getJobDetails|/airavata/cloneExperiment|/airavata/terminateExperiment"
                        + "|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|/airavata/getAllApplicationInterfaces"
                        + "|/airavata/getApplicationInputs|/airavata/getApplicationOutputs"
                        + "|/airavata/getComputeResource|/airavata/getAllComputeResourceNames|/airavata/getWorkflow|/airavata/getWorkflowTemplateId"
                        + "|/airavata/isWorkflowExistWithName|/airavata/createArtifact|/airavata/getArtifact|/airavata/createReplica"
                        + "|/airavata/getParentArtifact|/airavata/getChildArtifacts|/airavata/getAllAccessibleUsers"
                        + "|/airavata/getExperimentByAdmin|/airavata/cloneExperimentByAdmin"
                        + "|"
                        + USER_RESOURCE_PROFILE_USER_METHODS + "|/airavata/getAllUserResourceProfiles" + "|"
                        + SHARING_RESOURCE_METHODS + "|/airavata/getGateway|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|"
                        + GROUP_RESOURCE_PROFILE_METHODS + "|"
                        + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|"
                        + APPLICATION_MODULE_METHODS + "|"
                        + CREDENTIAL_TOKEN_METHODS + "|" + INTERMEDIATE_OUTPUTS_METHODS);
        rolePermissionConfig.put(
                "gateway-user",
                "/airavata/getAPIVersion|/airavata/getNotification|/airavata/getAllNotifications|"
                        + "/airavata/createProject|/airavata/updateProject|/airavata/getProject|/airavata/deleteProject|/airavata/getUserProjects|"
                        + "/airavata/searchProjectsByProjectName|/airavata/searchProjectsByProjectDesc|/airavata/searchExperimentsByName|"
                        + "/airavata/searchExperimentsByDesc|/airavata/searchExperimentsByApplication|/airavata/searchExperimentsByStatus|"
                        + "/airavata/searchExperimentsByCreationTime|/airavata/searchExperiments|"
                        + "/airavata/getExperimentsInProject|/airavata/getUserExperiments|/airavata/createExperiment|/airavata/deleteExperiment|"
                        + "/airavata/getExperiment|/airavata/updateExperiment|/airavata/updateExperimentConfiguration|"
                        + "/airavata/updateResourceScheduleing|/airavata/validateExperiment|/airavata/launchExperiment|/airavata/getExperimentStatus|"
                        + "/airavata/getExperimentOutputs|/airavata/getIntermediateOutputs|/airavata/getJobStatuses|/airavata/getJobDetails|"
                        + "/airavata/cloneExperiment|/airavata/terminateExperiment|/airavata/getApplicationInterface|/airavata/getAllApplicationInterfaceNames|"
                        + "/airavata/getAllApplicationInterfaces|/airavata/getApplicationInputs|/airavata/getApplicationOutputs|"
                        + "/airavata/getComputeResource|/airavata/getAllComputeResourceNames|"
                        + "/airavata/getWorkflow|/airavata/getWorkflowTemplateId|/airavata/isWorkflowExistWithName|/airavata/createArtifact|"
                        + "/airavata/getArtifact|/airavata/createReplica|/airavata/getParentArtifact|/airavata/getChildArtifacts|"
                        + "/airavata/getAllAccessibleUsers|/airavata/getAllApplicationDeployments|/airavata/getAllAppModules|/airavata/getApplicationModule|"
                        + USER_RESOURCE_PROFILE_USER_METHODS + "|" + SHARING_RESOURCE_METHODS
                        + "|" + SSH_ACCOUNT_PROVISIONER_METHODS + "|" + GROUP_RESOURCE_PROFILE_METHODS + "|"
                        + APPLICATION_DEPLOYMENT_METHODS + "|" + GROUP_BASED_AUTH_METHODS + "|"
                        + APPLICATION_MODULE_METHODS + "|"
                        + CREDENTIAL_TOKEN_METHODS + "|" + INTERMEDIATE_OUTPUTS_METHODS);
    }

    /**
     * Determines whether the given gateway group membership has permission to invoke the specified
     * API method.
     *
     * <p>As a stopgap solution, until all resources are secured with group-based authorization,
     * the Admins and Read Only Admins groups are mapped to the corresponding roles.
     *
     * @param membership  the user's gateway group membership
     * @param apiMethod   the fully-qualified API method path (e.g. {@code /airavata/createProject})
     * @return {@code true} if the membership is permitted to call the method
     */
    public boolean hasPermission(GatewayGroupMembership membership, String apiMethod) {
        final String role;
        if (membership.isInAdminsGroup()) {
            return true;
        } else if (membership.isInReadOnlyAdminsGroup()) {
            role = "admin-read-only";
        } else {
            // If not in Admins or Read Only Admins groups, treat as a gateway-user
            role = "gateway-user";
        }
        var pattern = Pattern.compile(this.rolePermissionConfig.get(role));
        var matcher = pattern.matcher(apiMethod);
        return matcher.matches();
    }

    /**
     * Returns the full role-to-permission-pattern map. Exposed for testing and diagnostics.
     *
     * @return an unmodifiable view of the role permission configuration
     */
    public HashMap<String, String> getRolePermissionConfig() {
        return rolePermissionConfig;
    }

    /**
     * Captures the relevant gateway-group membership flags for a user within a single gateway.
     */
    public static class GatewayGroupMembership {
        private boolean inAdminsGroup = false;
        private boolean inReadOnlyAdminsGroup = false;

        public boolean isInAdminsGroup() {
            return inAdminsGroup;
        }

        public void setInAdminsGroup(boolean inAdminsGroup) {
            this.inAdminsGroup = inAdminsGroup;
        }

        public boolean isInReadOnlyAdminsGroup() {
            return inReadOnlyAdminsGroup;
        }

        public void setInReadOnlyAdminsGroup(boolean inReadOnlyAdminsGroup) {
            this.inReadOnlyAdminsGroup = inReadOnlyAdminsGroup;
        }
    }
}
