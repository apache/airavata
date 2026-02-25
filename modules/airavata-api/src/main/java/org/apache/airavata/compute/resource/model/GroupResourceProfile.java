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
package org.apache.airavata.compute.resource.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal stub for GroupResourceProfile.
 * Temporary placeholder pending pipeline rewrite.
 */
public class GroupResourceProfile {

    private String groupResourceProfileId;
    private String gatewayId;
    private String groupResourceProfileName;
    private String defaultCredentialStoreToken;
    private List<GroupComputeResourcePreference> computePreferences;

    public GroupResourceProfile() {}

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGroupResourceProfileName() {
        return groupResourceProfileName;
    }

    public void setGroupResourceProfileName(String groupResourceProfileName) {
        this.groupResourceProfileName = groupResourceProfileName;
    }

    public String getDefaultCredentialStoreToken() {
        return defaultCredentialStoreToken;
    }

    public void setDefaultCredentialStoreToken(String defaultCredentialStoreToken) {
        this.defaultCredentialStoreToken = defaultCredentialStoreToken;
    }

    public List<GroupComputeResourcePreference> getComputePreferences() {
        if (computePreferences == null) {
            computePreferences = new ArrayList<>();
        }
        return computePreferences;
    }

    public void setComputePreferences(List<GroupComputeResourcePreference> computePreferences) {
        this.computePreferences = computePreferences;
    }

}
