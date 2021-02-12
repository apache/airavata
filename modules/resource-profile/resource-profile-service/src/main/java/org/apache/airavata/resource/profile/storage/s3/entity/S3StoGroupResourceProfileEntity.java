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
package org.apache.airavata.resource.profile.storage.s3.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "S3_STORAGE_GROUP_RESOURCE_PROFILE")
public class S3StoGroupResourceProfileEntity {

    @Id
    @Column(name = "S3_GROUP_RESOURCE_PROFILE_ID")
    private String s3GroupResourceProfileId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "S3_GROUP_RESOURCE_PROFILE_NAME")
    private String s3GroupResourceProfileName;

    @Column(name = "CREATION_TIME", updatable = false)
    private Long creationTime;

    @Column(name = "UPDATE_TIME")
    private Long updatedTime;

    @Column(name = "DEFAULT_S3_CREDENTIAL_STORE_TOKEN")
    private String defaultS3CredentialStoreToken;

    @OneToMany(targetEntity = S3StoGroupPreferenceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "s3GroupResourceProfileId", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<S3StoGroupPreferenceEntity> s3StoragePreferences;

    public String getS3GroupResourceProfileId() {
        return s3GroupResourceProfileId;
    }

    public S3StoGroupResourceProfileEntity setS3GroupResourceProfileId(String s3GroupResourceProfileId) {
        this.s3GroupResourceProfileId = s3GroupResourceProfileId;
        return this;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public S3StoGroupResourceProfileEntity setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
        return this;
    }

    public String getS3GroupResourceProfileName() {
        return s3GroupResourceProfileName;
    }

    public S3StoGroupResourceProfileEntity setS3GroupResourceProfileName(String s3GroupResourceProfileName) {
        this.s3GroupResourceProfileName = s3GroupResourceProfileName;
        return this;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public S3StoGroupResourceProfileEntity setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public S3StoGroupResourceProfileEntity setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getDefaultS3CredentialStoreToken() {
        return defaultS3CredentialStoreToken;
    }

    public S3StoGroupResourceProfileEntity setDefaultS3CredentialStoreToken(String defaultS3CredentialStoreToken) {
        this.defaultS3CredentialStoreToken = defaultS3CredentialStoreToken;
        return this;
    }

    public List<S3StoGroupPreferenceEntity> getS3StoragePreferences() {
        return s3StoragePreferences;
    }

    public S3StoGroupResourceProfileEntity setS3StoragePreferences(List<S3StoGroupPreferenceEntity> s3StoragePreferences) {
        this.s3StoragePreferences = s3StoragePreferences;
        return this;
    }
}

