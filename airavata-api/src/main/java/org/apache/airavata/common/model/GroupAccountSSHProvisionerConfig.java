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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: GroupAccountSSHProvisionerConfig
 */
public class GroupAccountSSHProvisionerConfig {
    private String resourceId;
    private String groupResourceProfileId;
    private String configName;
    private String configValue;

    public GroupAccountSSHProvisionerConfig() {}

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupAccountSSHProvisionerConfig that = (GroupAccountSSHProvisionerConfig) o;
        return Objects.equals(resourceId, that.resourceId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(configName, that.configName)
                && Objects.equals(configValue, that.configValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, groupResourceProfileId, configName, configValue);
    }

    @Override
    public String toString() {
        return "GroupAccountSSHProvisionerConfig{" + "resourceId=" + resourceId + ", groupResourceProfileId="
                + groupResourceProfileId + ", configName=" + configName + ", configValue=" + configValue + "}";
    }
}
