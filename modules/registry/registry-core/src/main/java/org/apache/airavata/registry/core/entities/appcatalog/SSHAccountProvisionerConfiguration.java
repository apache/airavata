/**
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
 */

package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;

/**
 * The persistent class for the ssh_account_provisioner_config database table.
 *
 */
@Entity
@Table(name = "SSH_ACCOUNT_PROVISIONER_CONFIG")
@IdClass(SSHAccountProvisionerConfigurationPK.class)
public class SSHAccountProvisionerConfiguration {
    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;
    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;
    @Id
    @Column(name = "CONFIG_NAME")
    private String configName;

    @Column(name = "CONFIG_VALUE")
    private String configValue;

    @ManyToOne(targetEntity = ComputeResourcePreferenceEntity.class, cascade= CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", nullable = false),
            @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID", nullable = false)
    })
    private ComputeResourcePreferenceEntity computeResourcePreference;

    public SSHAccountProvisionerConfiguration() {}

    public SSHAccountProvisionerConfiguration(String configName, String configValue, ComputeResourcePreferenceEntity computeResourcePreference) {
        this.gatewayId = computeResourcePreference.getGatewayId();
        this.resourceId = computeResourcePreference.getComputeResourceId();
        this.configName = configName;
        this.configValue = configValue;
        this.computeResourcePreference = computeResourcePreference;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public ComputeResourcePreferenceEntity getComputeResourcePreference() {
        return computeResourcePreference;
    }

    public void setComputeResourcePreference(ComputeResourcePreferenceEntity computeResourcePreference) {
        this.computeResourcePreference = computeResourcePreference;
    }
}
