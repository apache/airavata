/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.resource.profile.storage.scp.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "SCP_STORAGE_GROUP_RESOURCE_PROFILE")
public class SCPStoGroupResourceProfileEntity {

    @Id
    @Column(name = "SCP_GROUP_RESOURCE_PROFILE_ID")
    private String scpGroupResourceProfileId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "SCP_GROUP_RESOURCE_PROFILE_NAME")
    private String scpGroupResourceProfileName;

    @Column(name = "CREATION_TIME", updatable = false)
    private Long creationTime;

    @Column(name = "UPDATE_TIME")
    private Long updatedTime;

    @Column(name = "DEFAULT_SCP_CREDENTIAL_STORE_TOKEN")
    private String defaultSCPCredentialStoreToken;

    @OneToMany(targetEntity = SCPStoGroupPreferenceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "scpGroupResourceProfileId", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<SCPStoGroupPreferenceEntity> scpStoragePreferences;

    public String getScpGroupResourceProfileId() {
        return scpGroupResourceProfileId;
    }

    public SCPStoGroupResourceProfileEntity setScpGroupResourceProfileId(String scpGroupResourceProfileId) {
        this.scpGroupResourceProfileId = scpGroupResourceProfileId;
        return this;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public SCPStoGroupResourceProfileEntity setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
        return this;
    }

    public String getScpGroupResourceProfileName() {
        return scpGroupResourceProfileName;
    }

    public SCPStoGroupResourceProfileEntity setScpGroupResourceProfileName(String scpGroupResourceProfileName) {
        this.scpGroupResourceProfileName = scpGroupResourceProfileName;
        return this;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public SCPStoGroupResourceProfileEntity setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public SCPStoGroupResourceProfileEntity setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getDefaultSCPCredentialStoreToken() {
        return defaultSCPCredentialStoreToken;
    }

    public SCPStoGroupResourceProfileEntity setDefaultSCPCredentialStoreToken(String defaultSCPCredentialStoreToken) {
        this.defaultSCPCredentialStoreToken = defaultSCPCredentialStoreToken;
        return this;
    }

    public List<SCPStoGroupPreferenceEntity> getScpStoragePreferences() {
        return scpStoragePreferences;
    }

    public SCPStoGroupResourceProfileEntity setScpStoragePreferences(List<SCPStoGroupPreferenceEntity> scpStoragePreferences) {
        this.scpStoragePreferences = scpStoragePreferences;
        return this;
    }
}
