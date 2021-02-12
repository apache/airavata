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

@Entity
// TODO Add PK class
@Table(name = "SCP_STORAGE_GROUP_PREFERENCE")
@IdClass(SCPStoGroupPreferenceId.class)
public class SCPStoGroupPreferenceEntity {

    @Id
    @Column(name = "SCP_STORAGE_ID")
    private String scpStorageId;

    @Id
    @Column(name = "SCP_GROUP_RESOURCE_PROFILE_ID")
    private String scpGroupResourceProfileId;

    @Column(name = "LOGIN_USER_NAME")
    private String loginUserName;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialToken;

    @ManyToOne(targetEntity = SCPStoGroupResourceProfileEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "SCP_GROUP_RESOURCE_PROFILE_ID", insertable = false, updatable = false)
    private SCPStoGroupResourceProfileEntity scpGroupResourceProfile;


    @ManyToOne(targetEntity = SCPStorageEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "SCP_STORAGE_ID", insertable = false, updatable = false)
    private  SCPStorageEntity scpStorage;

    public String getScpStorageId() {
        return scpStorageId;
    }

    public SCPStoGroupPreferenceEntity setScpStorageId(String scpStorageId) {
        this.scpStorageId = scpStorageId;
        return this;
    }

    public String getScpGroupResourceProfileId() {
        return scpGroupResourceProfileId;
    }

    public SCPStoGroupPreferenceEntity setScpGroupResourceProfileId(String scpGroupResourceProfileId) {
        this.scpGroupResourceProfileId = scpGroupResourceProfileId;
        return this;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public SCPStoGroupPreferenceEntity setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
        return this;
    }

    public String getResourceSpecificCredentialToken() {
        return resourceSpecificCredentialToken;
    }

    public SCPStoGroupPreferenceEntity setResourceSpecificCredentialToken(String resourceSpecificCredentialToken) {
        this.resourceSpecificCredentialToken = resourceSpecificCredentialToken;
        return this;
    }

    public SCPStoGroupResourceProfileEntity getScpGroupResourceProfile() {
        return scpGroupResourceProfile;
    }

    public SCPStoGroupPreferenceEntity setScpGroupResourceProfile(SCPStoGroupResourceProfileEntity scpGroupResourceProfile) {
        this.scpGroupResourceProfile = scpGroupResourceProfile;
        return this;
    }

    public SCPStorageEntity getScpStorage() {
        return scpStorage;
    }

    public SCPStoGroupPreferenceEntity setScpStorage(SCPStorageEntity scpStorage) {
        this.scpStorage = scpStorage;
        return this;
    }
}

